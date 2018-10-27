/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.sql.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.commons.util.CryptUtil;
import com.clustercontrol.monitor.run.model.MonitorCheckInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;


/**
 * The persistent class for the cc_monitor_sql_info database table.
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name="cc_monitor_sql_info", schema="setting")
@Cacheable(true)
public class SqlCheckInfo extends MonitorCheckInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String connectionPassword;
	private String connectionUrl;
	private String connectionUser;
	private String jdbcDriver;
	private String query;
	private MonitorInfo monitorInfo;

	public SqlCheckInfo() {
	}

	@Transient
	public String getPassword() {
		return CryptUtil.decrypt(getPasswordCrypt());
	}

	public void setPassword(String connectionPassword) {
		setPasswordCrypt(CryptUtil.encrypt(connectionPassword));
	}

	@Column(name="connection_password")
	public String getPasswordCrypt() {
		return this.connectionPassword;
	}

	public void setPasswordCrypt(String connectionPassword) {
		this.connectionPassword = connectionPassword;
	}


	@Column(name="connection_url")
	public String getConnectionUrl() {
		return this.connectionUrl;
	}

	public void setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}


	@Column(name="connection_user")
	public String getUser() {
		return this.connectionUser;
	}

	public void setUser(String connectionUser) {
		this.connectionUser = connectionUser;
	}


	@Column(name="jdbc_driver")
	public String getJdbcDriver() {
		return this.jdbcDriver;
	}

	public void setJdbcDriver(String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
	}


	@Column(name="query")
	public String getQuery() {
		return this.query;
	}

	public void setQuery(String query) {
		this.query = query;
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
			monitorInfo.setSqlCheckInfo(this);
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
			this.monitorInfo.setSqlCheckInfo(null);
		}
	}

}