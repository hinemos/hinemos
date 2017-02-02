package com.clustercontrol.notify.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.repository.session.RepositoryControllerBean;

@XmlType(namespace = "http://notify.ws.clustercontrol.com")
@Entity
@Table(name="cc_notify_infra_info", schema="setting")
@Cacheable(true)
public class NotifyInfraInfo extends NotifyInfoDetail implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer infraExecFacilityFlg;

	private String infoInfraId;
	private String warnInfraId;
	private String criticalInfraId;
	private String unknownInfraId;

	private Integer infoInfraFailurePriority;
	private Integer warnInfraFailurePriority;
	private Integer criticalInfraFailurePriority;
	private Integer unknownInfraFailurePriority;

	private String infraExecFacilityId;

	private String infraExecScope;
	
	public NotifyInfraInfo() {
	}
	
	public NotifyInfraInfo(String notifyId) {
		super(notifyId);
	}

	@Column(name="infra_exec_facility_flg")
	public Integer getInfraExecFacilityFlg() {
		return infraExecFacilityFlg;
	}

	public void setInfraExecFacilityFlg(Integer infraExecFacilityFlg) {
		this.infraExecFacilityFlg = infraExecFacilityFlg;
	}

	@Column(name="info_infra_id")
	public String getInfoInfraId() {
		return infoInfraId;
	}

	public void setInfoInfraId(String infoInfraId) {
		this.infoInfraId = infoInfraId;
	}
	
	@Column(name="warn_infra_id")
	public String getWarnInfraId() {
		return warnInfraId;
	}

	public void setWarnInfraId(String warnInfraId) {
		this.warnInfraId = warnInfraId;
	}

	@Column(name="critical_infra_id")
	public String getCriticalInfraId() {
		return criticalInfraId;
	}

	public void setCriticalInfraId(String criticalInfraId) {
		this.criticalInfraId = criticalInfraId;
	}

	@Column(name="unknown_infra_id")
	public String getUnknownInfraId() {
		return unknownInfraId;
	}

	public void setUnknownInfraId(String unknownInfraId) {
		this.unknownInfraId = unknownInfraId;
	}

	@Column(name="info_infra_failure_priority")
	public Integer getInfoInfraFailurePriority() {
		return infoInfraFailurePriority;
	}

	public void setInfoInfraFailurePriority(Integer infoInfraFailurePriority) {
		this.infoInfraFailurePriority = infoInfraFailurePriority;
	}

	@Column(name="warn_infra_failure_priority")
	public Integer getWarnInfraFailurePriority() {
		return warnInfraFailurePriority;
	}

	public void setWarnInfraFailurePriority(Integer warnInfraFailurePriority) {
		this.warnInfraFailurePriority = warnInfraFailurePriority;
	}

	@Column(name="critical_infra_failure_priority")
	public Integer getCriticalInfraFailurePriority() {
		return criticalInfraFailurePriority;
	}

	public void setCriticalInfraFailurePriority(Integer criticalInfraFailurePriority) {
		this.criticalInfraFailurePriority = criticalInfraFailurePriority;
	}

	@Column(name="unknown_infra_failure_priority")
	public Integer getUnknownInfraFailurePriority() {
		return unknownInfraFailurePriority;
	}

	public void setUnknownInfraFailurePriority(Integer unknownInfraFailurePriority) {
		this.unknownInfraFailurePriority = unknownInfraFailurePriority;
	}

	@Column(name="infra_exec_facility")
	public String getInfraExecFacility() {
		return infraExecFacilityId;
	}

	public void setInfraExecFacility(String infraExecFacilityId) {
		this.infraExecFacilityId = infraExecFacilityId;
	}

	@Transient
	public String getInfraExecScope() {
		if (infraExecScope == null)
			try {
				infraExecScope = new RepositoryControllerBean().getFacilityPath(getInfraExecFacility(), null);
			} catch (HinemosUnknown e) {
				Logger.getLogger(this.getClass()).debug(e.getMessage(), e);
			}
		return infraExecScope;
	}

	public void setInfraExecScope(String infraExecScope) {
		this.infraExecScope = infraExecScope;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	protected void chainNotifyInfo() {
		getNotifyInfoEntity().setNotifyInfraInfo(this);
	}

	@Override
	protected void unchainNotifyInfo() {
		getNotifyInfoEntity().setNotifyInfraInfo(null);
	}

}
