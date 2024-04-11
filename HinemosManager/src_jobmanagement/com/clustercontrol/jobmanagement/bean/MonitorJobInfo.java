/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.annotation.EnumerateConstant;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.deserializer.EnumToConstantDeserializer;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ProcessingMethodEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.serializer.ConstantToEnumSerializer;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.serializer.LanguageTranslateSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 監視ジョブに関する情報を保持するクラス
 *
 * @version 5.1.0
 */
/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Requestクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class MonitorJobInfo implements Serializable, RequestDto {
	/** シリアライズ可能クラスに定義するUID */
	@JsonIgnore
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private static Log m_log = LogFactory.getLog( MonitorJobInfo.class );

	/** ファシリティID */
	private String facilityID;

	/** スコープ */
	@JsonSerialize(using=LanguageTranslateSerializer.class)
	private String scope;

	/** スコープ処理 */
	@JsonDeserialize(using=EnumToConstantDeserializer.class)
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=ProcessingMethodEnum.class)
	private Integer processingMethod = 0;

	/** 監視ID */
	private String monitorId = "";

	/** 監視結果戻り値(情報) */
	private Integer monitorInfoEndValue = MonitorJobConstant.INITIAL_END_VALUE_INFO;

	/** 監視結果戻り値（警告） */
	private Integer monitorWarnEndValue = MonitorJobConstant.INITIAL_END_VALUE_WARN;

	/** 監視結果戻り値（危険） */
	private Integer monitorCriticalEndValue = MonitorJobConstant.INITIAL_END_VALUE_CRITICAL;

	/** 監視結果戻り値（不明） */
	private Integer monitorUnknownEndValue = MonitorJobConstant.INITIAL_END_VALUE_UNKNOWN;

	/** 監視結果が得られない場合の待ち時間（分） */
	private Integer monitorWaitTime = 1;

	/** 監視結果が得られない場合の終了値 */
	private Integer monitorWaitEndValue = MonitorJobConstant.INITIAL_END_VALUE_UNKNOWN;

	/**
	 * スコープを返す。<BR>
	 * @return スコープ
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * スコープを設定する。<BR>
	 * @param scope スコープ
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 * ファシリティIDを返す。<BR>
	 * @return ファシリティID
	 */
	public String getFacilityID() {
		return facilityID;
	}

	/**
	 * ファシリティIDを設定する。<BR>
	 * @param facilityID ファシリティID
	 */
	public void setFacilityID(String facilityID) {
		this.facilityID = facilityID;
	}

	/**
	 * スコープ処理を返す。<BR>
	 * @return スコープ処理
	 * @see com.clustercontrol.bean.ProcessingMethodConstant
	 */
	public Integer getProcessingMethod() {
		return processingMethod;
	}

	/**
	 * スコープ処理を設定する。<BR>
	 * @param processingMethod スコープ処理
	 * @see com.clustercontrol.bean.ProcessingMethodConstant
	 */
	public void setProcessingMethod(Integer processingMethod) {
		this.processingMethod = processingMethod;
	}

	/**
	 * 監視IDを返す。<BR>
	 * @return 監視ID
	 */
	public String getMonitorId() {
		return monitorId;
	}
	/**
	 * 監視IDを設定する。<BR>
	 * @param monitorId 監視ID
	 */
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	/**
	 * 監視結果終了値（情報）を返す。<BR>
	 * @return 監視結果終了値（情報）
	 */
	public Integer getMonitorInfoEndValue() {
		return monitorInfoEndValue;
	}
	/**
	 * 監視結果終了値（情報）を設定する。<BR>
	 * @param monitorInfoEndValue 監視結果終了値（情報）
	 */
	public void setMonitorInfoEndValue(Integer monitorInfoEndValue) {
		this.monitorInfoEndValue = monitorInfoEndValue;
	}

	/**
	 * 監視結果終了値（警告）を返す。<BR>
	 * @return 監視結果終了値（警告）
	 */
	public Integer getMonitorWarnEndValue() {
		return monitorWarnEndValue;
	}
	/**
	 * 監視結果終了値（警告）を設定する。<BR>
	 * @param monitorWarnEndValue 監視結果終了値（警告）
	 */
	public void setMonitorWarnEndValue(Integer monitorWarnEndValue) {
		this.monitorWarnEndValue = monitorWarnEndValue;
	}

	/**
	 * 監視結果終了値（危険）を返す。<BR>
	 * @return 監視結果終了値（危険）
	 */
	public Integer getMonitorCriticalEndValue() {
		return monitorCriticalEndValue;
	}
	/**
	 * 監視結果終了値（危険）を設定する。<BR>
	 * @param monitorCriticalEndValue 監視結果終了値（危険）
	 */
	public void setMonitorCriticalEndValue(Integer monitorCriticalEndValue) {
		this.monitorCriticalEndValue = monitorCriticalEndValue;
	}

	/**
	 * 監視結果終了値（不明）を返す。<BR>
	 * @return 監視結果終了値（不明）
	 */
	public Integer getMonitorUnknownEndValue() {
		return monitorUnknownEndValue;
	}
	/**
	 * 監視結果終了値（不明）を設定する。<BR>
	 * @param monitorUnknownEndValue 監視結果終了値（不明）
	 */
	public void setMonitorUnknownEndValue(Integer monitorUnknownEndValue) {
		this.monitorUnknownEndValue = monitorUnknownEndValue;
	}

	/**
	 * 監視結果が得られない場合の待ち時間を返す。<BR>
	 * @return 監視結果が得られない場合の待ち時間
	 */
	public Integer getMonitorWaitTime() {
		return monitorWaitTime;
	}
	/**
	 * 監視結果が得られない場合の待ち時間を設定する。<BR>
	 * @param monitorWaitTime 監視結果が得られない場合の待ち時間
	 */
	public void setMonitorWaitTime(Integer monitorWaitTime) {
		this.monitorWaitTime = monitorWaitTime;
	}

	/**
	 * 監視結果が得られない場合の終了値を返す。<BR>
	 * @return 監視結果が得られない場合の終了値
	 */
	public Integer getMonitorWaitEndValue() {
		return monitorWaitEndValue;
	}
	/**
	 * 監視結果が得られない場合の終了値を設定する。<BR>
	 * @param monitorWaitEndValue 監視結果が得られない場合の終了値
	 */
	public void setMonitorWaitEndValue(Integer monitorWaitEndValue) {
		this.monitorWaitEndValue = monitorWaitEndValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((facilityID == null) ? 0 : facilityID.hashCode());
		result = prime * result
				+ ((processingMethod == null) ? 0 : processingMethod.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		result = prime * result + ((monitorId == null) ? 0 : monitorId.hashCode());
		result = prime * result + ((monitorInfoEndValue == null) ? 0 : monitorInfoEndValue.hashCode());
		result = prime * result + ((monitorWarnEndValue == null) ? 0 : monitorWarnEndValue.hashCode());
		result = prime * result + ((monitorCriticalEndValue == null) ? 0 : monitorCriticalEndValue.hashCode());
		result = prime * result + ((monitorUnknownEndValue == null) ? 0 : monitorUnknownEndValue.hashCode());
		result = prime * result + ((monitorWaitTime == null) ? 0 : monitorWaitTime.hashCode());
		result = prime * result + ((monitorWaitEndValue == null) ? 0 : monitorWaitEndValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MonitorJobInfo)) {
			return false;
		}
		MonitorJobInfo o1 = this;
		MonitorJobInfo o2 = (MonitorJobInfo)o;

		boolean ret = false;
		ret = 	equalsSub(o1.getFacilityID(), o2.getFacilityID()) &&
				equalsSub(o1.getProcessingMethod(), o2.getProcessingMethod()) &&
				equalsSub(o1.getMonitorId(), o2.getMonitorId()) &&
				equalsSub(o1.getMonitorInfoEndValue(), o2.getMonitorInfoEndValue()) &&
				equalsSub(o1.getMonitorWarnEndValue(), o2.getMonitorWarnEndValue()) &&
				equalsSub(o1.getMonitorCriticalEndValue(), o2.getMonitorCriticalEndValue()) &&
				equalsSub(o1.getMonitorUnknownEndValue(), o2.getMonitorUnknownEndValue()) &&
				equalsSub(o1.getMonitorWaitTime(), o2.getMonitorWaitTime()) &&
				equalsSub(o1.getMonitorWaitEndValue(), o2.getMonitorWaitEndValue());
		return ret;
	}

	private boolean equalsSub(Object o1, Object o2) {
		if (o1 == o2)
			return true;
		
		if (o1 == null)
			return false;
		
		boolean ret = o1.equals(o2);
		if (!ret) {
			if (m_log.isTraceEnabled()) {
				m_log.trace("equalsSub : " + o1 + "!=" + o2);
			}
		}
		return ret;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}