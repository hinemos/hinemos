/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ReferJobSelectTypeEnum;

public class ReferJobInfoResponse {
	// 参照ジョブは他ジョブの共通項目を保持していないので AbstractJobResponseは継承しない
	
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

	/** 参照先ジョブユニットID */
	private String referJobUnitId;

	/** 参照先ジョブID */
	private String referJobId;

	/** アイコンID */
	private String iconId;

	/** 参照ジョブ選択種別 */
	@RestBeanConvertEnum
	private ReferJobSelectTypeEnum referJobSelectType; 

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


	public ReferJobInfoResponse() {
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



}
