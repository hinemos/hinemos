/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.factory.IResourceManagement.Storage.StorageStatus;
import com.clustercontrol.xcloud.model.StorageEntity;

public class Storage extends LocationResource {
	private StorageEntity entity;

	public Storage() {
	}

	public Storage(StorageEntity entity) {
		setCloudScopeId(entity.getCloudScopeId());
		setLocationId(entity.getLocationId());
		setId(entity.getResourceId());
		setName(entity.getName());
		setResourceType(ResourceType.Storage);
		setResourceTypeAsPlatform(entity.getResourceTypeAsPlatform());
		
		List<ExtendedProperty> properties = new ArrayList<>();
		for (com.clustercontrol.xcloud.model.ExtendedProperty peoperty: entity.getExtendedProperties().values()) {
			properties.add(ExtendedProperty.convertWebEntity(peoperty));
		}
		setExtendedProperties(properties);
		this.entity = entity;
	}
	
	public String getStorageType() {
		return entity.getStorageType();
	}
	public void setStorageType(String storageType) {
		throw new UnsupportedOperationException();
	}

	public String getTargetFacilityId() {
		return entity.getTargetFacilityId();
	}
	public void setTargetFacilityId(String facilityId) {
		throw new UnsupportedOperationException();
	}

	public Integer getDeviceIndex(){
		return entity.getDeviceIndex();
	}
	public void setDeviceIndex(Integer deviceIndex){
		throw new UnsupportedOperationException();
	}
	
	public String getDeviceType(){
		return entity.getDeviceType();
	}
	public void setDeviceType(String deviceType){
		throw new UnsupportedOperationException();
	}

	public String getDeviceName(){
		return entity.getDeviceName();
	}
	public void setDeviceName(String deviceName){
		throw new UnsupportedOperationException();
	}

	public StorageStatus getStorageStatus() {
		return entity.getStorageStatus();
	}
	public void setStorageStatus(StorageStatus registState) {
		throw new UnsupportedOperationException();
	}

	public String getStorageStatusAsPlatform() {
		return entity.getStorageStatusAsPlatform();
	}
	public void setStorageStatusAsPlatform(String status) {
		throw new UnsupportedOperationException();
	}
	
	public String getTargetInstanceId() {
		return entity.getTargetInstanceId();
	}
	public void setTargetInstanceId(String targetInstanceId) {
		throw new UnsupportedOperationException();
	}
	
	public Long getRegDate() {
		return entity.getRegDate();
	}
	public void setRegDate(Long regDate) {
		throw new UnsupportedOperationException();
	}

	public Long getUpdateDate() {
		return entity.getUpdateDate();
	}
	public void setUpdateDate(Long updateDate) {
		throw new UnsupportedOperationException();
	}

	public String getRegUser() {
		return entity.getRegUser();
	}
	public void setRegUser(String regUser) {
		throw new UnsupportedOperationException();
	}

	public String getUpdateUser() {
		return entity.getUpdateUser();
	}
	public void setUpdateUser(String updateUser) {
		throw new UnsupportedOperationException();
	}

	public Integer getSize() {
		return entity.getSize();
	}
	public void setSize(Integer size) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void visit(IVisitor visitor) throws CloudManagerException {
		visitor.visit(this);
	}

	@Override
	public <T> T transform(ITransformer<T> transformer) throws CloudManagerException {
		return transformer.transform(this);
	}
	
	public static Storage convertWebEntity(StorageEntity entity) {
		return new Storage(entity);
	}

	public static List<Storage> convertWebEntities(List<StorageEntity> entities) {
		List<Storage> storages = new ArrayList<>();
		for (StorageEntity entity: entities) {
			storages.add(convertWebEntity(entity));
		}
		return storages;
	}
}
