/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import com.clustercontrol.util.HinemosTime;

/**
 * The primary key class for the cc_node_package_history_detail database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@Embeddable
public class NodePackageHistoryDetailPK implements Serializable {

	// default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String facilityId = "";
	private Long regDate = HinemosTime.currentTimeMillis();
	private String packageId = "";

	/**
	 * デフォルト値生成コンストラクタ.
	 */
	public NodePackageHistoryDetailPK() {
	}

	/**
	 * キー指定コンストラクタ.
	 */
	public NodePackageHistoryDetailPK(String facilityId, String packageId, Long regDate) {
		this.setFacilityId(facilityId);
		this.setRegDate(regDate);
		this.setPackageId(packageId);
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

	@Column(name = "package_id")
	public String getPackageId() {
		return packageId;
	}

	public void setPackageId(String packageId) {
		this.packageId = packageId;
	}
}
