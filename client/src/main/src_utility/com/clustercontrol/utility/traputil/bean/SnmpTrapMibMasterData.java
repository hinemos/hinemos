/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.traputil.bean;

/**
 * DTOをコピーしたクラス
 * 
 * DTOは動的にロードされるのでキャストできないので、
 * 
 * 
 */
public class SnmpTrapMibMasterData extends java.lang.Object implements
		java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6068473831626551615L;
	private java.lang.String mib;
	private java.lang.Integer orderNo;
	private java.lang.String description;
	private java.sql.Timestamp regDate;
	private java.sql.Timestamp updateDate;
	private java.lang.String regUser;
	private java.lang.String updateUser;

	/* begin value object */

	/* end value object */

	public SnmpTrapMibMasterData() {
	}

	public SnmpTrapMibMasterData(java.lang.String mib,
			java.lang.Integer orderNo, java.lang.String description,
			java.sql.Timestamp regDate, java.sql.Timestamp updateDate,
			java.lang.String regUser, java.lang.String updateUser) {
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

	public SnmpTrapMibMasterPK getPrimaryKey() {
		SnmpTrapMibMasterPK pk = new SnmpTrapMibMasterPK(this.getMib());
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

	public java.sql.Timestamp getRegDate() {
		return this.regDate;
	}

	public void setRegDate(java.sql.Timestamp regDate) {
		this.regDate = regDate;
	}

	public java.sql.Timestamp getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(java.sql.Timestamp updateDate) {
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

		str.append("mib=" + getMib() + " " + "orderNo=" + getOrderNo() + " "
				+ "description=" + getDescription() + " " + "regDate="
				+ getRegDate() + " " + "updateDate=" + getUpdateDate() + " "
				+ "regUser=" + getRegUser() + " " + "updateUser="
				+ getUpdateUser());
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
			if (this.regDate == null) {
				lEquals = lEquals && (lTest.regDate == null);
			} else {
				lEquals = lEquals && this.regDate.equals(lTest.regDate);
			}
			if (this.updateDate == null) {
				lEquals = lEquals && (lTest.updateDate == null);
			} else {
				lEquals = lEquals && this.updateDate.equals(lTest.updateDate);
			}
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

		result = 37 * result
				+ ((this.orderNo != null) ? this.orderNo.hashCode() : 0);

		result = 37
				* result
				+ ((this.description != null) ? this.description.hashCode() : 0);

		result = 37 * result
				+ ((this.regDate != null) ? this.regDate.hashCode() : 0);

		result = 37 * result
				+ ((this.updateDate != null) ? this.updateDate.hashCode() : 0);

		result = 37 * result
				+ ((this.regUser != null) ? this.regUser.hashCode() : 0);

		result = 37 * result
				+ ((this.updateUser != null) ? this.updateUser.hashCode() : 0);

		return result;
	}

}
