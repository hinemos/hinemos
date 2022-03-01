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
 * The primary key class for the cc_cfg_node_package database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@Embeddable
public class NodeCustomInfoPK implements Serializable, Cloneable {
	// default serial version id, required for serializable classes.
	/** シリアルバージョンUID(カラム構成変更時にアップ). */
	private static final long serialVersionUID = 1L;

	/** ファシリティID. */
	private String facilityId;

	/** 親の対象構成情報ID. */
	private String settingId;

	/** ユーザ任意情報ID. */
	private String settingCustomId;

	// コンストラクタ
	/** コンストラクタ：空. */
	public NodeCustomInfoPK() {
	}

	/** コンストラクタ：主キー指定. */
	public NodeCustomInfoPK(String facilityId, String settingId, String settingCustomId) {
		this.setFacilityId(facilityId);
		this.setSettingId(settingId);
		this.setSettingCustomId(settingCustomId);
	}

	/** ファシリティID. */
	@Column(name = "facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}

	/** ファシリティID. */
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	/** 親の対象構成情報ID. */
	@Column(name = "setting_id")
	public String getSettingId() {
		return this.settingId;
	}

	/** 親の対象構成情報ID. */
	public void setSettingId(String settingId) {
		this.settingId = settingId;
	}

	/** ユーザ任意情報ID. */
	@Column(name = "setting_custom_id")
	public String getSettingCustomId() {
		return this.settingCustomId;
	}

	/** ユーザ任意情報ID. */
	public void setSettingCustomId(String settingCustomId) {
		this.settingCustomId = settingCustomId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof NodeCustomInfoPK)) {
			return false;
		}
		NodeCustomInfoPK castOther = (NodeCustomInfoPK)other;
		return
				this.facilityId.equals(castOther.facilityId)
				&& this.settingId.equals(castOther.settingId)
				&& this.settingCustomId.equals(castOther.settingCustomId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.facilityId.hashCode();
		hash = hash * prime + this.settingId.hashCode();
		hash = hash * prime + this.settingCustomId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"facilityId",
				"settingId",
				"settingCustomId"
		};
		String[] values = {
				this.facilityId,
				this.settingId,
				this.settingCustomId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
	
	@Override
	public NodeCustomInfoPK clone() {
		try {
			NodeCustomInfoPK cloneInfo = (NodeCustomInfoPK)super.clone();
			cloneInfo.facilityId = this.facilityId;
			cloneInfo.settingId = this.settingId;
			cloneInfo.settingCustomId = this.settingCustomId;
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
}