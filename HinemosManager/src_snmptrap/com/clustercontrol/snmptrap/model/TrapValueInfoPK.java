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
package com.clustercontrol.snmptrap.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Embeddable;


/**
 * The primary key class for the cc_monitor_trap_value_info database table.
 * 
 */
@Embeddable
public class TrapValueInfoPK implements Serializable {
	private static final long serialVersionUID = 1L;

	private String monitorId;
	private String mib;
	private String trapOid;
	private Integer genericId;
	private Integer specificId;


	public TrapValueInfoPK() {
	}

	public TrapValueInfoPK(String monitorId, String mib, String trapOid, Integer genericId, Integer specificId) {
		this.setMonitorId(monitorId);
		this.setMib(mib);
		this.setTrapOid(trapOid);
		this.setGenericId(genericId);
		this.setSpecificId(specificId);
	}

	@Column(name="monitor_id")
	public String getMonitorId() {
		return monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	@Column(name="mib")
	public String getMib() {
		return mib;
	}
	public void setMib(String mib) {
		this.mib = mib;
	}

	@Column(name="trap_oid")
	public String getTrapOid() {
		return trapOid;
	}
	public void setTrapOid(String trapOid) {
		this.trapOid = trapOid;
	}

	@Column(name="generic_id")
	public Integer getGenericId() {
		return genericId;
	}
	public void setGenericId(Integer genericId) {
		this.genericId = genericId;
	}

	@Column(name="specific_id")
	public Integer getSpecificId() {
		return specificId;
	}
	public void setSpecificId(Integer specificId) {
		this.specificId = specificId;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TrapValueInfoPK other = (TrapValueInfoPK) obj;
		if (genericId == null) {
			if (other.genericId != null)
				return false;
		} else if (!genericId.equals(other.genericId))
			return false;
		if (mib == null) {
			if (other.mib != null)
				return false;
		} else if (!mib.equals(other.mib))
			return false;
		if (monitorId == null) {
			if (other.monitorId != null)
				return false;
		} else if (!monitorId.equals(other.monitorId))
			return false;
		if (specificId == null) {
			if (other.specificId != null)
				return false;
		} else if (!specificId.equals(other.specificId))
			return false;
		if (trapOid == null) {
			if (other.trapOid != null)
				return false;
		} else if (!trapOid.equals(other.trapOid))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((genericId == null) ? 0 : genericId.hashCode());
		result = prime * result + ((mib == null) ? 0 : mib.hashCode());
		result = prime * result
				+ ((monitorId == null) ? 0 : monitorId.hashCode());
		result = prime * result
				+ ((specificId == null) ? 0 : specificId.hashCode());
		result = prime * result + ((trapOid == null) ? 0 : trapOid.hashCode());
		return result;
	}

	@Override
	public String toString() {
		String[] names = {
				"monitorId",
				"mib",
				"trapOid",
				"genericId",
				"specificId"
		};
		Object[] values = {
				this.monitorId,
				this.mib,
				this.trapOid,
				this.genericId,
				this.specificId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}
