/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.plugin.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.monitor.run.model.MonitorCheckInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;

/**
 * The persistent class for the cc_monitor_plugin_info database table.
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name="cc_monitor_plugin_info", schema="setting")
@Cacheable(true)
public class PluginCheckInfo extends MonitorCheckInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<MonitorPluginNumericInfo> monitorPluginNumericInfoList = new ArrayList<>();
	private List<MonitorPluginStringInfo> monitorPluginStringInfoList = new ArrayList<>();
	private MonitorInfo monitorInfo;

	@Deprecated
	public PluginCheckInfo() {
	}

	//bi-directional many-to-one association to MonitorPluginNumericInfoEntity
	@OneToMany(mappedBy="monitorPluginInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public List<MonitorPluginNumericInfo> getMonitorPluginNumericInfoList() {
		return this.monitorPluginNumericInfoList;
	}

	public void setMonitorPluginNumericInfoList(List<MonitorPluginNumericInfo> monitorPluginNumericInfoList) {
		this.monitorPluginNumericInfoList = monitorPluginNumericInfoList;
	}

	//bi-directional many-to-one association to MonitorPluginNumericInfoEntity
	@OneToMany(mappedBy="monitorPluginInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public List<MonitorPluginStringInfo> getMonitorPluginStringInfoList() {
		return this.monitorPluginStringInfoList;
	}

	public void setMonitorPluginStringInfoList(List<MonitorPluginStringInfo> monitorPluginStringInfoList) {
		this.monitorPluginStringInfoList = monitorPluginStringInfoList;
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
	 * MonitorPluginNumericInfoEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteMonitorPluginNumericInfoEntities(List<MonitorPluginNumericInfoEntityPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorPluginNumericInfo> list = this.getMonitorPluginNumericInfoList();
			Iterator<MonitorPluginNumericInfo> iter = list.iterator();
			while(iter.hasNext()) {
				MonitorPluginNumericInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}

	/**
	 * MonitorPluginStringInfoEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteMonitorPluginStringInfoEntities(List<MonitorPluginStringInfoEntityPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorPluginStringInfo> list = this.getMonitorPluginStringInfoList();
			Iterator<MonitorPluginStringInfo> iter = list.iterator();
			while(iter.hasNext()) {
				MonitorPluginStringInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
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
			monitorInfo.setPluginCheckInfo(this);
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
			this.monitorInfo.setPluginCheckInfo(null);
		}
	}

}
