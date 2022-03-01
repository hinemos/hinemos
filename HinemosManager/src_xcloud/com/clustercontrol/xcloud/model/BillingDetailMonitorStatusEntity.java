/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/**
 * 課金"詳細"監視の確定日増分の前回監視日時を管理するクラス
 */
@NamedQueries({
	@NamedQuery(
			name = BillingDetailMonitorStatusEntity.getLastNotifiedDay,
			query = "SELECT b FROM BillingDetailMonitorStatusEntity b WHERE b.monitorId = :monitorId"
		)
	})
@Entity
@Table(name = "cc_cfg_xcloud_billing_detail_monitor_status", schema = "setting")
public class BillingDetailMonitorStatusEntity {

	public final static String getLastNotifiedDay = "getLastNotifiedDay";
	
	private String monitorId;
	private Long lastNotifiedDay;
	
	@Id
	@Column(name = "monitor_id")
	public String getMonitorId() {
		return monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}
	
	@Column(name = "last_notified_day")
	public Long getLastNotifiedDay() {
		return lastNotifiedDay;
	}
	public void setLastNotifiedDay(Long lastNotifiedDay) {
		this.lastNotifiedDay = lastNotifiedDay;
	}
}
