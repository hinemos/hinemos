/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck.monitor;


import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.plugin.impl.RestServiceAgentPlugin;
import com.clustercontrol.plugin.impl.RestServicePlugin;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * RestServiceの処理待ちリクエスト数を確認する処理の実装クラス
 */
public class RestServiceQueueMonitor extends SelfCheckMonitorBase {

	private static Log m_log = LogFactory.getLog( RestServiceQueueMonitor.class );

	private int threshold;

	private String monitorId = "SYS_REST";

	/**
	 * コンストラクタ
	 * @param validationQuery 動作確認クエリ
	 */
	public RestServiceQueueMonitor() {
	}

	/**
	 * セルフチェック処理名
	 */
	@Override
	public String toString() {
		return "monitoring rest service";
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
		checkForAgentBinaryQueue("ForAgentBinary");
		checkForAgentNodeConfigQueue("ForAgentNodeConfig");
	}
	
	private void checkForClientQueue(String subKey) {
		if (!HinemosPropertyCommon.selfcheck_monitoring_rest_queue.getBooleanValue()) {
			m_log.debug("skip checkForClientQueue");
			return;
		}

		/** ローカル変数 */
		int queueSize = 0;
		boolean warn = true;
		
		threshold = HinemosPropertyCommon.selfcheck_monitoring_rest_queue_threshold.getIntegerValue();

		/** メイン処理 */
		queueSize = RestServicePlugin.getQueueSize();
		if (queueSize <= threshold) {
			m_log.debug("rest service queue (ForClient) is normal. (queueSize = " + queueSize + ")");
			warn = false;
		}

		if (warn) {
			m_log.info("rest service queue (ForClient) is too large. (queueSize = " + queueSize + ")");
		}
		if (!isNotify(subKey, warn)) {
			return;
		}
		
		String port = Integer.toString(getClientPort());
		String[] msgAttr1 = { port, Integer.toString(queueSize), Integer.toString(threshold) };
		AplLogger.put(InternalIdCommon.SYS_SFC_SYS_008, msgAttr1,
				"too many request to Hinemos Manager (tcp:" + port + "). (queued request " +
						queueSize +
						" > threshold " +
						threshold +
				")");
	}
	
	private void checkForAgentQueue(String subKey) {
		if (!HinemosPropertyCommon.selfcheck_monitoring_rest_agent_queue.getBooleanValue()) {
			m_log.debug("skip checkForAgentQueue");
			return;
		}

		/** ローカル変数 */
		int queueSize = 0;
		boolean warn = true;
		
		threshold = HinemosPropertyCommon.selfcheck_monitoring_rest_agent_queue_threshold.getIntegerValue();

		/** メイン処理 */
		queueSize = RestServiceAgentPlugin.getAgentQueueSize();
		if (queueSize <= threshold) {
			m_log.debug("rest service queue (ForAgent) is normal. (queueSize = " + queueSize + ")");
			warn = false;
		}

		if (warn) {
			m_log.info("rest service queue (ForAgent) is too large. (queueSize = " + queueSize + ")");
		}
		if (!isNotify(subKey, warn)) {
			return;
		}
		
		String port = Integer.toString(getAgentPort());
		String[] msgAttr1 = { port, Integer.toString(queueSize), Integer.toString(threshold) };
		AplLogger.put(InternalIdCommon.SYS_SFC_SYS_008, msgAttr1,
				"too many request from Agent to Hinemos Manager (tcp:" + port + "). (queued request " +
						queueSize +
						" > threshold " +
						threshold +
				")");
	}
	
	private void checkForAgentHubQueue(String subKey) {
		if (!HinemosPropertyCommon.selfcheck_monitoring_rest_agenthub_queue.getBooleanValue()) {
			m_log.debug("skip checkForAgentHubQueue");
			return;
		}

		/** ローカル変数 */
		int queueSize = 0;
		boolean warn = true;
		
		threshold = HinemosPropertyCommon.selfcheck_monitoring_rest_agenthub_queue_threshold.getIntegerValue();

		/** メイン処理 */
		queueSize = RestServiceAgentPlugin.getAgentHubQueueSize();
		if (queueSize <= threshold) {
			m_log.debug("rest service queue (ForAgentHub) is normal. (queueSize = " + queueSize + ")");
			warn = false;
		}

		if (warn) {
			m_log.info("rest service queue (ForAgentHub) is too large. (queueSize = " + queueSize + ")");
		}
		if (!isNotify(subKey, warn)) {
			return;
		}
		
		String port = Integer.toString(getAgentPort());
		String[] msgAttr1 = { port, Integer.toString(queueSize), Integer.toString(threshold) };
		AplLogger.put(InternalIdCommon.SYS_SFC_SYS_008, msgAttr1,
				"too many request from AgentHub to Hinemos Manager (tcp:" + port + "). (queued request " +
						queueSize +
						" > threshold " +
						threshold +
				")");
	}

	private void checkForAgentBinaryQueue(String subKey) {
		if (!HinemosPropertyCommon.selfcheck_monitoring_rest_agentbinary_queue.getBooleanValue()) {
			m_log.debug("skip checkForAgentBinaryQueue");
			return;
		}

		/** ローカル変数 */
		int queueSize = 0;
		boolean warn = true;

		threshold = HinemosPropertyCommon.selfcheck_monitoring_rest_agentbinary_queue_threshold.getIntegerValue();

		/** メイン処理 */
		queueSize = RestServiceAgentPlugin.getAgentBinaryQueueSize();
		if (queueSize <= threshold) {
			m_log.debug("rest service queue (ForAgentBinary) is normal. (queueSize = " + queueSize + ")");
			warn = false;
		}

		if (warn) {
			m_log.info("rest service queue (ForAgentBinary) is too large. (queueSize = " + queueSize + ")");
		}
		if (!isNotify(subKey, warn)) {
			return;
		}

		String port = Integer.toString(getAgentPort());
		String[] msgAttr1 = { port, Integer.toString(queueSize), Integer.toString(threshold) };
		AplLogger.put(InternalIdCommon.SYS_SFC_SYS_008, msgAttr1,
				"too many request from AgentBinary to Hinemos Manager (tcp:" + port + "). (queued request " + queueSize
						+ " > threshold " + threshold + ")");
	}

	private void checkForAgentNodeConfigQueue(String subKey) {
		if (!HinemosPropertyCommon.selfcheck_monitoring_rest_agentnodeconfig_queue.getBooleanValue()) {
			m_log.debug("skip checkForAgentNodeConfigQueue");
			return;
		}

		/** ローカル変数 */
		int queueSize = 0;
		boolean warn = true;

		threshold = HinemosPropertyCommon.selfcheck_monitoring_rest_agentnodeconfig_queue_threshold.getIntegerValue();

		/** メイン処理 */
		queueSize = RestServiceAgentPlugin.getAgentNodeConfigQueueSize();
		if (queueSize <= threshold) {
			m_log.debug("rest service queue (ForAgentNodeConfig) is normal. (queueSize = " + queueSize + ")");
			warn = false;
		}

		if (warn) {
			m_log.info("rest service queue (ForAgentNodeConfig) is too large. (queueSize = " + queueSize + ")");
		}
		if (!isNotify(subKey, warn)) {
			return;
		}

		String port = Integer.toString(getAgentPort());
		String[] msgAttr1 = { port, Integer.toString(queueSize), Integer.toString(threshold) };
		AplLogger.put(InternalIdCommon.SYS_SFC_SYS_008, msgAttr1,
				"too many request from AgentNodeConfig to Hinemos Manager (tcp:" + port + "). (queued request " + queueSize
						+ " > threshold " + threshold + ")");
	}
	
	private int getClientPort() {
		int port = 8083;
		String address = HinemosPropertyCommon.rest_client_address.getStringValue();
		try {
			port = new URL(address).getPort();
		} catch (MalformedURLException e) {
			// ここは通らない
			m_log.warn(e.getMessage());
		}
		return port;
	}
	
	private int getAgentPort() {
		int port = 8084;
		String address = HinemosPropertyCommon.rest_agent_address.getStringValue();
		try {
			port = new URL(address).getPort();
		} catch (MalformedURLException e) {
			// ここは通らない
			m_log.warn(e.getMessage());
		}
		return port;
	}

}
