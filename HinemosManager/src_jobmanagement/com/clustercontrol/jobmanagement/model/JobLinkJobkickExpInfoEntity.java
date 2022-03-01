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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


/**
 * The persistent class for the cc_job_link_jobkick_exp_info database table.
 *
 */
@Entity
@Table(name="cc_job_link_jobkick_exp_info", schema="setting")
public class JobLinkJobkickExpInfoEntity  implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobLinkJobkickExpInfoEntityPK id;
	private String value;
	private JobKickEntity jobKickEntity;

	@Deprecated
	public JobLinkJobkickExpInfoEntity() {
	}

	public JobLinkJobkickExpInfoEntity(JobLinkJobkickExpInfoEntityPK pk) {
		this.setId(pk);
	}

	public JobLinkJobkickExpInfoEntity(
			String jobkickId,
			String key) {
		this(new JobLinkJobkickExpInfoEntityPK(jobkickId, key));
	}

	@EmbeddedId
	public JobLinkJobkickExpInfoEntityPK getId() {
		return this.id;
	}
	public void setId(JobLinkJobkickExpInfoEntityPK id) {
		this.id = id;
	}

	@Column(name="value")
	public String getValue() {
		return this.value;
	}
	public void setValue(String value) {
		this.value = value;
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
			List<JobLinkJobkickExpInfoEntity> list = jobKickEntity.getJobLinkJobkickExpInfoEntities();
			if (list == null) {
				list = new ArrayList<>();
			} else {
				for(JobLinkJobkickExpInfoEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobKickEntity.setJobLinkJobkickExpInfoEntities(list);
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
			List<JobLinkJobkickExpInfoEntity> list = jobKickEntity.getJobLinkJobkickExpInfoEntities();
			if (list != null) {
				Iterator<JobLinkJobkickExpInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobLinkJobkickExpInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
}