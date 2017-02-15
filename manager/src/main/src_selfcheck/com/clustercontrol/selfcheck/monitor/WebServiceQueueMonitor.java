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
import com.clustercontrol.plugin.impl.WebServiceCorePlugin;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * WebServiceの処理待ちリクエスト数を確認する処理の実装クラス
 */
public class WebServiceQueueMonitor extends SelfCheckMonitorBase {

	private static Log m_log = LogFactory.getLog( WebServiceQueueMonitor.class );

	private int threshold;

	private String monitorId = "SYS_WS";
	public final String subKey = "";

	/**
	 * コンストラクタ
	 * @param validationQuery 動作確認クエリ
	 */
	public WebServiceQueueMonitor() {
	}

	/**
	 * セルフチェック処理名
	 */
	@Override
	public String toString() {
		return "monitoring web service";
	}

	/**
	 * 監視項目ID
	 */
	@Override
	public String getMonitorId() {
		return monitorId;
	}

	/**
	 * データソースへの疎通確認
	 * @return 通知情報（アプリケーション名は未格納）
	 */
	@Override
	public void execute() {
		if (!HinemosPropertyUtil.getHinemosPropertyBool("selfcheck.monitoring.ws.queue", true)) {
			m_log.debug("skip");
			return;
		}

		/** ローカル変数 */
		int queueSize = 0;
		boolean warn = true;
		
		threshold = HinemosPropertyUtil.getHinemosPropertyNum(
				"selfcheck.monitoring.ws.queue.threshold", Long.valueOf(10000)).intValue();

		/** メイン処理 */
		queueSize = WebServiceCorePlugin.getQueueSize();
		if (queueSize <= threshold) {
			m_log.debug("web service queue is normal. (queueSize = " + queueSize + ")");
			warn = false;
		}

		if (warn) {
			m_log.info("web service queue is too large. (queueSize = " + queueSize + ")");
		}
		if (!isNotify(subKey, warn)) {
			return;
		}
		String[] msgAttr1 = { Integer.toString(queueSize), Integer.toString(threshold) };
		AplLogger.put(PriorityConstant.TYPE_WARNING, PLUGIN_ID, MessageConstant.MESSAGE_SYS_008_SYS_SFC, msgAttr1,
				"too many request to Hinemos Manager (tcp:8080). (queued request " +
						queueSize +
						" > threshold " +
						threshold +
				")");

		return;
	}

}
