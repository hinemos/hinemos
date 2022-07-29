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
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PrioritySelectEnum;

public class AbstractJobResponse {

	/** 所属ジョブユニットのジョブID */
	private String jobunitId;

	/** ジョブID */
	private String id;

	/** 親ジョブID */
	private String parentId;

	/** ジョブ名 */
	private String name;

	/** ジョブ種別 */
	@RestBeanConvertEnum
	private JobTypeEnum type;

	/** ジョブ待ち条件情報 */
	private JobWaitRuleInfoResponse waitRule;

	/** ジョブ終了状態情報 */
	private ArrayList<JobEndStatusInfoResponse> endStatus = new ArrayList<>();

	/** アイコンID */
	private String iconId;

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
	private String description;

	/** オーナーロールID */
	private String ownerRoleId;
	
	/** モジュール登録済フラグ */
	private Boolean registered;
	
	//ジョブ通知関連
	@RestBeanConvertEnum
	private PrioritySelectEnum beginPriority;
	@RestBeanConvertEnum
	private PrioritySelectEnum normalPriority;
	@RestBeanConvertEnum
	private PrioritySelectEnum warnPriority;
	@RestBeanConvertEnum
	private PrioritySelectEnum abnormalPriority;
	/** 通知ID**/
	private ArrayList<NotifyRelationInfoResponse> notifyRelationInfos = new ArrayList<>();


	public AbstractJobResponse() {
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


	public String getParentId() {
		return parentId;
	}


	public void setParentId(String parentId) {
		this.parentId = parentId;
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


	public ArrayList<JobEndStatusInfoResponse> getEndStatus() {
		return endStatus;
	}


	public void setEndStatus(ArrayList<JobEndStatusInfoResponse> endStatus) {
		this.endStatus = endStatus;
	}


	public String getIconId() {
		return iconId;
	}


	public void setIconId(String iconId) {
		this.iconId = iconId;
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


	public Boolean isRegistered() {
		return registered;
	}


	public void setRegistered(Boolean registered) {
		this.registered = registered;
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

}
