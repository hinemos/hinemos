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
 * The persistent class for the cc_job_output_mst database table.
 * 
 */
@Entity
@Table(name="cc_job_output_mst", schema="setting")
@Cacheable(true)
public class JobOutputMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobOutputMstEntityPK id;
	private Boolean sameNormalFlg;
	private String directory;
	private String fileName;
	private Boolean appendFlg;
	private Boolean failureOperationFlg;
	private Integer failureOperationType;
	private Integer failureOperationEndStatus;
	private Integer failureOperationEndValue;
	private Boolean failureNotifyFlg;
	private Integer failureNotifyPriority;
	private Boolean valid;
	private JobMstEntity jobMstEntity;

	@Deprecated
	public JobOutputMstEntity() {
	}

	public JobOutputMstEntity(JobOutputMstEntityPK pk) {
		this.setId(pk);
	}

	public JobOutputMstEntity(JobMstEntity jobMstEntity, Integer outputType) {
		this(new JobOutputMstEntityPK(
				jobMstEntity.getId().getJobunitId(),
				jobMstEntity.getId().getJobId(),
				outputType));
	}


	@EmbeddedId
	public JobOutputMstEntityPK getId() {
		return this.id;
	}

	public void setId(JobOutputMstEntityPK id) {
		this.id = id;
	}

	@Column(name="same_normal_flg")
	public Boolean getSameNormalFlg() {
		return sameNormalFlg;
	}
	public void setSameNormalFlg(Boolean sameNormalFlg) {
		this.sameNormalFlg = sameNormalFlg;
	}

	@Column(name="directory")
	public String getDirectory() {
		return directory;
	}
	public void setDirectory(String directory) {
		this.directory = directory;
	}

	@Column(name="file_name")
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Column(name="append_flg")
	public Boolean getAppendFlg() {
		return appendFlg;
	}
	public void setAppendFlg(Boolean appendFlg) {
		this.appendFlg = appendFlg;
	}

	@Column(name="failure_operation_flg")
	public Boolean getFailureOperationFlg() {
		return failureOperationFlg;
	}
	public void setFailureOperationFlg(Boolean failureOperationFlg) {
		this.failureOperationFlg = failureOperationFlg;
	}

	@Column(name="failure_operation_type")
	public Integer getFailureOperationType() {
		return failureOperationType;
	}
	public void setFailureOperationType(Integer failureOperationType) {
		this.failureOperationType = failureOperationType;
	}

	@Column(name="failure_operation_end_status")
	public Integer getFailureOperationEndStatus() {
		return failureOperationEndStatus;
	}
	public void setFailureOperationEndStatus(Integer failureOperationEndStatus) {
		this.failureOperationEndStatus = failureOperationEndStatus;
	}

	@Column(name="failure_operation_end_value")
	public Integer getFailureOperationEndValue() {
		return failureOperationEndValue;
	}
	public void setFailureOperationEndValue(Integer failureOperationEndValue) {
		this.failureOperationEndValue = failureOperationEndValue;
	}

	@Column(name="failure_notify_flg")
	public Boolean getFailureNotifyFlg() {
		return failureNotifyFlg;
	}
	public void setFailureNotifyFlg(Boolean failureNotifyFlg) {
		this.failureNotifyFlg = failureNotifyFlg;
	}

	@Column(name="failure_notify_priority")
	public Integer getFailureNotifyPriority() {
		return failureNotifyPriority;
	}
	public void setFailureNotifyPriority(Integer failureNotifyPriority) {
		this.failureNotifyPriority = failureNotifyPriority;
	}

	@Column(name="valid")
	public Boolean getValid() {
		return valid;
	}
	public void setValid(Boolean valid) {
		this.valid = valid;
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
			List<JobOutputMstEntity> list = jobMstEntity.getJobOutputMstEntities();
			if (list == null) {
				list = new ArrayList<JobOutputMstEntity>();
			} else {
				for(JobOutputMstEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobMstEntity.setJobOutputMstEntities(list);
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
			List<JobOutputMstEntity> list = this.jobMstEntity.getJobOutputMstEntities();
			if (list != null) {
				Iterator<JobOutputMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobOutputMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}