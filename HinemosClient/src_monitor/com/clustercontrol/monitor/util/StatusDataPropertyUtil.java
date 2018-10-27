/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.util;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.monitor.action.GetStatusListTableDefine;
import com.clustercontrol.monitor.bean.StatusInfoConstant;
import com.clustercontrol.util.Messages;

public class StatusDataPropertyUtil {

	/**
	 * ステータス情報DTOをプロパティに変換するメソッドです。<BR>
	 * <p>
	 * <ol>
	 *  <li>引数で指定された条件に一致するステータス情報を取得します。</li>
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
	 *   <li>マネージャ（子。テキスト）</li>
	 *   <li>重要度（子。テキスト）</li>
	 *   <li>プラグインID（子。テキスト）</li>
	 *   <li>監視項目ID（子。テキスト）</li>
	 *   <li>監視詳細（子。テキスト）</li>
	 *   <li>ファシリティID（子。テキスト）</li>
	 *   <li>スコープ（子。テキスト）</li>
	 *   <li>アプリケーション（子。テキスト）</li>
	 *   <li>最終変更日時（子。テキスト）</li>
	 *   <li>出力日時（子。テキスト）</li>
	 *   <li>メッセージ（子。テキスト）</li>
	 *   <li>オーナーロールID</li>
	 *  </ul>
	 * </ul>
	 *
	 * @param locale ロケール情報
	 * @return ステータス詳細情報表示用プロパティ
	 *
	 * @see com.clustercontrol.bean.Property
	 * @see com.clustercontrol.bean.PropertyDefineConstant
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public static Property dto2property(List<?> list, Locale locale) {

		String managerName = (String)list.get(GetStatusListTableDefine.MANAGER_NAME);
		Integer priority = (Integer)list.get(GetStatusListTableDefine.PRIORITY);
		String pluginId = (String) list.get(GetStatusListTableDefine.PLUGIN_ID);
		String monitorId = (String) list.get(GetStatusListTableDefine.MONITOR_ID);
		String monitorDetailId = (String) list.get(GetStatusListTableDefine.MONITOR_DETAIL_ID);
		String facilityId = (String) list.get(GetStatusListTableDefine.FACILITY_ID);
		String scope = (String) list.get(GetStatusListTableDefine.SCOPE);
		String application = (String) list.get(GetStatusListTableDefine.APPLICATION);
		Date updateTime = (Date) list.get(GetStatusListTableDefine.UPDATE_TIME);
		Date outputTime = (Date) list.get(GetStatusListTableDefine.OUTPUT_TIME);
		String message = (String) list.get(GetStatusListTableDefine.MESSAGE);
		String ownerRole = (String) list.get(GetStatusListTableDefine.OWNER_ROLE);

		//マネージャ名
		Property m_managerName =
				new Property(StatusInfoConstant.MANAGER_NAME, Messages.getString("facility.manager", locale), PropertyDefineConstant.EDITOR_TEXT);
		//重要度
		Property m_priority =
				new Property(StatusInfoConstant.PRIORITY, Messages.getString("priority", locale), PropertyDefineConstant.EDITOR_TEXT);
		//プラグインID
		Property m_pluginId =
				new Property(StatusInfoConstant.PLUGIN_ID, Messages.getString("plugin.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		//監視項目ID
		Property m_monitorId =
				new Property(StatusInfoConstant.MONITOR_ID, Messages.getString("monitor.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		//監視詳細
		Property m_monitorDetailId =
				new Property(StatusInfoConstant.MONITOR_DETAIL_ID, Messages.getString("monitor.detail.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		//ファシリティID
		Property m_facilityId =
				new Property(StatusInfoConstant.FACILITY_ID, Messages.getString("facility.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		//スコープ
		Property m_scopeText =
				new Property(StatusInfoConstant.SCOPE_TEXT, Messages.getString("scope", locale), PropertyDefineConstant.EDITOR_FACILITY);
		//アプリケーション
		Property m_application =
				new Property(StatusInfoConstant.APPLICATION, Messages.getString("application", locale), PropertyDefineConstant.EDITOR_TEXT);
		//変更日時
		Property m_updateTime =
				new Property(StatusInfoConstant.UPDATE_TIME, Messages.getString("update.time", locale), PropertyDefineConstant.EDITOR_DATETIME);
		//出力日時
		Property m_outputTime =
				new Property(StatusInfoConstant.OUTPUT_TIME, Messages.getString("generation.time", locale), PropertyDefineConstant.EDITOR_DATETIME);
		//メッセージ
		Property m_message =
				new Property(StatusInfoConstant.MESSAGE, Messages.getString("message", locale), PropertyDefineConstant.EDITOR_TEXTAREA, DataRangeConstant.TEXT);
		//オーナーロールID
		Property m_ownerRoleId =
				new Property (StatusInfoConstant.OWNER_ROLE_ID, Messages.getString("owner.role.id", locale), PropertyDefineConstant.EDITOR_TEXT);

		// 値を初期化
		m_managerName.setValue(managerName);
		m_priority.setValue(PriorityMessage.typeToString(priority));
		m_pluginId.setValue(pluginId);
		m_monitorId.setValue(monitorId);
		m_monitorDetailId.setValue(monitorDetailId);
		m_facilityId.setValue(facilityId);
		m_scopeText.setValue(scope);
		m_application.setValue(application);
		m_updateTime.setValue(updateTime);
		m_outputTime.setValue(outputTime);
		m_message.setValue(message);
		m_ownerRoleId.setValue(ownerRole);

		//変更の可/不可を設定
		m_managerName.setModify(PropertyDefineConstant.MODIFY_NG);
		m_priority.setModify(PropertyDefineConstant.MODIFY_NG);
		m_pluginId.setModify(PropertyDefineConstant.MODIFY_NG);
		m_monitorId.setModify(PropertyDefineConstant.MODIFY_NG);
		m_monitorDetailId.setModify(PropertyDefineConstant.MODIFY_NG);
		m_facilityId.setModify(PropertyDefineConstant.MODIFY_NG);
		m_scopeText.setModify(PropertyDefineConstant.MODIFY_NG);
		m_application.setModify(PropertyDefineConstant.MODIFY_NG);
		m_updateTime.setModify(PropertyDefineConstant.MODIFY_NG);
		m_outputTime.setModify(PropertyDefineConstant.MODIFY_NG);
		m_message.setModify(PropertyDefineConstant.MODIFY_NG);
		m_ownerRoleId.setModify(PropertyDefineConstant.MODIFY_NG);

		Property property = new Property(null, null, "");

		// 初期表示ツリーを構成。
		property.removeChildren();
		property.addChildren(m_managerName);
		property.addChildren(m_priority);
		property.addChildren(m_pluginId);
		property.addChildren(m_monitorId);
		property.addChildren(m_monitorDetailId);
		property.addChildren(m_facilityId);
		property.addChildren(m_scopeText);
		property.addChildren(m_application);
		property.addChildren(m_updateTime);
		property.addChildren(m_outputTime);
		property.addChildren(m_message);
		property.addChildren(m_ownerRoleId);
		return property;
	}
}
