package com.clustercontrol.nodemap.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


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
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}

	public MapAssociationEntity(String mapId, String source, String target) {
		this(new MapAssociationEntityPK(mapId, source, target));
	}


	@EmbeddedId
	public MapAssociationEntityPK getId() {
		return this.id;
	}

	public void setId(MapAssociationEntityPK id) {
		this.id = id;
	}

}