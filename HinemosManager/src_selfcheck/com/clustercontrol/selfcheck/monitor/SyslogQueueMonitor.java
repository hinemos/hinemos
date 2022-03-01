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
import com.clustercontrol.plugin.impl.SystemLogPlugin;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * 処理待ちsyslog数を確認する処理の実装クラス
 */
public class SyslogQueueMonitor extends SelfCheckMonitorBase {

	private static Log m_log = LogFactory.getLog( SyslogQueueMonitor.class );

	public int threshold;

	public final String monitorId = "SYS_SYSLOG";
	public final String subKey = "";
	public final String application = "SELFCHECK (syslog)";

	/**
	 * コンストラクタ
	 * @param validationQuery 動作確認クエリ
	 */
	public SyslogQueueMonitor() {
	}

	/**
	 * セルフチェック処理名
	 */
	@Override
	public String toString() {
		return "monitoring queue of syslog (threshold = " + threshold + ")";
	}

	/**
	 * 監視項目ID
	 */
	@Override
	public String getMonitorId() {
		return monitorId;
	}

	/**
	 * syslogの処理待ち数の確認処理
	 * @return 通知情報（アプリケーション名は未格納）
	 */
	@Override
	public void execute() {
		if (!HinemosPropertyCommon.selfcheck_monitoring_systemlog_queue.getBooleanValue()) {
			m_log.debug("skip");
			return;
		}

		/** ローカル変数 */
		int queueSize = 0;
		boolean warn = true;

		threshold = HinemosPropertyCommon.selfcheck_monitoring_systemlog_queue_threshold.getIntegerValue();

		/** メイン処理 */
		queueSize = SystemLogPlugin.getQueuedCount();

		if (queueSize <= threshold) {
			m_log.debug("syslog queue is normal. (queueSize = " + queueSize + ")");
			warn = false;
		}
		if (warn) {
			m_log.info("syslog queue is too large. (queueSize = " + queueSize + ")");
		}

		if (!isNotify(subKey, warn)) {
			return;
		}
		String[] msgAttr1 = { Integer.toString(queueSize), Integer.toString(threshold) };
		AplLogger.put(InternalIdCommon.SYS_SFC_SYS_009, msgAttr1,
				"too many syslog to Hinemos Manager. (queued syslog " +
						queueSize +
						" > threshold " +
						threshold +
				")");

		return;
	}

}
