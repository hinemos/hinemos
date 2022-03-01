/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.common.dto;

import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.endpoint.common.dto.enumtype.HinemosPropertyTypeEnum;
import com.clustercontrol.util.MessageConstant;

public class HinemosPropertyResponse {

	public HinemosPropertyResponse() {
	}

	private String key;
	@RestBeanConvertEnum
	private HinemosPropertyTypeEnum type;
	private String value;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String createDatetime;
	private String createUserId;
	@RestItemName(value = MessageConstant.DESCRIPTION)
	private String description;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String modifyDatetime;
	private String modifyUserId;
	@RestItemName(value = MessageConstant.OWNER_ROLE_ID)
	private String ownerRoleId;

	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public HinemosPropertyTypeEnum getType() {
		return type;
	}

	public void setType(HinemosPropertyTypeEnum type) {
		this.type = type;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getCreateDatetime() {
		return this.createDatetime;
	}

	public void setCreateDatetime(String createDatetime) {
		this.createDatetime = createDatetime;
	}

	public String getCreateUserId() {
		return this.createUserId;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getModifyDatetime() {
		return this.modifyDatetime;
	}

	public void setModifyDatetime(String modifyDatetime) {
		this.modifyDatetime = modifyDatetime;
	}

	public String getModifyUserId() {
		return this.modifyUserId;
	}

	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
	}

	public String getOwnerRoleId() {
		return this.ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	@Override
	public String toString() {
		return "HinemosPropertyResponseP1 [key=" + key + ", type=" + type + ", value=" + value + ", createDatetime="
				+ createDatetime + ", createUserId=" + createUserId + ", description=" + description
				+ ", modifyDatetime=" + modifyDatetime + ", modifyUserId=" + modifyUserId + ", modifyUserId="
				+ ownerRoleId + "]";
	}
}
