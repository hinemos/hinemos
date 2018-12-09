/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.monitor.entity;

/**
 * Data object for CollectorItemCalcMethodMst.
 * 
 * @version 6.1.0
 * @since 4.0.1
 */
public class CollectorItemCalcMethodMstData extends java.lang.Object implements java.io.Serializable {
	private static final long serialVersionUID = -7951518248295173778L;

	private java.lang.String collectMethod;
	private java.lang.String platformId;
	private java.lang.String subPlatformId;
	private java.lang.String itemCode;
	private java.lang.String calcMethod;

	/* begin value object */

	/* end value object */

	public CollectorItemCalcMethodMstData() {
	}

	public CollectorItemCalcMethodMstData(java.lang.String collectMethod, java.lang.String platformId,
			java.lang.String subPlatformId, java.lang.String itemCode, java.lang.String calcMethod) {
		setCollectMethod(collectMethod);
		setPlatformId(platformId);
		setSubPlatformId(subPlatformId);
		setItemCode(itemCode);
		setCalcMethod(calcMethod);
	}

	public CollectorItemCalcMethodMstData(CollectorItemCalcMethodMstData otherData) {
		setCollectMethod(otherData.getCollectMethod());
		setPlatformId(otherData.getPlatformId());
		setSubPlatformId(otherData.getSubPlatformId());
		setItemCode(otherData.getItemCode());
		setCalcMethod(otherData.getCalcMethod());

	}

	public com.clustercontrol.performance.monitor.entity.CollectorItemCalcMethodMstPK getPrimaryKey() {
		com.clustercontrol.performance.monitor.entity.CollectorItemCalcMethodMstPK pk = new com.clustercontrol.performance.monitor.entity.CollectorItemCalcMethodMstPK(
				this.getCollectMethod(), this.getPlatformId(), this.getSubPlatformId(), this.getItemCode());
		return pk;
	}

	public java.lang.String getCollectMethod() {
		return this.collectMethod;
	}

	public void setCollectMethod(java.lang.String collectMethod) {
		this.collectMethod = collectMethod;
	}

	public java.lang.String getPlatformId() {
		return this.platformId;
	}

	public void setPlatformId(java.lang.String platformId) {
		this.platformId = platformId;
	}

	public java.lang.String getSubPlatformId() {
		return this.subPlatformId;
	}

	public void setSubPlatformId(java.lang.String subPlatformId) {
		this.subPlatformId = subPlatformId;
	}

	public java.lang.String getItemCode() {
		return this.itemCode;
	}

	public void setItemCode(java.lang.String itemCode) {
		this.itemCode = itemCode;
	}

	public java.lang.String getCalcMethod() {
		return this.calcMethod;
	}

	public void setCalcMethod(java.lang.String calcMethod) {
		this.calcMethod = calcMethod;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer("{");

		str.append("collectMethod=" + getCollectMethod() + " " + "platformId=" + getPlatformId() + " "
				+ "subPlatformId=" + getSubPlatformId() + " " + "itemCode=" + getItemCode() + " " + "calcMethod="
				+ getCalcMethod());
		str.append('}');

		return (str.toString());
	}

	@Override
	public boolean equals(Object pOther) {
		if (pOther instanceof CollectorItemCalcMethodMstData) {
			CollectorItemCalcMethodMstData lTest = (CollectorItemCalcMethodMstData) pOther;
			boolean lEquals = true;

			if (this.collectMethod == null) {
				lEquals = lEquals && (lTest.collectMethod == null);
			} else {
				lEquals = lEquals && this.collectMethod.equals(lTest.collectMethod);
			}
			if (this.platformId == null) {
				lEquals = lEquals && (lTest.platformId == null);
			} else {
				lEquals = lEquals && this.platformId.equals(lTest.platformId);
			}
			if (this.subPlatformId == null) {
				lEquals = lEquals && (lTest.subPlatformId == null);
			} else {
				lEquals = lEquals && this.subPlatformId.equals(lTest.subPlatformId);
			}
			if (this.itemCode == null) {
				lEquals = lEquals && (lTest.itemCode == null);
			} else {
				lEquals = lEquals && this.itemCode.equals(lTest.itemCode);
			}
			if (this.calcMethod == null) {
				lEquals = lEquals && (lTest.calcMethod == null);
			} else {
				lEquals = lEquals && this.calcMethod.equals(lTest.calcMethod);
			}

			return lEquals;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + ((this.collectMethod != null) ? this.collectMethod.hashCode() : 0);

		result = 37 * result + ((this.platformId != null) ? this.platformId.hashCode() : 0);

		result = 37 * result + ((this.subPlatformId != null) ? this.subPlatformId.hashCode() : 0);

		result = 37 * result + ((this.itemCode != null) ? this.itemCode.hashCode() : 0);

		result = 37 * result + ((this.calcMethod != null) ? this.calcMethod.hashCode() : 0);

		return result;
	}

}
