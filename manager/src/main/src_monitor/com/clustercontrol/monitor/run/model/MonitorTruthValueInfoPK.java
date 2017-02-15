package com.clustercontrol.monitor.run.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_monitor_truth_value_info database table.
 * 
 */
@Embeddable
public class MonitorTruthValueInfoPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private Integer priority;
	private Integer truthValue;

	public MonitorTruthValueInfoPK() {
	}

	public MonitorTruthValueInfoPK(String monitorId, Integer priority, Integer truthValue) {
		this.setMonitorId(monitorId);
		this.setPriority(priority);
		this.setTruthValue(truthValue);
	}

	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public Integer getPriority() {
		return this.priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	@Column(name="truth_value")
	public Integer getTruthValue() {
		return this.truthValue;
	}
	public void setTruthValue(Integer truthValue) {
		this.truthValue = truthValue;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MonitorTruthValueInfoPK)) {
			return false;
		}
		MonitorTruthValueInfoPK castOther = (MonitorTruthValueInfoPK)other;
		return
				this.monitorId.equals(castOther.monitorId)
				&& this.priority.equals(castOther.priority)
				&& this.truthValue.equals(castOther.truthValue);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.monitorId.hashCode();
		hash = hash * prime + this.priority.hashCode();
		hash = hash * prime + this.truthValue.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"monitorId",
				"priority",
				"truthValue"
		};
		String[] values = {
				this.monitorId,
				this.priority.toString(),
				this.truthValue.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}