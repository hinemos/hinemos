/*

 Copyright (C) 2011 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

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