/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

@Embeddable
public class CollectDataPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private Integer collectorid;
	private Long time;

	public CollectDataPK() {
	}

	public CollectDataPK(Integer collectorid, Long time) {
		this.setCollectorid(collectorid);
		this.setTime(time);
	}

	@Column(name="collector_id")
	public Integer getCollectorid() {
		return this.collectorid;
	}
	public void setCollectorid(Integer collectorid) {
		this.collectorid = collectorid;
	}

	@Column(name="time")
	public Long getTime(){
		return this.time;
	}
	public void setTime(Long time){
		this.time = time;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CollectDataPK)) {
			return false;
		}
		CollectDataPK castOther = (CollectDataPK)other;
		return
				this.collectorid.equals(castOther.collectorid)
				&& this.time.equals(castOther.time);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.collectorid.hashCode();
		hash = hash * prime + this.time.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"collectorId",
				"time"
		};
		String[] values = {
				this.collectorid.toString(),
				this.time.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}