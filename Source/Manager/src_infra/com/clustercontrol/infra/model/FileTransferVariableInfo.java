/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

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

	/**
	 * FileTransferModuleInfoオブジェクト参照設定<BR>
	 * 
	 * FileTransferModuleInfo設定時はSetterに代わりこちらを使用すること。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void relateToFileTransferModuleInfo(FileTransferModuleInfo fileTransferModuleInfoEntity) {
		this.setFileTransferModuleInfoEntity(fileTransferModuleInfoEntity);
		if (fileTransferModuleInfoEntity != null) {
			List<FileTransferVariableInfo> list = fileTransferModuleInfoEntity.getFileTransferVariableList();
			if (list == null) {
				list = new ArrayList<FileTransferVariableInfo>();
			} else {
				for (FileTransferVariableInfo entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			fileTransferModuleInfoEntity.setFileTransferVariableList(list);
		}
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
}
