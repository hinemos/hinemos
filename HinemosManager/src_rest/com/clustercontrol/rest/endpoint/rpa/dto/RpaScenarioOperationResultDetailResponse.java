/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import com.clustercontrol.rpa.scenario.factory.RpaLogParseResult;
import com.clustercontrol.rpa.util.LogTypeEnum;

public class RpaScenarioOperationResultDetailResponse {
	
	public RpaScenarioOperationResultDetailResponse() {
	}

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

	/** ログ時刻 */
	public Long getLogTime() {
		return logTime;
	}
	public void setLogTime(Long logTime) {
		this.logTime = logTime;
	}

	/** メッセージ */
	public String getLog() {
		return log;
	}
	public void setLog(String log) {
		this.log = log;
	}

	/** ログ種別(開始、終了、エラー) */
	public LogTypeEnum getLogType() {
		return logType;
	}
	public void setLogType(LogTypeEnum logType) {
		this.logType = logType;
	}


	/** 手動実行時間 */
	public Long getManualTime() {
		return this.manualTime;
	}
	public void setManualTime(Long manualTime) {
		this.manualTime = manualTime;
	}


	/** 実行時間 */
	public Long getRunTime() {
		return this.runTime;
	}
	public void setRunTime(Long runTime) {
		this.runTime = runTime;
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

	/** 重要度 */
	public RpaLogParseResult.Priority getPriority() {
		return priority;
	}
	public void setPriority(RpaLogParseResult.Priority priority) {
		this.priority = priority;
	}
	
	@Override
	public String toString() {
		return "RpaScenarioOperationResultDetailResponse [logTime=" + logTime + ", log=" + log + ", logType=" + logType + ", manualTime=" + manualTime + ", runTime="
				+ runTime + ", coefficientCost=" + coefficientCost + ", reductionRate=" + reductionRate + ", reductionTime=" + reductionTime + ", priority=" + priority
				+ "]";
	}
}
