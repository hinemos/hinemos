/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * クラウドログ監視の最終ログ取得日時を管理するクラス
 */
@Entity
@Table(name = "cc_cfg_xcloud_log_monitor_run_status", schema = "setting")
public class CloudLogMonitorRunStatusEntity {
	
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
