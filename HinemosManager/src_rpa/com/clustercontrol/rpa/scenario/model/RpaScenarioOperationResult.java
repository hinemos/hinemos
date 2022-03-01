/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * シナリオ実績を格納するEntity定義
 * 
 */
@Entity
@Table(name="cc_rpa_scenario_operation_result", schema="log")
public class RpaScenarioOperationResult implements Serializable {
	private static final long serialVersionUID = 1L;

	/** ID(PK) */
	private Long resultId;
	/** シナリオID */
	private String scenarioId;
	/** ファシリティID */
	private String facilityId;
	/** 環境毎のRPAツールID */
	private String rpaToolEnvId;
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
	public static enum OperationResultStatus {
		/** 実行中 */
		NORMAL_RUNNING,
		/** 終了 */
		NORMAL_END,
		/** 実行中(エラーあり) */
		ERROR_RUNNING,
		/** 終了(エラーあり) */
		ERROR_END,
		/** 不明(終了ログなし) */
		UNKNOWN
	}
	/** ステップ数 */
	private Integer step;

	/** 開始時刻(日付のみ) */
	private Date startDateOnly;
	
	/** 開始時刻(時のみ) */
	private Integer startHour;
	
	/** シナリオ実績作成設定ID */
	private String scenarioOperationResultCreateSettingId;

	/** シナリオ実績明細 */
	private List<RpaScenarioOperationResultDetail> operationResultDetail = new ArrayList<>();

	public RpaScenarioOperationResult() {
	}

	public RpaScenarioOperationResult(String scenarioId, String facilityId, Long startDate, OperationResultStatus status, String scenarioOperationResultCreateSettingId, String rpaToolEnvId) {
		this.scenarioId = scenarioId;
		this.facilityId = facilityId;
		this.startDate = startDate;
		this.status = status;
		this.scenarioOperationResultCreateSettingId = scenarioOperationResultCreateSettingId;
		this.rpaToolEnvId = rpaToolEnvId;
		this.startDateOnly = new Date(startDate);
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDateOnly);
		this.startHour = cal.get(Calendar.HOUR_OF_DAY);
	}

	/** ID(PK) */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="result_id")
	public Long getResultId() {
		return resultId;
	}

	public void setResultId(Long resultId) {
		this.resultId = resultId;
	}

	/** シナリオID */
	@Column(name="scenario_id")
	public String getScenarioId() {
		return this.scenarioId;
	}
	public void setScenarioId(String scenarioId) {
		this.scenarioId = scenarioId;
	}

	/** ファシリティID */
	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	/** 環境毎のRPAツールID */
	@Column(name="rpa_tool_env_id")
	public String getRpaToolEnvId() {
		return this.rpaToolEnvId;
	}

	public void setRpaToolEnvId(String rpaToolEnvId) {
		this.rpaToolEnvId = rpaToolEnvId;
	}

	/** 開始時刻 */
	@Column(name="start_date")
	public Long getStartDate() {
		return this.startDate;
	}
	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}

	/** 終了時刻 */
	@Column(name="end_date")
	public Long getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}


	/** 手動操作時間 */
	@Column(name="manual_time")
	public Long getManualTime() {
		return this.manualTime;
	}

	public void setManualTime(Long manualTime) {
		this.manualTime = manualTime;
	}

	/** 手動操作コスト */
	@Column(name="coefficient_cost")
	public Integer getCoefficientCost() {
		return coefficientCost;
	}

	public void setCoefficientCost(Integer coefficientCost) {
		this.coefficientCost = coefficientCost;
	}

	/** 削減率 */
	@Column(name="reduction_rate")
	public Integer getReductionRate() {
		return reductionRate;
	}

	public void setReductionRate(Integer reductionRate) {
		this.reductionRate = reductionRate;
	}

	/** 削減時間 */
	@Column(name="reduction_time")
	public Long getReductionTime() {
		return reductionTime;
	}

	public void setReductionTime(Long reductionTime) {
		this.reductionTime = reductionTime;
	}

	/** 実行時間 */
	@Column(name="run_time")
	public Long getRunTime() {
		return this.runTime;
	}

	public void setRunTime(Long runTime) {
		this.runTime = runTime;
	}
	
	/** ステータス */
	@Enumerated(EnumType.STRING)
	@Column(name="status")
	public OperationResultStatus getStatus() {
		return this.status;
	}

	public void setStatus(OperationResultStatus status) {
		this.status = status;
	}

	/** ステップ数 */
	@Column(name="step")
	public Integer getStep() {
		return this.step;
	}

	public void setStep(Integer step) {
		this.step = step;
	}

	/** シナリオ実績作成設定ID */
	@Column(name="scenario_operation_result_create_setting_id")
	public String getScenarioOperationResultCreateSettingId() {
		return scenarioOperationResultCreateSettingId;
	}

	public void setScenarioOperationResultCreateSettingId(String scenarioOperationResultCreateSettingId) {
		this.scenarioOperationResultCreateSettingId = scenarioOperationResultCreateSettingId;
	}

	/** ステップ数を増加 */
	public void incrementStep() {
		if (this.step != null) {
			this.step += 1;
		} else {
			this.step = 1;
		}
	}

	/** 開始時刻(日付のみ) */
	@Temporal(TemporalType.DATE)
	@Column(name="start_date_only")
	public Date getStartDateOnly() {
		return startDateOnly;
	}

	public void setStartDateOnly(Date startDateOnly) {
		this.startDateOnly = startDateOnly;
	}

	/** 開始時刻(時のみ) */
	@Column(name="start_hour")
	public Integer getStartHour() {
		return startHour;
	}

	public void setStartHour(Integer startHour) {
		this.startHour = startHour;
	}

	/** 最新のログ時刻 */
	public long getLatestTime() {
		if (operationResultDetail.isEmpty()) {
			return 0;
		}
		
		return getLatestDetail().getLogTime();
	}

	/** 最新の実績明細 */
	public RpaScenarioOperationResultDetail getLatestDetail() {
		if (operationResultDetail.isEmpty()) {
			return null;
		}
		return operationResultDetail.get(operationResultDetail.size() - 1); 
	}

	/**
	 * シナリオ実績明細に引数のログ時刻のログが含まれているか判定する。
	 */
	public boolean detailsContainsLogTime(long logTime) {
		return operationResultDetail.stream().anyMatch(detail -> detail.getLogTime() == logTime);
	}
	
	/** 実績明細を追加 */
	public void addResultDetail(RpaScenarioOperationResultDetail detail){
		operationResultDetail.add(detail);
	}

	/** シナリオ実績明細 */
    @ElementCollection(fetch=FetchType.LAZY)
	@CollectionTable(
		name="cc_rpa_scenario_operation_result_detail", schema="log",
		joinColumns=@JoinColumn(name="result_id", referencedColumnName="result_id", updatable=false, insertable=false))
	@OrderBy("logTime ASC") // 取得時にログ時刻で昇順ソート
	public List<RpaScenarioOperationResultDetail> getOperationResultDetail() {
		return operationResultDetail;
	}

	public void setOperationResultDetail(List<RpaScenarioOperationResultDetail> operationResultDetail) {
		this.operationResultDetail = operationResultDetail;
	}
}
