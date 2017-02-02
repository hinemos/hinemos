package com.clustercontrol.nodemap.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;



/**
 * The persistent class for the cc_map_bg_image database table.
 * 
 */
@Entity
@Table(name="cc_map_bg_image", schema="binarydata")
@Cacheable(true)
public class MapBgImageEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String filename;
	private byte[] filedata;
	private List<MapInfoEntity> mapInfoEntities;

	@Deprecated
	public MapBgImageEntity() {
	}

	public MapBgImageEntity(String filename) {
		this.setFilename(filename);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}


	@Id
	public String getFilename() {
		return this.filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}


	public byte[] getFiledata() {
		return this.filedata;
	}

	public void setFiledata(byte[] filedata) {
		this.filedata = filedata;
	}


	//bi-directional many-to-one association to MapInfoEntity
	@OneToMany(mappedBy="mapBgImageEntity", fetch=FetchType.LAZY)
	public List<MapInfoEntity> getMapInfoEntities() {
		return this.mapInfoEntities;
	}

	public void setMapInfoEntities(List<MapInfoEntity> mapInfoEntities) {
		this.mapInfoEntities = mapInfoEntities;
	}

}