/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logfile.util;


import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.hinemosagent.bean.TopicInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;

/**
 * ファイルリストをエージェントに送信するクラス<BR>
 *
 */
public class LogfileManagerUtil{

	private static Log m_log = LogFactory.getLog(LogfileManagerUtil.class);

	/**
	 * topicを送信します。
	 * topicを受信したエージェントはログファイル監視の設定を再取得します。
	 * ファシリティIDがnullの場合は、全エージェントです。(Repositoryの変更時に呼ばれます。)
	 * 
	 * @param faciligyId
	 */
	public static void broadcastConfigured() {

		// MAIN
		m_log.info("broadcasting logfile monitor configuration modified.");

		TopicInfo topicInfo = new TopicInfo();
		topicInfo.setLogfileMonitorChanged(true);

		AgentConnectUtil.setTopic(null, topicInfo);
	}
	
	/**
	 * 流量制御を考慮しつつ全エージェントにtopicを送信します。
	 * topicを受信したエージェントはログファイル監視の設定を再取得します。 
	 * 
	 */
	public static void broadcastConfiguredFlowControl() {

		m_log.info("broadcasting logfile monitor configuration modified. with flow control.");

		TopicInfo topicInfo = new TopicInfo();
		topicInfo.setLogfileMonitorChanged(true);

		AgentConnectUtil.broadcastTopicFlowControl(topicInfo);
	}

	/**
	 * 特定エージェントに対して、ログファイル監視設定変更を通知する
	 * 
	 * @param facilityId 通知対象となるファシリティID
	 */
	public static void specificcastConfigured(String facilityId) {

		// MAIN
		m_log.info("specificcasting logfile monitor configuration modified.");

		if (facilityId == null || facilityId.isEmpty()) {
			return;
		}
		if(m_log.isDebugEnabled()){
			m_log.debug("specificcasting configuration modified. facilityId=" + facilityId);
		}
		TopicInfo topicInfo = new TopicInfo();
		topicInfo.setLogfileMonitorChanged(true);

		AgentConnectUtil.setTopic(facilityId, topicInfo);
	}

	/**
	 * 特定エージェントに対して、ログファイル監視設定変更を通知する
	 * 
	 * @param facilityIds 通知対象となるファシリティIDのHashSet
	 */
	public static void specificcastConfigured(HashSet<String> facilityIds) {

		// MAIN
		m_log.info("specificcasting logfile monitor configuration modified.");

		if (facilityIds == null || facilityIds.size() == 0) {
			return;
		}

		TopicInfo topicInfo = new TopicInfo();
		topicInfo.setLogfileMonitorChanged(true);

		for (String facilityId : facilityIds) {
			if (facilityId == null || facilityId.isEmpty()) {
				continue;
			}
			if(m_log.isDebugEnabled()){
				m_log.debug("specificcasting configuration modified. facilityId=" + facilityId);
			}
			AgentConnectUtil.setTopic(facilityId, topicInfo);
		}
	}
}