/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hinemosagent.bean;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.commons.bean.SettingUpdateInfo;

/**
 * マネージャ - エージェント間でやり取りされるTopicをまとめたクラス
 * 
 * @see com.clustercontrol.commons.bean.SettingUpdateInfo
 * @see com.clustercontrol.hinemosagent.bean.TopicInfo
 * @since 4.0
 */
@XmlType(namespace = "http://agent.ws.clustercontrol.com")
public class HinemosTopicInfo {

	private ArrayList<TopicInfo> topicInfoList = null;
	private SettingUpdateInfo settingUpdateInfo = null;
	private int awakePort = 24005;

	public HinemosTopicInfo() {

	}

	public HinemosTopicInfo(ArrayList<TopicInfo> topicInfoList, SettingUpdateInfo suInfo) {
		this.topicInfoList = topicInfoList;
		this.settingUpdateInfo = suInfo;
	}

	public ArrayList<TopicInfo> getTopicInfoList() {
		return topicInfoList;
	}

	public void setTopicInfoList(ArrayList<TopicInfo> topicInfoList) {
		this.topicInfoList = topicInfoList;
	}

	public SettingUpdateInfo getSettingUpdateInfo() {
		return settingUpdateInfo;
	}


	public void setSettingUpdateInfo(SettingUpdateInfo settingUpdateInfo) {
		this.settingUpdateInfo = settingUpdateInfo;
	}

	public int getAwakePort() {
		return awakePort;
	}

	public void setAwakePort(int awakePort) {
		this.awakePort = awakePort;
	}
}
