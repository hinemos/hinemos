/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.util.List;

import com.clustercontrol.xcloud.model.base.PropertyId;
import com.clustercontrol.xcloud.model.base.ValueObserver;

public interface INetwork extends IResource {
	public interface p {
		static final PropertyId<ValueObserver<String>> name = new PropertyId<ValueObserver<String>>("name"){};
		static final PropertyId<ValueObserver<String>> networkType = new PropertyId<ValueObserver<String>>("networkType"){};
	}

	String getId();
	String getName();
	String getNetworkType();
	List<String> getAttachedInstances();
	
	IComputeResources getCloudCompute();
}
