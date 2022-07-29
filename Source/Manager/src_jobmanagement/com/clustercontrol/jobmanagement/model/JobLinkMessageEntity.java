/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;


/**
 * The persistent class for the cc_job_link_message database table.
 *
 */
@Entity
@Table(name="cc_job_link_message", schema="log")
public class JobLinkMessageEntity  implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobLinkMessageEntityPK id;
	private String ipAddress;
	private String facilityName;
	private Long acceptDate;
	private String monitorDetailId;
	private String application;
	private Integer priority;
	private String message;
	private String messageOrg;
	private Long position;
	private boolean match = false;
	private List<JobLinkMessageExpInfoEntity> jobLinkMessageExpInfoEntities = new ArrayList<>();

	@Deprecated
	public JobLinkMessageEntity() {
	}

	public JobLinkMessageEntity(JobLinkMessageEntityPK pk) {
		this.setId(pk);
	}

	public JobLinkMessageEntity(
			String joblinkMessageId,
			String facilityId,
			Long sendDate) {
		this(new JobLinkMessageEntityPK(joblinkMessageId, facilityId, sendDate));
	}

	@EmbeddedId
	public JobLinkMessageEntityPK getId() {
		return this.id;
	}
	public void setId(JobLinkMessageEntityPK id) {
		this.id = id;
	}

	@Column(name="ip_address")
	public String getIpAddress() {
		return this.ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	@Column(name="facility_name")
	public String getFacilityName() {
		return facilityName;
	}
	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}

	@Column(name="accept_date")
	public Long getAcceptDate() {
		return acceptDate;
	}
	public void setAcceptDate(Long acceptDate) {
		this.acceptDate = acceptDate;
	}

	@Column(name="monitor_detail_id")
	public String getMonitorDetailId() {
		return monitorDetailId;
	}
	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	@Column(name="application")
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}

	@Column(name="priority")
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	@Column(name="message")
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	@Column(name="message_org")
	public String getMessageOrg() {
		return messageOrg;
	}
	public void setMessageOrg(String messageOrg) {
		this.messageOrg = messageOrg;
	}

	@XmlTransient
	@Column(name="position", insertable=false)
	public Long getPosition(){
		return this.position;
	}
	public void setPosition(Long position){
		this.position = position;
	}

	@Transient
	public boolean isMatch() {
		return match;
	}

	public void setMatch(boolean match) {
		this.match = match;
	}

	//bi-directional many-to-one association to JobLinkMessageExpInfoEntity
	@OneToMany(mappedBy="jobLinkMessageEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobLinkMessageExpInfoEntity> getJobLinkMessageExpInfoEntities() {
		return this.jobLinkMessageExpInfoEntities;
	}

	public void setJobLinkMessageExpInfoEntities(List<JobLinkMessageExpInfoEntity> jobLinkMessageExpInfoEntities) {
		this.jobLinkMessageExpInfoEntities = jobLinkMessageExpInfoEntities;
	}
}