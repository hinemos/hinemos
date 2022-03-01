/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.monitor.model;

import java.io.Serializable;
import java.util.Arrays;

import jakarta.persistence.*;

/**
 * The primary key class for the cc_collector_category_collect_mst database table.
 * 
 */
@Embeddable
public class CollectorCategoryCollectMstEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String platformId;
	private String subPlatformId;
	private String categoryCode;

	public CollectorCategoryCollectMstEntityPK() {
	}

	public CollectorCategoryCollectMstEntityPK(String platformId,
			String subPlatformId,
			String categoryCode) {
		this.setPlatformId(platformId);
		this.setSubPlatformId(subPlatformId);
		this.setCategoryCode(categoryCode);
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

	@Column(name="category_code")
	public String getCategoryCode() {
		return this.categoryCode;
	}
	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CollectorCategoryCollectMstEntityPK)) {
			return false;
		}
		CollectorCategoryCollectMstEntityPK castOther = (CollectorCategoryCollectMstEntityPK)other;
		return
				this.platformId.equals(castOther.platformId)
				&& this.subPlatformId.equals(castOther.subPlatformId)
				&& this.categoryCode.equals(castOther.categoryCode);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.platformId.hashCode();
		hash = hash * prime + this.subPlatformId.hashCode();
		hash = hash * prime + this.categoryCode.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"platformId",
				"subPlatformId",
				"categoryCode"
		};
		String[] values = {
				this.platformId,
				this.subPlatformId,
				this.categoryCode
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}