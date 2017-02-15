/*

Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.selfcheck.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.poller.bean.PollerProtocolConstant;
import com.clustercontrol.poller.impl.Snmp4jPollerImpl;
import com.clustercontrol.poller.util.DataTable;
import com.clustercontrol.poller.util.TableEntry;

/**
 * ファイルシステムの利用状況を確認する処理の実装クラス
 */
public class FileSystemPoller {

	private static Log m_log = LogFactory.getLog( FileSystemPoller.class );

	private String mountPoint = null;

	private int snmpPort = 161;
	private int snmpVersion = SnmpVersionConstant.TYPE_V2;
	private String snmpCommunity = "public";
	private int snmpRetries = 0;
	private int snmpTimeout = 3000;

	private static final String SNMP_POLLING_IPADDRESS = "127.0.0.1";
	private static final String POLLING_TARGET_OID = ".1.3.6.1.2.1.25.2.3.1";
	private static final String STORAGE_DESCR_OID = ".1.3.6.1.2.1.25.2.3.1.3";
	private static final String STORAGE_AllOCATION_UNIT_OID = ".1.3.6.1.2.1.25.2.3.1.4";
	private static final String STORAGE_SIZE_OID = ".1.3.6.1.2.1.25.2.3.1.5";
	private static final String STORAGE_USED_OID = ".1.3.6.1.2.1.25.2.3.1.6";

	/**
	 * コンストラクタ
	 * @param mountPoint 対象となるファイルシステムのマウントポイント
	 * @param fileSystemMaxUsage ファイルシステム使用率の上限値
	 * @param snmpPort SNMPポート番号
	 * @param snmpVersion SNMPバージョン
	 * @param snmpCommunity SNMPコミュニティ名
	 * @param snmpRetries SNMPリトライ回数
	 * @param snmpTimeout SNMPタイムアウト[msec]
	 */
	public FileSystemPoller(String mountPoint, int snmpPort, int snmpVersion, String snmpCommunity, int snmpRetries, int snmpTimeout) {
		this.mountPoint = mountPoint;
		this.snmpPort = snmpPort;
		this.snmpVersion = snmpVersion;
		this.snmpCommunity = snmpCommunity;
		this.snmpRetries = snmpRetries;
		this.snmpTimeout = snmpTimeout;
	}

	/**
	 * ファイルシステムの全体サイズを取得します（MB単位）。
	 */
	public int getFileSystemTotal(){
		return getFileSystemSize(STORAGE_SIZE_OID);
	}

	/**
	 * ファイルシステムの使用サイズを取得します（MB単位）。
	 */
	public int getFileSystemUsage(){
		return getFileSystemSize(STORAGE_USED_OID);
	}

	// 指定のファイルシステムの使用サイズを取得します（MB単位）。
	private int getFileSystemSize(String oid){
		/** ローカル変数 */
		Snmp4jPollerImpl poller = null;
		DataTable dataTable = null;
		Set<TableEntry> mibValues = null;

		String storageSizeOid = null;
		String allocationUnitsOid = null;
		String key = null;
		int index = 0;
		long size = 0;
		long sizeMByte = 0;
		long unit = 0;

		/** メイン処理 */
		if (m_log.isDebugEnabled()) m_log.debug("start snmp polling for getting usage of file system. (mountPoint = " + mountPoint + ")");


		try {
			// 収集対象のOID
			Set<String> oidSet = null;
			oidSet = new HashSet<String>();
			oidSet.add(POLLING_TARGET_OID);

			// ポーラを生成してポーリングを実行
			poller = Snmp4jPollerImpl.getInstance();
			dataTable = poller.polling(
					SNMP_POLLING_IPADDRESS,
					snmpPort,
					snmpVersion,
					snmpCommunity,
					snmpRetries,
					snmpTimeout,
					oidSet,
					null,
					null,
					null,
					null,
					null,
					null);

			// 収集されたMIB値の中から「HOST-RESOURCES-MIB::hrStorageDescr」のものを抽出
			mibValues = dataTable.getValueSetStartWith(getEntryKey(STORAGE_DESCR_OID));

			// snmpdが停止している場合は、mibValuesがnullとなる。
			if (mibValues == null) {
				throw new HinemosUnknown("snmpd is not running");
			}

			// ターゲットのファイルシステムの使用量を取得できるOIDを特定
			for (TableEntry entry : mibValues) {
				// 取得したMIBのリストの中からマウントポイントに一致するもの取得する
				if ( mountPoint.equals(entry.getValue().toString()) ) {
					key = entry.getKey();
					index = Integer.parseInt(key.substring(key.lastIndexOf('.') + 1));

					storageSizeOid = oid + "." + index;
					allocationUnitsOid = STORAGE_AllOCATION_UNIT_OID + "." + index;
				}
			}

			if (storageSizeOid != null && allocationUnitsOid != null) {
				TableEntry sizeEntry = dataTable.getValue(getEntryKey(storageSizeOid));
				size = (Long)sizeEntry.getValue();

				TableEntry unitEntry = dataTable.getValue(getEntryKey(allocationUnitsOid));
				unit = (Long)unitEntry.getValue();

				// Byte単位をMByte単位に変換する
				sizeMByte = (size * unit) / (1024 * 1024);

				if (m_log.isDebugEnabled()) m_log.debug("successful of snmp polling for getting usage of file system. (mountPoint = " + mountPoint + ")");
				return (int)sizeMByte;
			}

		} catch (HinemosUnknown e) {
			m_log.warn("failure in getting a size of a file system with snmp polling. " + e.getMessage());
		}

		// ポーリング結果を取得できなかった場合
		return -1;
	}

	/**
	 * OIDをTableEntryのキーに変換する
	 */
	private String getEntryKey(String oidString){
		return PollerProtocolConstant.PROTOCOL_SNMP + "." + oidString;
	}

	public static ArrayList<String> getOidList(){
		ArrayList<String> oidList = new ArrayList<String>();

		oidList.add(POLLING_TARGET_OID);
		oidList.add(STORAGE_DESCR_OID);
		oidList.add(STORAGE_AllOCATION_UNIT_OID);
		oidList.add(STORAGE_SIZE_OID);
		oidList.add(STORAGE_USED_OID);

		return oidList;
	}

	/**
	 * 単体試験用
	 */
	public static void main(String[] args) {
		String mountPoint = "/";

		FileSystemPoller poller = new FileSystemPoller(mountPoint, 161, SnmpVersionConstant.TYPE_V2, "public", 1, 3000);

		int total = poller.getFileSystemTotal();
		int usage = poller.getFileSystemUsage();

		System.out.println("File System Usage : " + usage + ", File System Total : " + total);
	}
}
