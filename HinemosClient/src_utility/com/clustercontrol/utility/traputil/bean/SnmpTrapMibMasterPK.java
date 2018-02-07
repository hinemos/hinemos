/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.traputil.bean;

/**
 * Primary key for SnmpTrapMibMaster.
 * 
 */
public class SnmpTrapMibMasterPK extends java.lang.Object implements
		java.io.Serializable {

	private static final long serialVersionUID = 4906046643869672497L;
	public java.lang.String mib;

	public SnmpTrapMibMasterPK() {
	}

	public SnmpTrapMibMasterPK(java.lang.String mib) {
		this.mib = mib;
	}

	public java.lang.String getMib() {
		return mib;
	}

	public void setMib(java.lang.String mib) {
		this.mib = mib;
	}

	@Override
	public int hashCode() {
		int _hashCode = 0;
		if (this.mib != null)
			_hashCode += this.mib.hashCode();

		return _hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SnmpTrapMibMasterPK))
			return false;

		SnmpTrapMibMasterPK pk = (SnmpTrapMibMasterPK) obj;
		boolean eq = true;

		if (this.mib != null) {
			eq = eq && this.mib.equals(pk.getMib());
		} else // this.mib == null
		{
			eq = eq && (pk.getMib() == null);
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
		toStringValue.append(']');
		return toStringValue.toString();
	}

}
