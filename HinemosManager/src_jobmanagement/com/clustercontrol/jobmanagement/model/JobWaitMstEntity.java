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

import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;

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
 * The persistent class for the cc_job_wait_mst database table.
 * 
 */
@Entity
@Table(name="cc_job_wait_mst", schema="setting")
@Cacheable(true)
public class JobWaitMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobWaitMstEntityPK id;
	private JobWaitGroupMstEntity jobWaitGroupMstEntity;
	private String description;

	@Deprecated
	public JobWaitMstEntity() {
	}

	public JobWaitMstEntity(JobWaitMstEntityPK pk) {
		this.setId(pk);
	}

	public JobWaitMstEntity(JobWaitGroupMstEntity jobWaitGroupMstEntity,
			Integer targetJobType,
			String targetJobunitId,
			String targetJobId,
			Integer targetInt1,
			Integer targetInt2,
			String targetStr1,
			String targetStr2,
			Long targetLong) {
		this(new JobWaitMstEntityPK(
				jobWaitGroupMstEntity.getId().getJobunitId(),
				jobWaitGroupMstEntity.getId().getJobId(),
				jobWaitGroupMstEntity.getId().getOrderNo(),
				targetJobType,
				targetJobunitId,
				targetJobId,
				targetInt1,
				targetInt2,
				targetStr1,
				targetStr2,
				targetLong));
	}

	@EmbeddedId
	public JobWaitMstEntityPK getId() {
		return this.id;
	}

	public void setId(JobWaitMstEntityPK id) {
		this.id = id;
	}

	//bi-directional many-to-one association to JobWaitGroupMstEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="order_no", referencedColumnName="order_no", insertable=false, updatable=false),
		@JoinColumn(name="job_id", referencedColumnName="job_id", insertable=false, updatable=false),
		@JoinColumn(name="jobunit_id", referencedColumnName="jobunit_id", insertable=false, updatable=false)
	})
	public JobWaitGroupMstEntity getJobWaitGroupMstEntity() {
		return this.jobWaitGroupMstEntity;
	}

	@Deprecated
	public void setJobWaitGroupMstEntity(JobWaitGroupMstEntity jobWaitGroupMstEntity) {
		this.jobWaitGroupMstEntity = jobWaitGroupMstEntity;
	}

	@Column(name="description")
	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * JobWaitGroupMstEntityオブジェクト参照設定<BR>
	 * 
	 * JobWaitGroupMstEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToJobWaitGroupMstEntity(JobWaitGroupMstEntity jobWaitGroupMstEntity) {
		this.setJobWaitGroupMstEntity(jobWaitGroupMstEntity);
		if (jobWaitGroupMstEntity != null) {
			List<JobWaitMstEntity> list = jobWaitGroupMstEntity.getJobWaitMstEntities();
			if (list == null) {
				list = new ArrayList<JobWaitMstEntity>();
			} else {
				for (JobWaitMstEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobWaitGroupMstEntity.setJobWaitMstEntities(list);
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

		// JobWaitGroupMstEntity
		if (this.jobWaitGroupMstEntity != null) {
			List<JobWaitMstEntity> list = this.jobWaitGroupMstEntity.getJobWaitMstEntities();
			if (list != null) {
				Iterator<JobWaitMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobWaitMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

	/** ジョブ終了状態 */
	public static JobWaitMstEntity createJobEndStatus(
			JobWaitGroupMstEntity jobWaitGroupMstEntity,
			String targetJobunitId,
			String targetJobId,
			Integer targetInt1) {
		return new JobWaitMstEntity(jobWaitGroupMstEntity,
				JudgmentObjectConstant.TYPE_JOB_END_STATUS,
				targetJobunitId, 
				targetJobId,
				targetInt1,
				0,
				"",
				"",
				0L);
	}

	/** ジョブ終了値 */
	public static JobWaitMstEntity createJobEndValue(
			JobWaitGroupMstEntity jobWaitGroupMstEntity,
			String targetJobunitId,
			String targetJobId,
			Integer targetInt1,
			String targetStr1) {
		return new JobWaitMstEntity(jobWaitGroupMstEntity,
				JudgmentObjectConstant.TYPE_JOB_END_VALUE,
				targetJobunitId,
				targetJobId,
				targetInt1,
				0,
				targetStr1,
				"",
				0L);
	}

	/** 時刻 */
	public static JobWaitMstEntity createTime(
			JobWaitGroupMstEntity jobWaitGroupMstEntity,
			Long targetLong) {
		return new JobWaitMstEntity(jobWaitGroupMstEntity,
				JudgmentObjectConstant.TYPE_TIME,
				"",
				"",
				0,
				0,
				"",
				"",
				targetLong);
	}

	/** セッション開始時の時間（分）  */
	public static JobWaitMstEntity createStartMinute(
			JobWaitGroupMstEntity jobWaitGroupMstEntity,
			Integer targetInt1) {
		return new JobWaitMstEntity(jobWaitGroupMstEntity,
				JudgmentObjectConstant.TYPE_START_MINUTE,
				"",
				"",
				targetInt1,
				0,
				"",
				"",
				0L);
	}

	/** ジョブ変数 */
	public static JobWaitMstEntity createJobParameter(
			JobWaitGroupMstEntity jobWaitGroupMstEntity,
			Integer targetInt1,
			String targetStr1,
			String targetStr2) {
		return new JobWaitMstEntity(jobWaitGroupMstEntity,
				JudgmentObjectConstant.TYPE_JOB_PARAMETER,
				"",
				"",
				targetInt1,
				0,
				targetStr1,
				targetStr2,
				0L);
	}

	/** セッション横断ジョブ終了状態 */
	public static JobWaitMstEntity createCrossSessionJobEndStatus(
			JobWaitGroupMstEntity jobWaitGroupMstEntity,
			String targetJobunitId,
			String targetJobId,
			Integer targetInt1,
			Integer targetInt2) {
		return new JobWaitMstEntity(jobWaitGroupMstEntity,
				JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS,
				targetJobunitId,
				targetJobId,
				targetInt1,
				targetInt2,
				"",
				"",
				0L);
	}

	/** セッション横断ジョブ終了値 */
	public static JobWaitMstEntity createCrossSessionJobEndValue(
			JobWaitGroupMstEntity jobWaitGroupMstEntity,
			String targetJobunitId,
			String targetJobId,
			Integer targetInt1,
			Integer targetInt2,
			String targetStr1) {
		return new JobWaitMstEntity(jobWaitGroupMstEntity,
				JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE,
				targetJobunitId,
				targetJobId,
				targetInt1,
				targetInt2,
				targetStr1,
				"",
				0L);
	}

	/** ジョブリターンコード */
	public static JobWaitMstEntity createJobReturnValue(
			JobWaitGroupMstEntity jobWaitGroupMstEntity,
			String targetJobunitId,
			String targetJobId,
			Integer targetInt1,
			String targetStr1) {
		return new JobWaitMstEntity(jobWaitGroupMstEntity,
				JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE,
				targetJobunitId,
				targetJobId,
				targetInt1,
				0,
				targetStr1,
				"",
				0L);
	}
}