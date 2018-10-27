/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.custom.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.hinemosagent.bean.TopicInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;

/**
 * マネージャ内で利用されるコマンド監視機能のユーティリティクラス<br/>
 * 
 * @since 4.0
 */
public class CustomManagerUtil {

	private static Log log = LogFactory.getLog(CustomManagerUtil.class);

	/**
	 * 接続されている全エージェントに対して、コマンド監視設定変更を通知する<br/>
	 */
	public static void broadcastConfigured() {
		// Local Variables
		TopicInfo topicInfo = null;

		// MAIN
		log.info("broadcasting custom monitor configuration modified.");

		topicInfo = new TopicInfo();
		topicInfo.setCustomMonitorChanged(true);

		AgentConnectUtil.setTopic(null, topicInfo);
	}

}
