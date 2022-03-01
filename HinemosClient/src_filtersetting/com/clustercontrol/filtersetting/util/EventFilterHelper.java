/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.util;

import org.openapitools.client.model.EventFilterBaseRequest;
import org.openapitools.client.model.EventFilterBaseRequest.FacilityTargetEnum;
import org.openapitools.client.model.EventFilterBaseResponse;
import org.openapitools.client.model.EventFilterConditionRequest;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.monitor.bean.CollectGraphFlgMessage;
import com.clustercontrol.monitor.bean.EventFilterConstant;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.bean.EventInfoConstant;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo.UserItemDisplayInfo;
import com.clustercontrol.monitor.util.EventHinemosPropertyUtil;
import com.clustercontrol.monitor.util.MonitorResultRestClientWrapper;
import com.clustercontrol.util.FilterConstant;
import com.clustercontrol.util.PropertyBuilder;
import com.clustercontrol.util.PropertyWrapper;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.TimezoneUtil;

/**
 * イベント履歴フィルタ設定関連のヘルパーです。
 */
public class EventFilterHelper {

	/**
	 * RequestDTO を複製して返します。
	 */
	public static EventFilterBaseRequest duplicate(EventFilterBaseRequest req) {
		EventFilterBaseRequest req2 = new EventFilterBaseRequest();
		try {
			RestClientBeanUtil.convertBean(req, req2);
		} catch (HinemosUnknown e) {
			// logic error
			throw new RuntimeException("Failed to duplicate EventFilterBaseRequest.\n"
					+ "req=" + req.toString());
		}
		return req2;
	}

	/**
	 * RequestDTO から ResponseDTO へ変換して返します。
	 */
	public static EventFilterBaseResponse convertToResponse(EventFilterBaseRequest req) {
		EventFilterBaseResponse response = new EventFilterBaseResponse();
		try {
			RestClientBeanUtil.convertBean(req, response);
		} catch (HinemosUnknown e) {
			// logic error
			throw new RuntimeException("Failed to convert EventFilterBaseRequest to EventFilterBaseResponse.\n"
					+ "req=" + req.toString() + "\nres=" + response.toString());
		}
		return response;
	}

	/**
	 * ResponseDTO から RequestDTO へ変換して返します。
	 */
	public static EventFilterBaseRequest convertToRequest(EventFilterBaseResponse res) {
		EventFilterBaseRequest request = new EventFilterBaseRequest();
		try {
			RestClientBeanUtil.convertBean(res, request);
		} catch (HinemosUnknown e) {
			// logic error
			throw new RuntimeException("Failed to convert EventFilterBaseResponse to EventFilterBaseRequest.\n"
					+ "res=" + res.toString() + "\nreq=" + request.toString());
		}
		return request;
	}

	/**
	 * デフォルトのフィルタ条件(基本条件＋詳細条件1シート)を返します。
	 */
	public static EventFilterBaseRequest createDefaultFilter(String facilityId) {
		EventFilterBaseRequest filter = new EventFilterBaseRequest();
		filter.setEntire(false);
		filter.setFacilityId(facilityId);
		filter.setFacilityTarget(FacilityTargetEnum.ALL);
		filter.addConditionsItem(createDefaultCondition());
		return filter;
	}

	/**
	 * デフォルトのフィルタ詳細条件(複数指定可能な条件)の1シート分を返します。
	 */
	public static EventFilterConditionRequest createDefaultCondition() {
		EventFilterConditionRequest o = new EventFilterConditionRequest();
		o.setPriorityInfo(true);
		o.setPriorityWarning(true);
		o.setPriorityCritical(true);
		o.setPriorityUnknown(true);
		o.setConfirmYet(true);
		o.setConfirmDoing(true);
		o.setConfirmDone(false);
		return o;
	}

	public enum PropertyConversionType {
		/** 通常のフィルタ条件 */
		NORMAL,
		/** 一括確認用のフィルタ条件 */
		BATCH_CONFIRM
	}

	/**
	 * フィルタ詳細条件を {@link Property} へ変換して返します。
	 * 
	 * @param cnd フィルタ詳細条件DTO。
	 * @param eventDspSetting 拡張項目のための追加情報。
	 * @param targetManagerName 検索対象マネージャ名。
	 * @param convType 変換の種類。
	 * @return
	 */
	public static Property convertConditionToProperty(EventFilterConditionRequest cnd, MultiManagerEventDisplaySettingInfo eventDspSetting,
			String targetManagerName, PropertyConversionType convType) {
		String SEPARATOR = FilterConstant.AND_SEPARATOR;

		Property property = new Property(null, null, "");
		property.removeChildren();

		// 重要度
		Property priority = new PropertyBuilder(EventFilterConstant.PRIORITY, "priority").build();
		priority.addChildren(new PropertyBuilder(EventFilterConstant.PRIORITY_CRITICAL, "critical").buildBool(cnd.getPriorityCritical()));
		priority.addChildren(new PropertyBuilder(EventFilterConstant.PRIORITY_WARNING, "warning").buildBool(cnd.getPriorityWarning()));
		priority.addChildren(new PropertyBuilder(EventFilterConstant.PRIORITY_INFO, "info").buildBool(cnd.getPriorityInfo()));
		priority.addChildren(new PropertyBuilder(EventFilterConstant.PRIORITY_UNKNOWN, "unknown").buildBool(cnd.getPriorityUnknown()));

		property.addChildren(priority);

		// 受信日時
		Property outputDate = new PropertyBuilder(EventFilterConstant.OUTPUT_DATE, "receive.time").build();
		outputDate.addChildren(new PropertyBuilder(EventFilterConstant.OUTPUT_FROM_DATE, "start").buildDateTime(cnd.getOutputDateFrom()));
		outputDate.addChildren(new PropertyBuilder(EventFilterConstant.OUTPUT_TO_DATE, "end").buildDateTime(cnd.getOutputDateTo()));

		property.addChildren(outputDate);

		// 出力日時
		Property generationDate = new PropertyBuilder(EventFilterConstant.GENERATION_DATE, "output.time").build();
		generationDate.addChildren(new PropertyBuilder(EventFilterConstant.GENERATION_FROM_DATE, "start").buildDateTime(cnd.getGenerationDateFrom()));
		generationDate.addChildren(new PropertyBuilder(EventFilterConstant.GENERATION_TO_DATE, "end").buildDateTime(cnd.getGenerationDateTo()));

		property.addChildren(generationDate);

		// 監視項目ID
		property.addChildren(new PropertyBuilder(EventFilterConstant.MONITOR_ID, "monitor.id")
				.setCopiable(true).buildMultipleTexts(cnd.getMonitorId(), SEPARATOR, DataRangeConstant.VARCHAR_64));

		// 監視項目詳細
		property.addChildren(new PropertyBuilder(EventFilterConstant.MONITOR_DETAIL_ID, "monitor.detail.id")
				.setCopiable(true).buildMultipleTexts(cnd.getMonitorDetail(), SEPARATOR, DataRangeConstant.VARCHAR_1024));

		// アプリケーション
		property.addChildren(new PropertyBuilder(EventFilterConstant.APPLICATION, "application")
				.setCopiable(true).buildMultipleTexts(cnd.getApplication(), SEPARATOR, DataRangeConstant.VARCHAR_64));

		// メッセージ
		property.addChildren(new PropertyBuilder(EventFilterConstant.MESSAGE, "message")
				.setCopiable(true).buildMultipleTexts(cnd.getMessage(), SEPARATOR, DataRangeConstant.VARCHAR_256));

		if (convType == PropertyConversionType.NORMAL) {
			// 確認
			Property confirm = new PropertyBuilder(EventFilterConstant.CONFIRMED, "confirmed").build();
			confirm.addChildren(new PropertyBuilder(EventFilterConstant.CONFIRMED_UNCONFIRMED, "monitor.unconfirmed").buildBool(cnd.getConfirmYet()));
			confirm.addChildren(new PropertyBuilder(EventFilterConstant.CONFIRMED_CONFIRMING, "monitor.confirming").buildBool(cnd.getConfirmDoing()));
			confirm.addChildren(new PropertyBuilder(EventFilterConstant.CONFIRMED_CONFIRMED, "monitor.confirmed").buildBool(cnd.getConfirmDone()));

			property.addChildren(confirm);

			// 確認ユーザ
			property.addChildren(new PropertyBuilder(EventFilterConstant.CONFIRM_USER, "confirm.user")
					.setCopiable(true).buildMultipleTexts(cnd.getConfirmUser(), SEPARATOR, DataRangeConstant.VARCHAR_64));
		}

		// コメント
		property.addChildren(new PropertyBuilder(EventFilterConstant.COMMENT, "comment")
				.setCopiable(true).buildMultipleTexts(cnd.getComment(), SEPARATOR, DataRangeConstant.VARCHAR_64));

		// コメントユーザ
		property.addChildren(new PropertyBuilder(EventFilterConstant.COMMENT_USER, "comment.user")
				.setCopiable(true).buildMultipleTexts(cnd.getCommentUser(), SEPARATOR, DataRangeConstant.VARCHAR_64));

		// 性能グラフ用フラグ
		Property graphFlg = new PropertyBuilder(EventFilterConstant.COLLECT_GRAPH_FLG, "collect.graph.flg", PropertyDefineConstant.EDITOR_SELECT)
				.setOptions("", CollectGraphFlgMessage.STRING_FLG_ON, CollectGraphFlgMessage.STRING_FLG_OFF)
				.setModifiable(true).build();

		if (cnd.getGraphFlag() != null) {
			if (cnd.getGraphFlag().booleanValue()) {
				graphFlg.setValue(CollectGraphFlgMessage.STRING_FLG_ON);
			} else {
				graphFlg.setValue(CollectGraphFlgMessage.STRING_FLG_OFF);
			}
		}

		property.addChildren(graphFlg);

		// オーナーロールID
		property.addChildren(new PropertyBuilder(EventFilterConstant.OWNER_ROLE_ID, "owner.role.id")
				.setCopiable(true).buildMultipleTexts(cnd.getOwnerRoleId(), SEPARATOR, DataRangeConstant.VARCHAR_64));

		// 通知のUUID
		property.addChildren(new PropertyBuilder(EventFilterConstant.NOTIFY_UUID, "notify.uuid")
				.setCopiable(true).buildText(cnd.getNotifyUUID(), DataRangeConstant.VARCHAR_64));

		// ユーザ項目
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; ++i) {
			UserItemDisplayInfo userItemInfo = eventDspSetting.getUserItemDisplayInfo(targetManagerName, i);
			if (!userItemInfo.getDisplayEnable()) continue;

			String value = null;
			if (cnd.getUserItems() != null) {
				value = cnd.getUserItems().get(String.valueOf(i));
			}
			String id = EventInfoConstant.getUserItemConst(i);
			String displayName = EventHinemosPropertyUtil.getDisplayName(userItemInfo.getDisplayName(), i);
			property.addChildren(new PropertyBuilder(id, displayName)
					.setCopiable(true).buildMultipleTexts(value, SEPARATOR, DataRangeConstant.VARCHAR_4096));
		}

		// イベント番号
		if (eventDspSetting.isEventNoDisplay(targetManagerName)) {
			Property position = new PropertyBuilder(EventInfoConstant.EVENT_NO, "monitor.eventno").build();
			position.addChildren(new PropertyBuilder(EventInfoConstant.EVENT_NO_FROM, "start").buildLong(cnd.getPositionFrom(), 0, DataRangeConstant.LONG_HIGH));
			position.addChildren(new PropertyBuilder(EventInfoConstant.EVENT_NO_TO, "end").buildLong(cnd.getPositionTo(), 0, DataRangeConstant.LONG_HIGH));

			property.addChildren(position);
		}

		return property;
	}

	/**
	 * {@link Property} をフィルタ詳細条件へ変換して返します。
	 * 
	 * @param property フィルタ詳細条件の入力値を保持するプロパティ。
	 * @param convType 変換の種類。
	 * @return フィルタ詳細条件を保持したDTO。
	 */
	public static EventFilterConditionRequest convertPropertyToCondition(Property property, PropertyConversionType convType) {
		EventFilterConditionRequest cnd = new EventFilterConditionRequest();

		PropertyWrapper p = new PropertyWrapper(property);
		p.setSimpleDateFormat(TimezoneUtil.getSimpleDateFormat(MonitorResultRestClientWrapper.DATETIME_FORMAT));
		String SEPARATOR = FilterConstant.AND_SEPARATOR;

		// 重要度
		cnd.setPriorityCritical(p.findBoolean(EventFilterConstant.PRIORITY_CRITICAL));
		cnd.setPriorityWarning(p.findBoolean(EventFilterConstant.PRIORITY_WARNING));
		cnd.setPriorityInfo(p.findBoolean(EventFilterConstant.PRIORITY_INFO));
		cnd.setPriorityUnknown(p.findBoolean(EventFilterConstant.PRIORITY_UNKNOWN));

		// 更新日時
		cnd.setOutputDateFrom(p.findTimeString(EventFilterConstant.OUTPUT_FROM_DATE));
		cnd.setOutputDateTo(p.findTimeString(EventFilterConstant.OUTPUT_TO_DATE));

		// 出力日時
		cnd.setGenerationDateFrom(p.findTimeString(EventFilterConstant.GENERATION_FROM_DATE));
		cnd.setGenerationDateTo(p.findTimeString(EventFilterConstant.GENERATION_TO_DATE));

		// 監視項目ID
		cnd.setMonitorId(p.joinNonEmptyStrings(EventFilterConstant.MONITOR_ID, SEPARATOR));

		// 監視詳細
		cnd.setMonitorDetail(p.joinNonEmptyStrings(EventFilterConstant.MONITOR_DETAIL_ID, SEPARATOR));

		// アプリケーション
		cnd.setApplication(p.joinNonEmptyStrings(EventFilterConstant.APPLICATION, SEPARATOR));

		// メッセージ
		cnd.setMessage(p.joinNonEmptyStrings(EventFilterConstant.MESSAGE, SEPARATOR));

		if (convType == PropertyConversionType.NORMAL) {
			// 確認有無
			cnd.setConfirmYet(p.findBoolean(EventFilterConstant.CONFIRMED_UNCONFIRMED));
			cnd.setConfirmDoing(p.findBoolean(EventFilterConstant.CONFIRMED_CONFIRMING));
			cnd.setConfirmDone(p.findBoolean(EventFilterConstant.CONFIRMED_CONFIRMED));

			// 確認ユーザ
			cnd.setConfirmUser(p.joinNonEmptyStrings(EventFilterConstant.CONFIRM_USER, SEPARATOR));
		}

		// コメント
		cnd.setComment(p.joinNonEmptyStrings(EventFilterConstant.COMMENT, SEPARATOR));

		// コメントユーザ
		cnd.setCommentUser(p.joinNonEmptyStrings(EventFilterConstant.COMMENT_USER, SEPARATOR));

		// オーナーロールID
		cnd.setOwnerRoleId(p.joinNonEmptyStrings(EventFilterConstant.OWNER_ROLE_ID, SEPARATOR));

		// 通知UUID
		cnd.setNotifyUUID(p.joinNonEmptyStrings(EventFilterConstant.NOTIFY_UUID, SEPARATOR));

		// 性能グラフ用フラグ
		cnd.setGraphFlag(CollectGraphFlgMessage.stringToType(p.findString(EventFilterConstant.COLLECT_GRAPH_FLG)));

		// ユーザ項目
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			cnd.putUserItemsItem(
					String.valueOf(i),
					p.joinNonEmptyStrings(EventFilterConstant.getUserItemConst(i), SEPARATOR));
		}

		//イベント番号
		cnd.setPositionFrom(p.findLong(EventFilterConstant.EVENT_NO_FROM));
		cnd.setPositionTo(p.findLong(EventFilterConstant.EVENT_NO_TO));

		return cnd;
	}

}
