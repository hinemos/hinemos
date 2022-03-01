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

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


/**
 * The persistent class for the cc_job_link_inherit_info database table.
 *
 */
@Entity
@Table(name="cc_job_link_inherit_info", schema="log")
public class JobLinkInheritInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobLinkInheritInfoEntityPK id;
	private String keyInfo;
	private String expKey;
	private JobInfoEntity jobInfoEntity;

	@Deprecated
	public JobLinkInheritInfoEntity() {
	}

	public JobLinkInheritInfoEntity(JobLinkInheritInfoEntityPK pk) {
		this.setId(pk);
	}

	public JobLinkInheritInfoEntity(JobInfoEntity jobInfoEntity, String paramId) {
		this(new JobLinkInheritInfoEntityPK(
				jobInfoEntity.getId().getSessionId(),
				jobInfoEntity.getId().getJobunitId(),
				jobInfoEntity.getId().getJobId(),
				paramId));
	}

	@EmbeddedId
	public JobLinkInheritInfoEntityPK getId() {
		return this.id;
	}
	public void setId(JobLinkInheritInfoEntityPK id) {
		this.id = id;
	}

	@Column(name="key_info")
	public String getKeyInfo() {
		return this.keyInfo;
	}
	public void setKeyInfo(String keyInfo) {
		this.keyInfo = keyInfo;
	}

	@Column(name="exp_key")
	public String getExpKey() {
		return this.expKey;
	}
	public void setExpKey(String expKey) {
		this.expKey =expKey;
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
			List<JobLinkInheritInfoEntity> list = jobInfoEntity.getJobLinkInheritInfoEntities();
			if (list == null) {
				list = new ArrayList<>();
			} else {
				for(JobLinkInheritInfoEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobInfoEntity.setJobLinkInheritInfoEntities(list);
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
			List<JobLinkInheritInfoEntity> list = jobInfoEntity.getJobLinkInheritInfoEntities();
			if (list != null) {
				Iterator<JobLinkInheritInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobLinkInheritInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
}