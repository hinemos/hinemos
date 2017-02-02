/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
