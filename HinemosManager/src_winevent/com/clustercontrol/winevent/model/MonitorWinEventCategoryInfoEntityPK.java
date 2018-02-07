/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.winevent.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The primary key class for the cc_monitor_winevent_category_info database table.
 * 
 */
@Embeddable
public class MonitorWinEventCategoryInfoEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private Integer category;

	public MonitorWinEventCategoryInfoEntityPK() {
	}

	public MonitorWinEventCategoryInfoEntityPK(String monitorId, Integer category) {
		this.setMonitorId(monitorId);
		this.setCategory(category);
	}

	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	@Column(name="category")
	public Integer getCategory(){
		return this.category;
	}
	public void setCategory(Integer category){
		this.category = category;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MonitorWinEventCategoryInfoEntityPK)) {
			return false;
		}
		MonitorWinEventCategoryInfoEntityPK castOther = (MonitorWinEventCategoryInfoEntityPK)other;
		return
				this.monitorId.equals(castOther.monitorId)
				&& this.category.equals(castOther.category);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.monitorId.hashCode();
		hash = hash * prime + this.category.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"monitorId",
				"eventId"
		};
		String[] values = {
				this.monitorId,
				this.category.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}