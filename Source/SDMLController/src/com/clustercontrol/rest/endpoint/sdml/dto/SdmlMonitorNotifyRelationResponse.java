/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.sdml.dto;

import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoResponse;
import com.clustercontrol.sdml.model.SdmlMonitorNotifyRelation;

@RestBeanConvertIdClassSet(infoClass = SdmlMonitorNotifyRelation.class, idName = "id")
public class SdmlMonitorNotifyRelationResponse {

	private String applicationId;
	private String sdmlMonitorTypeId;

	private List<NotifyRelationInfoResponse> notifyRelationList;

	public SdmlMonitorNotifyRelationResponse() {
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getSdmlMonitorTypeId() {
		return sdmlMonitorTypeId;
	}

	public void setSdmlMonitorTypeId(String sdmlMonitorTypeId) {
		this.sdmlMonitorTypeId = sdmlMonitorTypeId;
	}

	public List<NotifyRelationInfoResponse> getNotifyRelationList() {
		return notifyRelationList;
	}

	public void setNotifyRelationList(List<NotifyRelationInfoResponse> notifyRelationList) {
		this.notifyRelationList = notifyRelationList;
	}
}
