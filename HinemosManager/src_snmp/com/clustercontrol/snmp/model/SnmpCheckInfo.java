/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmp.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.model.MonitorCheckInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;


/**
 * The persistent class for the cc_monitor_snmp_info database table.
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name="cc_monitor_snmp_info", schema="setting")
@Cacheable(true)
public class SnmpCheckInfo extends MonitorCheckInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer convertFlg;
	private String snmpOid;
	private MonitorInfo monitorInfo;
	
	private String communityName;
	private Integer snmpPort;

	public SnmpCheckInfo() {
	}

	@Column(name="convert_flg")
	public Integer getConvertFlg() {
		return this.convertFlg;
	}

	public void setConvertFlg(Integer convertFlg) {
		this.convertFlg = convertFlg;
	}


	@Column(name="snmp_oid")
	public String getSnmpOid() {
		return this.snmpOid;
	}

	public void setSnmpOid(String snmpOid) {
		this.snmpOid = snmpOid;
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
			monitorInfo.setSnmpCheckInfo(this);
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
			this.monitorInfo.setSnmpCheckInfo(null);
		}
	}
	
	@Transient
	public String getCommunityName() {
		return this.communityName;
	}
	public void setCommunityName(String communityName) {
		this.communityName = communityName;
	}
	
	@Transient
	public Integer getSnmpPort() {
		return this.snmpPort;
	}
	public void setSnmpPort(Integer snmpPort) {
		this.snmpPort = snmpPort;
	}
}