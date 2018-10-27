/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * The persistent class for the cc_map_association database table.
 * 
 */
@Entity
@Table(name="cc_map_association", schema="setting")
@Cacheable(true)
public class MapAssociationEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private MapAssociationEntityPK id;

	@Deprecated
	public MapAssociationEntity() {
	}

	public MapAssociationEntity(MapAssociationEntityPK pk) {
		this.setId(pk);
	}


	@EmbeddedId
	public MapAssociationEntityPK getId() {
		return this.id;
	}

	public void setId(MapAssociationEntityPK id) {
		this.id = id;
	}

}