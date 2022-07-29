/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PrioritySelectEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ReferJobSelectTypeEnum;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoRequest;

public class JobInfoRequest implements RequestDto{
	
	/**
	 * ジョブツリーの情報だけの場合はfalse
	 * 全てのプロパティ値が入っている場合はtrue
	 **/
	private Boolean isUpdateTaget = false;

	/** 所属ジョブユニットのジョブID */
	private String jobunitId;

	/** ジョブID */
	private String id;

	/** ジョブ名 */
	private String name;

	/** ジョブ種別 */
	@RestBeanConvertEnum
	private JobTypeEnum type = JobTypeEnum.JOBUNIT;

	/** ジョブ待ち条件情報 */
	private JobWaitRuleInfoRequest waitRule;

	/** ジョブコマンド情報 */
	private JobCommandInfoRequest command;

	/** ジョブファイル転送情報 */
	private JobFileInfoRequest file;

	/** 監視ジョブ情報 */
	private JobMonitorInfoRequest monitor;

	/** ジョブ連携送信ジョブ情報 */
	private JobLinkSendInfoRequest jobLinkSend;

	/** ジョブ連携待機ジョブ情報 */
	private JobLinkRcvInfoRequest jobLinkRcv;

	/** ファイルチェックジョブ情報 */
	private JobFileCheckInfoRequest jobFileCheck;

	/** リソースジョブ情報 */
	private JobResourceInfoRequest resource;

	/** RPAシナリオジョブ情報 */
	private JobRpaInfoRequest rpa;

	/** ジョブ終了状態情報 */
	private ArrayList<JobEndStatusInfoRequest> endStatus;

	/** ジョブ変数情報 */
	private ArrayList<JobParameterInfoRequest> param;

	/** 参照先ジョブユニットID */
	private String referJobUnitId;

	/** 参照先ジョブID */
	private String referJobId;

	/** アイコンID */
	private String iconId;

	/** 参照ジョブ選択種別 */
	@RestBeanConvertEnum
	private ReferJobSelectTypeEnum referJobSelectType = ReferJobSelectTypeEnum.JOB_TREE; 

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
	private boolean isUseApprovalReqSentence;

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
	/** 通知ID **/
	private ArrayList<NotifyRelationInfoRequest> notifyRelationInfos;

	/** ジョブ待ち条件情報が変更したか */
	private Boolean isWaitRuleChanged = false;

	/** 参照先ジョブ情報が変更したか */
	private Boolean isReferJobChanged = false;

	public JobInfoRequest() {
	}



	public Boolean isUpdateTaget() {
		return isUpdateTaget;
	}



	public void setUpdateTaget(Boolean isUpdateTaget) {
		this.isUpdateTaget = isUpdateTaget;
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



	public JobWaitRuleInfoRequest getWaitRule() {
		return waitRule;
	}



	public void setWaitRule(JobWaitRuleInfoRequest waitRule) {
		this.waitRule = waitRule;
	}



	public JobCommandInfoRequest getCommand() {
		return command;
	}



	public void setCommand(JobCommandInfoRequest command) {
		this.command = command;
	}



	public JobFileInfoRequest getFile() {
		return file;
	}



	public void setFile(JobFileInfoRequest file) {
		this.file = file;
	}



	public JobMonitorInfoRequest getMonitor() {
		return monitor;
	}



	public void setMonitor(JobMonitorInfoRequest monitor) {
		this.monitor = monitor;
	}



	public JobLinkSendInfoRequest getJobLinkSend() {
		return jobLinkSend;
	}



	public void setJobLinkSend(JobLinkSendInfoRequest jobLinkSend) {
		this.jobLinkSend = jobLinkSend;
	}



	public JobLinkRcvInfoRequest getJobLinkRcv() {
		return jobLinkRcv;
	}



	public void setJobLinkRcv(JobLinkRcvInfoRequest jobLinkRcv) {
		this.jobLinkRcv = jobLinkRcv;
	}



	public JobFileCheckInfoRequest getJobFileCheck() {
		return jobFileCheck;
	}



	public void setJobFileCheck(JobFileCheckInfoRequest jobFileCheck) {
		this.jobFileCheck = jobFileCheck;
	}



	public JobResourceInfoRequest getResource() {
		return resource;
	}

	public void setResource(JobResourceInfoRequest resource) {
		this.resource = resource;
	}

	public JobRpaInfoRequest getRpa() {
		return rpa;
	}

	public void setRpa(JobRpaInfoRequest rpa) {
		this.rpa = rpa;
	}



	public ArrayList<JobEndStatusInfoRequest> getEndStatus() {
		return endStatus;
	}



	public void setEndStatus(ArrayList<JobEndStatusInfoRequest> endStatus) {
		this.endStatus = endStatus;
	}



	public ArrayList<JobParameterInfoRequest> getParam() {
		return param;
	}



	public void setParam(ArrayList<JobParameterInfoRequest> param) {
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



	public ArrayList<NotifyRelationInfoRequest> getNotifyRelationInfos() {
		return notifyRelationInfos;
	}



	public void setNotifyRelationInfos(ArrayList<NotifyRelationInfoRequest> notifyRelationInfos) {
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



	@Override
	public void correlationCheck() throws InvalidSetting {
		if (waitRule != null) {
			waitRule.correlationCheck();
		}
		if (command != null) {
			command.correlationCheck();
		}
		if (file != null) {
			file.correlationCheck();
		}
		if (monitor != null) {
			monitor.correlationCheck();
		}
		if (jobLinkSend != null) {
			jobLinkSend.correlationCheck();
		}
		if (jobLinkRcv != null) {
			jobLinkRcv.correlationCheck();
		}
		if (jobFileCheck != null) {
			jobFileCheck.correlationCheck();
		}
		if (resource != null) {
			resource.correlationCheck();
		}
		if (rpa != null) {
			rpa.correlationCheck();
		}
		if (endStatus != null) {
			for (JobEndStatusInfoRequest req : endStatus) {
				req.correlationCheck();
			}
		}
		if (param != null) {
			for (JobParameterInfoRequest req : param) {
				req.correlationCheck();
			}
		}
		if (notifyRelationInfos != null) {
			for (NotifyRelationInfoRequest req : notifyRelationInfos) {
				req.correlationCheck();
			}
		}
	}

}
