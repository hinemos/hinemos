/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.binary.model.BinaryPatternInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityEnum;

@RestBeanConvertIdClassSet(infoClass = BinaryPatternInfo.class, idName = "id")
public class BinaryPatternInfoResponse {
	private Integer orderNo;
	private String description;
	private String grepString;
	private String encoding;
	private Boolean processType;
	@RestBeanConvertEnum
	private PriorityEnum priority;
	private String message;
	private Boolean validFlg;

	public BinaryPatternInfoResponse() {
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
	public String getGrepString() {
		return grepString;
	}
	public void setGrepString(String grepString) {
		this.grepString = grepString;
	}
	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	public Boolean getProcessType() {
		return processType;
	}
	public void setProcessType(Boolean processType) {
		this.processType = processType;
	}
	public PriorityEnum getPriority() {
		return priority;
	}
	public void setPriority(PriorityEnum priority) {
		this.priority = priority;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Boolean getValidFlg() {
		return validFlg;
	}
	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}
	@Override
	public String toString() {
		return "BinaryPatternInfo [orderNo=" + orderNo + ", description=" + description
				+ ", grepString=" + grepString + ", encoding=" + encoding + ", processType=" + processType
				+ ", priority=" + priority + ", message=" + message + ", validFlg=" + validFlg + "]";
	}

}
