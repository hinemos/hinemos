/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.model;

import java.io.Serializable;

import com.clustercontrol.rpa.scenario.factory.RpaLogParseResult;
import com.clustercontrol.rpa.util.LogTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;


/**
 * シナリオ実績明細を格納するEntity定義
 * 
 */
@Embeddable
public class RpaScenarioOperationResultDetail implements Serializable {
	private static final long serialVersionUID = 1L;
	/** ログ時刻 */
	private Long logTime;
	/** メッセージ */
	private String log;
	/** ログ種別(開始、終了、エラー) */
	private LogTypeEnum logType;

	/** 手動操作時間 */
	private Long manualTime;
	/** 実行時間 */
	private Long runTime;
	/** 手動操作コスト */
	private Integer coefficientCost;
	/** 削減率*/
	private Integer reductionRate;
	/** 削減時間*/
	private Long reductionTime;
	/** 重要度 */
	private RpaLogParseResult.Priority priority;
	
	public RpaScenarioOperationResultDetail() {
	}

	/** ログ時刻 */
	@Column(name="log_time")
	public Long getLogTime() {
		return logTime;
	}

	public void setLogTime(Long logTime) {
		this.logTime = logTime;
	}

	/** メッセージ */
	@Column(name="log")
	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	/** ログ種別(開始、終了、エラー) */
	@Enumerated(EnumType.STRING)
	@Column(name="log_type")
	public LogTypeEnum getLogType() {
		return logType;
	}

	public void setLogType(LogTypeEnum logType) {
		this.logType = logType;
	}


	/** 手動実行時間 */
	@Column(name="manual_time")
	public Long getManualTime() {
		return this.manualTime;
	}

	public void setManualTime(Long manualTime) {
		this.manualTime = manualTime;
	}


	/** 実行時間 */
	@Column(name="run_time")
	public Long getRunTime() {
		return this.runTime;
	}

	public void setRunTime(Long runTime) {
		this.runTime = runTime;
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

	/** 重要度 */
	@Enumerated(EnumType.ORDINAL)
	@Column(name="priority")
	public RpaLogParseResult.Priority getPriority() {
		return priority;
	}
	
	public void setPriority(RpaLogParseResult.Priority priority) {
		this.priority = priority;
	}
}