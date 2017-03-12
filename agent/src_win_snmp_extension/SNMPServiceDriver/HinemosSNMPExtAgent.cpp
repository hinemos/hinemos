/*
 
Copyright (C) 2007, 2008 NTT DATA Corporation
 
This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License 
as published by the Free Software Foundation, version 2.
 
This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied 
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
PURPOSE.  See the GNU General Public License for more details.
 
*/

//
// Hinemos SNMP �g���G�[�W�F���gDLL for Hinemos ver. 2.4.0
//

// -------------------------------------------------------------
//    �C���N���[�h�t�@�C��
// -------------------------------------------------------------
#include <windows.h>
#include <stdio.h>
#include <snmp.h>
#include <winbase.h>
#include "HinemosSNMPExtAgent.h"

#include <bitset>
#include <string>
#include <map>
#include <vector>
#include <iostream>

using namespace std;

// -------------------------------------------------------------
//    �֐��錾
// -------------------------------------------------------------
UINT ResolveVarBind( SnmpVarBind *, UINT );
UINT OperateVarBind( UINT, int, SnmpVarBind * );

VOID PostEvtLog(WORD, LPTSTR, char *, DWORD );
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

AsnInteger GetDiskIOIndex(char *);
char *	   GetDiskIODevice(char *);
AsnCounter GetDiskIORead(char *);
AsnCounter GetDiskIOWrite(char *);
AsnCounter GetDiskIOReads(char *);
AsnCounter GetDiskIOWrites(char *);

VOID CreateDataStructure(void);
VOID SetEntry(UINT, UINT *, AsnInteger32 *, UINT, UINT, int);
VOID SetEntry(UINT, UINT *, AsnCounter32 *, UINT, UINT, int);

int writeLog(char *	);

// for debug
char *g_szAbout = NULL;
//


/* DLL�̏����� */
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

/* DLL�����[�h���ꂽ�ۂɁASNMP Service����ďo����A�������������s���B */
BOOL WINAPI SnmpExtensionInit(IN  DWORD dwTimeZeroReference,
							    OUT HANDLE *hPollForTrapEvent,
								OUT AsnObjectIdentifier *supportedView )
{

	// �V�X�e���C�x���g�쐬�p�̃p�����[�^
	SECURITY_ATTRIBUTES	sa;
	SECURITY_DESCRIPTOR	sd;
	sa.nLength = sizeof( SECURITY_ATTRIBUTES );
	sa.bInheritHandle = TRUE;
	sa.lpSecurityDescriptor = &sd;

    // �Z�L�����e�B��񏉊���
	if( !InitializeSecurityDescriptor( &sd, SECURITY_DESCRIPTOR_REVISION )){
		PostEvtLog( EVENTLOG_ERROR_TYPE, "ExtensionInit", "�Z�L�����e�B�������G���[", 0 );
		return FALSE;
	}
    // �Z�L�����e�B���ݒ�
	if( !SetSecurityDescriptorDacl( &sd,TRUE, (PACL)NULL, FALSE )){
		PostEvtLog( EVENTLOG_ERROR_TYPE, "ExtensionInit", "�Z�L�����e�B�ݒ�G���[", 0 );
		return FALSE;
	}
    // Trap�֐����N�����邽�߂̃V�X�e���C�x���g���쐬
	*hPollForTrapEvent = CreateEvent(&sa, FALSE, FALSE, TRAP_EVENT_NAME );
	if((*hPollForTrapEvent) == NULL ){
		PostEvtLog( EVENTLOG_ERROR_TYPE, "ExtensionInit", "�C�x���g�����G���[", 0 );
		return FALSE;
    }
    // �Ăь��w��̎��������p�����[�^��ۑ�
    dwTimeZero = dwTimeZeroReference;

    // �{DLL�̏����ΏۂƂȂ�MIB��OID��ݒ�
    *supportedView = MIB_OidPrefix;

	// Trap�֐����N�����邽�߂̃V�X�e���C�x���g��ۑ�

//	Trap�ɂ��Ă͎������Ȃ�
//	hSimulateTrap = *hPollForTrapEvent;

	//�K�v�ȃf�[�^�\�����쐬����
	CreateDataStructure();

	// "����������"��EventLog�����o��
	PostEvtLog( EVENTLOG_INFORMATION_TYPE, "ExtensionInit", "����������", 0 );
    return TRUE;
}


/*
 * �{DLL�̏����ΏۂƂȂ�MIB�ɑ΂��ASNMP�}�l�[�W�������Get/GetNext/Set���v�����ꂽ�ۂɁA
 * SNMP Service����ďo����v���ɉ������������s���B
 */
BOOL WINAPI SnmpExtensionQuery(IN BYTE requestType, IN OUT SnmpVarBindList *variableBindings,
							   OUT AsnInteger32 *errorStatus, OUT AsnInteger32 *errorIndex )
{
    static ULONG requestCount = 0;
    UINT    i;

    // �ϐ��̐����������J��Ԃ�
    for( i=0; i<(variableBindings->len); i++ ){

        // ���N�G�X�g�ƕϐ��ɑ΂��鏈��
        *errorStatus = ResolveVarBind( &variableBindings->list[i], requestType );

        // �G���[�̏ꍇ�̏���
        if( *errorStatus != SNMP_ERRORSTATUS_NOERROR ){
            *errorIndex = i + 1;
            break;
        }
    }
    return( SNMPAPI_NOERROR );
}

/*
 * ���N�G�X�g�̂�����OID�ɑ΂��āA���X�|���X��Ԃ�OID��ݒ肷��
 */
UINT ResolveVarBind(IN OUT SnmpVarBind *VarBind, IN UINT PduAction )
{

    UINT                 nResult;
	int                  index = -1;
	bool                 noSuchName = true;

	UINT size = mib_entries.size();

	//GETNEXT�ł̖₢���킹�ł́A
	//VarBind�ɁA�Ԃ�l�ƁA���ɎQ�Ƃ���OID���Z�b�g����return����B
	if (PduAction == SNMP_PDU_GETNEXT) {
		
		//�o�^����Ă���MIB�̈ꗗ�ƁA����OID���r����
		for (UINT i = 0; i < size ; i++) {
			int compResult = SnmpUtilOidCmp(&VarBind->name, &mib_entries[i].Oid);

			// �₢���킹�̂�����OID�ƃ��X�g���}�b�`���Ȃ��ꍇ�́A
			// �[���D��T���ŁA�₢���킹OID < ���X�g��OID�ƂȂ�ŏ���OID�ɂ��āA���\�l���擾����B
			if (compResult < 0) {

				if( PduAction != SNMP_PDU_GETNEXT ){
					return( SNMP_ERRORSTATUS_NOSUCHNAME );
				}

				noSuchName = false;

				// �����̌��ʁA�l���擾����OID
				PduAction = SNMP_PDU_GET;
				index = i;

				// �ϐ�����U������AOID��ݒ�
				SnmpUtilOidFree( &VarBind->name );
				SnmpUtilOidCpy( &VarBind->name, &mib_entries[i].Oid );

				break;

			// �₢���킹�̂�����OID�����݂����ꍇ
			} else if (compResult == 0) {
				PduAction = SNMP_PDU_GET;
				index = i;
				noSuchName = false;
				SnmpUtilOidFree( &VarBind->name );
				
				// ���X�g�̖����ł���΁A����OID��NULL�Ƃ��ĕԂ�
				if (i != size - 1){
					//����OID�����݂���ꍇ�͂����ݒ肷��
					index++;
					SnmpUtilOidCpy( &VarBind->name, &mib_entries[index].Oid );
					break;
				}
			}
		}

	// GET�ł̖₢���킹�ł́A���X�g���������āA���݂����琫�\�l���擾����B
	} else if (PduAction == SNMP_PDU_GET) {

		//map�o�R�ŏ����擾
		string requestOid(SnmpUtilOidToA(&VarBind->name));
		map<string, int>::iterator itr = oid_index_map.find(requestOid);

		//���݂�����t���O��؂�ւ���
		if (itr != oid_index_map.end()) {
			noSuchName = false;
			index = (*itr).second;
		}
	}

	if (noSuchName) {
        return( SNMP_ERRORSTATUS_NOSUCHNAME );
    }

    // �����֐����Ăяo��
    nResult = OperateVarBind( PduAction, index, VarBind );

    return( nResult );
}

/*
 * ���N�G�X�g�ɑ΂���Ԃ�l��ݒ肷��B
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
		
		//����OID���Z�b�g����
		if (index < oid_index_map.size() - 1) {
			 NextMibPtr = &mib_entries[next_index];

		//���X�g�Ō����OID�������ꍇ
		} else if (index == oid_index_map.size() - 1) {
			 NextMibPtr = NULL;

		// �Y������OID��������Ȃ������ꍇ�Areturn����
		} else {
			return( SNMP_ERRORSTATUS_NOSUCHNAME );
		}

		//���Ɍ���ׂ�OID���Z�b�g����
        SnmpUtilOidFree( &VarBind->name );
		SnmpUtilOidCpy( &VarBind->name, &NextMibPtr->Oid );

	    // �����֐����Ăяo�� (3.2) ���ċA�ďo
        ErrStat = OperateVarBind( SNMP_PDU_GET, index, VarBind );
        return( ErrStat );
*/
	case SNMP_PDU_GET:
        if(( MibPtr->Access != MIB_ACCESS_READONLY  ) &&
           ( MibPtr->Access != MIB_ACCESS_READWRITE )){
            return( SNMP_ERRORSTATUS_NOSUCHNAME );
	    }

		//���\�[�X�����擾
		GetMibData(MibPtr);

        VarBind->value.asnType = MibPtr->Type;
        switch( VarBind->value.asnType ){
            case ASN_RFC1155_COUNTER:
            case ASN_RFC1155_GAUGE:
            case ASN_INTEGER:
                VarBind->value.asnValue.number = *(AsnInteger *)(MibPtr->Storage);
                break;

            case ASN_OCTETSTRING:
				
				// octet�����Z�b�g
				//VarBind->value.asnValue.string.length = ((AsnOctetString *)(LPSTR)MibPtr->Storage)->length;
				//VarBind->value.asnValue.string.length = strlen((LPSTR)MibPtr->Storage);
				VarBind->value.asnValue.string.length = 1;

				// �������m��
				VarBind->value.asnValue.string.stream = (unsigned char *)SnmpUtilMemAlloc( VarBind->value.asnValue.string.length * sizeof(char) );

				if( VarBind->value.asnValue.string.stream == NULL ){
					return( SNMP_ERRORSTATUS_GENERR );
				}

				// �|�C���^�̏����R�s�[����
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

/* Windows�C�x���g�o�͗p�֐� */
VOID PostEvtLog( WORD logType, LPTSTR ModuleName, char *SendMsg, DWORD val )
{
    CHAR    Msg[512];			// ���b�Z�[�W
    HANDLE  hEventLog;          // �C�x���g�E���O�̃n���h��
	LPCSTR  Strings[2];         // �C�x���g�E���O�ւ̋L�ڕ�����
    WORD    EventCat = 0;       // �C�x���g�E�J�e�S��
    DWORD   EventID = 1000;     // �C�x���g���ʎq
    char    tmpbf[256];
  
    // �C�x���g�E���O�ɏ����L�^
	hEventLog = RegisterEventSource(NULL, "Hinemos SNMP Ext Agent");

    if( hEventLog != NULL ){
		strcpy( Msg, "Hinemos SNMP Ext Agent : ");
        sprintf( tmpbf,"%s %s(%d)", ModuleName, SendMsg, val );
	    strcat( Msg, tmpbf );
		Strings[0] = Msg;

		ReportEvent( hEventLog, logType, EventCat, EventID, NULL, 1, 0, Strings, NULL );
		DeregisterEventSource( hEventLog );
    }
	return;
}



/*
 * ���C�u�����ւ̃A�N�Z�X�֐�
 * OID�Ɋ�Â��ēK�؂Ȋ֐����Ăт����A�����Z�b�g����
 */
void GetMibData(IN OUT MIB_ENTRY *mib)
{

	//�U�蕪�����W�b�N������
	int id = mib->ResourceId;

	//diskIO�p
	char drive;
	string oid_string(SnmpUtilOidToA(&mib->Oid));
	AsnCounter value;
	char *device;

	map<string, char>::iterator itr1;
	map<string, AsnCounter>::iterator itr2;
	map<string, char>::iterator itr3;

	switch(id){
		case MIB_DISK_IO_DEVICE:
			//�h���C�u�����̎擾
			itr1 = oid_drivestring.find(oid_string);
			//���݂�����t���O��؂�ւ���
			if (&itr1 != NULL) {
				drive = (*itr1).second;
			}
			itr3 = MIB_dat_diskIODevice.find(oid_string);
			if (&itr3 != NULL) {
				device = &(*itr3).second;
			}
			break;
	}


	switch(id){
		case MIB_DISK_IO_READ:
		case MIB_DISK_IO_WRITE:
		case MIB_DISK_IO_READS:
		case MIB_DISK_IO_WRITES:
			//�h���C�u�����̎擾
			itr1 = oid_drivestring.find(oid_string);
			//���݂�����t���O��؂�ւ���
			if (&itr1 != NULL) {
				drive = (*itr1).second;
			}

			//���W�l���Z�b�g����ϐ����擾
			itr2 = MIB_dat_diskIO.find(oid_string);
			//���݂�����t���O��؂�ւ���
			if (&itr2 != NULL) {
				value = (*itr2).second;
			}
			break;
	}

	
	switch(id){
		//�Ԃ�l�ɂ͒l�ւ̃|�C���^��ݒ肷�邽�߁A
		//�l�̕ێ�����ϐ��̓O���[�o���ϐ��ł���K�v������
		//�߂�l�������āA�|�C���^��mib�ɃZ�b�g����

		case MIB_CPU_RAW_USER:
			MIB_dat_ssCpuRawUser = GetCpuRawUser();
			mib->Storage = &MIB_dat_ssCpuRawUser;
			break;

		case MIB_CPU_RAW_NICE:
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
			value = GetDiskIOIndex(&drive);
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

	}

}


void SetEntry(UINT idLength, UINT *ids, AsnInteger32 *storage, UINT access, UINT resourceId, int index) {

	MIB_ENTRY entry;
	AsnObjectIdentifier  TempOid;

	entry.Oid.idLength = idLength;
	entry.Oid.ids = (UINT *)ids;
	entry.Storage = storage;
	entry.Type = ASN_INTEGER;
	entry.Access = access;
	entry.ResourceId = resourceId;

	SnmpUtilOidFree( &TempOid );
	SnmpUtilOidCpy( &TempOid, &entry.Oid );
	string oidstring(SnmpUtilOidToA(&TempOid));
	oid_index_map.insert(map<string, int>::value_type(oidstring, index));
	mib_entries.push_back(entry);

}

void SetEntry(UINT idLength, UINT *ids, AsnCounter32 *storage, UINT access, UINT resourceId, int index) {

	MIB_ENTRY entry;
	AsnObjectIdentifier  TempOid;

	entry.Oid.idLength = idLength;
	entry.Oid.ids = (UINT *)ids;
	entry.Storage = storage;
	entry.Type = ASN_RFC1155_COUNTER;
	entry.Access = access;
	entry.ResourceId = resourceId;

	SnmpUtilOidFree( &TempOid );
	SnmpUtilOidCpy( &TempOid, &entry.Oid );
	string oidstring(SnmpUtilOidToA(&TempOid));
	oid_index_map.insert(map<string, int>::value_type(oidstring, index));
	mib_entries.push_back(entry);

}

void SetEntry(UINT idLength, UINT *ids, char *storage, UINT access, UINT resourceId, int index) {

	MIB_ENTRY entry;
	AsnObjectIdentifier  TempOid;

	entry.Oid.idLength = idLength;
	entry.Oid.ids = (UINT *)ids;
	entry.Storage = storage;
	entry.Type = ASN_OCTETSTRING;
	entry.Access = access;
	entry.ResourceId = resourceId;

	SnmpUtilOidFree( &TempOid );
	SnmpUtilOidCpy( &TempOid, &entry.Oid );
	string oidstring(SnmpUtilOidToA(&TempOid));
	oid_index_map.insert(map<string, int>::value_type(oidstring, index));
	mib_entries.push_back(entry);

}


/*
 * ���������ɃR�[�������A�f�[�^�\������邽�߂̊֐��B
 */
void CreateDataStructure()
{
	int index = 0;
	string oidstring;

	//OID�̑召�֌W���l�����āAmibentry��}�����邱��
	
	/** ������ **/
	SetEntry(OID_SIZEOF(MIB_memTotalSwap), MIB_memTotalSwap, &MIB_dat_memTotalSwap,
			MIB_ACCESS_READONLY, MIB_MEM_TOTAL_SWAP, index++);

	SetEntry(OID_SIZEOF(MIB_memAvailSwap), MIB_memAvailSwap, &MIB_dat_memAvailSwap,
			MIB_ACCESS_READONLY, MIB_MEM_AVAIL_SWAP, index++);

	SetEntry(OID_SIZEOF(MIB_memTotalReal), MIB_memTotalReal, &MIB_dat_memTotalReal,
			MIB_ACCESS_READONLY, MIB_MEM_TOTAL_REAL, index++);

	SetEntry(OID_SIZEOF(MIB_memAvailReal), MIB_memAvailReal, &MIB_dat_memAvailReal,
			MIB_ACCESS_READONLY, MIB_MEM_AVAIL_REAL, index++);

	SetEntry(OID_SIZEOF(MIB_memTotalFree), MIB_memTotalFree, &MIB_dat_memTotalFree,
			MIB_ACCESS_READONLY, MIB_MEM_TOTAL_FREE, index++);


	SetEntry(OID_SIZEOF(MIB_memBuffer), MIB_memBuffer, &MIB_dat_memBuffer,
			MIB_ACCESS_READONLY, MIB_MEM_BUFFER, index++);

	SetEntry(OID_SIZEOF(MIB_memCached), MIB_memCached, &MIB_dat_memCached,
			MIB_ACCESS_READONLY, MIB_MEM_CACHED, index++);

	/** ���[�h�A�x���[�W **/

	/******************************/

	/******************************/


	/** �X���b�v�C���^�A�E�g **/
	/******************************/
	SetEntry(OID_SIZEOF(MIB_ssSwapIn), MIB_ssSwapIn, &MIB_dat_ssSwapIn,
			MIB_ACCESS_READONLY, MIB_SS_SWAP_IN, index++);

	SetEntry(OID_SIZEOF(MIB_ssSwapOut), MIB_ssSwapOut, &MIB_dat_ssSwapOut,
			MIB_ACCESS_READONLY, MIB_SS_SWAP_OUT, index++);
	/******************************/

	
	/** CPU�g�p�� **/
	SetEntry(OID_SIZEOF(MIB_ssCpuRawUser), MIB_ssCpuRawUser, &MIB_dat_ssCpuRawUser,
			MIB_ACCESS_READONLY, MIB_CPU_RAW_USER, index++);

	SetEntry(OID_SIZEOF(MIB_ssCpuRawNice), MIB_ssCpuRawNice, &MIB_dat_ssCpuRawNice,
			MIB_ACCESS_READONLY, MIB_CPU_RAW_NICE, index++);

	SetEntry(OID_SIZEOF(MIB_ssCpuRawSystem), MIB_ssCpuRawSystem, &MIB_dat_ssCpuRawSystem,
			MIB_ACCESS_READONLY, MIB_CPU_RAW_SYSTEM, index++);

	SetEntry(OID_SIZEOF(MIB_ssCpuRawIdle), MIB_ssCpuRawIdle, &MIB_dat_ssCpuRawIdle,
			MIB_ACCESS_READONLY, MIB_CPU_RAW_IDLE, index++);

	SetEntry(OID_SIZEOF(MIB_ssCpuRawWait), MIB_ssCpuRawWait, &MIB_dat_ssCpuRawWait,
			MIB_ACCESS_READONLY, MIB_CPU_RAW_WAIT, index++);


	SetEntry(OID_SIZEOF(MIB_ssCpuRawKernel), MIB_ssCpuRawKernel, &MIB_dat_ssCpuRawKernel,
			MIB_ACCESS_READONLY, MIB_CPU_RAW_KERNEL, index++);

	SetEntry(OID_SIZEOF(MIB_ssCpuRawInterrupt), MIB_ssCpuRawInterrupt, &MIB_dat_ssCpuRawInterrupt,
			MIB_ACCESS_READONLY, MIB_CPU_RAW_INTERRUPT, index++);


	SetEntry(OID_SIZEOF(MIB_ssRawInterrupts), MIB_ssRawInterrupts, &MIB_dat_ssRawInterrupts,
			MIB_ACCESS_READONLY, MIB_RAW_INTERRUPTS, index++);

	SetEntry(OID_SIZEOF(MIB_ssRawContexts), MIB_ssRawContexts, &MIB_dat_ssRawContexts,
			MIB_ACCESS_READONLY, MIB_RAW_CONTEXTS, index++);


	/** �f�B�X�N **/

	//�L���ȃh���C�u�ꗗ���擾����i�A���t�@�x�b�g�P�����Ƃ���j
	char buf[1000] = {0};
	vector<char> drives;
	DWORD buf_size = 1000;
	DWORD cnt= 0;
	int drive_num = 0;

	// 26���̃r�b�g��Ńh���C�u�������擾
	const int MAX_DRIVE_NUM = 26;
	DWORD mask = GetLogicalDrives();
	bitset<MAX_DRIVE_NUM> bit_drives(mask);

	AsnObjectIdentifier temp_oid;
	string key;
	AsnCounter *MIB_dat;
	char *MIB_dat_String;
	map<string, AsnCounter>::iterator itr;
	map<string, char>::iterator itr_diskIODevice;


	//diskIODevice
	for (int i = 0; i < MAX_DRIVE_NUM; i++) {
		if (bit_drives.at(i)) {
			//�h���C�u�����Ɋ�Â��āAOID�̖�����ݒ肷��B
			char drive_char = 'A' + i;

			//OID�ƃh���C�u�����̃}�b�v
			temp_oid.idLength = MIB_DISK_IO_OID_LENGTH;
			temp_oid.ids = &MIB_diskIODevicePtr[MIB_DISK_IO_OID_LENGTH * i];
			key = (string)(SnmpUtilOidToA(&temp_oid));
			oid_drivestring.insert(map<string, char>::value_type(key, drive_char));

			//OID�Ǝ��W�l�i�[�p�̕ϐ�
			MIB_dat_diskIODevice.insert(map<string, char>::value_type(key, 0));
			itr_diskIODevice = MIB_dat_diskIODevice.find(key);
			if (itr_diskIODevice != MIB_dat_diskIODevice.end()) {
				MIB_dat_String = &(*itr_diskIODevice).second;
			}

			SetEntry(MIB_DISK_IO_OID_LENGTH, &MIB_diskIODevicePtr[MIB_DISK_IO_OID_LENGTH * i], MIB_dat_String,
					MIB_ACCESS_READONLY, MIB_DISK_IO_DEVICE, index++);
		}
	}

	//diskIORead
	for (int i = 0; i < MAX_DRIVE_NUM; i++) {
		if (bit_drives.at(i)) {
			//�h���C�u�����Ɋ�Â��āAOID�̖�����ݒ肷��B
			char drive_char = 'A' + i;

			//OID�ƃh���C�u�����̃}�b�v
			temp_oid.idLength = MIB_DISK_IO_OID_LENGTH;
			temp_oid.ids = &MIB_diskIOReadPtr[MIB_DISK_IO_OID_LENGTH * i];
			key = (string)(SnmpUtilOidToA(&temp_oid));
			oid_drivestring.insert(map<string, char>::value_type(key, drive_char));
			//OID�Ǝ��W�l�i�[�p�̕ϐ�
			MIB_dat_diskIO.insert(map<string, AsnCounter>::value_type(key, 0));
			itr = MIB_dat_diskIO.find(key);
			if (itr != MIB_dat_diskIO.end()) {
				MIB_dat = &(*itr).second;
			}
			//�G���g���̍쐬a
			SetEntry(MIB_DISK_IO_OID_LENGTH, &MIB_diskIOReadPtr[MIB_DISK_IO_OID_LENGTH * i], MIB_dat,
					MIB_ACCESS_READONLY, MIB_DISK_IO_READ, index++);
		}
	}


	//diskIOWrite
	for (int i = 0; i < MAX_DRIVE_NUM; i++) {
		if (bit_drives.at(i)) {
			//�h���C�u�����Ɋ�Â��āAOID�̖�����ݒ肷��B
			char drive_char = 'A' + i;

			//OID�ƃh���C�u�����̃}�b�v
			temp_oid.idLength = MIB_DISK_IO_OID_LENGTH;
			temp_oid.ids = &MIB_diskIOWritePtr[MIB_DISK_IO_OID_LENGTH * i];
			key = (string)(SnmpUtilOidToA(&temp_oid));
			oid_drivestring.insert(map<string, char>::value_type(key, drive_char));
			//OID�Ǝ��W�l�i�[�p�̕ϐ�
			MIB_dat_diskIO.insert(map<string, AsnCounter>::value_type(key, 0));
			itr = MIB_dat_diskIO.find(key);
			if (itr != MIB_dat_diskIO.end()) {
				MIB_dat = &(*itr).second;
			}
			//�G���g���̍쐬a
			SetEntry(MIB_DISK_IO_OID_LENGTH, &MIB_diskIOWritePtr[MIB_DISK_IO_OID_LENGTH * i], MIB_dat,
					MIB_ACCESS_READONLY, MIB_DISK_IO_WRITE, index++);
		}
	}


	//diskIOReads
	for (int i = 0; i < MAX_DRIVE_NUM; i++) {
		if (bit_drives.at(i)) {
			//�h���C�u�����Ɋ�Â��āAOID�̖�����ݒ肷��B
			char drive_char = 'A' + i;

			//OID�ƃh���C�u�����̃}�b�v
			temp_oid.idLength = MIB_DISK_IO_OID_LENGTH;
			temp_oid.ids = &MIB_diskIOReadsPtr[MIB_DISK_IO_OID_LENGTH * i];
			key = (string)(SnmpUtilOidToA(&temp_oid));
			oid_drivestring.insert(map<string, char>::value_type(key, drive_char));
			//OID�Ǝ��W�l�i�[�p�̕ϐ�
			MIB_dat_diskIO.insert(map<string, AsnCounter>::value_type(key, 0));
			itr = MIB_dat_diskIO.find(key);
			if (itr != MIB_dat_diskIO.end()) {
				MIB_dat = &(*itr).second;
			}
			//�G���g���̍쐬a
			SetEntry(MIB_DISK_IO_OID_LENGTH, &MIB_diskIOReadsPtr[MIB_DISK_IO_OID_LENGTH * i], MIB_dat,
					MIB_ACCESS_READONLY, MIB_DISK_IO_READS, index++);
		}
	}

	//diskIOWrites
	for (int i = 0; i < MAX_DRIVE_NUM; i++) {
		if (bit_drives.at(i)) {
			//�h���C�u�����Ɋ�Â��āAOID�̖�����ݒ肷��B
			char drive_char = 'A' + i;

			//OID�ƃh���C�u�����̃}�b�v
			temp_oid.idLength = MIB_DISK_IO_OID_LENGTH;
			temp_oid.ids = &MIB_diskIOWritesPtr[MIB_DISK_IO_OID_LENGTH * i];
			key = (string)(SnmpUtilOidToA(&temp_oid));
			oid_drivestring.insert(map<string, char>::value_type(key, drive_char));
			//OID�Ǝ��W�l�i�[�p�̕ϐ�
			MIB_dat_diskIO.insert(map<string, AsnCounter>::value_type(key, 0));
			itr = MIB_dat_diskIO.find(key);
			if (itr != MIB_dat_diskIO.end()) {
				MIB_dat = &(*itr).second;
			}
			//�G���g���̍쐬a
			SetEntry(MIB_DISK_IO_OID_LENGTH, &MIB_diskIOWritesPtr[MIB_DISK_IO_OID_LENGTH * i], MIB_dat,
					MIB_ACCESS_READONLY, MIB_DISK_IO_WRITES, index++);
		}
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





void main(){

	/*
	HINSTANCE dll = LoadLibrary((LPCWSTR)"HinemosSNMPExtAgent.dll");
	if(dll < (HINSTANCE)HINSTANCE_ERROR) {
	    // �G���[����
		FreeLibrary(dll);
		return;
	}

	api apiEntryPoint;
	apiEntryPoint = (api)GetProcAddress(dll, "SnmpExtensionQuery");

	if (apiEntryPoint == NULL) {
		//�G���[����
		FreeLibrary(dll);
		return;
	}
	*/

	AsnInteger errorStatus;
	AsnInteger errorIndex;
	
//	BYTE requestType = SNMP_PDU_GET;
//	UINT oid_name[] = { 1, 3, 6, 1, 4, 1, 2021, 11, 52, 0 };
	BYTE requestType = SNMP_PDU_GET;
	UINT oid_name[] = { 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 1};

	UINT value = 0;
	
	//�T���v���R�[�h�����Ă���ƁA�����͂���Ȋ���
	AsnObjectIdentifier oid;
	oid.idLength = OID_SIZEOF(oid_name);
	oid.ids = oid_name;

	//value��100�Ƃ���
	AsnObjectSyntax syntax;
	syntax.asnType = ASN_INTEGER;
	syntax.asnValue.number = 10;

	SnmpVarBind varBind;
	varBind.name = oid;
	varBind.value = syntax;

	//OID_PREFIX�z���ɂ��������Ă��邩
	//�m�ۂ������X�g�̐��ɂȂ�\��
	SnmpVarBindList varBindList;
	varBindList.list = &varBind;
	varBindList.len = 1;


	HANDLE handle;
	AsnObjectIdentifier prefix_oid;
	DWORD dwTimeZeroReference = 1000000;
	SnmpExtensionInit(dwTimeZeroReference, &handle, &prefix_oid);

//	CreateDataStructure();

	//BOOL result = (*apiEntryPoint)(requestType, varBindList, &errorStatus, &errorIndex);
	SnmpExtensionQuery(requestType, &varBindList, &errorStatus, &errorIndex);
	
	
	Sleep(1000);

//	FreeLibrary(dll);


}