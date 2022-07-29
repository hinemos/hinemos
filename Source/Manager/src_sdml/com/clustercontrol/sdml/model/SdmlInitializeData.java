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
 * The persistent class for the cc_sdml_initialize_data database table.
 * 
 */
@Entity
@Table(name="cc_sdml_initialize_data", schema="setting")
@Cacheable(true)
public class SdmlInitializeData implements Serializable {
	private static final long serialVersionUID = 1L;

	private SdmlInitializeDataPK id;
	private String value;

	@Deprecated
	public SdmlInitializeData() {
	}

	public SdmlInitializeData(SdmlInitializeDataPK pk) {
		this.setId(pk);
	}

	public SdmlInitializeData(String applicationId, String facilityId, String key) {
		this(new SdmlInitializeDataPK(applicationId, facilityId, key));
	}

	@EmbeddedId
	public SdmlInitializeDataPK getId() {
		return this.id;
	}
	public void setId(SdmlInitializeDataPK id) {
		this.id = id;
	}

	@Column(name="value")
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	@Transient
	public String getKey() {
		return this.id.getKey();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SdmlInitializeData [");
		sb.append("id = " + id.toString());
		sb.append(", value = " + value);
		sb.append("]");
		return sb.toString();
	}
}
