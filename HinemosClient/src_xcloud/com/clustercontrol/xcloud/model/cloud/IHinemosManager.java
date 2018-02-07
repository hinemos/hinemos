/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import com.clustercontrol.xcloud.model.base.ElementBaseModeWatch;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.base.PropertyId;
import com.clustercontrol.xcloud.model.base.ValueObserver;
import com.clustercontrol.xcloud.model.repository.ICloudRepository;

public interface IHinemosManager extends IElement {
	// プロパティの Id
	public interface p {
		static final PropertyId<ValueObserver<String>> managerName = new PropertyId<ValueObserver<String>>("managerName"){};
		static final PropertyId<ValueObserver<String>> accountName = new PropertyId<ValueObserver<String>>("accountName"){};
		static final PropertyId<ValueObserver<ICloudScopes>> cloudScopes = new PropertyId<ValueObserver<ICloudScopes>>("cloudScopes", true){};
		static final PropertyId<ValueObserver<ICloudRepository>> cloudRepository = new PropertyId<ValueObserver<ICloudRepository>>("cloudRepository", true){};
		static final PropertyId<ValueObserver<IBillingMonitors>> billingAlarms = new PropertyId<ValueObserver<IBillingMonitors>>("billingAlarms", true){};
	}

	String getUrl();
	String getManagerName();
	String getAccountName();
	
	ICloudPlatform[] getCloudPlatforms();
	ICloudPlatform getCloudPlatform(String cloudPlatformId);
	
	ICloudScopes getCloudScopes();
	
	ICloudRepository getCloudRepository();
	
	IBillingMonitors getBillingAlarms();

	<T> T getEndpoint(Class<T> endpointClass);
	
	ElementBaseModeWatch getModelWatch();
	
	void update();
	void updateLocation(ILocation location);
	
	boolean isInitialized();
}
