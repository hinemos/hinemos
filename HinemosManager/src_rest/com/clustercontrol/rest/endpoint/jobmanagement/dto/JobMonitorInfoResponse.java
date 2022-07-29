/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.jobmanagement.bean.MonitorJobConstant;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ProcessingMethodEnum;

public class JobMonitorInfoResponse {

	/** ファシリティID */
	private String facilityID;

	/** スコープ */
	@RestPartiallyTransrateTarget
	private String scope;

	/** スコープ処理 */
	@RestBeanConvertEnum
	private ProcessingMethodEnum processingMethod = ProcessingMethodEnum.ALL_NODE;

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

	public JobMonitorInfoResponse() {
	}


	public String getFacilityID() {
		return facilityID;
	}

	public void setFacilityID(String facilityID) {
		this.facilityID = facilityID;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public ProcessingMethodEnum getProcessingMethod() {
		return processingMethod;
	}

	public void setProcessingMethod(ProcessingMethodEnum processingMethod) {
		this.processingMethod = processingMethod;
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public Integer getMonitorInfoEndValue() {
		return monitorInfoEndValue;
	}

	public void setMonitorInfoEndValue(Integer monitorInfoEndValue) {
		this.monitorInfoEndValue = monitorInfoEndValue;
	}

	public Integer getMonitorWarnEndValue() {
		return monitorWarnEndValue;
	}

	public void setMonitorWarnEndValue(Integer monitorWarnEndValue) {
		this.monitorWarnEndValue = monitorWarnEndValue;
	}

	public Integer getMonitorCriticalEndValue() {
		return monitorCriticalEndValue;
	}

	public void setMonitorCriticalEndValue(Integer monitorCriticalEndValue) {
		this.monitorCriticalEndValue = monitorCriticalEndValue;
	}

	public Integer getMonitorUnknownEndValue() {
		return monitorUnknownEndValue;
	}

	public void setMonitorUnknownEndValue(Integer monitorUnknownEndValue) {
		this.monitorUnknownEndValue = monitorUnknownEndValue;
	}

	public Integer getMonitorWaitTime() {
		return monitorWaitTime;
	}

	public void setMonitorWaitTime(Integer monitorWaitTime) {
		this.monitorWaitTime = monitorWaitTime;
	}

	public Integer getMonitorWaitEndValue() {
		return monitorWaitEndValue;
	}

	public void setMonitorWaitEndValue(Integer monitorWaitEndValue) {
		this.monitorWaitEndValue = monitorWaitEndValue;
	}

}
