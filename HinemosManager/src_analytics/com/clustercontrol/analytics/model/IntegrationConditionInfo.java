/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.model;

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
 * 収集値統合監視の条件の設定Bean
 * The persistent class for the cc_integration_condition_info database table.
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name="cc_monitor_integration_condition_info", schema="setting")
@Cacheable(true)
public class IntegrationConditionInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private IntegrationConditionInfoPK id;
	private String description;
	private Boolean monitorNode = true;
	private String targetFacilityId;
	private String targetMonitorId;
	private Integer targetMonitorType;		// ダイアログ表示用
	private String targetItemName;
	private String targetDisplayName;
	private String targetItemDisplayName;	// ダイアログ表示用
	private String comparisonMethod;
	private String comparisonValue;
	private Boolean isAnd = true;
	private IntegrationCheckInfo monitorIntegrationInfo; 

	public IntegrationConditionInfo() {
	}
	public IntegrationConditionInfo(IntegrationConditionInfoPK pk) {
		this.setId(pk);
	}
	public IntegrationConditionInfo(String monitorId, Integer orderNo) {
		this(new IntegrationConditionInfoPK(monitorId, orderNo));
	}

	@XmlTransient
	@EmbeddedId
	public IntegrationConditionInfoPK getId() {
		if (this.id == null)
			this.id = new IntegrationConditionInfoPK();
		return this.id;
	}
	public void setId(IntegrationConditionInfoPK id) {
		this.id = id;
	}

	@Transient
	public String getMonitorId() {
		return getId().getMonitorId();
	}
	public void setMonitorId(String monitorId) {
		getId().setMonitorId(monitorId);
	}

	@XmlTransient
	@Transient
	public Integer getOrderNo() {
		return getId().getOrderNo();
	}
	public void setOrderNo(Integer orderNo) {
		getId().setOrderNo(orderNo);
	}

	@Transient
	public Integer getTargetMonitorType() {
		return this.targetMonitorType;
	}
	public void setTargetMonitorType(Integer targetMonitorType) {
		this.targetMonitorType = targetMonitorType;
	}

	@Transient
	public String getTargetItemDisplayName() {
		return this.targetItemDisplayName;
	}
	public void setTargetItemDisplayName(String targetItemDisplayName) {
		this.targetItemDisplayName = targetItemDisplayName;
	}

	@Column(name="description")
	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="monitor_node")
	public Boolean getMonitorNode() {
		return this.monitorNode;
	}
	public void setMonitorNode(Boolean monitorNode) {
		this.monitorNode = monitorNode;
	}

	@Column(name="target_facility_id")
	public String getTargetFacilityId() {
		return this.targetFacilityId;
	}
	public void setTargetFacilityId(String targetFacilityId) {
		this.targetFacilityId = targetFacilityId;
	}

	@Column(name="target_monitor_id")
	public String getTargetMonitorId() {
		return this.targetMonitorId;
	}
	public void setTargetMonitorId(String targetMonitorId) {
		this.targetMonitorId = targetMonitorId;
	}

	@Column(name="target_item_name")
	public String getTargetItemName() {
		return this.targetItemName;
	}

	public void setTargetItemName(String targetItemName) {
		this.targetItemName = targetItemName;
	}

	@Column(name="target_display_name")
	public String getTargetDisplayName() {
		return this.targetDisplayName;
	}

	public void setTargetDisplayName(String targetDisplayName) {
		this.targetDisplayName = targetDisplayName;
	}

	@Column(name="comparison_method")
	public String getComparisonMethod() {
		return this.comparisonMethod;
	}
	public void setComparisonMethod(String comparisonMethod) {
		this.comparisonMethod = comparisonMethod;
	}

	@Column(name="comparison_value")
	public String getComparisonValue() {
		return this.comparisonValue;
	}
	public void setComparisonValue(String comparisonValue) {
		this.comparisonValue = comparisonValue;
	}

	@Column(name="is_and")
	public Boolean getIsAnd() {
		return this.isAnd;
	}

	public void setIsAnd(Boolean isAnd) {
		this.isAnd = isAnd;
	}

	//bi-directional many-to-one association to IntegrationCheckInfo
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="monitor_id", insertable=false, updatable=false)
	public IntegrationCheckInfo getMonitorIntegrationInfo() {
		return this.monitorIntegrationInfo;
	}
	public void setMonitorIntegrationInfo(IntegrationCheckInfo monitorIntegrationInfo) {
		this.monitorIntegrationInfo = monitorIntegrationInfo;
	}

	/**
	 * IntegrationCheckInfoオブジェクト参照設定<BR>
	 * 
	 * IntegrationCheckInfo設定時はSetterに代わりこちらを使用すること。
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
	public void relateToMonitorIntegrationInfo(IntegrationCheckInfo monitorIntegrationInfo) {
		this.setMonitorIntegrationInfo(monitorIntegrationInfo);
		if (monitorIntegrationInfo != null) {
			List<IntegrationConditionInfo> list = monitorIntegrationInfo.getConditionList();
			if (list == null) {
				list = new ArrayList<>();
			} else {
				for (IntegrationConditionInfo entity :  list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorIntegrationInfo.setConditionList(list);
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
		// MonitorIntegrationInfo
		if (this.monitorIntegrationInfo != null) {
			List<IntegrationConditionInfo> list = this.monitorIntegrationInfo.getConditionList();
			if (list != null) {
				Iterator<IntegrationConditionInfo> iter = list.iterator();
				while(iter.hasNext()) {
					IntegrationConditionInfo entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
}