/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rpa.model.RpaScenarioCoefficientPattern;

@RestBeanConvertIdClassSet(infoClass=RpaScenarioCoefficientPattern.class,idName="id")
public class RpaScenarioCoefficientPatternResponse {
	
	public RpaScenarioCoefficientPatternResponse() {
	}

	/** 環境毎のRPAツールID */
	private String rpaToolEnvId;
	
	/** 順序 */
	private Integer orderNo;
	
	/** 係数 */
	private Double coefficient;
	
	/** パターン */
	private String pattern;
	
	/** 大文字小文字を区別するフラグ */
	private Boolean caseSensitivityFlg;

	/** 環境毎のRPAツールID */
	public String getRpaToolEnvId() {
		return rpaToolEnvId;
	}
	public void setRpaToolEnvId(String rpaToolEnvId) {
		this.rpaToolEnvId = rpaToolEnvId;
	}

	/** 順序 */
	public Integer getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	/** 係数 */
	public Double getCoefficient() {
		return coefficient;
	}
	public void setCoefficient(Double coefficient) {
		this.coefficient = coefficient;
	}

	/** パターン */
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/** 大文字小文字を区別するフラグ */
	public Boolean getCaseSensitivityFlg() {
		return caseSensitivityFlg;
	}
	public void setCaseSensitivityFlg(Boolean caseSensitivityFlg) {
		this.caseSensitivityFlg = caseSensitivityFlg;
	}

	@Override
	public String toString() {
		return "RpaScenarioCoefficientResponse [rpaToolEnvId=" + rpaToolEnvId
				+ ", orderNo=" + orderNo + ", coefficient=" + coefficient
				+ ", pattern=" + pattern + ", caseSensitivityFlg=" + caseSensitivityFlg + "]";
	}
}
