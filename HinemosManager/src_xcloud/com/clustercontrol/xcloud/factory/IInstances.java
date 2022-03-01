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
import com.clustercontrol.xcloud.bean.AddInstanceRequest;
import com.clustercontrol.xcloud.bean.CloneBackupedInstanceRequest;
import com.clustercontrol.xcloud.bean.ModifyInstanceRequest;
import com.clustercontrol.xcloud.bean.Option;
import com.clustercontrol.xcloud.model.EntityEntity;
import com.clustercontrol.xcloud.model.InstanceBackupEntity;
import com.clustercontrol.xcloud.model.InstanceBackupEntryEntity;
import com.clustercontrol.xcloud.model.InstanceEntity;


public interface IInstances extends IResources {
	public final static String backup_instanceName = "instanceName";
	public final static String backup_memo = "memo";
	public final static String backup_tags = "tags";
	
	IInstances setNodeRegist(boolean autoRegist);
	IInstances setInstanceDelete(boolean autoDelete);
	IInstances setNodeDelete(boolean autoNodeDelete);
	IInstances setCacheResourceManagement(CacheResourceManagement rm);
	
	InstanceEntity addInstance(AddInstanceRequest request) throws CloudManagerException;
	
	List<InstanceEntity> removeInstances(List<String> instanceIds) throws CloudManagerException;
	
	void powerOnInstances(List<String> instanceIds) throws CloudManagerException;
	void powerOffInstances(List<String> instanceIds) throws CloudManagerException;
	void suspendInstances(List<String> instanceIds) throws CloudManagerException;
	void rebootInstances(List<String> instanceIds) throws CloudManagerException;
	
	InstanceEntity getInstance(String instanceId) throws CloudManagerException;
	List<InstanceEntity> getInstances(List<String> instanceIds) throws CloudManagerException;
	InstanceEntity modifyInstance(ModifyInstanceRequest request) throws CloudManagerException;
	
	List<InstanceEntity> updateInstances(List<String> instanceIds) throws CloudManagerException;
	
	InstanceBackupEntity takeInstanceSnapshot(String instanceId, String name, String description, List<Option> options) throws CloudManagerException;

	List<InstanceBackupEntryEntity> deletInstanceSnapshots(String instanceId, List<String> snapshotIds) throws CloudManagerException;
	List<InstanceBackupEntity> updateInstanceBackups(List<String> instanceIds) throws CloudManagerException;
	InstanceEntity cloneBackupedInstance(CloneBackupedInstanceRequest request) throws CloudManagerException;

	InstanceEntity getInstanceByFacilityId(String facilityId) throws CloudManagerException;

	EntityEntity getEntityByPlatformEntityId(String platformEntityId) throws CloudManagerException;
	
	InstanceEntity assignNode(String instanceId) throws CloudManagerException;
}
