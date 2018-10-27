/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.monitor.entity;

/**
 * Data object for CollectorCategoryCollectMst.
 * 
 * @version 6.1.0
 * @since 4.0.1
 */
public class CollectorCategoryCollectMstData extends java.lang.Object implements java.io.Serializable {
	private static final long serialVersionUID = 425711575336789778L;

	private java.lang.String platformId;
	private java.lang.String subPlatformId;
	private java.lang.String categoryCode;
	private java.lang.String collectMethod;

	/* begin value object */

	/* end value object */

	public CollectorCategoryCollectMstData() {
	}

	public CollectorCategoryCollectMstData(java.lang.String platformId, java.lang.String subPlatformId,
			java.lang.String categoryCode, java.lang.String collectMethod) {
		setPlatformId(platformId);
		setSubPlatformId(subPlatformId);
		setCategoryCode(categoryCode);
		setCollectMethod(collectMethod);
	}

	public CollectorCategoryCollectMstData(CollectorCategoryCollectMstData otherData) {
		setPlatformId(otherData.getPlatformId());
		setSubPlatformId(otherData.getSubPlatformId());
		setCategoryCode(otherData.getCategoryCode());
		setCollectMethod(otherData.getCollectMethod());

	}

	public com.clustercontrol.performance.monitor.entity.CollectorCategoryCollectMstPK getPrimaryKey() {
		com.clustercontrol.performance.monitor.entity.CollectorCategoryCollectMstPK pk = new com.clustercontrol.performance.monitor.entity.CollectorCategoryCollectMstPK(
				this.getPlatformId(), this.getSubPlatformId(), this.getCategoryCode());
		return pk;
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

	public java.lang.String getCategoryCode() {
		return this.categoryCode;
	}

	public void setCategoryCode(java.lang.String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public java.lang.String getCollectMethod() {
		return this.collectMethod;
	}

	public void setCollectMethod(java.lang.String collectMethod) {
		this.collectMethod = collectMethod;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer("{");

		str.append("platformId=" + getPlatformId() + " " + "subPlatformId=" + getSubPlatformId() + " " + "categoryCode="
				+ getCategoryCode() + " " + "collectMethod=" + getCollectMethod());
		str.append('}');

		return (str.toString());
	}

	@Override
	public boolean equals(Object pOther) {
		if (pOther instanceof CollectorCategoryCollectMstData) {
			CollectorCategoryCollectMstData lTest = (CollectorCategoryCollectMstData) pOther;
			boolean lEquals = true;

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
			if (this.categoryCode == null) {
				lEquals = lEquals && (lTest.categoryCode == null);
			} else {
				lEquals = lEquals && this.categoryCode.equals(lTest.categoryCode);
			}
			if (this.collectMethod == null) {
				lEquals = lEquals && (lTest.collectMethod == null);
			} else {
				lEquals = lEquals && this.collectMethod.equals(lTest.collectMethod);
			}

			return lEquals;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + ((this.platformId != null) ? this.platformId.hashCode() : 0);

		result = 37 * result + ((this.subPlatformId != null) ? this.subPlatformId.hashCode() : 0);

		result = 37 * result + ((this.categoryCode != null) ? this.categoryCode.hashCode() : 0);

		result = 37 * result + ((this.collectMethod != null) ? this.collectMethod.hashCode() : 0);

		return result;
	}

}
