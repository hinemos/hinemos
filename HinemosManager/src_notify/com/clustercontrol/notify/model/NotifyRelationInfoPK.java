package com.clustercontrol.notify.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_notify_relation_info database table.
 * 
 */
@Embeddable
public class NotifyRelationInfoPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String notifyGroupId;
	private String notifyId;

	public NotifyRelationInfoPK() {
	}

	public NotifyRelationInfoPK(String notifyGroupId, String notifyId) {
		this.setNotifyGroupId(notifyGroupId);
		this.setNotifyId(notifyId);
	}

	@Column(name="notify_group_id")
	public String getNotifyGroupId() {
		return this.notifyGroupId;
	}
	public void setNotifyGroupId(String notifyGroupId) {
		this.notifyGroupId = notifyGroupId;
	}

	@Column(name="notify_id")
	public String getNotifyId() {
		return this.notifyId;
	}
	public void setNotifyId(String notifyId) {
		this.notifyId = notifyId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof NotifyRelationInfoPK)) {
			return false;
		}
		NotifyRelationInfoPK castOther = (NotifyRelationInfoPK)other;
		return
				this.notifyGroupId.equals(castOther.notifyGroupId)
				&& this.notifyId.equals(castOther.notifyId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.notifyGroupId.hashCode();
		hash = hash * prime + this.notifyId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"notifyGroupId",
				"notifyId"
		};
		String[] values = {
				this.notifyGroupId,
				this.notifyId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}