package com.clustercontrol.winevent.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The primary key class for the cc_monitor_winevent_keyword_info database table.
 * 
 */
@Embeddable
public class MonitorWinEventKeywordInfoEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private Long keyword;

	public MonitorWinEventKeywordInfoEntityPK() {
	}

	public MonitorWinEventKeywordInfoEntityPK(String monitorId, Long keyword) {
		this.setMonitorId(monitorId);
		this.setKeyword(keyword);
	}

	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	@Column(name="keyword")
	public Long getKeyword(){
		return this.keyword;
	}
	public void setKeyword(Long keyword){
		this.keyword = keyword;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MonitorWinEventKeywordInfoEntityPK)) {
			return false;
		}
		MonitorWinEventKeywordInfoEntityPK castOther = (MonitorWinEventKeywordInfoEntityPK)other;
		return
				this.monitorId.equals(castOther.monitorId)
				&& this.keyword.equals(castOther.keyword);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.monitorId.hashCode();
		hash = hash * prime + this.keyword.hashCode();
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
				this.keyword.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}