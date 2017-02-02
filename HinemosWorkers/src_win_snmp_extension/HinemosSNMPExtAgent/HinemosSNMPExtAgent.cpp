/*
 
Copyright (C) 2015 NTT DATA Corporation
 
This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License 
as published by the Free Software Foundation, version 2.
 
This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied 
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
PURPOSE.  See the GNU General Public License for more details.
 
*/

//
// Hinemos SNMP 拡張エージェントDLL for Hinemos ver. 5.1
//

// -------------------------------------------------------------
//    インクルードファイル
// -------------------------------------------------------------
#include <windows.h>
#include <stdio.h>
#include <tchar.h>
#include <snmp.h>
#include <winbase.h>
#include "HinemosSNMPExtAgent.h"

#include <bitset>
#include <string>
#include <map>
#include <vector>
#include <iostream>

#define SNMPEXTAGT_REGISTRY_KEY "SOFTWARE\\HinemosAgent\\SNMPExtAgent"

using namespace std;

// -------------------------------------------------------------
//    関数宣言
// -------------------------------------------------------------
UINT ResolveVarBind( SnmpVarBind *, UINT );
UINT OperateVarBind( UINT, int, SnmpVarBind * );

VOID PostEvtLog(WORD, LPTSTR, char *, long );
VOID PostEvtLogDouble( WORD logType, LPTSTR ModuleName, char *SendMsg, double val );
VOID PostEvtLogDWORDLONG(WORD, LPTSTR, char *, DWORDLONG );
VOID PostEvtLogChar( WORD logType, LPTSTR ModuleName, char *SendMsg, char c, long val);
VOID PostEvtLogString( WORD logType, LPTSTR ModuleName, char *SendMsg, char *str, long val);

VOID GetMibData(MIB_ENTRY *);

AsnCounter GetCpuRawUser(void);
AsnCounter GetCpuRawNice(void);
AsnCounter GetCpuRawSystem(void);
AsnCounter GetCpuRawIdle(void);
AsnCounter GetCpuRawWait(void);
AsnCounter GetCpuRawKernel(void);
AsnCounter GetCpuRawInterrupt(void);

AsnCounter GetRawInterrupts(void);
AsnCounter GetRawContexts(void);

AsnInteger GetMemTotalFree(void);
AsnInteger GetMemTotalReal(void);
AsnInteger GetMemTotalSwap(void);
AsnInteger GetMemAvailReal(void);
AsnInteger GetMemAvailSwap(void);

AsnInteger GetMemBuffer(void);
AsnInteger GetMemCached(void);


AsnInteger GetSsSwapIn(void);
AsnInteger GetSsSwapOut(void);

AsnInteger GetLaLoadInt1(void);
AsnInteger GetLaLoadInt2(void);
AsnInteger GetLaLoadInt3(void);

AsnInteger GetDiskIOIndex(int);
char *	   GetDiskIODevice(char *);
AsnCounter GetDiskIORead(char *);
AsnCounter GetDiskIOWrite(char *);
AsnCounter GetDiskIOReads(char *);
AsnCounter GetDiskIOWrites(char *);

char *     GetDiskPath(char *);
AsnInteger GetDiskPercent(char *);

VOID CreateDataStructure(void);
VOID SetEntry(UINT, UINT *, BYTE, void *, UINT, UINT, int);

long __ReadRegDword(HKEY hReg, const char *szKey);
int __ReadRegMultiSz(HKEY hReg, const char *szKey, long* values, int valuesSize);

int writeLog(char *	);

// for debug
char *g_szAbout = NULL;
//

// 各取得値を出力するかを示すフラグ（0:出力しない / 1:出力する）
int verbose = 0;

// APIエラー時に出力するイベントログのイベントレベル
WORD errorEventLevel = EVENTLOG_ERROR_TYPE;


// 無視するエラーコードの配列
long ignoreErrorCode[1024];
int ignoreErrorCodeSize;


/* DLLの初期化 */
BOOL WINAPI DllMain(HANDLE hDll, DWORD  dwReason, LPVOID lpReserved )
{

    switch( dwReason ){
        case DLL_PROCESS_ATTACH:
        case DLL_PROCESS_DETACH:
        case DLL_THREAD_ATTACH:
		case DLL_THREAD_DETACH:
        default:
            break;
    }

    return TRUE;
}

// 設定をレジストリから取得する
void GetRegistrySettings(){
	HKEY	hReg;
	long	rc = 0;

	rc = RegOpenKeyEx(HKEY_LOCAL_MACHINE, SNMPEXTAGT_REGISTRY_KEY, 0, KEY_READ, &hReg);
	if( rc == ERROR_SUCCESS ){
		// 無視すべきエラーコード
		ignoreErrorCodeSize = __ReadRegMultiSz(hReg, "IgnoreErrorCode", ignoreErrorCode, 1024) ;

		// 詳細ロギング
		verbose = __ReadRegDword(hReg, "Verbose") ;

		// APIエラー時イベントレベル
		long errorLevel = __ReadRegDword(hReg, "ErrorEventLevel");
		errorEventLevel = (errorLevel == -1) ? EVENTLOG_ERROR_TYPE : (WORD)errorLevel;
	}
	RegCloseKey(hReg);
}

// エラーコードに対してイベントを出力する必要があるか否かを判定する
bool CheckErrorCode(long code){
	// 成功ならfalse
	if(code == ERROR_SUCCESS || code == ERROR_IO_PENDING){
		return false;
	}

	for(int i=0; i<ignoreErrorCodeSize; i++){
		// 無視すべきエラーコードならfalse (-1の場合は何もチェックせずfalse)
		if(ignoreErrorCode[i] == code || ignoreErrorCode[i] == -1){
			return false;
		}
	}
	
	return true;
}

/* DLLがロードされた際に、SNMP Serviceから呼出され、初期化処理を行う。 */
BOOL WINAPI SnmpExtensionInit(IN  DWORD dwTimeZeroReference,
							    OUT HANDLE *hPollForTrapEvent,
								OUT AsnObjectIdentifier *supportedView )
{

	// システムイベント作成用のパラメータ
	SECURITY_ATTRIBUTES	sa;
	SECURITY_DESCRIPTOR	sd;
	sa.nLength = sizeof( SECURITY_ATTRIBUTES );
	sa.bInheritHandle = TRUE;
	sa.lpSecurityDescriptor = &sd;

	// "初期化開始"のEventLogを書出す
	PostEvtLog( EVENTLOG_INFORMATION_TYPE, "ExtensionInit", "Initialization Start", 0 );

    // セキュリティ情報初期化
	if( !InitializeSecurityDescriptor( &sd, SECURITY_DESCRIPTOR_REVISION )){
		PostEvtLog( EVENTLOG_ERROR_TYPE, "ExtensionInit", "Security Initialization Error", 0 );
		return FALSE;
	}
    // セキュリティ情報設定
	if( !SetSecurityDescriptorDacl( &sd,TRUE, (PACL)NULL, FALSE )){
		PostEvtLog( EVENTLOG_ERROR_TYPE, "ExtensionInit", "Security Settings Error", 0 );
		return FALSE;
	}
    // Trap関数を起動するためのシステムイベントを作成
	*hPollForTrapEvent = CreateEvent(&sa, FALSE, FALSE, TRAP_EVENT_NAME );
	if((*hPollForTrapEvent) == NULL ){
		PostEvtLog( EVENTLOG_ERROR_TYPE, "ExtensionInit", "Event Generation Error", 0 );
		return FALSE;
    }
    // 呼び元指定の時刻調整パラメータを保存
    dwTimeZero = dwTimeZeroReference;

    // 本DLLの処理対象となるMIBのOIDを設定
    *supportedView = MIB_OidPrefix;

	// Trap関数を起動するためのシステムイベントを保存

//	Trapについては実装しない
//	hSimulateTrap = *hPollForTrapEvent;

	// 設定をレジストリから取得する
	GetRegistrySettings();

	//必要なデータ構造を作成する
	CreateDataStructure();

	// "初期化完了"のEventLogを書出す
	PostEvtLog( EVENTLOG_INFORMATION_TYPE, "ExtensionInit", "Initialization Completed", 0 );

    return TRUE;
}


/*
 * 本DLLの処理対象となるMIBに対し、SNMPマネージャからのGet/GetNext/Setが要求された際に、
 * SNMP Serviceから呼出され要求に応じた処理を行う。
 */
BOOL WINAPI SnmpExtensionQuery(IN BYTE requestType, IN OUT SnmpVarBindList *variableBindings,
							   OUT AsnInteger32 *errorStatus, OUT AsnInteger32 *errorIndex )
{
    static ULONG requestCount = 0;
    UINT    i;

    // 変数の数分処理を繰り返す
    for( i=0; i<(variableBindings->len); i++ ){

        // リクエストと変数に対する処理
        *errorStatus = ResolveVarBind( &variableBindings->list[i], requestType );

        // エラーの場合の処理
        if( *errorStatus != SNMP_ERRORSTATUS_NOERROR ){
            *errorIndex = i + 1;
            break;
        }
    }
    return( SNMPAPI_NOERROR );
}

/*
 * リクエストのあったOIDに対して、レスポンスを返すOIDを設定する
 */
UINT ResolveVarBind(IN OUT SnmpVarBind *VarBind, IN UINT PduAction )
{

    UINT                 nResult;
	int                  index = -1;
	bool                 noSuchName = true;

	UINT size = mib_entries.size();

	//GETNEXTでの問い合わせでは、
	//VarBindに、返り値と、次に参照するOIDをセットしてreturnする。
	if (PduAction == SNMP_PDU_GETNEXT) {
		
		//登録されているMIBの一覧と、順にOIDを比較する
		for (UINT i = 0; i < size ; i++) {
			int compResult = SnmpUtilOidCmp(&VarBind->name, &mib_entries[i].Oid);

			// 問い合わせのあったOIDとリストがマッチしない場合は、
			// 深さ優先探索で、問い合わせOID < リストのOIDとなる最初のOIDについて、性能値を取得する。
			if (compResult < 0) {

				if( PduAction != SNMP_PDU_GETNEXT ){
					return( SNMP_ERRORSTATUS_NOSUCHNAME );
				}

				noSuchName = false;

				// 検索の結果、値を取得するOID
				PduAction = SNMP_PDU_GET;
				index = i;

				// 変数を一旦解放し、OIDを設定
				SnmpUtilOidFree( &VarBind->name );
				SnmpUtilOidCpy( &VarBind->name, &mib_entries[i].Oid );

				break;

			// 問い合わせのあったOIDが存在した場合
			} else if (compResult == 0) {
				PduAction = SNMP_PDU_GET;
				index = i;
				noSuchName = false;
				SnmpUtilOidFree( &VarBind->name );
				
				// リストの末尾であれば、次のOIDはNULLとして返す
				if (i != size - 1){
					//次のOIDが存在する場合はそれを設定する
					index++;
					SnmpUtilOidCpy( &VarBind->name, &mib_entries[index].Oid );
					break;
				}
			}
		}

	// GETでの問い合わせでは、リストを検索して、存在したら性能値を取得する。
	} else if (PduAction == SNMP_PDU_GET) {

		//map経由で情報を取得
		string requestOid(SnmpUtilOidToA(&VarBind->name));
		map<string, int>::iterator itr = oid_index_map.find(requestOid);

		//存在したらフラグを切り替える
		if (itr != oid_index_map.end()) {
			noSuchName = false;
			index = (*itr).second;
		}
	}

	if (noSuchName) {
        return( SNMP_ERRORSTATUS_NOSUCHNAME );
    }

    // 処理関数を呼び出す
    nResult = OperateVarBind( PduAction, index, VarBind );

    return( nResult );
}

/*
 * リクエストに対する返り値を設定する。
 */
UINT OperateVarBind(IN UINT Action, IN int index, IN SnmpVarBind *VarBind )
{
//	UINT ErrStat;

	UINT next_index = index + 1;
	MIB_ENTRY *MibPtr = &mib_entries[index];
//	MIB_ENTRY *NextMibPtr = NULL;

	switch( Action ){
/*
	case SNMP_PDU_GETNEXT:
		
		//次のOIDをセットする
		if (index < oid_index_map.size() - 1) {
			 NextMibPtr = &mib_entries[next_index];

		//リスト最後尾のOIDだった場合
		} else if (index == oid_index_map.size() - 1) {
			 NextMibPtr = NULL;

		// 該当するOIDが見つからなかった場合、returnする
		} else {
			return( SNMP_ERRORSTATUS_NOSUCHNAME );
		}

		//次に見るべきOIDをセットする
        SnmpUtilOidFree( &VarBind->name );
		SnmpUtilOidCpy( &VarBind->name, &NextMibPtr->Oid );

	    // 処理関数を呼び出す (3.2) ※再帰呼出
        ErrStat = OperateVarBind( SNMP_PDU_GET, index, VarBind );
        return( ErrStat );
*/
	case SNMP_PDU_GET:
        if(( MibPtr->Access != MIB_ACCESS_READONLY  ) &&
           ( MibPtr->Access != MIB_ACCESS_READWRITE )){
            return( SNMP_ERRORSTATUS_NOSUCHNAME );
	    }

		//リソース情報を取得
		GetMibData(MibPtr);

        VarBind->value.asnType = MibPtr->Type;
        switch( VarBind->value.asnType ){
            case ASN_RFC1155_COUNTER:
            case ASN_RFC1155_GAUGE:
            case ASN_INTEGER:
                VarBind->value.asnValue.number = *(AsnInteger *)(MibPtr->Storage);
                break;

            case ASN_OCTETSTRING:
				
				// octet数をセット
				//VarBind->value.asnValue.string.length = ((AsnOctetString *)(LPSTR)MibPtr->Storage)->length;
				VarBind->value.asnValue.string.length = strlen((LPSTR)MibPtr->Storage);

				if (verbose == 1) {
				}

				//VarBind->value.asnValue.string.length = 1;

				// メモリ確保
				VarBind->value.asnValue.string.stream = (unsigned char *)SnmpUtilMemAlloc( VarBind->value.asnValue.string.length * sizeof(char) );

				if( VarBind->value.asnValue.string.stream == NULL ){
					return( SNMP_ERRORSTATUS_GENERR );
				}

				// ポインタの情報をコピーする
				memcpy( VarBind->value.asnValue.string.stream, (LPSTR)MibPtr->Storage, VarBind->value.asnValue.string.length);

				VarBind->value.asnValue.string.dynamic = TRUE;
				
                break;

            default:
                return( SNMP_ERRORSTATUS_GENERR );
        }
        break;

/*
	case SNMP_PDU_SET:
        if( MibPtr->Access != MIB_ACCESS_READWRITE ){
            return( SNMP_ERRORSTATUS_NOSUCHNAME );
        }

        if( MibPtr->Type != VarBind->value.asnType ){
            return( SNMP_ERRORSTATUS_BADVALUE );
        }

        switch( VarBind->value.asnType ){
            case ASN_RFC1155_COUNTER:
            case ASN_RFC1155_GAUGE:
            case ASN_INTEGER:
                if(( VarBind->value.asnValue.number != MIB_CQPUB_ON  ) &&
                   ( VarBind->value.asnValue.number != MIB_CQPUB_OFF )){
                    return( SNMP_ERRORSTATUS_BADVALUE );
                }
                *(AsnInteger *)(MibPtr->Storage) = 
                    VarBind->value.asnValue.number;
                break;

            case ASN_OCTETSTRING:
                memcpy( (LPSTR)MibPtr->Storage,
                    VarBind->value.asnValue.string.stream,
                    VarBind->value.asnValue.string.length );
                wkLen = VarBind->value.asnValue.string.length;
                ((LPSTR)MibPtr->Storage)[wkLen] = '\0';
                break;

            default:
                return( SNMP_ERRORSTATUS_GENERR );
        }
        break;
*/
    default:
        return( SNMP_ERRORSTATUS_GENERR );
    }
    return( SNMP_ERRORSTATUS_NOERROR );
}

/* レジストリ参照用関数 */
long __ReadRegDword(HKEY hReg, const char *szKey)
{
    LONG    rc;
    DWORD   val, typ;
    DWORD   size = sizeof(val);

    rc = RegQueryValueEx(hReg, szKey, NULL, &typ, (BYTE*)&val, &size) ;
    if( (rc == ERROR_SUCCESS) && (typ == REG_DWORD) ){
        return  (long)val ;
	}

	// Failure: Type or Value does not exist!
    return  -1L;
}

/* レジストリ参照用関数 */
int __ReadRegMultiSz(HKEY hReg, const char *szKey, long* values, int valuesSize)
{
    LONG  rc = 0;
    DWORD typ;
    DWORD size = 0;
	LPTSTR lpValues = NULL;
	LPTSTR lpValue = NULL;
	int i=0;

	// Get size of the buffer for the values
    rc = RegQueryValueEx(hReg, szKey, NULL, NULL, NULL, &size) ;
    if(rc == ERROR_SUCCESS){
		// Allocate the buffer
		lpValues = (LPTSTR)malloc(size);
		if (lpValues == NULL) {
			return 0;
		}

		// Get the values
		rc = RegQueryValueEx(hReg, szKey, NULL, &typ, (LPBYTE)lpValues, &size);
		if( (rc == ERROR_SUCCESS) && (typ == REG_MULTI_SZ) ){
			lpValue = lpValues;

			for (i=0; '\0' != *lpValue && i < valuesSize; lpValue += _tcslen(lpValue) + 1)
			{
				// 10進数または16進数表記の文字列をlong形に変換する
				// （意図的にunsignedとすることでオーバーフローによる丸め誤差の発生を防ぎ、PDH_STATUSとの整合性を合わせている）
				values[i] = _tcstoul(lpValue, NULL, 0);
				i++;
			}
		}
		free(lpValues);
		return i;
	}

	return 0;
}

/* Windowsイベント出力用関数(long/DWORD用) */
VOID PostEvtLog( WORD logType, LPTSTR ModuleName, char *SendMsg, long val )
{
    CHAR    Msg[512];			// メッセージ
    HANDLE  hEventLog;          // イベント・ログのハンドル
	LPCSTR  Strings[2];         // イベント・ログへの記載文字列
    WORD    EventCat = 0;       // イベント・カテゴリ
    DWORD   EventID = 1000;     // イベント識別子
    char    tmpbf[256];

    // イベント・ログに情報を記録
	hEventLog = RegisterEventSource(NULL, "Hinemos SNMP Ext Agent");

    if( hEventLog != NULL ){
		strcpy( Msg, "Hinemos SNMP Ext Agent : ");
		sprintf_s( tmpbf, _countof(tmpbf), "%s %s %ld(0x%lx)", ModuleName, SendMsg, val, val );
	    strcat( Msg, tmpbf );
		Strings[0] = Msg;

		ReportEvent( hEventLog, logType, EventCat, EventID, NULL, 1, 0, Strings, NULL );
		DeregisterEventSource( hEventLog );
    }
	return;
}

/* Windowsイベント出力用関数(double用) */
VOID PostEvtLogDouble( WORD logType, LPTSTR ModuleName, char *SendMsg, double val )
{
    CHAR    Msg[512];			// メッセージ
    HANDLE  hEventLog;          // イベント・ログのハンドル
	LPCSTR  Strings[2];         // イベント・ログへの記載文字列
    WORD    EventCat = 0;       // イベント・カテゴリ
    DWORD   EventID = 1000;     // イベント識別子
    char    tmpbf[256];
  
    // イベント・ログに情報を記録
	hEventLog = RegisterEventSource(NULL, "Hinemos SNMP Ext Agent");

    if( hEventLog != NULL ){
		strcpy( Msg, "Hinemos SNMP Ext Agent : ");
        sprintf_s( tmpbf, _countof(tmpbf), "%s %s %lf", ModuleName, SendMsg, val );
	    strcat( Msg, tmpbf );
		Strings[0] = Msg;

		ReportEvent( hEventLog, logType, EventCat, EventID, NULL, 1, 0, Strings, NULL );
		DeregisterEventSource( hEventLog );
    }
	return;
}

/* Windowsイベント出力用関数(DWORDLONG用) */
VOID PostEvtLogDWORDLONG( WORD logType, LPTSTR ModuleName, char *SendMsg, DWORDLONG val )
{
    CHAR    Msg[512];			// メッセージ
    HANDLE  hEventLog;          // イベント・ログのハンドル
	LPCSTR  Strings[2];         // イベント・ログへの記載文字列
    WORD    EventCat = 0;       // イベント・カテゴリ
    DWORD   EventID = 1000;     // イベント識別子
    char    tmpbf[256];
  
    // イベント・ログに情報を記録
	hEventLog = RegisterEventSource(NULL, "Hinemos SNMP Ext Agent");

    if( hEventLog != NULL ){
		strcpy( Msg, "Hinemos SNMP Ext Agent : ");
        sprintf_s( tmpbf, _countof(tmpbf), "%s %s %llu", ModuleName, SendMsg, val );
	    strcat( Msg, tmpbf );
		Strings[0] = Msg;

		ReportEvent( hEventLog, logType, EventCat, EventID, NULL, 1, 0, Strings, NULL );
		DeregisterEventSource( hEventLog );
    }
	return;
}

/* Windowsイベント出力用関数(char用) */
VOID PostEvtLogChar( WORD logType, LPTSTR ModuleName, char *SendMsg, char c, long val)
{
    CHAR    Msg[512];			// メッセージ
    HANDLE  hEventLog;          // イベント・ログのハンドル
	LPCSTR  Strings[2];         // イベント・ログへの記載文字列
    WORD    EventCat = 0;       // イベント・カテゴリ
    DWORD   EventID = 1000;     // イベント識別子
    char    tmpbf[256];

    // イベント・ログに情報を記録
	hEventLog = RegisterEventSource(NULL, "Hinemos SNMP Ext Agent");

    if( hEventLog != NULL ){
		strcpy( Msg, "Hinemos SNMP Ext Agent : ");
		sprintf_s( tmpbf, _countof(tmpbf), "%s %s(%c) %ld(0x%lx)", ModuleName, SendMsg, c, val, val );
	    strcat( Msg, tmpbf );
		Strings[0] = Msg;

		ReportEvent( hEventLog, logType, EventCat, EventID, NULL, 1, 0, Strings, NULL );
		DeregisterEventSource( hEventLog );
    }
	return;
}

/* Windowsイベント出力用関数(string用) */
VOID PostEvtLogString( WORD logType, LPTSTR ModuleName, char *SendMsg, char *str, long val)
{
    CHAR    Msg[512];			// メッセージ
    HANDLE  hEventLog;          // イベント・ログのハンドル
	LPCSTR  Strings[2];         // イベント・ログへの記載文字列
    WORD    EventCat = 0;       // イベント・カテゴリ
    DWORD   EventID = 1000;     // イベント識別子
    char    tmpbf[256];

    // イベント・ログに情報を記録
	hEventLog = RegisterEventSource(NULL, "Hinemos SNMP Ext Agent");

    if( hEventLog != NULL ){
		strcpy( Msg, "Hinemos SNMP Ext Agent : ");
		sprintf_s( tmpbf, _countof(tmpbf), "%s %s(%s) %ld(0x%lx)", ModuleName, SendMsg, str, val, val );
	    strcat( Msg, tmpbf );
		Strings[0] = Msg;

		ReportEvent( hEventLog, logType, EventCat, EventID, NULL, 1, 0, Strings, NULL );
		DeregisterEventSource( hEventLog );
    }
	return;
}

/*
 * ライブラリへのアクセス関数
 * OIDに基づいて適切な関数を呼びだし、情報をセットする
 */
void GetMibData(IN OUT MIB_ENTRY *mib)
{

	//振り分けロジックを実装
	int id = mib->ResourceId;

	//diskIO用
	char drive;
	int driveIndex;

	string oid_string(SnmpUtilOidToA(&mib->Oid));
	AsnCounter value;
	char *device;
	char *octetString;

	map<string, char>::iterator itr1;
	map<string, AsnCounter>::iterator itr2;
	map<string, char>::iterator itr3;
	map<string, int>::iterator itr4;
	map<string, AsnInteger>::iterator itr5;
	map<string, string>:: iterator itr6;

	switch(id){
		case MIB_DISK_IO_INDEX:
			//ドライブINDEXの取得
			itr4 = oid_driveindex.find(oid_string);
			if (&itr4 != NULL) {
				driveIndex = (*itr4).second;
			}
			break;
		case MIB_DISK_IO_DEVICE:
			//ドライブ文字の取得
			itr1 = oid_drivestring.find(oid_string);
			//存在したらフラグを切り替える
			if (&itr1 != NULL) {
				drive = (*itr1).second;
			}
			itr3 = MIB_dat_diskIODevice.find(oid_string);
			if (&itr3 != NULL) {
				device = &(*itr3).second;
			}
			break;
		case MIB_DISK_IO_READ:
		case MIB_DISK_IO_WRITE:
		case MIB_DISK_IO_READS:
		case MIB_DISK_IO_WRITES:
		case MIB_DISK_PERCENT:
			//ドライブ文字の取得
			itr1 = oid_drivestring.find(oid_string);
			//存在したらフラグを切り替える
			if (&itr1 != NULL) {
				drive = (*itr1).second;
			}

			//収集値をセットする変数を取得
			itr2 = MIB_dat_diskIO.find(oid_string);
			//存在したらフラグを切り替える
			if (&itr2 != NULL) {
				value = (*itr2).second;
			}
			break;
		case MIB_DISK_PATH:
			//ドライブ文字の取得
			itr1 = oid_drivestring.find(oid_string);
			//存在したらフラグを切り替える
			if (&itr1 != NULL) {
				drive = (*itr1).second;
			}
			itr6 = MIB_dat_diskPath.find(oid_string);
			//収集値をセットする変数を取得
			if (&itr6 != NULL) {
				string stringTemp = (*itr6).second;
				octetString = (char*)stringTemp.c_str();
			}
			break;
	}

	
	switch(id){
		//返り値には値へのポインタを設定するため、
		//値の保持する変数はグローバル変数である必要がある
		//戻り値を代入して、ポインタをmibにセットする

		case MIB_CPU_RAW_USER:
			MIB_dat_ssCpuRawUser = GetCpuRawUser();
			mib->Storage = &MIB_dat_ssCpuRawUser;
			break;

		case MIB_CPU_RAW_NICE:
			MIB_dat_ssCpuRawNice = GetCpuRawNice();
			mib->Storage = &MIB_dat_ssCpuRawNice;
			break;

		case MIB_CPU_RAW_SYSTEM:
			MIB_dat_ssCpuRawSystem = GetCpuRawSystem();
			mib->Storage = &MIB_dat_ssCpuRawSystem;
			break;

		case MIB_CPU_RAW_IDLE:
			MIB_dat_ssCpuRawIdle = GetCpuRawIdle();
			mib->Storage = &MIB_dat_ssCpuRawIdle;
			break;

		case MIB_CPU_RAW_WAIT:
			MIB_dat_ssCpuRawWait = GetCpuRawWait();
			mib->Storage = &MIB_dat_ssCpuRawWait;
			break;

		case MIB_CPU_RAW_KERNEL:
			MIB_dat_ssCpuRawKernel = GetCpuRawKernel();
			mib->Storage = &MIB_dat_ssCpuRawKernel;
			break;

		case MIB_CPU_RAW_INTERRUPT:
			MIB_dat_ssRawInterrupts = GetCpuRawInterrupt();
			mib->Storage = &MIB_dat_ssRawInterrupts;
			break;

		case MIB_RAW_INTERRUPTS:
			MIB_dat_ssCpuRawInterrupt = GetRawInterrupts();
			mib->Storage = &MIB_dat_ssCpuRawInterrupt;
			break;

		case MIB_RAW_CONTEXTS:
			MIB_dat_ssRawContexts = GetRawContexts();
			mib->Storage = &MIB_dat_ssRawContexts;
			break;

		case MIB_MEM_TOTAL_REAL:
			MIB_dat_memTotalReal = GetMemTotalReal();
			mib->Storage = &MIB_dat_memTotalReal;
			break;

		case MIB_MEM_TOTAL_SWAP:
			MIB_dat_memTotalSwap = GetMemTotalSwap();
			mib->Storage = &MIB_dat_memTotalSwap;
			break;

		case MIB_MEM_AVAIL_REAL:
			MIB_dat_memAvailReal = GetMemAvailReal();
			mib->Storage = &MIB_dat_memAvailReal;
			break;

		case MIB_MEM_AVAIL_SWAP:
			MIB_dat_memAvailSwap = GetMemAvailSwap();
			mib->Storage = &MIB_dat_memAvailSwap;
			break;

		case MIB_MEM_TOTAL_FREE:
			MIB_dat_memTotalFree = GetMemTotalFree();
			mib->Storage = &MIB_dat_memTotalFree;
			break;

		case MIB_MEM_BUFFER:
			MIB_dat_memBuffer = GetMemBuffer();
			mib->Storage = &MIB_dat_memBuffer;
			break;

		case MIB_MEM_CACHED:
			MIB_dat_memCached = GetMemCached();
			mib->Storage = &MIB_dat_memCached;
			break;

		case MIB_SS_SWAP_IN:
			MIB_dat_ssSwapIn = GetSsSwapIn();
			mib->Storage = &MIB_dat_ssSwapIn;
			break;

		case MIB_SS_SWAP_OUT:
			MIB_dat_ssSwapOut = GetSsSwapOut();
			mib->Storage = &MIB_dat_ssSwapOut;
			break;

		case MIB_DISK_IO_INDEX:
			value = GetDiskIOIndex(driveIndex);
			mib->Storage = &value;
			break;

		case MIB_DISK_IO_DEVICE:
			device = GetDiskIODevice(&drive);
			mib->Storage = device;
			break;

		case MIB_DISK_IO_READ:
			value = GetDiskIORead(&drive);
			mib->Storage = &value;
			break;

		case MIB_DISK_IO_WRITE:
			value = GetDiskIOWrite(&drive);
			mib->Storage = &value;
			break;

		case MIB_DISK_IO_READS:
			value = GetDiskIOReads(&drive);
			mib->Storage = &value;
			break;

		case MIB_DISK_IO_WRITES:
			value = GetDiskIOWrites(&drive);
			mib->Storage = &value;
			break;

		case MIB_DISK_PERCENT:
			value = GetDiskPercent(&drive);
			mib->Storage = &value;
			break;

		case MIB_DISK_PATH:
			octetString = GetDiskPath(&drive);
			mib->Storage = octetString;
			break;

	}

}


void SetEntry(UINT idLength, UINT *ids, BYTE type, void *storage, UINT access, UINT resourceId, int index) {

	MIB_ENTRY entry;
	AsnObjectIdentifier  TempOid;

	entry.Oid.idLength = idLength;
	entry.Oid.ids = (UINT *)ids;
	entry.Type = type;	// ASN_INTEGER | ASN_RFC1155_COUNTER | ASN_OCTETSTRING
	entry.Storage = storage;
	entry.Access = access;
	entry.ResourceId = resourceId;

	SnmpUtilOidCpy( &TempOid, &entry.Oid );
	string oidstring(SnmpUtilOidToA(&TempOid));
	oid_index_map.insert(map<string, int>::value_type(oidstring, index));
	mib_entries.push_back(entry);
	SnmpUtilOidFree( &TempOid );
}


/*
 * 初期化時にコールされる、データ構造を作るための関数。
 */
void CreateDataStructure()
{
	int index = 0;
	string oidstring;

	//OIDの大小関係を考慮して、mibentryを挿入すること
	
	/** メモリ **/
	SetEntry(OID_SIZEOF(MIB_memTotalSwap), MIB_memTotalSwap, ASN_INTEGER, &MIB_dat_memTotalSwap,
			MIB_ACCESS_READONLY, MIB_MEM_TOTAL_SWAP, index++);

	SetEntry(OID_SIZEOF(MIB_memAvailSwap), MIB_memAvailSwap, ASN_INTEGER, &MIB_dat_memAvailSwap,
			MIB_ACCESS_READONLY, MIB_MEM_AVAIL_SWAP, index++);

	SetEntry(OID_SIZEOF(MIB_memTotalReal), MIB_memTotalReal, ASN_INTEGER, &MIB_dat_memTotalReal,
			MIB_ACCESS_READONLY, MIB_MEM_TOTAL_REAL, index++);

	SetEntry(OID_SIZEOF(MIB_memAvailReal), MIB_memAvailReal, ASN_INTEGER, &MIB_dat_memAvailReal,
			MIB_ACCESS_READONLY, MIB_MEM_AVAIL_REAL, index++);

	SetEntry(OID_SIZEOF(MIB_memTotalFree), MIB_memTotalFree, ASN_INTEGER, &MIB_dat_memTotalFree,
			MIB_ACCESS_READONLY, MIB_MEM_TOTAL_FREE, index++);


	SetEntry(OID_SIZEOF(MIB_memBuffer), MIB_memBuffer, ASN_INTEGER, &MIB_dat_memBuffer,
			MIB_ACCESS_READONLY, MIB_MEM_BUFFER, index++);

	SetEntry(OID_SIZEOF(MIB_memCached), MIB_memCached, ASN_INTEGER, &MIB_dat_memCached,
			MIB_ACCESS_READONLY, MIB_MEM_CACHED, index++);

	// "初期化完了"のEventLogを書出す
	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "CreateDataStructure", "Memory Initialization Completed", 0 );
	}

	/** 大容量ディスク使用率 **/
	/******************************/

	//有効なドライブ一覧を取得する（アルファベット１文字とする）
	char buf[1000] = {0};
	vector<char> drives;
	DWORD buf_size = 1000;
	DWORD cnt= 0;
	int drive_num = 0;

	// 26桁のビット列でドライブ文字を取得
	const int MAX_DRIVE_NUM = 26;
	int mask = (int)GetLogicalDrives();
	bitset<MAX_DRIVE_NUM> bit_drives(mask);

	AsnObjectIdentifier temp_oid;
	string key;
	AsnCounter *MIB_dat;
	char *MIB_dat_String;
	string *MIB_dat_octetString;
	AsnInteger *MIB_dat_Integer;
	map<string, AsnCounter>::iterator itr;
	map<string, char>::iterator itr_diskIODevice;
	map<string, AsnInteger>::iterator itr_diskIOIndex;
	map<string, AsnInteger>::iterator itr_diskParcent;
	map<string, string>::iterator itr_diskPath;


	//diskPath
	for (int i = 0; i < MAX_DRIVE_NUM; i++) {
		if (bit_drives.at(i)) {
			//ドライブ文字に基づいて、OIDの末尾を設定する。
			char drive_char = 'A' + i;

			//OIDとドライブ文字のマップ
			temp_oid.idLength = MIB_DISK_PATH_OID_LENGTH;
			temp_oid.ids = &MIB_diskPathPtr[MIB_DISK_PATH_OID_LENGTH * i];
			key = (string)(SnmpUtilOidToA(&temp_oid));
			oid_drivestring.insert(map<string, char>::value_type(key, drive_char));

			//OIDと収集値格納用の変数
			MIB_dat_diskPath.insert(map<string, string>::value_type(key, ""));
			itr_diskPath = MIB_dat_diskPath.find(key);
			if (itr_diskPath != MIB_dat_diskPath.end()) {
				MIB_dat_octetString = &(*itr_diskPath).second;
			}

			//エントリの作成
			SetEntry(MIB_DISK_PATH_OID_LENGTH, &MIB_diskPathPtr[MIB_DISK_PATH_OID_LENGTH * i], ASN_OCTETSTRING, MIB_dat_octetString,
					MIB_ACCESS_READONLY, MIB_DISK_PATH, index++);
		}
	}
	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "CreateDataStructure", "Disk Path Initialization Completed", 0 );
	}

	// diskParcent
	for (int i = 0; i < MAX_DRIVE_NUM; i++) {
		if (bit_drives.at(i)) {
			//ドライブ文字に基づいて、OIDの末尾を設定する。
			char drive_char = 'A' + i;

			//OIDとドライブ文字のマップ
			temp_oid.idLength = MIB_DISK_PERCENT_OID_LENGTH;
			temp_oid.ids = &MIB_diskParcentPtr[MIB_DISK_PERCENT_OID_LENGTH * i];
			key = (string)(SnmpUtilOidToA(&temp_oid));
			oid_drivestring.insert(map<string, char>::value_type(key, drive_char));

			//OIDとデバイスINDEXのマップ
			oid_driveindex.insert(map<string, int>::value_type(key, (i+1)));

			//OIDと収集値格納用の変数
			MIB_dat_diskPercent.insert(map<string, AsnInteger>::value_type(key, 0));
			itr_diskParcent = MIB_dat_diskPercent.find(key);
			if (itr_diskParcent != MIB_dat_diskPercent.end()) {
				MIB_dat_Integer = &(*itr_diskParcent).second;
			}
			//エントリの作成
			SetEntry(MIB_DISK_PERCENT_OID_LENGTH, &MIB_diskParcentPtr[MIB_DISK_PERCENT_OID_LENGTH * i], ASN_INTEGER, MIB_dat_Integer,
				MIB_ACCESS_READONLY, MIB_DISK_PERCENT, index++);
		}
	}
	if (verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "CreateDataStructure", "Disk Percent Initialization Completed", 0 );
	}

	/** ロードアベレージ **/

	/******************************/

	/******************************/


	/** スワップイン／アウト **/
	/******************************/
	SetEntry(OID_SIZEOF(MIB_ssSwapIn), MIB_ssSwapIn, ASN_INTEGER, &MIB_dat_ssSwapIn,
			MIB_ACCESS_READONLY, MIB_SS_SWAP_IN, index++);

	SetEntry(OID_SIZEOF(MIB_ssSwapOut), MIB_ssSwapOut, ASN_INTEGER, &MIB_dat_ssSwapOut,
			MIB_ACCESS_READONLY, MIB_SS_SWAP_OUT, index++);
	// "初期化完了"のEventLogを書出す
	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "CreateDataStructure", "Swap In/Out Initialization Completed", 0 );
	}

	/******************************/

	
	/** CPU使用率 **/
	SetEntry(OID_SIZEOF(MIB_ssCpuRawUser), MIB_ssCpuRawUser, ASN_RFC1155_COUNTER, &MIB_dat_ssCpuRawUser,
			MIB_ACCESS_READONLY, MIB_CPU_RAW_USER, index++);

	SetEntry(OID_SIZEOF(MIB_ssCpuRawNice), MIB_ssCpuRawNice, ASN_RFC1155_COUNTER, &MIB_dat_ssCpuRawNice,
			MIB_ACCESS_READONLY, MIB_CPU_RAW_NICE, index++);

	SetEntry(OID_SIZEOF(MIB_ssCpuRawSystem), MIB_ssCpuRawSystem, ASN_RFC1155_COUNTER, &MIB_dat_ssCpuRawSystem,
			MIB_ACCESS_READONLY, MIB_CPU_RAW_SYSTEM, index++);

	SetEntry(OID_SIZEOF(MIB_ssCpuRawIdle), MIB_ssCpuRawIdle, ASN_RFC1155_COUNTER, &MIB_dat_ssCpuRawIdle,
			MIB_ACCESS_READONLY, MIB_CPU_RAW_IDLE, index++);

	SetEntry(OID_SIZEOF(MIB_ssCpuRawWait), MIB_ssCpuRawWait, ASN_RFC1155_COUNTER, &MIB_dat_ssCpuRawWait,
			MIB_ACCESS_READONLY, MIB_CPU_RAW_WAIT, index++);


	SetEntry(OID_SIZEOF(MIB_ssCpuRawKernel), MIB_ssCpuRawKernel, ASN_RFC1155_COUNTER, &MIB_dat_ssCpuRawKernel,
			MIB_ACCESS_READONLY, MIB_CPU_RAW_KERNEL, index++);

	SetEntry(OID_SIZEOF(MIB_ssCpuRawInterrupt), MIB_ssCpuRawInterrupt, ASN_RFC1155_COUNTER, &MIB_dat_ssCpuRawInterrupt,
			MIB_ACCESS_READONLY, MIB_CPU_RAW_INTERRUPT, index++);


	SetEntry(OID_SIZEOF(MIB_ssRawInterrupts), MIB_ssRawInterrupts, ASN_RFC1155_COUNTER, &MIB_dat_ssRawInterrupts,
			MIB_ACCESS_READONLY, MIB_RAW_INTERRUPTS, index++);

	SetEntry(OID_SIZEOF(MIB_ssRawContexts), MIB_ssRawContexts, ASN_RFC1155_COUNTER, &MIB_dat_ssRawContexts,
			MIB_ACCESS_READONLY, MIB_RAW_CONTEXTS, index++);
	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "CreateDataStructure", "Cpu Initialization Completed", 0 );
	}

	/** ディスク **/

	//diskIOIndex
	for (int i = 0; i < MAX_DRIVE_NUM; i++) {
		if (bit_drives.at(i)) {
			//ドライブ文字に基づいて、OIDの末尾を設定する。
			char drive_char = 'A' + i;

			//OIDとドライブ文字のマップ
			temp_oid.idLength = MIB_DISK_IO_OID_LENGTH;
			temp_oid.ids = &MIB_diskIOIndexPtr[MIB_DISK_IO_OID_LENGTH * i];
			key = (string)(SnmpUtilOidToA(&temp_oid));
			oid_drivestring.insert(map<string, char>::value_type(key, drive_char));

			//OIDとデバイスINDEXのマップ
			oid_driveindex.insert(map<string, int>::value_type(key, (i+1)));

			//OIDと収集値格納用の変数
			MIB_dat_diskIOIndex.insert(map<string, AsnInteger>::value_type(key, 0));
			itr_diskIOIndex = MIB_dat_diskIOIndex.find(key);
			if (itr_diskIOIndex != MIB_dat_diskIOIndex.end()) {
				MIB_dat_Integer = &(*itr_diskIOIndex).second;
			}
			//エントリの作成
			SetEntry(MIB_DISK_IO_OID_LENGTH, &MIB_diskIOIndexPtr[MIB_DISK_IO_OID_LENGTH * i], ASN_INTEGER, MIB_dat_Integer,
					MIB_ACCESS_READONLY, MIB_DISK_IO_INDEX, index++);
		}
	}
	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "CreateDataStructure", "Disk IO Index Initialization Completed", 0 );
	}

	//diskIODevice
	for (int i = 0; i < MAX_DRIVE_NUM; i++) {
		if (bit_drives.at(i)) {
			//ドライブ文字に基づいて、OIDの末尾を設定する。
			char drive_char = 'A' + i;

			//OIDとドライブ文字のマップ
			temp_oid.idLength = MIB_DISK_IO_OID_LENGTH;
			temp_oid.ids = &MIB_diskIODevicePtr[MIB_DISK_IO_OID_LENGTH * i];
			key = (string)(SnmpUtilOidToA(&temp_oid));
			oid_drivestring.insert(map<string, char>::value_type(key, drive_char));

			//OIDと収集値格納用の変数
			MIB_dat_diskIODevice.insert(map<string, char>::value_type(key, 0));
			itr_diskIODevice = MIB_dat_diskIODevice.find(key);
			if (itr_diskIODevice != MIB_dat_diskIODevice.end()) {
				MIB_dat_String = &(*itr_diskIODevice).second;
			}

			SetEntry(MIB_DISK_IO_OID_LENGTH, &MIB_diskIODevicePtr[MIB_DISK_IO_OID_LENGTH * i], ASN_OCTETSTRING, MIB_dat_String,
					MIB_ACCESS_READONLY, MIB_DISK_IO_DEVICE, index++);
		}
	}
	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "CreateDataStructure", "Disk IO Device Initialization Completed", 0 );
	}

	//diskIORead
	for (int i = 0; i < MAX_DRIVE_NUM; i++) {
		if (bit_drives.at(i)) {
			//ドライブ文字に基づいて、OIDの末尾を設定する。
			char drive_char = 'A' + i;

			//OIDとドライブ文字のマップ
			temp_oid.idLength = MIB_DISK_IO_OID_LENGTH;
			temp_oid.ids = &MIB_diskIOReadPtr[MIB_DISK_IO_OID_LENGTH * i];
			key = (string)(SnmpUtilOidToA(&temp_oid));
			oid_drivestring.insert(map<string, char>::value_type(key, drive_char));
			//OIDと収集値格納用の変数
			MIB_dat_diskIO.insert(map<string, AsnCounter>::value_type(key, 0));
			itr = MIB_dat_diskIO.find(key);
			if (itr != MIB_dat_diskIO.end()) {
				MIB_dat = &(*itr).second;
			}
			//エントリの作成
			SetEntry(MIB_DISK_IO_OID_LENGTH, &MIB_diskIOReadPtr[MIB_DISK_IO_OID_LENGTH * i], ASN_RFC1155_COUNTER, MIB_dat,
					MIB_ACCESS_READONLY, MIB_DISK_IO_READ, index++);
		}
	}
	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "CreateDataStructure", "Disk IO Read Initialization Completed", 0 );
	}

	//diskIOWrite
	for (int i = 0; i < MAX_DRIVE_NUM; i++) {
		if (bit_drives.at(i)) {
			//ドライブ文字に基づいて、OIDの末尾を設定する。
			char drive_char = 'A' + i;

			//OIDとドライブ文字のマップ
			temp_oid.idLength = MIB_DISK_IO_OID_LENGTH;
			temp_oid.ids = &MIB_diskIOWritePtr[MIB_DISK_IO_OID_LENGTH * i];
			key = (string)(SnmpUtilOidToA(&temp_oid));
			oid_drivestring.insert(map<string, char>::value_type(key, drive_char));
			//OIDと収集値格納用の変数
			MIB_dat_diskIO.insert(map<string, AsnCounter>::value_type(key, 0));
			itr = MIB_dat_diskIO.find(key);
			if (itr != MIB_dat_diskIO.end()) {
				MIB_dat = &(*itr).second;
			}
			//エントリの作成
			SetEntry(MIB_DISK_IO_OID_LENGTH, &MIB_diskIOWritePtr[MIB_DISK_IO_OID_LENGTH * i], ASN_RFC1155_COUNTER, MIB_dat,
					MIB_ACCESS_READONLY, MIB_DISK_IO_WRITE, index++);
		}
	}
	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "CreateDataStructure", "Disk IO Write Initialization Completed", 0 );
	}

	//diskIOReads
	for (int i = 0; i < MAX_DRIVE_NUM; i++) {
		if (bit_drives.at(i)) {
			//ドライブ文字に基づいて、OIDの末尾を設定する。
			char drive_char = 'A' + i;

			//OIDとドライブ文字のマップ
			temp_oid.idLength = MIB_DISK_IO_OID_LENGTH;
			temp_oid.ids = &MIB_diskIOReadsPtr[MIB_DISK_IO_OID_LENGTH * i];
			key = (string)(SnmpUtilOidToA(&temp_oid));
			oid_drivestring.insert(map<string, char>::value_type(key, drive_char));
			//OIDと収集値格納用の変数
			MIB_dat_diskIO.insert(map<string, AsnCounter>::value_type(key, 0));
			itr = MIB_dat_diskIO.find(key);
			if (itr != MIB_dat_diskIO.end()) {
				MIB_dat = &(*itr).second;
			}
			//エントリの作成
			SetEntry(MIB_DISK_IO_OID_LENGTH, &MIB_diskIOReadsPtr[MIB_DISK_IO_OID_LENGTH * i], ASN_RFC1155_COUNTER, MIB_dat,
					MIB_ACCESS_READONLY, MIB_DISK_IO_READS, index++);
		}
	}
	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "CreateDataStructure", "Disk IO Reads Initialization Completed", 0 );
	}

	//diskIOWrites
	for (int i = 0; i < MAX_DRIVE_NUM; i++) {
		if (bit_drives.at(i)) {
			//ドライブ文字に基づいて、OIDの末尾を設定する。
			char drive_char = 'A' + i;

			//OIDとドライブ文字のマップ
			temp_oid.idLength = MIB_DISK_IO_OID_LENGTH;
			temp_oid.ids = &MIB_diskIOWritesPtr[MIB_DISK_IO_OID_LENGTH * i];
			key = (string)(SnmpUtilOidToA(&temp_oid));
			oid_drivestring.insert(map<string, char>::value_type(key, drive_char));
			//OIDと収集値格納用の変数
			MIB_dat_diskIO.insert(map<string, AsnCounter>::value_type(key, 0));
			itr = MIB_dat_diskIO.find(key);
			if (itr != MIB_dat_diskIO.end()) {
				MIB_dat = &(*itr).second;
			}
			//エントリの作成
			SetEntry(MIB_DISK_IO_OID_LENGTH, &MIB_diskIOWritesPtr[MIB_DISK_IO_OID_LENGTH * i], ASN_RFC1155_COUNTER, MIB_dat,
					MIB_ACCESS_READONLY, MIB_DISK_IO_WRITES, index++);
		}
	}
	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "CreateDataStructure", "Disk IO Writes Initialization Completed", 0 );
	}

	/**********/

}

int writeLog(char *ptr) {

	FILE* f;
	if ( fopen_s(&f, "C:\\test.log", "a") != 0)
		return 1;

	if (fprintf_s(f, "%s\n", ptr) < 0)
	{
		fclose(f);
		return 1;
	}

	if (fclose(f) != 0)
		return 1;

	return 0;

}
