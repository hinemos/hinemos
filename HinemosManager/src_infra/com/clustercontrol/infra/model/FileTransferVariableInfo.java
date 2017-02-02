package com.clustercontrol.infra.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;

@XmlType(namespace = "http://infra.ws.clustercontrol.com")
@Entity
@Table(name="cc_infra_file_transfer_variable_info", schema="setting")
@Cacheable(true)
public class FileTransferVariableInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private FileTransferVariableInfoPK id;
	private String value;
	private FileTransferModuleInfo fileTransferModuleInfoEntity;
	
	public FileTransferVariableInfo() {
	}
	
	public FileTransferVariableInfo(FileTransferModuleInfo parent, String name) {
		this.setId(new FileTransferVariableInfoPK(parent.getId().getManagementId(), parent.getId().getModuleId(), name));
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		setFileTransferModuleInfoEntity(parent);
		parent.getFileTransferVariableList().add(this);
	}
	
	@EmbeddedId
	public FileTransferVariableInfoPK getId() {
		if (id == null)
			id = new FileTransferVariableInfoPK();
		return id;
	}
	public void setId(FileTransferVariableInfoPK id) {
		this.id = id;
	}
	
	@XmlTransient
	@Transient
	public String getManagementId() {
		return getId().getManagementId();
	}
	public void setManagementId(String managementId) {
		getId().setManagementId(managementId);
	}
	
	@XmlTransient
	@Transient
	public String getModuleId() {
		return getId().getModuleId();
	}
	public void setModuleId(String moduleId) {
		getId().setModuleId(moduleId);
	}
	
	@Transient
	public String getName() {
		return getId().getName();
	}
	public void setName(String name) {
		getId().setName(name);
	}

	@Column(name="value")
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	//bi-directional many-to-one association to FileTransferModuleInfoEntity
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="management_id", referencedColumnName="management_id", insertable=false, updatable=false),
		@JoinColumn(name="module_id", referencedColumnName="module_id", insertable=false, updatable=false)
	})
	public FileTransferModuleInfo getFileTransferModuleInfoEntity() {
		return this.fileTransferModuleInfoEntity;
	}
	
	@Deprecated
	public void setFileTransferModuleInfoEntity(FileTransferModuleInfo fileTransferModuleInfoEntity) {
		this.fileTransferModuleInfoEntity = fileTransferModuleInfoEntity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileTransferVariableInfo other = (FileTransferVariableInfo) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FileTransferVariableInfoEntity [id=" + id + ", value=" + value + "]";
	}
	
//	@Override
//	public FileTransferVariableInfo clone() {
//		try {
//			FileTransferVariableInfo info = (FileTransferVariableInfo)super.clone();
//			info.name = this.name;
//			info.value = this.value;
//			return info;
//		} catch (CloneNotSupportedException e) {
//			throw new InternalError(e.toString());
//		}
//	}
}
