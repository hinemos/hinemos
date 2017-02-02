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

package com.clustercontrol.selfcheck.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.selfcheck.FileSystemUsageConfig;
import com.clustercontrol.selfcheck.util.FileSystemPoller;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * ファイルシステムの使用率を確認する処理の実装クラス
 */
public class FileSystemMonitor extends SelfCheckMonitorBase {

	private static Log m_log = LogFactory.getLog( FileSystemMonitor.class );

	public final String monitorId = "SYS_FS";
	public final String application = "SELFCHECK (FileSystem)";

	public int snmpPort;
	public int snmpVersion;
	public String snmpCommunity;
	public int snmpRetries;
	public int snmpTimeout;

	/**
	 * コンストラクタ
	 * @param mountPoint 対象となるファイルシステムのマウントポイント
	 * @param fileSystemUsagePer ファイルシステム使用率の上限値
	 * @param snmpPort SNMPポート番号
	 * @param snmpVersion SNMPバージョン
	 * @param snmpCommunity SNMPコミュニティ名
	 * @param snmpRetries SNMPリトライ回数
	 * @param snmpTimeout SNMPタイムアウト[msec]
	 */
	public FileSystemMonitor() {}

	/**
	 * 監視項目ID
	 */
	@Override
	public String getMonitorId() {
		return monitorId;
	}

	/**
	 * ファイルシステムの使用率が上限値以下であるかを確認する処理
	 * @return 通知情報（アプリケーション名は未格納）
	 */
	@Override
	public void execute() {
		if (!HinemosPropertyUtil.getHinemosPropertyBool("selfcheck.monitoring.filesystem.usage", false)) {
			m_log.debug("skip");
			return;
		}

		this.snmpPort = HinemosPropertyUtil.getHinemosPropertyNum("selfcheck.snmp.port", Long.valueOf(161)).intValue();
		String snmpVersionStr = HinemosPropertyUtil.getHinemosPropertyStr("selfcheck.snmp.version", SnmpVersionConstant.STRING_V2);
		snmpVersion = SnmpVersionConstant.stringToType(snmpVersionStr);
		snmpCommunity = HinemosPropertyUtil.getHinemosPropertyStr("selfcheck.snmp.community", "public");
		snmpRetries = HinemosPropertyUtil.getHinemosPropertyNum("selfcheck.snmp.retries", Long.valueOf(3)).intValue();
		snmpTimeout = HinemosPropertyUtil.getHinemosPropertyNum("selfcheck.snmp.timeout", Long.valueOf(3000)).intValue();

		/** ローカル変数 */
		String fsUsageRaw = HinemosPropertyUtil.getHinemosPropertyStr(
				"selfcheck.monitoring.filesystem.usage.list",
				"/:50");
		List<FileSystemUsageConfig> fsUsages = new ArrayList<FileSystemUsageConfig>();
		for (String fs : fsUsageRaw.split(",")) {
			String[] pair = fs.split(":");
			if (pair.length == 2) {
				fsUsages.add(new FileSystemUsageConfig(pair[0], Integer.parseInt(pair[1])));
			}
		}
		List<FileSystemUsageConfig> fsUsageList = Collections.unmodifiableList(fsUsages);

		for (FileSystemUsageConfig config : fsUsageList) {
			int fileSystemUsage = 0;
			int fileSystemTotal = 0;
			double fileSystemUsagePer = 0;
			boolean warn = true;
			String mountPoint = config.mountPoint;
			int thresholdPer = config.percentThreshold;
			String subKey = mountPoint;

			/** メイン処理 */
			m_log.debug("monitoring file system usage. (mountPoint = " + mountPoint + ", thresholdPer = " + thresholdPer + ")");
	
			// 利用可能なヒープ容量をMByte単位で取得する
			try {
				fileSystemUsage = new FileSystemPoller(mountPoint, snmpPort, snmpVersion, snmpCommunity, snmpRetries, snmpTimeout).getFileSystemUsage();
				fileSystemTotal = new FileSystemPoller(mountPoint, snmpPort, snmpVersion, snmpCommunity, snmpRetries, snmpTimeout).getFileSystemTotal();
			} catch (Exception e) {
				m_log.warn("filesystem usage collection failure. (mountPoint = " + mountPoint + ", threshold = " + thresholdPer + " [%])", e);
			}
	
			fileSystemUsagePer = (double)fileSystemUsage / (double)fileSystemTotal * 100.0;
	
			if (fileSystemUsage == -1 || fileSystemTotal == -1) {
				m_log.info("skipped monitoring file system usage. (mountPoint = " + mountPoint + ", threshold = " + thresholdPer + " [%])");
				return;
			} else {
				if (fileSystemUsagePer <= thresholdPer) {
					m_log.debug("usage of file system is low. (mountPoint = " + mountPoint
							+ ", usage = " + String.format("%.2f", fileSystemUsagePer) + " [%], threshold = " + thresholdPer + " [%])");
					warn = false;
				}
			}
			if (warn) {
				m_log.info("usage of file system is high. (mountPoint = " + mountPoint
						+ ", usage = " + String.format("%.2f", fileSystemUsagePer) + " [%], threshold = " + thresholdPer + " [%])");
			}
	
			if (!isNotify(subKey, warn)) {
				return;
			}
			String[] msgAttr1 = { mountPoint, String.format("%.2f", fileSystemUsagePer), Integer.toString(thresholdPer)};
			AplLogger.put(PriorityConstant.TYPE_WARNING, PLUGIN_ID, MessageConstant.MESSAGE_SYS_002_SYS_SFC, msgAttr1,
					"usage of filesystem(" +
							mountPoint +
							") is too high (" +
							String.format("%.2f", fileSystemUsagePer) +
							" [%] > threshold " +
							thresholdPer +
					" [%]).");
		}
		return;
	}

}
