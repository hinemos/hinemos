/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.base.IUpdatable;


public interface ICloudPlatformManager extends IElement, IUpdatable {
	IHinemosManager getHinemosManager();

	ICloudPlatform[] getCloudPlatforms();
	ICloudPlatform getCloudPlatform(String cloudServiceId);
}
