/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.reporting.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.dto.RequestDto;

public class ModifyTemplateSetRequest implements RequestDto {
	
	public ModifyTemplateSetRequest (){}
	
	private String templateSetName;
	private String ownerRoleId;
	private String description;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String regDate;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String updateDate;
	private String regUser;
	private String updateUser;
	private List<TemplateSetDetailInfoRequest> templateSetDetailInfoList;

	/**
	 * 
	 * @return
	 */
	public List<TemplateSetDetailInfoRequest> getTemplateSetDetailInfoList() {
		return templateSetDetailInfoList;
	}

	/**
	 * 
	 * @param templateSetDetailInfoList
	 */
	public void setTemplateSetDetailInfoList(List<TemplateSetDetailInfoRequest> templateSetDetailInfoList) {
		this.templateSetDetailInfoList = templateSetDetailInfoList;
	}

	public String getTemplateSetName() {
		return templateSetName;
	}

	public void setTemplateSetName(String templateSetName) {
		this.templateSetName = templateSetName;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getRegDate() {
		return regDate;
	}

	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	@Override
	public String toString() {
		return "ModifyTemplateSetRequest [templateSetName=" + templateSetName
				+ ", ownerRoleId=" + ownerRoleId + ", description=" + description + ", regDate = " + regDate
				+ ", updateDate=" + updateDate + ", regUser=" + regUser + ", updateUser=" + updateUser 
				+ ", templateSetDetailInfoList=" + templateSetDetailInfoList + "]";
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
