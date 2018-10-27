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
import com.clustercontrol.xcloud.model.CloudPlatformEntity;

public interface IPlatforms {
	CloudPlatformEntity getCloudPlatform(String cloudplatformId) throws CloudManagerException;
	List<CloudPlatformEntity> getAllCloudPlatforms() throws CloudManagerException;
}
