#include <windows.h>
#include <winbase.h>
#include <pdh.h>
#include <snmp.h>

#include <HinemosSNMPExtAgent.h>

//typedef BOOL (WINAPI *api)(BYTE, SnmpVarBindList, AsnInteger32 *, AsnInteger32 *);

//extern "C" BOOL WINAPI SnmpExtensionQuery(BYTE, SnmpVarBindList *, AsnInteger32 *, AsnInteger32 *);
//BOOL WINAPI SnmpExtensionQuery(BYTE, SnmpVarBindList *, AsnInteger32 *, AsnInteger32 *);

void main(){

	/*
	HINSTANCE dll = LoadLibrary((LPCWSTR)"HinemosSNMPExtAgent.dll");
	if(dll < (HINSTANCE)HINSTANCE_ERROR) {
	    // エラー処理
		FreeLibrary(dll);
		return;
	}

	api apiEntryPoint;
	apiEntryPoint = (api)GetProcAddress(dll, "SnmpExtensionQuery");

	if (apiEntryPoint == NULL) {
		//エラー処理
		FreeLibrary(dll);
		return;
	}
	*/

	AsnInteger errorStatus;
	AsnInteger errorIndex;
	
	BYTE requestType = SNMP_PDU_GET;
	UINT oid_name[] = { 1, 3, 6, 1, 4, 1, 2021, 11 };
	UINT value = 0;
	
	//サンプルコードを見ていると、引数はこんな感じ
	AsnObjectIdentifier oid;
	oid.idLength = OID_SIZEOF(oid_name);
	oid.ids = oid_name;

	//valueを100とする
	AsnObjectSyntax syntax;
	syntax.asnType = ASN_INTEGER;
	syntax.asnValue.number = 10;

	SnmpVarBind varBind;
	varBind.name = oid;
	varBind.value = syntax;

	//OID_PREFIX配下にいくつもっているか
	//確保したリストの数になる予定
	SnmpVarBindList varBindList;
	varBindList.list = &varBind;
	varBindList.len = 1;

	//BOOL result = (*apiEntryPoint)(requestType, varBindList, &errorStatus, &errorIndex);
	SnmpExtensionQuery(requestType, &varBindList, &errorStatus, &errorIndex);
	

//	FreeLibrary(dll);


}