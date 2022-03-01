/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.bean;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.QueryCriteria;
import com.clustercontrol.filtersetting.entity.FilterConditionEntity;
import com.clustercontrol.notify.monitor.model.StatusInfoEntity;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityEnum;

/**
 * ステータス通知結果フィルタ詳細情報。
 * フィルタ詳細情報は、複数指定(OR結合)可能な条件の情報です。
 */
public class StatusFilterConditionInfo extends FilterConditionInfo<StatusFilterItemType> {
	private static final Log logger = LogFactory.getLog(StatusFilterConditionInfo.class);

	/** 重要度の種類の総数 */
	public static final int PRIORITY_VARIATION = 4;

	// 初期値は全件検索 (FilterConditionItemEntitiyがない場合に制限されない)

	/** 重要度 危険 */
	private Boolean priorityCritical = Boolean.TRUE;

	/** 重要度 警告 */
	private Boolean priorityWarning = Boolean.TRUE;

	/** 重要度 情報 */
	private Boolean priorityInfo = Boolean.TRUE;

	/** 重要度 不明 */
	private Boolean priorityUnknown = Boolean.TRUE;

	/** 最終変更日時 開始 */
	private Long outputDateFrom;

	/** 最終変更日時 終了 */
	private Long outputDateTo;

	/** 出力日時 開始 */
	private Long generationDateFrom;

	/** 出力日時 終了 */
	private Long generationDateTo;

	/** 監視項目ID */
	private String monitorId;

	/** 監視詳細 */
	private String monitorDetail;

	/** アプリケーション */
	private String application;

	/** メッセージ */
	private String message;

	/** オーナーロールID */
	private String ownerRoleId;

	/**
	 * 全件検索用のデフォルト設定を行ったインスタンスを生成します。
	 */
	public static StatusFilterConditionInfo ofAllStatus() {
		StatusFilterConditionInfo o = new StatusFilterConditionInfo();
		return o;
	}

	/**
	 * クライアントビュー表示用のデフォルト設定を行ったインスタンスを生成します。
	 */
	public static StatusFilterConditionInfo ofClientViewDefault() {
		StatusFilterConditionInfo o = new StatusFilterConditionInfo();
		return o;
	}

	public StatusFilterConditionInfo() {
		super();
	}

	public StatusFilterConditionInfo(FilterConditionEntity entity) {
		super(entity);
		initializeItems(entity);
	}

	@Override
	public StatusFilterItemType convertFilterItemType(Integer dbValue) {
		return StatusFilterItemType.fromCode(dbValue);
	}

	@Override
	public StatusFilterItemType[] getAllFilterItemTypes() {
		return StatusFilterItemType.values();
	}

	@Override
	public Object getItemValue(StatusFilterItemType type) {
		// enum値を追加したときに対応漏れが起きないようにするため、
		// case不足の警告が出るようにしたいので、全列挙している。
		switch (type) {
		case APPLICATION:
			return getApplication();
		case GENERATION_DATE_FROM:
			return getGenerationDateFrom();
		case GENERATION_DATE_TO:
			return getGenerationDateTo();
		case MESSAGE:
			return getMessage();
		case MONITOR_DETAIL:
			return getMonitorDetail();
		case MONITOR_ID:
			return getMonitorId();
		case OUTPUT_DATE_FROM:
			return getOutputDateFrom();
		case OUTPUT_DATE_TO:
			return getOutputDateTo();
		case OWNER_ROLE_ID:
			return getOwnerRoleId();
		case PRIORITY_CRITICAL:
			return getPriorityCritical();
		case PRIORITY_INFO:
			return getPriorityInfo();
		case PRIORITY_UNKNOWN:
			return getPriorityUnknown();
		case PRIORITY_WARNING:
			return getPriorityWarning();
		}
		throw new RuntimeException("Unknown type. type=" + type);
	}

	@Override
	public void setItemValue(StatusFilterItemType type, String value) {
		try {
			// enum値を追加したときに対応漏れが起きないようにするため、
			// case不足の警告が出るようにしたいので、全列挙している。
			switch (type) {
			case APPLICATION:
				setApplication(value);
				return;
			case GENERATION_DATE_FROM:
				setGenerationDateFrom(Long.valueOf(value));
				return;
			case GENERATION_DATE_TO:
				setGenerationDateTo(Long.valueOf(value));
				return;
			case MESSAGE:
				setMessage(value);
				return;
			case MONITOR_ID:
				setMonitorId(value);
				return;
			case MONITOR_DETAIL:
				setMonitorDetail(value);
				return;
			case OUTPUT_DATE_FROM:
				setOutputDateFrom(Long.valueOf(value));
				return;
			case OUTPUT_DATE_TO:
				setOutputDateTo(Long.valueOf(value));
				return;
			case OWNER_ROLE_ID:
				setOwnerRoleId(value);
				return;
			case PRIORITY_CRITICAL:
				setPriorityCritical(Boolean.valueOf(value));
				return;
			case PRIORITY_INFO:
				setPriorityInfo(Boolean.valueOf(value));
				return;
			case PRIORITY_UNKNOWN:
				setPriorityUnknown(Boolean.valueOf(value));
				return;
			case PRIORITY_WARNING:
				setPriorityWarning(Boolean.valueOf(value));
				return;
			}
		} catch (Exception e) {
			// 通常はありえない。ログに記録してスルーする。
			logger.warn("setPropertyOf: Failed."
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
	 * @param statusInfoAlias JPQL内での{@link StatusInfoEntity}のエイリアス。
	 */
	public StatusFilterConditionCriteria createCriteria(String uniqueId, String statusInfoAlias) {
		StatusFilterConditionCriteria crt = new StatusFilterConditionCriteria(uniqueId, statusInfoAlias);
		crt.priority.setValues(getPriorityCodes());
		crt.outputDate.setFromTo(outputDateFrom, outputDateTo);
		crt.generationDate.setFromTo(generationDateFrom, generationDateTo);
		crt.monitorId.setPattern(monitorId);
		crt.monitorDetail.setPattern(monitorDetail);
		crt.application.setPattern(application);
		crt.message.setPattern(message);
		crt.ownerRoleId.setPattern(ownerRoleId);
		crt.setNegative(getNegative() != null && getNegative().booleanValue());
		return crt;
	}

	/**
	 * true になっている重要度のコードリストを返します。
	 */
	public List<Integer> getPriorityCodes() {
		List<Integer> priorityList = new ArrayList<Integer>();
		if (Boolean.TRUE.equals(priorityInfo)) {
			priorityList.add(PriorityEnum.INFO.getCode());
		}
		if (Boolean.TRUE.equals(priorityWarning)) {
			priorityList.add(PriorityEnum.WARNING.getCode());
		}
		if (Boolean.TRUE.equals(priorityCritical)) {
			priorityList.add(PriorityEnum.CRITICAL.getCode());
		}
		if (Boolean.TRUE.equals(priorityUnknown)) {
			priorityList.add(PriorityEnum.UNKNOWN.getCode());
		}
		return priorityList;
	}

	/**
	 * 重要度(情報、警告、危険、不明)へ同じ値を一括設定します。
	 */
	public void setAllPriorities(Boolean value) {
		priorityInfo = value;
		priorityWarning = value;
		priorityCritical = value;
		priorityUnknown = value;
	}

	public Boolean getPriorityCritical() {
		return priorityCritical;
	}

	public void setPriorityCritical(Boolean priorityCritical) {
		this.priorityCritical = priorityCritical;
	}

	public Boolean getPriorityWarning() {
		return priorityWarning;
	}

	public void setPriorityWarning(Boolean priorityWarning) {
		this.priorityWarning = priorityWarning;
	}

	public Boolean getPriorityInfo() {
		return priorityInfo;
	}

	public void setPriorityInfo(Boolean priorityInfo) {
		this.priorityInfo = priorityInfo;
	}

	public Boolean getPriorityUnknown() {
		return priorityUnknown;
	}

	public void setPriorityUnknown(Boolean priorityUnknown) {
		this.priorityUnknown = priorityUnknown;
	}

	public Long getOutputDateFrom() {
		return outputDateFrom;
	}

	public void setOutputDateFrom(Long outputDateFrom) {
		this.outputDateFrom = outputDateFrom;
	}

	public Long getOutputDateTo() {
		return outputDateTo;
	}

	public void setOutputDateTo(Long outputDateTo) {
		this.outputDateTo = outputDateTo;
	}

	public Long getGenerationDateFrom() {
		return generationDateFrom;
	}

	public void setGenerationDateFrom(Long generationDateFrom) {
		this.generationDateFrom = generationDateFrom;
	}

	public Long getGenerationDateTo() {
		return generationDateTo;
	}

	public void setGenerationDateTo(Long generationDateTo) {
		this.generationDateTo = generationDateTo;
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getMonitorDetail() {
		return monitorDetail;
	}

	public void setMonitorDetail(String monitorDetail) {
		this.monitorDetail = monitorDetail;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

}
