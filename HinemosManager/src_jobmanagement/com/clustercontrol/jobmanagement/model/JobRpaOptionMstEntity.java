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
 * The persistent class for the cc_job_param_mst database table.
 * 
 */
@Entity
@Table(name="cc_job_rpa_option_mst", schema="setting")
@Cacheable(true)
public class JobRpaOptionMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobRpaOptionMstEntityPK id;
	/** 実行パラメータ */
	private String option;
	/** 説明 */
	private String description;
	private JobMstEntity jobMstEntity;
	
	public JobRpaOptionMstEntity() {
	}

	public JobRpaOptionMstEntity(JobRpaOptionMstEntityPK pk) {
		this.setId(pk);
	}

	public JobRpaOptionMstEntity(JobMstEntity jobMstEntity, Integer order) {
		this(new JobRpaOptionMstEntityPK(
				jobMstEntity.getId().getJobunitId(),
				jobMstEntity.getId().getJobId(),
				order));
	}

	@EmbeddedId
	public JobRpaOptionMstEntityPK getId() {
		return id;
	}

	public void setId(JobRpaOptionMstEntityPK id) {
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

	public void setJobMstEntity(JobMstEntity jobMstEntity) {
		this.jobMstEntity = jobMstEntity;
	}

	//bi-directional many-to-one association to JobMstEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="job_id", referencedColumnName="job_id", insertable=false, updatable=false),
		@JoinColumn(name="jobunit_id", referencedColumnName="jobunit_id", insertable=false, updatable=false)
	})
	public JobMstEntity getJobMstEntity() {
		return this.jobMstEntity;
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
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void relateToJobMstEntity(JobMstEntity jobMstEntity) {
		this.setJobMstEntity(jobMstEntity);
		if (jobMstEntity != null) {
			List<JobRpaOptionMstEntity> list = jobMstEntity.getJobRpaOptionMstEntities();
			if (list == null) {
				list = new ArrayList<JobRpaOptionMstEntity>();
			} else {
				for(JobRpaOptionMstEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobMstEntity.setJobRpaOptionMstEntities(list);
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

		// JobMstEntity
		if (this.jobMstEntity != null) {
			List<JobRpaOptionMstEntity> list = this.jobMstEntity.getJobRpaOptionMstEntities();
			if (list != null) {
				Iterator<JobRpaOptionMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobRpaOptionMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}
