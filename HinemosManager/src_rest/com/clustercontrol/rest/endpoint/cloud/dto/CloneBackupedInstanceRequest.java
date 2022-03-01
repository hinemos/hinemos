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

public class CloneBackupedInstanceRequest implements RequestDto {
	private String instanceId;
	private String instanceSnapshotId;
	private BackupedDataRequest modifiedData;
	public CloneBackupedInstanceRequest() {
	}
	@Override
	public void correlationCheck() throws InvalidSetting {
	}
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
	public BackupedDataRequest getModifiedData() {
		return modifiedData;
	}
	public void setModifiedData(BackupedDataRequest modifiedData) {
		this.modifiedData = modifiedData;
	}
}
