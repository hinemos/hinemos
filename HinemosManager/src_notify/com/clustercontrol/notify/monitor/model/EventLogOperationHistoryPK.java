/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.monitor.model;

import java.io.Serializable;
import java.util.Arrays;

import jakarta.persistence.*;

/**
 * The primary key class for the cc_event_log database table.
 * 
 */
@Embeddable
public class EventLogOperationHistoryPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private Long logSeqNo;

	public EventLogOperationHistoryPK() {
	}

	public EventLogOperationHistoryPK(Long logSeqNo) {
		this.setLogSeqNo(logSeqNo);
	}

	@Column(name="log_seqno", insertable=false)
	public Long getLogSeqNo() {
		return this.logSeqNo;
	}
	public void setLogSeqNo(Long logSeqNo) {
		this.logSeqNo = logSeqNo;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof EventLogOperationHistoryPK)) {
			return false;
		}
		EventLogOperationHistoryPK castOther = (EventLogOperationHistoryPK)other;
		return this.logSeqNo.equals(castOther.logSeqNo);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.logSeqNo.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"logSeqNo",
		};
		String[] values = {
				this.logSeqNo.toString(),
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}