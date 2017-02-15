package com.clustercontrol.custom.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.custom.bean.CustomConstant;
import com.clustercontrol.monitor.run.model.MonitorCheckInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;



/**
 * The persistent class for the cc_monitor_custom_info database table.
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")

@Entity
@Table(name="cc_monitor_custom_info", schema="setting")
@Cacheable(true)
public class CustomCheckInfo extends MonitorCheckInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String monitorTypeId;
	private String command;
	private Boolean specifyUser;
	private String effectiveUser;
	private CustomConstant.CommandExecType executeType;
	private String selectedFacilityId;
	private Integer timeout;
	private MonitorInfo monitorInfo;
	private Integer convertFlg;

	public CustomCheckInfo() {
	}

	@Transient
	public String getMonitorTypeId(){
		return this.monitorTypeId;
	}
	public void setMonitorTypeId(String monitorTypeId){
		this.monitorTypeId = monitorTypeId;
	}
	
	public String getCommand() {
		return this.command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	@Column(name="specify_user")
	public Boolean getSpecifyUser() {
		return this.specifyUser;
	}

	public void setSpecifyUser(Boolean specifyUser) {
		this.specifyUser = specifyUser;
	}

	@Column(name="effective_user")
	public String getEffectiveUser() {
		return this.effectiveUser;
	}

	public void setEffectiveUser(String effectiveUser) {
		this.effectiveUser = effectiveUser;
	}

	@Enumerated(EnumType.ORDINAL)
	@Column(name="execute_type")
	public CustomConstant.CommandExecType getCommandExecType() {
		return this.executeType;
	}

	public void setCommandExecType(CustomConstant.CommandExecType executeType) {
		this.executeType = executeType;
	}


	@Column(name="selected_facility_id")
	public String getSelectedFacilityId() {
		return this.selectedFacilityId;
	}

	public void setSelectedFacilityId(String selectedFacilityId) {
		this.selectedFacilityId = selectedFacilityId;
	}


	public Integer getTimeout() {
		return this.timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	@Column(name="convert_flg")
	public Integer getConvertFlg() {
		return this.convertFlg;
	}

	public void setConvertFlg(Integer convertFlg) {
		this.convertFlg = convertFlg;
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
			monitorInfo.setCustomCheckInfo(this);
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
			this.monitorInfo.setCustomCheckInfo(null);
		}
	}

}