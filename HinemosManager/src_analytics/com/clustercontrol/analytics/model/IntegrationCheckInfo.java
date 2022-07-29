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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
 * 収集値統合監視の設定Bean
 * The persistent class for the cc_monitor_integration_info database table.
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name="cc_monitor_integration_info", schema="setting")
@Cacheable(true)
public class IntegrationCheckInfo extends MonitorCheckInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer timeout;
	private Boolean notOrder = false;
	private String messageOk;
	private String messageNg;
	private List<IntegrationConditionInfo> conditionList = new ArrayList<>();
	private MonitorInfo monitorInfo;

	public IntegrationCheckInfo() {
	}

	@Column(name="timeout")
	public Integer getTimeout() {
		return this.timeout;
	}
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	@Column(name="not_order")
	public Boolean getNotOrder() {
		return this.notOrder;
	}
	public void setNotOrder(Boolean notOrder) {
		this.notOrder = notOrder;
	}

	@Column(name="message_ok")
	public String getMessageOk() {
		return this.messageOk;
	}
	public void setMessageOk(String messageOk) {
		this.messageOk = messageOk;
	}

	@Column(name="message_ng")
	public String getMessageNg() {
		return this.messageNg;
	}
	public void setMessageNg(String messageNg) {
		this.messageNg = messageNg;
	}

	//bi-directional one-to-one association to MonitorInfo
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
			monitorInfo.setIntegrationCheckInfo(this);
		}
	}

	//bi-directional many-to-one association to MonitorStringValueInfoEntity
	@OneToMany(mappedBy="monitorIntegrationInfo", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<IntegrationConditionInfo> getConditionList() {
		return this.conditionList;
	}
	public void setConditionList(List<IntegrationConditionInfo> conditionList) {
		if (conditionList != null && conditionList.size() > 0) {
			Collections.sort(conditionList, new Comparator<IntegrationConditionInfo>() {
				@Override
				public int compare(IntegrationConditionInfo o1, IntegrationConditionInfo o2) {
					return o1.getId().getOrderNo().compareTo(o2.getId().getOrderNo());
				}
			});
		}
		this.conditionList = conditionList;
	}

	/**
	 * IntegrationConditionInfo削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteIntegrationConditionList(List<IntegrationConditionInfoPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<IntegrationConditionInfo> list = this.getConditionList();
			Iterator<IntegrationConditionInfo> iter = list.iterator();
			while(iter.hasNext()) {
				IntegrationConditionInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
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
			this.monitorInfo.setIntegrationCheckInfo(null);
		}
	}
}