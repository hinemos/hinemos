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

public interface IStorage extends IResource {
	// プロパティの Id
	interface p {
		static final PropertyId<ValueObserver<String>> name = new PropertyId<ValueObserver<String>>("name"){};
		static final PropertyId<ValueObserver<String>> storageType = new PropertyId<ValueObserver<String>>("storageType"){};
		static final PropertyId<ValueObserver<String>> facilityId = new PropertyId<ValueObserver<String>>("facilityId"){};
		static final PropertyId<ValueObserver<Integer>> deviceIndex = new PropertyId<ValueObserver<Integer>>("deviceIndex"){};
		static final PropertyId<ValueObserver<String>> deviceName = new PropertyId<ValueObserver<String>>("deviceName"){};
		static final PropertyId<ValueObserver<String>> deviceType = new PropertyId<ValueObserver<String>>("deviceType"){};
		static final PropertyId<ValueObserver<String>> status = new PropertyId<ValueObserver<String>>("status"){};
		static final PropertyId<ValueObserver<Integer>> size = new PropertyId<ValueObserver<Integer>>("size"){};
		static final PropertyId<ValueObserver<String>> nativeStatus = new PropertyId<ValueObserver<String>>("nativeStatus"){};
		static final PropertyId<ValueObserver<String>> targetInstanceId = new PropertyId<ValueObserver<String>>("targetInstanceId"){};
		static final PropertyId<ValueObserver<Long>> updateDate = new PropertyId<ValueObserver<Long>>("updateDate"){};
		static final PropertyId<ValueObserver<String>> updateUser = new PropertyId<ValueObserver<String>>("updateUser"){};
		static final PropertyId<ValueObserver<IStorageBackup>> backup = new PropertyId<ValueObserver<IStorageBackup>>("backup", true){};
	}
	IComputeResources getCloudComputeManager();

	String getId();
	String getName();
	String getStorageType();
	String getStatus();
	String getFacilityId();
	Integer getDeviceIndex();
	String getDeviceType();
	String getDeviceName();
	String getLocationId();
	Integer getSize();
	String getNativeStatus();
	String getCloudScopeId();
	Long getRegDate();
	Long getUpdateDate();
	String getRegUser();
	String getUpdateUser();
	
	String getTargetInstanceId();
	
	IStorageBackup getBackup();
	
	ICloudScope getCloudScope();
	ILocation getLocation();

	List<String> getAttachableInstances();
}
