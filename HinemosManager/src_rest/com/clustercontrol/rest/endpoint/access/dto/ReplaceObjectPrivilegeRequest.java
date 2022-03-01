/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class ReplaceObjectPrivilegeRequest implements RequestDto {

	public ReplaceObjectPrivilegeRequest() {
	}

	@RestItemName(value = MessageConstant.OBJECT_TYPE)
	@RestValidateString(notNull = true, maxLen = 64)
	private String objectType;

	@RestItemName(value = MessageConstant.OBJECT_ID)
	@RestValidateString(notNull = true, maxLen = 512)
	private String objectId;

	@RestItemName(value = MessageConstant.OBJECT_PRIVILEGE)
	private List<ObjectPrivilegeInfoRequestP1> objectPrigilegeInfoList;

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public List<ObjectPrivilegeInfoRequestP1> getObjectPrigilegeInfoList() {
		return objectPrigilegeInfoList;
	}

	public void setObjectPrigilegeInfoP1List(List<ObjectPrivilegeInfoRequestP1> objectPrigilegeInfoList) {
		this.objectPrigilegeInfoList = objectPrigilegeInfoList;
	}

	@Override
	public String toString() {
		return "ReplaceObjectPrivilegeRequest [objectType=" + objectType + ", objectId=" + objectId
				+ ", objectPrigilegeInfoList=" + objectPrigilegeInfoList + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
