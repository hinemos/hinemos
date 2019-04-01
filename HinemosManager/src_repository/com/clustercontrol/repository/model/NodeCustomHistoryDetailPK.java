/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.clustercontrol.util.HinemosTime;

/**
 * The primary key class for the cc_node_package_history_detail database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@Embeddable
public class NodeCustomHistoryDetailPK implements Serializable {

	// default serial version id, required for serializable classes.
	/** シリアルバージョンUID(カラム構成変更時にアップ). */
	private static final long serialVersionUID = 1L;

	/** ファシリティID. */
	private String facilityId = "";

	/** 収集日時. */
	private Long regDate = HinemosTime.currentTimeMillis();

	/** 親の対象構成情報ID. */
	private String settingId;

	/** ユーザ任意情報ID. */
	private String settingCustomId;

	/**
	 * デフォルト値生成コンストラクタ.
	 */
	public NodeCustomHistoryDetailPK() {
	}

	/**
	 * キー指定コンストラクタ.
	 */
	public NodeCustomHistoryDetailPK(String facilityId, Long regDate, String settingId, String settingCustomId) {
		this.setFacilityId(facilityId);
		this.setRegDate(regDate);
		this.setSettingId(settingId);
		this.setSettingCustomId(settingCustomId);
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
}
