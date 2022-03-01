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
 * The persistent class for the cc_sdml_monitor_type_mst database table.
 * 
 */
@Entity
@Table(name="cc_sdml_monitor_type_mst", schema="setting")
@Cacheable(true)
public class SdmlMonitorTypeMasterInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private SdmlMonitorTypeMasterInfoPK id;
	private String sdmlMonitorType;
	private String pluginId;

	@Deprecated
	public SdmlMonitorTypeMasterInfo() {
	}

	public SdmlMonitorTypeMasterInfo(SdmlMonitorTypeMasterInfoPK pk) {
		this.setId(pk);
	}

	public SdmlMonitorTypeMasterInfo(String sdmlMonitorTypeId) {
		this(new SdmlMonitorTypeMasterInfoPK(sdmlMonitorTypeId));
	}

	@EmbeddedId
	public SdmlMonitorTypeMasterInfoPK getId() {
		return this.id;
	}
	public void setId(SdmlMonitorTypeMasterInfoPK id) {
		this.id = id;
	}

	@Column(name="sdml_monitor_type")	
	public String getSdmlMonitorType() {
		return sdmlMonitorType;
	}
	public void setSdmlMonitorType(String sdmlMonitorType) {
		this.sdmlMonitorType = sdmlMonitorType;
	}

	@Column(name="plugin_id")
	public String getPluginId() {
		return pluginId;
	}
	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	@Transient
	public String getSdmlMonitorTypeId() {
		return this.id.getSdmlMonitorTypeId();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SdmlMonitorTypeMasterInfo [");
		sb.append("id = " + id.toString());
		sb.append(", sdmlMonitorType = " + sdmlMonitorType);
		sb.append(", pluginId = " + pluginId);
		sb.append("]");
		return sb.toString();
	}
}
