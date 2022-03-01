/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;
import java.util.Arrays;

import jakarta.persistence.*;

/**
 * The primary key class for the cc_cfg_node_process database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@Embeddable
public class NodeProcessInfoPK implements Serializable, Cloneable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String facilityId = "";
	private String processName = "";
	private Integer pid = -1;

	public NodeProcessInfoPK() {
	}

	public NodeProcessInfoPK(String facilityId, String processName, Integer pid) {
		this.setFacilityId(facilityId);
		this.setProcessName(processName);
		this.setPid(pid);
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="process_name")
	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	@Column(name="pid")
	public Integer getPid() {
		return pid;
	}

	public void setPid(Integer pid) {
		this.pid = pid;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof NodeProcessInfoPK)) {
			return false;
		}
		NodeProcessInfoPK castOther = (NodeProcessInfoPK)other;
		return
				this.facilityId.equals(castOther.facilityId)
				&& this.processName.equals(castOther.processName)
				&& this.pid.equals(castOther.pid);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.facilityId.hashCode();
		hash = hash * prime + this.processName.hashCode();
		hash = hash * prime + this.pid.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"facilityId",
				"processName",
				"pid"
		};
		String[] values = {
				this.facilityId,
				this.processName,
				this.pid.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
	
	@Override
	public NodeProcessInfoPK clone() {
		try {
			NodeProcessInfoPK cloneInfo = (NodeProcessInfoPK)super.clone();
			cloneInfo.facilityId = this.facilityId;
			cloneInfo.processName = this.processName;
			cloneInfo.pid = this.pid;
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
}