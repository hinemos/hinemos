/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.process.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.model.MonitorCheckInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;


/**
 * The persistent class for the cc_monitor_process_info database table.
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name="cc_monitor_process_info", schema="setting")
@Cacheable(true)
public class ProcessCheckInfo extends MonitorCheckInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String command;
	private String param;
	private Boolean caseSensitivityFlg;
	private MonitorInfo monitorInfo;

	public ProcessCheckInfo() {
	}

	@Column(name="command")
	public String getCommand() {
		return this.command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	@Column(name="param")
	public String getParam() {
		return this.param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	@Column(name="case_sensitivity_flg")
	public Boolean getCaseSensitivityFlg() {
		return this.caseSensitivityFlg;
	}

	public void setCaseSensitivityFlg(Boolean caseSensitivityFlg) {
		this.caseSensitivityFlg = caseSensitivityFlg;
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
			monitorInfo.setProcessCheckInfo(this);
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
			this.monitorInfo.setProcessCheckInfo(null);
		}
	}

}