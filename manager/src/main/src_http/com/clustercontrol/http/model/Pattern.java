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
@Table(name="cc_monitor_http_scenario_pattern_info", schema="setting")
//@Cacheable(true)
public class Pattern implements Serializable {
	private static final long serialVersionUID = 1L;

	private PatternPK id;
	private String pattern;
	private String description;
	private Boolean processType;
	private Boolean caseSensitivityFlg;
	private Boolean validFlg;

	private Page monitorHttpScenarioPageInfoEntity;


	public Pattern() {
	}

	public Pattern(PatternPK pk) {
		this.setId(pk);
	}

	public Pattern(String monitorId, Integer orderNo, Integer patternOrderNo) {
		this(new PatternPK(monitorId, orderNo, patternOrderNo));
	}

	@XmlTransient
	@EmbeddedId
	public PatternPK getId() {
		if (id == null)
			id = new PatternPK();
		return this.id;
	}

	public void setId(PatternPK id) {
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


	@Transient
	@XmlTransient
	public Integer getPageOrderNo() {
		return getId().getPageOrderNo();
	}

	public void setPageOrderNo(Integer pageOrderNo) {
		getId().setPageOrderNo(pageOrderNo);
	}


	@Transient
	@XmlTransient
	public Integer getPatternOrderNo() {
		return getId().getPatternOrderNo();
	}

	public void setPatternOrderNo(Integer patternOrderNo) {
		getId().setPatternOrderNo(patternOrderNo);
	}

	@Column(name="pattern")
	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}


	@Column(name="description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Column(name="process_type")
	public Boolean getProcessType() {
		return processType;
	}

	public void setProcessType(Boolean processType) {
		this.processType = processType;
	}


	@Column(name="case_sensitivity_flg")
	public Boolean getCaseSensitivityFlg() {
		return caseSensitivityFlg;
	}

	public void setCaseSensitivityFlg(Boolean caseSensitivityFlg) {
		this.caseSensitivityFlg = caseSensitivityFlg;
	}


	@Column(name="valid_flg")
	public Boolean getValidFlg() {
		return validFlg;
	}

	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
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
			List<Pattern> list = monitorHttpScenarioPageInfoEntity.getPatterns();
			if (list == null) {
				list = new ArrayList<Pattern>();
			} else {
				for (Pattern entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorHttpScenarioPageInfoEntity.setPatterns(list);
		}
	}

}
