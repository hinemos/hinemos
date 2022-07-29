/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;




/**
 * The persistent class for the cc_job_edit database table.
 * 
 */
@Entity
@Table(name="cc_job_edit", schema="setting")
@Cacheable(true)
public class JobEditEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String jobunitId;
	private String lockUser = null;
	private String lockIpAddress = null;
	private Integer editSession = null;


	@Deprecated
	public JobEditEntity() {
	}

	public JobEditEntity(String jobunitId) {
		this.setJobunitId(jobunitId);
	}

	@Id
	@Column(name="jobunit_id")
	public String getJobunitId() {
		return jobunitId;
	}

	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	@Column(name="lock_user")
	public String getLockUser() {
		return lockUser;
	}

	public void setLockUser(String lockUser) {
		this.lockUser = lockUser;
	}

	@Column(name="lock_ip_address")
	public String getLockIpAddress() {
		return lockIpAddress;
	}

	public void setLockIpAddress(String lockIpAddress) {
		this.lockIpAddress = lockIpAddress;
	}

	@Column(name="edit_session")
	public Integer getEditSession() {
		return editSession;
	}

	public void setEditSession(Integer editSession) {
		this.editSession= editSession;
	}
}