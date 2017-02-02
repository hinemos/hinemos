package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_job_session database table.
 *
 */
@Entity
@Table(name="cc_job_session", schema="log")
public class JobSessionEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String sessionId;
	private String jobId;
	private String jobunitId;
	private Integer operationFlg;
	private Long scheduleDate;
	private String triggerInfo;
	private Integer triggerType;
	private List<JobSessionJobEntity> jobSessionJobEntities;
	private Long position;

	@Deprecated
	public JobSessionEntity() {
	}

	public JobSessionEntity(String sessionId) {
		this.setSessionId(sessionId);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}


	@Id
	@Column(name="session_id")
	public String getSessionId() {
		return this.sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}


	@Column(name="job_id")
	public String getJobId() {
		return this.jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}


	@Column(name="jobunit_id")
	public String getJobunitId() {
		return this.jobunitId;
	}

	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}


	@Column(name="operation_flg")
	public Integer getOperationFlg() {
		return this.operationFlg;
	}

	public void setOperationFlg(Integer operationFlg) {
		this.operationFlg = operationFlg;
	}


	@Column(name="schedule_date")
	public Long getScheduleDate() {
		return this.scheduleDate;
	}

	public void setScheduleDate(Long scheduleDate) {
		this.scheduleDate = scheduleDate;
	}


	@Column(name="trigger_info")
	public String getTriggerInfo() {
		return this.triggerInfo;
	}

	public void setTriggerInfo(String triggerInfo) {
		this.triggerInfo = triggerInfo;
	}


	@Column(name="trigger_type")
	public Integer getTriggerType() {
		return this.triggerType;
	}

	public void setTriggerType(Integer triggerType) {
		this.triggerType = triggerType;
	}


	//bi-directional many-to-one association to JobSessionJobEntity
	@OneToMany(mappedBy="jobSessionEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobSessionJobEntity> getJobSessionJobEntities() {
		return this.jobSessionJobEntities;
	}

	public void setJobSessionJobEntities(List<JobSessionJobEntity> jobSessionJobEntities) {
		this.jobSessionJobEntities = jobSessionJobEntities;
	}
	
	@Column(name="position", insertable=false)
	public Long getPosition(){
		return this.position;
	}
	public void setPosition(Long position){
		this.position = position;
	}
}