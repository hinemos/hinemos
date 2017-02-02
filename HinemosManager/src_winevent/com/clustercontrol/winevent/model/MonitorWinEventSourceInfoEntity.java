/*

 Copyright (C) 2013 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.winevent.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


/**
 * The persistent class for the cc_monitor_winevent_source_info database table.
 * 
 */
@Entity
@Table(name="cc_monitor_winevent_source_info", schema="setting")
@Cacheable(true)
public class MonitorWinEventSourceInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private MonitorWinEventSourceInfoEntityPK id;
	private WinEventCheckInfo monitorWinEventInfoEntity;

	@Deprecated
	public MonitorWinEventSourceInfoEntity() {
	}

	public MonitorWinEventSourceInfoEntity(MonitorWinEventSourceInfoEntityPK id) {
		this.setId(id);
	}

	@EmbeddedId
	public MonitorWinEventSourceInfoEntityPK getId() {
		return this.id;
	}

	public void setId(MonitorWinEventSourceInfoEntityPK id) {
		this.id = id;
	}

	//bi-directional many-to-one association to WinEventCheckInfo
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="monitor_id", insertable=false, updatable=false)
	public WinEventCheckInfo getMonitorWinEventInfoEntity() {
		return this.monitorWinEventInfoEntity;
	}

	@Deprecated
	public void setMonitorWinEventInfoEntity(WinEventCheckInfo monitorWinEventInfoEntity) {
		this.monitorWinEventInfoEntity = monitorWinEventInfoEntity;
	}

	/**
	 * WinEventCheckInfoオブジェクト参照設定<BR>
	 * 
	 * WinEventCheckInfo設定時はSetterに代わりこちらを使用すること。
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
	public void relateToMonitorWinEventInfoEntity(WinEventCheckInfo monitorWinEventInfoEntity) {
		this.setMonitorWinEventInfoEntity(monitorWinEventInfoEntity);
		if (monitorWinEventInfoEntity != null) {
			List<MonitorWinEventSourceInfoEntity> list = monitorWinEventInfoEntity.getMonitorWinEventSourceInfoEntities();
			if (list == null) {
				list = new ArrayList<MonitorWinEventSourceInfoEntity>();
			} else {
				for(MonitorWinEventSourceInfoEntity entity : list){
					if (entity.getId().getMonitorId().equals(this.getId().getMonitorId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorWinEventInfoEntity.setMonitorWinEventSourceInfoEntities(list);
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

		// MonitorWinEventInfoEntity
		if (this.monitorWinEventInfoEntity != null) {
			List<MonitorWinEventSourceInfoEntity> list = this.monitorWinEventInfoEntity.getMonitorWinEventSourceInfoEntities();
			if (list != null) {
				Iterator<MonitorWinEventSourceInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					MonitorWinEventSourceInfoEntity entity = iter.next();
					if (entity.getId().getMonitorId().equals(this.getId().getMonitorId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}