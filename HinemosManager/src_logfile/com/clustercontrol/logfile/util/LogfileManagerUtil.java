/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logfile.util;


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
}