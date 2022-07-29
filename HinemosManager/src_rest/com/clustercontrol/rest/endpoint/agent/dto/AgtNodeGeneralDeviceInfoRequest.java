/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.repository.model.NodeDeviceInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertAssertion(to = NodeDeviceInfo.class)
@RestBeanConvertIdClassSet(infoClass = NodeDeviceInfo.class, idName = "id")
public class AgtNodeGeneralDeviceInfoRequest extends AgentRequestDto {

	// ---- from NodeDeviceInfoPK
	// private String facilityId;
	private Integer deviceIndex;
	private String deviceType;
	private String deviceName;

	// ---- from NodeDeviceInfo
	//private NodeDeviceInfoPK id;
	private String deviceDisplayName;
	private Long deviceSize;
	private String deviceSizeUnit;
	private String deviceDescription;

	public AgtNodeGeneralDeviceInfoRequest() {
	}

	// ---- accessors

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

}
