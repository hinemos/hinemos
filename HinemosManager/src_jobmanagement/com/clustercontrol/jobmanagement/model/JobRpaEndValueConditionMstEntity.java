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
 * The persistent class for the cc_job_rpa_endvalue_condition_mst database table.
 * 
 */
@Entity
@Table(name="cc_job_rpa_end_value_condition_mst", schema="setting")
@Cacheable(true)
public class JobRpaEndValueConditionMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobRpaEndValueConditionMstEntityPK id;
	/** 終了値判定条件種別 */
	private Integer conditionType;
	/** パターンマッチ表現 */
	private String pattern;
	/** 大文字・小文字を区別しないフラグ */
	private Boolean caseSensitivityFlg;
	/** 条件に一致したら処理するフラグ */
	private Boolean processType;
	/** リターンコード */
	private String returnCode;
	/** リターンコード判定条件 */
	private Integer returnCodeCondition;
	/** RPAツールのリターンコードをそのまま終了値とするフラグ */
	private Boolean useCommandReturnCodeFlg;
	/** 終了値 */
	private Integer endValue;
	/** 説明 */
	private String description;
	private JobMstEntity jobMstEntity;
	
	public JobRpaEndValueConditionMstEntity() {
	}

	public JobRpaEndValueConditionMstEntity(JobRpaEndValueConditionMstEntityPK pk) {
		this.setId(pk);
	}

	public JobRpaEndValueConditionMstEntity(JobMstEntity jobMstEntity, Integer order) {
		this(new JobRpaEndValueConditionMstEntityPK(
				jobMstEntity.getId().getJobunitId(),
				jobMstEntity.getId().getJobId(),
				order));
	}

	@EmbeddedId
	public JobRpaEndValueConditionMstEntityPK getId() {
		return id;
	}


	public void setId(JobRpaEndValueConditionMstEntityPK id) {
		this.id = id;
	}

	/**
	 * @return 終了値判定条件種別を返します。
	 */
	@Column(name="condition_type")
	public Integer getConditionType() {
		return conditionType;
	}

	/**
	 * @param conditionType
	 *            終了値判定条件種別を設定します。
	 */
	public void setConditionType(Integer conditionType) {
		this.conditionType = conditionType;
	}

	/**
	 * @return パターンマッチ表現を返します。
	 */
	@Column(name="pattern")
	public String getPattern() {
		return pattern;
	}

	/**
	 * @param パターンマッチ表現
	 *            patternを設定します。
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * @return 大文字・小文字を区別しないフラグを返します。
	 */
	@Column(name="case_sensitivity_flg")
	public Boolean getCaseSensitivityFlg() {
		return caseSensitivityFlg;
	}

	/**
	 * @param caseSensitivityFlg
	 *            大文字・小文字を区別しないフラグを設定します。
	 */
	public void setCaseSensitivityFlg(Boolean caseSensitivityFlg) {
		this.caseSensitivityFlg = caseSensitivityFlg;
	}

	/**
	 * @return 条件に一致したら処理するフラグを返します。
	 */
	@Column(name="process_type")
	public Boolean getProcessType() {
		return processType;
	}

	/**
	 * @param processType
	 *            条件に一致したら処理するフラグを設定します。
	 */
	public void setProcessType(Boolean processType) {
		this.processType = processType;
	}

	/**
	 * @return リターンコードを返します。
	 */
	@Column(name="return_code")
	public String getReturnCode() {
		return returnCode;
	}

	/**
	 * @param returnCode
	 *            リターンコードを設定します。
	 */
	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}

	/**
	 * @return リターンコード判定条件を返します。
	 */
	@Column(name="return_code_condition")
	public Integer getReturnCodeCondition() {
		return returnCodeCondition;
	}

	/**
	 * @param returnCodeCondition
	 *            リターンコード判定条件を設定します。
	 */
	public void setReturnCodeCondition(Integer returnCodeCondition) {
		this.returnCodeCondition = returnCodeCondition;
	}

	/**
	 * @return RPAツールのリターンコードをそのまま終了値とするフラグを返します。
	 */
	@Column(name="use_command_return_code_flg")
	public Boolean getUseCommandReturnCodeFlg() {
		return useCommandReturnCodeFlg;
	}

	/**
	 * @param useCommandReturnCodeFlg
	 *            RPAツールのリターンコードをそのまま終了値とするフラグを設定します。
	 */
	public void setUseCommandReturnCodeFlg(Boolean useCommandReturnCodeFlg) {
		this.useCommandReturnCodeFlg = useCommandReturnCodeFlg;
	}

	/**
	 * @return 終了値を返します。
	 */
	@Column(name="end_value")
	public Integer getEndValue() {
		return endValue;
	}

	/**
	 * @param endValue
	 *            終了値を設定します。
	 */
	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}

	/**
	 * @return 説明を返します。
	 */
	@Column(name="description")
	public String getDescription() {
		return description;
	}

	/**
	 * @param 説明
	 *            descriptionを設定します。
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public void setJobMstEntity(JobMstEntity jobMstEntity) {
		this.jobMstEntity = jobMstEntity;
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
			List<JobRpaEndValueConditionMstEntity> list = jobMstEntity.getJobRpaEndValueConditionMstEntities();
			if (list == null) {
				list = new ArrayList<JobRpaEndValueConditionMstEntity>();
			} else {
				for(JobRpaEndValueConditionMstEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobMstEntity.setJobRpaEndValueConditionMstEntities(list);
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
			List<JobRpaEndValueConditionMstEntity> list = this.jobMstEntity.getJobRpaEndValueConditionMstEntities();
			if (list != null) {
				Iterator<JobRpaEndValueConditionMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobRpaEndValueConditionMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}
