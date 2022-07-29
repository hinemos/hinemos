/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck.monitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.poller.bean.PollerProtocolConstant;
import com.clustercontrol.poller.impl.Snmp4jPollerImpl;
import com.clustercontrol.poller.util.DataTable;
import com.clustercontrol.poller.util.TableEntry;
import com.clustercontrol.util.EnvUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * メモリのスワップアウト発生を確認する処理の実装クラス
 */
public class RAMSwapOutMonitor extends SelfCheckMonitorBase {

	private static Log m_log = LogFactory.getLog( RAMSwapOutMonitor.class );

	public final String monitorId = "SYS_SWAPOUT";
	public final String subKey = "";
	public final String application = "SELFCHECK (swap-out)";

	public long intervalMSec;
	public int snmpPort;
	public int snmpVersion;
	public String snmpCommunity;
	public int snmpRetries;
	public int snmpTimeout;

	public static final String SNMP_POLLING_IPADDRESS = "127.0.0.1";
	public static final String POLLING_TARGET_OID  = ".1.3.6.1.4.1.2021.11.63";
	public static final String RAW_SWAP_OUT_OUT_OID = ".1.3.6.1.4.1.2021.11.63.0";

	private static volatile TableEntry previousMibValue = null;

	/**
	 * コンストラクタ
	 * @param snmpPort SNMPポート番号
	 * @param snmpVersion SNMPバージョン
	 * @param snmpCommunity SNMPコミュニティ名
	 * @param snmpRetries SNMPリトライ回数
	 * @param snmpTimeout SNMPタイムアウト[msec]
	 */
	public RAMSwapOutMonitor() {
	}

	/**
	 * セルフチェック処理名
	 */
	@Override
	public String toString() {
		return "monitoring ram's swap-out.";
	}

	/**
	 * 監視項目ID
	 */
	@Override
	public String getMonitorId() {
		return monitorId;
	}

	/**
	 * スワップアウトの発生を確認する処理
	 * @return 通知情報（アプリケーション名は未格納）
	 */
	@Override
	public void execute() {
		if(!HinemosPropertyCommon.selfcheck_monitoring_swapout.getBooleanValue()) {
			m_log.debug("skip");
			return;
		}
		
		// Windows版マネージャの場合は利用できない旨のINTERNALイベントを出力
		if (EnvUtil.isWindows()) {
			AplLogger.put(InternalIdCommon.SYS_SFC_SYS_018, new String[]{});
			m_log.warn("selfcheck monitoring swapout is unavailable on Windows Manager, skip");
			return;
		}

		/** ローカル変数 */
		long swapOutSize = 0;
		long lastUpdateTime = 0;
		boolean warn = true;

		this.intervalMSec = HinemosPropertyCommon.selfcheck_interval.getNumericValue() * 1000;
		this.snmpPort = HinemosPropertyCommon.selfcheck_snmp_port.getIntegerValue();
		String snmpVersionStr = HinemosPropertyCommon.selfcheck_snmp_version.getStringValue();
		this.snmpVersion = SnmpVersionConstant.stringToType(snmpVersionStr);
		this.snmpCommunity = HinemosPropertyCommon.selfcheck_snmp_community.getStringValue();
		this.snmpRetries = HinemosPropertyCommon.selfcheck_snmp_retries.getIntegerValue();
		this.snmpTimeout = HinemosPropertyCommon.selfcheck_snmp_timeout.getIntegerValue();

		/** メイン処理 */
		if (m_log.isDebugEnabled()) m_log.debug("monitoring swap-out.");

		// 最終収集日時を取得する
		if (previousMibValue != null) {
			lastUpdateTime = previousMibValue.getDate();
		}

		// 利用可能なヒープ容量をMByte単位で取得する
		swapOutSize = getSwapOut();

		if (swapOutSize < 0) {
			m_log.info("skipped monitoring swap-out.");
			return;
		} else if (swapOutSize == 0) {
			m_log.debug("swap-out does not occurred.");
			warn = false;
		}

		if (warn) {
			m_log.info("swap-out occurred. (swapOutSize=" + swapOutSize + ")");
		}
		if (!isNotify(subKey, warn)) {
			return;
		}
		String[] msgAttr1 = { Long.toString(swapOutSize) };
		AplLogger.put(InternalIdCommon.SYS_SFC_SYS_005, msgAttr1,
				"ram swap-out(" +
						swapOutSize +
						" [blocks]) occurred since " +
						String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", new Date(lastUpdateTime)) +
				".");

		return;
	}

	/**
	 * Swap Out のカウント値の前回ポーリング時との差分を求めます。
	 * ポーリングに失敗した場合、前回収集から収集間隔の2倍以上経過している場合は、
	 * -1を返します。
	 */
	private long getSwapOut(){
		
		Snmp4jPollerImpl poller = null;
		DataTable dataTable = null;
		TableEntry entry = null;

		long now = 0;
		long lastUpdateTime = 0;

		long previousSwapOut = 0;
		long currentSwapOut = 0;

		long swapOut = -1;

		/** メイン処理 */
		try {
			// 収集対象のOID
			Set<String> oidSet = new HashSet<String>();
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

			entry = dataTable.getValue(getEntryKey(RAW_SWAP_OUT_OUT_OID));

			// 前回収集値と比較する
			if (previousMibValue != null) {
				now = HinemosTime.currentTimeMillis();

				// 前回収集からの経過時間を算出
				lastUpdateTime = previousMibValue.getDate();

				// 収集間隔の2倍以上古い場合は判定しない
				if ((now - lastUpdateTime) < intervalMSec * 2) {
					previousSwapOut = (Long)previousMibValue.getValue();
					currentSwapOut = (Long)entry.getValue();

					// 単純に差分を求める（カウンタ値の桁溢れは考慮しない）
					swapOut = currentSwapOut - previousSwapOut;
				}
			}

			// 次回のために、現在値を前回収集値として格納する
			previousMibValue = entry;

		} catch (Exception e) {
			m_log.warn("failed to snmp polling.", e);
		}

		// 取得できなかった場合、もしくは判定できなかった場合
		return swapOut;
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
		oidList.add(RAW_SWAP_OUT_OUT_OID);

		return oidList;
	}

	/**
	 * 単体試験用
	 */
	public static void main(String[] args) {
		long swapOut = new RAMSwapOutMonitor().getSwapOut();

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		swapOut = new RAMSwapOutMonitor().getSwapOut();

		System.out.println("Usage : " + swapOut);
	}
}
