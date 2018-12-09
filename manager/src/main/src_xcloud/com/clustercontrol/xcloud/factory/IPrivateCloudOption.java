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
import com.clustercontrol.xcloud.bean.PrivateLocation;
import com.clustercontrol.xcloud.model.PrivateLocationEntity;


public interface IPrivateCloudOption extends ICloudOption {
	LocationSpec getLocationSpec();
	void validCredentialEntityAsAccount(Credential credential, List<PrivateLocationEntity> location) throws CloudManagerException;
	void validCredentialAsAccount(Credential credential, List<PrivateLocation> location) throws CloudManagerException;
}
