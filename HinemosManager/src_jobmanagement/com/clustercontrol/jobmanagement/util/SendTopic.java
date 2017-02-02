/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.util;

import com.clustercontrol.commons.bean.SettingUpdateInfo;
import com.clustercontrol.hinemosagent.bean.TopicInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.util.HinemosTime;

/**
 * トピックへメッセージを送信するクラス<BR>
 *
 * @version 2.1.0
 * @since 1.0.0
 */
public class SendTopic {


	/**
	 * 引数で指定されたメッセージをキューへ送信します。
	 *
	 * @param info 実行指示情報
	 * @throws JMSException
	 */
	public static void put(RunInstructionInfo info) {
		TopicInfo topicInfo = new TopicInfo();
		topicInfo.setRunInstructionInfo(info);

		AgentConnectUtil.setTopic(info.getFacilityId(), topicInfo);
	}

	/**
	 * ファイルチェックの再取得を促すためのトピック。
	 * @param facilityId
	 */
	public static void putFileCheck(String facilityId) {
		SettingUpdateInfo.getInstance().setJobFileCheckUpdateTime(HinemosTime.currentTimeMillis());

		TopicInfo topicInfo = new TopicInfo();
		topicInfo.setFileCheckChanged(true);

		AgentConnectUtil.setTopic(facilityId, topicInfo);
	}
}