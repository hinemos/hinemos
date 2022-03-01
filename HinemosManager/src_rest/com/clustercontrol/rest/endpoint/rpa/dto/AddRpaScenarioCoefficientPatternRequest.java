/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import java.math.BigDecimal;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.annotation.validation.RestValidateDouble;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rpa.model.RpaScenarioCoefficientPattern;
import com.clustercontrol.util.MessageConstant;

@RestBeanConvertIdClassSet(infoClass=RpaScenarioCoefficientPattern.class,idName="id")
public class AddRpaScenarioCoefficientPatternRequest implements RequestDto {
	
	public AddRpaScenarioCoefficientPatternRequest(){
	}

	/** 環境毎のRPAツールID */
	@RestItemName(MessageConstant.RPA_TOOL_ENV_ID)
	@RestValidateString(notNull=true, type=CheckType.ID, minLen=1, maxLen=64)
	private String rpaToolEnvId;
	
	/** 順序 */
	@RestItemName(MessageConstant.ORDER_NO)
	@RestValidateInteger(notNull=true, minVal=1, maxVal=Short.MAX_VALUE)
	private Integer orderNo;
	
	/** 係数 */
	@RestItemName(MessageConstant.RPA_SCENARIO_COEFFICIENT)
	@RestValidateDouble(minVal=0, maxVal=9.99)
	private Double coefficient;
	
	/** パターン */
	@RestItemName(MessageConstant.RPA_SCENARIO_PATTERN)
	@RestValidateString(notNull=true)
	private String pattern;
	
	/** 大文字小文字を区別するフラグ */
	@RestItemName(MessageConstant.CASE_SENSITIVITY_FLG)
	@RestValidateObject(notNull=true)
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
		return "AddRpaScenarioCoefficientRequest [rpaToolEnvId=" + rpaToolEnvId
				+ ", orderNo=" + orderNo + ", coefficient=" + coefficient
				+ ", pattern=" + pattern + ", caseSensitivityFlg=" + caseSensitivityFlg + "]";
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
		BigDecimal bd = BigDecimal.valueOf(getCoefficient());
		if (bd.scale() > 2 ){
			throw new InvalidSetting(MessageConstant.MESSAGE_RPA_SCENARIO_COEFFICIENT_SCALE_OVER.getMessage());
		}
	}

}
