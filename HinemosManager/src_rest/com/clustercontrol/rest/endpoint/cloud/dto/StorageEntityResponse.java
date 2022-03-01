/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.xcloud.factory.IResourceManagement.Storage.StorageStatus;

public class StorageEntityResponse {
	@RestBeanConvertDatetime
	private String regDate;
	@RestBeanConvertDatetime
	private String updateDate;
	private String regUser;
	private String updateUser;
	private Integer size;
	private String storageType;
	private String targetInstanceId;
	private String targetFacilityId;
	private Integer deviceIndex;
	private String deviceType;
	private String deviceName;
	private StorageStatus storageStatus;
	private String storageStatusAsPlatform;

	public StorageEntityResponse() {
	}

	public String getRegDate() {
		return regDate;
	}

	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public String getStorageType() {
		return storageType;
	}

	public void setStorageType(String storageType) {
		this.storageType = storageType;
	}

	public String getTargetInstanceId() {
		return targetInstanceId;
	}

	public void setTargetInstanceId(String targetInstanceId) {
		this.targetInstanceId = targetInstanceId;
	}

	public String getTargetFacilityId() {
		return targetFacilityId;
	}

	public void setTargetFacilityId(String targetFacilityId) {
		this.targetFacilityId = targetFacilityId;
	}

	public Integer getDeviceIndex() {
		return deviceIndex;
	}

	public void setDeviceIndex(Integer deviceIndex) {
		this.deviceIndex = deviceIndex;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public StorageStatus getStorageStatus() {
		return storageStatus;
	}

	public void setStorageStatus(StorageStatus storageStatus) {
		this.storageStatus = storageStatus;
	}

	public String getStorageStatusAsPlatform() {
		return storageStatusAsPlatform;
	}

	public void setStorageStatusAsPlatform(String storageStatusAsPlatform) {
		this.storageStatusAsPlatform = storageStatusAsPlatform;
	}
}
