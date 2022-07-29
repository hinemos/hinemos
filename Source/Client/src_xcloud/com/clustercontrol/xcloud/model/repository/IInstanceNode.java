/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.repository;

import com.clustercontrol.xcloud.model.cloud.Instance;

public interface IInstanceNode extends INode {
	Instance getInstance();
}
