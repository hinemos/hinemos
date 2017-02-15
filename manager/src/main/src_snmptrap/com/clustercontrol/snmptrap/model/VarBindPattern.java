package com.clustercontrol.snmptrap.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * The persistent class for the cc_monitor_trap_varbind_pattern_info database table.
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name="cc_monitor_trap_varbind_pattern_info", schema="setting")
@Cacheable(true)
public class VarBindPattern implements Serializable {
	private static final long serialVersionUID = 1L;

	private VarBindPatternPK id;
	private String description;
	private Boolean processType;
	private String pattern;
	private Integer priority;
	private Boolean caseSensitivityFlg;
	private Boolean validFlg;
	private TrapValueInfo monitorTrapValueInfoEntity;

	public VarBindPattern() {
	}

	public VarBindPattern(VarBindPatternPK id) {
		this.setId(id);
	}

	public VarBindPattern(
			String monitorId,
			String mib,
			String trapOid,
			Integer genericId,
			Integer specificId,
			Integer orderNo
			) {
		this(new VarBindPatternPK(
				monitorId,
				mib,
				trapOid,
				genericId,
				specificId,
				orderNo));
	}

	@XmlTransient
	@EmbeddedId
	public VarBindPatternPK getId() {
		if (id == null)
			id = new VarBindPatternPK();
		return id;
	}
	public void setId(VarBindPatternPK id) {
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

	@XmlTransient
	@Transient
	public String getMib() {
		return getId().getMib();
	}
	public void setMib(String mib) {
		getId().setMib(mib);
	}

	@XmlTransient
	@Transient
	public String getTrapOid() {
		return getId().getTrapOid();
	}
	public void setTrapOid(String trapOid) {
		getId().setTrapOid(trapOid);
	}

	@XmlTransient
	@Transient
	public Integer getGenericId() {
		return getId().getGenericId();
	}
	public void setGenericId(Integer genericId) {
		getId().setGenericId(genericId);
	}

	@XmlTransient
	@Transient
	public Integer getSpecificId() {
		return getId().getSpecificId();
	}
	public void setSpecificId(Integer specificId) {
		getId().setSpecificId(specificId);
	}

	@XmlTransient
	@Transient
	public Integer getOrderNo() {
		return getId().getOrderNo();
	}
	public void setOrderNo(Integer orderNo) {
		getId().setOrderNo(orderNo);
	}

	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="process_type")
	public Boolean getProcessType() {
		return processType;
	}
	public void setProcessType(Boolean processType) {
		this.processType = processType;
	}

	@Column(name="pattern")
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	@Column(name="priority")
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	@Column(name="case_sensitivity_flg")
	public Boolean getCaseSensitivityFlg() {
		return caseSensitivityFlg;
	}
	public void setCaseSensitivityFlg(Boolean caseSensitivityFlg) {
		this.caseSensitivityFlg = caseSensitivityFlg;
	}

	@Column(name="valid_flg")
	public Boolean getValidFlg() {
		return validFlg;
	}
	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	@Override
	public String toString() {
		return "MonitorTrapVarbindPatternInfoEntity [id=" + id
				+ ", description=" + description + ", processType="
				+ processType + ", pattern=" + pattern + ", priority="
				+ priority + ", caseSensitivityFlg="
				+ caseSensitivityFlg + ", validFlg=" + validFlg + "]";
	}
	
	//bi-directional many-to-one association to MonitorTrapValueInfoEntity
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
	@JoinColumn(name="monitor_id", referencedColumnName="monitor_id", insertable=false, updatable=false),
	@JoinColumn(name="mib", referencedColumnName="mib", insertable=false, updatable=false),
	@JoinColumn(name="trap_oid", referencedColumnName="trap_oid", insertable=false, updatable=false),
	@JoinColumn(name="generic_id", referencedColumnName="generic_id", insertable=false, updatable=false),
	@JoinColumn(name="specific_id", referencedColumnName="specific_id", insertable=false, updatable=false)
	})
	public TrapValueInfo getMonitorTrapValueInfoEntity() {
		return this.monitorTrapValueInfoEntity;
	}
	
	@Deprecated
	public void setMonitorTrapValueInfoEntity(TrapValueInfo trapValueInfoEntity) {
		this.monitorTrapValueInfoEntity = trapValueInfoEntity;
	}

	/**
	 * MonitorTrapValueInfoEntityオブジェクト参照設定<BR>
	 * 
	 * MonitorTrapValueInfoEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToMonitorTrapValueInfoEntity(TrapValueInfo trapValueInfoEntity) {
		this.setMonitorTrapValueInfoEntity(trapValueInfoEntity);
		if (trapValueInfoEntity != null) {
			List<VarBindPattern> list = trapValueInfoEntity.getVarBindPatterns();
			if (list == null) {
				list = new ArrayList<VarBindPattern>();
			} else {
				for(VarBindPattern entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			trapValueInfoEntity.setVarBindPatterns(list);
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

		// NodeEntity
		if (this.monitorTrapValueInfoEntity != null) {
			List<VarBindPattern> list = this.monitorTrapValueInfoEntity.getVarBindPatterns();
			if (list != null) {
				Iterator<VarBindPattern> iter = list.iterator();
				while(iter.hasNext()) {
					VarBindPattern entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
}