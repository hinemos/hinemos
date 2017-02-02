/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.monitor.util;

import java.sql.Date;
import java.util.Locale;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.monitor.bean.CollectGraphFlgMessage;
import com.clustercontrol.monitor.bean.ConfirmMessage;
import com.clustercontrol.monitor.bean.EventInfoConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.monitor.EventDataInfo;

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
	 * 
	 * @see com.clustercontrol.bean.Property
	 * @see com.clustercontrol.bean.PropertyDefineConstant
	 * @see com.clustercontrol.bean.PriorityConstant
	 * @see com.clustercontrol.bean.ConfirmConstant
	 */
	public static Property dto2property(EventDataInfo info, Locale locale) {

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
		//確認済み日時
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


		// 値を初期化
		m_priority.setValue(PriorityMessage.typeToString(info.getPriority().intValue()));

		if(info.getOutputDate() != null){
			m_outputDate.setValue(new Date(info.getOutputDate()));
		}
		if(info.getGenerationDate() != null){
			m_generationDate.setValue(new Date(info.getGenerationDate()));
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
			m_confirmDate.setValue(new Date(info.getConfirmDate()));
		}

		m_confirmUser.setValue(info.getConfirmUser());
		m_duplicationCount.setValue(info.getDuplicationCount());

		m_comment.setValue(info.getComment());
		if (info.getCommentDate() != null) {
			m_commentDate.setValue(new Date(info.getCommentDate()));
		}
		m_commentUser.setValue(info.getCommentUser());
		m_collectGraphFlg.setValue(CollectGraphFlgMessage.typeToString(info.isCollectGraphFlg()));
		m_ownerRoleId.setValue(info.getOwnerRoleId());

		//変更の可/不可を設定
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

		Property property = new Property(null, null, "");

		// 初期表示ツリーを構成。
		property.removeChildren();
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
}
