/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.dto.RequestDto;

@RestBeanConvertIdClassSet(infoClass = com.clustercontrol.repository.model.NodeDeviceInfo.class, idName = "id")
public class NodeGeneralDeviceInfoRequest implements RequestDto {

	private Integer deviceIndex;
	private String deviceType;
	private String deviceName;

	private String deviceDisplayName;
	private Long deviceSize;
	private String deviceSizeUnit;
	private String deviceDescription;

	public NodeGeneralDeviceInfoRequest() {
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

	public String getDeviceDisplayName() {
		return deviceDisplayName;
	}

	public void setDeviceDisplayName(String deviceDisplayName) {
		this.deviceDisplayName = deviceDisplayName;
	}

	public Long getDeviceSize() {
		return deviceSize;
	}

	public void setDeviceSize(Long deviceSize) {
		this.deviceSize = deviceSize;
	}

	public String getDeviceSizeUnit() {
		return deviceSizeUnit;
	}

	public void setDeviceSizeUnit(String deviceSizeUnit) {
		this.deviceSizeUnit = deviceSizeUnit;
	}

	public String getDeviceDescription() {
		return deviceDescription;
	}

	public void setDeviceDescription(String deviceDescription) {
		this.deviceDescription = deviceDescription;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
