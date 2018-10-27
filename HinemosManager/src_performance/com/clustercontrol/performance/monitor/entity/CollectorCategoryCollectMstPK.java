/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.monitor.entity;

/**
 * Primary key for CollectorCategoryCollectMst.
 * 
 * @version 6.1.0
 * @since 4.0.1
 */
public class CollectorCategoryCollectMstPK extends java.lang.Object implements java.io.Serializable {

	private static final long serialVersionUID = -4365229270063567182L;

	public java.lang.String platformId;
	public java.lang.String subPlatformId;
	public java.lang.String categoryCode;

	public CollectorCategoryCollectMstPK() {
	}

	public CollectorCategoryCollectMstPK(java.lang.String platformId, java.lang.String subPlatformId,
			java.lang.String categoryCode) {
		this.platformId = platformId;
		this.subPlatformId = subPlatformId;
		this.categoryCode = categoryCode;
	}

	public java.lang.String getPlatformId() {
		return platformId;
	}

	public java.lang.String getSubPlatformId() {
		return subPlatformId;
	}

	public java.lang.String getCategoryCode() {
		return categoryCode;
	}

	public void setPlatformId(java.lang.String platformId) {
		this.platformId = platformId;
	}

	public void setSubPlatformId(java.lang.String subPlatformId) {
		this.subPlatformId = subPlatformId;
	}

	public void setCategoryCode(java.lang.String categoryCode) {
		this.categoryCode = categoryCode;
	}

	@Override
	public int hashCode() {
		int _hashCode = 0;
		if (this.platformId != null)
			_hashCode += this.platformId.hashCode();
		if (this.subPlatformId != null)
			_hashCode += this.subPlatformId.hashCode();
		if (this.categoryCode != null)
			_hashCode += this.categoryCode.hashCode();

		return _hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof com.clustercontrol.performance.monitor.entity.CollectorCategoryCollectMstPK))
			return false;

		com.clustercontrol.performance.monitor.entity.CollectorCategoryCollectMstPK pk = (com.clustercontrol.performance.monitor.entity.CollectorCategoryCollectMstPK) obj;
		boolean eq = true;

		if (this.platformId != null) {
			eq = eq && this.platformId.equals(pk.getPlatformId());
		} else // this.platformId == null
		{
			eq = eq && (pk.getPlatformId() == null);
		}
		if (this.subPlatformId != null) {
			eq = eq && this.subPlatformId.equals(pk.getSubPlatformId());
		} else // this.subPlatformId == null
		{
			eq = eq && (pk.getSubPlatformId() == null);
		}
		if (this.categoryCode != null) {
			eq = eq && this.categoryCode.equals(pk.getCategoryCode());
		} else // this.categoryCode == null
		{
			eq = eq && (pk.getCategoryCode() == null);
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
		toStringValue.append(this.platformId).append('.');
		toStringValue.append(this.subPlatformId).append('.');
		toStringValue.append(this.categoryCode).append('.');
		toStringValue.append(']');
		return toStringValue.toString();
	}

}
