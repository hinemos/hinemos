/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.ent.bean;

/**
 * データソースとの関連性を指し示すファシリティIDとディスプレイ名識別クラス
 *
 * @version 5.0.a
 * @since 5.0.a
 */
public class DataKey {
	
	private String facilityId;
	private String displayName;
	
	public DataKey(String facilityId, String displayName) {
		super();
		this.facilityId = facilityId;
		this.displayName = displayName;
	}
	
	public DataKey(String facilityId) {
		super();
		this.facilityId = facilityId;
	}
	
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}
