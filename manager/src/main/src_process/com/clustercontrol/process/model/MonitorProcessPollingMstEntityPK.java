package com.clustercontrol.process.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_monitor_process_polling_mst database table.
 * 
 */
@Embeddable
public class MonitorProcessPollingMstEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String collectMethod;
	private String platformId;
	private String subPlatformId;
	private String variableId;

	public MonitorProcessPollingMstEntityPK() {
	}

	public MonitorProcessPollingMstEntityPK(String collectMethod,
			String platformId,
			String subPlatformId,
			String variableId) {
		this.setCollectMethod(collectMethod);
		this.setPlatformId(platformId);
		this.setSubPlatformId(subPlatformId);
		this.setVariableId(variableId);
	}

	@Column(name="collect_method")
	public String getCollectMethod() {
		return this.collectMethod;
	}
	public void setCollectMethod(String collectMethod) {
		this.collectMethod = collectMethod;
	}

	@Column(name="platform_id")
	public String getPlatformId() {
		return this.platformId;
	}
	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	@Column(name="sub_platform_id")
	public String getSubPlatformId() {
		return this.subPlatformId;
	}
	public void setSubPlatformId(String subPlatformId) {
		this.subPlatformId = subPlatformId;
	}

	@Column(name="variable_id")
	public String getVariableId() {
		return this.variableId;
	}
	public void setVariableId(String variableId) {
		this.variableId = variableId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MonitorProcessPollingMstEntityPK)) {
			return false;
		}
		MonitorProcessPollingMstEntityPK castOther = (MonitorProcessPollingMstEntityPK)other;
		return
				this.collectMethod.equals(castOther.collectMethod)
				&& this.platformId.equals(castOther.platformId)
				&& this.subPlatformId.equals(castOther.subPlatformId)
				&& this.variableId.equals(castOther.variableId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.collectMethod.hashCode();
		hash = hash * prime + this.platformId.hashCode();
		hash = hash * prime + this.subPlatformId.hashCode();
		hash = hash * prime + this.variableId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"collectMethod",
				"platformId",
				"subPlatformId",
				"variableId"
		};
		String[] values = {
				this.collectMethod,
				this.platformId,
				this.subPlatformId,
				this.variableId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}