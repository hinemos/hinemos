/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.DecisionObjectEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.WaitStatusEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JudgmentObjectEnum;

public class JobObjectInfoResponse {

	/** 判定対象種別 */
	@RestBeanConvertEnum
	private JudgmentObjectEnum type;

	/** ジョブID */
	private String jobId;

	/** ジョブ名 */
	private String jobName;

	/** 終了状態 */
	@RestBeanConvertEnum
	private WaitStatusEnum status;

	/** 終了値 */
	private String value;

	/** 時刻 HH:mm:ss想定 個別変換*/
	private String time;

	/** セッション開始時の時間（分） */
	private Integer startMinute;

	/** 説明 */
	private String description;

	/** 判定値1 */
	private String decisionValue;

	/** 判定条件 */
	@RestBeanConvertEnum
	private DecisionObjectEnum decisionCondition;

	/** セッション横断ジョブ履歴判定対象範囲（分）*/
	private Integer crossSessionRange;

	public JobObjectInfoResponse() {
	}

	public JudgmentObjectEnum getType() {
		return type;
	}

	public void setType(JudgmentObjectEnum type) {
		this.type = type;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public WaitStatusEnum getStatus() {
		return status;
	}

	public void setStatus(WaitStatusEnum status) {
		this.status = status;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public Integer getStartMinute() {
		return startMinute;
	}

	public void setStartMinute(Integer startMinute) {
		this.startMinute = startMinute;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDecisionValue() {
		return decisionValue;
	}

	public void setDecisionValue(String decisionValue) {
		this.decisionValue = decisionValue;
	}

	public DecisionObjectEnum getDecisionCondition() {
		return decisionCondition;
	}

	public void setDecisionCondition(DecisionObjectEnum decisionCondition) {
		this.decisionCondition = decisionCondition;
	}

	public Integer getCrossSessionRange() {
		return crossSessionRange;
	}

	public void setCrossSessionRange(Integer crossSessionRange) {
		this.crossSessionRange = crossSessionRange;
	}
}
