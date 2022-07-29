/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIgnore;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ConditionTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.EndStatusSelectEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.OperationEndDelayEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.OperationMultipleEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.OperationStartDelayEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PrioritySelectEnum;

public class JobWaitRuleInfoResponse {
	/** 保留 */
	private Boolean suspend = false;

	/** スキップ */
	private Boolean skip = false;

	/** スキップ時終了状態 */
	@RestBeanConvertEnum
	private EndStatusSelectEnum skipEndStatus = EndStatusSelectEnum.ABNORMAL;

	/** スキップ時終了値 */
	private Integer skipEndValue = EndStatusConstant.INITIAL_VALUE_NORMAL;

	/** 判定対象の条件関係 */
	@RestBeanConvertEnum
	private ConditionTypeEnum condition =ConditionTypeEnum.AND;

	/** ジョブ判定対象情報  内部の時刻を個別変換する為 自動変換対象外*/
	@RestBeanConvertIgnore
	private ArrayList<JobObjectGroupInfoResponse> objectGroup = new ArrayList<>();

	/** 条件を満たさなければ終了する */
	private Boolean endCondition = false;

	/** 条件を満たさない時の終了状態 */
	@RestBeanConvertEnum
	private EndStatusSelectEnum endStatus = EndStatusSelectEnum.ABNORMAL;

	/** 条件を満たさない時の終了値 */
	private Integer endValue = EndStatusConstant.INITIAL_VALUE_ABNORMAL;

	/** 排他分岐 */
	private Boolean exclusiveBranch = false;

	/** 排他分岐の終了状態 */
	@RestBeanConvertEnum
	private EndStatusSelectEnum exclusiveBranchEndStatus = EndStatusSelectEnum.NORMAL;

	/** 排他分岐の終了値 */
	private Integer exclusiveBranchEndValue = EndStatusConstant.INITIAL_VALUE_WARNING;

	/** 後続ジョブ優先度 */
	private List<JobNextJobOrderInfoResponse> exclusiveBranchNextJobOrderList;

	/** カレンダ */
	private Boolean calendar = false;

	/** カレンダID */
	private String calendarId;

	/** カレンダにより未実行時の終了状態 */
	@RestBeanConvertEnum
	private EndStatusSelectEnum calendarEndStatus = EndStatusSelectEnum.ABNORMAL;

	/** カレンダにより未実行時の終了値 */
	private Integer calendarEndValue = EndStatusConstant.INITIAL_VALUE_NORMAL;
	
	/** 繰り返し実行フラグ */
	private Boolean jobRetryFlg = false;

	/** 繰り返し完了状態 */
	@RestBeanConvertEnum
	private EndStatusSelectEnum jobRetryEndStatus = null;

	/** 繰り返し回数 */
	private Integer jobRetry = 10;

	/** 繰り返し試行回数（分） */
	private Integer jobRetryInterval = 0;

	/** 開始遅延 */
	private Boolean startDelay = false;

	/** 開始遅延セッション開始後の時間 */
	private Boolean startDelaySession = false;

	/** 開始遅延セッション開始後の時間の値 */
	private Integer startDelaySessionValue = 1;

	/** 開始遅延時刻*/ 
	private Boolean startDelayTime = false;

	/** 開始遅延時刻の値  HH:mm:ss 個別変換*/ 
	private String startDelayTimeValue = "00:00:00";

	/** 開始遅延判定対象の条件関係 */
	@RestBeanConvertEnum
	private ConditionTypeEnum startDelayConditionType =ConditionTypeEnum.AND;

	/** 開始遅延通知 */
	private Boolean startDelayNotify = false;

	/** 開始遅延通知重要度 */
	@RestBeanConvertEnum
	private PrioritySelectEnum startDelayNotifyPriority = PrioritySelectEnum.CRITICAL;

	/** 開始遅延操作 */
	private Boolean startDelayOperation = false;

	/** 開始遅延操作種別 */
	@RestBeanConvertEnum
	private OperationStartDelayEnum startDelayOperationType = OperationStartDelayEnum.STOP_SKIP;

	/** 開始遅延操作終了状態 */
	@RestBeanConvertEnum
	private EndStatusSelectEnum startDelayOperationEndStatus = EndStatusSelectEnum.ABNORMAL;

	/** 開始遅延操作終了値 */
	private Integer startDelayOperationEndValue = EndStatusConstant.INITIAL_VALUE_ABNORMAL;

	/** 終了遅延 */
	private Boolean endDelay = false;

	/** 終了遅延セッション開始後の時間 */
	private Boolean endDelaySession = false;

	/** 終了遅延セッション開始後の時間の値 */
	private Integer endDelaySessionValue = 1;

	/** 終了遅延ジョブ開始後の時間 */
	private Boolean endDelayJob = false;

	/** 終了遅延ジョブ開始後の時間の値 */
	private Integer endDelayJobValue = 1;

	/** 終了遅延時刻 */
	private Boolean endDelayTime = false;

	/** 終了遅延時刻の値 HH:mm:ss 個別変換 */
	private String endDelayTimeValue = "00:00:00";

	/** 終了遅延判定対象の条件関係 */
	@RestBeanConvertEnum
	private ConditionTypeEnum endDelayConditionType = ConditionTypeEnum.AND;

	/** 終了遅延通知 */
	private Boolean endDelayNotify = false;

	/** 終了遅延通知重要度 */
	@RestBeanConvertEnum
	private PrioritySelectEnum endDelayNotifyPriority = PrioritySelectEnum.CRITICAL;

	/** 終了遅延操作 */
	private Boolean endDelayOperation = false;

	/** 終了遅延操作種別 */
	@RestBeanConvertEnum
	private OperationEndDelayEnum endDelayOperationType = OperationEndDelayEnum.STOP_AT_ONCE;

	/** 終了遅延操作終了状態 */
	@RestBeanConvertEnum
	private EndStatusSelectEnum endDelayOperationEndStatus = EndStatusSelectEnum.ABNORMAL;

	/** 終了遅延操作終了値 */
	private Integer endDelayOperationEndValue = EndStatusConstant.INITIAL_VALUE_ABNORMAL;

	/** 終了遅延実行履歴からの変化量 */
	private Boolean endDelayChangeMount = false;

	/** 終了遅延実行履歴からの変化量の値 */
	private Double endDelayChangeMountValue = 1D;

	/** 多重度 */
	private Boolean multiplicityNotify = true;
	@RestBeanConvertEnum
	private PrioritySelectEnum multiplicityNotifyPriority = PrioritySelectEnum.WARNING;
	@RestBeanConvertEnum
	private OperationMultipleEnum multiplicityOperation = OperationMultipleEnum.WAIT;
	private Integer multiplicityEndValue = -1;
	
	/** 同時実行制御キュー */
	private Boolean queueFlg = false;
	private String queueId = null;
	

	public JobWaitRuleInfoResponse() {
	}


	public Boolean getSuspend() {
		return suspend;
	}


	public void setSuspend(Boolean suspend) {
		this.suspend = suspend;
	}


	public Boolean getSkip() {
		return skip;
	}


	public void setSkip(Boolean skip) {
		this.skip = skip;
	}


	public EndStatusSelectEnum getSkipEndStatus() {
		return skipEndStatus;
	}


	public void setSkipEndStatus(EndStatusSelectEnum skipEndStatus) {
		this.skipEndStatus = skipEndStatus;
	}


	public Integer getSkipEndValue() {
		return skipEndValue;
	}


	public void setSkipEndValue(Integer skipEndValue) {
		this.skipEndValue = skipEndValue;
	}


	public ConditionTypeEnum getCondition() {
		return condition;
	}


	public void setCondition(ConditionTypeEnum condition) {
		this.condition = condition;
	}


	public ArrayList<JobObjectGroupInfoResponse> getObjectGroup() {
		return objectGroup;
	}


	public void setObjectGroup(ArrayList<JobObjectGroupInfoResponse> objectGroup) {
		this.objectGroup = objectGroup;
	}


	public Boolean getEndCondition() {
		return endCondition;
	}


	public void setEndCondition(Boolean endCondition) {
		this.endCondition = endCondition;
	}


	public EndStatusSelectEnum getEndStatus() {
		return endStatus;
	}


	public void setEndStatus(EndStatusSelectEnum endStatus) {
		this.endStatus = endStatus;
	}


	public Integer getEndValue() {
		return endValue;
	}


	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}


	public Boolean getExclusiveBranch() {
		return exclusiveBranch;
	}


	public void setExclusiveBranch(Boolean exclusiveBranch) {
		this.exclusiveBranch = exclusiveBranch;
	}


	public EndStatusSelectEnum getExclusiveBranchEndStatus() {
		return exclusiveBranchEndStatus;
	}


	public void setExclusiveBranchEndStatus(EndStatusSelectEnum exclusiveBranchEndStatus) {
		this.exclusiveBranchEndStatus = exclusiveBranchEndStatus;
	}


	public Integer getExclusiveBranchEndValue() {
		return exclusiveBranchEndValue;
	}


	public void setExclusiveBranchEndValue(Integer exclusiveBranchEndValue) {
		this.exclusiveBranchEndValue = exclusiveBranchEndValue;
	}


	public List<JobNextJobOrderInfoResponse> getExclusiveBranchNextJobOrderList() {
		return exclusiveBranchNextJobOrderList;
	}


	public void setExclusiveBranchNextJobOrderList(List<JobNextJobOrderInfoResponse> exclusiveBranchNextJobOrderList) {
		this.exclusiveBranchNextJobOrderList = exclusiveBranchNextJobOrderList;
	}


	public Boolean getCalendar() {
		return calendar;
	}


	public void setCalendar(Boolean calendar) {
		this.calendar = calendar;
	}


	public String getCalendarId() {
		return calendarId;
	}


	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}


	public EndStatusSelectEnum getCalendarEndStatus() {
		return calendarEndStatus;
	}


	public void setCalendarEndStatus(EndStatusSelectEnum calendarEndStatus) {
		this.calendarEndStatus = calendarEndStatus;
	}


	public Integer getCalendarEndValue() {
		return calendarEndValue;
	}


	public void setCalendarEndValue(Integer calendarEndValue) {
		this.calendarEndValue = calendarEndValue;
	}


	public Boolean getJobRetryFlg() {
		return jobRetryFlg;
	}


	public void setJobRetryFlg(Boolean jobRetryFlg) {
		this.jobRetryFlg = jobRetryFlg;
	}


	public EndStatusSelectEnum getJobRetryEndStatus() {
		return jobRetryEndStatus;
	}


	public void setJobRetryEndStatus(EndStatusSelectEnum jobRetryEndStatus) {
		this.jobRetryEndStatus = jobRetryEndStatus;
	}


	public Integer getJobRetry() {
		return jobRetry;
	}


	public void setJobRetry(Integer jobRetry) {
		this.jobRetry = jobRetry;
	}


	public Integer getJobRetryInterval() {
		return jobRetryInterval;
	}


	public void setJobRetryInterval(Integer jobRetryInterval) {
		this.jobRetryInterval = jobRetryInterval;
	}


	public Boolean getStartDelay() {
		return startDelay;
	}


	public void setStartDelay(Boolean startDelay) {
		this.startDelay = startDelay;
	}


	public Boolean getStartDelaySession() {
		return startDelaySession;
	}


	public void setStartDelaySession(Boolean startDelaySession) {
		this.startDelaySession = startDelaySession;
	}


	public Integer getStartDelaySessionValue() {
		return startDelaySessionValue;
	}


	public void setStartDelaySessionValue(Integer startDelaySessionValue) {
		this.startDelaySessionValue = startDelaySessionValue;
	}


	public Boolean getStartDelayTime() {
		return startDelayTime;
	}


	public void setStartDelayTime(Boolean startDelayTime) {
		this.startDelayTime = startDelayTime;
	}


	public String getStartDelayTimeValue() {
		return startDelayTimeValue;
	}


	public void setStartDelayTimeValue(String startDelayTimeValue) {
		this.startDelayTimeValue = startDelayTimeValue;
	}


	public ConditionTypeEnum getStartDelayConditionType() {
		return startDelayConditionType;
	}


	public void setStartDelayConditionType(ConditionTypeEnum startDelayConditionType) {
		this.startDelayConditionType = startDelayConditionType;
	}


	public Boolean getStartDelayNotify() {
		return startDelayNotify;
	}


	public void setStartDelayNotify(Boolean startDelayNotify) {
		this.startDelayNotify = startDelayNotify;
	}


	public PrioritySelectEnum getStartDelayNotifyPriority() {
		return startDelayNotifyPriority;
	}


	public void setStartDelayNotifyPriority(PrioritySelectEnum startDelayNotifyPriority) {
		this.startDelayNotifyPriority = startDelayNotifyPriority;
	}


	public Boolean getStartDelayOperation() {
		return startDelayOperation;
	}


	public void setStartDelayOperation(Boolean startDelayOperation) {
		this.startDelayOperation = startDelayOperation;
	}


	public OperationStartDelayEnum getStartDelayOperationType() {
		return startDelayOperationType;
	}


	public void setStartDelayOperationType(OperationStartDelayEnum startDelayOperationType) {
		this.startDelayOperationType = startDelayOperationType;
	}


	public EndStatusSelectEnum getStartDelayOperationEndStatus() {
		return startDelayOperationEndStatus;
	}


	public void setStartDelayOperationEndStatus(EndStatusSelectEnum startDelayOperationEndStatus) {
		this.startDelayOperationEndStatus = startDelayOperationEndStatus;
	}


	public Integer getStartDelayOperationEndValue() {
		return startDelayOperationEndValue;
	}


	public void setStartDelayOperationEndValue(Integer startDelayOperationEndValue) {
		this.startDelayOperationEndValue = startDelayOperationEndValue;
	}


	public Boolean getEndDelay() {
		return endDelay;
	}


	public void setEndDelay(Boolean endDelay) {
		this.endDelay = endDelay;
	}


	public Boolean getEndDelaySession() {
		return endDelaySession;
	}


	public void setEndDelaySession(Boolean endDelaySession) {
		this.endDelaySession = endDelaySession;
	}


	public Integer getEndDelaySessionValue() {
		return endDelaySessionValue;
	}


	public void setEndDelaySessionValue(Integer endDelaySessionValue) {
		this.endDelaySessionValue = endDelaySessionValue;
	}


	public Boolean getEndDelayJob() {
		return endDelayJob;
	}


	public void setEndDelayJob(Boolean endDelayJob) {
		this.endDelayJob = endDelayJob;
	}


	public Integer getEndDelayJobValue() {
		return endDelayJobValue;
	}


	public void setEndDelayJobValue(Integer endDelayJobValue) {
		this.endDelayJobValue = endDelayJobValue;
	}


	public Boolean getEndDelayTime() {
		return endDelayTime;
	}


	public void setEndDelayTime(Boolean endDelayTime) {
		this.endDelayTime = endDelayTime;
	}


	public String getEndDelayTimeValue() {
		return endDelayTimeValue;
	}


	public void setEndDelayTimeValue(String endDelayTimeValue) {
		this.endDelayTimeValue = endDelayTimeValue;
	}


	public ConditionTypeEnum getEndDelayConditionType() {
		return endDelayConditionType;
	}


	public void setEndDelayConditionType(ConditionTypeEnum endDelayConditionType) {
		this.endDelayConditionType = endDelayConditionType;
	}


	public Boolean getEndDelayNotify() {
		return endDelayNotify;
	}


	public void setEndDelayNotify(Boolean endDelayNotify) {
		this.endDelayNotify = endDelayNotify;
	}


	public PrioritySelectEnum getEndDelayNotifyPriority() {
		return endDelayNotifyPriority;
	}


	public void setEndDelayNotifyPriority(PrioritySelectEnum endDelayNotifyPriority) {
		this.endDelayNotifyPriority = endDelayNotifyPriority;
	}


	public Boolean getEndDelayOperation() {
		return endDelayOperation;
	}


	public void setEndDelayOperation(Boolean endDelayOperation) {
		this.endDelayOperation = endDelayOperation;
	}


	public OperationEndDelayEnum getEndDelayOperationType() {
		return endDelayOperationType;
	}


	public void setEndDelayOperationType(OperationEndDelayEnum endDelayOperationType) {
		this.endDelayOperationType = endDelayOperationType;
	}


	public EndStatusSelectEnum getEndDelayOperationEndStatus() {
		return endDelayOperationEndStatus;
	}


	public void setEndDelayOperationEndStatus(EndStatusSelectEnum endDelayOperationEndStatus) {
		this.endDelayOperationEndStatus = endDelayOperationEndStatus;
	}


	public Integer getEndDelayOperationEndValue() {
		return endDelayOperationEndValue;
	}


	public void setEndDelayOperationEndValue(Integer endDelayOperationEndValue) {
		this.endDelayOperationEndValue = endDelayOperationEndValue;
	}


	public Boolean getEndDelayChangeMount() {
		return endDelayChangeMount;
	}


	public void setEndDelayChangeMount(Boolean endDelayChangeMount) {
		this.endDelayChangeMount = endDelayChangeMount;
	}


	public Double getEndDelayChangeMountValue() {
		return endDelayChangeMountValue;
	}


	public void setEndDelayChangeMountValue(Double endDelayChangeMountValue) {
		this.endDelayChangeMountValue = endDelayChangeMountValue;
	}


	public Boolean getMultiplicityNotify() {
		return multiplicityNotify;
	}


	public void setMultiplicityNotify(Boolean multiplicityNotify) {
		this.multiplicityNotify = multiplicityNotify;
	}


	public PrioritySelectEnum getMultiplicityNotifyPriority() {
		return multiplicityNotifyPriority;
	}


	public void setMultiplicityNotifyPriority(PrioritySelectEnum multiplicityNotifyPriority) {
		this.multiplicityNotifyPriority = multiplicityNotifyPriority;
	}


	public OperationMultipleEnum getMultiplicityOperation() {
		return multiplicityOperation;
	}


	public void setMultiplicityOperation(OperationMultipleEnum multiplicityOperation) {
		this.multiplicityOperation = multiplicityOperation;
	}


	public Integer getMultiplicityEndValue() {
		return multiplicityEndValue;
	}


	public void setMultiplicityEndValue(Integer multiplicityEndValue) {
		this.multiplicityEndValue = multiplicityEndValue;
	}


	public Boolean getQueueFlg() {
		return queueFlg;
	}


	public void setQueueFlg(Boolean queueFlg) {
		this.queueFlg = queueFlg;
	}


	public String getQueueId() {
		return queueId;
	}


	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}


}
