/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.model;

import java.io.Serializable;
import jakarta.persistence.*;

/**
 * The primary key class for the cc_log_async_task database table.
 * 
 */
@Embeddable
public class AsyncTaskEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String worker;
	private Long taskId;

	public AsyncTaskEntityPK() {
	}

	public AsyncTaskEntityPK(String worker, Long taskId) {
		this.setWorker(worker);
		this.setTaskId(taskId);
	}

	@Column(name="worker")
	public String getWorker() {
		return this.worker;
	}
	public void setWorker(String worker) {
		this.worker = worker;
	}

	@Column(name="task_id")
	public Long getTaskId() {
		return this.taskId;
	}
	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AsyncTaskEntityPK)) {
			return false;
		}
		AsyncTaskEntityPK castOther = (AsyncTaskEntityPK)other;
		return
				this.worker.equals(castOther.worker)
				&& this.taskId.equals(castOther.taskId);

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.worker.hashCode();
		hash = hash * prime + this.taskId.hashCode();

		return hash;
	}
}