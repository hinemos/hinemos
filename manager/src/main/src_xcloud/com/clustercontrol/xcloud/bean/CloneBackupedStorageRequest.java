/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

public class CloneBackupedStorageRequest extends Request {
	private String storageId;
	private String storageSnapshotId;
	private BackupedData modifiedData;
	
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

	public BackupedData getModifiedData() {
		return modifiedData;
	}
	public void setModifiedData(BackupedData modifiedData) {
		this.modifiedData = modifiedData;
	}
}
