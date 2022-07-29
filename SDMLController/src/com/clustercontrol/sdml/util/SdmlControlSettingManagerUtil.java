/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.hinemosagent.bean.TopicInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;

/**
 * SDML制御設定の更新をエージェントに送信するクラス
 */
public class SdmlControlSettingManagerUtil {

	private static Log logger = LogFactory.getLog(SdmlControlSettingManagerUtil.class);

	/**
	 * topicを送信します。
	 */
	public static void broadcastConfiguredFlowControl() {

		logger.info("broadcasting sdml control setting modified. with flow control.");

		TopicInfo topicInfo = new TopicInfo();
		topicInfo.setSdmlControlSettingChanged(true);

		AgentConnectUtil.broadcastTopicFlowControl(topicInfo);
	}
}
