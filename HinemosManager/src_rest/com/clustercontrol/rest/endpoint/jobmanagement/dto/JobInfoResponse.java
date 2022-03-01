/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PrioritySelectEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ReferJobSelectTypeEnum;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoResponse;

public class JobInfoResponse {

	/** 所属ジョブユニットのジョブID */
	private String jobunitId;

	/** ジョブID */
	private String id;
	/** ジョブ名 */
	@RestPartiallyTransrateTarget
	private String name;

	/** ジョブ種別 */
	@RestBeanConvertEnum
	private JobTypeEnum type = JobTypeEnum.JOBUNIT;

	/** ジョブ待ち条件情報 */
	private JobWaitRuleInfoResponse waitRule;

	/** ジョブコマンド情報 */
	private JobCommandInfoResponse command;

	/** ジョブファイル転送情報 */
	private JobFileInfoResponse file;

	/** 監視ジョブ情報 */
	private JobMonitorInfoResponse monitor;

	/** ジョブ連携送信ジョブ情報 */
	private JobLinkSendInfoResponse jobLinkSend;

	/** ジョブ連携待機ジョブ情報 */
	private JobLinkRcvInfoResponse jobLinkRcv;

	/** ファイルチェックジョブ情報 */
	private JobFileCheckInfoResponse jobFileCheck;

	/** リソース制御ジョブ情報 */
	private JobResourceInfoResponse resource;

	/** RPAシナリオジョブ情報 */
	private JobRpaInfoResponse rpa;

	/** ジョブ終了状態情報 */
	private ArrayList<JobEndStatusInfoResponse> endStatus;

	/** ジョブ変数情報 */
	private ArrayList<JobParameterInfoResponse> param;

	/** 参照先ジョブユニットID */
	private String referJobUnitId;

	/** 参照先ジョブID */
	private String referJobId;

	/** アイコンID */
	private String iconId;

	/** 参照ジョブ選択種別 */
	@RestBeanConvertEnum
	private ReferJobSelectTypeEnum referJobSelectType = ReferJobSelectTypeEnum.JOB_TREE; 

	/** 作成日時 */
	@RestBeanConvertDatetime
	private String createTime;

	/** 最終更新日時 */
	@RestBeanConvertDatetime
	private String updateTime; 

	/** 新規作成ユーザ */
	private String createUser;

	/** 最終更新ユーザ */
	private String updateUser;

	/** 説明 */
	private String description = "";

	/** オーナーロールID */
	private String ownerRoleId = "";
	
	/** モジュール登録済フラグ */
	private boolean registered = false;
	
	/** 承認依頼先ロールID */
	private String approvalReqRoleId = "";
	
	/** 承認依頼先ユーザID */
	private String approvalReqUserId = "";
	
	/** 承認依頼文 */
	private String approvalReqSentence = "";
	
	/** 承認依頼メール件名 */
	private String approvalReqMailTitle = "";
	
	/** 承認依頼メール本文 */
	private String approvalReqMailBody = "";
	
	/** 承認依頼文の利用有無フラグ */
	private boolean isUseApprovalReqSentence = false;

	/** 実行対象ノードの決定タイミング */
	private boolean expNodeRuntimeFlg;

	//ジョブ通知関連
	@RestBeanConvertEnum
	private PrioritySelectEnum beginPriority = PrioritySelectEnum.CRITICAL;
	@RestBeanConvertEnum
	private PrioritySelectEnum normalPriority =  PrioritySelectEnum.CRITICAL;
	@RestBeanConvertEnum
	private PrioritySelectEnum warnPriority =  PrioritySelectEnum.CRITICAL;
	@RestBeanConvertEnum
	private PrioritySelectEnum abnormalPriority = PrioritySelectEnum.CRITICAL;
	/** 通知ID**/
	private ArrayList<NotifyRelationInfoResponse> notifyRelationInfos;

	/** ジョブ待ち条件情報が変更したか */
	private Boolean isWaitRuleChanged = false;

	/** 参照先ジョブ情報が変更したか */
	private Boolean isReferJobChanged = false;

	public JobInfoResponse() {
	}

	public String getJobunitId() {
		return jobunitId;
	}


	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public JobTypeEnum getType() {
		return type;
	}


	public void setType(JobTypeEnum type) {
		this.type = type;
	}


	public JobWaitRuleInfoResponse getWaitRule() {
		return waitRule;
	}


	public void setWaitRule(JobWaitRuleInfoResponse waitRule) {
		this.waitRule = waitRule;
	}


	public JobCommandInfoResponse getCommand() {
		return command;
	}


	public void setCommand(JobCommandInfoResponse command) {
		this.command = command;
	}


	public JobFileInfoResponse getFile() {
		return file;
	}


	public void setFile(JobFileInfoResponse file) {
		this.file = file;
	}


	public JobMonitorInfoResponse getMonitor() {
		return monitor;
	}

	
	public void setMonitor(JobMonitorInfoResponse monitor) {
		this.monitor = monitor;
	}

	
	public JobLinkSendInfoResponse getJobLinkSend() {
		return jobLinkSend;
	}

	public void setJobLinkSend(JobLinkSendInfoResponse jobLinkSend) {
		this.jobLinkSend = jobLinkSend;
	}

	public JobLinkRcvInfoResponse getJobLinkRcv() {
		return jobLinkRcv;
	}

	public void setJobLinkRcv(JobLinkRcvInfoResponse jobLinkRcv) {
		this.jobLinkRcv = jobLinkRcv;
	}

	public JobFileCheckInfoResponse getJobFileCheck() {
		return jobFileCheck;
	}

	public void setJobFileCheck(JobFileCheckInfoResponse jobFileCheck) {
		this.jobFileCheck = jobFileCheck;
	}

	public JobResourceInfoResponse getResource() {
		return resource;
	}

	public void setResource(JobResourceInfoResponse resource) {
		this.resource = resource;
	}

	public JobRpaInfoResponse getRpa() {
		return rpa;
	}

	public void setRpa(JobRpaInfoResponse rpa) {
		this.rpa = rpa;
	}

	public ArrayList<JobEndStatusInfoResponse> getEndStatus() {
		return endStatus;
	}


	public void setEndStatus(ArrayList<JobEndStatusInfoResponse> endStatus) {
		this.endStatus = endStatus;
	}


	public ArrayList<JobParameterInfoResponse> getParam() {
		return param;
	}


	public void setParam(ArrayList<JobParameterInfoResponse> param) {
		this.param = param;
	}


	public String getReferJobUnitId() {
		return referJobUnitId;
	}


	public void setReferJobUnitId(String referJobUnitId) {
		this.referJobUnitId = referJobUnitId;
	}


	public String getReferJobId() {
		return referJobId;
	}


	public void setReferJobId(String referJobId) {
		this.referJobId = referJobId;
	}


	public String getIconId() {
		return iconId;
	}


	public void setIconId(String iconId) {
		this.iconId = iconId;
	}


	public ReferJobSelectTypeEnum getReferJobSelectType() {
		return referJobSelectType;
	}


	public void setReferJobSelectType(ReferJobSelectTypeEnum referJobSelectType) {
		this.referJobSelectType = referJobSelectType;
	}


	public String getCreateTime() {
		return createTime;
	}


	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}


	public String getUpdateTime() {
		return updateTime;
	}


	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}


	public String getCreateUser() {
		return createUser;
	}


	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}


	public String getUpdateUser() {
		return updateUser;
	}


	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getOwnerRoleId() {
		return ownerRoleId;
	}


	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}


	public boolean isRegistered() {
		return registered;
	}


	public void setRegistered(boolean registered) {
		this.registered = registered;
	}


	public String getApprovalReqRoleId() {
		return approvalReqRoleId;
	}


	public void setApprovalReqRoleId(String approvalReqRoleId) {
		this.approvalReqRoleId = approvalReqRoleId;
	}


	public String getApprovalReqUserId() {
		return approvalReqUserId;
	}


	public void setApprovalReqUserId(String approvalReqUserId) {
		this.approvalReqUserId = approvalReqUserId;
	}


	public String getApprovalReqSentence() {
		return approvalReqSentence;
	}


	public void setApprovalReqSentence(String approvalReqSentence) {
		this.approvalReqSentence = approvalReqSentence;
	}


	public String getApprovalReqMailTitle() {
		return approvalReqMailTitle;
	}


	public void setApprovalReqMailTitle(String approvalReqMailTitle) {
		this.approvalReqMailTitle = approvalReqMailTitle;
	}


	public String getApprovalReqMailBody() {
		return approvalReqMailBody;
	}


	public void setApprovalReqMailBody(String approvalReqMailBody) {
		this.approvalReqMailBody = approvalReqMailBody;
	}


	public boolean getIsUseApprovalReqSentence() {
		return isUseApprovalReqSentence;
	}


	public void setIsUseApprovalReqSentence(boolean isUseApprovalReqSentence) {
		this.isUseApprovalReqSentence = isUseApprovalReqSentence;
	}

	public boolean getExpNodeRuntimeFlg() {
		return expNodeRuntimeFlg;
	}

	public void setExpNodeRuntimeFlg(boolean expNodeRuntimeFlg) {
		this.expNodeRuntimeFlg = expNodeRuntimeFlg;
	}


	public PrioritySelectEnum getBeginPriority() {
		return beginPriority;
	}


	public void setBeginPriority(PrioritySelectEnum beginPriority) {
		this.beginPriority = beginPriority;
	}


	public PrioritySelectEnum getNormalPriority() {
		return normalPriority;
	}


	public void setNormalPriority(PrioritySelectEnum normalPriority) {
		this.normalPriority = normalPriority;
	}


	public PrioritySelectEnum getWarnPriority() {
		return warnPriority;
	}


	public void setWarnPriority(PrioritySelectEnum warnPriority) {
		this.warnPriority = warnPriority;
	}


	public PrioritySelectEnum getAbnormalPriority() {
		return abnormalPriority;
	}


	public void setAbnormalPriority(PrioritySelectEnum abnormalPriority) {
		this.abnormalPriority = abnormalPriority;
	}


	public ArrayList<NotifyRelationInfoResponse> getNotifyRelationInfos() {
		return notifyRelationInfos;
	}


	public void setNotifyRelationInfos(ArrayList<NotifyRelationInfoResponse> notifyRelationInfos) {
		this.notifyRelationInfos = notifyRelationInfos;
	}

	public Boolean isWaitRuleChanged() {
		return isWaitRuleChanged;
	}

	public void setWaitRuleChanged(Boolean isWaitRuleChanged) {
		this.isWaitRuleChanged = isWaitRuleChanged;
	}

	public Boolean isReferJobChanged() {
		return isReferJobChanged;
	}

	public void setReferJobChanged(Boolean isReferJobChanged) {
		this.isReferJobChanged = isReferJobChanged;
	}



}
