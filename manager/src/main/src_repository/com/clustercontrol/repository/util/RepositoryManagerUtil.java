/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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
 * マネージャ内で利用されるリポジトリ管理機能のユーティリティクラス
 * 
 * @since 4.0
 */
public class RepositoryManagerUtil {

	private static Log log = LogFactory.getLog(RepositoryManagerUtil.class);

	/**
	 * 接続されている全エージェントに対して、リポジトリ設定変更を通知する
	 */
	public static void broadcastConfigured() {
		// Local Variables
		TopicInfo topicInfo = null;

		// MAIN
		log.info("broadcasting configuration modified.");

		topicInfo = new TopicInfo();
		topicInfo.setRepositoryChanged(true);

		AgentConnectUtil.setTopic(null, topicInfo);
	}

}
