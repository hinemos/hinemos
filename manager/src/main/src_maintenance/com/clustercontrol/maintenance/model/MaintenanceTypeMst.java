/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;



/**
 * The persistent class for the cc_maintenance_type_mst database table.
 * 
 */
@XmlType(namespace = "http://maintenance.ws.clustercontrol.com")
@Entity
@Table(name="cc_maintenance_type_mst", schema="setting")
@Cacheable(true)
public class MaintenanceTypeMst implements Serializable {
	private static final long serialVersionUID = 1L;
	private String typeId;
	private String nameId;
	private Integer orderNo;
	private List<MaintenanceInfo> maintenanceInfoEntities;

	public MaintenanceTypeMst() {
	}

	@Id
	@Column(name="type_id")
	public String getType_id() {
		return this.typeId;
	}

	public void setType_id(String typeId) {
		this.typeId = typeId;
	}


	@Column(name="name_id")
	public String getName_id() {
		return this.nameId;
	}

	public void setName_id(String nameId) {
		this.nameId = nameId;
	}


	@Column(name="order_no")
	public Integer getOrder_no() {
		return this.orderNo;
	}

	public void setOrder_no(Integer orderNo) {
		this.orderNo = orderNo;
	}


	//bi-directional many-to-one association to MaintenanceInfoEntity
	@XmlTransient
	@OneToMany(mappedBy="maintenanceTypeMstEntity", fetch=FetchType.LAZY)
	public List<MaintenanceInfo> getMaintenanceInfoEntities() {
		return this.maintenanceInfoEntities;
	}

	public void setMaintenanceInfoEntities(List<MaintenanceInfo> maintenanceInfoEntities) {
		this.maintenanceInfoEntities = maintenanceInfoEntities;
	}

}