/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.bean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.QueryCriteria;
import com.clustercontrol.filtersetting.entity.FilterConditionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;

/**
 * ジョブ実行履歴フィルタ詳細情報。
 * フィルタ詳細情報は、複数指定(OR結合)可能な条件の情報です。
 */
public class JobHistoryFilterConditionInfo extends FilterConditionInfo<JobHistoryFilterItemType> {
	private static final Log log = LogFactory.getLog(JobHistoryFilterConditionInfo.class);

	/** 開始・再実行日時 開始 */
	private Long startDateFrom;

	/** 開始・再実行日時 終了 */
	private Long startDateTo;

	/** 終了・中断日時 開始 */
	private Long endDateFrom;

	/** 終了・中断日時 終了 */
	private Long endDateTo;

	/** セッションID */
	private String sessionId;

	/** ジョブID */
	private String jobId;

	// 以下の status, endStatus, triggerType はenum相当のプロパティですが
	// 妥当なenumが主担当機能(jobmanagement)に用意されていないので、本クラスにおいても内部コードで取り扱います。

	/** 実行状態 */
	private Integer status;

	/** 終了状態 */
	private Integer endStatus;

	/** 実行契機種別 */
	private Integer triggerType;

	/** 実行契機情報 */
	private String triggerInfo;

	/** オーナーロールID */
	private String ownerRoleId;

	/**
	 * 全件表示用のデフォルト設定を行ったインスタンスを生成します。
	 */
	public static JobHistoryFilterConditionInfo ofAllHistories() {
		JobHistoryFilterConditionInfo o = new JobHistoryFilterConditionInfo();
		return o;
	}

	/**
	 * クライアントビュー表示用のデフォルト設定を行ったインスタンスを生成します。
	 */
	public static JobHistoryFilterConditionInfo ofClientViewDefault() {
		return ofAllHistories();
	}

	public JobHistoryFilterConditionInfo() {
		super();
	}

	public JobHistoryFilterConditionInfo(FilterConditionEntity entity) {
		super(entity);
		initializeItems(entity);
	}

	@Override
	public JobHistoryFilterItemType convertFilterItemType(Integer dbValue) {
		return JobHistoryFilterItemType.fromCode(dbValue);
	}

	@Override
	public JobHistoryFilterItemType[] getAllFilterItemTypes() {
		return JobHistoryFilterItemType.values();
	}

	@Override
	public Object getItemValue(JobHistoryFilterItemType type) {
		// enum値を追加したときに対応漏れが起きないようにするため、
		// case不足の警告が出るようにしたいので、全列挙している。
		switch (type) {
		case END_DATE_FROM:
			return getEndDateFrom();
		case END_DATE_TO:
			return getEndDateTo();
		case END_STATUS:
			return getEndStatus();
		case SESSION_ID:
			return getSessionId();
		case JOB_ID:
			return getJobId();
		case OWNER_ROLE_ID:
			return getOwnerRoleId();
		case START_DATE_FROM:
			return getStartDateFrom();
		case START_DATE_TO:
			return getStartDateTo();
		case STATUS:
			return getStatus();
		case TRIGGER_INFO:
			return getTriggerInfo();
		case TRIGGER_TYPE:
			return getTriggerType();
		}
		throw new RuntimeException("Unknown type. type=" + type);
	}

	@Override
	public void setItemValue(JobHistoryFilterItemType type, String value) {
		try {
			// enum値を追加したときに対応漏れが起きないようにするため、
			// case不足の警告が出るようにしたいので、全列挙している。
			switch (type) {
			case END_DATE_FROM:
				setEndDateFrom(Long.valueOf(value));
				return;
			case END_DATE_TO:
				setEndDateTo(Long.valueOf(value));
				return;
			case END_STATUS:
				setEndStatus(Integer.valueOf(value));
				return;
			case SESSION_ID:
				setSessionId(value);
				return;
			case JOB_ID:
				setJobId(value);
				return;
			case OWNER_ROLE_ID:
				setOwnerRoleId(value);
				return;
			case START_DATE_FROM:
				setStartDateFrom(Long.valueOf(value));
				return;
			case START_DATE_TO:
				setStartDateTo(Long.valueOf(value));
				return;
			case STATUS:
				setStatus(Integer.valueOf(value));
				return;
			case TRIGGER_INFO:
				setTriggerInfo(value);
				return;
			case TRIGGER_TYPE:
				setTriggerType(Integer.valueOf(value));
				return;
			}
		} catch (Exception e) {
			// 通常はありえない。ログに記録してスルーする。
			log.warn("setPropertyOf: Failed."
					+ " type=" + type
					+ ", value=" + value
					+ ", exception=" + e.getClass().getName()
					+ ", message=" + e.getMessage());
			return;
		}
		throw new RuntimeException("Unknown type. type=" + type);
	}

	/**
	 * SQL条件式構築オブジェクトを生成します。
	 * 
	 * @param uniqueId ユニークID。詳細は{@link QueryCriteria}を参照。
	 * @param jobSessiobJobAlias JPQL内での{@link JobSessionJobEntity}のエイリアス。
	 * @param jobSessionAlias JPQL内での{@link JobSessionEntity}のエイリアス。
	 */
	public JobHistoryFilterConditionCriteria createCriteria(
			String uniqueId, String jobSessiobJobAlias, String jobSessionAlias) {
		JobHistoryFilterConditionCriteria crt = new JobHistoryFilterConditionCriteria(
				uniqueId, jobSessiobJobAlias, jobSessionAlias);
		crt.startDate.setFromTo(startDateFrom, startDateTo);
		crt.endDate.setFromTo(endDateFrom, endDateTo);
		crt.sessionId.setPattern(sessionId);
		crt.jobId.setPattern(jobId);
		crt.status.setValue(status);
		crt.endStatus.setValue(endStatus);
		crt.ownerRoleId.setPattern(ownerRoleId);
		crt.triggerType.setValue(triggerType);
		crt.triggerInfo.setPattern(triggerInfo);
		crt.setNegative(getNegative() != null && getNegative().booleanValue());
		return crt;
	}

	public Long getStartDateFrom() {
		return startDateFrom;
	}

	public void setStartDateFrom(Long startDateFrom) {
		this.startDateFrom = startDateFrom;
	}

	public Long getStartDateTo() {
		return startDateTo;
	}

	public void setStartDateTo(Long startDateTo) {
		this.startDateTo = startDateTo;
	}

	public Long getEndDateFrom() {
		return endDateFrom;
	}

	public void setEndDateFrom(Long endDateFrom) {
		this.endDateFrom = endDateFrom;
	}

	public Long getEndDateTo() {
		return endDateTo;
	}

	public void setEndDateTo(Long endDateTo) {
		this.endDateTo = endDateTo;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getEndStatus() {
		return endStatus;
	}

	public void setEndStatus(Integer endStatus) {
		this.endStatus = endStatus;
	}

	public Integer getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(Integer triggerType) {
		this.triggerType = triggerType;
	}

	public String getTriggerInfo() {
		return triggerInfo;
	}

	public void setTriggerInfo(String triggerInfo) {
		this.triggerInfo = triggerInfo;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

}
