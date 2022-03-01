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
 * The persistent class for the cc_job_param_info database table.
 * 
 */
@Entity
@Table(name="cc_job_rpa_option_info", schema="log")
@Cacheable(true)
public class JobRpaOptionInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobRpaOptionInfoEntityPK id;
	/** 実行パラメータ */
	private String option;
	/** 説明 */
	private String description;
	private JobInfoEntity jobInfoEntity;
	
	public JobRpaOptionInfoEntity() {
	}

	public JobRpaOptionInfoEntity(JobRpaOptionInfoEntityPK pk) {
		this.setId(pk);
	}

	public JobRpaOptionInfoEntity(JobInfoEntity jobInfoEntity, Integer order) {
		this(new JobRpaOptionInfoEntityPK(
				jobInfoEntity.getId().getSessionId(),
				jobInfoEntity.getId().getJobunitId(),
				jobInfoEntity.getId().getJobId(),
				order));
	}

	@EmbeddedId
	public JobRpaOptionInfoEntityPK getId() {
		return id;
	}

	public void setId(JobRpaOptionInfoEntityPK id) {
		this.id = id;
	}

	/**
	 * @return 実行パラメータを返します。
	 */
	@Column(name="option")
	public String getOption() {
		return option;
	}

	/**
	 * @param option
	 *            実行パラメータを設定します。
	 */
	public void setOption(String option) {
		this.option = option;
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

	public void setJobInfoEntity(JobInfoEntity jobInfoEntity) {
		this.jobInfoEntity = jobInfoEntity;
	}

	//bi-directional many-to-one association to JobInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="session_id", referencedColumnName="session_id", insertable=false, updatable=false),
		@JoinColumn(name="job_id", referencedColumnName="job_id", insertable=false, updatable=false),
		@JoinColumn(name="jobunit_id", referencedColumnName="jobunit_id", insertable=false, updatable=false)
	})
	public JobInfoEntity getJobInfoEntity() {
		return this.jobInfoEntity;
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
			List<JobRpaOptionInfoEntity> list = jobInfoEntity.getJobRpaOptionInfoEntities();
			if (list == null) {
				list = new ArrayList<JobRpaOptionInfoEntity>();
			} else {
				for(JobRpaOptionInfoEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobInfoEntity.setJobRpaOptionInfoEntities(list);
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
			List<JobRpaOptionInfoEntity> list = this.jobInfoEntity.getJobRpaOptionInfoEntities();
			if (list != null) {
				Iterator<JobRpaOptionInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobRpaOptionInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}
