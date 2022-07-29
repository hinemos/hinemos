/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.reporting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class TemplateSetDetailInfoRequest implements RequestDto {
	
	public TemplateSetDetailInfoRequest (){}
	
	private String templateSetId;
	@RestItemName(value = MessageConstant.ORDER_NO)
	@RestValidateInteger(notNull = true, minVal=1)
	private Integer orderNo;
	private String description;
	private String templateId;
	private String titleName;

	public String getTemplateSetId() {
		return templateSetId;
	}

	public void setTemplateSetId(String templateSetId) {
		this.templateSetId = templateSetId;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getTitleName() {
		return titleName;
	}

	public void setTitleName(String titleName) {
		this.titleName = titleName;
	}

	@Override
	public String toString() {
		return "AddTemplateSetRequest [templateSetId;=" + templateSetId + ", orderNo=" + orderNo
				+ ", description=" + description + ", templateId = " + templateId + ", titleName=" + titleName + "]";
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
