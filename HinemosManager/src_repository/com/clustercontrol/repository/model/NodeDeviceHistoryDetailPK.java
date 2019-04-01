/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;

import javax.persistence.*;

import com.clustercontrol.util.HinemosTime;

/**
 * The primary key class for the cc_node_XXX_history_detail database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@Embeddable
public class NodeDeviceHistoryDetailPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String facilityId = "";
	private Integer deviceIndex = -1;
	private String deviceType = "";
	private String deviceName = "";
	private Long regDate = HinemosTime.currentTimeMillis();

	public NodeDeviceHistoryDetailPK() {
	}

	public NodeDeviceHistoryDetailPK(String facilityId,
			Integer deviceIndex,
			String deviceType,
			String deviceName,
			Long regDate) {
		this.setFacilityId(facilityId);
		this.setDeviceIndex(deviceIndex);
		this.setDeviceType(deviceType);
		this.setDeviceName(deviceName);
		this.setRegDate(regDate);
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="device_index")
	public Integer getDeviceIndex() {
		return this.deviceIndex;
	}
	public void setDeviceIndex(Integer deviceIndex) {
		this.deviceIndex = deviceIndex;
	}

	@Column(name="device_type")
	public String getDeviceType() {
		return this.deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	@Column(name="device_name")
	public String getDeviceName() {
		return this.deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	@Column(name="reg_date")
	public Long getRegDate() {
		return this.regDate;
	}
	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}
}