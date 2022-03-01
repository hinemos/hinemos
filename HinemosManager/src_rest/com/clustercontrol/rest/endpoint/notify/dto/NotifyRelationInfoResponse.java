/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyTypeEnum;

@RestBeanConvertIdClassSet(infoClass = NotifyRelationInfo.class, idName = "id")
public class NotifyRelationInfoResponse {
	private String notifyId;
	@RestBeanConvertEnum
	private NotifyTypeEnum notifyType;

	public NotifyRelationInfoResponse() {
	}

	public String getNotifyId() {
		return notifyId;
	}

	public void setNotifyId(String notifyId) {
		this.notifyId = notifyId;
	}

	public NotifyTypeEnum getNotifyType() {
		return notifyType;
	}

	public void setNotifyType(NotifyTypeEnum notifyType) {
		this.notifyType = notifyType;
	}

	@Override
	public String toString() {
		return "NotifyRelationInfo [notifyId=" + notifyId + ", notifyType=" + notifyType + "]";
	}

}