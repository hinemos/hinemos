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

import com.clustercontrol.rpa.model.RpaManagementToolRunParamMst;

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
 * The persistent class for the cc_job_rpa_request_param_mst database table.
 * 
 */
@Entity
@Table(name = "cc_job_rpa_run_param_mst", schema = "setting")
@Cacheable(true)
public class JobRpaRunParamMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobRpaRunParamMstEntityPK id;
	/** パラメータ名 */
	private String paramName;
	/** パラメータ値 */
	private String paramValue;
	/** パラメータタイプ */
	private Integer paramType;
	/** 複数指定する項目かどうかのフラグ */
	private Boolean arrayFlg;
	private JobMstEntity jobMstEntity;
	/** RPA管理ツールリクエストパラメータ */
	private RpaManagementToolRunParamMst rpaManagementToolRunParamMst;

	public JobRpaRunParamMstEntity() {
	}

	public JobRpaRunParamMstEntity(JobRpaRunParamMstEntityPK pk) {
		this.setId(pk);
	}

	public JobRpaRunParamMstEntity(JobMstEntity jobMstEntity, Integer paramId) {
		this.setId(new JobRpaRunParamMstEntityPK(jobMstEntity.getId().getJobunitId(),
				jobMstEntity.getId().getJobId(), paramId));
	}

	@EmbeddedId
	public JobRpaRunParamMstEntityPK getId() {
		return id;
	}

	public void setId(JobRpaRunParamMstEntityPK id) {
		this.id = id;
	}

	/**
	 * @return パラメータ名を返します。
	 */
	@Column(name = "param_name")
	public String getParamName() {
		return paramName;
	}

	/**
	 * @param paramName
	 *            パラメータ名を設定します。
	 */
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	/**
	 * @return パラメータ値を返します。
	 */
	@Column(name = "param_value")
	public String getParamValue() {
		return paramValue;
	}

	/**
	 * @param paramValue
	 *            パラメータ値を設定します。
	 */
	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	/**
	 * @return パラメータタイプを返します。
	 */
	@Column(name = "param_type")
	public Integer getParamType() {
		return paramType;
	}

	/**
	 * @param paramType
	 *            パラメータタイプを設定します。
	 */
	public void setParamType(Integer paramType) {
		this.paramType = paramType;
	}

	/**
	 * @return 複数指定する項目かどうかのフラグを返します。
	 */
	@Column(name = "array_flg")
	public Boolean getArrayFlg() {
		return arrayFlg;
	}

	/**
	 * @param arrayFlg
	 *            複数指定する項目かどうかのフラグを設定します。
	 */
	public void setArrayFlg(Boolean arrayFlg) {
		this.arrayFlg = arrayFlg;
	}

	// bi-directional many-to-one association to JobMstEntity
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({ @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false),
			@JoinColumn(name = "jobunit_id", referencedColumnName = "jobunit_id", insertable = false, updatable = false) })
	public JobMstEntity getJobMstEntity() {
		return jobMstEntity;
	}

	@Deprecated
	public void setJobMstEntity(JobMstEntity jobMstEntity) {
		this.jobMstEntity = jobMstEntity;
	}

	// uni-directional many-to-one association to
	// RpaManagementToolRunParamMst
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "param_id", referencedColumnName = "param_id", insertable = false, updatable = false)
	public RpaManagementToolRunParamMst getRpaManagementToolRunParamMst() {
		return rpaManagementToolRunParamMst;
	}

	public void setRpaManagementToolRunParamMst(RpaManagementToolRunParamMst rpaManagementToolRunParamMst) {
		this.rpaManagementToolRunParamMst = rpaManagementToolRunParamMst;
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
	 * based on references held by the owning side of the relationship. It is
	 * the developer’s responsibility to keep the in-memory references held on
	 * the owning side and those held on the inverse side consistent with each
	 * other when they change.
	 */
	public void relateToJobMstEntity(JobMstEntity jobMstEntity) {
		this.setJobMstEntity(jobMstEntity);
		if (jobMstEntity != null) {
			List<JobRpaRunParamMstEntity> list = jobMstEntity.getJobRpaRunParamMstEntities();
			if (list == null) {
				list = new ArrayList<JobRpaRunParamMstEntity>();
			} else {
				for (JobRpaRunParamMstEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobMstEntity.setJobRpaRunParamMstEntities(list);
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
	 * based on references held by the owning side of the relationship. It is
	 * the developer’s responsibility to keep the in-memory references held on
	 * the owning side and those held on the inverse side consistent with each
	 * other when they change.
	 */
	public void unchain() {

		// JobMstEntity
		if (this.jobMstEntity != null) {
			List<JobRpaRunParamMstEntity> list = this.jobMstEntity.getJobRpaRunParamMstEntities();
			if (list != null) {
				Iterator<JobRpaRunParamMstEntity> iter = list.iterator();
				while (iter.hasNext()) {
					JobRpaRunParamMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())) {
						iter.remove();
						break;
					}
				}
			}
		}
	}
}
