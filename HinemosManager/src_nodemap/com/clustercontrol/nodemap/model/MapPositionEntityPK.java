/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.model;

import java.io.Serializable;
import java.util.Arrays;

import jakarta.persistence.*;

/**
 * The primary key class for the cc_map_position database table.
 * 
 */
@Embeddable
public class MapPositionEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String mapId;
	private String elementId;

	public MapPositionEntityPK() {
	}

	public MapPositionEntityPK(String mapId, String elementId) {
		this.setMapId(mapId);
		this.setElementId(elementId);
	}

	@Column(name="map_id")
	public String getMapId() {
		return this.mapId;
	}
	public void setMapId(String mapId) {
		this.mapId = mapId;
	}

	@Column(name="element_id")
	public String getElementId() {
		return this.elementId;
	}
	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MapPositionEntityPK)) {
			return false;
		}
		MapPositionEntityPK castOther = (MapPositionEntityPK)other;
		return
				this.mapId.equals(castOther.mapId)
				&& this.elementId.equals(castOther.elementId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.mapId.hashCode();
		hash = hash * prime + this.elementId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"mapId",
				"elementId"
		};
		String[] values = {
				this.mapId,
				this.elementId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}