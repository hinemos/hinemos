/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/**
 * リソース制御ジョブ状態テーブルの Entity クラス
 */
@NamedQueries({
	@NamedQuery(
			name=ResourceJobRunConditionEntity.findResourceJobRunCondition,
			query="SELECT r FROM ResourceJobRunConditionEntity r WHERE r.sessionId = :sessionId AND r.jobunit = :jobunit AND r.jobId = :jobId"
	),
	@NamedQuery(
			name=ResourceJobRunConditionEntity.findAllResourceJobRunCondition,
			query="SELECT r FROM ResourceJobRunConditionEntity r"
	)
})
@Entity
@Table(name="cc_xcloud_resource_job_run_condition", schema="log")
@IdClass(ResourceJobRunConditionEntity.ResourceJobRunConditionEntityPK.class)
public class ResourceJobRunConditionEntity extends EntityBase {

	public static final String findResourceJobRunCondition = "findResourceJobRunCondition";
	public static final String findAllResourceJobRunCondition = "findAllResourceJobRunCondition";

	/**
	 * PKクラス
	 */
	public static class ResourceJobRunConditionEntityPK {
		private String sessionId;
		private String jobunit;
		private String jobId;

		public ResourceJobRunConditionEntityPK() {
		}

		public ResourceJobRunConditionEntityPK(String sessionId, String jobunit, String jobId) {
			this.sessionId = sessionId;
			this.jobunit = jobunit;
			this.jobId = jobId;
		}

		public String getSessionId() {
			return sessionId;
		}

		public void setSessionId(String sessionId) {
			this.sessionId = sessionId;
		}

		public String getJobunit() {
			return jobunit;
		}

		public void setJobunit(String jobunit) {
			this.jobunit = jobunit;
		}

		public String getJobId() {
			return jobId;
		}

		public void setJobId(String jobId) {
			this.jobId = jobId;
		}
	}

	private String sessionId;
	private String jobunit;
	private String jobId;
	private int runCondition;

	public ResourceJobRunConditionEntity() {
	}

	@Id
	@Column(name="session_id")
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Id
	@Column(name="jobunit_id")
	public String getJobunit() {
		return jobunit;
	}

	public void setJobunit(String jobunit) {
		this.jobunit = jobunit;
	}

	@Id
	@Column(name="job_id")
	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	@Column(name="run_condition")
	public int getRunCondition() {
		return runCondition;
	}

	public void setRunCondition(int runCondition) {
		this.runCondition = runCondition;
	}

	@Override
	public ResourceJobRunConditionEntity.ResourceJobRunConditionEntityPK getId() {
		return new ResourceJobRunConditionEntity.ResourceJobRunConditionEntityPK(getSessionId(), getJobunit(), getJobId());
	}
}
