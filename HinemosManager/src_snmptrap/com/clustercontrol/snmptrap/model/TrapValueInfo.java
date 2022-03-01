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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;

/**
 * The persistent class for the cc_monitor_trap_value_info database table.
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name="cc_monitor_trap_value_info", schema="setting")
@Cacheable(false)
public class TrapValueInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private TrapValueInfoPK id;
	private String uei;
	private Integer version;
	private String logmsg;
	private String description;
	private Boolean procVarbindSpecified;
	private Integer priorityAnyVarBind;
	private String formatVarBinds;
	private Boolean validFlg;
	private TrapCheckInfo monitorTrapInfoEntity;

	private List<VarBindPattern> varBindPatterns = new ArrayList<>();

	public TrapValueInfo() {
	}

	public TrapValueInfo(TrapValueInfoPK id) {
		this.setId(id);
	}

	public TrapValueInfo(
			String monitorId,
			String mib,
			String trapOid,
			Integer genericId,
			Integer specificId
			) {
		this(new TrapValueInfoPK(monitorId, mib, trapOid, genericId, specificId));
	}
	
	@XmlTransient
	@EmbeddedId
	public TrapValueInfoPK getId() {
		if (this.id == null)
			id = new TrapValueInfoPK();
		return id;
	}
	public void setId(TrapValueInfoPK id) {
		this.id = id;
	}
	
	@XmlTransient
	@Transient
	public String getMonitorId() {
		return getId().getMonitorId();
	}
	public void setMonitorId(String monitorId) {
		getId().setMonitorId(monitorId);
	}

	@Transient
	public String getMib() {
		return getId().getMib();
	}
	public void setMib(String mib) {
		getId().setMib(mib);
	}

	@Transient
	public String getTrapOid() {
		return getId().getTrapOid();
	}
	public void setTrapOid(String trapOid) {
		getId().setTrapOid(trapOid);
	}

	@Transient
	public Integer getGenericId() {
		return getId().getGenericId();
	}
	public void setGenericId(Integer genericId) {
		getId().setGenericId(genericId);
	}

	@Transient
	public Integer getSpecificId() {
		return getId().getSpecificId();
	}
	public void setSpecificId(Integer specificId) {
		getId().setSpecificId(specificId);
	}
	
	@Column(name="uei")
	public String getUei() {
		return uei;
	}
	public void setUei(String uei) {
		this.uei = uei;
	}

	@Column(name="version")
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}

	@Column(name="logmsg")
	public String getLogmsg() {
		return logmsg;
	}
	public void setLogmsg(String logmsg) {
		this.logmsg = logmsg;
	}

	@Column(name="descr")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="proc_varbind_specified")
	public Boolean getProcessingVarbindSpecified() {
		return procVarbindSpecified;
	}
	public void setProcessingVarbindSpecified(Boolean procVarbindSpecified) {
		this.procVarbindSpecified = procVarbindSpecified;
	}

	@Column(name="priority_any_varbind")
	public Integer getPriorityAnyVarbind() {
		return priorityAnyVarBind;
	}
	public void setPriorityAnyVarbind(Integer priorityAnyVarbind) {
		this.priorityAnyVarBind = priorityAnyVarbind;
	}

	@Column(name="format_varbinds")
	public String getFormatVarBinds() {
		return formatVarBinds;
	}
	public void setFormatVarBinds(String formatVarBinds) {
		this.formatVarBinds = formatVarBinds;
	}

	@Column(name="valid_flg")
	public Boolean getValidFlg() {
		return validFlg;
	}
	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	@OneToMany(mappedBy="monitorTrapValueInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<VarBindPattern> getVarBindPatterns() {
		return this.varBindPatterns;
	}

	public void setVarBindPatterns(List<VarBindPattern> varBindPatterns) {
		if (varBindPatterns != null && varBindPatterns.size() > 0) {
			Collections.sort(varBindPatterns, new Comparator<VarBindPattern>() {
				@Override
				public int compare(VarBindPattern o1, VarBindPattern o2) {
					return o1.getId().getOrderNo().compareTo(o2.getId().getOrderNo());
				}
			});
		}
		this.varBindPatterns = varBindPatterns;
	}

	@Override
	public String toString() {
		return "MonitorTrapValueInfoEntity [id=" + id + ", uei=" + uei
				+ ", version=" + version + ", logmsg=" + logmsg
				+ ", description=" + description + ", procVarbindSpecified="
				+ procVarbindSpecified + ", procVarbindSpecified="
				+ priorityAnyVarBind + ", formatVarBind=" + formatVarBinds
				+ ", validFlg=" + validFlg
				+ ", varBindPatterns="
				+ varBindPatterns + "]";
	}
	
	//bi-directional many-to-one association to MonitorTrapInfoEntity
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="monitor_id", insertable=false, updatable=false)
	public TrapCheckInfo getMonitorTrapInfoEntity() {
		return this.monitorTrapInfoEntity;
	}

	@Deprecated
	public void setMonitorTrapInfoEntity(TrapCheckInfo monitorTrapInfoEntity) {
		this.monitorTrapInfoEntity = monitorTrapInfoEntity;
	}
	
	/**
	 * MonitorTrapInfoEntityオブジェクト参照設定<BR>
	 * 
	 * MonitorTrapInfoEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToMonitorTrapInfoEntity(TrapCheckInfo monitorTrapInfoEntity) {
		this.setMonitorTrapInfoEntity(monitorTrapInfoEntity);
		if (monitorTrapInfoEntity != null) {
			List<TrapValueInfo> list = monitorTrapInfoEntity.getTrapValueInfos();
			if (list == null) {
				list = new ArrayList<TrapValueInfo>();
			} else {
				for(TrapValueInfo entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorTrapInfoEntity.setTrapValueInfos(list);
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

		// MonitorTrapInfoEntity
		if (this.monitorTrapInfoEntity != null) {
			List<TrapValueInfo> list = this.monitorTrapInfoEntity.getTrapValueInfos();
			if (list != null) {
				Iterator<TrapValueInfo> iter = list.iterator();
				while(iter.hasNext()) {
					TrapValueInfo entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
	
	public void deleteMonitorTrapVarbindPatternInfoEntities(List<VarBindPatternPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<VarBindPattern> list = this.getVarBindPatterns();
			Iterator<VarBindPattern> iter = list.iterator();
			while(iter.hasNext()) {
				VarBindPattern entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}
}
