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
 * The primary key class for the cc_node_os_history_detail database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@Embeddable
public class NodeVariableHistoryDetailPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String facilityId = "";
	private String nodeVariableName = "";
	private Long regDate = HinemosTime.currentTimeMillis();

	public NodeVariableHistoryDetailPK() {
	}

	public NodeVariableHistoryDetailPK(String facilityId, String nodeVariableName, Long regDate) {
		this.setFacilityId(facilityId);
		this.setNodeVariableName(nodeVariableName);
		this.setRegDate(regDate);
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="node_variable_name")
	public String getNodeVariableName() {
		return this.nodeVariableName;
	}
	public void setNodeVariableName(String nodeVariableName) {
		this.nodeVariableName = nodeVariableName;
	}

	@Column(name="reg_date")
	public Long getRegDate() {
		return this.regDate;
	}
	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}
}