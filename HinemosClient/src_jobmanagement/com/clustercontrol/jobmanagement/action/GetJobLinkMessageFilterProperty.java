/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.action;

import java.util.Locale;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageFilterPropertyConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;

/**
 * ジョブ履歴[受信ジョブ連携メッセージ一覧]ビューのフィルタ用プロパティを取得するクライアント側アクションクラス<BR>
 *
 * マネージャにSessionBean経由でアクセスし、フィルタ用プロパティを取得する
 *
 */
public class GetJobLinkMessageFilterProperty {

	/**
	 * マネージャにSessionBean経由でアクセスし、フィルタ用プロパティを取得する
	 *
	 * @return フィルタ用プロパティ
	 *
	 */
	public Property getProperty() {

		Locale locale = Locale.getDefault();

		// マネージャ
		Property m_manager = new Property(JobLinkMessageFilterPropertyConstant.MANAGER,
				Messages.getString("facility.manager", locale), PropertyDefineConstant.EDITOR_SELECT);
		// ジョブ連携メッセージID
		Property m_joblinkMessageId = new Property(JobLinkMessageFilterPropertyConstant.JOBLINK_MESSAGE_ID,
				Messages.getString("joblink.message.id", locale), PropertyDefineConstant.EDITOR_TEXT,
				DataRangeConstant.VARCHAR_64);
		// 送信元ファシリティID
		Property m_srcFacilityId = new Property(JobLinkMessageFilterPropertyConstant.SRC_FACILITY_ID,
				Messages.getString("source.facility.id", locale), PropertyDefineConstant.EDITOR_FACILITY);
		// 送信元ファシリティ名
		Property m_srcFacilityName = new Property(JobLinkMessageFilterPropertyConstant.SRC_FACILITY_NAME,
				Messages.getString("source.scope", locale), PropertyDefineConstant.EDITOR_TEXT,
				DataRangeConstant.VARCHAR_64);
		// 監視詳細
		Property m_monitorDetailId = new Property(JobLinkMessageFilterPropertyConstant.MONITOR_DETAIL_ID,
				Messages.getString("monitor.detail.id", locale), PropertyDefineConstant.EDITOR_TEXT,
				DataRangeConstant.VARCHAR_64);
		// アプリケーション
		Property m_application = new Property(JobLinkMessageFilterPropertyConstant.APPLICATION,
				Messages.getString("application", locale), PropertyDefineConstant.EDITOR_TEXT,
				DataRangeConstant.VARCHAR_64);
		// 重要度
		Property m_priority = new Property(JobLinkMessageFilterPropertyConstant.PRIORITY,
				Messages.getString("priority", locale), PropertyDefineConstant.EDITOR_TEXT);
		// 重要度（危険）
		Property m_priorityCritical = new Property(JobLinkMessageFilterPropertyConstant.PRIORITY_CRITICAL,
				Messages.getString("critical", locale), PropertyDefineConstant.EDITOR_BOOL);
		// 重要度(警告)
		Property m_priorityWarning = new Property(JobLinkMessageFilterPropertyConstant.PRIORITY_WARNING,
				Messages.getString("warning", locale), PropertyDefineConstant.EDITOR_BOOL);
		// 重要度(情報)
		Property m_priorityInfo = new Property(JobLinkMessageFilterPropertyConstant.PRIORITY_INFO,
				Messages.getString("info", locale), PropertyDefineConstant.EDITOR_BOOL);
		// 重要度(不明)
		Property m_priorityUnknown = new Property(JobLinkMessageFilterPropertyConstant.PRIORITY_UNKNOWN,
				Messages.getString("unknown", locale), PropertyDefineConstant.EDITOR_BOOL);
		// メッセージ
		Property m_message = new Property(JobLinkMessageFilterPropertyConstant.MESSAGE,
				Messages.getString("message", locale), PropertyDefineConstant.EDITOR_TEXT,
				DataRangeConstant.VARCHAR_64);
		// 送信日時
		Property m_sendDate = new Property(JobLinkMessageFilterPropertyConstant.SEND_DATE,
				Messages.getString("send.time", locale), PropertyDefineConstant.EDITOR_TEXT);
		// 送信日時（From）
		Property m_sendFromDate = new Property(JobLinkMessageFilterPropertyConstant.SEND_FROM_DATE,
				Messages.getString("start", locale), PropertyDefineConstant.EDITOR_DATETIME);
		// 送信日時（To）
		Property m_sendToDate = new Property(JobLinkMessageFilterPropertyConstant.SEND_TO_DATE,
				Messages.getString("end", locale), PropertyDefineConstant.EDITOR_DATETIME);
		// 受信日時
		Property m_acceptDate = new Property(JobLinkMessageFilterPropertyConstant.ACCEPT_DATE,
				Messages.getString("receive.time", locale), PropertyDefineConstant.EDITOR_TEXT);
		// 受信日時（From）
		Property m_acceptFromDate = new Property(JobLinkMessageFilterPropertyConstant.ACCEPT_FROM_DATE,
				Messages.getString("start", locale), PropertyDefineConstant.EDITOR_DATETIME);
		// 受信日時（To）
		Property m_acceptToDate = new Property(JobLinkMessageFilterPropertyConstant.ACCEPT_TO_DATE,
				Messages.getString("end", locale), PropertyDefineConstant.EDITOR_DATETIME);

		Object[] obj = RestConnectManager.getActiveManagerSet().toArray();
		Object[] val = new Object[obj.length + 1];
		val[0] = "";
		for (int i = 0; i < obj.length; i++) {
			val[i + 1] = obj[i];
		}

		Object[][] managerValues = { val, val };
		m_manager.setSelectValues(managerValues);
		m_manager.setValue("");
		m_joblinkMessageId.setValue("");
		m_srcFacilityId.setValue("");
		m_srcFacilityName.setValue("");
		m_monitorDetailId.setValue("");
		m_application.setValue("");
		m_priority.setValue("");
		m_priorityCritical.setValue(true);
		m_priorityWarning.setValue(true);
		m_priorityInfo.setValue(true);
		m_priorityUnknown.setValue(true);
		m_message.setValue("");
		m_sendDate.setValue("");
		m_sendFromDate.setValue("");
		m_sendToDate.setValue("");
		m_acceptDate.setValue("");
		m_acceptFromDate.setValue("");
		m_acceptToDate.setValue("");

		// 変更の可/不可を設定
		m_manager.setModify(PropertyDefineConstant.MODIFY_OK);
		m_joblinkMessageId.setModify(PropertyDefineConstant.MODIFY_OK);
		m_srcFacilityId.setModify(PropertyDefineConstant.MODIFY_OK);
		m_srcFacilityName.setModify(PropertyDefineConstant.MODIFY_OK);
		m_monitorDetailId.setModify(PropertyDefineConstant.MODIFY_OK);
		m_application.setModify(PropertyDefineConstant.MODIFY_OK);
		m_priority.setModify(PropertyDefineConstant.MODIFY_NG);
		m_priorityCritical.setModify(PropertyDefineConstant.MODIFY_OK);
		m_priorityWarning.setModify(PropertyDefineConstant.MODIFY_OK);
		m_priorityInfo.setModify(PropertyDefineConstant.MODIFY_OK);
		m_priorityUnknown.setModify(PropertyDefineConstant.MODIFY_OK);
		m_message.setModify(PropertyDefineConstant.MODIFY_OK);
		m_sendDate.setModify(PropertyDefineConstant.MODIFY_NG);
		m_sendFromDate.setModify(PropertyDefineConstant.MODIFY_OK);
		m_sendToDate.setModify(PropertyDefineConstant.MODIFY_OK);
		m_acceptDate.setModify(PropertyDefineConstant.MODIFY_NG);
		m_acceptFromDate.setModify(PropertyDefineConstant.MODIFY_OK);
		m_acceptToDate.setModify(PropertyDefineConstant.MODIFY_OK);

		Property property = new Property(null, null, "");

		// 初期表示ツリーを構成。
		property.removeChildren();
		property.addChildren(m_manager);
		property.addChildren(m_joblinkMessageId);
		property.addChildren(m_srcFacilityId);
		property.addChildren(m_srcFacilityName);
		property.addChildren(m_monitorDetailId);
		property.addChildren(m_application);
		property.addChildren(m_priority);
		property.addChildren(m_message);
		property.addChildren(m_sendDate);
		property.addChildren(m_acceptDate);

		// 重要度
		m_priority.removeChildren();
		m_priority.addChildren(m_priorityCritical);
		m_priority.addChildren(m_priorityWarning);
		m_priority.addChildren(m_priorityInfo);
		m_priority.addChildren(m_priorityUnknown);

		// 送信日時
		m_sendDate.removeChildren();
		m_sendDate.addChildren(m_sendFromDate);
		m_sendDate.addChildren(m_sendToDate);

		// 受信日時
		m_acceptDate.removeChildren();
		m_acceptDate.addChildren(m_acceptFromDate);
		m_acceptDate.addChildren(m_acceptToDate);

		return property;
	}
}
