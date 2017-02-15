package com.clustercontrol.accesscontrol.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_object_privilege database table.
 * 
 */
@Embeddable
public class ObjectPrivilegeInfoPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String objectType;
	private String objectId;
	private String roleId;
	private String objectPrivilege;

	public ObjectPrivilegeInfoPK() {
	}

	public ObjectPrivilegeInfoPK(String objectType, String objectId, String roleId, String objectPrivilege) {
		this.setObjectType(objectType);
		this.setObjectId(objectId);
		this.setRoleId(roleId);
		this.setObjectPrivilege(objectPrivilege);
	}

	@Column(name="object_type")
	public String getObjectType() {
		return this.objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	@Column(name="object_id")
	public String getObjectId() {
		return this.objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	@Column(name="role_id")
	public String getRoleId() {
		return this.roleId;
	}
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	@Column(name="object_privilege")
	public String getObjectPrivilege() {
		return this.objectPrivilege;
	}
	public void setObjectPrivilege(String objectPrivilege) {
		this.objectPrivilege = objectPrivilege;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ObjectPrivilegeInfoPK)) {
			return false;
		}
		ObjectPrivilegeInfoPK castOther = (ObjectPrivilegeInfoPK)other;
		return
				this.objectType.equals(castOther.objectType)
				&& this.objectId.equals(castOther.objectId)
				&& this.roleId.equals(castOther.roleId)
				&& this.objectPrivilege.equals(castOther.objectPrivilege);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.objectType.hashCode();
		hash = hash * prime + this.objectId.hashCode();
		hash = hash * prime + this.roleId.hashCode();
		hash = hash * prime + this.objectPrivilege.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"objectType",
				"objectId",
				"roleId",
				"objectPrivilege"
		};
		String[] values = {
				this.objectType,
				this.objectId,
				this.roleId,
				this.objectPrivilege
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}