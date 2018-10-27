/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.process.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_monitor_process_method_mst database table.
 * 
 */
@Embeddable
public class MonitorProcessMethodMstEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String platformId;
	private String subPlatformId;

	public MonitorProcessMethodMstEntityPK() {
	}

	public MonitorProcessMethodMstEntityPK(String platformId, String subPlatformId) {
		this.setPlatformId(platformId);
		this.setSubPlatformId(subPlatformId);
	}

	@Column(name="platform_id")
	public String getPlatformId() {
		return this.platformId;
	}
	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	@Column(name="sub_platform_id")
	public String getSubPlatformId() {
		return this.subPlatformId;
	}
	public void setSubPlatformId(String subPlatformId) {
		this.subPlatformId = subPlatformId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MonitorProcessMethodMstEntityPK)) {
			return false;
		}
		MonitorProcessMethodMstEntityPK castOther = (MonitorProcessMethodMstEntityPK)other;
		return
				this.platformId.equals(castOther.platformId)
				&& this.subPlatformId.equals(castOther.subPlatformId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.platformId.hashCode();
		hash = hash * prime + this.subPlatformId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"platformId",
				"subPlatformId"
		};
		String[] values = {
				this.platformId,
				this.subPlatformId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}