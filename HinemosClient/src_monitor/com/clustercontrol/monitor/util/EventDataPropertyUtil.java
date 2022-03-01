/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.util;

import java.sql.Date;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.openapitools.client.model.EventLogInfoResponse;
import org.openapitools.client.model.EventLogOperationHistoryEntityResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.monitor.bean.CollectGraphFlgMessage;
import com.clustercontrol.monitor.bean.ConfirmMessage;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.bean.EventInfoConstant;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo.UserItemDisplayInfo;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

public class EventDataPropertyUtil {

	/**
	 * イベント情報DTOをプロパティに変換するメソッドです。<BR>
	 * <p>
	 * <ol>
	 *  <li>引数で指定された条件に一致するイベント情報を取得します。</li>
	 *  <li>フィルタ項目毎にID, 名前, 処理定数（{@link com.clustercontrol.bean.PropertyDefineConstant}）を指定し、
	 *      プロパティ（{@link com.clustercontrol.bean.Property}）を生成します。</li>
	 *  <li>各項目のプロパティに値を設定し、ツリー状に定義します。</li>
	 * </ol>
	 * 
	 * <p>プロパティに定義する項目は、下記の通りです。
	 * <p>
	 * <ul>
	 *  <li>プロパティ（親。ダミー）</li>
	 *  <ul>
	 *   <li>重要度（子。テキスト）</li>
	 *   <li>受信日時（子。テキスト）</li>
	 *   <li>出力日時（子。テキスト）</li>
	 *   <li>プラグインID（子。テキスト）</li>
	 *   <li>監視項目ID（子。テキスト）</li>
	 *   <li>ファシリティID（子。テキスト）</li>
	 *   <li>スコープ（子。テキスト）</li>
	 *   <li>アプリケーション（子。テキスト）</li>
	 *   <li>メッセージ（子。テキスト）</li>
	 *   <li>オリジナルメッセージ（子。テキストエリア）</li>
	 *   <li>確認（子。テキスト）</li>
	 *   <li>確認済み日時（子。テキスト）</li>
	 *   <li>確認ユーザ（子。テキスト）</li>
	 *   <li>重複カウンタ（子。テキスト）</li>
	 *   <li>コメント入力</li>
	 *   <li>コメント（子。テキスト）</li>
	 *   <li>コメント更新日時</li>
	 *   <li>コメント更新ユーザ</li>
	 *  </ul>
	 * </ul>
	 * 
	 * @param locale ロケール情報
	 * @return イベント詳細情報表示用プロパティ
	 * @throws ParseException 
	 * 
	 * @see com.clustercontrol.bean.Property
	 * @see com.clustercontrol.bean.PropertyDefineConstant
	 * @see com.clustercontrol.bean.PriorityConstant
	 * @see com.clustercontrol.bean.ConfirmConstant
	 */
	public static Property dto2property(EventLogInfoResponse info, Locale locale, MultiManagerEventDisplaySettingInfo eventDspSetting, String managerName) throws ParseException {
		//マネージャ
		Property m_managerName = 
				new Property(managerName, Messages.getString("string.manager", locale), PropertyDefineConstant.EDITOR_TEXT);
		//重要度
		Property m_priority =
				new Property(EventInfoConstant.PRIORITY, Messages.getString("priority", locale), PropertyDefineConstant.EDITOR_TEXT);
		//受信日時
		Property m_outputDate =
				new Property(EventInfoConstant.OUTPUT_DATE, Messages.getString("receive.time", locale), PropertyDefineConstant.EDITOR_DATETIME);
		//出力日時
		Property m_generationDate =
				new Property(EventInfoConstant.GENERATION_DATE, Messages.getString("output.time", locale), PropertyDefineConstant.EDITOR_DATETIME);
		//プラグインID
		Property m_pluginId =
				new Property(EventInfoConstant.PLUGIN_ID, Messages.getString("plugin.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		//監視項目ID
		Property m_monitorId =
				new Property(EventInfoConstant.MONITOR_ID, Messages.getString("monitor.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		//監視詳細
		Property m_monitorDetailId =
				new Property(EventInfoConstant.MONITOR_DETAIL_ID, Messages.getString("monitor.detail.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		//ファシリティID
		Property m_facilityId =
				new Property(EventInfoConstant.FACILITY_ID, Messages.getString("facility.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		//スコープ
		Property m_scopeText =
				new Property(EventInfoConstant.SCOPE_TEXT, Messages.getString("scope", locale), PropertyDefineConstant.EDITOR_FACILITY);
		//アプリケーション
		Property m_application =
				new Property(EventInfoConstant.APPLICATION, Messages.getString("application", locale), PropertyDefineConstant.EDITOR_TEXT);
		//メッセージ
		Property m_message =
				new Property(EventInfoConstant.MESSAGE, Messages.getString("message", locale), PropertyDefineConstant.EDITOR_TEXTAREA, DataRangeConstant.TEXT);
		//メッセージ(コード表記)
		Property m_messageCode =
				new Property(EventInfoConstant.MESSAGE, Messages.getString("message.code.disp", locale), PropertyDefineConstant.EDITOR_TEXTAREA, DataRangeConstant.TEXT);
		//オリジナルメッセージ
		Property m_messageOrg =
				new Property(EventInfoConstant.MESSAGE_ORG, Messages.getString("message.org", locale), PropertyDefineConstant.EDITOR_TEXTAREA, DataRangeConstant.TEXT);
		//確認
		Property m_confirmed =
				new Property(EventInfoConstant.CONFIRMED, Messages.getString("confirmed", locale), PropertyDefineConstant.EDITOR_TEXT);
		//確認日時
		Property m_confirmDate =
				new Property(EventInfoConstant.CONFIRM_DATE, Messages.getString("confirm.time", locale), PropertyDefineConstant.EDITOR_DATETIME);
		//確認ユーザ
		Property m_confirmUser =
				new Property(EventInfoConstant.CONFIRM_USER, Messages.getString("confirm.user", locale), PropertyDefineConstant.EDITOR_TEXT);
		//重複カウンタ
		Property m_duplicationCount =
				new Property(EventInfoConstant.DUPLICATION_COUNT, Messages.getString("duplication.count", locale), PropertyDefineConstant.EDITOR_NUM);
		//コメント入力
		//コメント
		Property m_comment =
				new Property (EventInfoConstant.COMMENT, Messages.getString("comment", locale), PropertyDefineConstant.EDITOR_TEXTAREA, DataRangeConstant.TEXT);
		//コメント更新日時
		Property m_commentDate =
				new Property (EventInfoConstant.COMMENT_DATE, Messages.getString("comment.date", locale), PropertyDefineConstant.EDITOR_DATETIME);
		//コメント更新ユーザ
		Property m_commentUser =
				new Property (EventInfoConstant.COMMENT_USER, Messages.getString("comment.user", locale), PropertyDefineConstant.EDITOR_TEXT);
		//性能グラフ用フラグ
		Property m_collectGraphFlg =
				new Property (EventInfoConstant.COLLECT_GRAPH_FLG, Messages.getString("collect.graph.flg", locale), PropertyDefineConstant.EDITOR_TEXT);
		//オーナーロールID
		Property m_ownerRoleId =
				new Property (EventInfoConstant.OWNER_ROLE_ID, Messages.getString("owner.role.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		//通知のUUID
		Property m_notifyUUID =
				new Property (EventInfoConstant.NOTIFY_UUID, Messages.getString("notify.uuid", locale), PropertyDefineConstant.EDITOR_TEXT);
		//イベント操作履歴
		Property m_eventHistory =
				new Property (EventInfoConstant.EVENT_HISTORY, Messages.getString("event.history", locale), PropertyDefineConstant.EDITOR_TEXTAREA, DataRangeConstant.TEXT);

		// 値を初期化
		m_managerName.setValue(managerName);
		m_priority.setValue(PriorityMessage.typeToString(info.getPriority().intValue()));

		if(info.getOutputDate() != null){
			Long m_outputDateLong = null;
				java.util.Date tmpDate = MonitorResultRestClientWrapper.parseDate(info.getOutputDate());
				m_outputDateLong = tmpDate.getTime();
			m_outputDate.setValue(new Date(m_outputDateLong));
		}
		if(info.getGenerationDate() != null){
			Long m_generationDateLong = null;
				java.util.Date tmpDate = MonitorResultRestClientWrapper.parseDate(info.getGenerationDate());
				m_generationDateLong = tmpDate.getTime();
			m_generationDate.setValue(new Date(m_generationDateLong));
		}
		
		m_pluginId.setValue(info.getPluginId());
		m_monitorId.setValue(info.getMonitorId());
		m_monitorDetailId.setValue(info.getMonitorDetailId());
		m_facilityId.setValue(info.getFacilityId());
		m_scopeText.setValue(HinemosMessage.replace(info.getScopeText()));
		m_application.setValue(HinemosMessage.replace(info.getApplication()));
		m_message.setValue(HinemosMessage.replace(info.getMessage()));
		m_messageCode.setValue(info.getMessage());
		m_messageOrg.setValue(nullToSpace(HinemosMessage.replace(info.getMessageOrg())));
		m_confirmed.setValue(ConfirmMessage.typeToString(info.getConfirmed().intValue()));
		if(info.getConfirmDate() != null){
			Long m_confirmDateLong = null;
				java.util.Date tmpDate = MonitorResultRestClientWrapper.parseDate(info.getConfirmDate());
				m_confirmDateLong = tmpDate.getTime();
			m_confirmDate.setValue(new Date(m_confirmDateLong));
		}

		m_confirmUser.setValue(info.getConfirmUser());
		m_duplicationCount.setValue(info.getDuplicationCount());

		m_comment.setValue(info.getComment());
		if (info.getCommentDate() != null) {
			Long m_commentDateLong = null;
				java.util.Date tmpDate = MonitorResultRestClientWrapper.parseDate(info.getCommentDate());
				m_commentDateLong = tmpDate.getTime();
			m_commentDate.setValue(new Date(m_commentDateLong));
		}
		m_commentUser.setValue(info.getCommentUser());
		m_collectGraphFlg.setValue(CollectGraphFlgMessage.typeToString(info.getCollectGraphFlg()));
		m_ownerRoleId.setValue(info.getOwnerRoleId());
		m_notifyUUID.setValue(info.getNotifyUUID());
		m_eventHistory.setValue(getEventHistoryString(info.getEventLogHitory()));

		//変更の可/不可を設定
		m_managerName.setModify(PropertyDefineConstant.MODIFY_NG);
		m_priority.setModify(PropertyDefineConstant.MODIFY_NG);
		m_outputDate.setModify(PropertyDefineConstant.MODIFY_NG);
		m_generationDate.setModify(PropertyDefineConstant.MODIFY_NG);
		m_pluginId.setModify(PropertyDefineConstant.MODIFY_NG);
		m_monitorId.setModify(PropertyDefineConstant.MODIFY_NG);
		m_monitorDetailId.setModify(PropertyDefineConstant.MODIFY_NG);
		m_facilityId.setModify(PropertyDefineConstant.MODIFY_NG);
		m_scopeText.setModify(PropertyDefineConstant.MODIFY_NG);
		m_application.setModify(PropertyDefineConstant.MODIFY_NG);
		m_message.setModify(PropertyDefineConstant.MODIFY_NG);
		m_messageCode.setModify(PropertyDefineConstant.MODIFY_NG);
		m_messageOrg.setModify(PropertyDefineConstant.MODIFY_NG);
		m_confirmed.setModify(PropertyDefineConstant.MODIFY_NG);
		m_confirmDate.setModify(PropertyDefineConstant.MODIFY_NG);
		m_confirmUser.setModify(PropertyDefineConstant.MODIFY_NG);
		m_duplicationCount.setModify(PropertyDefineConstant.MODIFY_NG);
		m_comment.setModify(PropertyDefineConstant.MODIFY_OK);
		m_commentDate.setModify(PropertyDefineConstant.MODIFY_NG);
		m_commentUser.setModify(PropertyDefineConstant.MODIFY_NG);
		m_collectGraphFlg.setModify(PropertyDefineConstant.MODIFY_NG);
		m_ownerRoleId.setModify(PropertyDefineConstant.MODIFY_NG);
		m_notifyUUID.setModify(PropertyDefineConstant.MODIFY_NG);
		m_eventHistory.setModify(PropertyDefineConstant.MODIFY_NG);

		Property property = new Property(null, null, "");

		// 初期表示ツリーを構成。
		property.removeChildren();
		property.addChildren(m_managerName);
		property.addChildren(m_priority);
		property.addChildren(m_outputDate);
		property.addChildren(m_generationDate);
		property.addChildren(m_pluginId);
		property.addChildren(m_monitorId);
		property.addChildren(m_monitorDetailId);
		property.addChildren(m_facilityId);
		property.addChildren(m_scopeText);
		property.addChildren(m_application);
		property.addChildren(m_message);
		property.addChildren(m_messageCode);
		property.addChildren(m_messageOrg);
		property.addChildren(m_confirmed);
		property.addChildren(m_confirmDate);
		property.addChildren(m_confirmUser);
		property.addChildren(m_duplicationCount);
		property.addChildren(m_comment);
		property.addChildren(m_commentDate);
		property.addChildren(m_commentUser);
		property.addChildren(m_collectGraphFlg);
		property.addChildren(m_ownerRoleId);
		property.addChildren(m_notifyUUID);
		property.addChildren(m_eventHistory);

		//ユーザ項目の設定
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			UserItemDisplayInfo userItemInfo = eventDspSetting.getUserItemDisplayInfo(managerName, i);
			if (userItemInfo.getDisplayEnable()) {
				Property userItemProperty =  new Property (
						EventInfoConstant.getUserItemConst(i), 
						EventHinemosPropertyUtil.getDisplayName(userItemInfo.getDisplayName(), i),
						PropertyDefineConstant.EDITOR_TEXT,
						DataRangeConstant.VARCHAR_4096);
				
				//値のセット
				userItemProperty.setValue(EventUtil.getUserItemValue(info, i));
				
				//入力可否の変更
				if (userItemInfo.isModifyClientEnable()) {
					userItemProperty.setModify(PropertyDefineConstant.MODIFY_OK);
				} else {
					userItemProperty.setModify(PropertyDefineConstant.MODIFY_NG);
				}
				property.addChildren(userItemProperty);
			}
		}
		//イベント情報の設定
		if (eventDspSetting.isEventNoDisplay(managerName)){ 
			Property eventNo =  
					new Property (EventInfoConstant.EVENT_NO, Messages.getString("monitor.eventno", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.TEXT);
			eventNo.setModify(PropertyDefineConstant.MODIFY_NG);
			
			//値のセット
			eventNo.setValue(info.getPosition());
			
			//入力可否の変更
			eventNo.setModify(PropertyDefineConstant.MODIFY_NG);
			
			property.addChildren(eventNo);
		}
		
		
		
		return property;
	}
	
	/**
	 * Nullを空文字へ変換
	 * 
	 * @param target
	 * @return
	 */
	private static String nullToSpace(String target){

		if(target == null){
			return "";
		}
		return target;
	}

	/**
	 * イベント操作履歴の文字列を取得
	 * 
	 * @param list
	 * @return String
	 */
	private static String getEventHistoryString(List<EventLogOperationHistoryEntityResponse> list){

		StringBuilder sb = new StringBuilder();
		
		if (list == null || list.size() == 0){
			return "";
		}
		
		for (EventLogOperationHistoryEntityResponse history : list) {
			String date = history.getOperationDate().split(Pattern.quote("."))[0];
			String user = history.getOperationUser();
			String detail = HinemosMessage.replace(history.getDetail());
			
			sb.append(String.format("%s [%s] %s", date, user, detail));
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}
}
