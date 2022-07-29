/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.dto.RequestDto;

@RestBeanConvertIdClassSet(infoClass = NotifyRelationInfo.class, idName = "id")
public class NotifyRelationInfoRequest implements RequestDto {
	private String notifyId;

	public NotifyRelationInfoRequest() {
	}

	public String getNotifyId() {
		return notifyId;
	}

	public void setNotifyId(String notifyId) {
		this.notifyId = notifyId;
	}

	@Override
	public String toString() {
		return "NotifyRelationInfoRequest [notifyId=" + notifyId + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}