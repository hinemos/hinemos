/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
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
@XmlType(namespace = "http://collect.ws.clustercontrol.com")
@Entity
@Table(name="cc_collect_key", schema="log")
@Cacheable(true)
public class CollectKeyInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private CollectKeyInfoPK id;
	private Integer collectorid;
	
	public CollectKeyInfo() {
	}

	public CollectKeyInfo(CollectKeyInfoPK pk) {
		this.setId(pk);
	}
	
	public CollectKeyInfo(String item_code, String display_name, String monitor_id, String facilityid) {
		this(new CollectKeyInfoPK(item_code, display_name, monitor_id, facilityid));
	}
	
	public CollectKeyInfo(CollectKeyInfoPK pk,Integer collectorid){
		this.setId(pk);
		this.setCollectorid(collectorid);
	}
	
	public CollectKeyInfo(String item_code, String display_name, String monitor_id, String facilityid, Integer collectorid){
		this(item_code, display_name, monitor_id, facilityid);
		this.setCollectorid(collectorid);
		
	}
	
	@XmlTransient
	@EmbeddedId
	public CollectKeyInfoPK getId() {
		if (id == null)
			id = new CollectKeyInfoPK();
		return this.id;
	}
	public void setId(CollectKeyInfoPK pk) {
		this.id = pk;
	}
	
	@Transient
	public String getItemName() {
		return getId().getItemName();
	}
	public void setItemName(String item_name) {
		getId().setItemName(item_name);
	}

	@Transient
	public String getDisplayName(){
		return getId().getDisplayName();
	}
	public void setDisplayName(String display_name){
		getId().setDisplayName(display_name);
	}

	@Transient
	public String getMonitorId() {
		return getId().getMonitorId();
	}
	public void setMonitorId(String monitor_id) {
		getId().setMonitorId(monitor_id);
	}

	@Transient
	public String getFacilityid() {
		return getId().getFacilityid();
	}
	public void setFacilityid(String facilityid) {
		getId().setFacilityid(facilityid);
	}
	
	@Column(name="collector_id")
	public Integer getCollectorid() {
		return this.collectorid;
	}
	public void setCollectorid(Integer collectorid) {
		this.collectorid = collectorid;
	}
}