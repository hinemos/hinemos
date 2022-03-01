/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * RPAシナリオジョブ（間接実行）状態テーブルの Entity クラス
 * 
 */
@Entity
@Table(name = "cc_job_rpa_run_condition", schema = "log")
@Cacheable(true)
public class JobRpaRunConditionEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobRpaRunConditionEntityPK id;
	/** 実行状態 */
	private int runCondition;
	/** RPA管理ツールで実行した処理の識別子となる文字列 */
	private String runIdentifier;

	public JobRpaRunConditionEntity() {
	}

	public JobRpaRunConditionEntity(JobRpaRunConditionEntityPK pk) {
		this.setId(pk);
	}

	@EmbeddedId
	public JobRpaRunConditionEntityPK getId() {
		return id;
	}

	public void setId(JobRpaRunConditionEntityPK id) {
		this.id = id;
	}
	
	/**
	 * @return 実行状態を返します。
	 */
	@Column(name="run_condition")
	public int getRunCondition() {
		return runCondition;
	}

	/**
	 * @param runCondition
	 *            実行状態を設定します。
	 */
	public void setRunCondition(int runCondition) {
		this.runCondition = runCondition;
	}
	
	/**
	 * @return RPA管理ツールで実行した処理の識別子となる文字列を返します。
	 */
	@Column(name="run_identifier")
	public String getRunIdentifier() {
		return runIdentifier;
	}

	/**
	 * @param runIdentifier
	 *            RPA管理ツールで実行した処理の識別子となる文字列を設定します。
	 */
	public void setRunIdentifier(String runIdentifier) {
		this.runIdentifier = runIdentifier;
	}

}
