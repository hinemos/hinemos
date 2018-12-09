/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.entity;

/**
 * Primary key for NotifyJobInfo.
 * 
 * @version 6.1.0
 * @since 4.0.1
 */
public class NotifyJobInfoPK extends java.lang.Object implements java.io.Serializable {

	private static final long serialVersionUID = 5695542413675444706L;

	public java.lang.String notifyId;
	public java.lang.Integer priority;

	public NotifyJobInfoPK() {
	}

	public NotifyJobInfoPK(java.lang.String notifyId, java.lang.Integer priority) {
		this.notifyId = notifyId;
		this.priority = priority;
	}

	public java.lang.String getNotifyId() {
		return notifyId;
	}

	public java.lang.Integer getPriority() {
		return priority;
	}

	public void setNotifyId(java.lang.String notifyId) {
		this.notifyId = notifyId;
	}

	public void setPriority(java.lang.Integer priority) {
		this.priority = priority;
	}

	@Override
	public int hashCode() {
		int _hashCode = 0;
		if (this.notifyId != null)
			_hashCode += this.notifyId.hashCode();
		if (this.priority != null)
			_hashCode += this.priority.hashCode();

		return _hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof com.clustercontrol.notify.entity.NotifyJobInfoPK))
			return false;

		com.clustercontrol.notify.entity.NotifyJobInfoPK pk = (com.clustercontrol.notify.entity.NotifyJobInfoPK) obj;
		boolean eq = true;

		if (this.notifyId != null) {
			eq = eq && this.notifyId.equals(pk.getNotifyId());
		} else // this.notifyId == null
		{
			eq = eq && (pk.getNotifyId() == null);
		}
		if (this.priority != null) {
			eq = eq && this.priority.equals(pk.getPriority());
		} else // this.priority == null
		{
			eq = eq && (pk.getPriority() == null);
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
		toStringValue.append(this.notifyId).append('.');
		toStringValue.append(this.priority).append('.');
		toStringValue.append(']');
		return toStringValue.toString();
	}

}
