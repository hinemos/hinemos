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

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.clustercontrol.repository.model.CollectorPlatformMstEntity;



/**
 * The persistent class for the cc_monitor_process_method_mst database table.
 * 
 */
@Entity
@Table(name="cc_monitor_process_method_mst", schema="setting")
@Cacheable(true)
public class MonitorProcessMethodMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private MonitorProcessMethodMstEntityPK id;
	private String collectMethod;
	private CollectorPlatformMstEntity collectorPlatformMstEntity;

	@Deprecated
	public MonitorProcessMethodMstEntity() {
	}

	@EmbeddedId
	public MonitorProcessMethodMstEntityPK getId() {
		return this.id;
	}

	public void setId(MonitorProcessMethodMstEntityPK id) {
		this.id = id;
	}


	@Column(name="collect_method")
	public String getCollectMethod() {
		return this.collectMethod;
	}

	public void setCollectMethod(String collectMethod) {
		this.collectMethod = collectMethod;
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
			List<MonitorProcessMethodMstEntity> list = collectorPlatformMstEntity.getMonitorProcessMethodMstEntities();
			if (list == null) {
				list = new ArrayList<MonitorProcessMethodMstEntity>();
			} else {
				for(MonitorProcessMethodMstEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			collectorPlatformMstEntity.setMonitorProcessMethodMstEntities(list);
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
			List<MonitorProcessMethodMstEntity> list = this.collectorPlatformMstEntity.getMonitorProcessMethodMstEntities();
			if (list != null) {
				Iterator<MonitorProcessMethodMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					MonitorProcessMethodMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}