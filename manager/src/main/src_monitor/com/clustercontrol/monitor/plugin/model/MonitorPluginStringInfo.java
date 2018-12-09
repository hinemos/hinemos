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

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The persistent class for the cc_monitor_plugin_string_info database table.
 * 
 */
@Entity
@Table(name="cc_monitor_plugin_string_info", schema="setting")
@Cacheable(true)
public class MonitorPluginStringInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private MonitorPluginStringInfoEntityPK id;
	private String value;
	private PluginCheckInfo monitorPluginInfoEntity;

	@Deprecated
	public MonitorPluginStringInfo() {
	}

	public MonitorPluginStringInfo(MonitorPluginStringInfoEntityPK id) {
		this.setId(id);
	}

	@XmlTransient
	@EmbeddedId
	public MonitorPluginStringInfoEntityPK getId() {
		if (id == null)
			id = new MonitorPluginStringInfoEntityPK();
		return id;
	}
	public void setId(MonitorPluginStringInfoEntityPK id) {
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
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
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
			List<MonitorPluginStringInfo> list = monitorPluginInfoEntity.getMonitorPluginStringInfoList();
			if (list == null) {
				list = new ArrayList<MonitorPluginStringInfo>();
			} else {
				for(MonitorPluginStringInfo entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorPluginInfoEntity.setMonitorPluginStringInfoList(list);
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
			List<MonitorPluginStringInfo> list = this.monitorPluginInfoEntity.getMonitorPluginStringInfoList();
			if (list != null) {
				Iterator<MonitorPluginStringInfo> iter = list.iterator();
				while(iter.hasNext()) {
					MonitorPluginStringInfo entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}
