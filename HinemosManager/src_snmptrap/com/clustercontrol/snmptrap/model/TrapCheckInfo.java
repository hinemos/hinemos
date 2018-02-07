/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmptrap.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.monitor.run.model.MonitorCheckInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;


/**
 * The persistent class for the cc_monitor_trap_info database table.
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name="cc_monitor_trap_info", schema="setting")
@Cacheable(false)
public class TrapCheckInfo extends MonitorCheckInfo implements Serializable {


	private static final long serialVersionUID = 1L;

	private Boolean charsetConvert;
	private String charsetName;
	private Boolean communityCheck;
	private String communityName;
	private Boolean notifyofReceivingUnspecifiedFlg;
	private Integer priorityUnspecified;
	private MonitorInfo monitorInfo;

	private List<TrapValueInfo> monitorTrapValueInfoEntities = new ArrayList<>();

	public TrapCheckInfo() {
	}

	@Column(name="charset_convert")
	public Boolean getCharsetConvert() {
		return this.charsetConvert;
	}
	public void setCharsetConvert(Boolean charsetConvert) {
		this.charsetConvert = charsetConvert;
	}


	@Column(name="charset_name")
	public String getCharsetName() {
		return this.charsetName;
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}


	@Column(name="community_check")
	public Boolean getCommunityCheck() {
		return this.communityCheck;
	}

	public void setCommunityCheck(Boolean communityCheck) {
		this.communityCheck = communityCheck;
	}


	@Column(name="community_name")
	public String getCommunityName() {
		return this.communityName;
	}

	public void setCommunityName(String communityName) {
		this.communityName = communityName;
	}


	@Column(name="notifyof_receiving_unspecified_flg")
	public Boolean getNotifyofReceivingUnspecifiedFlg() {
		return notifyofReceivingUnspecifiedFlg;
	}
	public void setNotifyofReceivingUnspecifiedFlg(
			Boolean notifyofReceivingUnspecifiedFlg) {
		this.notifyofReceivingUnspecifiedFlg = notifyofReceivingUnspecifiedFlg;
	}

	@Column(name="priority_unspecified")
	public Integer getPriorityUnspecified() {
		return priorityUnspecified;
	}
	public void setPriorityUnspecified(Integer priorityUnspecified) {
		this.priorityUnspecified = priorityUnspecified;
	}

	@OneToMany(mappedBy="monitorTrapInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<TrapValueInfo> getTrapValueInfos() {
		return this.monitorTrapValueInfoEntities;
	}

	public void setTrapValueInfos(List<TrapValueInfo> monitorTrapValueInfoEntities) {
		this.monitorTrapValueInfoEntities = monitorTrapValueInfoEntities;
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
			monitorInfo.setTrapCheckInfo(this);
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
			this.monitorInfo.setTrapCheckInfo(null);
		}
	}

	public void deleteMonitorTrapValueInfoEntities(List<TrapValueInfoPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<TrapValueInfo> list = this.getTrapValueInfos();
			Iterator<TrapValueInfo> iter = list.iterator();
			while(iter.hasNext()) {
				TrapValueInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					for (VarBindPattern p: entity.getVarBindPatterns()) {
						em.remove(p);
					}
					entity.getVarBindPatterns().clear();
					
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}
}