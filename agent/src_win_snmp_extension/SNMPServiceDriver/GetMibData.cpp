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


//���������擾�p
MEMORYSTATUSEX memstatex;

//Byte->KByte�ϊ��p�萔
int DIV_MEMORY = 1024;

//100�i�m�b�P�ʂŎ擾�����CPU�g�p���̃J�E���^�l���A10�~���b�P�ʂɕϊ�����萔
int DIV_CPU = 100000;

//�y�[�W�T�C�Y�擾�p
SYSTEM_INFO system_info;

//�P�y�[�W������̃T�C�Y�iKByte�j
int page_size = -1;

char * device = (char*)malloc(1);

//�e�֐���int���ŕԂ��悤�Ɏ�������

AsnCounter GetCpuRawUser(){
	long ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	/* �V�K�N�G���[���쐬 */
	PdhOpenQuery(NULL, 0, &hQuery);

	/* �J�E���^���N�G���[�ɒǉ� */
	PdhAddCounter(hQuery, "\\Processor(_Total)\\% User Time", 0, &hCounter);
//	PdhAddCounter(hQuery, "\\Process(_Total)\\% User Time", 0, &hCounter);

	/* �l�����W���� */
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)(rawValue.FirstValue / DIV_CPU);

	/* �I�� */
	PdhCloseQuery(hQuery);

	/* �v���Z�b�T�̐����v�Z����
	SYSTEM_INFO siSysInfo;
	GetSystemInfo(&siSysInfo);
	
	return ret/siSysInfo.dwNumberOfProcessors;
	*/

	return ret;
}


//Windows�ł͎擾�ł��Ȃ��̂ŁA0��Ԃ�
AsnCounter GetCpuRawNice(){
	return 0;
}


AsnCounter GetCpuRawSystem(){
	long ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	// �V�K�N�G���[���쐬 
	PdhOpenQuery(NULL, 0, &hQuery);

	// �J�E���^���N�G���[�ɒǉ�
	PdhAddCounter(hQuery, "\\Processor(_Total)\\% Privileged Time", 0, &hCounter);
//	PdhAddCounter(hQuery, "\\Process(_Total)\\% Privileged Time", 0, &hCounter);
	
	// �l�����W����
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)(rawValue.FirstValue / DIV_CPU);

	// �I�� 
	PdhCloseQuery(hQuery);
	
	/* �v���Z�b�T�̐����v�Z����
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

	/* �V�K�N�G���[���쐬 */
	PdhOpenQuery(NULL, 0, &hQuery);

	/* �J�E���^���N�G���[�ɒǉ� */
//	PdhAddCounter(hQuery, "\\Processor(_Total)\\% Idle Time", 0, &hCounter);
	PdhAddCounter(hQuery, "\\Process(Idle)\\% Processor Time", 0, &hCounter);
	
	/* �l�����W���� */
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)(rawValue.FirstValue / DIV_CPU);

	/* �I�� */
	PdhCloseQuery(hQuery);
	
	/* �v���Z�b�T�̐����v�Z���� */
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

	/* �V�K�N�G���[���쐬 */
	PdhOpenQuery(NULL, 0, &hQuery);

	/* �J�E���^���N�G���[�ɒǉ� */
	PdhAddCounter(hQuery, "\\Processor(_Total)\\% Interrupt Time", 0, &hCounter);
	
	/* �l�����W���� */
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)(rawValue.FirstValue / DIV_CPU);

	/* �I�� */
	PdhCloseQuery(hQuery);
	
	return ret;
}

AsnCounter GetRawInterrupts(){
	long ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	/* �V�K�N�G���[���쐬 */
	PdhOpenQuery(NULL, 0, &hQuery);

	/* �J�E���^���N�G���[�ɒǉ� */
	PdhAddCounter(hQuery, "\\Processor(_Total)\\Interrupts/sec", 0, &hCounter);
	
	/* �l�����W���� */
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)(rawValue.FirstValue / DIV_CPU);

	/* �I�� */
	PdhCloseQuery(hQuery);
	
	return ret;
}

AsnCounter GetRawContexts(){
	long ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	/* �V�K�N�G���[���쐬 */
	PdhOpenQuery(NULL, 0, &hQuery);

	/* �J�E���^���N�G���[�ɒǉ� */
	PdhAddCounter(hQuery, "\\System\\Context Switches/sec", 0, &hCounter);
	
	/* �l�����W���� */
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)(rawValue.FirstValue / DIV_CPU);

	/* �I�� */
	PdhCloseQuery(hQuery);
	
	return ret;
}


// ���z�������̍��v��Ԃ�
AsnInteger GetMemTotalReal(){
	memstatex.dwLength = sizeof(memstatex);
	GlobalMemoryStatusEx(&memstatex);
	return memstatex.ullTotalPhys / DIV_MEMORY;
}

// �X���b�v�̍��v�i�y�[�W�t�@�C���̃T�C�Y�j��Ԃ�
// �X���b�v���v = �R�~�b�g�`���[�W�̐����l - �������������v
AsnInteger GetMemTotalSwap(){
	memstatex.dwLength = sizeof(memstatex);
	GlobalMemoryStatusEx(&memstatex);
	return (memstatex.ullTotalPageFile  - memstatex.ullTotalPhys) / DIV_MEMORY;
}

// �������̋󂫗e�ʂ�Ԃ�
// �^�X�N�}�l�[�W���̗��p�\�̒l
AsnInteger GetMemAvailReal(){
	memstatex.dwLength = sizeof(memstatex);
	GlobalMemoryStatusEx(&memstatex);
	return memstatex.ullAvailPhys / DIV_MEMORY;
}

// �X���b�v�̋󂫗e�ʂ�Ԃ�
// �y�[�W�t�@�C���̃T�C�Y�Ǝg�p������Z�o����
// �y�[�W�t�@�C���̃T�C�Y = �R�~�b�g�`���[�W�̐����l - �������������v
AsnInteger GetMemAvailSwap(){
	return  (100 - GetPagingFileUsage()) * 	GetMemTotalSwap() / 100;
}

// �������󂫗e�ʂƃX���b�v�󂫗e�ʂ̘a
AsnInteger GetMemTotalFree(){
	return GetMemAvailReal() + GetMemAvailSwap();
}

AsnInteger GetMemBuffer(){
	int ret = 0;
	return ret;
}


// �L���b�V��������
// �p�t�H�[�}���X���j�^�ŁAMemory-System Cache Resident Bytes�Ŏ擾�ł���l
// �y�[�W���O�\�Ȓl��Ԃ��Ƃ������ƂŁA��L�p�����[�^��I��
AsnInteger GetMemCached(){
	int ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_FMT_COUNTERVALUE fmtValue;

	// �V�K�N�G���[���쐬
	PdhOpenQuery(NULL, 0, &hQuery);

	/* �J�E���^���N�G���[�ɒǉ� */
	PdhAddCounter(hQuery, "\\Memory\\System Cache Resident Bytes", 0, &hCounter);
	
	// �l�����W����
	PdhCollectQueryData(hQuery);
	PdhGetFormattedCounterValue(hCounter, PDH_FMT_LONG, NULL, &fmtValue);
	ret = fmtValue.longValue;

	// �I��
	PdhCloseQuery(hQuery);
	
	return ret / DIV_MEMORY;
}

//1�y�[�W������̃T�C�Y�~�y�[�W�C���񐔂���Z�o
AsnInteger GetSsSwapIn(){

	int ret = 0;

	//�y�[�W�T�C�Y���擾����Ă��Ȃ������ꍇ
	if (page_size == -1) {
		GetSystemInfo(&system_info);
		page_size = system_info.dwPageSize;
	}

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_FMT_COUNTERVALUE fmtValue;

	// �V�K�N�G���[���쐬
	PdhOpenQuery(NULL, 0, &hQuery);

	/* �J�E���^���N�G���[�ɒǉ� */
	PdhAddCounter(hQuery, "\\Memory\\Page Reads/sec", 0, &hCounter);
	
	// �l�����W����
	PdhCollectQueryData(hQuery);
	PdhGetFormattedCounterValue(hCounter, PDH_FMT_LONG, NULL, &fmtValue);
	ret = fmtValue.longValue * page_size / DIV_MEMORY;

	// �I��
	PdhCloseQuery(hQuery);
	

	return ret;
}

//1�y�[�W������̃T�C�Y�~�y�[�W�A�E�g�񐔂���Z�o
AsnInteger GetSsSwapOut(){

	int ret = 0;

	//�y�[�W�T�C�Y���擾����Ă��Ȃ������ꍇ
	if (page_size == -1) {
		GetSystemInfo(&system_info);
		page_size = system_info.dwPageSize;
	}

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_FMT_COUNTERVALUE fmtValue;

	// �V�K�N�G���[���쐬
	PdhOpenQuery(NULL, 0, &hQuery);

	/* �J�E���^���N�G���[�ɒǉ� */
	PdhAddCounter(hQuery, "\\Memory\\Page Writes/sec", 0, &hCounter);
	
	// �l�����W����
	PdhCollectQueryData(hQuery);
	PdhGetFormattedCounterValue(hCounter, PDH_FMT_LONG, NULL, &fmtValue);
	ret = fmtValue.longValue * page_size / DIV_MEMORY;

	// �I��
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

	// �V�K�N�G���[���쐬
	PdhOpenQuery(NULL, 0, &hQuery);

	string strQuery = "\\LogicalDisk(";
	strQuery.append(driveChar, 1);
	string tmp = ":)\\Disk Read Bytes/sec";
	strQuery.append(tmp);

	PdhAddCounter(hQuery, strQuery.c_str(), 0, &hCounter);
	
	// �l�����W����
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)rawValue.FirstValue;

	// �I��
	PdhCloseQuery(hQuery);
	
	return ret;
}


AsnCounter GetDiskIOWrite(char *driveChar){
	long ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	// �V�K�N�G���[���쐬
	PdhOpenQuery(NULL, 0, &hQuery);

	string strQuery = "\\LogicalDisk(";
	strQuery.append(driveChar, 1);
	string tmp = ":)\\Disk Write Bytes/sec";
	strQuery.append(tmp);

	PdhAddCounter(hQuery, strQuery.c_str(), 0, &hCounter);
	
	// �l�����W����
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)rawValue.FirstValue;

	// �I��
	PdhCloseQuery(hQuery);
	
	return ret;
}


AsnCounter GetDiskIOReads(char *driveChar){
	long ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	// �V�K�N�G���[���쐬
	PdhOpenQuery(NULL, 0, &hQuery);

	string strQuery = "\\LogicalDisk(";
	strQuery.append(driveChar, 1);
	string tmp = ":)\\Disk Reads/sec";
	strQuery.append(tmp);


	PdhAddCounter(hQuery, strQuery.c_str(), 0, &hCounter);
	
	// �l�����W����
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)rawValue.FirstValue;

	// �I��
	PdhCloseQuery(hQuery);
	
	return ret;
}


AsnCounter GetDiskIOWrites(char *driveChar){
	long ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_RAW_COUNTER rawValue;

	// �V�K�N�G���[���쐬
	PdhOpenQuery(NULL, 0, &hQuery);

	string strQuery = "\\LogicalDisk(";
	strQuery.append(driveChar, 1);
	string tmp = ":)\\Disk Writes/sec";
	strQuery.append(tmp);

	PdhAddCounter(hQuery, strQuery.c_str(), 0, &hCounter);
	
	// �l�����W����
	PdhCollectQueryData(hQuery);
	PdhGetRawCounterValue(hCounter, NULL, &rawValue);
	ret = (long)rawValue.FirstValue;

	// �I��
	PdhCloseQuery(hQuery);
	
	return ret;
}


// PagingFile�̎g�p�������ŕԂ�
double GetPagingFileUsage(void) {
	int ret = 0;

	HQUERY hQuery;
	HCOUNTER hCounter;
	PDH_FMT_COUNTERVALUE fmtValue;

	int totalSwap = GetMemTotalSwap();

	// �V�K�N�G���[���쐬
	PdhOpenQuery(NULL, 0, &hQuery);

	/* �J�E���^���N�G���[�ɒǉ� */
	PdhAddCounter(hQuery, "\\Paging File(_Total)\\% Usage", 0, &hCounter);
	
	// �l�����W����
	PdhCollectQueryData(hQuery);
	PdhGetFormattedCounterValue(hCounter, PDH_FMT_DOUBLE, NULL, &fmtValue);
	ret = fmtValue.doubleValue;

	// �I��
	PdhCloseQuery(hQuery);
	
	return ret;

}