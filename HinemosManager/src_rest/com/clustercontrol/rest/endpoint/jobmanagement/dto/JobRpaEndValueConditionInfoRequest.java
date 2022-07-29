/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.bean.RpaJobEndValueConditionInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaJobEndValueConditionTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaJobReturnCodeConditionEnum;
import com.clustercontrol.util.MessageConstant;

/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Infoクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
@RestBeanConvertAssertion(to = RpaJobEndValueConditionInfo.class)
public class JobRpaEndValueConditionInfoRequest implements RequestDto {

	/** 判定条件の優先順位 */
	private Integer orderNo;
	/** 判定条件タイプ */
	@RestBeanConvertEnum
	private RpaJobEndValueConditionTypeEnum conditionType;
	/** パターンマッチ文字列 */
	private String pattern;
	/** 大文字・小文字を区別しないフラグ */
	private Boolean caseSensitivityFlg;
	/** 条件に一致する場合フラグ */
	private Boolean processType;
	/** リターンコードの指定文字列 */
	private String returnCode;
	/** リターンコードの判定条件 */
	@RestBeanConvertEnum
	private RpaJobReturnCodeConditionEnum returnCodeCondition;
	/** コマンドのリターンコードをそのまま終了値にするフラグ */
	private Boolean useCommandReturnCodeFlg;
	/** 終了値 */
	private Integer endValue;
	/** 説明 */
	private String description;

	public JobRpaEndValueConditionInfoRequest() {
	}

	/**
	 * @return the orderNo
	 */
	public Integer getOrderNo() {
		return orderNo;
	}

	/**
	 * @param orderNo
	 *            the orderNo to set
	 */
	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	/**
	 * @return the conditionType
	 */
	public RpaJobEndValueConditionTypeEnum getConditionType() {
		return conditionType;
	}

	/**
	 * @param conditionType
	 *            the conditionType to set
	 */
	public void setConditionType(RpaJobEndValueConditionTypeEnum conditionType) {
		this.conditionType = conditionType;
	}

	/**
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * @param pattern
	 *            the pattern to set
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * @return the caseSensitivityFlg
	 */
	public Boolean getCaseSensitivityFlg() {
		return caseSensitivityFlg;
	}

	/**
	 * @param caseSensitivityFlg
	 *            the caseSensitivityFlg to set
	 */
	public void setCaseSensitivityFlg(Boolean caseSensitivityFlg) {
		this.caseSensitivityFlg = caseSensitivityFlg;
	}

	/**
	 * @return the processType
	 */
	public Boolean getProcessType() {
		return processType;
	}

	/**
	 * @param processType
	 *            the processType to set
	 */
	public void setProcessType(Boolean processType) {
		this.processType = processType;
	}

	/**
	 * @return the returnCode
	 */
	public String getReturnCode() {
		return returnCode;
	}

	/**
	 * @param returnCode
	 *            the returnCode to set
	 */
	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}

	/**
	 * @return the returnCodeCondition
	 */
	public RpaJobReturnCodeConditionEnum getReturnCodeCondition() {
		return returnCodeCondition;
	}

	/**
	 * @param returnCodeCondition
	 *            the returnCodeCondition to set
	 */
	public void setReturnCodeCondition(RpaJobReturnCodeConditionEnum returnCodeCondition) {
		this.returnCodeCondition = returnCodeCondition;
	}

	/**
	 * @return the useCommandReturnCodeFlg
	 */
	public Boolean getUseCommandReturnCodeFlg() {
		return useCommandReturnCodeFlg;
	}

	/**
	 * @param useCommandReturnCodeFlg
	 *            the useCommandReturnCodeFlg to set
	 */
	public void setUseCommandReturnCodeFlg(Boolean useCommandReturnCodeFlg) {
		this.useCommandReturnCodeFlg = useCommandReturnCodeFlg;
	}

	/**
	 * @return the endValue
	 */
	public Integer getEndValue() {
		return endValue;
	}

	/**
	 * @param endValue
	 *            the endValue to set
	 */
	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		// 終了値判定条件のリターンコードによる判定条件 の組み合わせ入力チェック
		if (conditionType.equals(RpaJobEndValueConditionTypeEnum.RETURN_CODE)) {
			if (returnCodeCondition == null) {
				String[] args = { MessageConstant.JUDGEMENT_CONDITION.getMessage() };
				String message = MessageConstant.MESSAGE_JOB_RPA_END_VALUE_CONDITION_TYPE_RETCD_INPUT.getMessage(args);
				throw new InvalidSetting(message);
			}
		}
	}
}
