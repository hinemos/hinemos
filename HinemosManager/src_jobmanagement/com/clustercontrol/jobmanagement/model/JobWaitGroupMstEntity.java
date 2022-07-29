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
 * The persistent class for the cc_job_wait_group_mst database table.
 * 
 */
@Entity
@Table(name="cc_job_wait_group_mst", schema="setting")
@Cacheable(true)
public class JobWaitGroupMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobWaitGroupMstEntityPK id;
	private Integer conditionType = ConditionTypeConstant.TYPE_AND;
	private Boolean isGroup = false;
	private List<JobWaitMstEntity> jobWaitMstEntities;
	private JobMstEntity jobMstEntity;

	@Deprecated
	public JobWaitGroupMstEntity() {
	}

	public JobWaitGroupMstEntity(JobWaitGroupMstEntityPK pk) {
		this.setId(pk);
	}

	public JobWaitGroupMstEntity(JobMstEntity jobMstEntity, Integer orderNo) {
		this(new JobWaitGroupMstEntityPK(
				jobMstEntity.getId().getJobunitId(),
				jobMstEntity.getId().getJobId(),
				orderNo));
	}


	@EmbeddedId
	public JobWaitGroupMstEntityPK getId() {
		return this.id;
	}

	public void setId(JobWaitGroupMstEntityPK id) {
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

	//bi-directional many-to-one association to JobWaitMstEntity
	@OneToMany(mappedBy="jobWaitGroupMstEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobWaitMstEntity> getJobWaitMstEntities() {
		return this.jobWaitMstEntities;
	}

	public void setJobWaitMstEntities(List<JobWaitMstEntity> jobWaitMstEntities) {
		this.jobWaitMstEntities = jobWaitMstEntities;
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
			List<JobWaitGroupMstEntity> list = jobMstEntity.getJobWaitGroupMstEntities();
			if (list == null) {
				list = new ArrayList<JobWaitGroupMstEntity>();
			} else {
				for(JobWaitGroupMstEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobMstEntity.setJobWaitGroupMstEntities(list);
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
			List<JobWaitGroupMstEntity> list = this.jobMstEntity.getJobWaitGroupMstEntities();
			if (list != null) {
				Iterator<JobWaitGroupMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobWaitGroupMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}