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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;



/**
 * The persistent class for the cc_job_runtime_param database table.
 * 
 */
@Entity
@Table(name="cc_job_runtime_param", schema="setting")
public class JobRuntimeParamEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobRuntimeParamEntityPK id;
	private Integer paramType;
	private String defaultValue;
	private String description;
	private Boolean requiredFlg;
	private JobKickEntity jobKickEntity;
	private List<JobRuntimeParamDetailEntity> jobRuntimeParamDetailEntities;

	@Deprecated
	public JobRuntimeParamEntity() {
	}

	public JobRuntimeParamEntity(JobRuntimeParamEntityPK pk) {
		this.setId(pk);
	}

	public JobRuntimeParamEntity(JobKickEntity jobKickEntity, String paramId) {
		this(new JobRuntimeParamEntityPK(
				jobKickEntity.getJobkickId(),
				paramId));
	}

	@EmbeddedId
	public JobRuntimeParamEntityPK getId() {
		return this.id;
	}
	public void setId(JobRuntimeParamEntityPK id) {
		this.id = id;
	}

	@Column(name="param_type")
	public Integer getParamType() {
		return paramType;
	}
	public void setParamType(Integer paramType) {
		this.paramType = paramType;
	}

	@Column(name="default_value")
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="required_flg")
	public Boolean getRequiredFlg() {
		return requiredFlg;
	}
	public void setRequiredFlg(Boolean requiredFlg) {
		this.requiredFlg = requiredFlg;
	}

	//bi-directional many-to-one association to JobRuntimeParamDetailEntity
	@OneToMany(mappedBy="jobRuntimeParamEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobRuntimeParamDetailEntity> getJobRuntimeParamDetailEntities() {
		return this.jobRuntimeParamDetailEntities;
	}
	public void setJobRuntimeParamDetailEntities(List<JobRuntimeParamDetailEntity> jobRuntimeParamDetailEntities) {
		if (jobRuntimeParamDetailEntities != null && jobRuntimeParamDetailEntities.size() > 0) {
			Collections.sort(jobRuntimeParamDetailEntities, new Comparator<JobRuntimeParamDetailEntity>() {
				@Override
				public int compare(JobRuntimeParamDetailEntity o1, JobRuntimeParamDetailEntity o2) {
					return o1.getId().getOrderNo().compareTo(o2.getId().getOrderNo());
				}
			});
		}
		this.jobRuntimeParamDetailEntities = jobRuntimeParamDetailEntities;
	}

	//bi-directional many-to-one association to JobKickEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="jobkick_id", referencedColumnName="jobkick_id", insertable=false, updatable=false)
	public JobKickEntity getJobKickEntity() {
		return this.jobKickEntity;
	}
	@Deprecated
	public void setJobKickEntity(JobKickEntity jobKickEntity) {
		this.jobKickEntity = jobKickEntity;
	}

	/**
	 * JobKickEntityオブジェクト参照設定<BR>
	 * 
	 * JobKickEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToJobKickEntity(JobKickEntity jobKickEntity) {
		this.setJobKickEntity(jobKickEntity);
		if (jobKickEntity != null) {
			List<JobRuntimeParamEntity> list = jobKickEntity.getJobRuntimeParamEntities();
			if (list == null) {
				list = new ArrayList<JobRuntimeParamEntity>();
			} else {
				for(JobRuntimeParamEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobKickEntity.setJobRuntimeParamEntities(list);
		}
	}

	/**
	 * JobRuntimeParamDetailEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteJobRuntimeParamDetailEntities(List<JobRuntimeParamDetailEntityPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<JobRuntimeParamDetailEntity> list = this.getJobRuntimeParamDetailEntities();
			Iterator<JobRuntimeParamDetailEntity> iter = list.iterator();
			while(iter.hasNext()) {
				JobRuntimeParamDetailEntity entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
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

		// JobKickEntity
		if (this.jobKickEntity != null) {
			List<JobRuntimeParamEntity> list = this.jobKickEntity.getJobRuntimeParamEntities();
			if (list != null) {
				Iterator<JobRuntimeParamEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobRuntimeParamEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
}