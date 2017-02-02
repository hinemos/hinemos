package com.clustercontrol.nodemap.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_map_icon_image database table.
 * 
 */
@Entity
@Table(name="cc_map_icon_image", schema="binarydata")
@Cacheable(true)
public class MapIconImageEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String filename;
	private byte[] filedata;

	@Deprecated
	public MapIconImageEntity() {
	}

	public MapIconImageEntity(String filename) {
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

}