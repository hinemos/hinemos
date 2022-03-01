/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.ArrayList;

public class GetHinemosTopicResponse {

	private ArrayList<TopicInfoResponse> topicInfoList;
	private SettingUpdateInfoResponse settingUpdateInfo;
	private Integer awakePort;
	// 送信元のエージェントがマネージャ認識されているか
	private boolean registered;

	public GetHinemosTopicResponse() {
	}

	public ArrayList<TopicInfoResponse> getTopicInfoList() {
		return topicInfoList;
	}

	public void setTopicInfoList(ArrayList<TopicInfoResponse> topicInfoList) {
		this.topicInfoList = topicInfoList;
	}

	public SettingUpdateInfoResponse getSettingUpdateInfo() {
		return settingUpdateInfo;
	}

	public void setSettingUpdateInfo(SettingUpdateInfoResponse settingUpdateInfo) {
		this.settingUpdateInfo = settingUpdateInfo;
	}

	public Integer getAwakePort() {
		return awakePort;
	}

	public void setAwakePort(Integer awakePort) {
		this.awakePort = awakePort;
	}

	public boolean isRegistered() {
		return registered;
	}

	public void setRegistered(boolean registered) {
		this.registered = registered;
	}
}
