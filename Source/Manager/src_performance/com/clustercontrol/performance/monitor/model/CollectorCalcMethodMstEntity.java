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
 * The persistent class for the cc_collector_calc_method_mst database table.
 * 
 */
@Entity
@Table(name="cc_collector_calc_method_mst", schema="setting")
@Cacheable(true)
public class CollectorCalcMethodMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String calcMethod;
	private String className;
	private String expression;
	private List<CollectorItemCalcMethodMstEntity> collectorItemCalcMethodMstEntities;

	@Deprecated
	public CollectorCalcMethodMstEntity() {
	}

	public CollectorCalcMethodMstEntity(String calcMethod) {
		this.setCalcMethod(calcMethod);
	}


	@Id
	@Column(name="calc_method")
	public String getCalcMethod() {
		return this.calcMethod;
	}

	public void setCalcMethod(String calcMethod) {
		this.calcMethod = calcMethod;
	}


	@Column(name="class_name")
	public String getClassName() {
		return this.className;
	}

	public void setClassName(String className) {
		this.className = className;
	}


	@Column(name="expression")
	public String getExpression() {
		return this.expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}


	//bi-directional many-to-one association to CollectorItemCalcMethodMstEntity
	@OneToMany(mappedBy="collectorCalcMethodMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<CollectorItemCalcMethodMstEntity> getCollectorItemCalcMethodMstEntities() {
		return this.collectorItemCalcMethodMstEntities;
	}

	public void setCollectorItemCalcMethodMstEntities(List<CollectorItemCalcMethodMstEntity> collectorItemCalcMethodMstEntities) {
		this.collectorItemCalcMethodMstEntities = collectorItemCalcMethodMstEntities;
	}

}