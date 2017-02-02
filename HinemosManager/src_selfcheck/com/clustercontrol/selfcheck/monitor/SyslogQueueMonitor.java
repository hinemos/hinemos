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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.plugin.impl.SystemLogPlugin;
import com.clustercontrol.util.MessageConstant;
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
		if (!HinemosPropertyUtil.getHinemosPropertyBool("selfcheck.monitoring.systemlog.queue", true)) {
			m_log.debug("skip");
			return;
		}

		/** ローカル変数 */
		int queueSize = 0;
		boolean warn = true;

		threshold = HinemosPropertyUtil.getHinemosPropertyNum(
				"selfcheck.monitoring.systemlog.queue.threshold", Long.valueOf(10000)).intValue();

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
		AplLogger.put(PriorityConstant.TYPE_WARNING, PLUGIN_ID, MessageConstant.MESSAGE_SYS_009_SYS_SFC, msgAttr1,
				"too many syslog to Hinemos Manager. (queued syslog " +
						queueSize +
						" > threshold " +
						threshold +
				")");

		return;
	}

}
