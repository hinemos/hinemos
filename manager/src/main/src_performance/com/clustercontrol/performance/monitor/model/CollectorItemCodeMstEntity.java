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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;


/**
 * The persistent class for the cc_collector_item_code_mst database table.
 * 
 */
@Entity
@Table(name="cc_collector_item_code_mst", schema="setting")
@Cacheable(true)
public class CollectorItemCodeMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String itemCode;
	private Boolean deviceSupport;
	private String deviceType;
	private Boolean graphRange;
	private String itemName;
	private String measure;
	private String parentItemCode;
	private List<CollectorItemCalcMethodMstEntity> collectorItemCalcMethodMstEntities;
	private CollectorCategoryMstEntity collectorCategoryMstEntity;
	private List<CollectorPollingMstEntity> collectorPollingMstEntities;

	@Deprecated
	public CollectorItemCodeMstEntity() {
	}

	public CollectorItemCodeMstEntity(String itemCode) {
		this.setItemCode(itemCode);
	}

	@Id
	@Column(name="item_code")
	public String getItemCode() {
		return this.itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}


	@Column(name="device_support")
	public Boolean getDeviceSupport() {
		return this.deviceSupport;
	}

	public void setDeviceSupport(Boolean deviceSupport) {
		this.deviceSupport = deviceSupport;
	}


	@Column(name="device_type")
	public String getDeviceType() {
		return this.deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}


	@Column(name="graph_range")
	public Boolean getGraphRange() {
		return this.graphRange;
	}

	public void setGraphRange(Boolean graphRange) {
		this.graphRange = graphRange;
	}


	@Column(name="item_name")
	public String getItemName() {
		return this.itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}


	@Column(name="measure")
	public String getMeasure() {
		return this.measure;
	}

	public void setMeasure(String measure) {
		this.measure = measure;
	}


	@Column(name="parent_item_code")
	public String getParentItemCode() {
		return this.parentItemCode;
	}

	public void setParentItemCode(String parentItemCode) {
		this.parentItemCode = parentItemCode;
	}


	//bi-directional many-to-one association to CollectorItemCalcMethodMstEntity
	@OneToMany(mappedBy="collectorItemCodeMstEntity", fetch=FetchType.LAZY)
	public List<CollectorItemCalcMethodMstEntity> getCollectorItemCalcMethodMstEntities() {
		return this.collectorItemCalcMethodMstEntities;
	}

	public void setCollectorItemCalcMethodMstEntities(List<CollectorItemCalcMethodMstEntity> collectorItemCalcMethodMstEntities) {
		this.collectorItemCalcMethodMstEntities = collectorItemCalcMethodMstEntities;
	}


	//bi-directional many-to-one association to CollectorCategoryMstEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="category_code")
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
			List<CollectorItemCodeMstEntity> list = collectorCategoryMstEntity.getCollectorItemCodeMstEntities();
			if (list == null) {
				list = new ArrayList<CollectorItemCodeMstEntity>();
			} else {
				for(CollectorItemCodeMstEntity entity : list){
					if (entity.getItemCode().equals(this.itemCode)) {
						return;
					}
				}
			}
			list.add(this);
			collectorCategoryMstEntity.setCollectorItemCodeMstEntities(list);
		}
	}


	//bi-directional many-to-one association to CollectorPollingMstEntity
	@OneToMany(mappedBy="collectorItemCodeMstEntity", fetch=FetchType.LAZY)
	public List<CollectorPollingMstEntity> getCollectorPollingMstEntities() {
		return this.collectorPollingMstEntities;
	}

	public void setCollectorPollingMstEntities(List<CollectorPollingMstEntity> collectorPollingMstEntities) {
		this.collectorPollingMstEntities = collectorPollingMstEntities;
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

		// CollectorCategoryMstEntity
		if (this.collectorCategoryMstEntity != null) {
			List<CollectorItemCodeMstEntity> list = this.collectorCategoryMstEntity.getCollectorItemCodeMstEntities();
			if (list != null) {
				Iterator<CollectorItemCodeMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					CollectorItemCodeMstEntity entity = iter.next();
					if (entity.getItemCode().equals(this.getItemCode())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}