/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.monitor.entity;

/**
 * Data object for CollectorCategoryMst.
 * 
 * @version 6.1.0
 * @since 4.0.1
 */
public class CollectorCategoryMstData extends java.lang.Object implements java.io.Serializable {
	private static final long serialVersionUID = -7817348308254852363L;

	private java.lang.String categoryCode;
	private java.lang.String categoryName;

	/* begin value object */

	/* end value object */

	public CollectorCategoryMstData() {
	}

	public CollectorCategoryMstData(java.lang.String categoryCode, java.lang.String categoryName) {
		setCategoryCode(categoryCode);
		setCategoryName(categoryName);
	}

	public CollectorCategoryMstData(CollectorCategoryMstData otherData) {
		setCategoryCode(otherData.getCategoryCode());
		setCategoryName(otherData.getCategoryName());

	}

	public java.lang.String getPrimaryKey() {
		return getCategoryCode();
	}

	public java.lang.String getCategoryCode() {
		return this.categoryCode;
	}

	public void setCategoryCode(java.lang.String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public java.lang.String getCategoryName() {
		return this.categoryName;
	}

	public void setCategoryName(java.lang.String categoryName) {
		this.categoryName = categoryName;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer("{");

		str.append("categoryCode=" + getCategoryCode() + " " + "categoryName=" + getCategoryName());
		str.append('}');

		return (str.toString());
	}

	@Override
	public boolean equals(Object pOther) {
		if (pOther instanceof CollectorCategoryMstData) {
			CollectorCategoryMstData lTest = (CollectorCategoryMstData) pOther;
			boolean lEquals = true;

			if (this.categoryCode == null) {
				lEquals = lEquals && (lTest.categoryCode == null);
			} else {
				lEquals = lEquals && this.categoryCode.equals(lTest.categoryCode);
			}
			if (this.categoryName == null) {
				lEquals = lEquals && (lTest.categoryName == null);
			} else {
				lEquals = lEquals && this.categoryName.equals(lTest.categoryName);
			}

			return lEquals;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + ((this.categoryCode != null) ? this.categoryCode.hashCode() : 0);

		result = 37 * result + ((this.categoryName != null) ? this.categoryName.hashCode() : 0);

		return result;
	}

}
