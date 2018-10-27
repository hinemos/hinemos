/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.util.List;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.bean.AddStorageRequest;
import com.clustercontrol.xcloud.bean.CloneBackupedStorageRequest;
import com.clustercontrol.xcloud.bean.Option;
import com.clustercontrol.xcloud.model.InstanceEntity;
import com.clustercontrol.xcloud.model.StorageBackupEntity;
import com.clustercontrol.xcloud.model.StorageEntity;


public interface IStorages extends IResources {
	public final static String backup_storageName = "storageName";
	public final static String backup_storageSize = "storageSize";

	IStorages setCachedInstanceEntity(InstanceEntity instance) throws CloudManagerException;
	IStorages setDoDiffCheck(boolean doDiffCheck);

	StorageEntity addStorage(AddStorageRequest request) throws CloudManagerException;
	
	void removeStorages(List<String> storageIds) throws CloudManagerException;
	
	List<StorageEntity> getStorages(List<String> storageIds) throws CloudManagerException;
	StorageEntity getStorage(String storageId) throws CloudManagerException;
	
	List<StorageEntity> updateStorages(List<String> storageId) throws CloudManagerException;

	void attachStorage(String instanceId, String storageId, List<Option> options) throws CloudManagerException;
	void detachStorage(List<String> storageIds) throws CloudManagerException;

	StorageBackupEntity takeStorageSnapshot(String storageId, String name, String description, List<Option> options) throws CloudManagerException;
	void deletStorageSnapshots(String instanceId, List<String> snapshotIds) throws CloudManagerException;
	List<StorageBackupEntity> updateStorageBackups(List<String> storageIds) throws CloudManagerException;
	StorageEntity cloneBackupedStorage(CloneBackupedStorageRequest request) throws CloudManagerException;
	
	IStorages setCacheResourceManagement(CacheResourceManagement rm);
}
