/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import java.util.List;

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
	public static void broadcastConfiguredFlowControl() {
		// Local Variables
		TopicInfo topicInfo = null;

		// MAIN
		log.info("broadcasting configuration modified. with flow control.");

		topicInfo = new TopicInfo();
		topicInfo.setRepositoryChanged(true);

		AgentConnectUtil.broadcastTopicFlowControl(topicInfo);
	}

	/**
	 * 特定エージェントに対して、リポジトリ設定変更を通知する
	 * 
	 * @param notifyFacilityIdList 通知対象となるファシリティIDのList
	 */
	public static void specificcastConfigured( List<String> notifyFacilityIdList ) {
		// Local Variables
		TopicInfo topicInfo = null;

		// MAIN
		log.info("notifying configuration modified.");

		topicInfo = new TopicInfo();
		topicInfo.setRepositoryChanged(true);
		
		for ( String target : notifyFacilityIdList ){
			if(log.isDebugEnabled()){
				log.debug("notifying configuration modified. target="+target);
			}
			AgentConnectUtil.setTopic(target, topicInfo);
		}
		
	}

}
