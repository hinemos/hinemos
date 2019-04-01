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

import javax.persistence.*;

/**
 * The primary key class for the cc_node_config_setting_item_info database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@Embeddable
public class NodeConfigSettingItemInfoPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String settingId;
	private String settingItemId;

	public NodeConfigSettingItemInfoPK() {
	}

	public NodeConfigSettingItemInfoPK(String settingId, String settingItemId) {
		this.setSettingId(settingId);
		this.setSettingItemId(settingItemId);
	}

	@Column(name="setting_id")
	public String getSettingId() {
		return this.settingId;
	}
	public void setSettingId(String settingId) {
		this.settingId = settingId;
	}

	@Column(name="setting_item_id")
	public String getSettingItemId() {
		return this.settingItemId;
	}
	public void setSettingItemId(String settingItemId) {
		this.settingItemId = settingItemId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof NodeConfigSettingItemInfoPK)) {
			return false;
		}
		NodeConfigSettingItemInfoPK castOther = (NodeConfigSettingItemInfoPK)other;
		return
				this.settingId.equals(castOther.settingId)
				&& this.settingItemId.equals(castOther.settingItemId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.settingId.hashCode();
		hash = hash * prime + this.settingItemId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"settingId",
				"settingItemId"
		};
		String[] values = {
				this.settingId,
				this.settingItemId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}