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
import com.clustercontrol.xcloud.model.base.ValueObserver;
import com.clustercontrol.xcloud.model.repository.IScope;

public interface ILocation extends IElement {
	// プロパティの Id
	public interface p {
		static final PropertyId<ValueObserver<String>> name = new PropertyId<ValueObserver<String>>("name"){};
		
		static final PropertyId<ValueObserver<IComputeResources>> computeResources = new PropertyId<ValueObserver<IComputeResources>>("computeResources", true){};
		static final PropertyId<CollectionObserver<IServiceCondition>> serviceConditions = new PropertyId<CollectionObserver<IServiceCondition>>("serviceConditions", true){};
		static final PropertyId<CollectionObserver<IEndpoint>> endpoints = new PropertyId<CollectionObserver<IEndpoint>>("endpoints", true){};
	}
	
	ICloudScope getCloudScope();
	
	String getId();
	
	String getName();
	String getEntryType();
	String getLocationType();
	
	IComputeResources getComputeResources();
	
	IScope getCounterScope();
	
	IServiceCondition[] getServiceConditions();
	IServiceCondition[] getServiceConditionsWithInitializing();

	IEndpoint[] getEndpoints();

	void updateLocation();
	void updateServiceConditions();
}
