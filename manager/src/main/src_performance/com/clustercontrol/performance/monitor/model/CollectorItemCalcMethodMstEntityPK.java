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

import javax.persistence.*;

/**
 * The primary key class for the cc_collector_item_calc_method_mst database table.
 * 
 */
@Embeddable
public class CollectorItemCalcMethodMstEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String collectMethod;
	private String platformId;
	private String subPlatformId;
	private String itemCode;

	public CollectorItemCalcMethodMstEntityPK() {
	}

	public CollectorItemCalcMethodMstEntityPK(String collectMethod,
			String platformId,
			String subPlatformId,
			String itemCode) {
		this.setCollectMethod(collectMethod);
		this.setPlatformId(platformId);
		this.setSubPlatformId(subPlatformId);
		this.setItemCode(itemCode);
	}

	@Column(name="collect_method")
	public String getCollectMethod() {
		return this.collectMethod;
	}
	public void setCollectMethod(String collectMethod) {
		this.collectMethod = collectMethod;
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

	@Column(name="item_code")
	public String getItemCode() {
		return this.itemCode;
	}
	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CollectorItemCalcMethodMstEntityPK)) {
			return false;
		}
		CollectorItemCalcMethodMstEntityPK castOther = (CollectorItemCalcMethodMstEntityPK)other;
		return
				this.collectMethod.equals(castOther.collectMethod)
				&& this.platformId.equals(castOther.platformId)
				&& this.subPlatformId.equals(castOther.subPlatformId)
				&& this.itemCode.equals(castOther.itemCode);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.collectMethod.hashCode();
		hash = hash * prime + this.platformId.hashCode();
		hash = hash * prime + this.subPlatformId.hashCode();
		hash = hash * prime + this.itemCode.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"collectMethod",
				"platformId",
				"subPlatformId",
				"itemCode"
		};
		String[] values = {
				this.collectMethod,
				this.platformId,
				this.subPlatformId,
				this.itemCode
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}