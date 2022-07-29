/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.snmptrap.entity;

/**
 * Data object for SnmpTrapMibMaster.
 * 
 * @version 6.1.0
 * @since 4.0.1
 */
public class SnmpTrapMibMasterData extends java.lang.Object implements java.io.Serializable {
	private static final long serialVersionUID = -1910124815658404510L;

	private java.lang.String mib;
	private java.lang.Integer orderNo;
	private java.lang.String description;
	private long regDate;
	private long updateDate;
	private java.lang.String regUser;
	private java.lang.String updateUser;

	/* begin value object */

	/* end value object */

	public SnmpTrapMibMasterData() {
	}

	public SnmpTrapMibMasterData(java.lang.String mib, java.lang.Integer orderNo, java.lang.String description,
			long regDate, long updateDate, java.lang.String regUser, java.lang.String updateUser) {
		setMib(mib);
		setOrderNo(orderNo);
		setDescription(description);
		setRegDate(regDate);
		setUpdateDate(updateDate);
		setRegUser(regUser);
		setUpdateUser(updateUser);
	}

	public SnmpTrapMibMasterData(SnmpTrapMibMasterData otherData) {
		setMib(otherData.getMib());
		setOrderNo(otherData.getOrderNo());
		setDescription(otherData.getDescription());
		setRegDate(otherData.getRegDate());
		setUpdateDate(otherData.getUpdateDate());
		setRegUser(otherData.getRegUser());
		setUpdateUser(otherData.getUpdateUser());

	}

	public com.clustercontrol.snmptrap.entity.SnmpTrapMibMasterPK getPrimaryKey() {
		com.clustercontrol.snmptrap.entity.SnmpTrapMibMasterPK pk = new com.clustercontrol.snmptrap.entity.SnmpTrapMibMasterPK(
				this.getMib());
		return pk;
	}

	public java.lang.String getMib() {
		return this.mib;
	}

	public void setMib(java.lang.String mib) {
		this.mib = mib;
	}

	public java.lang.Integer getOrderNo() {
		return this.orderNo;
	}

	public void setOrderNo(java.lang.Integer orderNo) {
		this.orderNo = orderNo;
	}

	public java.lang.String getDescription() {
		return this.description;
	}

	public void setDescription(java.lang.String description) {
		this.description = description;
	}

	public long getRegDate() {
		return this.regDate;
	}

	public void setRegDate(long regDate) {
		this.regDate = regDate;
	}

	public long getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(long updateDate) {
		this.updateDate = updateDate;
	}

	public java.lang.String getRegUser() {
		return this.regUser;
	}

	public void setRegUser(java.lang.String regUser) {
		this.regUser = regUser;
	}

	public java.lang.String getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(java.lang.String updateUser) {
		this.updateUser = updateUser;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer("{");

		str.append("mib=" + getMib() + " " + "orderNo=" + getOrderNo() + " " + "description=" + getDescription() + " "
				+ "regDate=" + getRegDate() + " " + "updateDate=" + getUpdateDate() + " " + "regUser=" + getRegUser()
				+ " " + "updateUser=" + getUpdateUser());
		str.append('}');

		return (str.toString());
	}

	@Override
	public boolean equals(Object pOther) {
		if (pOther instanceof SnmpTrapMibMasterData) {
			SnmpTrapMibMasterData lTest = (SnmpTrapMibMasterData) pOther;
			boolean lEquals = true;

			if (this.mib == null) {
				lEquals = lEquals && (lTest.mib == null);
			} else {
				lEquals = lEquals && this.mib.equals(lTest.mib);
			}
			if (this.orderNo == null) {
				lEquals = lEquals && (lTest.orderNo == null);
			} else {
				lEquals = lEquals && this.orderNo.equals(lTest.orderNo);
			}
			if (this.description == null) {
				lEquals = lEquals && (lTest.description == null);
			} else {
				lEquals = lEquals && this.description.equals(lTest.description);
			}
			lEquals = lEquals && this.regDate == lTest.regDate;
			lEquals = lEquals && this.updateDate == lTest.updateDate;
			if (this.regUser == null) {
				lEquals = lEquals && (lTest.regUser == null);
			} else {
				lEquals = lEquals && this.regUser.equals(lTest.regUser);
			}
			if (this.updateUser == null) {
				lEquals = lEquals && (lTest.updateUser == null);
			} else {
				lEquals = lEquals && this.updateUser.equals(lTest.updateUser);
			}

			return lEquals;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + ((this.mib != null) ? this.mib.hashCode() : 0);

		result = 37 * result + ((this.orderNo != null) ? this.orderNo.hashCode() : 0);

		result = 37 * result + ((this.description != null) ? this.description.hashCode() : 0);

		result = 37 * result + (int) (regDate ^ (regDate >>> 32));

		result = 37 * result + (int) (updateDate ^ (updateDate >>> 32));

		result = 37 * result + ((this.regUser != null) ? this.regUser.hashCode() : 0);

		result = 37 * result + ((this.updateUser != null) ? this.updateUser.hashCode() : 0);

		return result;
	}

}
