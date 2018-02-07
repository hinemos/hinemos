/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import com.clustercontrol.xcloud.factory.task.CloudTaskStatus;
import com.clustercontrol.xcloud.persistence.ApplyCurrentTime;

@Entity
@Table(name="cc_cfg_xcloud_task", schema="setting")
public class CloudTaskEntity {
	private String key;
	private String cloudScopeId;
	private String className;
	private String savedData;
	private Boolean autoDelete = false;
	private Boolean optionDependent = false;
	private CloudTaskStatus taskStatus;
	private Long updateDate;
	
	@Id
	@Column(name="task_key")
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}

	@Column(name="cloud_scope_id")
	public String getCloudScopeId() {
		return cloudScopeId;
	}
	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}

	@Column(name="option_dependent_flg")
	public Boolean getOptionDependent() {
		return optionDependent;
	}
	public void setOptionDependent(Boolean optionDependent) {
		this.optionDependent = optionDependent;
	}
	
	@Column(name="auto_delete_flg")
	public Boolean getAutoDelete() {
		return autoDelete;
	}
	public void setAutoDelete(Boolean autoDelete) {
		this.autoDelete = autoDelete;
	}

	@Column(name="class_name")
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	
	@Column(name="saved_data")
	public String getSavedData() {
		return savedData;
	}
	public void setSavedData(String savedData) {
		this.savedData = savedData;
	}
	
	@Column(name="task_status")
	@Enumerated(EnumType.STRING)
	public CloudTaskStatus getTaskStatus() {
		return taskStatus;
	}
	public void setTaskStatus(CloudTaskStatus taskStatus) {
		this.taskStatus = taskStatus;
	}
	
	@Column(name="update_date")
	@ApplyCurrentTime
	public Long getUpdateDate() {
		return this.updateDate;
	}
	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}
}
