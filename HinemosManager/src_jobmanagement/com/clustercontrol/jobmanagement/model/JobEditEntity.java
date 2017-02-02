package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;



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
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
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