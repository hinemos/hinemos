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
 * The persistent class for the cc_job_command_param_info database table.
 * 
 */
@Entity
@Table(name="cc_job_command_param_info", schema="log")
public class JobCommandParamInfoEntity  implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobCommandParamInfoEntityPK id;
	private Boolean jobStandardOutputFlg;
	private String value;
	private JobInfoEntity jobInfoEntity;

	@Deprecated
	public JobCommandParamInfoEntity() {
	}

	public JobCommandParamInfoEntity(JobCommandParamInfoEntityPK pk) {
		this.setId(pk);
	}

	public JobCommandParamInfoEntity(JobInfoEntity jobInfoEntity, String jobunitId, String jobId, String paramId) {
		this(new JobCommandParamInfoEntityPK(
				jobInfoEntity.getId().getSessionId(),
				jobunitId,
				jobId,
				paramId));
	}

	@EmbeddedId
	public JobCommandParamInfoEntityPK getId() {
		return this.id;
	}

	public void setId(JobCommandParamInfoEntityPK id) {
		this.id = id;
	}

	@Column(name="job_standard_output_flg")
	public Boolean getJobStandardOutputFlg() {
		return jobStandardOutputFlg;
	}

	public void setJobStandardOutputFlg(Boolean jobStandardOutputFlg) {
		this.jobStandardOutputFlg = jobStandardOutputFlg;
	}

	@Column(name="value")
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	//bi-directional many-to-one association to JobInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="job_id", referencedColumnName="job_id", insertable=false, updatable=false),
		@JoinColumn(name="jobunit_id", referencedColumnName="jobunit_id", insertable=false, updatable=false),
		@JoinColumn(name="session_id", referencedColumnName="session_id", insertable=false, updatable=false)
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
	public void relateToJobCommandParamInfoEntity(JobInfoEntity jobInfoEntity) {
		this.setJobInfoEntity(jobInfoEntity);
		if (jobInfoEntity != null) {
			List<JobCommandParamInfoEntity> list = jobInfoEntity.getJobCommandParamInfoEntities();
			if (list == null) {
				list = new ArrayList<JobCommandParamInfoEntity>();
			} else {
				for (JobCommandParamInfoEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobInfoEntity.setJobCommandParamInfoEntities(list);
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
			List<JobCommandParamInfoEntity> list = this.jobInfoEntity.getJobCommandParamInfoEntities();
			if (list != null) {
				Iterator<JobCommandParamInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobCommandParamInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}