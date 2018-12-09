/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_cfg_node_hostname database table.
 * 
 */
@Embeddable
public class NodeHostnameInfoPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String facilityId;
	private String hostname;

	public NodeHostnameInfoPK() {
	}

	public NodeHostnameInfoPK(String facilityId, String hostname) {
		this.setFacilityId(facilityId);
		this.setHostname(hostname);
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="hostname")
	public String getHostname() {
		return this.hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof NodeHostnameInfoPK)) {
			return false;
		}
		NodeHostnameInfoPK castOther = (NodeHostnameInfoPK)other;
		return
				this.facilityId.equals(castOther.facilityId)
				&& this.hostname.equals(castOther.hostname);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.facilityId.hashCode();
		hash = hash * prime + this.hostname.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"facilityId",
				"hostname"
		};
		String[] values = {
				this.facilityId,
				this.hostname
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}