/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.monitor.model;

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


/**
 * The persistent class for the cc_collector_category_mst database table.
 * 
 */
@Entity
@Table(name="cc_collector_category_mst", schema="setting")
@Cacheable(true)
public class CollectorCategoryMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String categoryCode;
	private String categoryName;
	private List<CollectorCategoryCollectMstEntity> collectorCategoryCollectMstEntities;
	private List<CollectorItemCodeMstEntity> collectorItemCodeMstEntities;

	@Deprecated
	public CollectorCategoryMstEntity() {
	}

	public CollectorCategoryMstEntity(String categoryCode) {
		this.setCategoryCode(categoryCode);
	}


	@Id
	@Column(name="category_code")
	public String getCategoryCode() {
		return this.categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}


	@Column(name="category_name")
	public String getCategoryName() {
		return this.categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}


	//bi-directional many-to-one association to CollectorCategoryCollectMstEntity
	@OneToMany(mappedBy="collectorCategoryMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<CollectorCategoryCollectMstEntity> getCollectorCategoryCollectMstEntities() {
		return this.collectorCategoryCollectMstEntities;
	}

	public void setCollectorCategoryCollectMstEntities(List<CollectorCategoryCollectMstEntity> collectorCategoryCollectMstEntities) {
		this.collectorCategoryCollectMstEntities = collectorCategoryCollectMstEntities;
	}


	//bi-directional many-to-one association to CollectorItemCodeMstEntity
	@OneToMany(mappedBy="collectorCategoryMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<CollectorItemCodeMstEntity> getCollectorItemCodeMstEntities() {
		return this.collectorItemCodeMstEntities;
	}

	public void setCollectorItemCodeMstEntities(List<CollectorItemCodeMstEntity> collectorItemCodeMstEntities) {
		this.collectorItemCodeMstEntities = collectorItemCodeMstEntities;
	}

}