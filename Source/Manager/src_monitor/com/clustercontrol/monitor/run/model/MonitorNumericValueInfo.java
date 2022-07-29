/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


/**
 * The persistent class for the cc_monitor_numeric_value_info database table.
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")

@Entity
@Table(name="cc_monitor_numeric_value_info", schema="setting")
@Cacheable(true)
public class MonitorNumericValueInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private MonitorNumericValueInfoPK id;
	private String message;
	private Double thresholdLowerLimit;
	private Double thresholdUpperLimit;
	private MonitorInfo monitorInfo;

	public MonitorNumericValueInfo() {
	}

	public MonitorNumericValueInfo(MonitorNumericValueInfoPK pk) {
		this.setId(pk);
	}

	public MonitorNumericValueInfo(MonitorInfo monitorInfo, 
			String monitorNumericType, Integer priority) {
		this(new MonitorNumericValueInfoPK(monitorInfo.getMonitorId(), 
				monitorNumericType, priority));
	}

	@XmlTransient
	@EmbeddedId
	public MonitorNumericValueInfoPK getId() {
		if (this.id == null)
			this.id = new MonitorNumericValueInfoPK();
		return this.id;
	}

	public void setId(MonitorNumericValueInfoPK id) {
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
	public String getMonitorNumericType() {
		return getId().getMonitorNumericType();
	}
	public void setMonitorNumericType(String monitorNumericType) {
		getId().setMonitorNumericType(monitorNumericType);
	}

	@Transient
	public Integer getPriority() {
		return getId().getPriority();
	}
	public void setPriority(Integer priority) {
		getId().setPriority(priority);
	}

	@Column(name="message")
	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Column(name="threshold_lower_limit")
	public Double getThresholdLowerLimit() {
		return this.thresholdLowerLimit;
	}

	public void setThresholdLowerLimit(Double thresholdLowerLimit) {
		this.thresholdLowerLimit = thresholdLowerLimit;
	}


	@Column(name="threshold_upper_limit")
	public Double getThresholdUpperLimit() {
		return this.thresholdUpperLimit;
	}

	public void setThresholdUpperLimit(Double thresholdUpperLimit) {
		this.thresholdUpperLimit = thresholdUpperLimit;
	}

	//bi-directional many-to-one association to MonitorInfo
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="monitor_id", insertable=false, updatable=false)
	public MonitorInfo getMonitorInfo() {
		return this.monitorInfo;
	}

	public void setMonitorInfo(MonitorInfo monitorInfo) {
		this.monitorInfo = monitorInfo;
	}

	@Override
	public String toString() {
		String[] names = {
				"monitorId",
				"monitorNumericType",
				"priority",
				"thresholdLowerLimit",
				"thresholdUpperLimit",
				"message"
		};
		String[] values = {
				this.id.getMonitorId(),
				this.id.getMonitorNumericType(),
				this.id.getPriority().toString(),
				this.thresholdLowerLimit.toString(),
				this.thresholdUpperLimit.toString(),
				this.message
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}

	/**
	 * MonitorInfoオブジェクト参照設定<BR>
	 * 
	 * MonitorInfo設定時はSetterに代わりこちらを使用すること。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void relateToMonitorInfo(MonitorInfo monitorInfo) {
		this.setMonitorInfo(monitorInfo);
		if (monitorInfo != null) {
			List<MonitorNumericValueInfo> list = monitorInfo.getNumericValueInfo();
			if (list == null) {
				list = new ArrayList<MonitorNumericValueInfo>();
			} else {
				for(MonitorNumericValueInfo entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorInfo.setNumericValueInfo(list);
		}
	}


	/**
	 * 削除前処理<BR>
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void unchain() {

		// MonitorInfo
		if (this.monitorInfo != null) {
			List<MonitorNumericValueInfo> list = this.monitorInfo.getNumericValueInfo();
			if (list != null) {
				Iterator<MonitorNumericValueInfo> iter = list.iterator();
				while(iter.hasNext()) {
					MonitorNumericValueInfo entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
}