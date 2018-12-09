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
 * The persistent class for the cc_job_runtime_param_detail database table.
 * 
 */
@Entity
@Table(name="cc_job_runtime_param_detail", schema="setting")
public class JobRuntimeParamDetailEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobRuntimeParamDetailEntityPK id;
	private String paramValue;
	private String description;
	private JobRuntimeParamEntity jobRuntimeParamEntity;

	@Deprecated
	public JobRuntimeParamDetailEntity() {
	}

	public JobRuntimeParamDetailEntity(JobRuntimeParamDetailEntityPK pk) {
		this.setId(pk);
	}

	public JobRuntimeParamDetailEntity(JobRuntimeParamEntity jobRuntimeParamEntity, Integer orderNo) {
		this(new JobRuntimeParamDetailEntityPK(
				jobRuntimeParamEntity.getId().getJobkickId(),
				jobRuntimeParamEntity.getId().getParamId(),
				orderNo));
	}

	@EmbeddedId
	public JobRuntimeParamDetailEntityPK getId() {
		return this.id;
	}
	public void setId(JobRuntimeParamDetailEntityPK id) {
		this.id = id;
	}

	@Column(name="param_value")
	public String getParamValue() {
		return paramValue;
	}
	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	//bi-directional many-to-one association to JobRuntimeParamEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="jobkick_id", referencedColumnName="jobkick_id", insertable=false, updatable=false),
		@JoinColumn(name="param_id", referencedColumnName="param_id", insertable=false, updatable=false)
	})
	public JobRuntimeParamEntity getJobRuntimeParamEntity() {
		return this.jobRuntimeParamEntity;
	}
	@Deprecated
	public void setJobRuntimeParamEntity(JobRuntimeParamEntity jobRuntimeParamEntity) {
		this.jobRuntimeParamEntity = jobRuntimeParamEntity;
	}

	/**
	 * JobRuntimeParamEntityオブジェクト参照設定<BR>
	 * 
	 * JobRuntimeParamEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToJobRuntimeParamEntity(JobRuntimeParamEntity jobRuntimeParamEntity) {
		this.setJobRuntimeParamEntity(jobRuntimeParamEntity);
		if (jobRuntimeParamEntity != null) {
			List<JobRuntimeParamDetailEntity> list = jobRuntimeParamEntity.getJobRuntimeParamDetailEntities();
			if (list == null) {
				list = new ArrayList<JobRuntimeParamDetailEntity>();
			} else {
				for(JobRuntimeParamDetailEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobRuntimeParamEntity.setJobRuntimeParamDetailEntities(list);
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

		// JobRuntimeParamEntity
		if (this.jobRuntimeParamEntity != null) {
			List<JobRuntimeParamDetailEntity> list = this.jobRuntimeParamEntity.getJobRuntimeParamDetailEntities();
			if (list != null) {
				Iterator<JobRuntimeParamDetailEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobRuntimeParamDetailEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
}