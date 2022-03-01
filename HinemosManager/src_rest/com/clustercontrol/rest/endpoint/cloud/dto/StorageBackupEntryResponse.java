/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.xcloud.factory.IResourceManagement.StorageSnapshot.StorageSnapshotStatusType;

public class StorageBackupEntryResponse {
	private String snapshotId;
	private String name;
	private StorageSnapshotStatusType status;
	private String statusAsPlatform;
	@RestBeanConvertDatetime
	private String createTime;
	private String desription;

	private BackupedDataResponse backupedData;

	public StorageBackupEntryResponse() {
	}

	public String getSnapshotId() {
		return snapshotId;
	}

	public void setSnapshotId(String snapshotId) {
		this.snapshotId = snapshotId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public StorageSnapshotStatusType getStatus() {
		return status;
	}

	public void setStatus(StorageSnapshotStatusType status) {
		this.status = status;
	}

	public String getStatusAsPlatform() {
		return statusAsPlatform;
	}

	public void setStatusAsPlatform(String statusAsPlatform) {
		this.statusAsPlatform = statusAsPlatform;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getDesription() {
		return desription;
	}

	public void setDesription(String desription) {
		this.desription = desription;
	}

	public BackupedDataResponse getBackupedData() {
		return backupedData;
	}

	public void setBackupedData(BackupedDataResponse backupedData) {
		this.backupedData = backupedData;
	}
}
