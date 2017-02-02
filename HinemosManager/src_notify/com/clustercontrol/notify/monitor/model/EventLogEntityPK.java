package com.clustercontrol.notify.monitor.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_event_log database table.
 * 
 */
@Embeddable
public class EventLogEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private String monitorDetailId;
	private String pluginId;
	private Long outputDate;
	private String facilityId;

	public EventLogEntityPK() {
	}

	public EventLogEntityPK(String monitorId,
			String monitorDetailId,
			String pluginId,
			Long outputDate,
			String facilityId) {
		this.setMonitorId(monitorId);
		this.setMonitorDetailId(monitorDetailId);
		this.setPluginId(pluginId);
		this.setOutputDate(outputDate);
		this.setFacilityId(facilityId);
	}

	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	@Column(name="monitor_detail_id")
	public String getMonitorDetailId() {
		return this.monitorDetailId;
	}
	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	@Column(name="plugin_id")
	public String getPluginId() {
		return this.pluginId;
	}
	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	@Column(name="output_date")
	public Long getOutputDate() {
		return this.outputDate;
	}
	public void setOutputDate(Long outputDate) {
		this.outputDate = outputDate;
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof EventLogEntityPK)) {
			return false;
		}
		EventLogEntityPK castOther = (EventLogEntityPK)other;
		return
				this.monitorId.equals(castOther.monitorId)
				&& this.monitorDetailId.equals(castOther.monitorDetailId)
				&& this.pluginId.equals(castOther.pluginId)
				&& this.outputDate.equals(castOther.outputDate)
				&& this.facilityId.equals(castOther.facilityId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.monitorId.hashCode();
		hash = hash * prime + this.monitorDetailId.hashCode();
		hash = hash * prime + this.pluginId.hashCode();
		hash = hash * prime + this.outputDate.hashCode();
		hash = hash * prime + this.facilityId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"monitorId",
				"monitorDetailId",
				"pluginId",
				"outputDate",
				"facilityId"
		};
		String[] values = {
				this.monitorId,
				this.monitorDetailId,
				this.pluginId,
				this.outputDate.toString(),
				this.facilityId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}