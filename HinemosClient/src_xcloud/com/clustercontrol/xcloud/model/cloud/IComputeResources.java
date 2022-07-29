/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import com.clustercontrol.xcloud.model.base.CollectionObserver;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.base.PropertyId;


public interface IComputeResources extends IElement {
	// プロパティの Id
	public interface p {
		static final PropertyId<CollectionObserver<IStorage>> storages = new PropertyId<CollectionObserver<IStorage>>("storages", true){};
		static final PropertyId<CollectionObserver<IInstance>> instances = new PropertyId<CollectionObserver<IInstance>>("instances", true){};
		static final PropertyId<CollectionObserver<INetwork>> networks = new PropertyId<CollectionObserver<INetwork>>("networks", true){};
	}

	ILocation getLocation();
	
	IInstance[] getInstances();
	IInstance getInstance(String instanceId);
	
	IStorage[] getStorages();
	void updateStorages();

	INetwork[] getNetworks();
	INetwork[] getNetworksWithInitializing();
	void updateNetworks();
}
