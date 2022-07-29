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

public class JobInfoResponseP1 {

	/** 所属ジョブユニットのジョブID */
	private String jobunitId;

	/** ジョブID */
	private String id;

	/** ジョブ名 */
	private String name;

	/** ジョブ種別 */
	@RestBeanConvertEnum
	private JobTypeEnum type = JobTypeEnum.JOBUNIT;

	/** オーナーロールID */
	private String ownerRoleId = "";

	/** 実行対象ノードの決定タイミング */
	private boolean expNodeRuntimeFlg = false;

	/** 最終更新日時 */
	@RestBeanConvertDatetime
	private String updateTime; 

	/** 説明 */
	private String description = "";

	/** アイコンID */
	private String iconId;

	/** ジョブ待ち条件情報 */
	private JobWaitRuleInfoResponse waitRule;

	/** 参照先ジョブユニットID */
	private String referJobUnitId;

	/** 参照先ジョブID */
	private String referJobId;	

	/** 参照ジョブ選択種別 */
	@RestBeanConvertEnum
	private ReferJobSelectTypeEnum referJobSelectType = ReferJobSelectTypeEnum.JOB_TREE;

	/** モジュール登録済フラグ */
	private boolean registered = false;
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIconId() {
		return iconId;
	}

	public void setIconId(String iconId) {
		this.iconId = iconId;
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

	public ReferJobSelectTypeEnum getReferJobSelectType() {
		return referJobSelectType;
	}

	public void setReferJobSelectType(ReferJobSelectTypeEnum referJobSelectType) {
		this.referJobSelectType = referJobSelectType;
	}

	public boolean isRegistered() {
		return registered;
	}

	public void setRegistered(boolean registered) {
		this.registered = registered;
	}

	public JobInfoResponseP1() {
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

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public boolean getExpNodeRuntimeFlg() {
		return expNodeRuntimeFlg;
	}

	public void setExpNodeRuntimeFlg(boolean expNodeRuntimeFlg) {
		this.expNodeRuntimeFlg = expNodeRuntimeFlg;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

}
