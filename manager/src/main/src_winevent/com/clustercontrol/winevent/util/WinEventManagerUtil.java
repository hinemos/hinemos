/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.winevent.util;

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

}
