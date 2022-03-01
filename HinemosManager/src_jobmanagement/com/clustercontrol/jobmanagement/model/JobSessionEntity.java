/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


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
	private Long sessionGenerateDate;
	private String sessionGenerateJobkickId;
	private Boolean expNodeRuntimeFlg;
	private List<JobSessionJobEntity> jobSessionJobEntities;
	private Long position;

	@Deprecated
	public JobSessionEntity() {
	}

	public JobSessionEntity(String sessionId) {
		this.setSessionId(sessionId);
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

	@Column(name="session_generate_date")
	public Long getSessionGenerateDate() {
		return sessionGenerateDate;
	}

	public void setSessionGenerateDate(Long sessionGenerateDate) {
		this.sessionGenerateDate = sessionGenerateDate;
	}

	@Column(name="session_generate_jobkick_id")
	public String getSessionGenerateJobkickId() {
		return sessionGenerateJobkickId;
	}

	public void setSessionGenerateJobkickId(String sessionGenerateJobkickId) {
		this.sessionGenerateJobkickId = sessionGenerateJobkickId;
	}

	@Column(name="exp_node_runtime_flg")
	public Boolean getExpNodeRuntimeFlg() {
		return expNodeRuntimeFlg;
	}

	public void setExpNodeRuntimeFlg(Boolean expNodeRuntimeFlg) {
		this.expNodeRuntimeFlg = expNodeRuntimeFlg;
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