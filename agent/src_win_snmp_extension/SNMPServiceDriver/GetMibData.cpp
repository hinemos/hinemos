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

#include <windows.h>
#include <stdio.h>
#include <pdh.h>
#include <winbase.h>
#include <snmp.h>

#include <string>

using namespace std;

double GetPagingFileUsage(void);


//メモリ情報取得用
MEMORYSTATUSEX memstatex;

//Byte->KByte変換用定数
int DIV_MEMORY = 1024;

//100ナノ秒単位で取得されるCPU使用率のカウンタ値を、10ミリ秒単位に変換する定数
int DIV_CPU = 100000;

//ページサイズ取得用
SYSTEM_INFO system_info;

//１ページあたりのサイズ（KByte）
int page_size = -1;

char * device = (char*)malloc(1);

//各関数はint等で返すように実装する

AsnCounter GetCpuRawUser(){
	long ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	/* 新規クエリーを作成 */
	PdhOpenQuery(NULL, 0, &hQuery);

	/* カウンタをクエリーに追加 */
	PdhAddCounter(hQuery, "\\Processor(_Total)\\% User Time", 0, &hCounter);
//	PdhAddCounter(hQuery, "\\Process(_Total)\\% User Time", 0, &hCounter);

	/* 値を収集する */
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)(rawValue.FirstValue / DIV_CPU);

	/* 終了 */
	PdhCloseQuery(hQuery);

	/* プロセッサの数を計算する
	SYSTEM_INFO siSysInfo;
	GetSystemInfo(&siSysInfo);
	
	return ret/siSysInfo.dwNumberOfProcessors;
	*/

	return ret;
}


//Windowsでは取得できないので、0を返す
AsnCounter GetCpuRawNice(){
	return 0;
}


AsnCounter GetCpuRawSystem(){
	long ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	// 新規クエリーを作成 
	PdhOpenQuery(NULL, 0, &hQuery);

	// カウンタをクエリーに追加
	PdhAddCounter(hQuery, "\\Processor(_Total)\\% Privileged Time", 0, &hCounter);
//	PdhAddCounter(hQuery, "\\Process(_Total)\\% Privileged Time", 0, &hCounter);
	
	// 値を収集する
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)(rawValue.FirstValue / DIV_CPU);

	// 終了 
	PdhCloseQuery(hQuery);
	
	/* プロセッサの数を計算する
	SYSTEM_INFO siSysInfo;
	GetSystemInfo(&siSysInfo);
	
	return ret/siSysInfo.dwNumberOfProcessors;
	*/

	return ret;
}


AsnCounter GetCpuRawIdle(){
	long ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	/* 新規クエリーを作成 */
	PdhOpenQuery(NULL, 0, &hQuery);

	/* カウンタをクエリーに追加 */
//	PdhAddCounter(hQuery, "\\Processor(_Total)\\% Idle Time", 0, &hCounter);
	PdhAddCounter(hQuery, "\\Process(Idle)\\% Processor Time", 0, &hCounter);
	
	/* 値を収集する */
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)(rawValue.FirstValue / DIV_CPU);

	/* 終了 */
	PdhCloseQuery(hQuery);
	
	/* プロセッサの数を計算する */
	SYSTEM_INFO siSysInfo;
	GetSystemInfo(&siSysInfo);
	
	return ret/siSysInfo.dwNumberOfProcessors;
}


AsnCounter GetCpuRawWait(){
	long ret = 0;
	return ret;
}

AsnCounter GetCpuRawKernel(){
	long ret = 0;
	return ret;
}

AsnCounter GetCpuRawInterrupt(){
	long ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	/* 新規クエリーを作成 */
	PdhOpenQuery(NULL, 0, &hQuery);

	/* カウンタをクエリーに追加 */
	PdhAddCounter(hQuery, "\\Processor(_Total)\\% Interrupt Time", 0, &hCounter);
	
	/* 値を収集する */
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)(rawValue.FirstValue / DIV_CPU);

	/* 終了 */
	PdhCloseQuery(hQuery);
	
	return ret;
}

AsnCounter GetRawInterrupts(){
	long ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	/* 新規クエリーを作成 */
	PdhOpenQuery(NULL, 0, &hQuery);

	/* カウンタをクエリーに追加 */
	PdhAddCounter(hQuery, "\\Processor(_Total)\\Interrupts/sec", 0, &hCounter);
	
	/* 値を収集する */
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)(rawValue.FirstValue / DIV_CPU);

	/* 終了 */
	PdhCloseQuery(hQuery);
	
	return ret;
}

AsnCounter GetRawContexts(){
	long ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	/* 新規クエリーを作成 */
	PdhOpenQuery(NULL, 0, &hQuery);

	/* カウンタをクエリーに追加 */
	PdhAddCounter(hQuery, "\\System\\Context Switches/sec", 0, &hCounter);
	
	/* 値を収集する */
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)(rawValue.FirstValue / DIV_CPU);

	/* 終了 */
	PdhCloseQuery(hQuery);
	
	return ret;
}


// 仮想メモリの合計を返す
AsnInteger GetMemTotalReal(){
	memstatex.dwLength = sizeof(memstatex);
	GlobalMemoryStatusEx(&memstatex);
	return memstatex.ullTotalPhys / DIV_MEMORY;
}

// スワップの合計（ページファイルのサイズ）を返す
// スワップ合計 = コミットチャージの制限値 - 物理メモリ合計
AsnInteger GetMemTotalSwap(){
	memstatex.dwLength = sizeof(memstatex);
	GlobalMemoryStatusEx(&memstatex);
	return (memstatex.ullTotalPageFile  - memstatex.ullTotalPhys) / DIV_MEMORY;
}

// メモリの空き容量を返す
// タスクマネージャの利用可能の値
AsnInteger GetMemAvailReal(){
	memstatex.dwLength = sizeof(memstatex);
	GlobalMemoryStatusEx(&memstatex);
	return memstatex.ullAvailPhys / DIV_MEMORY;
}

// スワップの空き容量を返す
// ページファイルのサイズと使用率から算出する
// ページファイルのサイズ = コミットチャージの制限値 - 物理メモリ合計
AsnInteger GetMemAvailSwap(){
	return  (100 - GetPagingFileUsage()) * 	GetMemTotalSwap() / 100;
}

// メモリ空き容量とスワップ空き容量の和
AsnInteger GetMemTotalFree(){
	return GetMemAvailReal() + GetMemAvailSwap();
}

AsnInteger GetMemBuffer(){
	int ret = 0;
	return ret;
}


// キャッシュメモリ
// パフォーマンスモニタで、Memory-System Cache Resident Bytesで取得できる値
// ページング可能な値を返すということで、上記パラメータを選択
AsnInteger GetMemCached(){
	int ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_FMT_COUNTERVALUE fmtValue;

	// 新規クエリーを作成
	PdhOpenQuery(NULL, 0, &hQuery);

	/* カウンタをクエリーに追加 */
	PdhAddCounter(hQuery, "\\Memory\\System Cache Resident Bytes", 0, &hCounter);
	
	// 値を収集する
	PdhCollectQueryData(hQuery);
	PdhGetFormattedCounterValue(hCounter, PDH_FMT_LONG, NULL, &fmtValue);
	ret = fmtValue.longValue;

	// 終了
	PdhCloseQuery(hQuery);
	
	return ret / DIV_MEMORY;
}

//1ページあたりのサイズ×ページイン回数から算出
AsnInteger GetSsSwapIn(){

	int ret = 0;

	//ページサイズが取得されていなかった場合
	if (page_size == -1) {
		GetSystemInfo(&system_info);
		page_size = system_info.dwPageSize;
	}

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_FMT_COUNTERVALUE fmtValue;

	// 新規クエリーを作成
	PdhOpenQuery(NULL, 0, &hQuery);

	/* カウンタをクエリーに追加 */
	PdhAddCounter(hQuery, "\\Memory\\Page Reads/sec", 0, &hCounter);
	
	// 値を収集する
	PdhCollectQueryData(hQuery);
	PdhGetFormattedCounterValue(hCounter, PDH_FMT_LONG, NULL, &fmtValue);
	ret = fmtValue.longValue * page_size / DIV_MEMORY;

	// 終了
	PdhCloseQuery(hQuery);
	

	return ret;
}

//1ページあたりのサイズ×ページアウト回数から算出
AsnInteger GetSsSwapOut(){

	int ret = 0;

	//ページサイズが取得されていなかった場合
	if (page_size == -1) {
		GetSystemInfo(&system_info);
		page_size = system_info.dwPageSize;
	}

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_FMT_COUNTERVALUE fmtValue;

	// 新規クエリーを作成
	PdhOpenQuery(NULL, 0, &hQuery);

	/* カウンタをクエリーに追加 */
	PdhAddCounter(hQuery, "\\Memory\\Page Writes/sec", 0, &hCounter);
	
	// 値を収集する
	PdhCollectQueryData(hQuery);
	PdhGetFormattedCounterValue(hCounter, PDH_FMT_LONG, NULL, &fmtValue);
	ret = fmtValue.longValue * page_size / DIV_MEMORY;

	// 終了
	PdhCloseQuery(hQuery);

	return ret;
}


AsnInteger GetLaLoadInt1(){
	int ret = 0;
	return ret;
}

AsnInteger GetLaLoadInt2(){
	int ret = 0;
	return ret;
}

AsnInteger GetLaLoadInt3(){
	int ret = 0;
	return ret;
}

AsnInteger GetDiskIOIndex(char *driveChar){
	long ret = 0;

	return ret;
}

char * GetDiskIODevice(char *driveChar){
	strcpy(device, driveChar);
	return device;
}


AsnCounter GetDiskIORead(char *driveChar){
	long ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	// 新規クエリーを作成
	PdhOpenQuery(NULL, 0, &hQuery);

	string strQuery = "\\LogicalDisk(";
	strQuery.append(driveChar, 1);
	string tmp = ":)\\Disk Read Bytes/sec";
	strQuery.append(tmp);

	PdhAddCounter(hQuery, strQuery.c_str(), 0, &hCounter);
	
	// 値を収集する
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)rawValue.FirstValue;

	// 終了
	PdhCloseQuery(hQuery);
	
	return ret;
}


AsnCounter GetDiskIOWrite(char *driveChar){
	long ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	// 新規クエリーを作成
	PdhOpenQuery(NULL, 0, &hQuery);

	string strQuery = "\\LogicalDisk(";
	strQuery.append(driveChar, 1);
	string tmp = ":)\\Disk Write Bytes/sec";
	strQuery.append(tmp);

	PdhAddCounter(hQuery, strQuery.c_str(), 0, &hCounter);
	
	// 値を収集する
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)rawValue.FirstValue;

	// 終了
	PdhCloseQuery(hQuery);
	
	return ret;
}


AsnCounter GetDiskIOReads(char *driveChar){
	long ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	// 新規クエリーを作成
	PdhOpenQuery(NULL, 0, &hQuery);

	string strQuery = "\\LogicalDisk(";
	strQuery.append(driveChar, 1);
	string tmp = ":)\\Disk Reads/sec";
	strQuery.append(tmp);


	PdhAddCounter(hQuery, strQuery.c_str(), 0, &hCounter);
	
	// 値を収集する
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)rawValue.FirstValue;

	// 終了
	PdhCloseQuery(hQuery);
	
	return ret;
}


AsnCounter GetDiskIOWrites(char *driveChar){
	long ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	// 新規クエリーを作成
	PdhOpenQuery(NULL, 0, &hQuery);

	string strQuery = "\\LogicalDisk(";
	strQuery.append(driveChar, 1);
	string tmp = ":)\\Disk Writes/sec";
	strQuery.append(tmp);

	PdhAddCounter(hQuery, strQuery.c_str(), 0, &hCounter);
	
	// 値を収集する
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)rawValue.FirstValue;

	// 終了
	PdhCloseQuery(hQuery);
	
	return ret;
}


// PagingFileの使用率を％で返す
double GetPagingFileUsage(void) {
	int ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_FMT_COUNTERVALUE fmtValue;

	int totalSwap = GetMemTotalSwap();

	// 新規クエリーを作成
	PdhOpenQuery(NULL, 0, &hQuery);

	/* カウンタをクエリーに追加 */
	PdhAddCounter(hQuery, "\\Paging File(_Total)\\% Usage", 0, &hCounter);
	
	// 値を収集する
	PdhCollectQueryData(hQuery);
	PdhGetFormattedCounterValue(hCounter, PDH_FMT_DOUBLE, NULL, &fmtValue);
	ret = fmtValue.doubleValue;

	// 終了
	PdhCloseQuery(hQuery);
	
	return ret;

}