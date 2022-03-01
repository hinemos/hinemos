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
import com.clustercontrol.rest.dto.RequestDto;

public class AddTemplateSetRequest implements RequestDto {
	
	public AddTemplateSetRequest (){}
	
	private String templateSetId;
	private String templateSetName;
	private String ownerRoleId;
	private String description;
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
	
	public String getTemplateSetId() {
		return templateSetId;
	}

	public void setTemplateSetId(String templateSetId) {
		this.templateSetId = templateSetId;
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

	@Override
	public String toString() {
		return "AddTemplateSetRequest [templateSetId;=" + templateSetId + ", templateSetName=" + templateSetName
				+ ", ownerRoleId=" + ownerRoleId + ", description=" + description
				+ ", templateSetDetailInfoList=" + templateSetDetailInfoList + "]";
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
