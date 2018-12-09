/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.factory.IResourceManagement;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.LocationEntity;

public class FacilityIdUtil {
	private FacilityIdUtil() {
	}
	
	public static String nextId() {
		if ((System.getProperty("db.name") == null || System.getProperty("db.name").equals("sqlserver")) && System.getProperty("os.name").toLowerCase().startsWith("windows")) {
			Logger.getLogger(FacilityIdUtil.class).debug("FacilityIdUtil.nextId(): OS=" +System.getProperty("os.name") + ",query=SELECT NEXT VALUE FOR setting.cc_cfg_xcloud_instance_id_sequence");
			Query query = Session.current().getEntityManager().createNativeQuery("SELECT NEXT VALUE FOR setting.cc_cfg_xcloud_instance_id_sequence");
			return String.format("%06d", (Long)query.getSingleResult());
		} else {
			Logger.getLogger(FacilityIdUtil.class).debug("FacilityIdUtil.nextId(): OS=" +System.getProperty("os.name") + ",query=SELECT nextval('setting.cc_cfg_xcloud_instance_id_sequence')");
			Query query = Session.current().getEntityManager().createNativeQuery("SELECT nextval('setting.cc_cfg_xcloud_instance_id_sequence')");
			return String.format("%06d", (Long)query.getSingleResult());
		}
	}
	
	public static String getCloudScopeNodeId(String platformId, String cloudScopeId) {
		return String.format("_%s_%s_Node", platformId, cloudScopeId);
	}

	public static String getCloudScopeScopeId(String platformId, String cloudScopeId) {
		return String.format("_%s_%s", platformId, cloudScopeId);
	}
	
	public static String getCloudScopeScopeId(CloudScopeEntity cloudScope) {
		return getCloudScopeScopeId(cloudScope.getPlatformId(), cloudScope.getCloudScopeId());
	}
	
	public static String getResourceId(String type, String cloudScopeId, String resourceId) {
		return String.format("_%s_%s_%s", type, cloudScopeId, resourceId);
	}
	
	public static String getLocationScopeId(String cloudScopeId, LocationEntity location) {
		return getResourceId(location.getLocationType(), cloudScopeId, location.getLocationId());
	}
	
	public static String getFolderId(String cloudScopeId, IResourceManagement.Folder o1) {
		return FacilityIdUtil.getResourceId(o1.getElementType(), cloudScopeId, o1.getId());
	}
	
	public static String getEntityId(String cloudScopeId, String entityType, String entityId) throws CloudManagerException {
		return FacilityIdUtil.getResourceId(entityType, cloudScopeId, entityId);
	}

	public static String getAllNodeScopeId(String platformId, String cloudScopeId) {
		return String.format("_%s_ALL_%s", platformId, cloudScopeId);
	}
	
	public static String getResourceId(String cloudScopeId, IResourceManagement.Resource o1) {
		return FacilityIdUtil.getResourceId(o1.getResourceType().name(), cloudScopeId, o1.getResourceId());
	}
}
