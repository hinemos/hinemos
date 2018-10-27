/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.http.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name="cc_monitor_http_scenario_variable_info", schema="setting")
//@Cacheable(true)
public class Variable implements Serializable {
	private static final long serialVersionUID = 1L;

	private VariablePK id;
	private String value;
	private Boolean matchingWithResponseFlg;
	
	private Page monitorHttpScenarioPageInfoEntity;

	public Variable() {
	}

	public Variable(VariablePK pk) {
		this.setId(pk);
	}

	public Variable(String monitorId, Integer orderNo, String name) {
		this(new VariablePK(monitorId, orderNo, name));
	}

	@XmlTransient
	@EmbeddedId
	public VariablePK getId() {
		if (this.id == null)
			this.id = new VariablePK();
		return this.id;
	}

	public void setId(VariablePK id) {
		this.id = id;
	}
	
	@XmlTransient
	@Transient
	public String getMonitorId() {
		return getId().getMonitorId();
	}

	public void setMonitorId(String monitorId) {
		getId().setMonitorId(monitorId);
	}


	@XmlTransient
	@Transient
	public Integer getPageOrderNo() {
		return getId().getPageOrderNo();
	}

	public void setPageOrderNo(Integer pageOrderNo) {
		getId().setPageOrderNo(pageOrderNo);
	}
	
	@Transient
	public String getName() {
		return getId().getName();
	}
	public void setName(String priority) {
		getId().setName(priority);
	}

	@Column(name="value")
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}


	@Column(name="matching_with_response_flg")
	public Boolean getMatchingWithResponseFlg() {
		return matchingWithResponseFlg;
	}

	public void setMatchingWithResponseFlg(Boolean matchingWithResponseFlg) {
		this.matchingWithResponseFlg = matchingWithResponseFlg;
	}
	
	//bi-directional many-to-one association to MonitorHttpScenarioPageInfoEntity
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="monitor_id", referencedColumnName="monitor_id", insertable=false, updatable=false),
		@JoinColumn(name="page_order_no", referencedColumnName="page_order_no", insertable=false, updatable=false)
	})
	public Page getMonitorHttpScenarioPageInfoEntity() {
		return this.monitorHttpScenarioPageInfoEntity;
	}
	
	@Deprecated
	public void setMonitorHttpScenarioPageInfoEntity(Page monitorHttpScenarioPageInfoEntity) {
		this.monitorHttpScenarioPageInfoEntity = monitorHttpScenarioPageInfoEntity;
	}
	
	public void relateToMonitorHttpScenarioPageInfoEntity(Page monitorHttpScenarioPageInfoEntity) {
		this.setMonitorHttpScenarioPageInfoEntity(monitorHttpScenarioPageInfoEntity);
		if (monitorHttpScenarioPageInfoEntity != null) {
			List<Variable> list = monitorHttpScenarioPageInfoEntity.getVariables();
			if (list == null) {
				list = new ArrayList<Variable>();
			} else {
				for (Variable entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorHttpScenarioPageInfoEntity.setVariables(list);
		}
	}
}
