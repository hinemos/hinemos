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


import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.plugin.impl.WebServiceAgentPlugin;
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
		checkForClientQueue("ForClient");
		checkForAgentQueue("ForAgent");
		checkForAgentHubQueue("ForAgentHub");
	}
	
	private void checkForClientQueue(String subKey) {
		if (!HinemosPropertyUtil.getHinemosPropertyBool("selfcheck.monitoring.ws.queue", true)) {
			m_log.debug("skip checkForClientQueue");
			return;
		}

		/** ローカル変数 */
		int queueSize = 0;
		boolean warn = true;
		
		threshold = HinemosPropertyUtil.getHinemosPropertyNum(
				"selfcheck.monitoring.ws.queue.threshold", Long.valueOf(240)).intValue();

		/** メイン処理 */
		queueSize = WebServiceCorePlugin.getQueueSize();
		if (queueSize <= threshold) {
			m_log.debug("web service queue (ForClient) is normal. (queueSize = " + queueSize + ")");
			warn = false;
		}

		if (warn) {
			m_log.info("web service queue (ForClient) is too large. (queueSize = " + queueSize + ")");
		}
		if (!isNotify(subKey, warn)) {
			return;
		}
		
		String port = Integer.toString(getClientPort());
		String[] msgAttr1 = { port, Integer.toString(queueSize), Integer.toString(threshold) };
		AplLogger.put(PriorityConstant.TYPE_WARNING, PLUGIN_ID, MessageConstant.MESSAGE_SYS_008_SYS_SFC, msgAttr1,
				"too many request to Hinemos Manager (tcp:" + port + "). (queued request " +
						queueSize +
						" > threshold " +
						threshold +
				")");
	}
	
	private void checkForAgentQueue(String subKey) {
		if (!HinemosPropertyUtil.getHinemosPropertyBool("selfcheck.monitoring.ws.agent.queue", true)) {
			m_log.debug("skip checkForAgentQueue");
			return;
		}

		/** ローカル変数 */
		int queueSize = 0;
		boolean warn = true;
		
		threshold = HinemosPropertyUtil.getHinemosPropertyNum(
				"selfcheck.monitoring.ws.agent.queue.threshold", Long.valueOf(960)).intValue();

		/** メイン処理 */
		queueSize = WebServiceAgentPlugin.getAgentQueueSize();
		if (queueSize <= threshold) {
			m_log.debug("web service queue (ForAgent) is normal. (queueSize = " + queueSize + ")");
			warn = false;
		}

		if (warn) {
			m_log.info("web service queue (ForAgent) is too large. (queueSize = " + queueSize + ")");
		}
		if (!isNotify(subKey, warn)) {
			return;
		}
		
		String port = Integer.toString(getAgentPort());
		String[] msgAttr1 = { port, Integer.toString(queueSize), Integer.toString(threshold) };
		AplLogger.put(PriorityConstant.TYPE_WARNING, PLUGIN_ID, MessageConstant.MESSAGE_SYS_008_SYS_SFC, msgAttr1,
				"too many request from Agent to Hinemos Manager (tcp:" + port + "). (queued request " +
						queueSize +
						" > threshold " +
						threshold +
				")");
	}
	
	private void checkForAgentHubQueue(String subKey) {
		if (!HinemosPropertyUtil.getHinemosPropertyBool("selfcheck.monitoring.ws.agenthub.queue", true)) {
			m_log.debug("skip checkForAgentHubQueue");
			return;
		}

		/** ローカル変数 */
		int queueSize = 0;
		boolean warn = true;
		
		threshold = HinemosPropertyUtil.getHinemosPropertyNum(
				"selfcheck.monitoring.ws.agenthub.queue.threshold", Long.valueOf(960)).intValue();

		/** メイン処理 */
		queueSize = WebServiceAgentPlugin.getAgentHubQueueSize();
		if (queueSize <= threshold) {
			m_log.debug("web service queue (ForAgentHub) is normal. (queueSize = " + queueSize + ")");
			warn = false;
		}

		if (warn) {
			m_log.info("web service queue (ForAgentHub) is too large. (queueSize = " + queueSize + ")");
		}
		if (!isNotify(subKey, warn)) {
			return;
		}
		
		String port = Integer.toString(getAgentPort());
		String[] msgAttr1 = { port, Integer.toString(queueSize), Integer.toString(threshold) };
		AplLogger.put(PriorityConstant.TYPE_WARNING, PLUGIN_ID, MessageConstant.MESSAGE_SYS_008_SYS_SFC, msgAttr1,
				"too many request from AgentHub to Hinemos Manager (tcp:" + port + "). (queued request " +
						queueSize +
						" > threshold " +
						threshold +
				")");
	}
	
	private int getClientPort() {
		int port = 8080;
		String address = HinemosPropertyUtil.getHinemosPropertyStr("ws.client.address" , "http://0.0.0.0:8080");
		try {
			port = new URL(address).getPort();
		} catch (MalformedURLException e) {
			// ここは通らない
			m_log.warn(e.getMessage());
		}
		return port;
	}
	
	private int getAgentPort() {
		int port = 8081;
		String address = HinemosPropertyUtil.getHinemosPropertyStr("ws.agent.address" , "http://0.0.0.0:8081");
		try {
			port = new URL(address).getPort();
		} catch (MalformedURLException e) {
			// ここは通らない
			m_log.warn(e.getMessage());
		}
		return port;
	}

}
