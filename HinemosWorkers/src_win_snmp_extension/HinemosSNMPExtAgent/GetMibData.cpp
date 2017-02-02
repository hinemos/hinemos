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

#include <windows.h>
#include <stdio.h>
#include <pdh.h>
#include <winbase.h>
#include <snmp.h>

#include <string>
#include <vector>
#include <locale>

using namespace std;

double GetPagingFileUsage(void);
bool CheckErrorCode(long code);

VOID PostEvtLog(WORD, LPTSTR, char *, long );
VOID PostEvtLogDouble( WORD logType, LPTSTR ModuleName, char *SendMsg, double val );
VOID PostEvtLogDWORDLONG(WORD, LPTSTR, char *, DWORDLONG );
VOID PostEvtLogChar( WORD logType, LPTSTR ModuleName, char *SendMsg, char c, long val);
VOID PostEvtLogString( WORD logType, LPTSTR ModuleName, char *SendMsg, char *str, long val);

//メモリ情報取得用
MEMORYSTATUSEX memstatex;

//Byte->KByte変換用定数
LONGLONG DIV_MEMORY = 1024LL;

//100ナノ秒単位で取得されるCPU使用率のカウンタ値を、10ミリ秒単位に変換する定数
LONG DIV_CPU = 100000L;

//ページサイズ取得用
SYSTEM_INFO system_info;

//１ページあたりのサイズ（KByte）
int page_size = -1;

char* device = (char*)malloc(2);
char* driveStr = (char*)malloc(4);

extern int verbose;
extern WORD errorEventLevel;


//各関数はint等で返すように実装する

AsnCounter GetCpuRawUser(){
	long ret = 0L;
	PDH_STATUS status = ERROR_SUCCESS;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	// 新規クエリーを作成 
	status = PdhOpenQuery(NULL, 0, &hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawUser", "PdhOpenQuery", status);
	}

	// カウンタをクエリーに追加
	status = PdhAddCounter(hQuery, "\\Processor(_Total)\\% User Time", 0, &hCounter);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawUser", "PdhAddCounter", status);
	}

	// カウンタ値を更新
	status = PdhCollectQueryData(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawUser", "PdhCollectQueryData", status);
	}

	// カウンタ値を取得
	status = PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawUser", "PdhGetRawCounterValue", status);
	}

	ret = (long)(rawValue.FirstValue / DIV_CPU);

	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetCpuRawUser", "ssCpuRawUser", ret);
	}

	// 終了 
	status = PdhCloseQuery(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawUser", "PdhCloseQuery", status);
	}

	return ret;
}


//Windowsでは取得できないので、0を返す
AsnCounter GetCpuRawNice(){
	long ret = 0L;
	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetCpuRawNice", "ssCpuRawNice", ret);
	}
	return ret;
}


AsnCounter GetCpuRawSystem(){
	long ret = 0L;
	PDH_STATUS status = ERROR_SUCCESS;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	// 新規クエリーを作成 
	status = PdhOpenQuery(NULL, 0, &hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawSystem", "PdhOpenQuery", status);
	}

	// カウンタをクエリーに追加
	status = PdhAddCounter(hQuery, "\\Processor(_Total)\\% Privileged Time", 0, &hCounter);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawSystem", "PdhAddCounter", status);
	}

	// カウンタ値を更新
	status = PdhCollectQueryData(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawSystem", "PdhCollectQueryData", status);
	}

	// カウンタ値を取得
	status = PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawSystem", "PdhGetRawCounterValue", status);
	}

	ret = (long)(rawValue.FirstValue / DIV_CPU);

	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetCpuRawSystem", "ssCpuRawSystem", ret);
	}

	// 終了 
	status = PdhCloseQuery(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawSystem", "PdhCloseQuery", status);
	}

	return ret;
}


AsnCounter GetCpuRawIdle(){
	long ret = 0L;
	PDH_STATUS status = ERROR_SUCCESS;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	// 新規クエリーを作成
	status = PdhOpenQuery(NULL, 0, &hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawIdle", "PdhOpenQuery", status);
	}

	// カウンタをクエリーに追加
	status = PdhAddCounter(hQuery, "\\Processor(_Total)\\% Idle Time", 0, &hCounter);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawIdle", "PdhAddCounter", status);
	}

	// カウンタ値を更新
	status = PdhCollectQueryData(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawIdle", "PdhCollectQueryData", status);
	}

	// カウンタ値を取得
	status = PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawIdle", "PdhGetRawCounterValue", status);
	}

	ret = (long)(rawValue.FirstValue / DIV_CPU);

	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetCpuRawIdle", "ssCpuRawIdle", ret);
	}

	// 終了
	status = PdhCloseQuery(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawIdle", "PdhCloseQuery", status);
	}
	
	return ret;
}


AsnCounter GetCpuRawWait(){
	long ret = 0L;
	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetCpuRawWait", "ssCpuRawWait", ret);
	}
	return ret;
}

AsnCounter GetCpuRawKernel(){
	long ret = 0L;
	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetCpuRawKernel", "ssCpuRawKernel", ret);
	}
	return ret;
}

AsnCounter GetCpuRawInterrupt(){
	long ret = 0L;
	PDH_STATUS status = ERROR_SUCCESS;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	// 新規クエリーを作成
	status = PdhOpenQuery(NULL, 0, &hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawInterrupt", "PdhOpenQuery", status);
	}

	// カウンタをクエリーに追加
	status = PdhAddCounter(hQuery, "\\Processor(_Total)\\% Interrupt Time", 0, &hCounter);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawInterrupt", "PdhAddCounter", status);
	}
	
	// カウンタ値を更新
	status = PdhCollectQueryData(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawInterrupt", "PdhCollectQueryData", status);
	}

	// カウンタ値を取得
	status = PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawInterrupt", "PdhGetRawCounterValue", status);
	}

	ret = (long)(rawValue.FirstValue / DIV_CPU);

	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetCpuRawInterrupt", "ssCpuRawInterrupt", ret);
	}

	// 終了
	status = PdhCloseQuery(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetCpuRawInterrupt", "PdhCloseQuery", status);
	}
	
	return ret;
}

AsnCounter GetRawInterrupts(){
	long ret = 0L;
	PDH_STATUS status = ERROR_SUCCESS;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	// 新規クエリーを作成
	status = PdhOpenQuery(NULL, 0, &hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetRawInterrupts", "PdhOpenQuery", status);
	}

	// カウンタをクエリーに追加
	status = PdhAddCounter(hQuery, "\\Processor(_Total)\\Interrupts/sec", 0, &hCounter);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetRawInterrupts", "PdhAddCounter", status);
	}

	// カウンタ値を更新
	status = PdhCollectQueryData(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetRawInterrupts", "PdhCollectQueryData", status);
	}

	// カウンタ値を取得
	status = PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetRawInterrupts", "PdhGetRawCounterValue", status);
	}

	ret = (long)(rawValue.FirstValue / DIV_CPU);

	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetRawInterrupts", "ssRawInterrupts", ret);
	}

	// 終了
	status = PdhCloseQuery(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetRawInterrupts", "PdhCloseQuery", status);
	}
	
	return ret;
}

AsnCounter GetRawContexts(){
	long ret = 0L;
	PDH_STATUS status = ERROR_SUCCESS;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	// 新規クエリーを作成
	status = PdhOpenQuery(NULL, 0, &hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetRawContexts", "PdhOpenQuery", status);
	}

	// カウンタをクエリーに追加
	status = PdhAddCounter(hQuery, "\\System\\Context Switches/sec", 0, &hCounter);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetRawContexts", "PdhAddCounter", status);
	}
	
	// カウンタ値を更新
	status = PdhCollectQueryData(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetRawContexts", "PdhCollectQueryData", status);
	}

	// カウンタ値を取得
	status = PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetRawContexts", "PdhGetRawCounterValue", status);
	}

	ret = (long)(rawValue.FirstValue / DIV_CPU);

	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetRawContexts", "ssRawContexts", ret);
	}

	// 終了
	status = PdhCloseQuery(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetRawContexts", "PdhCloseQuery", status);
	}
	
	return ret;
}


// 仮想メモリの合計を返す
AsnInteger GetMemTotalReal(){
	long ret = 0L;

	ZeroMemory(&memstatex, sizeof(MEMORYSTATUSEX));
	memstatex.dwLength = sizeof(memstatex);

	// GlobalMemoryStatusEx関数が失敗した場合
	if(GlobalMemoryStatusEx(&memstatex) == 0){
		DWORD status = GetLastError();

		if(verbose == 1 && CheckErrorCode(status) && status != ERROR_IO_PENDING){
			PostEvtLog( errorEventLevel, "GetMemTotalReal", "GlobalMemoryStatusEx", status);
		}
	}

	ret = (long)(memstatex.ullTotalPhys / DIV_MEMORY);

	if(verbose == 1){
		PostEvtLogDWORDLONG( EVENTLOG_INFORMATION_TYPE, "GetMemTotalReal", "GlobalMemoryStatusEx ullTotalPhys", memstatex.ullTotalPhys);
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetMemTotalReal", "memTotalReal", ret);
	}

	return ret;
}

// スワップの合計（ページファイルのサイズ）を返す
// スワップ合計 = コミットチャージの制限値 - 物理メモリ合計
AsnInteger GetMemTotalSwap(){
	long ret = 0L;

	ZeroMemory(&memstatex, sizeof(MEMORYSTATUSEX));
	memstatex.dwLength = sizeof(memstatex);

	// GlobalMemoryStatusEx関数が失敗した場合
	if(GlobalMemoryStatusEx(&memstatex) == 0){
		DWORD status = GetLastError();

		if(verbose == 1 && CheckErrorCode(status) && status != ERROR_IO_PENDING){
			PostEvtLog( errorEventLevel, "GetMemTotalSwap", "GlobalMemoryStatusEx", status);
		}
	}

	ret = (long)((memstatex.ullTotalPageFile  - memstatex.ullTotalPhys) / DIV_MEMORY);

	/*
	memstatex.ullTotalPageFile = 物理メモリ + ページングファイル - オーバヘッド
	memstatex.ullTotalPhys     = 物理メモリ
	(オーバヘッドはメモリサイズが2GB以上の場合に用意される様子。)
	
	ret = memstatex.ullTotalPageFile - memstatex.ullTotalPhys = ページングファイル - オーバヘッド
	ページングファイルが0の場合は、GetMemTotalSwapがマイナスになってしまうため、
	ret < 0の場合はret=0にする。
	
	注:本来はオーバヘッドの値を取ってくるべきだが、その方法がわからず。。。
	*/
	if (ret < 0) {
		ret = 0;
	}

	if(verbose == 1){
		PostEvtLogDWORDLONG( EVENTLOG_INFORMATION_TYPE, "GetMemTotalSwap", "GlobalMemoryStatusEx ullTotalPageFile", memstatex.ullTotalPageFile);
		PostEvtLogDWORDLONG( EVENTLOG_INFORMATION_TYPE, "GetMemTotalSwap", "GlobalMemoryStatusEx ullTotalPhys", memstatex.ullTotalPhys);
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetMemTotalSwap", "GlobalMemoryStatusEx memTotalSwap", ret);
	}

	return ret;
}

// メモリの空き容量を返す
// タスクマネージャの利用可能の値
AsnInteger GetMemAvailReal(){
	long ret = 0L;

	ZeroMemory(&memstatex, sizeof(MEMORYSTATUSEX));
	memstatex.dwLength = sizeof(memstatex);

	// GlobalMemoryStatusEx関数が失敗した場合
	if(GlobalMemoryStatusEx(&memstatex) == 0){
		DWORD status = GetLastError();

		if(verbose == 1 && CheckErrorCode(status) && status != ERROR_IO_PENDING){
			PostEvtLog( errorEventLevel, "GetMemAvailReal", "GlobalMemoryStatusEx", status);
		}
	}

	ret = (long)(memstatex.ullAvailPhys / DIV_MEMORY);

	if(verbose == 1){
		PostEvtLogDWORDLONG( EVENTLOG_INFORMATION_TYPE, "GetMemAvailReal", "GlobalMemoryStatusEx ullAvailPhys", memstatex.ullAvailPhys);
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetMemAvailReal", "memAvailReal", ret);
	}

	return ret;
}

// スワップの空き容量を返す
// ページファイルのサイズと使用率から算出する
// ページファイルのサイズ = コミットチャージの制限値 - 物理メモリ合計
AsnInteger GetMemAvailSwap(){
	long ret = 0L;
	ret = (100 - GetPagingFileUsage()) * GetMemTotalSwap() / 100;

	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetMemAvailSwap", "memAvailSwap", ret);
	}

	return ret;
}

// メモリ空き容量とスワップ空き容量の和
AsnInteger GetMemTotalFree(){
	long ret = 0L;
	ret = GetMemAvailReal() + GetMemAvailSwap();

	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetMemTotalFree", "memTotalFree", ret);
	}

	return ret;
}

AsnInteger GetMemBuffer(){
	long ret = 0L;
	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetMemBuffer", "memBuffer", ret);
	}

	return ret;
}


// キャッシュメモリ
// パフォーマンスモニタで、Memory-System Cache Resident Bytesで取得できる値
// ページング可能な値を返すということで、上記パラメータを選択
AsnInteger GetMemCached(){
	long ret = 0L;
	PDH_STATUS status = ERROR_SUCCESS;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_FMT_COUNTERVALUE fmtValue;

	// 新規クエリーを作成
	status = PdhOpenQuery(NULL, 0, &hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetMemCached", "PdhOpenQuery", status);
	}

	// カウンタをクエリーに追加
	status = PdhAddCounter(hQuery, "\\Memory\\System Cache Resident Bytes", 0, &hCounter);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetMemCached", "PdhAddCounter", status);
	}
	
	// カウンタ値を更新
	status = PdhCollectQueryData(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetMemCached", "PdhCollectQueryData", status);
	}

	// カウンタ値を取得
	status = PdhGetFormattedCounterValue(hCounter, PDH_FMT_LARGE, NULL, &fmtValue);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetMemCached", "PdhGetFormattedCounterValue", status);
	}

	ret = (long) (fmtValue.largeValue / DIV_MEMORY);

	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetMemCached", "memCached", ret);
	}

	// 終了
	status = PdhCloseQuery(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetMemCached", "PdhCloseQuery", status);
	}

	return ret;
}

//1ページあたりのサイズ×ページイン回数から算出
AsnInteger GetSsSwapIn(){
	long ret = 0L;
	PDH_STATUS status = ERROR_SUCCESS;

	//ページサイズが取得されていなかった場合
	if (page_size == -1) {
		GetSystemInfo(&system_info);
		page_size = system_info.dwPageSize;
	}

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_FMT_COUNTERVALUE fmtValue;

	// 新規クエリーを作成
	status = PdhOpenQuery(NULL, 0, &hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetSsSwapIn", "PdhOpenQuery", status);
	}

	// カウンタをクエリーに追加
	status = PdhAddCounter(hQuery, "\\Memory\\Page Reads/sec", 0, &hCounter);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetSsSwapIn", "PdhAddCounter", status);
	}

	// カウンタ値を更新
	status = PdhCollectQueryData(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetSsSwapIn", "PdhCollectQueryData", status);
	}

	Sleep(1000);

	status = PdhCollectQueryData(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetSsSwapIn", "PdhCollectQueryData", status);
	}

	// カウンタ値を取得
	status = PdhGetFormattedCounterValue(hCounter, PDH_FMT_DOUBLE, NULL, &fmtValue);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetSsSwapIn", "PdhGetFormattedCounterValue", status);
	}

	ret = (long)(fmtValue.doubleValue * page_size / DIV_MEMORY);

	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetSsSwapIn", "ssSwapIn", ret);
	}


	// 終了
	status = PdhCloseQuery(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetSsSwapIn", "PdhCloseQuery", status);
	}

	return ret;
}

//1ページあたりのサイズ×ページアウト回数から算出
AsnInteger GetSsSwapOut(){
	long ret = 0L;
	PDH_STATUS status = ERROR_SUCCESS;

	//ページサイズが取得されていなかった場合
	if (page_size == -1) {
		GetSystemInfo(&system_info);
		page_size = system_info.dwPageSize;
	}

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_FMT_COUNTERVALUE fmtValue;

	// 新規クエリーを作成
	status = PdhOpenQuery(NULL, 0, &hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetSsSwapOut", "PdhOpenQuery", status);
	}

	// カウンタをクエリーに追加
	status = PdhAddCounter(hQuery, "\\Memory\\Page Writes/sec", 0, &hCounter);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetSsSwapOut", "PdhAddCounter", status);
	}

	// カウンタ値を更新
	status = PdhCollectQueryData(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetSsSwapOut", "PdhCollectQueryData", status);
	}

	Sleep(1000);

	status = PdhCollectQueryData(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetSsSwapOut", "PdhCollectQueryData", status);
	}

	// カウンタ値を取得
	status = PdhGetFormattedCounterValue(hCounter, PDH_FMT_DOUBLE, NULL, &fmtValue);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetSsSwapOut", "PdhGetFormattedCounterValue", status);
	}

	ret = (long)(fmtValue.doubleValue * page_size / DIV_MEMORY);

	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetSsSwapOut", "ssSwapOut", ret);
	}

	// 終了
	status = PdhCloseQuery(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetSsSwapOut", "PdhCloseQuery", status);
	}

	return ret;
}


AsnInteger GetLaLoadInt1(){
	long ret = 0L;
	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetLaLoadInt1", "laLoadInt.1", ret);
	}

	return ret;
}

AsnInteger GetLaLoadInt2(){
	long ret = 0L;
	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetLaLoadInt2", "laLoadInt.2", ret);
	}

	return ret;
}

AsnInteger GetLaLoadInt3(){
	long ret = 0L;
	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetLaLoadInt3", "laLoadInt.3", ret);
	}

	return ret;
}

AsnInteger GetDiskIOIndex(int index){
	long ret = (long)index;
	if(verbose == 1){
		PostEvtLog( EVENTLOG_INFORMATION_TYPE, "GetDiskIOIndex", "diskIOIndex", ret);
	}

	return ret;
}

char * GetDiskIODevice(char *driveChar){
	strcpy(device, driveChar);
	device[1] = '\0';
	if(verbose == 1){
		PostEvtLogChar( EVENTLOG_INFORMATION_TYPE, "GetDiskIODevice", "diskIODevice", driveChar[0], 0);
	}

	return device;
}


AsnCounter GetDiskIORead(char *driveChar){
	long ret = 0L;
	PDH_STATUS status = ERROR_SUCCESS;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	// 新規クエリーを作成
	status = PdhOpenQuery(NULL, 0, &hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetDiskIORead", "PdhOpenQuery", status);
	}

	// カウンタをクエリーに追加
	string strQuery = "\\LogicalDisk(";
	strQuery.append(driveChar, 1);
	string tmp = ":)\\Disk Read Bytes/sec";
	strQuery.append(tmp);

	status = PdhAddCounter(hQuery, strQuery.c_str(), 0, &hCounter);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLogString( errorEventLevel, "GetDiskIORead", "PdhAddCounter", (char*)strQuery.c_str(), status);
	}

	// カウンタ値を更新
	status = PdhCollectQueryData(hQuery);
	if(verbose == 1 && CheckErrorCode(status) && status != 0x800007d5){	// PDH_NO_DATA(0x800007d5)の場合はイベントを出さない
		PostEvtLogString( errorEventLevel, "GetDiskIORead", "PdhCollectQueryData", (char*)strQuery.c_str(), status);
	}

	// カウンタ値を取得
	status = PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLogString( errorEventLevel, "GetDiskIORead", "PdhGetRawCounterValue", (char*)strQuery.c_str(), status);
	}

	ret = (long)rawValue.FirstValue;

	if(verbose == 1){
		PostEvtLogString( EVENTLOG_INFORMATION_TYPE, "GetDiskIORead", "diskIONRead", (char*)strQuery.c_str(), ret);
	}

	// 終了
	status = PdhCloseQuery(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetDiskIORead", "PdhCloseQuery", status);
	}

	return ret;
}


AsnCounter GetDiskIOWrite(char *driveChar){
	long ret = 0L;
	PDH_STATUS status = ERROR_SUCCESS;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	// 新規クエリーを作成
	status = PdhOpenQuery(NULL, 0, &hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetDiskIOWrite", "PdhOpenQuery", status);
	}

	// カウンタをクエリーに追加
	string strQuery = "\\LogicalDisk(";
	strQuery.append(driveChar, 1);
	string tmp = ":)\\Disk Write Bytes/sec";
	strQuery.append(tmp);

	status = PdhAddCounter(hQuery, strQuery.c_str(), 0, &hCounter);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLogString( errorEventLevel, "GetDiskIOWrite", "PdhAddCounter", (char*)strQuery.c_str(), status);
	}

	// カウンタ値を更新
	status = PdhCollectQueryData(hQuery);
	if(verbose == 1 && CheckErrorCode(status) && status != 0x800007d5){	// PDH_NO_DATA(0x800007d5)の場合はイベントを出さない
		PostEvtLogString( errorEventLevel, "GetDiskIOWrite", "PdhCollectQueryData", (char*)strQuery.c_str(), status);
	}

	// カウンタ値を取得
	status = PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLogString( errorEventLevel, "GetDiskIOWrite", "PdhGetRawCounterValue", (char*)strQuery.c_str(), status);
	}

	ret = (long)rawValue.FirstValue;

	if(verbose == 1){
		PostEvtLogString( EVENTLOG_INFORMATION_TYPE, "GetDiskIOWrite", "diskIONWritten", (char*)strQuery.c_str(), ret);
	}

	// 終了
	status = PdhCloseQuery(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetDiskIOWrite", "PdhCloseQuery", status);
	}

	return ret;
}


AsnCounter GetDiskIOReads(char *driveChar){
	long ret = 0L;
	PDH_STATUS status = ERROR_SUCCESS;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	// 新規クエリーを作成
	status = PdhOpenQuery(NULL, 0, &hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetDiskIOReads", "PdhOpenQuery", status);
	}

	// カウンタをクエリーに追加
	string strQuery = "\\LogicalDisk(";
	strQuery.append(driveChar, 1);
	string tmp = ":)\\Disk Reads/sec";
	strQuery.append(tmp);

	status = PdhAddCounter(hQuery, strQuery.c_str(), 0, &hCounter);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLogString( errorEventLevel, "GetDiskIOReads", "PdhAddCounter", (char*)strQuery.c_str(), status);
	}

	// カウンタ値を更新
	status = PdhCollectQueryData(hQuery);
	if(verbose == 1 && CheckErrorCode(status) && status != 0x800007d5){	// PDH_NO_DATA(0x800007d5)の場合はイベントを出さない
		PostEvtLogString( errorEventLevel, "GetDiskIOReads", "PdhCollectQueryData", (char*)strQuery.c_str(), status);
	}

	// カウンタ値を取得
	status = PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLogString( errorEventLevel, "GetDiskIOReads", "PdhGetRawCounterValue", (char*)strQuery.c_str(), status);
	}

	ret = (long)rawValue.FirstValue;

	if(verbose == 1){
		PostEvtLogString( EVENTLOG_INFORMATION_TYPE, "GetDiskIOReads", "diskIOReads", (char*)strQuery.c_str(), ret);
	}

	// 終了
	status = PdhCloseQuery(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetDiskIOReads", "PdhCloseQuery", status);
	}

	return ret;
}

AsnCounter GetDiskIOWrites(char *driveChar){
	long ret = 0L;
	PDH_STATUS status = ERROR_SUCCESS;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	// 新規クエリーを作成
	status = PdhOpenQuery(NULL, 0, &hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetDiskIOWrites", "PdhOpenQuery", status);
	}

	// カウンタをクエリーに追加
	string strQuery = "\\LogicalDisk(";
	strQuery.append(driveChar, 1);
	string tmp = ":)\\Disk Writes/sec";
	strQuery.append(tmp);

	status = PdhAddCounter(hQuery, strQuery.c_str(), 0, &hCounter);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLogString( errorEventLevel, "GetDiskIOWrites", "PdhAddCounter", (char*)strQuery.c_str(), status);
	}

	// カウンタ値を更新
	status = PdhCollectQueryData(hQuery);
	if(verbose == 1 && CheckErrorCode(status) && status != 0x800007d5){	// PDH_NO_DATA(0x800007d5)の場合はイベントを出さない
		PostEvtLogString( errorEventLevel, "GetDiskIOWrites", "PdhCollectQueryData", (char*)strQuery.c_str(), status);
	}

	// カウンタ値を取得
	status = PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLogString( errorEventLevel, "GetDiskIOWrites", "PdhGetRawCounterValue", (char*)strQuery.c_str(), status);
	}

	ret = (long)rawValue.FirstValue;

	if(verbose == 1){
		PostEvtLogString( EVENTLOG_INFORMATION_TYPE, "GetDiskIOWrites", "diskIOWrites", (char*)strQuery.c_str(), ret);
	}

	// 終了
	status = PdhCloseQuery(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetDiskIOWrites", "PdhCloseQuery", status);
	}

	return ret;
}


// PagingFileの使用率を％で返す
double GetPagingFileUsage(void) {
	double ret = 0;
	PDH_STATUS status = ERROR_SUCCESS;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_FMT_COUNTERVALUE fmtValue;

	int totalSwap = GetMemTotalSwap();

	// 新規クエリーを作成
	status = PdhOpenQuery(NULL, 0, &hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetPagingFileUsage", "PdhOpenQuery", status);
	}

	// カウンタをクエリーに追加
	status = PdhAddCounter(hQuery, "\\Paging File(_Total)\\% Usage", 0, &hCounter);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetPagingFileUsage", "PdhAddCounter", status);
	}
	
	// カウンタ値を更新
	status = PdhCollectQueryData(hQuery);
	// PDH_NO_DATA(0x800007d5)の場合はページングファイル使用率を0とする。
	if(status == 0x800007d5) {
		return 0;
	}
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetPagingFileUsage", "PdhCollectQueryData", status);
	}

	// カウンタ値を取得
	status = PdhGetFormattedCounterValue(hCounter, PDH_FMT_DOUBLE, NULL, &fmtValue);
	// PDH_INVALID_DATA(0xC0000BC6)場合(ページングファイルなし設定)はページングファイル使用率を0とする。
	if(status == 0xC0000BC6) {
		return 0;
	}
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetPagingFileUsage", "PdhGetFormattedCounterValue", status);
	}

	ret = fmtValue.doubleValue;

	if(verbose == 1){
		PostEvtLogDouble( EVENTLOG_INFORMATION_TYPE, "GetPagingFileUsage", "pagingFileUsage", ret);
	}

	// 終了
	status = PdhCloseQuery(hQuery);
	if(verbose == 1 && CheckErrorCode(status)){
		PostEvtLog( errorEventLevel, "GetPagingFileUsage", "PdhCloseQuery", status);
	}
	
	return ret;

}

AsnInteger GetDiskPercent(char *driveChar){
	long ret = 0L;
	ULARGE_INTEGER i64Used;
    ULARGE_INTEGER i64Free;
    ULARGE_INTEGER i64Avail;
    ULARGE_INTEGER i64Total;

	// 必ず C:\ の形式とする (最後は \ となること)
	strcpy(driveStr, driveChar);
	driveStr[1] = ':';
	driveStr[2] = '\\';
	driveStr[3] = '\0';

	if (0 == GetDiskFreeSpaceEx(driveStr, &i64Free, &i64Total, &i64Avail))
	{
		// 指定されたディスクの容量に関する情報が取得できない場合
		// ex. フロッピードライブや光学ドライブなどがマウントされていないなど。
		DWORD errorCode = GetLastError();
		if (verbose == 1)
		{
			// エラーコードが 21(x15)=ERROR_NOT_READY の場合、デバイスの準備ができていない(メディアが挿入されていない)だけ
			if (errorCode != ERROR_NOT_READY)
			{
				PostEvtLogChar(errorEventLevel, "GetDiskPercent", "drive", driveChar[0], 1);
				PostEvtLog(errorEventLevel, "GetDiskPercent", "GetDiskFreeSpaceEx", errorCode);
			}
		}
		// この場合は -1 とする。
		return -1L;
	}

	// 使用済みを算出
	i64Used.QuadPart = i64Total.QuadPart - i64Avail.QuadPart;
	ret = (i64Used.QuadPart *100) / i64Total.QuadPart;
	
	if(verbose == 1)
	{
		PostEvtLogChar(EVENTLOG_INFORMATION_TYPE, "GetDiskPercent", "drive", driveChar[0], 1);
		PostEvtLog(EVENTLOG_INFORMATION_TYPE, "GetDiskPercent", "i64Used", i64Used.QuadPart);
		PostEvtLog(EVENTLOG_INFORMATION_TYPE, "GetDiskPercent", "i64Avail", i64Avail.QuadPart);
		PostEvtLog(EVENTLOG_INFORMATION_TYPE, "GetDiskPercent", "i64Total", i64Total.QuadPart);
		PostEvtLog(EVENTLOG_INFORMATION_TYPE, "GetDiskPercent", "Parcent", ret);
	}

	return ret;
}

char * GetDiskPath(char *driveChar){

	// ドライブ名の作成
	strcpy(driveStr, driveChar);
	driveStr[1] = ':';
	driveStr[2] = '\\';
	driveStr[3] = '\0';

	if(verbose == 1){
		PostEvtLogChar(EVENTLOG_INFORMATION_TYPE, "GetDiskPath", "GetDiskPath", driveChar[0], 0);
		PostEvtLogString(EVENTLOG_INFORMATION_TYPE, "GetDiskPath", "GetDiskPath", (char*)driveStr, 0);

	}

	return driveStr;

}

