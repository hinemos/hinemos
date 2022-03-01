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
 * The persistent class for the cc_job_rpa_endvalue_condition_info database table.
 * 
 */
@Entity
@Table(name="cc_job_rpa_end_value_condition_info", schema="log")
@Cacheable(true)
public class JobRpaEndValueConditionInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobRpaEndValueConditionInfoEntityPK id;
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
	private JobInfoEntity jobInfoEntity;
	
	public JobRpaEndValueConditionInfoEntity() {
	}

	public JobRpaEndValueConditionInfoEntity(JobRpaEndValueConditionInfoEntityPK pk) {
		this.setId(pk);
	}

	public JobRpaEndValueConditionInfoEntity(JobInfoEntity jobInfoEntity, Integer order) {
		this(new JobRpaEndValueConditionInfoEntityPK(
				jobInfoEntity.getId().getSessionId(),
				jobInfoEntity.getId().getJobunitId(),
				jobInfoEntity.getId().getJobId(),
				order));
	}

	@EmbeddedId
	public JobRpaEndValueConditionInfoEntityPK getId() {
		return id;
	}


	public void setId(JobRpaEndValueConditionInfoEntityPK id) {
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

	public void setJobInfoEntity(JobInfoEntity jobInfoEntity) {
		this.jobInfoEntity = jobInfoEntity;
	}

	//bi-directional many-to-one association to JobInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="session_id", referencedColumnName="job_id", insertable=false, updatable=false),
		@JoinColumn(name="job_id", referencedColumnName="job_id", insertable=false, updatable=false),
		@JoinColumn(name="jobunit_id", referencedColumnName="jobunit_id", insertable=false, updatable=false)
	})
	public JobInfoEntity getJobInfoEntity() {
		return this.jobInfoEntity;
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
			List<JobRpaEndValueConditionInfoEntity> list = jobInfoEntity.getJobRpaEndValueConditionInfoEntities();
			if (list == null) {
				list = new ArrayList<JobRpaEndValueConditionInfoEntity>();
			} else {
				for(JobRpaEndValueConditionInfoEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobInfoEntity.setJobRpaEndValueConditionInfoEntities(list);
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
			List<JobRpaEndValueConditionInfoEntity> list = this.jobInfoEntity.getJobRpaEndValueConditionInfoEntities();
			if (list != null) {
				Iterator<JobRpaEndValueConditionInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobRpaEndValueConditionInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}
