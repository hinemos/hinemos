/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;

import javax.persistence.*;

/**
 * The primary key class for the cc_node_config_custom_setting_info database
 * table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@Embeddable
public class NodeConfigCustomInfoPK implements Serializable {

	/** シリアルバージョンUID(カラム構成変更時にアップ). */
	private static final long serialVersionUID = 1L;

	/** 親の対象構成情報ID. */
	private String settingId;

	/** ユーザ任意情報ID. */
	private String settingCustomId;

	/** 空のコンストラクタ. */
	public NodeConfigCustomInfoPK() {
	}

	/** キー指定コンストラクタ. */
	public NodeConfigCustomInfoPK(String settingId, String settingCustomId) {
		this.setSettingId(settingId);
		this.setSettingCustomId(settingCustomId);
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
		if (!(other instanceof NodeConfigCustomInfoPK)) {
			return false;
		}
		NodeConfigCustomInfoPK castOther = (NodeConfigCustomInfoPK) other;
		return this.settingId.equals(castOther.settingId) && this.settingCustomId.equals(castOther.settingCustomId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.settingId.hashCode();
		hash = hash * prime + this.settingCustomId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String returnString = "NodeConfigCustomInfoPK [" + //
				"settingId=" + settingId //
				+ ", " + "settingCustomId=" + settingCustomId //
				+ "]";
		return returnString;
	}
}