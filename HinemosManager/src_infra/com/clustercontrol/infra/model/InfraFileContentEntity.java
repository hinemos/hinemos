package com.clustercontrol.infra.model;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


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
	
	public InfraFileContentEntity(String fileId) {
		this.fileId = fileId;
		
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
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