/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.selfcheck.FileSystemUsageConfig;
import com.clustercontrol.selfcheck.util.FileSystemPoller;
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
		if (!HinemosPropertyCommon.selfcheck_monitoring_filesystem_usage.getBooleanValue()) {
			m_log.debug("skip");
			return;
		}

		this.snmpPort = HinemosPropertyCommon.selfcheck_snmp_port.getIntegerValue();
		String snmpVersionStr = HinemosPropertyCommon.selfcheck_snmp_version.getStringValue();
		snmpVersion = SnmpVersionConstant.stringToType(snmpVersionStr);
		snmpCommunity = HinemosPropertyCommon.selfcheck_snmp_community.getStringValue();
		snmpRetries = HinemosPropertyCommon.selfcheck_snmp_retries.getIntegerValue();
		snmpTimeout = HinemosPropertyCommon.selfcheck_snmp_timeout.getIntegerValue();

		/** ローカル変数 */
		String fsUsageRaw = HinemosPropertyCommon.selfcheck_monitoring_filesystem_usage_list.getStringValue();
		List<FileSystemUsageConfig> fsUsages = new ArrayList<FileSystemUsageConfig>();
		Pattern p = Pattern.compile("(.*):([0-9]+)");
		for (String fs : fsUsageRaw.split(",")) {
			Matcher m = p.matcher(fs);
			if (m.matches()) {
				fsUsages.add(new FileSystemUsageConfig(m.group(1), Integer.parseInt(m.group(2))));
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
	
			if (fileSystemUsage == -1 || fileSystemTotal == -1 || Double.isNaN(fileSystemUsagePer) ) {
				m_log.info("skipped monitoring file system usage. (mountPoint = " + mountPoint + ", threshold = " + thresholdPer + " [%])");
				continue;
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
				continue;
			} 
			String[] msgAttr1 = { mountPoint.replace(":\\", ":/"), String.format("%.2f", fileSystemUsagePer), Integer.toString(thresholdPer)};
			AplLogger.put(InternalIdCommon.SYS_SFC_SYS_002, msgAttr1,
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
