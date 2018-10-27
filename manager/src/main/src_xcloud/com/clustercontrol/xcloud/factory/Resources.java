/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import org.apache.log4j.Logger;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.persistence.Transactional;

public abstract class Resources implements IResources {
	private CloudLoginUserEntity user;
	private LocationEntity location;

	private Logger logger;
	
	@Override
	@Transactional(Transactional.TransactionOption.Supported)
	public void setAccessInformation(CloudLoginUserEntity user, LocationEntity location) throws CloudManagerException {
		this.user = user;
		this.location = location;
	}
	
	protected CloudScopeEntity getCloudScope()  {
		return getUser().getCloudScope();
	}
	
	protected CloudLoginUserEntity getUser()  {
		return user;
	}

	protected LocationEntity getLocation() {
		return location;
	}

	protected Logger getLogger() {
		if (logger == null) {
			logger = Logger.getLogger(this.getClass());
		}
		return logger;
	}
}
