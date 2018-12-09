/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.util.ArrayList;
import java.util.List;

public class Storage extends Resource implements IStorage {
	protected Integer deviceIndex;
	protected String deviceName;
	protected String deviceType;
	protected String facilityId;
	protected String locationId;
	protected String name;
	protected String storageType;
	protected Long regDate;
	protected String regUser;
	protected String cloudScopeId;
	protected Integer size;
	protected String id;
	protected String status;
	protected String nativeStatus;
	protected Long updateDate;
	protected String updateUser;
	protected StorageBackup backup = new StorageBackup(this);

	protected String targetInstanceId;
	protected List<String> attachableInstances = new ArrayList<>();

	public Storage() {
		this.backup = new StorageBackup(this);
	}

	@Override
	public String getId() {return id;}
	public void setId(String id) {this.id = id;}

	@Override
	public String getName() {return name;}
	public void setName(String name) {internalSetProperty(IStorage.p.name, name, ()->this.name, (s)->this.name=s);}

	@Override
	public String getStorageType() {return storageType;}
	public void setStorageType(String storageType) {internalSetProperty(IStorage.p.storageType, storageType, ()->this.storageType, (s)->this.storageType=s);}

	@Override
	public String getFacilityId() {return facilityId;}
	public void setFacilityId(String facilityId) {internalSetProperty(IStorage.p.facilityId, facilityId, ()->this.facilityId, (s)->this.facilityId=s);}

	@Override
	public String getLocationId() {return locationId;}
	public void setLocationId(String locationId) {this.locationId = locationId;}

	@Override
	public String getStatus() {return status;}
	public void setStatus(String status) {internalSetProperty(IStorage.p.status, status, ()->this.status, (s)->this.status=s);}

	@Override
	public Integer getSize() {return size;}
	public void setSize(Integer size) {internalSetProperty(IStorage.p.size, size, ()->this.size, (s)->this.size=s);}

	@Override
	public String getNativeStatus() {return nativeStatus;}
	public void setNativeStatus(String nativeStatus) {internalSetProperty(IStorage.p.nativeStatus, nativeStatus, ()->this.nativeStatus, (s)->this.nativeStatus=s);}

	@Override
	public Integer getDeviceIndex() {return deviceIndex;}
	public void setDeviceIndex(Integer deviceIndex) {internalSetProperty(IStorage.p.deviceIndex, deviceIndex, ()->this.deviceIndex, (s)->this.deviceIndex=s);}

	@Override
	public String getDeviceType() {return deviceType;}
	public void setDeviceType(String deviceType) {internalSetProperty(IStorage.p.deviceType, deviceType, ()->this.deviceType, (s)->this.deviceType=s);}

	@Override
	public String getDeviceName() {return deviceName;}
	public void setDeviceName(String deviceName) {internalSetProperty(IStorage.p.deviceName, deviceName, ()->this.deviceName, (s)->this.deviceName=s);}

	@Override
	public Long getRegDate() {return regDate;}
	public void setRegDate(Long regDate) {this.regDate = regDate;}

	@Override
	public String getRegUser() {return regUser;}
	public void setRegUser(String regUser) {this.regUser = regUser;}

	@Override
	public Long getUpdateDate() {return updateDate;}
	public void setUpdateDate(Long updateDate) {internalSetProperty(IStorage.p.updateDate, updateDate, ()->this.updateDate, (s)->this.updateDate=s);}

	@Override
	public String getUpdateUser() {return updateUser;}
	public void setUpdateUser(String updateUser) {internalSetProperty(IStorage.p.updateUser, updateUser, ()->this.updateUser, (s)->this.updateUser=s);}

	@Override
	public String getCloudScopeId() {return cloudScopeId;}
	public void setCloudScopeId(String cloudScopeId) {this.cloudScopeId = cloudScopeId;}

	@Override
	public String getTargetInstanceId() {return targetInstanceId;}
	public void setTargetInstanceId(String targetInstanceId) {internalSetProperty(IStorage.p.targetInstanceId, targetInstanceId, ()->this.targetInstanceId, (s)->this.targetInstanceId=s);}

	@Override
	public ComputeResources getCloudComputeManager() {return (ComputeResources)getOwner();}

	public boolean equalValues(com.clustercontrol.ws.xcloud.Storage source) {
		return getId().equals(source.getId());
	}

	public static Storage convert(com.clustercontrol.ws.xcloud.Storage source) {
		Storage storage = new Storage();
		storage.update(source);
		return storage;
	}

	protected void update(com.clustercontrol.ws.xcloud.Storage source) {
		setId(source.getId());
		setName(source.getName());
		setStatus(source.getStorageStatus().value());
		setFacilityId(source.getTargetFacilityId());
		setDeviceIndex(source.getDeviceIndex());
		setDeviceName(source.getDeviceName());
		setDeviceType(source.getDeviceType());
		setLocationId(source.getLocationId());
		setSize(source.getSize());
		setNativeStatus(source.getStorageStatusAsPlatform());
		setCloudScopeId(source.getCloudScopeId());
		setRegDate(source.getRegDate());
		setRegUser(source.getRegUser());
		setUpdateDate(source.getUpdateDate());
		setUpdateUser(source.getUpdateUser());
		setTargetInstanceId(source.getTargetInstanceId());
		setStorageType(source.getStorageType());
		updateExtendedProperties(source.getExtendedProperties());
	}

	@Override
	public Location getLocation() {
		if (getCloudComputeManager() != null)
			return getCloudComputeManager().getLocation();
		return null;
	}

	@Override
	public StorageBackup getBackup() {
		return backup;
	}

	@Override
	public List<String> getAttachableInstances() {
		return attachableInstances;
	}
	public void setAttachableInstances(List<String> attachableInstances) {
		this.attachableInstances = attachableInstances;
	}
}
