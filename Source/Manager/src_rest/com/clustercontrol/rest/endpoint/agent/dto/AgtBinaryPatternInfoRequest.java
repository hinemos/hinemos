/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.binary.model.BinaryPatternInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertAssertion(to = BinaryPatternInfo.class)
@RestBeanConvertIdClassSet(infoClass = BinaryPatternInfo.class, idName = "id")
public class AgtBinaryPatternInfoRequest extends AgentRequestDto {

	// ---- from MonitorStringValueInfoPK
	private String monitorId;
	// private Integer orderNo;

	// ---- from BinaryPatternInfo
	// private MonitorStringValueInfoPK id; // 上記のとおり展開  
	private String description;
	private String grepString;
	private String encoding;
	private Boolean processType;
	private Integer priority;
	private String message;
	private Boolean validFlg;
	// private MonitorInfo monitorInfo; // 循環参照させない

	public AgtBinaryPatternInfoRequest() {
	}

	// ---- accessors

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
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

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
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

}
