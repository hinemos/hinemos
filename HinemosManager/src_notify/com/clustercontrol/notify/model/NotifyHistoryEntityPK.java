package com.clustercontrol.notify.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_notify_history database table.
 * 
 */
@Embeddable
public class NotifyHistoryEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String facilityId;
	private String pluginId;
	private String monitorId;
	private String notifyId;
	private String subKey;

	public NotifyHistoryEntityPK() {
	}

	public NotifyHistoryEntityPK(String facilityId,
			String pluginId,
			String monitorId,
			String notifyId,
			String subKey) {
		this.setFacilityId(facilityId);
		this.setPluginId(pluginId);
		this.setMonitorId(monitorId);
		this.setNotifyId(notifyId);
		this.setSubKey(subKey);
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="plugin_id")
	public String getPluginId() {
		return this.pluginId;
	}
	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	@Column(name="notify_id")
	public String getNotifyId() {
		return this.notifyId;
	}
	public void setNotifyId(String notifyId) {
		this.notifyId = notifyId;
	}

	@Column(name="sub_key")
	public String getSubKey() {
		return this.subKey;
	}
	public void setSubKey(String subKey) {
		this.subKey = subKey;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof NotifyHistoryEntityPK)) {
			return false;
		}
		NotifyHistoryEntityPK castOther = (NotifyHistoryEntityPK)other;
		return
				this.facilityId.equals(castOther.facilityId)
				&& this.pluginId.equals(castOther.pluginId)
				&& this.monitorId.equals(castOther.monitorId)
				&& this.notifyId.equals(castOther.notifyId)
				&& this.subKey.equals(castOther.subKey);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.facilityId.hashCode();
		hash = hash * prime + this.pluginId.hashCode();
		hash = hash * prime + this.monitorId.hashCode();
		hash = hash * prime + this.notifyId.hashCode();
		hash = hash * prime + this.subKey.hashCode();

		return hash;
	}
	@Override
	public String toString() {
		String[] names = {
				"facilityId",
				"pluginId",
				"monitorId",
				"notifyId",
				"subKey"
		};
		String[] values = {
				this.facilityId,
				this.pluginId,
				this.monitorId,
				this.notifyId,
				this.subKey
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}