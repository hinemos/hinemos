/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * The persistent class for the cc_collect_key database table.
 * 
 */
@XmlType(namespace = "http://hub.ws.clustercontrol.com")
@Entity
@Table(name="cc_collect_string_key", schema="log")
public class CollectStringKeyInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private CollectStringKeyInfoPK id;
	private Long collectId;
	
	public CollectStringKeyInfo() {
	}

	public CollectStringKeyInfo(CollectStringKeyInfoPK pk) {
		this.setId(pk);
	}
	
	public CollectStringKeyInfo(String monitor_id, String facilityid, String targetName) {
		this(new CollectStringKeyInfoPK(monitor_id, facilityid, targetName));
	}
	
	public CollectStringKeyInfo(CollectStringKeyInfoPK pk, String targetName, Long collectorid){
		this.setId(pk);
		this.setCollectId(collectorid);
	}
	
	public CollectStringKeyInfo(String monitorId, String facilityId, String targetName, Long collectid){
		this(monitorId, facilityId, targetName);
		this.setCollectId(collectid);
	}
	
	@XmlTransient
	@EmbeddedId
	public CollectStringKeyInfoPK getId() {
		if (id == null)
			id = new CollectStringKeyInfoPK();
		return this.id;
	}
	public void setId(CollectStringKeyInfoPK id) {
		this.id = id;
	}

	@Transient
	public String getMonitorId() {
		return getId().getMonitorId();
	}
	public void setMonitorId(String monitorId) {
		getId().setMonitorId(monitorId);
	}

	@Transient
	public String getFacilityId() {
		return getId().getFacilityId();
	}
	public void setFacilityId(String facilityId) {
		getId().setFacilityId(facilityId);
	}

	@Transient
	public String getTargetName() {
		return getId().getTargetName();
	}
	public void setTargetName(String targetName) {
		getId().setTargetName(targetName);
	}
	
	@Column(name="collect_id")
	public Long getCollectId() {
		return this.collectId;
	}
	public void setCollectId(Long collectId) {
		this.collectId = collectId;
	}

	@Override
	public String toString() {
		return "CollectStringKeyInfo [id=" + id + ", collectId=" + collectId + "]";
	}
}