/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.plugin.model;

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
import javax.xml.bind.annotation.XmlType;

/**
 * The persistent class for the cc_monitor_plugin_numeric_info database table.
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name="cc_monitor_plugin_numeric_info", schema="setting")
@Cacheable(true)
public class MonitorPluginNumericInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private MonitorPluginNumericInfoEntityPK id;
	private Double value;
	private PluginCheckInfo monitorPluginInfoEntity;

	public MonitorPluginNumericInfo() {
	}

	public MonitorPluginNumericInfo(MonitorPluginNumericInfoEntityPK id) {
		this.setId(id);
	}

	public MonitorPluginNumericInfo(String monitorId, String key) {
		this(new MonitorPluginNumericInfoEntityPK(monitorId, key));
	}
	
	@XmlTransient
	@EmbeddedId
	public MonitorPluginNumericInfoEntityPK getId() {
		if (id == null)
			id = new MonitorPluginNumericInfoEntityPK();
		return id;
	}

	public void setId(MonitorPluginNumericInfoEntityPK id) {
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
	public String getKey() {
		return getId().getKey();
	}

	public void setKey(String key) {
		getId().setKey(key);
	}
	
	@Column(name="property_value")
	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	//bi-directional many-to-one association to CalInfoEntity
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="monitor_id", insertable=false, updatable=false)
	public PluginCheckInfo getMonitorPluginInfoEntity() {
		return monitorPluginInfoEntity;
	}

	@Deprecated
	public void setMonitorPluginInfoEntity(
			PluginCheckInfo monitorPluginInfoEntity) {
		this.monitorPluginInfoEntity = monitorPluginInfoEntity;
	}

	/**
	 * MonitorPluginInfoEntityオブジェクト参照設定<BR>
	 * 
	 * MonitorPluginInfoEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToMonitorPluginInfoEntity(PluginCheckInfo monitorPluginInfoEntity) {
		this.setMonitorPluginInfoEntity(monitorPluginInfoEntity);
		if (monitorPluginInfoEntity != null) {
			List<MonitorPluginNumericInfo> list = monitorPluginInfoEntity.getMonitorPluginNumericInfoList();
			if (list == null) {
				list = new ArrayList<MonitorPluginNumericInfo>();
			} else {
				for(MonitorPluginNumericInfo entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorPluginInfoEntity.setMonitorPluginNumericInfoList(list);
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

		// MonitorPluginInfoEntity
		if (this.monitorPluginInfoEntity != null) {
			List<MonitorPluginNumericInfo> list = this.monitorPluginInfoEntity.getMonitorPluginNumericInfoList();
			if (list != null) {
				Iterator<MonitorPluginNumericInfo> iter = list.iterator();
				while(iter.hasNext()) {
					MonitorPluginNumericInfo entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
}
