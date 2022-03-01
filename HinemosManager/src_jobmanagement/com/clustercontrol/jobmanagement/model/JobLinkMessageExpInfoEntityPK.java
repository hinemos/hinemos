/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;

import jakarta.persistence.*;

/**
 * The primary key class for the cc_joblink_message_exp_info database table.
 * 
 */
@Embeddable
public class JobLinkMessageExpInfoEntityPK implements Serializable {

	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	private String joblinkMessageId;
	private String facilityId;
	private Long sendDate;
	private String key;

	public JobLinkMessageExpInfoEntityPK() {
	}

	public JobLinkMessageExpInfoEntityPK(
			String joblinkMessageId,
			String facilityId,
			Long sendDate,
			String key) {
		this.setJoblinkMessageId(joblinkMessageId);
		this.setFacilityId(facilityId);
		this.setSendDate(sendDate);
		this.setKey(key);
	}

	@Column(name="joblink_message_id")
	public String getJoblinkMessageId() {
		return this.joblinkMessageId;
	}
	public void setJoblinkMessageId(String joblinkMessageId) {
		this.joblinkMessageId = joblinkMessageId;
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="send_date")
	public Long getSendDate() {
		return this.sendDate;
	}
	public void setSendDate(Long sendDate) {
		this.sendDate = sendDate;
	}

	@Column(name="key")
	public String getKey() {
		return this.key;
	}
	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof JobLinkMessageExpInfoEntityPK)) {
			return false;
		}
		JobLinkMessageExpInfoEntityPK castOther = (JobLinkMessageExpInfoEntityPK)other;
		return
				this.joblinkMessageId.equals(castOther.joblinkMessageId)
				&& this.facilityId.equals(castOther.facilityId)
				&& this.sendDate.equals(castOther.sendDate)
				&& this.key.equals(castOther.key);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.joblinkMessageId.hashCode();
		hash = hash * prime + this.facilityId.hashCode();
		hash = hash * prime + this.sendDate.hashCode();
		hash = hash * prime + this.key.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		return "JobLinkMessageExpInfoEntityPK ["
				+ "joblinkMessageId=" + joblinkMessageId
				+ ", facilityId=" + facilityId
				+ ", sendDate=" + sendDate
				+ ", key=" + key + "]";
	}
}