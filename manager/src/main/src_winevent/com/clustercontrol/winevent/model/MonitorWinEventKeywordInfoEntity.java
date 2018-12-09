/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
 * The persistent class for the cc_monitor_winevent_keyword_info database table.
 * 
 */
@Entity
@Table(name="cc_monitor_winevent_keyword_info", schema="setting")
@Cacheable(true)
public class MonitorWinEventKeywordInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private MonitorWinEventKeywordInfoEntityPK id;
	private WinEventCheckInfo monitorWinEventInfoEntity;

	@Deprecated
	public MonitorWinEventKeywordInfoEntity() {
	}

	public MonitorWinEventKeywordInfoEntity(MonitorWinEventKeywordInfoEntityPK id) {
		this.setId(id);
	}

	@EmbeddedId
	public MonitorWinEventKeywordInfoEntityPK getId() {
		return this.id;
	}

	public void setId(MonitorWinEventKeywordInfoEntityPK id) {
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
			List<MonitorWinEventKeywordInfoEntity> list = monitorWinEventInfoEntity.getMonitorWinEventKeywordInfoEntities();
			if (list == null) {
				list = new ArrayList<MonitorWinEventKeywordInfoEntity>();
			} else {
				for(MonitorWinEventKeywordInfoEntity entity : list){
					if (entity.getId().getMonitorId().equals(this.getId().getMonitorId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorWinEventInfoEntity.setMonitorWinEventKeywordInfoEntities(list);
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
			List<MonitorWinEventKeywordInfoEntity> list = this.monitorWinEventInfoEntity.getMonitorWinEventKeywordInfoEntities();
			if (list != null) {
				Iterator<MonitorWinEventKeywordInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					MonitorWinEventKeywordInfoEntity entity = iter.next();
					if (entity.getId().getMonitorId().equals(this.getId().getMonitorId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}