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
 * The persistent class for the cc_node_os_history_detail database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@Embeddable
public class NodeOsHistoryDetailPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String facilityId = "";
	private Long regDate = HinemosTime.currentTimeMillis();

	public NodeOsHistoryDetailPK() {
	}

	public NodeOsHistoryDetailPK(String facilityId, Long regDate) {
		this.setFacilityId(facilityId);
		this.setRegDate(regDate);
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="reg_date")
	public Long getRegDate() {
		return this.regDate;
	}
	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}
}