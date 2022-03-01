/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.http.model.Pattern;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertIdClassSet(infoClass = Pattern.class, idName = "id")
public class PatternResponse {
	private Integer patternOrderNo;
	private String pattern;
	private String description;
	private Boolean processType;
	private Boolean caseSensitivityFlg;
	private Boolean validFlg;

	public PatternResponse() {
	}

	public Integer getPatternOrderNo() {
		return patternOrderNo;
	}
	public void setPatternOrderNo(Integer patternOrderNo) {
		this.patternOrderNo = patternOrderNo;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Boolean getProcessType() {
		return processType;
	}
	public void setProcessType(Boolean processType) {
		this.processType = processType;
	}
	public Boolean getCaseSensitivityFlg() {
		return caseSensitivityFlg;
	}
	public void setCaseSensitivityFlg(Boolean caseSensitivityFlg) {
		this.caseSensitivityFlg = caseSensitivityFlg;
	}
	public Boolean getValidFlg() {
		return validFlg;
	}
	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}
	@Override
	public String toString() {
		return "Pattern [patternOrderNo=" + patternOrderNo
				+ ", pattern=" + pattern + ", description=" + description + ", processType=" + processType
				+ ", caseSensitivityFlg=" + caseSensitivityFlg + ", validFlg=" + validFlg + "]";
	}

}
