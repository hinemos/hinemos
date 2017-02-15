package com.clustercontrol.accesscontrol.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;



/**
 * The persistent class for the cc_object_privilege database table.
 * 
 */
@XmlType(namespace = "http://access.ws.clustercontrol.com")

@Entity
@Table(name="cc_object_privilege", schema="setting")
@Cacheable(true)
public class ObjectPrivilegeInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private ObjectPrivilegeInfoPK id;
	private Long createDate = 0l;
	private String createUserId = "";
	private Long modifyDate = 0l;
	private String modifyUserId = "";

	public ObjectPrivilegeInfo() {
	}

	public ObjectPrivilegeInfo(ObjectPrivilegeInfoPK pk) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}

	public ObjectPrivilegeInfo(String objectType, String objectId, String userId, String objectPrivilege) {
		this(new ObjectPrivilegeInfoPK(objectType, objectId, userId, objectPrivilege));
	}
	
	@XmlTransient
	@EmbeddedId
	public ObjectPrivilegeInfoPK getId() {
		if (this.id == null)
			this.id = new ObjectPrivilegeInfoPK();
		return this.id;
	}

	public void setId(ObjectPrivilegeInfoPK id) {
		this.id = id;
	}

	@Transient
	public String getRoleId() {
		return getId().getRoleId();
	}
	public void setRoleId(String roleId) {
		getId().setRoleId(roleId);
	}
	
	@Transient
	public String getObjectType() {
		return getId().getObjectType();
	}
	public void setObjectType(String objectType) {
		getId().setObjectType(objectType);
	}
	
	@Transient
	public String getObjectId() {
		return getId().getObjectId();
	}
	public void setObjectId(String objectId) {
		getId().setObjectId(objectId);
	}
	
	@Transient
	public String getObjectPrivilege() {
		return getId().getObjectPrivilege();
	}
	public void setObjectPrivilege(String objectPrivilege) {
		getId().setObjectPrivilege(objectPrivilege);
	}
	
	@Column(name="create_datetime")
	public Long getCreateDate() {
		return this.createDate;
	}

	public void setCreateDate(Long createDatetime) {
		this.createDate = createDatetime;
	}


	@Column(name="create_user_id")
	public String getCreateUserId() {
		return this.createUserId;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}


	@Column(name="modify_datetime")
	public Long getModifyDate() {
		return this.modifyDate;
	}

	public void setModifyDate(Long modifyDatetime) {
		this.modifyDate = modifyDatetime;
	}


	@Column(name="modify_user_id")
	public String getModifyUserId() {
		return this.modifyUserId;
	}

	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
	}
}