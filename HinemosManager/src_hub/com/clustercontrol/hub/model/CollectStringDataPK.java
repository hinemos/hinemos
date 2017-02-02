/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.hub.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class CollectStringDataPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private Long collectId;
	private Long dataId;

	public CollectStringDataPK() {
	}

	public CollectStringDataPK(Long collectId, Long dataId) {
		this.setCollectId(collectId);
		this.setDataId(dataId);
	}

	@Column(name="collect_id")
	public Long getCollectId() {
		return this.collectId;
	}
	public void setCollectId(Long collectId) {
		this.collectId = collectId;
	}
	
	@Column(name="data_id")
	public Long getDataId() {
		return this.dataId;
	}
	public void setDataId(Long dataId) {
		this.dataId = dataId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CollectStringDataPK)) {
			return false;
		}
		CollectStringDataPK castOther = (CollectStringDataPK)other;
		return
				this.collectId.equals(castOther.collectId)
				&& this.dataId.equals(castOther.dataId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.collectId.hashCode();
		hash = hash * prime + this.dataId.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		return "CollectStringDataPK [collectId=" + collectId + ", dataId=" + dataId + "]";
	}
}