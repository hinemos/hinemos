/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class CloneBackupedStorageRequest implements RequestDto {
	private String storageId;
	private String storageSnapshotId;
	private BackupedDataRequest modifiedData;

	public CloneBackupedStorageRequest() {
	}

	public String getStorageId() {
		return storageId;
	}

	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}

	public String getStorageSnapshotId() {
		return storageSnapshotId;
	}

	public void setStorageSnapshotId(String storageSnapshotId) {
		this.storageSnapshotId = storageSnapshotId;
	}

	public BackupedDataRequest getModifiedData() {
		return modifiedData;
	}

	public void setModifiedData(BackupedDataRequest modifiedData) {
		this.modifiedData = modifiedData;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
