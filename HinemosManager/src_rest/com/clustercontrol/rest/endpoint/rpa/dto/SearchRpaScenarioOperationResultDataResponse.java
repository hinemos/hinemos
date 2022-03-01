/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResult.OperationResultStatus;

public class SearchRpaScenarioOperationResultDataResponse {
	
	public SearchRpaScenarioOperationResultDataResponse() {
	}

	/** ID(PK) */
	private Long resultId;
	/** シナリオID */
	private String scenarioId;
	/** シナリオ名 */
	private String scenarioName;
	/** ファシリティID */
	private String facilityId;
	/** ファシリティ名 */
	private String facilityName;
	/** 開始時刻 */
	private Long startDate;
	/** 終了時刻 */
	private Long endDate;
	/** 実行時間 */
	private Long runTime;
	/** ステータス */
	private OperationResultStatus status;
	/** ステップ数 */
	private Integer step;


	/** ID(PK) */
	public Long getResultId() {
		return resultId;
	}
	public void setResultId(Long resultId) {
		this.resultId = resultId;
	}
	
	/** シナリオID */
	public String getScenarioId() {
		return scenarioId;
	}
	public void setScenarioId(String scenarioId) {
		this.scenarioId = scenarioId;
	}
	
	/** シナリオ名 */
	public String getScenarioName() {
		return scenarioName;
	}
	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}
	
	/** シナリオID */
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	
	/** シナリオ名 */
	public String getFacilityName() {
		return facilityName;
	}
	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}
	
	/** 開始時刻 */
	public Long getStartDate() {
		return startDate;
	}
	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}
	
	/** 終了時刻 */
	public Long getEndDate() {
		return this.endDate;
	}
	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}

	/** 実行時間 */
	public Long getRunTime() {
		return this.runTime;
	}
	public void setRunTime(Long runTime) {
		this.runTime = runTime;
	}

	/** ステータス */
	public OperationResultStatus getStatus() {
		return this.status;
	}
	public void setStatus(OperationResultStatus status) {
		this.status = status;
	}

	/** ステップ数 */
	public Integer getStep() {
		return this.step;
	}
	public void setStep(Integer step) {
		this.step = step;
	}

	@Override
	public String toString() {
		return "SearchRpaScenarioOperationResultDataResponse [resultId=" + resultId 
				+ ", scenarioId=" + scenarioId + ", scenarioName=" + scenarioName 
				+ ", facilityid=" + facilityId  + ", facilityName=" + facilityName
				+ ", startDate=" + startDate + ", endDate=" + endDate + ", runTime = " + runTime 
				+ ", status=" + status  + ", step=" + step
				+ "]";
	}

}
