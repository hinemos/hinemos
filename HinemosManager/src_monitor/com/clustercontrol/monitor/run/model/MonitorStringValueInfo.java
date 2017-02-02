package com.clustercontrol.monitor.run.model;

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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


/**
 * The persistent class for the cc_monitor_string_value_info database table.
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")

@Entity
@Table(name="cc_monitor_string_value_info", schema="setting")
@Cacheable(true)
public class MonitorStringValueInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private MonitorStringValueInfoPK id;
	private String message;
	private Boolean caseSensitivityFlg;
	private String description;
	private String pattern;
	private Integer priority;
	private Boolean processType;
	private Boolean validFlg;
	private MonitorInfo monitorInfo;

	public MonitorStringValueInfo() {
	}

	public MonitorStringValueInfo(MonitorStringValueInfoPK pk) {
		this.setId(pk);
	}

	public MonitorStringValueInfo(String monitorId, Integer orderNo) {
		this(new MonitorStringValueInfoPK(monitorId, orderNo));
	}

	@XmlTransient
	@EmbeddedId
	public MonitorStringValueInfoPK getId() {
		if (this.id == null)
			this.id = new MonitorStringValueInfoPK();
		return this.id;
	}

	public void setId(MonitorStringValueInfoPK id) {
		this.id = id;
	}
	
	@Transient
	public String getMonitorId() {
		return getId().getMonitorId();
	}
	public void setMonitorId(String monitorId) {
		getId().setMonitorId(monitorId);
	}

	@XmlTransient
	@Transient
	public Integer getOrderNo() {
		return getId().getOrderNo();
	}
	public void setOrderNo(Integer orderNo) {
		getId().setOrderNo(orderNo);
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Column(name="case_sensitivity_flg")
	public Boolean getCaseSensitivityFlg() {
		return this.caseSensitivityFlg;
	}

	public void setCaseSensitivityFlg(Boolean caseSensitivityFlg) {
		this.caseSensitivityFlg = caseSensitivityFlg;
	}


	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPattern() {
		return this.pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}


	public Integer getPriority() {
		return this.priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}


	@Column(name="process_type")
	public Boolean getProcessType() {
		return this.processType;
	}

	public void setProcessType(Boolean processType) {
		this.processType = processType;
	}


	@Column(name="valid_flg")
	public Boolean getValidFlg() {
		return this.validFlg;
	}

	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	//bi-directional many-to-one association to MonitorInfo
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="monitor_id", insertable=false, updatable=false)
	public MonitorInfo getMonitorInfo() {
		return this.monitorInfo;
	}

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
			List<MonitorStringValueInfo> list = monitorInfo.getStringValueInfo();
			if (list == null) {
				list = new ArrayList<MonitorStringValueInfo>();
			} else {
				for(MonitorStringValueInfo entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			monitorInfo.setStringValueInfo(list);
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
			List<MonitorStringValueInfo> list = this.monitorInfo.getStringValueInfo();
			if (list != null) {
				Iterator<MonitorStringValueInfo> iter = list.iterator();
				while(iter.hasNext()) {
					MonitorStringValueInfo entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
}