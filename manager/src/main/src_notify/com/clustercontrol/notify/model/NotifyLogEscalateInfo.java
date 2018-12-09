/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

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


/**
 * The persistent class for the cc_notify_log_escalate_info database table.
 * 
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
@Entity
@Table(name="cc_notify_log_escalate_info", schema="setting")
@Cacheable(true)
public class NotifyLogEscalateInfo extends NotifyInfoDetail implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer escalateFacilityFlg;
	private Integer escalatePort;

	private String infoEscalateMessage;
	private String warnEscalateMessage;
	private String criticalEscalateMessage;
	private String unknownEscalateMessage;

	private Integer infoSyslogPriority;
	private Integer warnSyslogPriority;
	private Integer criticalSyslogPriority;
	private Integer unknownSyslogPriority;

	private Integer infoSyslogFacility;
	private Integer warnSyslogFacility;
	private Integer criticalSyslogFacility;
	private Integer unknownSyslogFacility;

	private String escalateFacilityId;
	
	/**転送先スコープテキスト*/
	private String	escalateScope;

	public NotifyLogEscalateInfo() {
	}

	public NotifyLogEscalateInfo(String notifyId) {
		super(notifyId);
	}

	@Column(name="escalate_facility_flg")
	public Integer getEscalateFacilityFlg() {
		return this.escalateFacilityFlg;
	}

	public void setEscalateFacilityFlg(Integer escalateFacilityFlg) {
		this.escalateFacilityFlg = escalateFacilityFlg;
	}

	@Column(name="info_escalate_message")
	public String getInfoEscalateMessage() {
		return this.infoEscalateMessage;
	}

	public void setInfoEscalateMessage(String infoEscalateMessage) {
		this.infoEscalateMessage = infoEscalateMessage;
	}


	@Column(name="warn_escalate_message")
	public String getWarnEscalateMessage() {
		return this.warnEscalateMessage;
	}

	public void setWarnEscalateMessage(String warnEscalateMessage) {
		this.warnEscalateMessage = warnEscalateMessage;
	}


	@Column(name="critical_escalate_message")
	public String getCriticalEscalateMessage() {
		return this.criticalEscalateMessage;
	}

	public void setCriticalEscalateMessage(String criticalEscalateMessage) {
		this.criticalEscalateMessage = criticalEscalateMessage;
	}


	@Column(name="unknown_escalate_message")
	public String getUnknownEscalateMessage() {
		return this.unknownEscalateMessage;
	}

	public void setUnknownEscalateMessage(String unknownEscalateMessage) {
		this.unknownEscalateMessage = unknownEscalateMessage;
	}



	@Column(name="info_syslog_facility")
	public Integer getInfoSyslogFacility() {
		return this.infoSyslogFacility;
	}

	public void setInfoSyslogFacility(Integer infoSyslogFacility) {
		this.infoSyslogFacility = infoSyslogFacility;
	}


	@Column(name="warn_syslog_facility")
	public Integer getWarnSyslogFacility() {
		return this.warnSyslogFacility;
	}

	public void setWarnSyslogFacility(Integer warnSyslogFacility) {
		this.warnSyslogFacility = warnSyslogFacility;
	}


	@Column(name="critical_syslog_facility")
	public Integer getCriticalSyslogFacility() {
		return this.criticalSyslogFacility;
	}

	public void setCriticalSyslogFacility(Integer criticalSyslogFacility) {
		this.criticalSyslogFacility = criticalSyslogFacility;
	}


	@Column(name="unknown_syslog_facility")
	public Integer getUnknownSyslogFacility() {
		return this.unknownSyslogFacility;
	}

	public void setUnknownSyslogFacility(Integer unknownSyslogFacility) {
		this.unknownSyslogFacility = unknownSyslogFacility;
	}


	@Column(name="info_syslog_priority")
	public Integer getInfoSyslogPriority() {
		return this.infoSyslogPriority;
	}

	public void setInfoSyslogPriority(Integer infoSyslogPriority) {
		this.infoSyslogPriority = infoSyslogPriority;
	}


	@Column(name="warn_syslog_priority")
	public Integer getWarnSyslogPriority() {
		return this.warnSyslogPriority;
	}

	public void setWarnSyslogPriority(Integer warnSyslogPriority) {
		this.warnSyslogPriority = warnSyslogPriority;
	}


	@Column(name="critical_syslog_priority")
	public Integer getCriticalSyslogPriority() {
		return this.criticalSyslogPriority;
	}

	public void setCriticalSyslogPriority(Integer criticalSyslogPriority) {
		this.criticalSyslogPriority = criticalSyslogPriority;
	}


	@Column(name="unknown_syslog_priority")
	public Integer getUnknownSyslogPriority() {
		return this.unknownSyslogPriority;
	}

	public void setUnknownSyslogPriority(Integer unknownSyslogPriority) {
		this.unknownSyslogPriority = unknownSyslogPriority;
	}


	@Column(name="escalate_port")
	public Integer getEscalatePort() {
		return this.escalatePort;
	}

	public void setEscalatePort(Integer escalatePort) {
		this.escalatePort = escalatePort;
	}


	@Column(name="escalate_facility")
	public String getEscalateFacility() {
		return this.escalateFacilityId;
	}

	public void setEscalateFacility(String escalateFacilityId) {
		this.escalateFacilityId = escalateFacilityId;
	}

	@Transient
	public String getEscalateScope() {
		if (escalateScope == null)
			try {
				escalateScope = new RepositoryControllerBean().getFacilityPath(getEscalateFacility(), null);
			} catch (HinemosUnknown e) {
				Logger.getLogger(this.getClass()).debug(e.getMessage(), e);
			}
		return escalateScope;
	}

	public void setEscalateScope(String escalateScope) {
		this.escalateScope = escalateScope;
	}

	@Override
	protected void chainNotifyInfo() {
		getNotifyInfoEntity().setNotifyLogEscalateInfo(this);
	}

	@Override
	protected void unchainNotifyInfo() {
		getNotifyInfoEntity().setNotifyLogEscalateInfo(null);
	}
}