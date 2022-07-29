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
 * The persistent class for the cc_job_rpa_response_end_status_mst database
 * table.
 * 
 */
@Entity
@Table(name = "cc_job_rpa_check_end_value_mst", schema = "setting")
@Cacheable(true)
public class JobRpaCheckEndValueMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobRpaCheckEndValueMstEntityPK id;
	/**
	 * 終了状態<br>
	 * RPA管理ツールのレスポンスに含まれる値
	 */
	private String endStatus;
	/** 終了値 */
	private Integer endValue;
	private JobMstEntity jobMstEntity;
	/** RPA管理ツールリクエストパラメータ */
	private RpaManagementToolEndStatusMst rpaManagementToolEndStatusMst;

	public JobRpaCheckEndValueMstEntity() {
	}

	public JobRpaCheckEndValueMstEntity(JobRpaCheckEndValueMstEntityPK pk) {
		this.setId(pk);
	}

	public JobRpaCheckEndValueMstEntity(JobMstEntity jobMstEntity, Integer endStatusId) {
		this.setId(new JobRpaCheckEndValueMstEntityPK(jobMstEntity.getId().getJobunitId(),
				jobMstEntity.getId().getJobId(), endStatusId));
	}

	@EmbeddedId
	public JobRpaCheckEndValueMstEntityPK getId() {
		return id;
	}

	public void setId(JobRpaCheckEndValueMstEntityPK id) {
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

	// bi-directional many-to-one association to JobMstEntity
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({ @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false),
			@JoinColumn(name = "jobunit_id", referencedColumnName = "jobunit_id", insertable = false, updatable = false) })
	public JobMstEntity getJobMstEntity() {
		return jobMstEntity;
	}

	@Deprecated
	public void setJobMstEntity(JobMstEntity jobMstEntity) {
		this.jobMstEntity = jobMstEntity;
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
	 * JobMstEntityオブジェクト参照設定<BR>
	 * 
	 * JobMstEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToJobMstEntity(JobMstEntity jobMstEntity) {
		this.setJobMstEntity(jobMstEntity);
		if (jobMstEntity != null) {
			List<JobRpaCheckEndValueMstEntity> list = jobMstEntity.getJobRpaCheckEndValueMstEntities();
			if (list == null) {
				list = new ArrayList<JobRpaCheckEndValueMstEntity>();
			} else {
				for (JobRpaCheckEndValueMstEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobMstEntity.setJobRpaCheckEndValueMstEntities(list);
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

		// JobMstEntity
		if (this.jobMstEntity != null) {
			List<JobRpaCheckEndValueMstEntity> list = this.jobMstEntity.getJobRpaCheckEndValueMstEntities();
			if (list != null) {
				Iterator<JobRpaCheckEndValueMstEntity> iter = list.iterator();
				while (iter.hasNext()) {
					JobRpaCheckEndValueMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())) {
						iter.remove();
						break;
					}
				}
			}
		}
	}
}
