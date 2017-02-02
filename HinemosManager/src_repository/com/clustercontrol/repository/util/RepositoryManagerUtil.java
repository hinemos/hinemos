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
