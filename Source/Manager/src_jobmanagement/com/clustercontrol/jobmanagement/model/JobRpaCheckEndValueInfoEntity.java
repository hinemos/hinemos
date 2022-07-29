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
import java.util.Iterator;
import java.util.List;

import com.clustercontrol.rpa.model.RpaManagementToolEndStatusMst;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The persistent class for the cc_job_rpa_response_end_status_info database
 * table.
 * 
 */
@Entity
@Table(name = "cc_job_rpa_check_end_value_info", schema = "log")
@Cacheable(true)
public class JobRpaCheckEndValueInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobRpaCheckEndValueInfoEntityPK id;
	/**
	 * 終了状態<br>
	 * RPA管理ツールのレスポンスに含まれる値
	 */
	private String endStatus;
	/** 終了値 */
	private Integer endValue;
	private JobInfoEntity jobInfoEntity;
	/** RPA管理ツールリクエストパラメータ */
	private RpaManagementToolEndStatusMst rpaManagementToolEndStatusMst;

	public JobRpaCheckEndValueInfoEntity() {
	}

	public JobRpaCheckEndValueInfoEntity(JobRpaCheckEndValueInfoEntityPK pk) {
		this.setId(pk);
	}

	public JobRpaCheckEndValueInfoEntity(JobInfoEntity jobInfoEntity, Integer endStatusId) {
		this.setId(new JobRpaCheckEndValueInfoEntityPK(jobInfoEntity.getId().getSessionId(),
				jobInfoEntity.getId().getJobunitId(), jobInfoEntity.getId().getJobId(), endStatusId));
	}

	@EmbeddedId
	public JobRpaCheckEndValueInfoEntityPK getId() {
		return id;
	}

	public void setId(JobRpaCheckEndValueInfoEntityPK id) {
		this.id = id;
	}

	/**
	 * @return 終了状態を返します。
	 */
	@Column(name = "end_status")
	public String getEndStatus() {
		return endStatus;
	}

	/**
	 * @param endStatus
	 *            終了状態を設定します。
	 */
	public void setEndStatus(String endStatus) {
		this.endStatus = endStatus;
	}

	/**
	 * @return 終了値を返します。
	 */
	@Column(name = "end_value")
	public Integer getEndValue() {
		return endValue;
	}

	/**
	 * @param endValue
	 *            終了値を設定します。
	 */
	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}

	// bi-directional many-to-one association to JobInfoEntity
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
			@JoinColumn(name = "session_id", referencedColumnName = "session_id", insertable = false, updatable = false),
			@JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false),
			@JoinColumn(name = "jobunit_id", referencedColumnName = "jobunit_id", insertable = false, updatable = false) })
	public JobInfoEntity getJobInfoEntity() {
		return jobInfoEntity;
	}

	@Deprecated
	public void setJobInfoEntity(JobInfoEntity jobInfoEntity) {
		this.jobInfoEntity = jobInfoEntity;
	}

	// uni-directional many-to-one association to
	// RpaManagementToolEndStatusMst
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "end_status_id", referencedColumnName = "end_status_id", insertable = false, updatable = false)
	public RpaManagementToolEndStatusMst getRpaManagementToolEndStatusMst() {
		return rpaManagementToolEndStatusMst;
	}

	public void setRpaManagementToolEndStatusMst(
			RpaManagementToolEndStatusMst rpaManagementToolEndStatusMst) {
		this.rpaManagementToolEndStatusMst = rpaManagementToolEndStatusMst;
	}

	/**
	 * JobInfoEntityオブジェクト参照設定<BR>
	 * 
	 * JobInfoEntity設定時はSetterに代わりこちらを使用すること。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship. It is
	 * the developer’s responsibility to keep the in-memory references held on
	 * the owning side and those held on the inverse side consistent with each
	 * other when they change.
	 */
	public void relateToJobInfoEntity(JobInfoEntity jobInfoEntity) {
		this.setJobInfoEntity(jobInfoEntity);
		if (jobInfoEntity != null) {
			List<JobRpaCheckEndValueInfoEntity> list = jobInfoEntity.getJobRpaCheckEndValueInfoEntities();
			if (list == null) {
				list = new ArrayList<JobRpaCheckEndValueInfoEntity>();
			} else {
				for (JobRpaCheckEndValueInfoEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobInfoEntity.setJobRpaCheckEndValueInfoEntities(list);
		}
	}

	/**
	 * 削除前処理<BR>
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship. It is
	 * the developer’s responsibility to keep the in-memory references held on
	 * the owning side and those held on the inverse side consistent with each
	 * other when they change.
	 */
	public void unchain() {

		// JobInfoEntity
		if (this.jobInfoEntity != null) {
			List<JobRpaCheckEndValueInfoEntity> list = this.jobInfoEntity.getJobRpaCheckEndValueInfoEntities();
			if (list != null) {
				Iterator<JobRpaCheckEndValueInfoEntity> iter = list.iterator();
				while (iter.hasNext()) {
					JobRpaCheckEndValueInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())) {
						iter.remove();
						break;
					}
				}
			}
		}
	}

}
