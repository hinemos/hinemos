/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.xcloud.factory.IResourceManagement.InstanceSnapshot.InstanceSnapshotStatusType;

public class InstanceBackupEntryResponse {
	private String id;
	private String name;
	private String description;

	private InstanceSnapshotStatusType status;
	private String statusAsPlatform;
	@RestBeanConvertDatetime
	private String createTime;
	private BackupedDataResponse backupedData;

	public InstanceBackupEntryResponse() {
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public InstanceSnapshotStatusType getStatus() {
		return status;
	}

	public void setStatus(InstanceSnapshotStatusType status) {
		this.status = status;
	}

	public String getStatudAsPlatform() {
		return statusAsPlatform;
	}

	public void setStatudAsPlatform(String statudAsPlatform) {
		this.statusAsPlatform = statudAsPlatform;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getStatusAsPlatform() {
		return statusAsPlatform;
	}

	public void setStatusAsPlatform(String statusAsPlatform) {
		this.statusAsPlatform = statusAsPlatform;
	}

	public BackupedDataResponse getBackupedData() {
		return backupedData;
	}

	public void setBackupedData(BackupedDataResponse backupedData) {
		this.backupedData = backupedData;
	}

}
