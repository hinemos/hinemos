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
import com.clustercontrol.plugin.impl.CustomTrapPlugin;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * 処理待ちカスタムトラップ数を確認する処理の実装クラス
 */
public class CustomTrapQueueMonitor extends SelfCheckMonitorBase {

	private static Log m_log = LogFactory.getLog( CustomTrapQueueMonitor.class );

	private int threshold;

	private static final String monitorId = "SYS_CUSTOMTRAP";
	private static final String subKey = "";

	/**
	 * コンストラクタ
	 * @param validationQuery 動作確認クエリ
	 */
	public CustomTrapQueueMonitor() {
	}

	/**
	 * セルフチェック処理名
	 */
	@Override
	public String toString() {
		return "monitoring customtrap queue (threshold = " + threshold + ")";
	}

	/**
	 * 監視項目ID
	 */
	@Override
	public String getMonitorId() {
		return monitorId;
	}

	/**
	 * カスタムトラップの処理待ち数の確認処理
	 * @return 通知情報（アプリケーション名は未格納）
	 */
	@Override
	public void execute() {
		if (!HinemosPropertyCommon.selfcheck_monitoring_customtrap_queue.getBooleanValue()) {
			m_log.debug("skip");
			return;
		}
		
		/** ローカル変数 */
		int queueSize = 0;
		boolean warn = true;
		
		threshold = HinemosPropertyCommon.selfcheck_monitoring_customtrap_queue_threshold.getIntegerValue();

		/** メイン処理 */
		queueSize = CustomTrapPlugin.getQueuedCount();

		if (queueSize <= threshold) {
			m_log.debug("customtrap queue is normal. (queueSize = " + queueSize + ", threshold = " + threshold + ")");
			warn = false;
		}
		if (warn) {
			m_log.info("customtrap queue is too large. (queueSize = " + queueSize + ", threshold = " + threshold + ")");
		}

		if (!isNotify(subKey, warn)) {
			return;
		}
		String[] msgAttr1 = { Integer.toString(queueSize), Integer.toString(threshold) };
		AplLogger.put(InternalIdCommon.SYS_SFC_SYS_031, msgAttr1,
				"too many customtrap to Hinemos Manager. (queued customtrap " +
						queueSize +
						" > threshold " +
						threshold +
				")");

		return;
	}

}
