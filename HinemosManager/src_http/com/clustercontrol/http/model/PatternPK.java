/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.http.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Embeddable;


/**
 * The primary key class for the cc_monitor_http_scenario_pattern_info database table.
 * 
 */
@Embeddable
public class PatternPK implements Serializable {
	private static final long serialVersionUID = 1L;

	private String monitorId;
	private Integer pageOrderNo;
	private Integer patternOrderNo;

	public PatternPK() {
	}

	public PatternPK(String monitorId,
			Integer pageOrderNo,
			Integer patternOrderNo) {
		this.setMonitorId(monitorId);
		this.setPageOrderNo(pageOrderNo);
		this.setPatternOrderNo(patternOrderNo);
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


	@Column(name="pattern_order_no")
	public Integer getPatternOrderNo() {
		return patternOrderNo;
	}

	public void setPatternOrderNo(Integer patternOrderNo) {
		this.patternOrderNo = patternOrderNo;
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PatternPK)) {
			return false;
		}
		PatternPK castOther = (PatternPK)other;
		return
				this.monitorId.equals(castOther.monitorId)
				&& this.pageOrderNo.equals(castOther.pageOrderNo)
				&& this.patternOrderNo.equals(castOther.patternOrderNo);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.monitorId.hashCode();
		hash = hash * prime + this.pageOrderNo.hashCode();
		hash = hash * prime + this.patternOrderNo.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"monitorId",
				"pageOrderNo",
				"patternOrderNo"
		};
		Object[] values = {
				this.monitorId,
				this.pageOrderNo,
				this.patternOrderNo
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}
