/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.hinemosagent.bean.TopicInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;

/**
 * 対象構成情報の更新をエージェントに送信するクラス
 * 
 */
public class NodeConfigSettingManagerUtil {

	private static Log log = LogFactory.getLog(NodeConfigSettingManagerUtil.class);

	/**
	 * topicを送信します。
	 */
	public static void broadcastConfiguredFlowControl() {
		// Local Variables
		TopicInfo topicInfo = null;

		// MAIN
		log.info("broadcasting configuration modified. with flow control.");

		topicInfo = new TopicInfo();
		topicInfo.setNodeConfigSettingChanged(true);

		AgentConnectUtil.broadcastTopicFlowControl(topicInfo);
	}
}
