/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmptrap.entity;

/**
 * Primary key for SnmpTrapMaster.
 * 
 * @version 6.1.0
 * @since 4.0.1
 */
public class SnmpTrapMasterPK extends java.lang.Object implements java.io.Serializable {

	private static final long serialVersionUID = -4568812097277566165L;

	public java.lang.String mib;
	public java.lang.String trapOid;
	public java.lang.Integer genericId;
	public java.lang.Integer specificId;

	public SnmpTrapMasterPK() {
	}

	public SnmpTrapMasterPK(java.lang.String mib, java.lang.String trapOid, java.lang.Integer genericId,
			java.lang.Integer specificId) {
		this.mib = mib;
		this.trapOid = trapOid;
		this.genericId = genericId;
		this.specificId = specificId;
	}

	public java.lang.String getMib() {
		return mib;
	}

	public java.lang.String getTrapOid() {
		return trapOid;
	}

	public java.lang.Integer getGenericId() {
		return genericId;
	}

	public java.lang.Integer getSpecificId() {
		return specificId;
	}

	public void setMib(java.lang.String mib) {
		this.mib = mib;
	}

	public void setTrapOid(java.lang.String trapOid) {
		this.trapOid = trapOid;
	}

	public void setGenericId(java.lang.Integer genericId) {
		this.genericId = genericId;
	}

	public void setSpecificId(java.lang.Integer specificId) {
		this.specificId = specificId;
	}

	@Override
	public int hashCode() {
		int _hashCode = 0;
		if (this.mib != null)
			_hashCode += this.mib.hashCode();
		if (this.trapOid != null)
			_hashCode += this.trapOid.hashCode();
		if (this.genericId != null)
			_hashCode += this.genericId.hashCode();
		if (this.specificId != null)
			_hashCode += this.specificId.hashCode();

		return _hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof com.clustercontrol.snmptrap.entity.SnmpTrapMasterPK))
			return false;

		com.clustercontrol.snmptrap.entity.SnmpTrapMasterPK pk = (com.clustercontrol.snmptrap.entity.SnmpTrapMasterPK) obj;
		boolean eq = true;

		if (this.mib != null) {
			eq = eq && this.mib.equals(pk.getMib());
		} else // this.mib == null
		{
			eq = eq && (pk.getMib() == null);
		}
		if (this.trapOid != null) {
			eq = eq && this.trapOid.equals(pk.getTrapOid());
		} else // this.trapOid == null
		{
			eq = eq && (pk.getTrapOid() == null);
		}
		if (this.genericId != null) {
			eq = eq && this.genericId.equals(pk.getGenericId());
		} else // this.genericId == null
		{
			eq = eq && (pk.getGenericId() == null);
		}
		if (this.specificId != null) {
			eq = eq && this.specificId.equals(pk.getSpecificId());
		} else // this.specificId == null
		{
			eq = eq && (pk.getSpecificId() == null);
		}

		return eq;
	}

	/**
	 * @return String representation of this pk in the form of
	 *         [.field1.field2.field3].
	 */
	@Override
	public String toString() {
		StringBuffer toStringValue = new StringBuffer("[.");
		toStringValue.append(this.mib).append('.');
		toStringValue.append(this.trapOid).append('.');
		toStringValue.append(this.genericId).append('.');
		toStringValue.append(this.specificId).append('.');
		toStringValue.append(']');
		return toStringValue.toString();
	}

}
