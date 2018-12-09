/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.entity;

/**
 * Data object for NotifyLogEscalateInfo.
 * 
 * @version 6.1.0
 * @since 4.0.1
 */
public class NotifyLogEscalateInfoData extends java.lang.Object implements java.io.Serializable {
	private static final long serialVersionUID = -3044397638435396243L;

	private java.lang.String notifyId;
	private java.lang.Integer priority;
	private java.lang.Boolean validFlg;
	private java.lang.String escalateMessage;
	private java.lang.Integer syslogPriority;
	private java.lang.Integer syslogFacility;
	private java.lang.Integer escalateFacilityFlg;
	private java.lang.String escalateFacility;
	private java.lang.Integer escalatePort;
	private java.lang.String ownerRoleId;

	/* begin value object */

	/* end value object */

	public NotifyLogEscalateInfoData() {
	}

	public NotifyLogEscalateInfoData(java.lang.String notifyId, java.lang.Integer priority, java.lang.Boolean validFlg,
			java.lang.String escalateMessage, java.lang.Integer syslogPriority, java.lang.Integer syslogFacility,
			java.lang.Integer escalateFacilityFlg, java.lang.String escalateFacility, java.lang.Integer escalatePort,
			java.lang.String ownerRoleId) {
		setNotifyId(notifyId);
		setPriority(priority);
		setValidFlg(validFlg);
		setEscalateMessage(escalateMessage);
		setSyslogPriority(syslogPriority);
		setSyslogFacility(syslogFacility);
		setEscalateFacilityFlg(escalateFacilityFlg);
		setEscalateFacility(escalateFacility);
		setEscalatePort(escalatePort);
		setOwnerRoleId(ownerRoleId);
	}

	public NotifyLogEscalateInfoData(NotifyLogEscalateInfoData otherData) {
		setNotifyId(otherData.getNotifyId());
		setPriority(otherData.getPriority());
		setValidFlg(otherData.getValidFlg());
		setEscalateMessage(otherData.getEscalateMessage());
		setSyslogPriority(otherData.getSyslogPriority());
		setSyslogFacility(otherData.getSyslogFacility());
		setEscalateFacilityFlg(otherData.getEscalateFacilityFlg());
		setEscalateFacility(otherData.getEscalateFacility());
		setEscalatePort(otherData.getEscalatePort());
		setOwnerRoleId(otherData.getOwnerRoleId());

	}

	public com.clustercontrol.notify.entity.NotifyLogEscalateInfoPK getPrimaryKey() {
		com.clustercontrol.notify.entity.NotifyLogEscalateInfoPK pk = new com.clustercontrol.notify.entity.NotifyLogEscalateInfoPK(
				this.getNotifyId(), this.getPriority());
		return pk;
	}

	public java.lang.String getNotifyId() {
		return this.notifyId;
	}

	public void setNotifyId(java.lang.String notifyId) {
		this.notifyId = notifyId;
	}

	public java.lang.Integer getPriority() {
		return this.priority;
	}

	public void setPriority(java.lang.Integer priority) {
		this.priority = priority;
	}

	public java.lang.Boolean getValidFlg() {
		return this.validFlg;
	}

	public void setValidFlg(java.lang.Boolean validFlg) {
		this.validFlg = validFlg;
	}

	public java.lang.String getEscalateMessage() {
		return this.escalateMessage;
	}

	public void setEscalateMessage(java.lang.String escalateMessage) {
		this.escalateMessage = escalateMessage;
	}

	public java.lang.Integer getSyslogPriority() {
		return this.syslogPriority;
	}

	public void setSyslogPriority(java.lang.Integer syslogPriority) {
		this.syslogPriority = syslogPriority;
	}

	public java.lang.Integer getSyslogFacility() {
		return this.syslogFacility;
	}

	public void setSyslogFacility(java.lang.Integer syslogFacility) {
		this.syslogFacility = syslogFacility;
	}

	public java.lang.Integer getEscalateFacilityFlg() {
		return this.escalateFacilityFlg;
	}

	public void setEscalateFacilityFlg(java.lang.Integer escalateFacilityFlg) {
		this.escalateFacilityFlg = escalateFacilityFlg;
	}

	public java.lang.String getEscalateFacility() {
		return this.escalateFacility;
	}

	public void setEscalateFacility(java.lang.String escalateFacility) {
		this.escalateFacility = escalateFacility;
	}

	public java.lang.Integer getEscalatePort() {
		return this.escalatePort;
	}

	public void setEscalatePort(java.lang.Integer escalatePort) {
		this.escalatePort = escalatePort;
	}

	public java.lang.String getOwnerRoleId() {
		return this.ownerRoleId;
	}

	public void setOwnerRoleId(java.lang.String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer("{");

		str.append("notifyId=" + getNotifyId() + " " + "priority=" + getPriority() + " " + "validFlg=" + getValidFlg()
				+ " " + "escalateMessage=" + getEscalateMessage() + " " + "syslogPriority=" + getSyslogPriority() + " "
				+ "syslogFacility=" + getSyslogFacility() + " " + "escalateFacilityFlg=" + getEscalateFacilityFlg()
				+ " " + "escalateFacility=" + getEscalateFacility() + " " + "escalatePort=" + getEscalatePort() + " "
				+ "ownerRoleId=" + getOwnerRoleId());
		str.append('}');

		return (str.toString());
	}

	@Override
	public boolean equals(Object pOther) {
		if (pOther instanceof NotifyLogEscalateInfoData) {
			NotifyLogEscalateInfoData lTest = (NotifyLogEscalateInfoData) pOther;
			boolean lEquals = true;

			if (this.notifyId == null) {
				lEquals = lEquals && (lTest.notifyId == null);
			} else {
				lEquals = lEquals && this.notifyId.equals(lTest.notifyId);
			}
			if (this.priority == null) {
				lEquals = lEquals && (lTest.priority == null);
			} else {
				lEquals = lEquals && this.priority.equals(lTest.priority);
			}
			if (this.validFlg == null) {
				lEquals = lEquals && (lTest.validFlg == null);
			} else {
				lEquals = lEquals && this.validFlg.equals(lTest.validFlg);
			}
			if (this.escalateMessage == null) {
				lEquals = lEquals && (lTest.escalateMessage == null);
			} else {
				lEquals = lEquals && this.escalateMessage.equals(lTest.escalateMessage);
			}
			if (this.syslogPriority == null) {
				lEquals = lEquals && (lTest.syslogPriority == null);
			} else {
				lEquals = lEquals && this.syslogPriority.equals(lTest.syslogPriority);
			}
			if (this.syslogFacility == null) {
				lEquals = lEquals && (lTest.syslogFacility == null);
			} else {
				lEquals = lEquals && this.syslogFacility.equals(lTest.syslogFacility);
			}
			if (this.escalateFacilityFlg == null) {
				lEquals = lEquals && (lTest.escalateFacilityFlg == null);
			} else {
				lEquals = lEquals && this.escalateFacilityFlg.equals(lTest.escalateFacilityFlg);
			}
			if (this.escalateFacility == null) {
				lEquals = lEquals && (lTest.escalateFacility == null);
			} else {
				lEquals = lEquals && this.escalateFacility.equals(lTest.escalateFacility);
			}
			if (this.escalatePort == null) {
				lEquals = lEquals && (lTest.escalatePort == null);
			} else {
				lEquals = lEquals && this.escalatePort.equals(lTest.escalatePort);
			}
			if (this.ownerRoleId == null) {
				lEquals = lEquals && (lTest.ownerRoleId == null);
			} else {
				lEquals = lEquals && this.ownerRoleId.equals(lTest.ownerRoleId);
			}

			return lEquals;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + ((this.notifyId != null) ? this.notifyId.hashCode() : 0);

		result = 37 * result + ((this.priority != null) ? this.priority.hashCode() : 0);

		result = 37 * result + ((this.validFlg != null) ? this.validFlg.hashCode() : 0);

		result = 37 * result + ((this.escalateMessage != null) ? this.escalateMessage.hashCode() : 0);

		result = 37 * result + ((this.syslogPriority != null) ? this.syslogPriority.hashCode() : 0);

		result = 37 * result + ((this.syslogFacility != null) ? this.syslogFacility.hashCode() : 0);

		result = 37 * result + ((this.escalateFacilityFlg != null) ? this.escalateFacilityFlg.hashCode() : 0);

		result = 37 * result + ((this.escalateFacility != null) ? this.escalateFacility.hashCode() : 0);

		result = 37 * result + ((this.escalatePort != null) ? this.escalatePort.hashCode() : 0);

		result = 37 * result + ((this.ownerRoleId != null) ? this.ownerRoleId.hashCode() : 0);

		return result;
	}

}
