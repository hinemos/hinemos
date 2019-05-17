/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.monitor.bean.CollectGraphFlgMessage;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.monitor.bean.EventFilterConstant;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.bean.EventInfoConstant;
import com.clustercontrol.monitor.bean.StatusFilterConstant;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo.UserItemDisplayInfo;
import com.clustercontrol.repository.bean.FacilityTargetMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.ws.monitor.EventFilterInfo;

public class EventFilterPropertyUtil {
	/**
	 * プロパティをイベント情報フィルタDTOに変換するメソッドです。
	 * 
	 * @param property
	 * @return イベント情報フィルタ
	 */
	public static EventFilterInfo property2dto(Property property, MultiManagerEventDisplaySettingInfo eventDspSetting, String managerName){
		EventFilterInfo info = new EventFilterInfo();

		Timestamp outputFromDate = null;
		Timestamp outputToDate = null;
		Timestamp generationFromDate = null;
		Timestamp generationToDate = null;
		String monitorId = null;
		String monitorDetailId = null;
		Integer facilityType = null;
		String application = null;
		String message = null;
		String confirmUser = null;
		String comment = null;
		String commentUser = null;
		String ownerRoleId = null;

		ArrayList<?> values = null;

		final int fromDateNanos = 0;
		final int toDateNanos = 999999999;
		
		// 全検索
		values = PropertyUtil.getPropertyValue(property,
				EventFilterConstant.ALL_SEARCH);
		if (values.get(0) instanceof Boolean) {
			info.setAllSearch((Boolean)values.get(0));
		}

		//重要度取得
		values = PropertyUtil.getPropertyValue(property,
				EventFilterConstant.PRIORITY_CRITICAL);
		if (!"".equals(values.get(0))) {
			if ((Boolean)values.get(0)) {
				info.getPriorityList().add(PriorityConstant.TYPE_CRITICAL);
			}
		}
		values = PropertyUtil.getPropertyValue(property,
				EventFilterConstant.PRIORITY_WARNING);
		if (!"".equals(values.get(0))) {
			if ((Boolean)values.get(0)) {
				info.getPriorityList().add(PriorityConstant.TYPE_WARNING);
			}
		}
		values = PropertyUtil.getPropertyValue(property,
				EventFilterConstant.PRIORITY_INFO);
		if (!"".equals(values.get(0))) {
			if ((Boolean)values.get(0)) {
				info.getPriorityList().add(PriorityConstant.TYPE_INFO);
			}
		}
		values = PropertyUtil.getPropertyValue(property,
				EventFilterConstant.PRIORITY_UNKNOWN);
		if (!"".equals(values.get(0))) {
			if ((Boolean)values.get(0)) {
				info.getPriorityList().add(PriorityConstant.TYPE_UNKNOWN);
			}
		}

		//更新日時（自）取得
		values = PropertyUtil.getPropertyValue(property,
				EventFilterConstant.OUTPUT_FROM_DATE);
		if (values.get(0) instanceof Date) {
			outputFromDate = new Timestamp(((Date) values.get(0)).getTime());
			outputFromDate.setNanos(fromDateNanos);
			info.setOutputDateFrom(outputFromDate.getTime());
		}

		//更新日時（至）取得
		values = PropertyUtil.getPropertyValue(property,
				EventFilterConstant.OUTPUT_TO_DATE);
		if (values.get(0) instanceof Date) {
			outputToDate = new Timestamp(((Date) values.get(0)).getTime());
			outputToDate.setNanos(toDateNanos);
			info.setOutputDateTo(outputToDate.getTime());
		}

		//出力日時（自）取得
		values = PropertyUtil.getPropertyValue(property,
				EventFilterConstant.GENERATION_FROM_DATE);
		if (values.get(0) instanceof Date) {
			generationFromDate = new Timestamp(((Date) values.get(0))
					.getTime());
			generationFromDate.setNanos(fromDateNanos);
			info.setGenerationDateFrom(generationFromDate.getTime());
		}

		//出力日時（至）取得
		values = PropertyUtil.getPropertyValue(property,
				EventFilterConstant.GENERATION_TO_DATE);
		if (values.get(0) instanceof Date) {
			generationToDate = new Timestamp(((Date) values.get(0))
					.getTime());
			generationToDate.setNanos(toDateNanos);
			info.setGenerationDateTo(generationToDate.getTime());
		}

		//監視項目ID取得
		values = PropertyUtil.getPropertyValue(property,
				EventFilterConstant.MONITOR_ID);
		if (!"".equals(values.get(0))) {
			monitorId = (String) values.get(0);
			info.setMonitorId(monitorId);
		}

		//監視詳細取得
		values = PropertyUtil.getPropertyValue(property,
				EventFilterConstant.MONITOR_DETAIL_ID);
		if (!"".equals(values.get(0))) {
			monitorDetailId = (String) values.get(0);
			info.setMonitorDetailId(monitorDetailId);
		}

		//対象ファシリティ種別取得
		values = PropertyUtil.getPropertyValue(property,
				StatusFilterConstant.FACILITY_TYPE);
		if(values.size() > 0 && values.get(0) instanceof String) {
			String facilityTypeStr = (String)values.get(0);
			facilityType = FacilityTargetMessage.stringToType(facilityTypeStr);
			info.setFacilityType(facilityType);
		}

		//アプリケーション取得
		values = PropertyUtil.getPropertyValue(property,
				StatusFilterConstant.APPLICATION);
		if (!"".equals(values.get(0))) {
			application = (String) values.get(0);
			info.setApplication(application);
		}

		//メッセージ取得
		values = PropertyUtil.getPropertyValue(property,
				StatusFilterConstant.MESSAGE);
		if (!"".equals(values.get(0))) {
			message = (String) values.get(0);
			info.setMessage(message);
		}

		// 確認有無取得
		values = PropertyUtil.getPropertyValue(property,
				EventFilterConstant.CONFIRMED_UNCONFIRMED);
		if (!"".equals(values.get(0))) {
			if ((Boolean)values.get(0)) {
				info.getConfirmFlgTypeList().add(ConfirmConstant.TYPE_UNCONFIRMED);
			}
		}
		values = PropertyUtil.getPropertyValue(property,
				EventFilterConstant.CONFIRMED_CONFIRMING);
		if (!"".equals(values.get(0))) {
			if ((Boolean)values.get(0)) {
				info.getConfirmFlgTypeList().add(ConfirmConstant.TYPE_CONFIRMING);
			}
		}
		values = PropertyUtil.getPropertyValue(property,
				EventFilterConstant.CONFIRMED_CONFIRMED);
		if (!"".equals(values.get(0))) {
			if ((Boolean)values.get(0)) {
				info.getConfirmFlgTypeList().add(ConfirmConstant.TYPE_CONFIRMED);
			}
		}

		// 確認ユーザ
		values = PropertyUtil.getPropertyValue(property,
				EventFilterConstant.CONFIRM_USER);
		if (!"".equals(values.get(0))) {
			confirmUser = (String) values.get(0);
		}
		info.setConfirmedUser(confirmUser);

		// コメント
		values = PropertyUtil.getPropertyValue(property,
				EventFilterConstant.COMMENT);
		if (!"".equals(values.get(0))) {
			comment = (String) values.get(0);
		}
		info.setComment(comment);

		// コメントユーザ
		values = PropertyUtil.getPropertyValue(property,
				EventFilterConstant.COMMENT_USER);
		if (!"".equals(values.get(0))) {
			commentUser = (String) values.get(0);
		}
		info.setCommentUser(commentUser);

		// オーナーロールID
		values = PropertyUtil.getPropertyValue(property,
				EventFilterConstant.OWNER_ROLE_ID);
		if (!"".equals(values.get(0))) {
			ownerRoleId = (String) values.get(0);
		}
		info.setOwnerRoleId(ownerRoleId);

		// 性能グラフ用フラグ
		values = PropertyUtil.getPropertyValue(property, 
				EventFilterConstant.COLLECT_GRAPH_FLG);
		Boolean collectFlg = CollectGraphFlgMessage.stringToType((String)values.get(0));
		info.setCollectGraphFlg(collectFlg);

		//ユーザ項目の設定
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			String userItemValue = null;
			UserItemDisplayInfo userItemInfo = eventDspSetting.getUserItemDisplayInfo(managerName, i);
			if (!userItemInfo.getDisplayEnable()) {
				continue;
			}
			
			values = PropertyUtil.getPropertyValue(property, 
					EventFilterConstant.getUserItemConst(i));
			if (!"".equals(values.get(0))) {
				userItemValue = (String) values.get(0);
			}
			
			EventUtil.setUserItemValue(info, i, userItemValue);
		}
		//イベント情報の設定
		if (eventDspSetting.isEventNoDisplay(managerName)){ 
			//イベント番号（自）取得
			values = PropertyUtil.getPropertyValue(property,
					EventFilterConstant.EVENT_NO_FROM);
			if (values.get(0) instanceof Long) {
				info.setPositionFrom((Long) values.get(0));
			}

			//イベント番号（至）取得
			values = PropertyUtil.getPropertyValue(property,
					EventFilterConstant.EVENT_NO_TO);
			if (values.get(0) instanceof Long) {
				info.setPositionTo((Long) values.get(0));
			}
		}
		
		return info;
	}

	/**
	 * イベント情報フィルタ用プロパティを取得します。<BR>
	 * <p>
	 * <ol>
	 *  <li>フィルタ項目毎にID, 名前, 処理定数（{@link com.clustercontrol.bean.PropertyDefineConstant}）を指定し、
	 *      プロパティ（{@link com.clustercontrol.bean.Property}）を生成します。</li>
	 *  <li>各フィルタ項目のプロパティをツリー状に定義します。</li>
	 * </ol>
	 * 
	 * <p>プロパティに定義するフィルタ条件は、下記の通りです。
	 * <p>
	 * <ul>
	 *  <li>プロパティ（親。ダミー）</li>
	 *  <ul>
	 *   <li>重要度（子。テキスト）</li>
	 *   <ul>
	 *    <li>危険（孫。チェックボックス）</li>
	 *    <li>警告（孫。チェックボックス）</li>
	 *    <li>情報（孫。チェックボックス）</li>
	 *    <li>不明（孫。チェックボックス）</li>
	 *   </ul>
	 *   <li>受信日時（子。テキスト）</li>
	 *   <ul>
	 *    <li>開始（孫。日時ダイアログ）</li>
	 *    <li>終了（孫。日時ダイアログ）</li>
	 *   </ul>
	 *   <li>出力日時（子。テキスト）</li>
	 *   <ul>
	 *    <li>開始（孫。日時ダイアログ）</li>
	 *    <li>終了（孫。日時ダイアログ）</li>
	 *   </ul>
	 *   <li>監視項目ID（子。テキスト）</li>
	 *   <li>監視詳細（子。テキスト）</li>
	 *   <li>ファシリティのターゲット（子。コンボボックス）</li>
	 *   <li>アプリケーション（子。テキスト）</li>
	 *   <li>メッセージ（子。テキスト）</li>
	 *   <li>確認（子。コンボボックス）</li>
	 *   <li>コメント</li>
	 *   <li>コメントユーザ</li>
	 *  </ul>
	 * </ul>
	 * 
	 * @param locale ロケール情報
	 * @return イベント情報フィルタ用プロパティ
	 * 
	 * @see com.clustercontrol.bean.Property
	 * @see com.clustercontrol.bean.PropertyDefineConstant
	 * @see com.clustercontrol.bean.PriorityConstant
	 * @see com.clustercontrol.bean.FacilityTargetConstant
	 * @see com.clustercontrol.bean.ConfirmConstant
	 */
	public static Property getProperty(Locale locale, MultiManagerEventDisplaySettingInfo eventDspSetting, String managerName) {
		//全検索
		Property m_allSearch =
				new Property(EventFilterConstant.ALL_SEARCH, Messages.getString("all.search", locale), PropertyDefineConstant.EDITOR_BOOL);
		//重要度
		Property m_priority =
				new Property(EventFilterConstant.PRIORITY, Messages.getString("priority", locale), PropertyDefineConstant.EDITOR_TEXT);
		//重要度（危険）
		Property m_priorityCritical =
				new Property(EventFilterConstant.PRIORITY_CRITICAL, Messages.getString("critical", locale), PropertyDefineConstant.EDITOR_BOOL);
		//重要度(警告)
		Property m_priorityWarning =
				new Property(EventFilterConstant.PRIORITY_WARNING, Messages.getString("warning", locale), PropertyDefineConstant.EDITOR_BOOL);
		//重要度(情報)
		Property m_priorityInfo =
				new Property(EventFilterConstant.PRIORITY_INFO, Messages.getString("info", locale), PropertyDefineConstant.EDITOR_BOOL);
		//重要度(不明)
		Property m_priorityUnknown =
				new Property(EventFilterConstant.PRIORITY_UNKNOWN, Messages.getString("unknown", locale), PropertyDefineConstant.EDITOR_BOOL);
		
		//受信日時（自）
		Property m_outputFromDate =
				new Property(EventFilterConstant.OUTPUT_FROM_DATE, Messages.getString("start", locale), PropertyDefineConstant.EDITOR_DATETIME);
		//受信日時（至）
		Property m_outputToDate =
				new Property(EventFilterConstant.OUTPUT_TO_DATE, Messages.getString("end", locale), PropertyDefineConstant.EDITOR_DATETIME);
		//出力日時（自）
		Property m_generationFromDate =
				new Property(EventFilterConstant.GENERATION_FROM_DATE, Messages.getString("start", locale), PropertyDefineConstant.EDITOR_DATETIME);
		//出力日時（至）
		Property m_generationToDate =
				new Property(EventFilterConstant.GENERATION_TO_DATE, Messages.getString("end", locale), PropertyDefineConstant.EDITOR_DATETIME);
		
		//監視項目ID
		Property m_monitorId =
				new Property(EventFilterConstant.MONITOR_ID, Messages.getString("monitor.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//監視項目詳細
		Property m_monitorDetailId =
				new Property(EventFilterConstant.MONITOR_DETAIL_ID, Messages.getString("monitor.detail.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		
		//対象ファシリティ種別
		Property m_facilityType =
				new Property(EventFilterConstant.FACILITY_TYPE, Messages.getString("facility.target", locale), PropertyDefineConstant.EDITOR_SELECT);
		//アプリケーション
		Property m_application =
				new Property(EventFilterConstant.APPLICATION, Messages.getString("application", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//メッセージ
		Property m_message =
				new Property(EventFilterConstant.MESSAGE, Messages.getString("message", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//確認
		Property m_confirmed =
				new Property(EventFilterConstant.CONFIRMED, Messages.getString("confirmed", locale), PropertyDefineConstant.EDITOR_TEXT);
		//未確認
		Property m_confirmedUnconfirmed =
				new Property(EventFilterConstant.CONFIRMED_UNCONFIRMED, Messages.getString("monitor.unconfirmed", locale), PropertyDefineConstant.EDITOR_BOOL);
		//確認中
		Property m_confirmedConfirming =
				new Property(EventFilterConstant.CONFIRMED_CONFIRMING, Messages.getString("monitor.confirming", locale), PropertyDefineConstant.EDITOR_BOOL);
		//確認済
		Property m_confirmedConfirmed =
				new Property(EventFilterConstant.CONFIRMED_CONFIRMED, Messages.getString("monitor.confirmed", locale), PropertyDefineConstant.EDITOR_BOOL);
		//受信日時
		Property m_outputDate =
				new Property(EventFilterConstant.OUTPUT_DATE, Messages.getString("receive.time", locale), PropertyDefineConstant.EDITOR_TEXT);
		//出力日時
		Property m_generationDate =
				new Property(EventFilterConstant.GENERATION_DATE, Messages.getString("output.time", locale), PropertyDefineConstant.EDITOR_TEXT);

		//確認ユーザ
		Property m_confirmedUser =
				new Property(EventFilterConstant.CONFIRM_USER, Messages.getString("confirm.user", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		// コメント
		Property m_comment =
				new Property(EventFilterConstant.COMMENT, Messages.getString("comment", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		// コメントユーザ
		Property m_commentUser =
				new Property(EventFilterConstant.COMMENT_USER, Messages.getString("comment.user", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		// 性能グラフ用フラグ
		Property m_collectGraphFlg =
				new Property(EventFilterConstant.COLLECT_GRAPH_FLG, Messages.getString("collect.graph.flg", locale), PropertyDefineConstant.EDITOR_SELECT);

		//オーナーロールID
		Property m_ownerRoleId =
				new Property(EventFilterConstant.OWNER_ROLE_ID, Messages.getString("owner.role.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		//値を初期化
		m_allSearch.setValue(false);
		m_priority.setValue("");
		m_priorityCritical.setValue(true);
		m_priorityWarning.setValue(true);
		m_priorityInfo.setValue(true);
		m_priorityUnknown.setValue(true);
		
		m_outputFromDate.setValue("");
		m_outputToDate.setValue("");
		m_generationFromDate.setValue("");
		m_generationToDate.setValue("");

		m_monitorId.setValue("");
		m_monitorDetailId.setValue("");

		Object facilityTypeValues[][] = {
				{ FacilityTargetMessage.STRING_ALL, FacilityTargetMessage.STRING_BENEATH},
				{ FacilityTargetMessage.STRING_ALL, FacilityTargetMessage.STRING_BENEATH}};

		m_facilityType.setSelectValues(facilityTypeValues);
		m_facilityType.setValue(FacilityTargetMessage.STRING_ALL);

		m_application.setValue("");
		m_message.setValue("");

		m_confirmed.setValue("");
		m_confirmedUnconfirmed.setValue(true);
		m_confirmedConfirming.setValue(true);
		m_confirmedConfirmed.setValue(false);

		m_outputDate.setValue("");
		m_generationDate.setValue("");

		m_confirmedUser.setValue("");

		m_comment.setValue("");
		m_commentUser.setValue("");

		m_ownerRoleId.setValue("");
		
		Object collectValues[][] = {
				{"", CollectGraphFlgMessage.STRING_FLG_ON, CollectGraphFlgMessage.STRING_FLG_OFF},
				{"", CollectGraphFlgMessage.STRING_FLG_ON, CollectGraphFlgMessage.STRING_FLG_OFF}};
		m_collectGraphFlg.setSelectValues(collectValues);
		m_collectGraphFlg.setValue("");


		//変更の可/不可を設定
		m_allSearch.setModify(PropertyDefineConstant.MODE_MODIFY);
		m_priority.setModify(PropertyDefineConstant.MODIFY_NG);
		m_priorityCritical.setModify(PropertyDefineConstant.MODIFY_OK);
		m_priorityWarning.setModify(PropertyDefineConstant.MODIFY_OK);
		m_priorityInfo.setModify(PropertyDefineConstant.MODIFY_OK);
		m_priorityUnknown.setModify(PropertyDefineConstant.MODIFY_OK);
		m_outputFromDate.setModify(PropertyDefineConstant.MODIFY_OK);
		m_outputToDate.setModify(PropertyDefineConstant.MODIFY_OK);
		m_generationFromDate.setModify(PropertyDefineConstant.MODIFY_OK);
		m_generationToDate.setModify(PropertyDefineConstant.MODIFY_OK);
		m_monitorId.setModify(PropertyDefineConstant.MODIFY_OK);
		m_monitorDetailId.setModify(PropertyDefineConstant.MODIFY_OK);
		m_facilityType.setModify(PropertyDefineConstant.MODIFY_OK);
		m_application.setModify(PropertyDefineConstant.MODIFY_OK);
		m_message.setModify(PropertyDefineConstant.MODIFY_OK);
		m_confirmed.setModify(PropertyDefineConstant.MODIFY_NG);
		m_confirmedUnconfirmed.setModify(PropertyDefineConstant.MODIFY_OK);
		m_confirmedConfirming.setModify(PropertyDefineConstant.MODIFY_OK);
		m_confirmedConfirmed.setModify(PropertyDefineConstant.MODIFY_OK);
		
		m_outputDate.setModify(PropertyDefineConstant.MODIFY_NG);
		m_generationDate.setModify(PropertyDefineConstant.MODIFY_NG);

		m_confirmedUser.setModify(PropertyDefineConstant.MODIFY_OK);

		m_comment.setModify(PropertyDefineConstant.MODIFY_OK);
		m_commentUser.setModify(PropertyDefineConstant.MODIFY_OK);

		m_ownerRoleId.setModify(PropertyDefineConstant.MODIFY_OK);
		
		m_collectGraphFlg.setModify(PropertyDefineConstant.MODIFY_OK);

		Property property = new Property(null, null, "");

		// 初期表示ツリーを構成。
		property.removeChildren();
		property.addChildren(m_allSearch);
		property.addChildren(m_priority);
		property.addChildren(m_outputDate);
		property.addChildren(m_generationDate);
		property.addChildren(m_monitorId);
		property.addChildren(m_monitorDetailId);
		property.addChildren(m_facilityType);
		property.addChildren(m_application);
		property.addChildren(m_message);		
		property.addChildren(m_confirmed);
		property.addChildren(m_confirmedUser);
		property.addChildren(m_comment);
		property.addChildren(m_commentUser);
		property.addChildren(m_collectGraphFlg);
		property.addChildren(m_ownerRoleId);

		// 重要度
		m_priority.removeChildren();
		m_priority.addChildren(m_priorityCritical);
		m_priority.addChildren(m_priorityWarning);
		m_priority.addChildren(m_priorityInfo);
		m_priority.addChildren(m_priorityUnknown);

		// 受信日時
		m_outputDate.removeChildren();
		m_outputDate.addChildren(m_outputFromDate);
		m_outputDate.addChildren(m_outputToDate);

		// 出力日時
		m_generationDate.removeChildren();
		m_generationDate.addChildren(m_generationFromDate);
		m_generationDate.addChildren(m_generationToDate);

		m_confirmed.removeChildren();
		m_confirmed.addChildren(m_confirmedUnconfirmed);
		m_confirmed.addChildren(m_confirmedConfirming);
		m_confirmed.addChildren(m_confirmedConfirmed);
		
		setCustomizeProperty(property, locale, eventDspSetting, managerName, null);
		
		return property;
	}
	
	/**
	 * カスタマイズ可能な項目をプロパティに追加する。
	 * 
	 */
	private static void setCustomizeProperty(Property parent, Locale locale, MultiManagerEventDisplaySettingInfo eventDspSetting, String managerName, Map<String, Property> cacheMap) {
		//ユーザ項目の設定
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			UserItemDisplayInfo userItemInfo = eventDspSetting.getUserItemDisplayInfo(managerName, i);
			if (!userItemInfo.getDisplayEnable()) {
				continue;
			}
			
			String id = EventInfoConstant.getUserItemConst(i);
			
			Property userItemProperty =  new Property (
					id, 
					EventHinemosPropertyUtil.getDisplayName(userItemInfo.getDisplayName(), i),
					PropertyDefineConstant.EDITOR_TEXT,
					DataRangeConstant.VARCHAR_128);
			
			userItemProperty.setModify(PropertyDefineConstant.MODIFY_OK);
			
			Object value = "";
			
			if (cacheMap != null && cacheMap.containsKey(id)) {
				value = cacheMap.get(id).getValue();
			}
			
			userItemProperty.setValue(value);
			
			parent.addChildren(userItemProperty);
			
		}
		
		//イベント情報の設定
		if (eventDspSetting.isEventNoDisplay(managerName)){ 
			Property eventNo =  
					new Property (EventInfoConstant.EVENT_NO, Messages.getString("monitor.eventno", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.TEXT);
			
			Property eventNoFrom =  
					new Property (EventInfoConstant.EVENT_NO_FROM, Messages.getString("start", locale), PropertyDefineConstant.EDITOR_NUM_LONG, DataRangeConstant.INTEGER_HIGH, 0);
						
			Property eventNoTo =  
					new Property (EventInfoConstant.EVENT_NO_TO, Messages.getString("end", locale), PropertyDefineConstant.EDITOR_NUM_LONG, DataRangeConstant.INTEGER_HIGH, 0);
			
			eventNo.setModify(PropertyDefineConstant.MODIFY_NG);
			eventNoFrom.setModify(PropertyDefineConstant.MODIFY_OK);
			eventNoTo.setModify(PropertyDefineConstant.MODIFY_OK);
			
			eventNo.setValue("");
			
			Object value = "";
			if (cacheMap != null && cacheMap.containsKey(EventInfoConstant.EVENT_NO_FROM)) {
				value = cacheMap.get(EventInfoConstant.EVENT_NO_FROM).getValue();
			}
			eventNoFrom.setValue(value);
			
			value = "";
			if (cacheMap != null && cacheMap.containsKey(EventInfoConstant.EVENT_NO_TO)) {
				value = cacheMap.get(EventInfoConstant.EVENT_NO_TO).getValue();
			}
			eventNoTo.setValue(value);
			
			parent.addChildren(eventNo);
			eventNo.removeChildren();
			eventNo.addChildren(eventNoFrom);
			eventNo.addChildren(eventNoTo);
		}
	}
	
	/**
	 * キャッシュされているプロパティを最新の設定内容で更新する
	 * 
	 */
	public static void updatePropertyDisp(Property property, Locale locale, MultiManagerEventDisplaySettingInfo eventDspSetting, String managerName) {
		
		//変更可能な項目をマップに退避し、対象のプロパティから削除する
		Map<String, Property> cacheMap = new HashMap<String, Property>();
		
		List<String> targetIdList = new ArrayList<String>();
		
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			targetIdList.add(EventInfoConstant.getUserItemConst(i));
		}
		
		targetIdList.add(EventInfoConstant.EVENT_NO_FROM);
		targetIdList.add(EventInfoConstant.EVENT_NO_TO);
		targetIdList.add(EventInfoConstant.EVENT_NO);
		
		for (String targetId : targetIdList) {
			setAndRemoveProperty(cacheMap, property, targetId);
		}
		
		setCustomizeProperty(property, locale, eventDspSetting, managerName, cacheMap);
	}
	
	private static void setAndRemoveProperty(Map<String, Property> cacheMap, Property parent, String targetId) {
		for (Object child : parent.getChildren()) {
			Property prop = (Property) child;
			if (targetId.equals(prop.getID())) {
				parent.removeChildren(prop);
				cacheMap.put(targetId, prop);
				break;
			}
			setAndRemoveProperty(cacheMap, prop, targetId);
		}

	}
}
