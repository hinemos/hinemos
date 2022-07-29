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


/**
 * The persistent class for the cc_notify_status_info database table.
 * 
 */
@Entity
@Table(name="cc_notify_status_info", schema="setting")
@Cacheable(true)
public class NotifyStatusInfo extends NotifyInfoDetail implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer statusInvalidFlg;
	private Integer statusUpdatePriority;
	private Integer statusValidPeriod;

	public NotifyStatusInfo() {
	}

	public NotifyStatusInfo(String notifyId) {
		super(notifyId);
	}

	@Column(name="status_invalid_flg")
	public Integer getStatusInvalidFlg() {
		return this.statusInvalidFlg;
	}

	public void setStatusInvalidFlg(Integer statusInvalidFlg) {
		this.statusInvalidFlg = statusInvalidFlg;
	}


	@Column(name="status_update_priority")
	public Integer getStatusUpdatePriority() {
		return this.statusUpdatePriority;
	}

	public void setStatusUpdatePriority(Integer statusUpdatePriority) {
		this.statusUpdatePriority = statusUpdatePriority;
	}


	@Column(name="status_valid_period")
	public Integer getStatusValidPeriod() {
		return this.statusValidPeriod;
	}

	public void setStatusValidPeriod(Integer statusValidPeriod) {
		this.statusValidPeriod = statusValidPeriod;
	}

	@Override
	protected void chainNotifyInfo() {
		getNotifyInfoEntity().setNotifyStatusInfo(this);
	}

	@Override
	protected void unchainNotifyInfo() {
		getNotifyInfoEntity().setNotifyStatusInfo(null);
	}
}