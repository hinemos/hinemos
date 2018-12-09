/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.process.entity;

/**
 * Primary key for MonitorProcessMethodMst.
 *
 * @version 6.1.0
 * @since 4.0.1
 */
public class MonitorProcessMethodMstPK extends java.lang.Object implements java.io.Serializable {

	private static final long serialVersionUID = -6806574893621541012L;

	public java.lang.String platformId;
	public java.lang.String subPlatformId;

	public MonitorProcessMethodMstPK() {
	}

	public MonitorProcessMethodMstPK(java.lang.String platformId, java.lang.String subPlatformId) {
		this.platformId = platformId;
		this.subPlatformId = subPlatformId;
	}

	public java.lang.String getPlatformId() {
		return platformId;
	}

	public java.lang.String getSubPlatformId() {
		return subPlatformId;
	}

	public void setPlatformId(java.lang.String platformId) {
		this.platformId = platformId;
	}

	public void setSubPlatformId(java.lang.String subPlatformId) {
		this.subPlatformId = subPlatformId;
	}

	@Override
	public int hashCode() {
		int _hashCode = 0;
		if (this.platformId != null)
			_hashCode += this.platformId.hashCode();
		if (this.subPlatformId != null)
			_hashCode += this.subPlatformId.hashCode();

		return _hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof com.clustercontrol.process.entity.MonitorProcessMethodMstPK))
			return false;

		com.clustercontrol.process.entity.MonitorProcessMethodMstPK pk = (com.clustercontrol.process.entity.MonitorProcessMethodMstPK) obj;
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
		toStringValue.append(']');
		return toStringValue.toString();
	}

}
