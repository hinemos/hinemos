/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.clustercontrol.performance.monitor.model.CollectorCategoryCollectMstEntity;
import com.clustercontrol.performance.monitor.model.CollectorItemCalcMethodMstEntity;
import com.clustercontrol.performance.monitor.model.CollectorPollingMstEntity;
import com.clustercontrol.process.model.MonitorProcessMethodMstEntity;
import com.clustercontrol.process.model.MonitorProcessPollingMstEntity;


/**
 * The persistent class for the cc_collector_platform_mst database table.
 * 
 */
@Entity
@Table(name="cc_collector_platform_mst", schema="setting")
@Cacheable(true)
public class CollectorPlatformMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String platformId;
	private Integer orderNo;
	private String platformName;
	private List<CollectorCategoryCollectMstEntity> collectorCategoryCollectMstEntities;
	private List<CollectorItemCalcMethodMstEntity> collectorItemCalcMethodMstEntities;
	private List<CollectorPollingMstEntity> collectorPollingMstEntities;
	private List<MonitorProcessMethodMstEntity> monitorProcessMethodMstEntities;
	private List<MonitorProcessPollingMstEntity> monitorProcessPollingMstEntities;

	@Deprecated
	public CollectorPlatformMstEntity() {
	}

	public CollectorPlatformMstEntity(String platformId) {
		this.setPlatformId(platformId);
	}


	@Id
	@Column(name="platform_id")
	public String getPlatformId() {
		return this.platformId;
	}

	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}


	@Column(name="order_no")
	public Integer getOrderNo() {
		return this.orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}


	@Column(name="platform_name")
	public String getPlatformName() {
		return this.platformName;
	}

	public void setPlatformName(String platformName) {
		this.platformName = platformName;
	}


	//bi-directional many-to-one association to CollectorCategoryCollectMstEntity
	@OneToMany(mappedBy="collectorPlatformMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<CollectorCategoryCollectMstEntity> getCollectorCategoryCollectMstEntities() {
		return this.collectorCategoryCollectMstEntities;
	}

	public void setCollectorCategoryCollectMstEntities(List<CollectorCategoryCollectMstEntity> collectorCategoryCollectMstEntities) {
		this.collectorCategoryCollectMstEntities = collectorCategoryCollectMstEntities;
	}


	//bi-directional many-to-one association to CollectorItemCalcMethodMstEntity
	@OneToMany(mappedBy="collectorPlatformMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<CollectorItemCalcMethodMstEntity> getCollectorItemCalcMethodMstEntities() {
		return this.collectorItemCalcMethodMstEntities;
	}

	public void setCollectorItemCalcMethodMstEntities(List<CollectorItemCalcMethodMstEntity> collectorItemCalcMethodMstEntities) {
		this.collectorItemCalcMethodMstEntities = collectorItemCalcMethodMstEntities;
	}


	//bi-directional many-to-one association to CollectorPollingMstEntity
	@OneToMany(mappedBy="collectorPlatformMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<CollectorPollingMstEntity> getCollectorPollingMstEntities() {
		return this.collectorPollingMstEntities;
	}

	public void setCollectorPollingMstEntities(List<CollectorPollingMstEntity> collectorPollingMstEntities) {
		this.collectorPollingMstEntities = collectorPollingMstEntities;
	}


	//bi-directional many-to-one association to MonitorProcessMethodMstEntity
	@OneToMany(mappedBy="collectorPlatformMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorProcessMethodMstEntity> getMonitorProcessMethodMstEntities() {
		return this.monitorProcessMethodMstEntities;
	}

	public void setMonitorProcessMethodMstEntities(List<MonitorProcessMethodMstEntity> monitorProcessMethodMstEntities) {
		this.monitorProcessMethodMstEntities = monitorProcessMethodMstEntities;
	}


	//bi-directional many-to-one association to MonitorProcessPollingMstEntity
	@OneToMany(mappedBy="collectorPlatformMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorProcessPollingMstEntity> getMonitorProcessPollingMstEntities() {
		return this.monitorProcessPollingMstEntities;
	}

	public void setMonitorProcessPollingMstEntities(List<MonitorProcessPollingMstEntity> monitorProcessPollingMstEntities) {
		this.monitorProcessPollingMstEntities = monitorProcessPollingMstEntities;
	}

}