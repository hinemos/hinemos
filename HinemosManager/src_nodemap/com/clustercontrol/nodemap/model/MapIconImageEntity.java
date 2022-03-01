/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


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
	}


	@Id
	@Column(name="filename")
	public String getFilename() {
		return this.filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}


	@Column(name="filedata")
	public byte[] getFiledata() {
		return this.filedata;
	}

	public void setFiledata(byte[] filedata) {
		this.filedata = filedata;
	}

}