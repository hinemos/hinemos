/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * The persistent class for the cc_sdml_control_monitor_relation database table.
 * 
 */
@Entity
@Table(name = "cc_sdml_control_monitor_relation", schema = "setting")
@Cacheable(true)
public class SdmlControlMonitorRelation implements Serializable {
	private static final long serialVersionUID = 1L;

	private SdmlControlMonitorRelationPK id;
	private String sdmlMonitorTypeId;
	private String subType;
	private Boolean monitorFlg;
	private Boolean collectorFlg;

	@Deprecated
	public SdmlControlMonitorRelation() {
	}

	public SdmlControlMonitorRelation(SdmlControlMonitorRelationPK pk) {
		this.setId(pk);
	}

	public SdmlControlMonitorRelation(String applicationId, String facilityId, String monitorId) {
		this(new SdmlControlMonitorRelationPK(applicationId, facilityId, monitorId));
	}

	@EmbeddedId
	public SdmlControlMonitorRelationPK getId() {
		return this.id;
	}

	public void setId(SdmlControlMonitorRelationPK id) {
		this.id = id;
	}

	@Column(name = "sdml_monitor_type_id")
	public String getSdmlMonitorTypeId() {
		return sdmlMonitorTypeId;
	}

	public void setSdmlMonitorTypeId(String sdmlMonitorTypeId) {
		this.sdmlMonitorTypeId = sdmlMonitorTypeId;
	}

	@Column(name = "sub_type")
	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	@Column(name = "monitor_flg")
	public Boolean getMonitorFlg() {
		return monitorFlg;
	}

	public void setMonitorFlg(Boolean monitorFlg) {
		this.monitorFlg = monitorFlg;
	}

	@Column(name = "collector_flg")
	public Boolean getCollectorFlg() {
		return collectorFlg;
	}

	public void setCollectorFlg(Boolean collectorFlg) {
		this.collectorFlg = collectorFlg;
	}

	@Transient
	public String getApplicationId() {
		return this.id.getApplicationId();
	}

	@Transient
	public String getFacilityId() {
		return this.id.getFacilityId();
	}

	@Transient
	public String getMonitorId() {
		return this.id.getMonitorId();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SdmlControlMonitorRelation [");
		sb.append("id = " + id.toString());
		sb.append(", sdmlMonitorTypeId = " + sdmlMonitorTypeId);
		sb.append(", subType = " + subType);
		sb.append("]");
		return sb.toString();
	}
}
