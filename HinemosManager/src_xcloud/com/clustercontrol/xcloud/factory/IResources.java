/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.LocationEntity;

public interface IResources {
	public void setAccessInformation(CloudLoginUserEntity user, LocationEntity location) throws CloudManagerException;
}
