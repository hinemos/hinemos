/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * The persistent class for the cc_notify_rest_info database table.
 * 
 */
@Entity
@Table(name="cc_notify_rest_info", schema="setting")
@Cacheable(true)
public class NotifyRestInfo extends NotifyInfoDetail implements Serializable {
	private static final long serialVersionUID = 1L;

	private String infoRestAccessId;
	private String warnRestAccessId;
	private String criticalRestAccessId;
	private String unknownRestAccessId;

	public NotifyRestInfo() {
	}

	public NotifyRestInfo(String notifyId) {
		super(notifyId);
	}

	@Column(name="info_rest_access_id")
	public String getInfoRestAccessId() {
		return infoRestAccessId;
	}

	public void setInfoRestAccessId(String infoRestAccessId) {
		this.infoRestAccessId = infoRestAccessId;
	}

	@Column(name="warn_rest_access_id")
	public String getWarnRestAccessId() {
		return warnRestAccessId;
	}

	public void setWarnRestAccessId(String warnRestAccessId) {
		this.warnRestAccessId = warnRestAccessId;
	}

	@Column(name="critical_rest_access_id")
	public String getCriticalRestAccessId() {
		return criticalRestAccessId;
	}

	public void setCriticalRestAccessId(String criticalRestAccessId) {
		this.criticalRestAccessId = criticalRestAccessId;
	}

	@Column(name="unknown_rest_access_id")
	public String getUnknownRestAccessId() {
		return unknownRestAccessId;
	}

	public void setUnknownRestAccessId(String unknownRestAccessId) {
		this.unknownRestAccessId = unknownRestAccessId;
	}

	@Override
	protected void chainNotifyInfo() {
		getNotifyInfoEntity().setNotifyRestInfo(this);
	}

	@Override
	protected void unchainNotifyInfo() {
		getNotifyInfoEntity().setNotifyRestInfo(null);
	}
}