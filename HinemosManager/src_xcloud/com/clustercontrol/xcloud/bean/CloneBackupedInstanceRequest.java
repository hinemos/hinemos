/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;


/**
 * インスタンス作成要求に必要な情報を保持するクラス。 
 * {@link com.clustercontrol.ws.cloud.CloudEndpoint#addInstance(String,String,CreateInstanceRequest request) addInstance 関数} にて使用される。
 *
 */
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
