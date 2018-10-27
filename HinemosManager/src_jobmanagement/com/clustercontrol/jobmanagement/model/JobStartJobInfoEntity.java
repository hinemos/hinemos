/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


/**
 * The persistent class for the cc_job_start_job_info database table.
 * 
 */
@Entity
@Table(name="cc_job_start_job_info", schema="log")
public class JobStartJobInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobStartJobInfoEntityPK id;
	private JobInfoEntity jobInfoEntity;
	private String targetJobDescription;
	private Integer targetJobCrossSessionRange;

	@Deprecated
	public JobStartJobInfoEntity() {
	}

	public JobStartJobInfoEntity(JobStartJobInfoEntityPK pk) {
		this.setId(pk);
	}

	public JobStartJobInfoEntity(JobInfoEntity jobInfoEntity,
			String targetJobunitId,
			String targetJobId,
			Integer targetJobType,
			Integer targetJobEndValue) {
		this(new JobStartJobInfoEntityPK(
				jobInfoEntity.getId().getSessionId(),
				jobInfoEntity.getId().getJobunitId(),
				jobInfoEntity.getId().getJobId(),
				targetJobunitId,
				targetJobId,
				targetJobType,
				targetJobEndValue));
	}


	@EmbeddedId
	public JobStartJobInfoEntityPK getId() {
		return this.id;
	}

	public void setId(JobStartJobInfoEntityPK id) {
		this.id = id;
	}


	//bi-directional many-to-one association to JobInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="job_id", referencedColumnName="job_id", insertable=false, updatable=false),
		@JoinColumn(name="jobunit_id", referencedColumnName="jobunit_id", insertable=false, updatable=false),
		@JoinColumn(name="session_id", referencedColumnName="session_id", insertable=false, updatable=false)
	})
	public JobInfoEntity getJobInfoEntity() {
		return this.jobInfoEntity;
	}

	@Deprecated
	public void setJobInfoEntity(JobInfoEntity jobInfoEntity) {
		this.jobInfoEntity = jobInfoEntity;
	}

	@Column(name="target_job_description")
	public String getTargetJobDescription() {
		return this.targetJobDescription;
	}
	public void setTargetJobDescription(String targetJobDescription) {
		this.targetJobDescription = targetJobDescription;
	}

	@Column(name="target_job_cross_session_range")
	public Integer getTargetJobCrossSessionRange() {
		return targetJobCrossSessionRange;
	}

	public void setTargetJobCrossSessionRange(Integer targetJobCrossSessionRange) {
		this.targetJobCrossSessionRange = targetJobCrossSessionRange;
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
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void relateToJobInfoEntity(JobInfoEntity jobInfoEntity) {
		this.setJobInfoEntity(jobInfoEntity);
		if (jobInfoEntity != null) {
			List<JobStartJobInfoEntity> list = jobInfoEntity.getJobStartJobInfoEntities();
			if (list == null) {
				list = new ArrayList<JobStartJobInfoEntity>();
			} else {
				for (JobStartJobInfoEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobInfoEntity.setJobStartJobInfoEntities(list);
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
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void unchain() {

		// JobInfoEntity
		if (this.jobInfoEntity != null) {
			List<JobStartJobInfoEntity> list = this.jobInfoEntity.getJobStartJobInfoEntities();
			if (list != null) {
				Iterator<JobStartJobInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobStartJobInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}