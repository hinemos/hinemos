/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.monitor.model;

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
 * The persistent class for the cc_collector_category_collect_mst database table.
 * 
 */
@Entity
@Table(name="cc_collector_category_collect_mst", schema="setting")
@Cacheable(true)
public class CollectorCategoryCollectMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private CollectorCategoryCollectMstEntityPK id;
	private String collectMethod;
	private CollectorCategoryMstEntity collectorCategoryMstEntity;
	private CollectorPlatformMstEntity collectorPlatformMstEntity;

	@Deprecated
	public CollectorCategoryCollectMstEntity() {
	}

	public CollectorCategoryCollectMstEntity(CollectorCategoryCollectMstEntityPK pk) {
		this.setId(pk);
	}

	public CollectorCategoryCollectMstEntity(CollectorPlatformMstEntity collectorPlatformMstEntity,
			CollectorCategoryMstEntity collectorCategoryMstEntity,
			String subPlatformId) {
		this(new CollectorCategoryCollectMstEntityPK(
				collectorPlatformMstEntity.getPlatformId(),
				subPlatformId,
				collectorCategoryMstEntity.getCategoryCode()));
	}


	@EmbeddedId
	public CollectorCategoryCollectMstEntityPK getId() {
		return this.id;
	}

	public void setId(CollectorCategoryCollectMstEntityPK id) {
		this.id = id;
	}


	@Column(name="collect_method")
	public String getCollectMethod() {
		return this.collectMethod;
	}

	public void setCollectMethod(String collectMethod) {
		this.collectMethod = collectMethod;
	}


	//bi-directional many-to-one association to CollectorCategoryMstEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="category_code", insertable=false, updatable=false)
	public CollectorCategoryMstEntity getCollectorCategoryMstEntity() {
		return this.collectorCategoryMstEntity;
	}

	@Deprecated
	public void setCollectorCategoryMstEntity(CollectorCategoryMstEntity collectorCategoryMstEntity) {
		this.collectorCategoryMstEntity = collectorCategoryMstEntity;
	}

	/**
	 * CollectorCategoryMstEntityオブジェクト参照設定<BR>
	 * 
	 * CollectorCategoryMstEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToCollectorCategoryMstEntity(CollectorCategoryMstEntity collectorCategoryMstEntity) {
		this.setCollectorCategoryMstEntity(collectorCategoryMstEntity);
		if (collectorCategoryMstEntity != null) {
			List<CollectorCategoryCollectMstEntity> list = collectorCategoryMstEntity.getCollectorCategoryCollectMstEntities();
			if (list == null) {
				list = new ArrayList<CollectorCategoryCollectMstEntity>();
			} else {
				for(CollectorCategoryCollectMstEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			collectorCategoryMstEntity.setCollectorCategoryCollectMstEntities(list);
		}
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
			List<CollectorCategoryCollectMstEntity> list = collectorPlatformMstEntity.getCollectorCategoryCollectMstEntities();
			if (list == null) {
				list = new ArrayList<CollectorCategoryCollectMstEntity>();
			} else {
				for(CollectorCategoryCollectMstEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			collectorPlatformMstEntity.setCollectorCategoryCollectMstEntities(list);
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
		if (this.collectorCategoryMstEntity != null) {
			List<CollectorCategoryCollectMstEntity> list = this.collectorCategoryMstEntity.getCollectorCategoryCollectMstEntities();
			if (list != null) {
				Iterator<CollectorCategoryCollectMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					CollectorCategoryCollectMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}

		// CollectorCategoryMstEntity
		if (this.collectorPlatformMstEntity != null) {
			List<CollectorCategoryCollectMstEntity> list = this.collectorPlatformMstEntity.getCollectorCategoryCollectMstEntities();
			if (list != null) {
				Iterator<CollectorCategoryCollectMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					CollectorCategoryCollectMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}