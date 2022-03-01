/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResult.OperationResultStatus;

public class RpaScenarioOperationResultWithDetailResponse {
	
	public RpaScenarioOperationResultWithDetailResponse() {
	}

	/** ID(PK) */
	private Long resultId;
	/** シナリオID */
	private String scenarioId;
	/** ファシリティID */
	private String facilityId;
	/** 開始時刻 */
	private Long startDate;
	/** 終了時刻 */
	private Long endDate;
	/** 手動操作時間 */
	private Long manualTime;
	/** 手動操作コスト */
	private Integer coefficientCost;
	/** 削減率*/
	private Integer reductionRate;
	/** 削減時間*/
	private Long reductionTime;
	/** 実行時間*/
	private Long runTime;
	/** ステータス */
	private OperationResultStatus status;
	/** ステップ数 */
	private Integer step;
	/** シナリオ名 */
	private String scenarioName;
	/** ファシリティ名 */
	private String facilityName;
	/** RPAツール名 */
	private String rpaToolName;
	/** シナリオ実績明細 */
	private List<RpaScenarioOperationResultDetailResponse> operationResultDetail = new ArrayList<>();


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
	
	/** ファシリティID */
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
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

	/** 手動操作時間 */
	public Long getManualTime() {
		return this.manualTime;
	}
	public void setManualTime(Long manualTime) {
		this.manualTime = manualTime;
	}

	/** 手動操作コスト */
	public Integer getCoefficientCost() {
		return coefficientCost;
	}
	public void setCoefficientCost(Integer coefficientCost) {
		this.coefficientCost = coefficientCost;
	}

	/** 削減率 */
	public Integer getReductionRate() {
		return reductionRate;
	}
	public void setReductionRate(Integer reductionRate) {
		this.reductionRate = reductionRate;
	}

	/** 削減時間 */
	public Long getReductionTime() {
		return reductionTime;
	}
	public void setReductionTime(Long reductionTime) {
		this.reductionTime = reductionTime;
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
	
	/** シナリオ名 */
	public String getScenarioName() {
		return scenarioName;
	}
	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}
	
	/** ファシリティ名 */
	public String getFacilityName() {
		return facilityName;
	}
	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}
	
	/** RPAツール名 */
	public String getRpaToolName() {
		return this.rpaToolName;
	}
	public void setRpaToolName(String rpaToolName) {
		this.rpaToolName = rpaToolName;
	}

	/** シナリオ実績明細 */
	public List<RpaScenarioOperationResultDetailResponse> getOperationResultDetail() {
		return operationResultDetail;
	}
	public void setOperationResultDetail(List<RpaScenarioOperationResultDetailResponse> operationResultDetail) {
		this.operationResultDetail = operationResultDetail;
	}

	@Override
	public String toString() {
		return "RpaScenarioOperationResultWithDetailResponse [resultId=" + resultId + ", scenarioId=" + scenarioId 
				+ ", facilityId=" + facilityId + ", startDate=" + startDate + ", endDate=" + endDate + ", manualTime=" + manualTime
				+ ", runTime = " + runTime + ", coefficientCost=" + coefficientCost + ", reductionRate=" + reductionRate 
				+ ", reductionTime=" + reductionTime + ", status=" + status + ", step=" + step
				+ ", scenarioName=" + scenarioName + ", facilityName=" + facilityName + ", rpaToolName=" + rpaToolName 
				+ ", operationResultDetail=" + operationResultDetail
				+ "]";
	}
}
