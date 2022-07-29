/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.model.MonitorCheckInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;


/**
 * 相関係数監視の設定Bean
 * The persistent class for the cc_monitor_correlation_info database table.
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name="cc_monitor_correlation_info", schema="setting")
@Cacheable(true)
public class CorrelationCheckInfo extends MonitorCheckInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String targetMonitorId;
	private String targetItemName;
	private String targetDisplayName;
	private String referMonitorId;
	private String referItemName;
	private String referDisplayName;
	private String referFacilityId;
	private Integer analysysRange;
	private MonitorInfo monitorInfo;

	public CorrelationCheckInfo() {
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

	@Column(name="refer_monitor_id")
	public String getReferMonitorId() {
		return this.referMonitorId;
	}

	public void setReferMonitorId(String referMonitorId) {
		this.referMonitorId = referMonitorId;
	}

	@Column(name="refer_item_name")
	public String getReferItemName() {
		return this.referItemName;
	}

	public void setReferItemName(String referItemName) {
		this.referItemName = referItemName;
	}

	@Column(name="refer_display_name")
	public String getReferDisplayName() {
		return this.referDisplayName;
	}

	public void setReferDisplayName(String referDisplayName) {
		this.referDisplayName = referDisplayName;
	}

	@Column(name="refer_facility_id")
	public String getReferFacilityId() {
		return this.referFacilityId;
	}

	public void setReferFacilityId(String referFacilityId) {
		this.referFacilityId = referFacilityId;
	}

	@Column(name="analysys_range")
	public Integer getAnalysysRange() {
		return analysysRange;
	}

	public void setAnalysysRange(Integer analysysRange) {
		this.analysysRange = analysysRange;
	}

	//bi-directional one-to-one association to MonitorInfo
	@XmlTransient
	@OneToOne(fetch=FetchType.LAZY, optional=false)
	@PrimaryKeyJoinColumn(name="monitor_id")
	public MonitorInfo getMonitorInfo() {
		return this.monitorInfo;
	}

	@Deprecated
	public void setMonitorInfo(MonitorInfo monitorInfo) {
		this.monitorInfo = monitorInfo;
	}

	/**
	 * MonitorInfoオブジェクト参照設定<BR>
	 * 
	 * MonitorInfo設定時はSetterに代わりこちらを使用すること。
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
	public void relateToMonitorInfo(MonitorInfo monitorInfo) {
		this.setMonitorInfo(monitorInfo);
		if (monitorInfo != null) {
			monitorInfo.setCorrelationCheckInfo(this);
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
		// MonitorInfo
		if (this.monitorInfo != null) {
			this.monitorInfo.setCorrelationCheckInfo(null);
		}
	}
}