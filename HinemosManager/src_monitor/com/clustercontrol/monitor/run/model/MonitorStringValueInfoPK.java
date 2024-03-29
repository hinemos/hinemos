/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.model;

import java.io.Serializable;
import java.util.Arrays;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * The primary key class for the cc_monitor_string_value_info database table.
 * 
 */
@Embeddable
public class MonitorStringValueInfoPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private Integer orderNo;

	public MonitorStringValueInfoPK() {
	}

	public MonitorStringValueInfoPK(String monitorId, Integer orderNo) {
		this.setMonitorId(monitorId);
		this.setOrderNo(orderNo);
	}

	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	@Column(name="order_no")
	public Integer getOrderNo() {
		return this.orderNo;
	}
	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MonitorStringValueInfoPK)) {
			return false;
		}
		MonitorStringValueInfoPK castOther = (MonitorStringValueInfoPK)other;
		return
				this.monitorId.equals(castOther.monitorId)
				&& this.orderNo.equals(castOther.orderNo);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.monitorId.hashCode();
		hash = hash * prime + this.orderNo.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"monitorId",
				"orderNo"
		};
		String[] values = {
				this.monitorId,
				String.valueOf(this.orderNo)
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}