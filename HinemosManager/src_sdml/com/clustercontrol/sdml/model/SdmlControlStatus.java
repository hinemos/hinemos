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
 * The persistent class for the cc_sdml_control_status database table.
 * 
 */
@Entity
@Table(name="cc_sdml_control_status", schema="setting")
@Cacheable(false)
public class SdmlControlStatus implements Serializable {
	private static final long serialVersionUID = 1L;

	private SdmlControlStatusPK id;
	private Integer status;
	private Long lastUpdateDate;
	private String lastControlCode;
	private Long applicationStartupDate;
	private Integer internalCheckInterval;

	@Deprecated
	public SdmlControlStatus() {
	}

	public SdmlControlStatus(SdmlControlStatusPK pk) {
		this.setId(pk);
	}

	public SdmlControlStatus(String applicationId, String facilityId) {
		this(new SdmlControlStatusPK(applicationId, facilityId));
	}

	@EmbeddedId
	public SdmlControlStatusPK getId() {
		return this.id;
	}
	public void setId(SdmlControlStatusPK id) {
		this.id = id;
	}

	@Column(name="status")
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name="last_update_date")
	public Long getLastUpdateDate() {
		return lastUpdateDate;
	}
	public void setLastUpdateDate(Long lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	@Column(name="last_control_code")
	public String getLastControlCode() {
		return lastControlCode;
	}
	public void setLastControlCode(String lastControlCode) {
		this.lastControlCode = lastControlCode;
	}

	@Column(name="application_startup_date")
	public Long getApplicationStartupDate() {
		return applicationStartupDate;
	}
	public void setApplicationStartupDate(Long applicationStartupDate) {
		this.applicationStartupDate = applicationStartupDate;
	}

	@Column(name="internal_check_interval")
	public Integer getInternalCheckInterval() {
		return internalCheckInterval;
	}
	public void setInternalCheckInterval(Integer internalCheckInterval) {
		this.internalCheckInterval = internalCheckInterval;
	}

	@Transient
	public String getApplicationId() {
		return this.id.getApplicationId();
	}

	@Transient
	public String getFacilityId() {
		return this.id.getFacilityId();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SdmlControlStatus [");
		sb.append("id = " + id.toString());
		sb.append(", status = " + status.toString());
		sb.append(", lastUpdateDate = " + lastUpdateDate.toString());
		sb.append(", lastControlCode = " + lastControlCode);
		sb.append(", applicationStartupDate = " + applicationStartupDate.toString());
		sb.append(", internalCheckInterval = " + internalCheckInterval.toString());
		sb.append("]");
		return sb.toString();
	}
}
