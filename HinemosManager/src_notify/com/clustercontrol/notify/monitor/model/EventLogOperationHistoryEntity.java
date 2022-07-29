/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.monitor.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * The persistent class for the cc_event_log_operation_history database table.
 *
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
@Entity
@Table(name="cc_event_log_operation_history", schema="log")
public class EventLogOperationHistoryEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	private EventLogOperationHistoryPK id;
	private String monitorId;
	private String monitorDetailId;
	private String pluginId;
	private Long outputDate;
	private String facilityId;
	private Long operationDate;
	private String operationUser;
	private Integer historyType;
	private String detail;
	
	public EventLogOperationHistoryEntity() {
	}

	public EventLogOperationHistoryEntity(EventLogOperationHistoryPK pk) {
		this.setId(pk);
	}

	@XmlTransient
	@EmbeddedId
	public EventLogOperationHistoryPK getId() {
		return this.id;
	}

	public void setId(EventLogOperationHistoryPK id) {
		this.id = id;
	}

	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	@Column(name="monitor_detail_id")
	public String getMonitorDetailId() {
		return this.monitorDetailId;
	}
	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	@Column(name="plugin_id")
	public String getPluginId() {
		return this.pluginId;
	}
	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	@Column(name="output_date")
	public Long getOutputDate() {
		return this.outputDate;
	}
	public void setOutputDate(Long outputDate) {
		this.outputDate = outputDate;
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	
	@Column(name="operation_date")
	public Long getOperationDate() {
		return this.operationDate;
	}
	public void setOperationDate(Long operationDate) {
		this.operationDate = operationDate;
	}
	
	@Column(name="operation_user")
	public String getOperationUser() {
		return this.operationUser;
	}
	public void setOperationUser(String operationUser) {
		this.operationUser = operationUser;
	}
	
	@Column(name="history_type")
	public Integer getHistoryType() {
		return this.historyType;
	}
	public void setHistoryType(Integer historyType) {
		this.historyType = historyType;
	}
	
	@Column(name="detail")
	public String getDetail() {
		return this.detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 1;
		hash = hash * prime + ((id == null) ? 0 : id.hashCode());
		hash = hash * prime + ((monitorId == null) ? 0 : monitorId.hashCode());
		hash = hash * prime + ((monitorDetailId == null) ? 0 : monitorDetailId.hashCode());
		hash = hash * prime + ((pluginId == null) ? 0 : pluginId.hashCode());
		hash = hash * prime + ((outputDate == null) ? 0 : outputDate.hashCode());
		hash = hash * prime + ((facilityId == null) ? 0 : facilityId.hashCode());
		hash = hash * prime + ((operationDate == null) ? 0 : operationDate.hashCode());
		hash = hash * prime + ((operationUser == null) ? 0 : operationUser.hashCode());
		hash = hash * prime + ((historyType == null) ? 0 : historyType.hashCode());
		hash = hash * prime + ((detail == null) ? 0 : detail.hashCode());
		
		return hash;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) 
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventLogOperationHistoryEntity other = (EventLogOperationHistoryEntity) obj;
		
		if (!isEquals(id, other.id)) {return false;}
		if (!isEquals(monitorId, other.monitorId)) {return false;}
		if (!isEquals(monitorDetailId, other.monitorDetailId)) {return false;}
		if (!isEquals(pluginId, other.pluginId)) {return false;}
		if (!isEquals(outputDate, other.outputDate)) {return false;}
		if (!isEquals(facilityId, other.facilityId)) {return false;}
		if (!isEquals(operationDate, other.operationDate)) {return false;}
		if (!isEquals(operationUser, other.operationUser)) {return false;}
		if (!isEquals(historyType, other.historyType)) {return false;}
		if (!isEquals(detail, other.detail)) {return false;}
	
		return true;
	}

	private static boolean isEquals(Object o1, Object o2) {
		if (o1 == null) {
			if (o2 != null) {
				return false;
			}
			return true;
		} else {
			return !o1.equals(o2);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String[] props = {
			"id",
			"monitorId",
			"monitorDetailId",
			"pluginId",
			"outputDate",
			"facilityId",
			"operationDate",
			"operationUser",
			"historyType",
			"detail",
		};
		String[] values = {
			((id == null) ? "" : id.toString()),
			((monitorId == null) ? "" : monitorId.toString()),
			((monitorDetailId == null) ? "" : monitorDetailId.toString()),
			((pluginId == null) ? "" : pluginId.toString()),
			((outputDate == null) ? "" : outputDate.toString()),
			((facilityId == null) ? "" : facilityId.toString()),
			((operationDate == null) ? "" : operationDate.toString()),
			((operationUser == null) ? "" : operationUser.toString()),
			((historyType == null) ? "" : historyType.toString()),
			((detail == null) ? "" : detail.toString()),
		};
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < props.length;i++) {
			if (i != 0) {sb.append(" ,");}
			sb.append(props[i]).append("=").append(values[i]);
		}
		
		return String.format("EventLogOperationHistory [ %s ]", sb.toString());
	}

	

}