package com.clustercontrol.winevent.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The primary key class for the cc_monitor_winevent_id_info database table.
 * 
 */
@Embeddable
public class MonitorWinEventIdInfoEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private Integer eventId;

	public MonitorWinEventIdInfoEntityPK() {
	}

	public MonitorWinEventIdInfoEntityPK(String monitorId, Integer eventId) {
		this.setMonitorId(monitorId);
		this.setEventId(eventId);
	}

	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	@Column(name="event_id")
	public Integer getEventId(){
		return this.eventId;
	}
	public void setEventId(Integer eventId){
		this.eventId = eventId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MonitorWinEventIdInfoEntityPK)) {
			return false;
		}
		MonitorWinEventIdInfoEntityPK castOther = (MonitorWinEventIdInfoEntityPK)other;
		return
				this.monitorId.equals(castOther.monitorId)
				&& this.eventId.equals(castOther.eventId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.monitorId.hashCode();
		hash = hash * prime + this.eventId.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"monitorId",
				"eventId"
		};
		String[] values = {
				this.monitorId,
				this.eventId.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}