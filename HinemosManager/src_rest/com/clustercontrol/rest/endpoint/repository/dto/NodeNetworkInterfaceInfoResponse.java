/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.repository.model.NodeDeviceInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;

@RestBeanConvertIdClassSet(infoClass = NodeDeviceInfo.class, idName = "id")
public class NodeNetworkInterfaceInfoResponse {

	private String nicIpAddress;
	private String nicMacAddress;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String regDate;
	private String regUser;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String updateDate;
	private String updateUser;
	private Boolean searchTarget;

	private Integer deviceIndex;
	private String deviceType;
	private String deviceName;

	private String deviceDisplayName;
	private Long deviceSize;
	private String deviceSizeUnit;
	private String deviceDescription;

	public NodeNetworkInterfaceInfoResponse() {
	}

	public String getNicIpAddress() {
		return nicIpAddress;
	}

	public void setNicIpAddress(String nicIpAddress) {
		this.nicIpAddress = nicIpAddress;
	}

	public String getNicMacAddress() {
		return nicMacAddress;
	}

	public void setNicMacAddress(String nicMacAddress) {
		this.nicMacAddress = nicMacAddress;
	}

	public String getRegDate() {
		return regDate;
	}

	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Boolean getSearchTarget() {
		return searchTarget;
	}

	public void setSearchTarget(Boolean searchTarget) {
		this.searchTarget = searchTarget;
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
}
