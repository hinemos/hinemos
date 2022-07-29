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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name="cc_monitor_http_scenario_page_info", schema="setting")
@Cacheable(true)
public class Page implements Serializable {
	private static final long serialVersionUID = 1L;

	private PagePK id;
	private String url;
	private String description;
	private String statusCode;
	private String post;
	private Integer priority;
	private String message;
	
	private HttpScenarioCheckInfo monitorHttpScenarioInfoEntity;

	private List<Pattern> patterns = new ArrayList<>();

	private List<Variable> variables = new ArrayList<>();

	public Page() {
	}

	public Page(PagePK id) {
		this.setId(id);
	}

	public Page(String monitorId, Integer orderNo) {
		this(new PagePK(monitorId, orderNo));
	}

	@EmbeddedId
	public PagePK getId() {
		if (id == null)
			id = new PagePK();
		return this.id;
	}

	public void setId(PagePK id) {
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

	@Column(name="url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}


	@Column(name="description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Column(name="status_code")
	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}


	@Column(name="post")
	public String getPost() {
		return post;
	}

	public void setPost(String post) {
		this.post = post;
	}


	@Column(name="priority")
	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}


	@Column(name="message")
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	//bi-directional many-to-one association to MonitorHttpScenarioInfoEntity
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="monitor_id", insertable=false, updatable=false)
	public HttpScenarioCheckInfo getMonitorHttpScenarioInfoEntity() {
		return this.monitorHttpScenarioInfoEntity;
	}
	
	@Deprecated
	public void setMonitorHttpScenarioInfoEntity(HttpScenarioCheckInfo monitorHttpScenarioInfoEntity) {
		this.monitorHttpScenarioInfoEntity = monitorHttpScenarioInfoEntity;
	}


	public void relateToMonitorHttpScenarioInfoEntity(HttpScenarioCheckInfo httpScenarioCheckInfo) {
		this.setMonitorHttpScenarioInfoEntity(httpScenarioCheckInfo);
		if (httpScenarioCheckInfo != null) {
			List<Page> list = httpScenarioCheckInfo.getPages();
			if (list == null) {
				list = new ArrayList<Page>();
			} else {
				for (Page entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			httpScenarioCheckInfo.setPages(list);
		}
	}

	@OneToMany(mappedBy="monitorHttpScenarioPageInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public List<Pattern> getPatterns() {
		return this.patterns;
	}

	public void setPatterns(List<Pattern> patterns) {
		if (patterns != null && patterns.size() > 0) {
			Collections.sort(patterns, new Comparator<Pattern>() {
				@Override
				public int compare(Pattern o1, Pattern o2) {
					return o1.getId().getPatternOrderNo().compareTo(o2.getId().getPatternOrderNo());
				}
			});
		}
		this.patterns = patterns;
	}


	@OneToMany(mappedBy="monitorHttpScenarioPageInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public List<Variable> getVariables() {
		return this.variables;
	}

	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}
}
