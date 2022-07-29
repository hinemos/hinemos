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

import com.clustercontrol.jobmanagement.bean.ConditionTypeConstant;

import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


/**
 * The persistent class for the cc_job_wait_group_info database table.
 * 
 */
@Entity
@Table(name="cc_job_wait_group_info", schema="log")
@Cacheable(true)
public class JobWaitGroupInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobWaitGroupInfoEntityPK id;
	private Integer conditionType = ConditionTypeConstant.TYPE_AND;
	private Boolean isGroup = false;
	private List<JobWaitInfoEntity> jobWaitInfoEntities;
	private JobInfoEntity jobInfoEntity;

	@Deprecated
	public JobWaitGroupInfoEntity() {
	}

	public JobWaitGroupInfoEntity(JobWaitGroupInfoEntityPK pk) {
		this.setId(pk);
	}

	public JobWaitGroupInfoEntity(JobInfoEntity jobInfoEntity, Integer orderNo) {
		this(new JobWaitGroupInfoEntityPK(
				jobInfoEntity.getId().getSessionId(),
				jobInfoEntity.getId().getJobunitId(),
				jobInfoEntity.getId().getJobId(),
				orderNo));
	}


	@EmbeddedId
	public JobWaitGroupInfoEntityPK getId() {
		return this.id;
	}

	public void setId(JobWaitGroupInfoEntityPK id) {
		this.id = id;
	}

	@Column(name="condition_type")
	public Integer getConditionType() {
		return conditionType;
	}
	public void setConditionType(Integer conditionType) {
		this.conditionType = conditionType;
	}

	@Column(name="is_group")
	public Boolean getIsGroup() {
		return isGroup;
	}
	public void setIsGroup(Boolean isGroup) {
		this.isGroup = isGroup;
	}

	//bi-directional many-to-one association to JobWaitInfoEntity
	@OneToMany(mappedBy="jobWaitGroupInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobWaitInfoEntity> getJobWaitInfoEntities() {
		return this.jobWaitInfoEntities;
	}

	public void setJobWaitInfoEntities(List<JobWaitInfoEntity> jobWaitInfoEntities) {
		this.jobWaitInfoEntities = jobWaitInfoEntities;
	}

	//bi-directional many-to-one association to JobInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="session_id", referencedColumnName="session_id", insertable=false, updatable=false),
		@JoinColumn(name="jobunit_id", referencedColumnName="jobunit_id", insertable=false, updatable=false),
		@JoinColumn(name="job_id", referencedColumnName="job_id", insertable=false, updatable=false)
	})
	public JobInfoEntity getJobInfoEntity() {
		return this.jobInfoEntity;
	}

	@Deprecated
	public void setJobInfoEntity(JobInfoEntity jobInfoEntity) {
		this.jobInfoEntity = jobInfoEntity;
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
			List<JobWaitGroupInfoEntity> list = jobInfoEntity.getJobWaitGroupInfoEntities();
			if (list == null) {
				list = new ArrayList<JobWaitGroupInfoEntity>();
			} else {
				for(JobWaitGroupInfoEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobInfoEntity.setJobWaitGroupInfoEntities(list);
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
		if (this.jobInfoEntity != null) {
			List<JobWaitGroupInfoEntity> list = this.jobInfoEntity.getJobWaitGroupInfoEntities();
			if (list != null) {
				Iterator<JobWaitGroupInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobWaitGroupInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}