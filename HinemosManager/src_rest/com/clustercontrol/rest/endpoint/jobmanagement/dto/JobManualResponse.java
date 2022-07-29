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

public class JobManualResponse {
	/** 実行契機ID */
	private String id;

	/** 実行契機名 */
	private String name;

	/** ジョブID */
	private String jobId;

	/** ジョブ名 */
	private String jobName;

	/** ジョブユニットID */
	private String jobunitId;

	/** オーナーロールID */
	private String ownerRoleId;

	/** 新規作成ユーザ */
	private String createUser;

	/** 作成日時 */
	@RestBeanConvertDatetime
	private String createTime;

	/** 最新更新ユーザ */
	private String updateUser;

	/** 最新更新日時 */
	@RestBeanConvertDatetime
	private String updateTime;

	/** 有効/無効 */
	private Boolean valid = false;

	/** ランタイムジョブ変数情報 */
	private ArrayList<JobRuntimeParamRequest> jobRuntimeParamList;

	public JobManualResponse(){
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean isValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
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

	public String getJobunitId() {
		return jobunitId;
	}

	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public ArrayList<JobRuntimeParamRequest> getJobRuntimeParamList() {
		return jobRuntimeParamList;
	}

	public void setJobRuntimeParamList(ArrayList<JobRuntimeParamRequest> jobRuntimeParamList) {
		this.jobRuntimeParamList = jobRuntimeParamList;
	}

}
