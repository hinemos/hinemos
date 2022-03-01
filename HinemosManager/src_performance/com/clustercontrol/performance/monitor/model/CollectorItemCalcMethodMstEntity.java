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

import jakarta.persistence.Cacheable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.clustercontrol.repository.model.CollectorPlatformMstEntity;



/**
 * The persistent class for the cc_collector_item_calc_method_mst database table.
 * 
 */
@Entity
@Table(name="cc_collector_item_calc_method_mst", schema="setting")
@Cacheable(true)
public class CollectorItemCalcMethodMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private CollectorItemCalcMethodMstEntityPK id;
	private CollectorCalcMethodMstEntity collectorCalcMethodMstEntity;
	private CollectorItemCodeMstEntity collectorItemCodeMstEntity;
	private CollectorPlatformMstEntity collectorPlatformMstEntity;

	@Deprecated
	public CollectorItemCalcMethodMstEntity() {
	}

	public CollectorItemCalcMethodMstEntity(CollectorItemCalcMethodMstEntityPK pk) {
		this.setId(pk);
	}

	public CollectorItemCalcMethodMstEntity(CollectorPlatformMstEntity collectorPlatformMstEntity,
			CollectorItemCodeMstEntity collectorItemCodeMstEntity,
			String collectMethod,
			String subPlatformId) {
		this(new CollectorItemCalcMethodMstEntityPK(
				collectMethod,
				collectorPlatformMstEntity.getPlatformId(),
				subPlatformId,
				collectorItemCodeMstEntity.getItemCode()));
	}


	@EmbeddedId
	public CollectorItemCalcMethodMstEntityPK getId() {
		return this.id;
	}

	public void setId(CollectorItemCalcMethodMstEntityPK id) {
		this.id = id;
	}


	//bi-directional many-to-one association to CollectorCalcMethodMstEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="calc_method")
	public CollectorCalcMethodMstEntity getCollectorCalcMethodMstEntity() {
		return this.collectorCalcMethodMstEntity;
	}

	@Deprecated
	public void setCollectorCalcMethodMstEntity(CollectorCalcMethodMstEntity collectorCalcMethodMstEntity) {
		this.collectorCalcMethodMstEntity = collectorCalcMethodMstEntity;
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
	public void relateToCollectorCalcMethodMstEntity(CollectorCalcMethodMstEntity collectorCalcMethodMstEntity) {
		this.setCollectorCalcMethodMstEntity(collectorCalcMethodMstEntity);
		if (collectorCalcMethodMstEntity != null) {
			List<CollectorItemCalcMethodMstEntity> list = collectorCalcMethodMstEntity.getCollectorItemCalcMethodMstEntities();
			if (list == null) {
				list = new ArrayList<CollectorItemCalcMethodMstEntity>();
			} else {
				for(CollectorItemCalcMethodMstEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			collectorCalcMethodMstEntity.setCollectorItemCalcMethodMstEntities(list);
		}
	}


	//bi-directional many-to-one association to CollectorItemCodeMstEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="item_code", insertable=false, updatable=false)
	public CollectorItemCodeMstEntity getCollectorItemCodeMstEntity() {
		return this.collectorItemCodeMstEntity;
	}

	@Deprecated
	public void setCollectorItemCodeMstEntity(CollectorItemCodeMstEntity collectorItemCodeMstEntity) {
		this.collectorItemCodeMstEntity = collectorItemCodeMstEntity;
	}

	/**
	 * CollectorItemCodeMstEntityオブジェクト参照設定<BR>
	 * 
	 * CollectorItemCodeMstEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToCollectorItemCodeMstEntity(CollectorItemCodeMstEntity collectorItemCodeMstEntity) {
		this.setCollectorItemCodeMstEntity(collectorItemCodeMstEntity);
		if (collectorItemCodeMstEntity != null) {
			List<CollectorItemCalcMethodMstEntity> list = collectorItemCodeMstEntity.getCollectorItemCalcMethodMstEntities();
			if (list == null) {
				list = new ArrayList<CollectorItemCalcMethodMstEntity>();
			} else {
				for(CollectorItemCalcMethodMstEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			collectorItemCodeMstEntity.setCollectorItemCalcMethodMstEntities(list);
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
			List<CollectorItemCalcMethodMstEntity> list = collectorPlatformMstEntity.getCollectorItemCalcMethodMstEntities();
			if (list == null) {
				list = new ArrayList<CollectorItemCalcMethodMstEntity>();
			} else {
				for(CollectorItemCalcMethodMstEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			collectorPlatformMstEntity.setCollectorItemCalcMethodMstEntities(list);
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

		// CollectorCalcMethodMstEntity
		if (this.collectorCalcMethodMstEntity != null) {
			List<CollectorItemCalcMethodMstEntity> list = this.collectorCalcMethodMstEntity.getCollectorItemCalcMethodMstEntities();
			if (list != null) {
				Iterator<CollectorItemCalcMethodMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					CollectorItemCalcMethodMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}

		// CollectorItemCodeMstEntity
		if (this.collectorItemCodeMstEntity != null) {
			List<CollectorItemCalcMethodMstEntity> list = this.collectorItemCodeMstEntity.getCollectorItemCalcMethodMstEntities();
			if (list != null) {
				Iterator<CollectorItemCalcMethodMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					CollectorItemCalcMethodMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}

		// CollectorPlatformMstEntity
		if (this.collectorPlatformMstEntity != null) {
			List<CollectorItemCalcMethodMstEntity> list = this.collectorPlatformMstEntity.getCollectorItemCalcMethodMstEntities();
			if (list != null) {
				Iterator<CollectorItemCalcMethodMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					CollectorItemCalcMethodMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}