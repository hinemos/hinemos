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
import com.clustercontrol.xcloud.bean.Network;

public interface INetworks extends IResources {

	List<Network> getAllNetwork() throws CloudManagerException;

}
