/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.clustercontrol.xcloud.factory.IResourceManagement.Storage.StorageStatus;

/**
 * インスタンス情報のBMP Entity Bean クラス<BR>
 */
@NamedQueries({
	@NamedQuery(
			name=StorageEntity.findStoragesByLocation,
			query="SELECT s FROM StorageEntity s WHERE s.cloudScopeId = :cloudScopeId AND s.locationId = :locationId"
			),
	@NamedQuery(
			name=StorageEntity.findStorages,
			query="SELECT s FROM StorageEntity s WHERE s.cloudScopeId = :cloudScopeId AND s.locationId = :locationId AND s.resourceId IN :storageIds"
			)
})
@Entity
@Table(name="cc_cfg_xcloud_storage", schema="setting")
@DiscriminatorValue(StorageEntity.typeName)
public class StorageEntity extends LocationResourceEntity {
	public static final String typeName = "storage";
	
	public static final String findStoragesByLocation = "findStoragesByLocation";
	public static final String findStorages = "findStorages";
	
	private String name;
	private Integer size;
	private String storageType;
	
	private String targetInstanceId;
	private String facilityId;
	private Integer deviceIndex;
	private String deviceType;
	private String deviceName;
	
	private StorageStatus storageStatus;
	private String storageStatusAsPlatform;
	private String resourceTypeAsPlatform;

	private StorageBackupEntity backup;

	public StorageEntity() {
	}

	@Column(name="storage_name")
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Column(name="size")
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}

	@Column(name="storage_type")
	public String getStorageType() {
		return this.storageType;
	}
	public void setStorageType(String storageType) {
		this.storageType = storageType;
	}
	
	@Column(name="target_facility_id", unique=true)
	public String getTargetFacilityId() {
		return this.facilityId;
	}
	public void setTargetFacilityId( String facilityId ) {
		this.facilityId = facilityId;
	}

	@Column(name="device_index")
	public java.lang.Integer getDeviceIndex() {
		return this.deviceIndex;
	}
	public void setDeviceIndex( java.lang.Integer deviceIndex ) {
		this.deviceIndex = deviceIndex;
	}

	@Column(name="device_type")
	public String getDeviceType() {
		return this.deviceType;
	}
	public void setDeviceType( String deviceType ) {
		this.deviceType = deviceType;
	}
	
	@Column(name="device_name")
	public String getDeviceName() {
		return this.deviceName;
	}
	public void setDeviceName( String deviceName ) {
		this.deviceName = deviceName;
	}
	
	@Column(name="storage_status")
	@Enumerated(EnumType.STRING)
	public StorageStatus getStorageStatus() {
		return this.storageStatus;
	}
	public void setStorageStatus(StorageStatus storageStatus) {
		this.storageStatus = storageStatus;
	}
	
	@Column(name="storage_status_as_platform")
	public String getStorageStatusAsPlatform() {
		return storageStatusAsPlatform;
	}
	public void setStorageStatusAsPlatform(String storageStatusAsPlatform) {
		this.storageStatusAsPlatform = storageStatusAsPlatform;
	}

	@Column(name="resource_type_as_platform")
	public String getResourceTypeAsPlatform() {
		return resourceTypeAsPlatform;
	}
	public void setResourceTypeAsPlatform(String resourceTypeAsPlatform) {
		this.resourceTypeAsPlatform = resourceTypeAsPlatform;
	}
	
	@OneToOne(fetch=FetchType.EAGER)
	@JoinColumns({
		@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id", insertable=false, updatable=false),
		@JoinColumn(name="location_id", referencedColumnName="location_id", insertable=false, updatable=false),
		@JoinColumn(name="resource_id", referencedColumnName="storage_id", insertable=false, updatable=false)
	})
	public StorageBackupEntity getBackup() {
		return this.backup;
	}
	public void setBackup(StorageBackupEntity backup) {
		this.backup = backup;
	}

	@Column(name="target_instance_id")
	public String getTargetInstanceId() {
		return targetInstanceId;
	}
	public void setTargetInstanceId(String targetInstanceId) {
		this.targetInstanceId = targetInstanceId;
	}
}
