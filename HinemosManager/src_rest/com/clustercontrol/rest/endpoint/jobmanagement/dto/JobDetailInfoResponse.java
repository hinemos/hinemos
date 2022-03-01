/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.EndStatusEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.StatusEnum;

public class JobDetailInfoResponse {
	/** 実行状態(履歴で利用) */
	@RestBeanConvertEnum
	private StatusEnum status;

	/** 終了状態 */
	@RestBeanConvertEnum
	private EndStatusEnum endStatus;

	/** 終了値 */
	private Integer endValue;

	/** ファシリティID */
	private String facilityId;

	/** スコープ */
	@RestPartiallyTransrateTarget
	private String scope;

	/** 時刻 独自変換 HH:mm:ss*/
	private List<String> waitRuleTimeList;

	/** 開始日時 */
	@RestBeanConvertDatetime
	private String startDate;

	/** 終了日時 */
	@RestBeanConvertDatetime
	private String endDate;
	
	/** 実行回数 */
	private Integer runCount;
	
	/** スキップ */
	private Boolean skip;


	public JobDetailInfoResponse() {
	}


	public StatusEnum getStatus() {
		return status;
	}


	public void setStatus(StatusEnum status) {
		this.status = status;
	}


	public EndStatusEnum getEndStatus() {
		return endStatus;
	}


	public void setEndStatus(EndStatusEnum endStatus) {
		this.endStatus = endStatus;
	}


	public Integer getEndValue() {
		return endValue;
	}


	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}


	public String getFacilityId() {
		return facilityId;
	}


	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}


	public String getScope() {
		return scope;
	}


	public void setScope(String scope) {
		this.scope = scope;
	}


	public List<String> getWaitRuleTimeList() {
		return waitRuleTimeList;
	}


	public void setWaitRuleTimeList(List<String> waitRuleTimeList) {
		this.waitRuleTimeList = waitRuleTimeList;
	}

	public String getStartDate() {
		return startDate;
	}


	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}


	public String getEndDate() {
		return endDate;
	}


	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}


	public Integer getRunCount() {
		return runCount;
	}


	public void setRunCount(Integer runCount) {
		this.runCount = runCount;
	}

	public Boolean getSkip() {
		return skip;
	}

	public void setSkip(Boolean skip) {
		this.skip = skip;
	}



}
