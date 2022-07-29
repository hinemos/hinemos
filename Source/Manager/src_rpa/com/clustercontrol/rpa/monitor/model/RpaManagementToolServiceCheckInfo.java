/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.monitor.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlTransient;

import com.clustercontrol.monitor.run.model.MonitorCheckInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/**
 * RPA管理ツールサービス監視情報のEntityクラス
 */
@Entity
@Table(name="cc_monitor_rpa_tool_management_service_info", schema="setting")
@Cacheable(true)
public class RpaManagementToolServiceCheckInfo extends MonitorCheckInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * コネクションタイムアウト
	 */
	private Integer connectTimeout;
	/**
	 * リクエストタイムアウト
	 */
	private Integer requestTimeout;
	
	/**
	 * 監視設定情報
	 */
	private MonitorInfo monitorInfo;

	/**
	 * コネクションタイムアウト
	 */
	@Column(name="connect_timeout")
	public Integer getConnectTimeout() {
		return this.connectTimeout;
	}

	public void setConnectTimeout(Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	
	/**
	 * リクエストタイムアウト
	 */
	@Column(name="request_timeout")
	public Integer getRequestTimeout() {
		return requestTimeout;
	}

	public void setRequestTimeout(Integer requestTimeout) {
		this.requestTimeout = requestTimeout;
	}
	
	//bi-directional one-to-one association to MonitorInfo
	/**
	 * 監視設定情報
	 */
	@XmlTransient
	@OneToOne(fetch=FetchType.LAZY, optional=false)
	@PrimaryKeyJoinColumn
	public MonitorInfo getMonitorInfo() {
		return this.monitorInfo;
	}

	@Deprecated
	public void setMonitorInfo(MonitorInfo monitorInfo) {
		this.monitorInfo = monitorInfo;
	}
}
