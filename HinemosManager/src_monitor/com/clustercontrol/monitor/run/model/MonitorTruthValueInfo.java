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


/**
 * The persistent class for the cc_monitor_truth_value_info database table.
 * 
 */
@Entity
@Table(name="cc_monitor_truth_value_info", schema="setting")
@Cacheable(true)
public class MonitorTruthValueInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private MonitorTruthValueInfoPK id;
	private String message;
	private MonitorInfo monitorInfo;

	@Deprecated
	public MonitorTruthValueInfo() {
	}

	public MonitorTruthValueInfo(MonitorTruthValueInfoPK pk) {
		this.setId(pk);
	}

	public MonitorTruthValueInfo(String monitorId, Integer priority, Integer truthValue) {
		this(new MonitorTruthValueInfoPK(monitorId, priority, truthValue));
	}

	@XmlTransient
	@EmbeddedId
	public MonitorTruthValueInfoPK getId() {
		if (this.id == null)
			this.id = new MonitorTruthValueInfoPK();
		return this.id;
	}

	public void setId(MonitorTruthValueInfoPK id) {
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
	public Integer getPriority() {
		return getId().getPriority();
	}
	public void setPriority(Integer priority) {
		getId().setPriority(priority);
	}

	@Transient
	public Integer getTruthValue() {
		return getId().getTruthValue();
	}
	public void setTruthValue(Integer truthValue) {
		getId().setTruthValue(truthValue);
	}

	@Column(name="message")
	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
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
			List<MonitorTruthValueInfo> list = monitorInfo.getTruthValueInfo();
			if (list == null) {
				list = new ArrayList<MonitorTruthValueInfo>();
			} else {
				for(MonitorTruthValueInfo entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorInfo.setTruthValueInfo(list);
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
			List<MonitorTruthValueInfo> list = this.monitorInfo.getTruthValueInfo();
			if (list != null) {
				Iterator<MonitorTruthValueInfo> iter = list.iterator();
				while(iter.hasNext()) {
					MonitorTruthValueInfo entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
}