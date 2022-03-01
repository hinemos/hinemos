/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.QueryCriteria;
import com.clustercontrol.filtersetting.entity.FilterConditionEntity;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.rest.endpoint.monitorresult.dto.enumtype.ConfirmFlgTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityEnum;

/**
 * イベント履歴フィルタ詳細情報。
 * フィルタ詳細情報は、複数指定(OR結合)可能な条件の情報です。
 */
public class EventFilterConditionInfo extends FilterConditionInfo<EventFilterItemType> {
	private static final Log logger = LogFactory.getLog(EventFilterConditionInfo.class);

	/** 重要度の種類の総数 */
	public static final int PRIORITY_VARIATION = 4;

	/** 確認フラグの種類の総数 */
	public static final int CONFIRM_VARIATION = 3;

	// 初期値は全件検索 (FilterConditionItemEntitiyがない場合に制限されない)

	/** 重要度危険 */
	private Boolean priorityCritical = Boolean.TRUE;

	/** 重要度警告 */
	private Boolean priorityWarning = Boolean.TRUE;

	/** 重要度情報 */
	private Boolean priorityInfo = Boolean.TRUE;

	/** 重要度不明 */
	private Boolean priorityUnknown = Boolean.TRUE;

	/** 出力日時 開始 */
	private Long generationDateFrom;

	/** 出力日時 終了 */
	private Long generationDateTo;

	/** 受信日時 開始 */
	private Long outputDateFrom;

	/** 受信日時 終了 */
	private Long outputDateTo;

	/** 監視項目ID */
	private String monitorId;

	/** 監視詳細 */
	private String monitorDetail;

	/** アプリケーション */
	private String application;

	/** メッセージ */
	private String message;

	/** 確認 未確認 */
	private Boolean confirmYet = Boolean.TRUE;

	/** 確認 確認中 */
	private Boolean confirmDoing = Boolean.TRUE;

	/** 確認 確認済み */
	private Boolean confirmDone = Boolean.TRUE;

	/** 確認ユーザ */
	private String confirmUser;

	/** コメント */
	private String comment;

	/** コメント更新ユーザ */
	private String commentUser;

	/** 性能グラフ用フラグ */
	private Boolean graphFlag;

	/** オーナーロールID */
	private String ownerRoleId;

	/** 通知のUUID */
	private String notifyUUID;

	/** ユーザ定義項目 */
	private Map<String, String> userItems = new HashMap<>();

	/** イベント番号 開始 */
	private Long positionFrom;

	/** イベント番号 終了 */
	private Long positionTo;

	/**
	 * 全件検索設定を行ったインスタンスを生成します。
	 */
	public static EventFilterConditionInfo ofAllEvents() {
		EventFilterConditionInfo o = new EventFilterConditionInfo();
		return o;
	}

	/**
	 * クライアントビュー表示用のデフォルト設定を行ったインスタンスを生成します。
	 */
	public static EventFilterConditionInfo ofClientViewDefault() {
		EventFilterConditionInfo o = new EventFilterConditionInfo();
		o.confirmDone = Boolean.FALSE;
		return o;
	}

	/**
	 * ファイルダウンロード用のデフォルト設定を行ったインスタンスを生成します。
	 */
	public static EventFilterConditionInfo ofDownloadDefault() {
		return ofAllEvents();
	}

	/**
	 * 一括確認用のデフォルト設定を行ったインスタンスを生成します。
	 */
	public static EventFilterConditionInfo ofBatchConfirmingDefault() {
		// 一括確認では本クラスの confirm* の値などは使用されないが、一応適当に設定はしておく
		return ofClientViewDefault();
	}

	public EventFilterConditionInfo() {
		super();
	}

	public EventFilterConditionInfo(FilterConditionEntity entity) {
		super(entity);
		initializeItems(entity);
	}

	@Override
	public EventFilterItemType convertFilterItemType(Integer dbValue) {
		Objects.requireNonNull(dbValue, "dbValue");
		return EventFilterItemType.fromCode(dbValue);
	}

	@Override
	public EventFilterItemType[] getAllFilterItemTypes() {
		return EventFilterItemType.values();
	}

	@Override
	public Object getItemValue(EventFilterItemType type) {
		// enum値を追加したときに対応漏れが起きないようにするため、
		// case不足の警告が出るようにしたいので、全列挙している。
		switch (type) {
		case APPLICATION:
			return getApplication();
		case COLLECT_GRAPH_FLG:
			return getGraphFlag();
		case COMMENT:
			return getComment();
		case COMMENT_USER:
			return getCommentUser();
		case CONFIRM_FLG_DOING:
			return getConfirmDoing();
		case CONFIRM_FLG_DONE:
			return getConfirmDone();
		case CONFIRM_FLG_NOT_YET:
			return getConfirmYet();
		case CONFIRM_USER:
			return getConfirmUser();
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
		case POSITION_FROM:
			return getPositionFrom();
		case POSITION_TO:
			return getPositionTo();
		case PRIORITY_CRITICAL:
			return getPriorityCritical();
		case PRIORITY_INFO:
			return getPriorityInfo();
		case PRIORITY_UNKNOWN:
			return getPriorityUnknown();
		case PRIORITY_WARNING:
			return getPriorityWarning();
		// @formatter:off
        case USER_ITEM_01: case USER_ITEM_02: case USER_ITEM_03: case USER_ITEM_04: case USER_ITEM_05: case USER_ITEM_06: case USER_ITEM_07: case USER_ITEM_08: case USER_ITEM_09: case USER_ITEM_10:
        case USER_ITEM_11: case USER_ITEM_12: case USER_ITEM_13: case USER_ITEM_14: case USER_ITEM_15: case USER_ITEM_16: case USER_ITEM_17: case USER_ITEM_18: case USER_ITEM_19: case USER_ITEM_20:
        case USER_ITEM_21: case USER_ITEM_22: case USER_ITEM_23: case USER_ITEM_24: case USER_ITEM_25: case USER_ITEM_26: case USER_ITEM_27: case USER_ITEM_28: case USER_ITEM_29: case USER_ITEM_30:
        case USER_ITEM_31: case USER_ITEM_32: case USER_ITEM_33: case USER_ITEM_34: case USER_ITEM_35: case USER_ITEM_36: case USER_ITEM_37: case USER_ITEM_38: case USER_ITEM_39: case USER_ITEM_40:
        // @formatter:on
			int num = type.getCode().intValue() - EventFilterItemType.USER_ITEM_01.getCode().intValue() + 1;
			return getUserItem(num);
		case NOTIFY_UUID:
			return getNotifyUUID();
		}
		throw new RuntimeException("Unknown type. type=" + type);
	}

	@Override
	public void setItemValue(EventFilterItemType type, String value) {
		try {
			// enum値を追加したときに対応漏れが起きないようにするため、
			// case不足の警告が出るようにしたいので、全列挙している。
			switch (type) {
			case APPLICATION:
				setApplication(value);
				return;
			case COLLECT_GRAPH_FLG:
				setGraphFlag(Boolean.valueOf(value));
				return;
			case COMMENT:
				setComment(value);
				return;
			case COMMENT_USER:
				setCommentUser(value);
				return;
			case CONFIRM_FLG_DOING:
				setConfirmDoing(Boolean.valueOf(value));
				return;
			case CONFIRM_FLG_DONE:
				setConfirmDone(Boolean.valueOf(value));
				return;
			case CONFIRM_FLG_NOT_YET:
				setConfirmYet(Boolean.valueOf(value));
				return;
			case CONFIRM_USER:
				setConfirmUser(value);
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
			case MONITOR_DETAIL:
				setMonitorDetail(value);
				return;
			case MONITOR_ID:
				setMonitorId(value);
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
			case NOTIFY_UUID:
				setNotifyUUID(value);
				return;
			case POSITION_FROM:
				setPositionFrom(Long.valueOf(value));
				return;
			case POSITION_TO:
				setPositionTo(Long.valueOf(value));
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
			// @formatter:off
            case USER_ITEM_01: case USER_ITEM_02: case USER_ITEM_03: case USER_ITEM_04: case USER_ITEM_05: case USER_ITEM_06: case USER_ITEM_07: case USER_ITEM_08: case USER_ITEM_09: case USER_ITEM_10:
            case USER_ITEM_11: case USER_ITEM_12: case USER_ITEM_13: case USER_ITEM_14: case USER_ITEM_15: case USER_ITEM_16: case USER_ITEM_17: case USER_ITEM_18: case USER_ITEM_19: case USER_ITEM_20:
            case USER_ITEM_21: case USER_ITEM_22: case USER_ITEM_23: case USER_ITEM_24: case USER_ITEM_25: case USER_ITEM_26: case USER_ITEM_27: case USER_ITEM_28: case USER_ITEM_29: case USER_ITEM_30:
            case USER_ITEM_31: case USER_ITEM_32: case USER_ITEM_33: case USER_ITEM_34: case USER_ITEM_35: case USER_ITEM_36: case USER_ITEM_37: case USER_ITEM_38: case USER_ITEM_39: case USER_ITEM_40:
            // @formatter:on
				int num = type.getCode().intValue() - EventFilterItemType.USER_ITEM_01.getCode().intValue() + 1;
				setUserItem(num, value);
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

	// eclipseで自動生成したメソッドではないので、自動生成を再実施する際にまとめて削除しないように注意
	public String getUserItem(int num) {
		if (num < 1 || num > EventFilterItemType.MAX_USER_ITEM_NUMBER) {
			throw new IllegalArgumentException("Illegal userItem number: " + num);
		}
		return userItems.get(String.valueOf(num));
	}

	// eclipseで自動生成したメソッドではないので、自動生成を再実施する際にまとめて削除しないように注意
	public void setUserItem(int num, String value) {
		if (num < 1 || num > EventFilterItemType.MAX_USER_ITEM_NUMBER) {
			throw new IllegalArgumentException("Illegal userItem number: " + num);
		}
		userItems.put(String.valueOf(num), value);
	}

	/**
	 * SQL条件式構築オブジェクトを生成します。
	 * 
	 * @param uniqueId ユニークID。詳細は{@link QueryCriteria}を参照。
	 * @param eventLogAlias JPQL内での{@link EventLogEntity}のエイリアス。
	 */
	public EventFilterConditionCriteria createCriteria(String uniqueId, String eventLogAlias) {
		EventFilterConditionCriteria ec = new EventFilterConditionCriteria(uniqueId, eventLogAlias);
		ec.priority.setValues(getPriorityCodes());
		ec.outputDate.setFromTo(outputDateFrom, outputDateTo);
		ec.generationDate.setFromTo(generationDateFrom, generationDateTo);
		ec.monitorId.setPattern(monitorId);
		ec.monitorDetail.setPattern(monitorDetail);
		ec.application.setPattern(application);
		ec.message.setPattern(message);
		ec.comment.setPattern(comment);
		ec.commentUser.setPattern(commentUser);
		ec.graphFlag.setValue(graphFlag);
		ec.confirmFlag.setValues(getConfirmFlagCodes());
		ec.confirmUser.setPattern(confirmUser);
		ec.ownerRoleId.setPattern(ownerRoleId);
		for (int i = 1; i <= EventFilterItemType.MAX_USER_ITEM_NUMBER; ++i) {
			ec.userItems.setValue(i, getUserItem(i));
		}
		ec.position.setFromTo(positionFrom, positionTo);
		ec.notifyUUID.setValue(notifyUUID);
		ec.setNegative(getNegative() != null && getNegative().booleanValue());
		return ec;
	}

	/**
	 * true になっている確認フラグのコードリストを返します。
	 */
	public List<Integer> getConfirmFlagCodes() {
		List<Integer> confirmFlgTypeList = new ArrayList<Integer>();
		if (Boolean.TRUE.equals(confirmYet)) {
			confirmFlgTypeList.add(ConfirmFlgTypeEnum.TYPE_UNCONFIRMED.getCode());
		}
		if (Boolean.TRUE.equals(confirmDoing)) {
			confirmFlgTypeList.add(ConfirmFlgTypeEnum.TYPE_CONFIRMING.getCode());
		}
		if (Boolean.TRUE.equals(confirmDone)) {
			confirmFlgTypeList.add(ConfirmFlgTypeEnum.TYPE_CONFIRMED.getCode());
		}
		return confirmFlgTypeList;
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

	/**
	 * 確認フラグ(未確認、確認中、確認済み)へ同じ値を一括設定します。
	 */
	public void setAllConfirmFlags(Boolean value) {
		confirmYet = value;
		confirmDoing = value;
		confirmDone = value;
	}

	/**
	 * 確認フラグを設定します。
	 * 
	 * @param flagCode フラグの種類を示す {@link ConfirmConstant} で定義されている定数値。
	 * @param value フラグ設定値。
	 */
	public void setConfirmFlag(int flagCode, Boolean value) {
		switch (flagCode) {
		case ConfirmConstant.TYPE_UNCONFIRMED:
			confirmYet = value;
			break;
		case ConfirmConstant.TYPE_CONFIRMING:
			confirmDoing = value;
			break;
		case ConfirmConstant.TYPE_CONFIRMED:
			confirmDone = value;
			break;
		default:
			// NEVER
			throw new RuntimeException("Unknown code=" + flagCode);
		}
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

	public Boolean getConfirmYet() {
		return confirmYet;
	}

	public void setConfirmYet(Boolean confirmYet) {
		this.confirmYet = confirmYet;
	}

	public Boolean getConfirmDoing() {
		return confirmDoing;
	}

	public void setConfirmDoing(Boolean confirmDoing) {
		this.confirmDoing = confirmDoing;
	}

	public Boolean getConfirmDone() {
		return confirmDone;
	}

	public void setConfirmDone(Boolean confirmDone) {
		this.confirmDone = confirmDone;
	}

	public String getConfirmUser() {
		return confirmUser;
	}

	public void setConfirmUser(String confirmUser) {
		this.confirmUser = confirmUser;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCommentUser() {
		return commentUser;
	}

	public void setCommentUser(String commentUser) {
		this.commentUser = commentUser;
	}

	public Boolean getGraphFlag() {
		return graphFlag;
	}

	public void setGraphFlag(Boolean graphFlag) {
		this.graphFlag = graphFlag;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public Map<String, String> getUserItems() {
		return userItems;
	}

	public void setUserItems(Map<String, String> userItems) {
		Objects.requireNonNull(userItems, "userItems");
		this.userItems = userItems;
	}

	public Long getPositionFrom() {
		return positionFrom;
	}

	public void setPositionFrom(Long positionFrom) {
		this.positionFrom = positionFrom;
	}

	public Long getPositionTo() {
		return positionTo;
	}

	public void setPositionTo(Long positionTo) {
		this.positionTo = positionTo;
	}

	public String getNotifyUUID() {
		return notifyUUID;
	}

	public void setNotifyUUID(String notifyUUID) {
		this.notifyUUID = notifyUUID;
	}
}
