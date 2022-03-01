/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.model;

import java.io.Serializable;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;



@Entity
@Table(name="cc_infra_file_content", schema="binarydata")
@AttributeOverride(name="objectId", column=@Column(name="file_id", insertable=false, updatable=false))
@Cacheable(false)
public class InfraFileContentEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String fileId;
	private byte[] fileContent;
	
	public InfraFileContentEntity() {
	}

	@Id
	@Column(name="file_id")
	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	@Column(name="file_content")
	public byte[] getFileContent() {
		return fileContent;
	}

	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}
}