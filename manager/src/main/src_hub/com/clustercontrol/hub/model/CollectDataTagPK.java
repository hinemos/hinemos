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
public class CollectDataTagPK implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Long collectId;
	private Long dataId;
	private String key;

	public CollectDataTagPK() {
	}

	public CollectDataTagPK(Long collectId, Long dataId, String key) {
		this.setCollectId(collectId);
		this.setDataId(dataId);
		this.setKey(key);
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

	@Column(name="tag_key")
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
}