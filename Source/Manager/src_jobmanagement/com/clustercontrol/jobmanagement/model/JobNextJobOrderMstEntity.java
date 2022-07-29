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

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;



/**
 * The persistent class for the cc_job_next_job_order_mst database table.
 *
 */
@Entity
@Table(name="cc_job_next_job_order_mst", schema="setting")
public class JobNextJobOrderMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobNextJobOrderMstEntityPK id;
	private Integer order; 
	private JobMstEntity jobMstEntity;
	
	public JobNextJobOrderMstEntity() {
	}
	
	public JobNextJobOrderMstEntity(JobNextJobOrderMstEntityPK pk) {
		this.setId(pk);
	}

	@EmbeddedId
	public JobNextJobOrderMstEntityPK getId() {
		return this.id;
	}

	public void setId(JobNextJobOrderMstEntityPK id) {
		this.id = id;
	}

	@Column(name="order_no")
	public Integer getOrder() {
		return order;
	}
	public void setOrder(Integer order) {
		this.order = order;
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
			List<JobNextJobOrderMstEntity> list = jobMstEntity.getJobNextJobOrderMstEntities();
			if (list == null) {
				list = new ArrayList<JobNextJobOrderMstEntity>();
			} else {
				for(JobNextJobOrderMstEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobMstEntity.setJobNextJobOrderMstEntities(list);
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
			List<JobNextJobOrderMstEntity> list = this.jobMstEntity.getJobNextJobOrderMstEntities();
			if (list != null) {
				Iterator<JobNextJobOrderMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobNextJobOrderMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
}
