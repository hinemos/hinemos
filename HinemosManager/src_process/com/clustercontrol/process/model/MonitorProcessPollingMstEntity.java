/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.process.model;

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
import com.clustercontrol.repository.model.CollectorPlatformMstEntity;



/**
 * The persistent class for the cc_monitor_process_polling_mst database table.
 * 
 */
@Entity
@Table(name="cc_monitor_process_polling_mst", schema="setting")
@Cacheable(true)
public class MonitorProcessPollingMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private MonitorProcessPollingMstEntityPK id;
	private String entryKey;
	private String pollingTarget;
	private CollectorPlatformMstEntity collectorPlatformMstEntity;

	@Deprecated
	public MonitorProcessPollingMstEntity() {
	}

	@EmbeddedId
	public MonitorProcessPollingMstEntityPK getId() {
		return this.id;
	}

	public void setId(MonitorProcessPollingMstEntityPK id) {
		this.id = id;
	}


	@Column(name="entry_key")
	public String getEntryKey() {
		return this.entryKey;
	}

	public void setEntryKey(String entryKey) {
		this.entryKey = entryKey;
	}


	@Column(name="polling_target")
	public String getPollingTarget() {
		return this.pollingTarget;
	}

	public void setPollingTarget(String pollingTarget) {
		this.pollingTarget = pollingTarget;
	}


	//bi-directional many-to-one association to CollectorPlatformMstEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="platform_id", insertable=false, updatable=false)
	public CollectorPlatformMstEntity getCollectorPlatformMstEntity() {
		return this.collectorPlatformMstEntity;
	}

	@Deprecated
	public void setCollectorPlatformMstEntity(CollectorPlatformMstEntity collectorPlatformMstEntity) {
		this.collectorPlatformMstEntity = collectorPlatformMstEntity;
	}

	/**
	 * CollectorPlatformMstEntityオブジェクト参照設定<BR>
	 * 
	 * CollectorPlatformMstEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToCollectorPlatformMstEntity(CollectorPlatformMstEntity collectorPlatformMstEntity) {
		this.setCollectorPlatformMstEntity(collectorPlatformMstEntity);
		if (collectorPlatformMstEntity != null) {
			List<MonitorProcessPollingMstEntity> list = collectorPlatformMstEntity.getMonitorProcessPollingMstEntities();
			if (list == null) {
				list = new ArrayList<MonitorProcessPollingMstEntity>();
			} else {
				for(MonitorProcessPollingMstEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			collectorPlatformMstEntity.setMonitorProcessPollingMstEntities(list);
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

		// CollectorPlatformMstEntity
		if (this.collectorPlatformMstEntity != null) {
			List<MonitorProcessPollingMstEntity> list = this.collectorPlatformMstEntity.getMonitorProcessPollingMstEntities();
			if (list != null) {
				Iterator<MonitorProcessPollingMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					MonitorProcessPollingMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}