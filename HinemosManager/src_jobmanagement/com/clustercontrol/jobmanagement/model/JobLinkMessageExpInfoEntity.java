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
 * The persistent class for the cc_job_link_message_exp_info database table.
 *
 */
@Entity
@Table(name="cc_job_link_message_exp_info", schema="log")
public class JobLinkMessageExpInfoEntity  implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobLinkMessageExpInfoEntityPK id;
	private String value;
	private JobLinkMessageEntity jobLinkMessageEntity;

	@Deprecated
	public JobLinkMessageExpInfoEntity() {
	}

	public JobLinkMessageExpInfoEntity(JobLinkMessageExpInfoEntityPK pk) {
		this.setId(pk);
	}

	public JobLinkMessageExpInfoEntity(
			String joblinkMessageId,
			String facilityId,
			Long sendDate,
			String key) {
		this(new JobLinkMessageExpInfoEntityPK(joblinkMessageId, facilityId, sendDate, key));
	}

	public JobLinkMessageExpInfoEntity(JobLinkMessageEntity jobLinkMessageEntity, String key) {
		this(jobLinkMessageEntity.getId().getJoblinkMessageId(),
				jobLinkMessageEntity.getId().getFacilityId(),
				jobLinkMessageEntity.getId().getSendDate(),
				key);
	}

	@EmbeddedId
	public JobLinkMessageExpInfoEntityPK getId() {
		return this.id;
	}
	public void setId(JobLinkMessageExpInfoEntityPK id) {
		this.id = id;
	}

	@Column(name="value")
	public String getValue() {
		return this.value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	//bi-directional many-to-one association to JobLinkMessageEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="joblink_message_id", referencedColumnName="joblink_message_id", insertable=false, updatable=false),
		@JoinColumn(name="facility_id", referencedColumnName="facility_id", insertable=false, updatable=false),
		@JoinColumn(name="send_date", referencedColumnName="send_date", insertable=false, updatable=false)
	})
	public JobLinkMessageEntity getJobLinkMessageEntity() {
		return this.jobLinkMessageEntity;
	}

	@Deprecated
	public void setJobLinkMessageEntity(JobLinkMessageEntity jobLinkMessageEntity) {
		this.jobLinkMessageEntity = jobLinkMessageEntity;
	}

	/**
	 * JobLinkMessageEntityオブジェクト参照設定<BR>
	 *
	 * JobLinkMessageEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToJobLinkMessageEntity(JobLinkMessageEntity jobLinkMessageEntity) {
		this.setJobLinkMessageEntity(jobLinkMessageEntity);
		if (jobLinkMessageEntity != null) {
			List<JobLinkMessageExpInfoEntity> list = jobLinkMessageEntity.getJobLinkMessageExpInfoEntities();
			if(list == null) {
				list = new ArrayList<JobLinkMessageExpInfoEntity>();
			} else {
				for(JobLinkMessageExpInfoEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobLinkMessageEntity.setJobLinkMessageExpInfoEntities(list);
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

		// getJobLinkMessageEntity
		if (this.jobLinkMessageEntity != null) {
			List<JobLinkMessageExpInfoEntity> list = this.jobLinkMessageEntity.getJobLinkMessageExpInfoEntities();
			if (list != null) {
				Iterator<JobLinkMessageExpInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobLinkMessageExpInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
}