/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.queue.internal;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * The persistent class for the log.cc_job_queue_item database table.
 * 
 * @since 6.2.0
 */
@Entity
@Table(name = "cc_job_queue_item", schema = "log")
public class JobQueueItemEntity implements Serializable {
	// 実装を変更したときのバージョン番号に合わせる。
	// {major high}_{major low}_{minor}_{patch}
	private static final long serialVersionUID = 6_02_00_00000000L;

	private JobQueueItemEntityPK id;
	private Integer statusId;
	private JobQueueItemStatus status;
	private Long regDate;
	
	@Deprecated
	public JobQueueItemEntity() {
	}
	
	public JobQueueItemEntity(JobQueueItemEntityPK id) {
		setId(id);
	}
	
	public JobQueueItemEntity(String queueId, String sessionId, String jobunitId, String jobId) {
		this(new JobQueueItemEntityPK(queueId, sessionId, jobunitId, jobId));
	}

	/*-----------------
	 * getter/setter
	 ----------------*/
	@EmbeddedId
	public JobQueueItemEntityPK getId() {
		return id;
	}
	public void setId(JobQueueItemEntityPK id) {
		this.id = id;
	}

	@Column(name="status")
	public Integer getStatusId() {
		return statusId;
	}
	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
		this.status = JobQueueItemStatus.valueOf(statusId);
	}

	@Transient
	public JobQueueItemStatus getStatus() {
		return status;
	}
	public void setStatus(JobQueueItemStatus status) {
		this.statusId = status.getId();
		this.status = status;
	}
	
	@Column(name="reg_date")
	public Long getRegDate() {
		return regDate;
	}
	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}
}