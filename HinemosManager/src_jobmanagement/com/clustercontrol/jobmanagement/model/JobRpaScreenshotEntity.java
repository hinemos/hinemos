/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The persistent class for the cc_job_rpa_screenshot database table.
 * 
 */
@Entity
@Table(name="cc_job_rpa_screenshot", schema="binarydata")
public class JobRpaScreenshotEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobRpaScreenshotEntityPK id;
	/** 画像データ */
	private byte[] filedata;
	/** 説明 */
	private String description;
	private JobSessionNodeEntity jobSessionNodeEntity;

	public JobRpaScreenshotEntity() {
	}

	public JobRpaScreenshotEntity(JobRpaScreenshotEntityPK id) {
		this.id = id;
	}

	@EmbeddedId
	public JobRpaScreenshotEntityPK getId() {
		return id;
	}

	public void setId(JobRpaScreenshotEntityPK id) {
		this.id = id;
	}

	/**
	 * @return 画像データを返します。
	 */
	@Basic(fetch=FetchType.LAZY)
	@Column(name="filedata")
	public byte[] getFiledata() {
		// 画像データは参照したタイミングで取得する
		return filedata;
	}

	/**
	 * @param filedata
	 *            画像データを設定します。
	 */
	public void setFiledata(byte[] filedata) {
		this.filedata = filedata;
	}

	/**
	 * @return 説明を返します。
	 */
	@Column(name="description")
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            説明を設定します。
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	//bi-directional many-to-one association to JobSessionJobEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="facility_id", referencedColumnName="facility_id", insertable=false, updatable=false),
		@JoinColumn(name="job_id", referencedColumnName="job_id", insertable=false, updatable=false),
		@JoinColumn(name="jobunit_id", referencedColumnName="jobunit_id", insertable=false, updatable=false),
		@JoinColumn(name="session_id", referencedColumnName="session_id", insertable=false, updatable=false)
	})
	public JobSessionNodeEntity getJobSessionNodeEntity() {
		return this.jobSessionNodeEntity;
	}

	@Deprecated
	public void setJobSessionNodeEntity(JobSessionNodeEntity jobSessionNodeEntity) {
		this.jobSessionNodeEntity = jobSessionNodeEntity;
	}
}
