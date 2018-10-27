/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.monitor.entity;

/**
 * Data object for CollectorItemCodeMst.
 * 
 * @version 6.1.0
 * @since 4.0.1
 */
public class CollectorItemCodeMstData extends java.lang.Object implements java.io.Serializable {
	private static final long serialVersionUID = -8394706261410237910L;

	private java.lang.String itemCode;
	private java.lang.String categoryCode;
	private java.lang.String parentItemCode;
	private java.lang.String itemName;
	private java.lang.String measure;
	private java.lang.Boolean deviceSupport;
	private java.lang.String deviceType;
	private java.lang.Boolean graphRange;

	/* begin value object */

	/* end value object */

	public CollectorItemCodeMstData() {
	}

	public CollectorItemCodeMstData(java.lang.String itemCode, java.lang.String categoryCode,
			java.lang.String parentItemCode, java.lang.String itemName, java.lang.String measure,
			java.lang.Boolean deviceSupport, java.lang.String deviceType, java.lang.Boolean graphRange) {
		setItemCode(itemCode);
		setCategoryCode(categoryCode);
		setParentItemCode(parentItemCode);
		setItemName(itemName);
		setMeasure(measure);
		setDeviceSupport(deviceSupport);
		setDeviceType(deviceType);
		setGraphRange(graphRange);
	}

	public CollectorItemCodeMstData(CollectorItemCodeMstData otherData) {
		setItemCode(otherData.getItemCode());
		setCategoryCode(otherData.getCategoryCode());
		setParentItemCode(otherData.getParentItemCode());
		setItemName(otherData.getItemName());
		setMeasure(otherData.getMeasure());
		setDeviceSupport(otherData.isDeviceSupport());
		setDeviceType(otherData.getDeviceType());
		setGraphRange(otherData.isGraphRange());

	}

	public java.lang.String getPrimaryKey() {
		return getItemCode();
	}

	public java.lang.String getItemCode() {
		return this.itemCode;
	}

	public void setItemCode(java.lang.String itemCode) {
		this.itemCode = itemCode;
	}

	public java.lang.String getCategoryCode() {
		return this.categoryCode;
	}

	public void setCategoryCode(java.lang.String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public java.lang.String getParentItemCode() {
		return this.parentItemCode;
	}

	public void setParentItemCode(java.lang.String parentItemCode) {
		this.parentItemCode = parentItemCode;
	}

	public java.lang.String getItemName() {
		return this.itemName;
	}

	public void setItemName(java.lang.String itemName) {
		this.itemName = itemName;
	}

	public java.lang.String getMeasure() {
		return this.measure;
	}

	public void setMeasure(java.lang.String measure) {
		this.measure = measure;
	}

	public java.lang.Boolean isDeviceSupport() {
		return this.deviceSupport;
	}

	public void setDeviceSupport(java.lang.Boolean deviceSupport) {
		this.deviceSupport = deviceSupport;
	}

	public java.lang.String getDeviceType() {
		return this.deviceType;
	}

	public void setDeviceType(java.lang.String deviceType) {
		this.deviceType = deviceType;
	}

	public java.lang.Boolean isGraphRange() {
		return this.graphRange;
	}

	public void setGraphRange(java.lang.Boolean graphRange) {
		this.graphRange = graphRange;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer("{");

		str.append("itemCode=" + getItemCode() + " " + "categoryCode=" + getCategoryCode() + " " + "parentItemCode="
				+ getParentItemCode() + " " + "itemName=" + getItemName() + " " + "measure=" + getMeasure() + " "
				+ "deviceSupport=" + isDeviceSupport() + " " + "deviceType=" + getDeviceType() + " " + "graphRange="
				+ isGraphRange());
		str.append('}');

		return (str.toString());
	}

	@Override
	public boolean equals(Object pOther) {
		if (pOther instanceof CollectorItemCodeMstData) {
			CollectorItemCodeMstData lTest = (CollectorItemCodeMstData) pOther;
			boolean lEquals = true;

			if (this.itemCode == null) {
				lEquals = lEquals && (lTest.itemCode == null);
			} else {
				lEquals = lEquals && this.itemCode.equals(lTest.itemCode);
			}
			if (this.categoryCode == null) {
				lEquals = lEquals && (lTest.categoryCode == null);
			} else {
				lEquals = lEquals && this.categoryCode.equals(lTest.categoryCode);
			}
			if (this.parentItemCode == null) {
				lEquals = lEquals && (lTest.parentItemCode == null);
			} else {
				lEquals = lEquals && this.parentItemCode.equals(lTest.parentItemCode);
			}
			if (this.itemName == null) {
				lEquals = lEquals && (lTest.itemName == null);
			} else {
				lEquals = lEquals && this.itemName.equals(lTest.itemName);
			}
			if (this.measure == null) {
				lEquals = lEquals && (lTest.measure == null);
			} else {
				lEquals = lEquals && this.measure.equals(lTest.measure);
			}
			if (this.deviceSupport == null) {
				lEquals = lEquals && (lTest.deviceSupport == null);
			} else {
				lEquals = lEquals && this.deviceSupport.equals(lTest.deviceSupport);
			}
			if (this.deviceType == null) {
				lEquals = lEquals && (lTest.deviceType == null);
			} else {
				lEquals = lEquals && this.deviceType.equals(lTest.deviceType);
			}
			if (this.graphRange == null) {
				lEquals = lEquals && (lTest.graphRange == null);
			} else {
				lEquals = lEquals && this.graphRange.equals(lTest.graphRange);
			}

			return lEquals;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + ((this.itemCode != null) ? this.itemCode.hashCode() : 0);

		result = 37 * result + ((this.categoryCode != null) ? this.categoryCode.hashCode() : 0);

		result = 37 * result + ((this.parentItemCode != null) ? this.parentItemCode.hashCode() : 0);

		result = 37 * result + ((this.itemName != null) ? this.itemName.hashCode() : 0);

		result = 37 * result + ((this.measure != null) ? this.measure.hashCode() : 0);

		result = 37 * result + ((this.deviceSupport != null) ? this.deviceSupport.hashCode() : 0);

		result = 37 * result + ((this.deviceType != null) ? this.deviceType.hashCode() : 0);

		result = 37 * result + ((this.graphRange != null) ? this.graphRange.hashCode() : 0);

		return result;
	}

}
