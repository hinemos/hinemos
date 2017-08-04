package com.clustercontrol.nodemap.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_map_association database table.
 * 
 */
@Embeddable
public class MapAssociationEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String mapId;
	private String source;
	private String target;

	public MapAssociationEntityPK() {
	}

	public MapAssociationEntityPK(String mapId, String source, String target) {
		this.setMapId(mapId);
		this.setSource(source);
		this.setTarget(target);
	}

	@Column(name="map_id")
	public String getMapId() {
		return this.mapId;
	}
	public void setMapId(String mapId) {
		this.mapId = mapId;
	}

	@Column(name="source")
	public String getSource() {
		return this.source;
	}
	public void setSource(String source) {
		this.source = source;
	}

	@Column(name="target")
	public String getTarget() {
		return this.target;
	}
	public void setTarget(String target) {
		this.target = target;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MapAssociationEntityPK)) {
			return false;
		}
		MapAssociationEntityPK castOther = (MapAssociationEntityPK)other;
		return
				this.mapId.equals(castOther.mapId)
				&& this.source.equals(castOther.source)
				&& this.target.equals(castOther.target);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.mapId.hashCode();
		hash = hash * prime + this.source.hashCode();
		hash = hash * prime + this.target.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"mapId",
				"source",
				"target"
		};
		String[] values = {
				this.mapId,
				this.source,
				this.target
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}