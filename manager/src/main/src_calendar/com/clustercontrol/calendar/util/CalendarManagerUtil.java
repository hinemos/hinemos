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
