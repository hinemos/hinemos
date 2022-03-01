/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
	 * 引数で指定されたメッセージをキューへ送信します。<BR>
	 * 特定バージョン以降のエージェントにのみトピックを送りたい場合にバージョンを指定します。
	 * 
	 * @param info 実行指示情報
	 * @param supportedAgentversion 対象とするエージェントのバージョン
	 */
	public static void put(RunInstructionInfo info, String supportedAgentversion) {
		TopicInfo topicInfo = new TopicInfo();
		topicInfo.setRunInstructionInfo(info);
		topicInfo.setSupportedAgentVersion(supportedAgentversion);

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