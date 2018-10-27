/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.util.List;

import com.clustercontrol.ws.xcloud.PlatformUser;
import com.clustercontrol.xcloud.model.base.CollectionObserver;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.base.PropertyId;
import com.clustercontrol.xcloud.model.base.ValueObserver;
import com.clustercontrol.xcloud.model.repository.ICloudScopeScope;

public interface ICloudScope extends IElement {
	// プロパティの Id
	public interface p {
		static final PropertyId<ValueObserver<String>> name = new PropertyId<ValueObserver<String>>("name"){};
		static final PropertyId<ValueObserver<String>> description = new PropertyId<ValueObserver<String>>("description"){};
		static final PropertyId<ValueObserver<Boolean>> openness = new PropertyId<ValueObserver<Boolean>>("openness"){};
		static final PropertyId<ValueObserver<Long>> updateDate = new PropertyId<ValueObserver<Long>>("updateDate"){};
		static final PropertyId<ValueObserver<String>> updateUser = new PropertyId<ValueObserver<String>>("updateUser"){};
		
		static final PropertyId<CollectionObserver<ILocation>> locations = new PropertyId<CollectionObserver<ILocation>>("locations", true){};
		static final PropertyId<ValueObserver<ILoginUsers>> loginUsers = new PropertyId<ValueObserver<ILoginUsers>>("loginUsers", true){};
		
		static final PropertyId<CollectionObserver<IServiceCondition>> serviceConditions = new PropertyId<CollectionObserver<IServiceCondition>>("serviceConditions", true){};

		static final PropertyId<ValueObserver<Integer>> retentionPeriod = new PropertyId<ValueObserver<Integer>>("retentionPeriod"){};
		static final PropertyId<ValueObserver<Boolean>> billingDetailCollectorFlg = new PropertyId<ValueObserver<Boolean>>("billingDetailCollectorFlg"){};

		static final PropertyId<CollectionObserver<IExtendedProperty>> extendedProperties = new PropertyId<CollectionObserver<IExtendedProperty>>("extendedProperties", true){};
	}

	String getId();
	String getName();
	String getPlatformId();
	String getDescription();

	String getAccountId();

	Long getRegDate();
	String getRegUser();
	
	Long getUpdateDate();
	String getUpdateUser();
	
	boolean isPublic();
	
	ILoginUsers getLoginUsers();

	String getOwnerRoleId();

	ILocation[] getLocations();
	ILocation getLocation(String locationId);
	
	ICloudScopeScope getCounterScope();
	
	ICloudScopes getCloudScopes();

	ICloudPlatform getCloudPlatform();
	
	String getNodeId();
	
	List<PlatformUser> getUnassignedUsers();
	
	Integer getRetentionPeriod();
	Boolean getBillingDetailCollectorFlg();
	
	IServiceCondition[] getServiceConditions();
	IServiceCondition[] getServiceConditionsWithInitializing();

	IExtendedProperty[] getExtendedProperties();
	String getExtendedProperty(String name);
	
	void updateServiceConditions();
}
