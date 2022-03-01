/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

public class CloneBackupedInstanceRequest extends Request {
	private String instanceId;
	private String instanceSnapshotId;
	private BackupedData modifiedData;
	
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getInstanceSnapshotId() {
		return instanceSnapshotId;
	}
	public void setInstanceSnapshotId(String instanceSnapshotId) {
		this.instanceSnapshotId = instanceSnapshotId;
	}

	public BackupedData getModifiedData() {
		return modifiedData;
	}
	public void setModifiedData(BackupedData modifiedData) {
		this.modifiedData = modifiedData;
	}
}
