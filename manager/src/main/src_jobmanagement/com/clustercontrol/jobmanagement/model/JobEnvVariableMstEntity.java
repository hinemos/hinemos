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

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


/**
 * The persistent class for the cc_job_env_variable_mst database table.
 * 
 */
@Entity
@Table(name="cc_job_env_variable_mst", schema="setting")
@Cacheable(true)
public class JobEnvVariableMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobEnvVariableMstEntityPK id;
	private String description;
	private String value;
	private JobMstEntity jobMstEntity;

	@Deprecated
	public JobEnvVariableMstEntity() {
	}

	public JobEnvVariableMstEntity(JobEnvVariableMstEntityPK pk) {
		this.setId(pk);
	}

	public JobEnvVariableMstEntity(JobMstEntity jobMstEntity, String envVariableId) {
		this(new JobEnvVariableMstEntityPK(
				jobMstEntity.getId().getJobunitId(),
				jobMstEntity.getId().getJobId(),
				envVariableId));
	}


	@EmbeddedId
	public JobEnvVariableMstEntityPK getId() {
		return this.id;
	}

	public void setId(JobEnvVariableMstEntityPK id) {
		this.id = id;
	}

	@Column(name="description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="value")
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
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

	@Deprecated
	public void setJobMstEntity(JobMstEntity jobMstEntity) {
		this.jobMstEntity = jobMstEntity;
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
			List<JobEnvVariableMstEntity> list = jobMstEntity.getJobEnvVariableMstEntities();
			if (list == null) {
				list = new ArrayList<JobEnvVariableMstEntity>();
			} else {
				for(JobEnvVariableMstEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobMstEntity.setJobEnvVariableMstEntities(list);
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
			List<JobEnvVariableMstEntity> list = this.jobMstEntity.getJobEnvVariableMstEntities();
			if (list != null) {
				Iterator<JobEnvVariableMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobEnvVariableMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}