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
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.clustercontrol.repository.model.CollectorPlatformMstEntity;



/**
 * The persistent class for the cc_collector_polling_mst database table.
 * 
 */
@Entity
@Table(name="cc_collector_polling_mst", schema="setting")
@Cacheable(true)
public class CollectorPollingMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private CollectorPollingMstEntityPK id;
	private String entryKey;
	private String failureValue;
	private String pollingTarget;
	private CollectorItemCodeMstEntity collectorItemCodeMstEntity;
	private CollectorPlatformMstEntity collectorPlatformMstEntity;
	private SnmpValueTypeMstEntity snmpValueTypeMstEntity;

	@Deprecated
	public CollectorPollingMstEntity() {
	}

	public CollectorPollingMstEntity(CollectorPollingMstEntityPK pk) {
		this.setId(pk);
	}

	public CollectorPollingMstEntity(CollectorPlatformMstEntity collectorPlatformMstEntity,
			CollectorItemCodeMstEntity collectorItemCodeMstEntity,
			String collectMethod,
			String subPlatformId,
			String variableId) {
		this(new CollectorPollingMstEntityPK(
				collectMethod,
				collectorPlatformMstEntity.getPlatformId(),
				subPlatformId,
				collectorItemCodeMstEntity.getItemCode(),
				variableId));
	}


	@EmbeddedId
	public CollectorPollingMstEntityPK getId() {
		return this.id;
	}

	public void setId(CollectorPollingMstEntityPK id) {
		this.id = id;
	}


	@Column(name="entry_key")
	public String getEntryKey() {
		return this.entryKey;
	}

	public void setEntryKey(String entryKey) {
		this.entryKey = entryKey;
	}


	@Column(name="failure_value")
	public String getFailureValue() {
		return this.failureValue;
	}

	public void setFailureValue(String failureValue) {
		this.failureValue = failureValue;
	}


	@Column(name="polling_target")
	public String getPollingTarget() {
		return this.pollingTarget;
	}

	public void setPollingTarget(String pollingTarget) {
		this.pollingTarget = pollingTarget;
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
			List<CollectorPollingMstEntity> list = collectorItemCodeMstEntity.getCollectorPollingMstEntities();
			if (list == null) {
				list = new ArrayList<CollectorPollingMstEntity>();
			} else {
				for(CollectorPollingMstEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			collectorItemCodeMstEntity.setCollectorPollingMstEntities(list);
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
	public void relateToCollectorPlatformMstEntity(CollectorPlatformMstEntity collectorPlatformMstEntity) {
		this.setCollectorPlatformMstEntity(collectorPlatformMstEntity);
		if (collectorPlatformMstEntity != null) {
			List<CollectorPollingMstEntity> list = collectorPlatformMstEntity.getCollectorPollingMstEntities();
			if (list == null) {
				list = new ArrayList<CollectorPollingMstEntity>();
			} else {
				for(CollectorPollingMstEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			collectorPlatformMstEntity.setCollectorPollingMstEntities(list);
		}
	}


	//bi-directional many-to-one association to SnmpValueTypeMstEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="value_type")
	public SnmpValueTypeMstEntity getSnmpValueTypeMstEntity() {
		return this.snmpValueTypeMstEntity;
	}

	@Deprecated
	public void setSnmpValueTypeMstEntity(SnmpValueTypeMstEntity snmpValueTypeMstEntity) {
		this.snmpValueTypeMstEntity = snmpValueTypeMstEntity;
	}

	/**
	 * SnmpValueTypeMstEntityオブジェクト参照設定<BR>
	 * 
	 * SnmpValueTypeMstEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToSnmpValueTypeMstEntity(SnmpValueTypeMstEntity snmpValueTypeMstEntity) {
		this.setSnmpValueTypeMstEntity(snmpValueTypeMstEntity);
		if (snmpValueTypeMstEntity != null) {
			List<CollectorPollingMstEntity> list = snmpValueTypeMstEntity.getCollectorPollingMstEntities();
			if (list == null) {
				list = new ArrayList<CollectorPollingMstEntity>();
			} else {
				for(CollectorPollingMstEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			snmpValueTypeMstEntity.setCollectorPollingMstEntities(list);
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

		// CollectorItemCodeMstEntity
		if (this.collectorItemCodeMstEntity != null) {
			List<CollectorPollingMstEntity> list = this.collectorItemCodeMstEntity.getCollectorPollingMstEntities();
			if (list != null) {
				Iterator<CollectorPollingMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					CollectorPollingMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}

		// CollectorPlatformMstEntity
		if (this.collectorPlatformMstEntity != null) {
			List<CollectorPollingMstEntity> list = this.collectorPlatformMstEntity.getCollectorPollingMstEntities();
			if (list != null) {
				Iterator<CollectorPollingMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					CollectorPollingMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}

		// SnmpValueTypeMstEntity
		if (this.snmpValueTypeMstEntity != null) {
			List<CollectorPollingMstEntity> list = this.snmpValueTypeMstEntity.getCollectorPollingMstEntities();
			if (list != null) {
				Iterator<CollectorPollingMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					CollectorPollingMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}

	}

}