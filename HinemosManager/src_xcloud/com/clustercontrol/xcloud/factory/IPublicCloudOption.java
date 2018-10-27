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
import com.clustercontrol.xcloud.bean.Credential;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.PublicLocationEntity;


public interface IPublicCloudOption extends ICloudOption {
	void validCredentialAsAccount(Credential credential) throws CloudManagerException;
	PublicLocationEntity getLocation(String locationId) throws CloudManagerException;
	List<PublicLocationEntity> getLocations();
	default List<PublicLocationEntity> getLocations(CloudScopeEntity cloudScope) {
		return this.getLocations();
	}
}
