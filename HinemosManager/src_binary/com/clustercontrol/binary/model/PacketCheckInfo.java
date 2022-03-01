/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.model.MonitorCheckInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;

/**
 * The persistent class for the cc_monitor_packet database table.
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name = "cc_monitor_packet", schema = "setting")
@Cacheable(true)
public class PacketCheckInfo extends MonitorCheckInfo implements Serializable {
	/** シリアルバージョンUID(カラム構成変更時にアップ). */
	private static final long serialVersionUID = 1L;

	// ------DBと紐づく項目
	// 主キー.
	/** 監視ID. */
	private String monitorId = null;

	// パケットキャプチャ監視情報.
	/** プロミスキャスモード. */
	private boolean promiscuousMode = false;
	/** フィルター文字列(BPF Expression). */
	private String filterStr = null;

	// ------Javaのみ.
	// 親.
	/** 監視情報. */
	private MonitorInfo monitorInfo = null;

	// コンストラクタ.
	public PacketCheckInfo() {
	}

	/** 監視ID. */
	@XmlTransient
	@Id
	@Column(name = "monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}

	/** 監視ID. */
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	/** プロミスキャスモード. */
	@Column(name = "promiscuous_mode")
	public boolean isPromiscuousMode() {
		return promiscuousMode;
	}

	/** プロミスキャスモード. */
	public void setPromiscuousMode(boolean promiscuousMode) {
		this.promiscuousMode = promiscuousMode;
	}

	/** フィルター文字列(BPF Expression). */
	@Column(name = "filter_str")
	public String getFilterStr() {
		return filterStr;
	}

	/** フィルター文字列(BPF Expression). */
	public void setFilterStr(String filterStr) {
		this.filterStr = filterStr;
	}

	// bi-directional one-to-one association to MonitorInfo
	@XmlTransient
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@PrimaryKeyJoinColumn
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
	 * based on references held by the owning side of the relationship. It is
	 * the developer’s responsibility to keep the in-memory references held on
	 * the owning side and those held on the inverse side consistent with each
	 * other when they change.
	 */
	public void relateToMonitorInfo(MonitorInfo monitorInfo) {
		this.setMonitorInfo(monitorInfo);
		if (monitorInfo != null) {
			monitorInfo.setPacketCheckInfo(this);
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
	 * based on references held by the owning side of the relationship. It is
	 * the developer’s responsibility to keep the in-memory references held on
	 * the owning side and those held on the inverse side consistent with each
	 * other when they change.
	 */
	public void unchain() {

		// MonitorInfo
		if (this.monitorInfo != null) {
			this.monitorInfo.setPacketCheckInfo(null);
		}
	}

	@Override
	public String toString() {

		String delimiter = ", ";

		String checkInfo = "PacketCheckInfo [";

		checkInfo = checkInfo + "monitorId=" + monitorId;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "promiscuousMode=" + promiscuousMode;
		checkInfo = checkInfo + delimiter;
		checkInfo = checkInfo + "filterStr=" + filterStr;

		checkInfo = checkInfo + "]";

		return checkInfo;
	}
}