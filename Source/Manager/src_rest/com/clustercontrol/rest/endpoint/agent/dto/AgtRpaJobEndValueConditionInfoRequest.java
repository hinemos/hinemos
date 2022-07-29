/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.jobmanagement.bean.RpaJobEndValueConditionInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(to = RpaJobEndValueConditionInfo.class)
public class AgtRpaJobEndValueConditionInfoRequest {

	// ---- from RpaJobEndValueConditionInfo
	private Integer orderNo;
	private Integer conditionType;
	private String pattern;
	private Boolean caseSensitivityFlg;
	private Boolean processType;
	private String returnCode;
	private Integer returnCodeCondition;
	private Boolean useCommandReturnCodeFlg;
	private Integer endValue;
	private String description;

	public AgtRpaJobEndValueConditionInfoRequest() {
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public Integer getConditionType() {
		return conditionType;
	}

	public void setConditionType(Integer conditionType) {
		this.conditionType = conditionType;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public Boolean getCaseSensitivityFlg() {
		return caseSensitivityFlg;
	}

	public void setCaseSensitivityFlg(Boolean caseSensitivityFlg) {
		this.caseSensitivityFlg = caseSensitivityFlg;
	}

	public Boolean getProcessType() {
		return processType;
	}

	public void setProcessType(Boolean processType) {
		this.processType = processType;
	}

	public String getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}

	public Integer getReturnCodeCondition() {
		return returnCodeCondition;
	}

	public void setReturnCodeCondition(Integer returnCodeCondition) {
		this.returnCodeCondition = returnCodeCondition;
	}

	public Boolean getUseCommandReturnCodeFlg() {
		return useCommandReturnCodeFlg;
	}

	public void setUseCommandReturnCodeFlg(Boolean useCommandReturnCodeFlg) {
		this.useCommandReturnCodeFlg = useCommandReturnCodeFlg;
	}

	public Integer getEndValue() {
		return endValue;
	}

	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
