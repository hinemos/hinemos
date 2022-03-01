/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.http.model;

import java.io.Serializable;
import java.util.Arrays;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;


/**
 * The primary key class for the cc_monitor_http_scenario_variable_info database table.
 * 
 */
@Embeddable
public class VariablePK implements Serializable {
	private static final long serialVersionUID = 1L;

	private String monitorId;
	private Integer pageOrderNo;
	private String name;

	public VariablePK() {
	}

	public VariablePK(String monitorId,
			Integer pageOrderNo,
			String name) {
		this.setMonitorId(monitorId);
		this.setPageOrderNo(pageOrderNo);
		this.setName(name);
	}

	@Column(name="monitor_id")
	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}


	@Column(name="page_order_no")
	public Integer getPageOrderNo() {
		return pageOrderNo;
	}

	public void setPageOrderNo(Integer pageOrderNo) {
		this.pageOrderNo = pageOrderNo;
	}


	@Column(name="name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof VariablePK)) {
			return false;
		}
		VariablePK castOther = (VariablePK)other;
		return
				this.monitorId.equals(castOther.monitorId)
				&& this.pageOrderNo.equals(castOther.pageOrderNo)
				&& this.name.equals(castOther.name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.monitorId.hashCode();
		hash = hash * prime + this.pageOrderNo.hashCode();
		hash = hash * prime + this.name.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"monitorId",
				"pageOrderNo",
				"name"
		};
		Object[] values = {
				this.monitorId,
				this.pageOrderNo,
				this.name
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}
