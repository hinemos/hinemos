/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.bean.HRepository;
import com.clustercontrol.xcloud.model.CloudScopeEntity;

public interface IRepository {
	public HRepository getRepository() throws CloudManagerException;
	
	public HRepository getRepository(String ownerRole) throws CloudManagerException;
	
	public HRepository updateLocationRepository(String cloudScopeId, String locationId) throws CloudManagerException;
	
	public HRepository updateLocationRepository(String cloudScopeId, String locationId, String hinemosUser) throws CloudManagerException;
	
	public void createCloudScopeRepository(CloudScopeEntity cloudScope) throws CloudManagerException;

	public void removeCloudScopeRepository(CloudScopeEntity cloudScope) throws CloudManagerException;
	
	public IRepository setCacheResourceManagement(CacheResourceManagement rm);
}
