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
import com.clustercontrol.xcloud.bean.PlatformUser;
import com.clustercontrol.xcloud.model.CloudScopeEntity;


public interface IUserManagement {
	void setScope(CloudScopeEntity scope);
	
	List<PlatformUser> getAvailableUsers() throws CloudManagerException;

	void validCredentialAsUser(Credential credential) throws CloudManagerException;
}
