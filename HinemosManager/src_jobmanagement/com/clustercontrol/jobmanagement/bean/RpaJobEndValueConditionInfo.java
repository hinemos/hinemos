/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobEndValueConditionTypeConstant;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobReturnCodeConditionConstant;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.annotation.EnumerateConstant;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.deserializer.EnumToConstantDeserializer;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaJobEndValueConditionTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaJobReturnCodeConditionEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.serializer.ConstantToEnumSerializer;
import com.clustercontrol.util.MessageConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * RPAシナリオジョブ（直接実行）の終了値判定条件に関する情報を保持するクラス
 */
/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Requestクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
public class RpaJobEndValueConditionInfo implements Serializable, RequestDto, Comparable<RpaJobEndValueConditionInfo> {
	/** シリアライズ可能クラスに定義するUID */
	@JsonIgnore
	private static final long serialVersionUID = 1L;
	/** 判定条件の優先順位 */
	private Integer orderNo;
	/** 判定条件タイプ */
	@JsonDeserialize(using=EnumToConstantDeserializer.class)
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=RpaJobEndValueConditionTypeEnum.class)
	private Integer conditionType;
	/** パターンマッチ文字列 */
	private String pattern;
	/** 大文字・小文字を区別しないフラグ */
	private Boolean caseSensitivityFlg;
	/** 条件に一致する場合フラグ */
	private Boolean processType;
	/** リターンコードの指定文字列 */
	private String returnCode;
	/** リターンコードの判定条件 */
	@JsonDeserialize(using=EnumToConstantDeserializer.class)
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=RpaJobReturnCodeConditionEnum.class)
	private Integer returnCodeCondition;
	/** コマンドのリターンコードをそのまま終了値にするフラグ */
	private Boolean useCommandReturnCodeFlg;
	/** 終了値 */
	private Integer endValue;
	/** 説明 */
	private String description;

	/**
	 * @return 判定条件の優先順位を返します。
	 */
	public Integer getOrderNo() {
		return orderNo;
	}

	/**
	 * @param orderNo
	 *            判定条件の優先順位を設定します。
	 */
	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	/**
	 * @return 判定条件タイプを返します。
	 */
	public Integer getConditionType() {
		return conditionType;
	}

	/**
	 * @param conditionType
	 *            判定条件タイプを設定します。
	 */
	public void setConditionType(Integer conditionType) {
		this.conditionType = conditionType;
	}

	/**
	 * @return パターンマッチ文字列を返します。
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * @param pattern
	 *            パターンマッチ文字列を設定します。
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * @return 大文字・小文字を区別しないフラグを返します。
	 */
	public Boolean getCaseSensitivityFlg() {
		return caseSensitivityFlg;
	}

	/**
	 * @param caseSensitivityFlg
	 *            大文字・小文字を区別しないフラグを設定します。
	 */
	public void setCaseSensitivityFlg(Boolean caseSensitivityFlg) {
		this.caseSensitivityFlg = caseSensitivityFlg;
	}

	/**
	 * @return 条件に一致する場合フラグを返します。
	 */
	public Boolean getProcessType() {
		return processType;
	}

	/**
	 * @param processType
	 *            条件に一致する場合フラグを設定します。
	 */
	public void setProcessType(Boolean processType) {
		this.processType = processType;
	}

	/**
	 * @return リターンコードの指定文字列を返します。
	 */
	public String getReturnCode() {
		return returnCode;
	}

	/**
	 * @param returnCode
	 *            リターンコードの指定文字列を設定します。
	 */
	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}

	/**
	 * @return リターンコードの判定条件を返します。
	 */
	public Integer getReturnCodeCondition() {
		return returnCodeCondition;
	}

	/**
	 * @param returnCodeCondition
	 *            リターンコードの判定条件を設定します。
	 */
	public void setReturnCodeCondition(Integer returnCodeCondition) {
		this.returnCodeCondition = returnCodeCondition;
	}

	/**
	 * @return コマンドのリターンコードをそのまま終了値にするフラグを返します。
	 */
	public Boolean getUseCommandReturnCodeFlg() {
		return useCommandReturnCodeFlg;
	}

	/**
	 * @param useCommandReturnCodeFlg
	 *            コマンドのリターンコードをそのまま終了値にするフラグを設定します。
	 */
	public void setUseCommandReturnCodeFlg(Boolean useCommandReturnCodeFlg) {
		this.useCommandReturnCodeFlg = useCommandReturnCodeFlg;
	}

	/**
	 * @return 終了値を返します。
	 */
	public Integer getEndValue() {
		return endValue;
	}

	/**
	 * @param endValue
	 *            終了値を設定します。
	 */
	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}

	/**
	 * @return 説明を返します。
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            説明を設定します。
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((caseSensitivityFlg == null) ? 0 : caseSensitivityFlg.hashCode());
		result = prime * result + ((returnCodeCondition == null) ? 0 : returnCodeCondition.hashCode());
		result = prime * result + ((conditionType == null) ? 0 : conditionType.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((endValue == null) ? 0 : endValue.hashCode());
		result = prime * result + ((orderNo == null) ? 0 : orderNo.hashCode());
		result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
		result = prime * result + ((processType == null) ? 0 : processType.hashCode());
		result = prime * result + ((returnCode == null) ? 0 : returnCode.hashCode());
		result = prime * result + ((useCommandReturnCodeFlg == null) ? 0 : useCommandReturnCodeFlg.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RpaJobEndValueConditionInfo other = (RpaJobEndValueConditionInfo) obj;
		if (caseSensitivityFlg == null) {
			if (other.caseSensitivityFlg != null)
				return false;
		} else if (!caseSensitivityFlg.equals(other.caseSensitivityFlg))
			return false;
		if (returnCodeCondition == null) {
			if (other.returnCodeCondition != null)
				return false;
		} else if (!returnCodeCondition.equals(other.returnCodeCondition))
			return false;
		if (conditionType == null) {
			if (other.conditionType != null)
				return false;
		} else if (!conditionType.equals(other.conditionType))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (endValue == null) {
			if (other.endValue != null)
				return false;
		} else if (!endValue.equals(other.endValue))
			return false;
		if (orderNo == null) {
			if (other.orderNo != null)
				return false;
		} else if (!orderNo.equals(other.orderNo))
			return false;
		if (pattern == null) {
			if (other.pattern != null)
				return false;
		} else if (!pattern.equals(other.pattern))
			return false;
		if (processType == null) {
			if (other.processType != null)
				return false;
		} else if (!processType.equals(other.processType))
			return false;
		if (returnCode == null) {
			if (other.returnCode != null)
				return false;
		} else if (!returnCode.equals(other.returnCode))
			return false;
		if (useCommandReturnCodeFlg == null) {
			if (other.useCommandReturnCodeFlg != null)
				return false;
		} else if (!useCommandReturnCodeFlg.equals(other.useCommandReturnCodeFlg))
			return false;
		return true;
	}

	public static void main(String args[]) {
		RpaJobEndValueConditionInfo a = new RpaJobEndValueConditionInfo();
		a.setOrderNo(1);
		a.setConditionType(RpaJobEndValueConditionTypeConstant.LOG);
		a.setPattern(".*【エラー】.*");
		a.setCaseSensitivityFlg(false);
		a.setProcessType(true);
		a.setReturnCode("0");
		a.setReturnCodeCondition(RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC);
		a.setUseCommandReturnCodeFlg(false);
		a.setEndValue(0);
		a.setDescription("");

		RpaJobEndValueConditionInfo b = new RpaJobEndValueConditionInfo();
		b.setOrderNo(1);
		b.setConditionType(RpaJobEndValueConditionTypeConstant.LOG);
		b.setPattern(".*【エラー】.*");
		b.setCaseSensitivityFlg(false);
		b.setProcessType(true);
		b.setReturnCode("0");
		b.setReturnCodeCondition(RpaJobReturnCodeConditionConstant.EQUAL_NUMERIC);
		b.setUseCommandReturnCodeFlg(false);
		b.setEndValue(0);
		b.setDescription("");

		System.out.println("a.equals(b)=" + a.equals(b));
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		// 終了値判定条件のリターンコードによる判定条件 の組み合わせ入力チェック
		if (conditionType.equals(RpaJobEndValueConditionTypeEnum.RETURN_CODE.getCode())) {
			if (returnCodeCondition == null) {
				String[] args = { MessageConstant.JUDGEMENT_CONDITION.getMessage() };
				String message = MessageConstant.MESSAGE_JOB_RPA_END_VALUE_CONDITION_TYPE_RETCD_INPUT.getMessage(args);
				throw new InvalidSetting(message);
			}
		}
	}

	@Override
	public int compareTo(RpaJobEndValueConditionInfo o) {
		return this.getOrderNo() - o.getOrderNo();
	}
}
