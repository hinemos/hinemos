/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.repository.session.RepositoryControllerBean;



/**
 * The persistent class for the cc_notify_job_info database table.
 * 
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
@Entity
@Table(name="cc_notify_job_info", schema="setting")
@Cacheable(true)
public class NotifyJobInfo extends NotifyInfoDetail implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer jobExecFacilityFlg;

	private String infoJobunitId;
	private String warnJobunitId;
	private String criticalJobunitId;
	private String unknownJobunitId;

	private String infoJobId;
	private String warnJobId;
	private String criticalJobId;
	private String unknownJobId;

	private Integer infoJobFailurePriority;
	private Integer warnJobFailurePriority;
	private Integer criticalJobFailurePriority;
	private Integer unknownJobFailurePriority;

	private String jobExecFacilityId;

	private String jobExecScope;
	
	public NotifyJobInfo() {
	}

	public NotifyJobInfo(String notifyId) {
		super(notifyId);
	}

	@Column(name="job_exec_facility_flg")
	public Integer getJobExecFacilityFlg() {
		return this.jobExecFacilityFlg;
	}

	public void setJobExecFacilityFlg(Integer jobExecFacilityFlg) {
		this.jobExecFacilityFlg = jobExecFacilityFlg;
	}

	@Column(name="info_jobunit_id")
	public String getInfoJobunitId() {
		return this.infoJobunitId;
	}

	public void setInfoJobunitId(String infoJobunitId) {
		this.infoJobunitId = infoJobunitId;
	}

	@Column(name="warn_jobunit_id")
	public String getWarnJobunitId() {
		return this.warnJobunitId;
	}

	public void setWarnJobunitId(String warnJobunitId) {
		this.warnJobunitId = warnJobunitId;
	}

	@Column(name="critical_jobunit_id")
	public String getCriticalJobunitId() {
		return this.criticalJobunitId;
	}

	public void setCriticalJobunitId(String criticalJobunitId) {
		this.criticalJobunitId = criticalJobunitId;
	}

	@Column(name="unknown_jobunit_id")
	public String getUnknownJobunitId() {
		return this.unknownJobunitId;
	}

	public void setUnknownJobunitId(String unknownJobunitId) {
		this.unknownJobunitId = unknownJobunitId;
	}

	@Column(name="info_job_id")
	public String getInfoJobId() {
		return this.infoJobId;
	}

	public void setInfoJobId(String infoJobId) {
		this.infoJobId = infoJobId;
	}

	@Column(name="warn_job_id")
	public String getWarnJobId() {
		return this.warnJobId;
	}

	public void setWarnJobId(String warnJobId) {
		this.warnJobId = warnJobId;
	}

	@Column(name="critical_job_id")
	public String getCriticalJobId() {
		return this.criticalJobId;
	}

	public void setCriticalJobId(String criticalJobId) {
		this.criticalJobId = criticalJobId;
	}

	@Column(name="unknown_job_id")
	public String getUnknownJobId() {
		return this.unknownJobId;
	}

	public void setUnknownJobId(String unknownJobId) {
		this.unknownJobId = unknownJobId;
	}


	@Column(name="info_job_failure_priority")
	public Integer getInfoJobFailurePriority() {
		return this.infoJobFailurePriority;
	}

	public void setInfoJobFailurePriority(Integer infoJobFailurePriority) {
		this.infoJobFailurePriority = infoJobFailurePriority;
	}

	@Column(name="warn_job_failure_priority")
	public Integer getWarnJobFailurePriority() {
		return this.warnJobFailurePriority;
	}

	public void setWarnJobFailurePriority(Integer warnJobFailurePriority) {
		this.warnJobFailurePriority = warnJobFailurePriority;
	}

	@Column(name="critical_job_failure_priority")
	public Integer getCriticalJobFailurePriority() {
		return this.criticalJobFailurePriority;
	}

	public void setCriticalJobFailurePriority(Integer criticalJobFailurePriority) {
		this.criticalJobFailurePriority = criticalJobFailurePriority;
	}

	@Column(name="unknown_job_failure_priority")
	public Integer getUnknownJobFailurePriority() {
		return this.unknownJobFailurePriority;
	}

	public void setUnknownJobFailurePriority(Integer unknownJobFailurePriority) {
		this.unknownJobFailurePriority = unknownJobFailurePriority;
	}


	@Column(name="job_exec_facility")
	public String getJobExecFacility() {
		return this.jobExecFacilityId;
	}

	public void setJobExecFacility(String jobExecFacilityId) {
		this.jobExecFacilityId = jobExecFacilityId;
	}
	
	@Transient
	public String getJobExecScope() {
		if (jobExecScope == null)
			try {
				jobExecScope = new RepositoryControllerBean().getFacilityPath(getJobExecFacility(), null);
			} catch (HinemosUnknown e) {
				Logger.getLogger(this.getClass()).debug(e.getMessage(), e);
			}
		return jobExecScope;
	}

	public void setJobExecScope(String jobExecScope) {
		this.jobExecScope = jobExecScope;
	}

	@Override
	protected void chainNotifyInfo() {
		getNotifyInfoEntity().setNotifyJobInfo(this);
	}

	@Override
	protected void unchainNotifyInfo() {
		getNotifyInfoEntity().setNotifyJobInfo(null);
	}
}