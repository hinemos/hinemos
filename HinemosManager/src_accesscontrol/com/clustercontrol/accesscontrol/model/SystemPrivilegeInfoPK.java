package com.clustercontrol.accesscontrol.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_object_privilege database table.
 * 
 */
@Embeddable
public class SystemPrivilegeInfoPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String systemFunction;
	private String systemPrivilege;

	public SystemPrivilegeInfoPK() {
	}

	public SystemPrivilegeInfoPK(String systemFunction, String systemPrivilege) {
		this.setSystemFunction(systemFunction);
		this.setSystemPrivilege(systemPrivilege);
	}

	@Column(name="system_function")
	public String getSystemFunction() {
		return this.systemFunction;
	}
	public void setSystemFunction(String systemFunction) {
		this.systemFunction = systemFunction;
	}

	@Column(name="system_privilege")
	public String getSystemPrivilege() {
		return this.systemPrivilege;
	}
	public void setSystemPrivilege(String systemPrivilege) {
		this.systemPrivilege = systemPrivilege;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SystemPrivilegeInfoPK)) {
			return false;
		}
		SystemPrivilegeInfoPK castOther = (SystemPrivilegeInfoPK)other;
		return
				this.systemFunction.equals(castOther.systemFunction)
				&& this.systemPrivilege.equals(castOther.systemPrivilege);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.systemFunction.hashCode();
		hash = hash * prime + this.systemPrivilege.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"systemFunction",
				"systemPrivilege"
		};
		String[] values = {
				this.systemFunction,
				this.systemPrivilege
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}