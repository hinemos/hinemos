/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.queue.internal;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The primary key class for the cc_job_queue_item database table.
 * 
 * @since 6.2.0
 */
@Embeddable
public class JobQueueItemEntityPK implements Serializable {
	// 実装を変更したときのバージョン番号に合わせる。
	// {major high}_{major low}_{minor}_{patch}
	private static final long serialVersionUID = 6_02_00_00000000L;

	private String queueId;
	private String sessionId;
	private String jobunitId;
	private String jobId;

	@Deprecated
	public JobQueueItemEntityPK() {
	}

	public JobQueueItemEntityPK(String queueId, String sessionId, String jobunitId, String jobId) {
		setQueueId(queueId);
		setSessionId(sessionId);
		setJobunitId(jobunitId);
		setJobId(jobId);
	}

	@Override
	public String toString() {
		return "JobQueueItemEntityPK[" + queueId + "," + sessionId + "," + jobunitId + "," + jobId + "]";
	}
	
	/*-----------------
	 * getter/setter
	 ----------------*/
	@Column(name="queue_id")
	public String getQueueId() {
		return queueId;
	}
	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

	@Column(name="session_id")
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Column(name="jobunit_id")
	public String getJobunitId() {
		return jobunitId;
	}
	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	@Column(name="job_id")
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	/*-----------------
	 * hashCode/equals
	 * eclipseで自動生成しただけのもの。
	 * フィールド構成が変更になったら忘れずに再生成すること。
	 ----------------*/
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
		result = prime * result + ((jobunitId == null) ? 0 : jobunitId.hashCode());
		result = prime * result + ((queueId == null) ? 0 : queueId.hashCode());
		result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		JobQueueItemEntityPK other = (JobQueueItemEntityPK) obj;
		if (jobId == null) {
			if (other.jobId != null) return false;
		} else if (!jobId.equals(other.jobId)) return false;
		if (jobunitId == null) {
			if (other.jobunitId != null) return false;
		} else if (!jobunitId.equals(other.jobunitId)) return false;
		if (queueId == null) {
			if (other.queueId != null) return false;
		} else if (!queueId.equals(other.queueId)) return false;
		if (sessionId == null) {
			if (other.sessionId != null) return false;
		} else if (!sessionId.equals(other.sessionId)) return false;
		return true;
	}
}
