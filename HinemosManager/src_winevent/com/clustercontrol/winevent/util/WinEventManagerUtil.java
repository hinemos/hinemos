/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.winevent.util;

import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.hinemosagent.bean.TopicInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;

/**
 * マネージャ内で利用されるWindowsイベント監視機能のユーティリティクラス<br/>
 * 
 * @since 4.1.0
 */
public class WinEventManagerUtil {

	private static Log log = LogFactory.getLog(WinEventManagerUtil.class);

	/**
	 * 接続されている全エージェントに対して、Windowsイベント監視設定変更を通知する<br/>
	 */
	public static void broadcastConfigured() {
		// Local Variables
		TopicInfo topicInfo = null;

		// MAIN
		log.info("broadcasting winevent monitor configuration modified.");

		topicInfo = new TopicInfo();
		topicInfo.setWinEventMonitorChanged(true);

		AgentConnectUtil.setTopic(null, topicInfo);
	}
	/**
	 * 流量制御を考慮しつつ全エージェントにtopicを送信します。
	 * topicを受信したエージェントはWindowsイベント監視の設定を再取得します。 
	 * 
	 */
	public static void broadcastConfiguredFlowControl() {

		log.info("broadcasting winevent monitor configuration modified. with flow control.");

		TopicInfo topicInfo = new TopicInfo();
		topicInfo.setWinEventMonitorChanged(true);

		AgentConnectUtil.broadcastTopicFlowControl(topicInfo);
	}

	/**
	 * 特定エージェントに対して、Windowsイベント監視設定変更を通知する
	 * 
	 * @param facilityId 通知対象となるファシリティID
	 */
	public static void specificcastConfigured(String facilityId) {

		// MAIN
		log.info("specificcasting winevent monitor configuration modified.");

		if (facilityId == null || facilityId.isEmpty()) {
			return;
		}
		if(log.isDebugEnabled()){
			log.debug("specificcasting configuration modified. facilityId=" + facilityId);
		}
		TopicInfo topicInfo = new TopicInfo();
		topicInfo.setWinEventMonitorChanged(true);

		AgentConnectUtil.setTopic(facilityId, topicInfo);
	}

	/**
	 * 特定エージェントに対して、Windowsイベント監視設定変更を通知する
	 * 
	 * @param facilityIds 通知対象となるファシリティIDのHashSet
	 */
	public static void specificcastConfigured(HashSet<String> facilityIds) {

		// MAIN
		log.info("specificcasting winevent monitor configuration modified.");

		if (facilityIds == null || facilityIds.size() == 0) {
			return;
		}

		TopicInfo topicInfo = new TopicInfo();
		topicInfo.setWinEventMonitorChanged(true);

		for (String facilityId : facilityIds) {
			if (facilityId == null || facilityId.isEmpty()) {
				continue;
			}
			if(log.isDebugEnabled()){
				log.debug("specificcasting configuration modified. facilityId=" + facilityId);
			}
			AgentConnectUtil.setTopic(facilityId, topicInfo);
		}
	}
}
