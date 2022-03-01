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
