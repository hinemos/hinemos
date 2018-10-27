/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import com.clustercontrol.xcloud.model.CredentialBaseEntity;

public interface ICloudSpec {
	boolean isPublicCloud();
	Class<? extends CredentialBaseEntity> getSupportedCredential();
	boolean isInstanceMemoEnabled();
	boolean isCloudServiceMonitorEnabled();
	boolean isBillingAlarmEnabled();
}
