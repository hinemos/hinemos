/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck.monitor;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.plugin.impl.SnmpTrapPlugin;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * 処理待ちsnmptrap数を確認する処理の実装クラス
 */
public class SnmpTrapQueueMonitor extends SelfCheckMonitorBase {

	private static Log m_log = LogFactory.getLog( SnmpTrapQueueMonitor.class );

	public int threshold;

	public final String monitorId = "SYS_SNMPTRAP";
	public final String subKey = "";
	public final String application = "SELFCHECK (snmptrap)";

	/**
	 * コンストラクタ
	 * @param validationQuery 動作確認クエリ
	 */
	public SnmpTrapQueueMonitor() {
	}

	/**
	 * セルフチェック処理名
	 */
	@Override
	public String toString() {
		return "monitoring snmptrap queue (threshold = " + threshold + ")";
	}

	/**
	 * 監視項目ID
	 */
	@Override
	public String getMonitorId() {
		return monitorId;
	}

	/**
	 * snmptrapの処理待ち数の確認処理
	 * @return 通知情報（アプリケーション名は未格納）
	 */
	@Override
	public void execute() {
		if (!HinemosPropertyCommon.selfcheck_monitoring_snmptrap_queue.getBooleanValue()) {
			m_log.debug("skip");
			return;
		}
		
		/** ローカル変数 */
		int queueSize = 0;
		boolean warn = true;
		
		threshold = HinemosPropertyCommon.selfcheck_monitoring_snmptrap_queue_threshold.getIntegerValue();

		/** メイン処理 */
		queueSize = SnmpTrapPlugin.getQueuedCount();

		if (queueSize <= threshold) {
			m_log.debug("snmptrap queue is normal. (queueSize = " + queueSize + ", threshold = " + threshold + ")");
			warn = false;
		}
		if (warn) {
			m_log.info("snmptrap queue is too large. (queueSize = " + queueSize + ", threshold = " + threshold + ")");
		}

		if (!isNotify(subKey, warn)) {
			return;
		}
		String[] msgAttr1 = { Integer.toString(queueSize), Integer.toString(threshold) };
		AplLogger.put(InternalIdCommon.SYS_SFC_SYS_010, msgAttr1,
				"too many snmptrap to Hinemos Manager. (queued snmptrap " +
						queueSize +
						" > threshold " +
						threshold +
				")");

		return;
	}

}
