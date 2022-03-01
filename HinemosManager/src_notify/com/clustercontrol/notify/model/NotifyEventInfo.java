/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import javax.xml.bind.annotation.XmlType;


/**
 * The persistent class for the cc_notify_event_info database table.
 * 
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
@Entity
@Table(name="cc_notify_event_info", schema="setting")
@Cacheable(true)
public class NotifyEventInfo extends NotifyInfoDetail implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer infoEventNormalState;
	private Integer warnEventNormalState;
	private Integer criticalEventNormalState;
	private Integer unknownEventNormalState;

	public NotifyEventInfo() {
	}

	public NotifyEventInfo(String notifyId) {
		super(notifyId);
	}

	@Column(name="info_event_normal_state")
	public Integer getInfoEventNormalState() {
		return this.infoEventNormalState;
	}

	public void setInfoEventNormalState(Integer infoEventNormalState) {
		this.infoEventNormalState = infoEventNormalState;
	}

	@Column(name="warn_event_normal_state")
	public Integer getWarnEventNormalState() {
		return this.warnEventNormalState;
	}

	public void setWarnEventNormalState(Integer warnEventNormalState) {
		this.warnEventNormalState = warnEventNormalState;
	}

	@Column(name="critical_event_normal_state")
	public Integer getCriticalEventNormalState() {
		return this.criticalEventNormalState;
	}

	public void setCriticalEventNormalState(Integer criticalEventNormalState) {
		this.criticalEventNormalState = criticalEventNormalState;
	}

	@Column(name="unknown_event_normal_state")
	public Integer getUnknownEventNormalState() {
		return this.unknownEventNormalState;
	}

	public void setUnknownEventNormalState(Integer unknownEventNormalState) {
		this.unknownEventNormalState = unknownEventNormalState;
	}

	@Override
	protected void chainNotifyInfo() {
		getNotifyInfoEntity().setNotifyEventInfo(this);
	}

	@Override
	protected void unchainNotifyInfo() {
		getNotifyInfoEntity().setNotifyEventInfo(null);
	}
}