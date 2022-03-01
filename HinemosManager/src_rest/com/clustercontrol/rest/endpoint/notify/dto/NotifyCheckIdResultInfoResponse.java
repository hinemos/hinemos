/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import java.util.List;

public class NotifyCheckIdResultInfoResponse {
	private String notifyId;
	private List<String> notifyGroupIdList;

	public NotifyCheckIdResultInfoResponse() {
	}

	public String getNotifyId() {
		return notifyId;
	}

	public void setNotifyId(String notifyId) {
		this.notifyId = notifyId;
	}

	public List<String> getNotifyGroupIdList() {
		return notifyGroupIdList;
	}

	public void setNotifyGroupIdList(List<String> notifyGroupIdList) {
		this.notifyGroupIdList = notifyGroupIdList;
	}
}
