/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.bean.SettingUpdateInfo;
import com.clustercontrol.hinemosagent.bean.TopicInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.util.HinemosTime;

/**
 * マネージャ内で利用されるカレンダ機能のユーティリティクラス
 * 
 * @since 4.0
 */
public class CalendarManagerUtil {

	private static Log log = LogFactory.getLog(CalendarManagerUtil.class);

	/**
	 * 接続されている全エージェントに対して、カレンダ設定変更を通知する
	 */
	public static void broadcastConfigured() {
		// Local Variables
		TopicInfo topicInfo = null;

		// MAIN
		log.info("broadcasting configuration modified.");
		SettingUpdateInfo.getInstance().setCalendarUpdateTime(HinemosTime.currentTimeMillis());

		topicInfo = new TopicInfo();
		topicInfo.setCalendarChanged(true);

		AgentConnectUtil.setTopic(null, topicInfo);
	}

}
