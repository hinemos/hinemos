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

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import com.clustercontrol.util.HinemosTime;

/**
 * The primary key class for the cc_node_XXX_history database table.
 * 
 *
 * @version 6.2.0
 * @since 6.2.0
 */
@Embeddable
public class NodeHistoryPK implements Serializable, Cloneable {

	// default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	protected String facilityId = "";
	protected Long regDate = HinemosTime.currentTimeMillis();

	/**
	 * デフォルト値生成コンストラクタ.
	 */
	public NodeHistoryPK() {
	}

	/**
	 * キー指定コンストラクタ.
	 */
	public NodeHistoryPK(String facilityId, Long regDate) {
		this.setFacilityId(facilityId);
		this.setRegDate(regDate);
	}

	@Column(name = "facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name = "reg_date")
	public Long getRegDate() {
		return this.regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((facilityId == null) ? 0 : facilityId.hashCode());
		result = prime * result + ((regDate == null) ? 0 : regDate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof NodeHistoryPK)) {
			return false;
		}
		NodeHistoryPK castOther = (NodeHistoryPK)obj;
		return
			this.facilityId.equals(castOther.facilityId)
			&& this.regDate.equals(castOther.regDate);
	}

	@Override
	public String toString() {
		String[] names = { "facilityId", "regDate" };
		String[] values = { this.facilityId, this.regDate.toString() };
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
	
	@Override
	public NodeHistoryPK clone() {
		try {
			NodeHistoryPK cloneInfo = (NodeHistoryPK)super.clone();
			cloneInfo.facilityId = this.facilityId;
			cloneInfo.regDate = this.regDate;

			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
}
