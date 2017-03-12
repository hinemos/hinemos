/*
 
Copyright (C) 2007 NTT DATA Corporation
 
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
#include <snmp.h>
#include <winbase.h>

#include <string>
#include <map>
#include <vector>

using namespace std;

#define MIB_PREFIX_LEN            MIB_OidPrefix.idLength
#define MAX_STRING_LEN            255
#define MIB_CQPUB_ON              1
#define MIB_CQPUB_OFF             2
#define MIB_ACCESS_READONLY       0
#define MIB_ACCESS_READWRITE      1
#define TRAP_EVENT_NAME           "HinemosSNMPExtAgent_TRAP"
#define OID_SIZEOF( Oid )         ( sizeof Oid / sizeof(UINT) )


#define MIB_CPU_RAW_USER		0
#define MIB_CPU_RAW_NICE		1
#define MIB_CPU_RAW_SYSTEM		2
#define MIB_CPU_RAW_IDLE		3
#define MIB_CPU_RAW_WAIT		4
#define MIB_CPU_RAW_KERNEL		5
#define MIB_CPU_RAW_INTERRUPT	6

#define MIB_RAW_INTERRUPTS		18
#define MIB_RAW_CONTEXTS		19

#define MIB_MEM_TOTAL_REAL		7
#define MIB_MEM_TOTAL_SWAP		8
#define MIB_MEM_AVAIL_REAL		9
#define MIB_MEM_AVAIL_SWAP		10
#define MIB_MEM_TOTAL_FREE		11
#define MIB_MEM_BUFFER			20
#define MIB_MEM_CACHED			21

#define MIB_SS_SWAP_IN			12
#define MIB_SS_SWAP_OUT			13

#define MIB_DISK_IO_INDEX		22
#define MIB_DISK_IO_DEVICE		23
#define MIB_DISK_IO_READ		14
#define MIB_DISK_IO_WRITE		15
#define MIB_DISK_IO_READS		16
#define MIB_DISK_IO_WRITES		17


#define MIB_DISK_IO_OID_LENGTH  13

bool end_of_mib_view = false;

// -------------------------------------------------------------
//    構造体
// -------------------------------------------------------------
typedef struct mib_entry {
    AsnObjectIdentifier Oid;
	void            *Storage;
	BYTE             Type;
	UINT             Access;
	UINT			 ResourceId;
//  struct mib_entry *MibNext;
} MIB_ENTRY;


// -------------------------------------------------------------
//    ワークエリア
// -------------------------------------------------------------
// SNMPエージェント共通データ
DWORD   dwTimeZero    = 0;        // 時刻調整パラメータ
HANDLE  hSimulateTrap = NULL;     // Trap用システムイベント

map<string, int> oid_index_map;
vector<MIB_ENTRY>  mib_entries;

map<string, char> oid_drivestring;

// この拡張エージェントがサポートするOIDのプレフィックス
UINT OID_Prefix[] = { 1, 3, 6, 1, 4, 1, 2021 };
AsnObjectIdentifier  MIB_OidPrefix = { OID_SIZEOF(OID_Prefix), OID_Prefix };


/********** OID **********/

/**** memory ****/
UINT MIB_memTotalSwap[] = { 1, 3, 6, 1, 4, 1, 2021, 4, 3, 0 };
UINT MIB_memAvailSwap[] = { 1, 3, 6, 1, 4, 1, 2021, 4, 4, 0 };
UINT MIB_memTotalReal[] = { 1, 3, 6, 1, 4, 1, 2021, 4, 5, 0 };
UINT MIB_memAvailReal[] = { 1, 3, 6, 1, 4, 1, 2021, 4, 6, 0 };
UINT MIB_memTotalFree[] = { 1, 3, 6, 1, 4, 1, 2021, 4, 11, 0 };
UINT MIB_memBuffer[]    = { 1, 3, 6, 1, 4, 1, 2021, 4, 14, 0 };
UINT MIB_memCached[]    = { 1, 3, 6, 1, 4, 1, 2021, 4, 15, 0 };


/**** load average ****/
UINT MIB_laLoadInt_1[]       = { 1, 3, 6, 1, 4, 1, 2021, 10, 1, 5, 1 };
UINT MIB_laLoadInt_2[]       = { 1, 3, 6, 1, 4, 1, 2021, 10, 1, 5, 2 };
UINT MIB_laLoadInt_3[]       = { 1, 3, 6, 1, 4, 1, 2021, 10, 1, 5, 3 };


/**** systemStats ****/
UINT MIB_ssSwapIn[]     = { 1, 3, 6, 1, 4, 1, 2021, 11, 3, 0 };
UINT MIB_ssSwapOut[]    = { 1, 3, 6, 1, 4, 1, 2021, 11, 4, 0 };

UINT MIB_ssCpuRawUser[]      = { 1, 3, 6, 1, 4, 1, 2021, 11, 50, 0 };
UINT MIB_ssCpuRawNice[]      = { 1, 3, 6, 1, 4, 1, 2021, 11, 51, 0 };
UINT MIB_ssCpuRawSystem[]    = { 1, 3, 6, 1, 4, 1, 2021, 11, 52, 0 };
UINT MIB_ssCpuRawIdle[]      = { 1, 3, 6, 1, 4, 1, 2021, 11, 53, 0 };
UINT MIB_ssCpuRawWait[]      = { 1, 3, 6, 1, 4, 1, 2021, 11, 54, 0 };
UINT MIB_ssCpuRawKernel[]    = { 1, 3, 6, 1, 4, 1, 2021, 11, 55, 0 };
UINT MIB_ssCpuRawInterrupt[] = { 1, 3, 6, 1, 4, 1, 2021, 11, 56, 0 };

UINT MIB_ssRawInterrupts[]   = { 1, 3, 6, 1, 4, 1, 2021, 11, 59, 0 };
UINT MIB_ssRawContexts[]     = { 1, 3, 6, 1, 4, 1, 2021, 11, 60, 0 };



/**** diskIOEntry ****/

UINT MIB_diskIOIndex[26][13]  = {{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 1},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 2},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 3},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 4},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 5},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 6},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 7},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 8},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 9},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 10},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 11},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 12},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 13},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 14},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 15},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 16},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 17},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 18},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 19},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 20},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 21},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 22},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 23},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 24},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 25},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 1, 26}};

UINT MIB_diskIODevice[26][13] = {{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 1},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 2},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 3},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 4},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 5},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 6},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 7},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 8},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 9},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 10},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 11},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 12},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 13},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 14},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 15},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 16},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 17},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 18},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 19},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 20},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 21},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 22},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 23},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 24},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 25},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 2, 26}};

UINT MIB_diskIORead[26][13]   = {{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 1},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 2},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 3},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 4},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 5},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 6},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 7},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 8},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 9},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 10},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 11},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 12},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 13},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 14},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 15},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 16},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 17},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 18},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 19},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 20},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 21},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 22},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 23},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 24},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 25},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 3, 26}};


UINT MIB_diskIOWrite[26][13]   = {{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 1},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 2},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 3},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 4},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 5},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 6},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 7},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 8},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 9},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 10},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 11},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 12},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 13},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 14},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 15},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 16},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 17},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 18},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 19},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 20},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 21},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 22},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 23},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 24},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 25},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 4, 26}};

UINT MIB_diskIOReads[26][13]   = {{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 1},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 2},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 3},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 4},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 5},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 6},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 7},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 8},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 9},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 10},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 11},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 12},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 13},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 14},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 15},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 16},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 17},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 18},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 19},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 20},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 21},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 22},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 23},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 24},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 25},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 5, 26}};


UINT MIB_diskIOWrites[26][13]   = {{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 1},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 2},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 3},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 4},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 5},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 6},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 7},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 8},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 9},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 10},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 11},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 12},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 13},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 14},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 15},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 16},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 17},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 18},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 19},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 20},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 21},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 22},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 23},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 24},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 25},
								{ 1, 3, 6, 1, 4, 1, 2021, 13, 15, 1, 1, 6, 26}};


//oid定義情報へのポインタ
UINT *MIB_diskIOIndexPtr = &MIB_diskIOIndex[0][0];
UINT *MIB_diskIODevicePtr = &MIB_diskIODevice[0][0];
UINT *MIB_diskIOReadPtr = &MIB_diskIORead[0][0];
UINT *MIB_diskIOWritePtr = &MIB_diskIOWrite[0][0];
UINT *MIB_diskIOReadsPtr = &MIB_diskIOReads[0][0];
UINT *MIB_diskIOWritesPtr = &MIB_diskIOWrites[0][0];

vector<AsnObjectIdentifier> MIB_diskIO;
/*************************/


/**** 収集値格納用変数 ****/

AsnInteger MIB_dat_memTotalSwap = 0;
AsnInteger MIB_dat_memAvailSwap = 0;
AsnInteger MIB_dat_memTotalReal = 0;
AsnInteger MIB_dat_memAvailReal = 0;
AsnInteger MIB_dat_memTotalFree = 0;

AsnInteger MIB_dat_memBuffer = 0;
AsnInteger MIB_dat_memCached = 0;

AsnInteger MIB_dat_laLoadInt_1   = 0;
AsnInteger MIB_dat_laLoadInt_2   = 0;
AsnInteger MIB_dat_laLoadInt_3   = 0;

AsnInteger MIB_dat_ssSwapIn = 0;
AsnInteger MIB_dat_ssSwapOut = 0;

AsnCounter MIB_dat_ssCpuRawUser      = 0;
AsnCounter MIB_dat_ssCpuRawNice      = 0;
AsnCounter MIB_dat_ssCpuRawSystem    = 0;
AsnCounter MIB_dat_ssCpuRawIdle      = 0;
AsnCounter MIB_dat_ssCpuRawWait      = 0;
AsnCounter MIB_dat_ssCpuRawKernel    = 0;
AsnCounter MIB_dat_ssCpuRawInterrupt = 0;

AsnCounter MIB_dat_ssRawInterrupts   = 0;
AsnCounter MIB_dat_ssRawContexts     = 0;


map<string, AsnCounter> MIB_dat_diskIO;
map<string, char> MIB_dat_diskIODevice;


vector<AsnCounter> MIB_dat_diskIORead;
vector<AsnCounter> MIB_dat_diskIOWrite;
vector<AsnCounter> MIB_dat_diskIOReads;
vector<AsnCounter> MIB_dat_diskIOWrites;

