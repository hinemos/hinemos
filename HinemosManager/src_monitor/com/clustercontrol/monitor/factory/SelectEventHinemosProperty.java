/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.factory;

import java.util.HashMap;
import java.util.Map;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.monitor.bean.EventCustomCommandInfo;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.bean.EventNoDisplayInfo;
import com.clustercontrol.monitor.bean.EventUserExtensionItemInfo;
import com.clustercontrol.platform.HinemosPropertyDefault;


/**
 * イベントに関するHinemosプロパティを検索するクラス<BR>
 *
 */
public class SelectEventHinemosProperty {
	
	
	/** ユーザ拡張イベント項目のHinmeosプロパティでのフォーマット */
	private static final String USER_ITEM_ZEROPAD_NUM = "%02d";

	/** ユーザ拡張イベント項目のHinmeosプロパティでのフォーマット */
	private static final String COMMAND_ZEROPAD_NUM = "%01d";
	
	public static Map<Integer, EventUserExtensionItemInfo> getEventUserExtensionItemInfo() {
		Map<Integer, EventUserExtensionItemInfo> ret = new HashMap<>();
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			String indexStr = String.format(SelectEventHinemosProperty.USER_ITEM_ZEROPAD_NUM, i);
			EventUserExtensionItemInfo info = new EventUserExtensionItemInfo();
			info.setDisplayEnable(HinemosPropertyCommon.monitor_event_useritem_item$_display_enable.getBooleanValue(indexStr));
			info.setExportEnable(HinemosPropertyCommon.monitor_event_useritem_item$_export_enable.getBooleanValue(indexStr));
			info.setDisplayName(HinemosPropertyCommon.monitor_event_useritem_item$_displayname.getStringValue(indexStr));
			info.setRegistInitValue(HinemosPropertyCommon.monitor_event_useritem_item$_regist_initvalue.getStringValue(indexStr));
			info.setModifyClientEnable(HinemosPropertyCommon.monitor_event_useritem_item$_modify_client_enable.getBooleanValue(indexStr));
			info.setModifyRequired(HinemosPropertyCommon.monitor_event_useritem_item$_modify_required.getBooleanValue(indexStr));
			info.setModifyValidation(HinemosPropertyCommon.monitor_event_useritem_item$_modify_validation.getStringValue(indexStr));
			info.setModifyFormat(HinemosPropertyCommon.monitor_event_useritem_item$_modify_format.getStringValue(indexStr));
			ret.put(i, info);
		}
		return ret;
	}
	
	/**
	 * Hinemosプロパティをイベントカスタムコマンド設定のオブジェクトに変換
	 * 
	 * @return イベントカスタムコマンド設定のマップ（key：コマンドNo、value:イベントカスタムコマンド設定）
	 */
	public static Map<Integer, EventCustomCommandInfo> getEventCustomCommandInfo() {
		Map<Integer, EventCustomCommandInfo> ret = new HashMap<>();
		for (int i = 1; i <= EventHinemosPropertyConstant.COMMAND_SIZE; i++) {
			String indexStr = String.format(SelectEventHinemosProperty.COMMAND_ZEROPAD_NUM, i);
			EventCustomCommandInfo info = new EventCustomCommandInfo();
			info.setEncode(HinemosPropertyDefault.monitor_event_customcmd_common_stdout_encode.getStringValue());
			info.setEnable(HinemosPropertyCommon.monitor_event_customcmd_cmd$_enable.getBooleanValue(indexStr));
			info.setDisplayName(HinemosPropertyCommon.monitor_event_customcmd_cmd$_displayname.getStringValue(indexStr));
			info.setDescription(HinemosPropertyCommon.monitor_event_customcmd_cmd$_description.getStringValue(indexStr));
			info.setCommand(HinemosPropertyCommon.monitor_event_customcmd_cmd$_command.getStringValue(indexStr));
			info.setWarnRc(HinemosPropertyCommon.monitor_event_customcmd_cmd$_warnrc.getNumericValue(indexStr));
			info.setErrorRc(HinemosPropertyCommon.monitor_event_customcmd_cmd$_errorrc.getNumericValue(indexStr));
			info.setMaxEventSize(HinemosPropertyCommon.monitor_event_customcmd_cmd$_max_eventsize.getNumericValue(indexStr));
			info.setThread(HinemosPropertyCommon.monitor_event_customcmd_cmd$_thread.getNumericValue(indexStr));
			info.setQueue(HinemosPropertyCommon.monitor_event_customcmd_cmd$_queue.getNumericValue(indexStr));
			info.setDateFormat(HinemosPropertyCommon.monitor_event_customcmd_cmd$_date_format.getStringValue(indexStr));
			info.setUser(HinemosPropertyCommon.monitor_event_customcmd_cmd$_user.getStringValue(indexStr));
			info.setTimeout(HinemosPropertyCommon.monitor_event_customcmd_cmd$_timeout.getNumericValue(indexStr));
			info.setBuffer(HinemosPropertyCommon.monitor_event_customcmd_cmd$_buffer.getNumericValue(indexStr));
			info.setMode(HinemosPropertyCommon.monitor_event_customcmd_cmd$_mode.getStringValue(indexStr));
			info.setLogin(HinemosPropertyCommon.monitor_event_customcmd_cmd$_login.getBooleanValue(indexStr));
			info.setResultPollingKeeptime(HinemosPropertyCommon.monitor_event_customcmd_cmd$_result_keeptime.getNumericValue(indexStr));
			info.setResultPollingDelay(HinemosPropertyCommon.monitor_event_customcmd_cmd$_result_polling_delay.getNumericValue(indexStr));
			info.setResultPollingInterval(HinemosPropertyCommon.monitor_event_customcmd_cmd$_result_polling_interval.getNumericValue(indexStr));
			
			ret.put(i, info);
			
		}
		return ret;
	}
	
	/**
	 * Hinemosプロパティをイベント番号表示設定のオブジェクトに変換
	 * 
	 * @return イベント番号表示設定
	 */
	public static EventNoDisplayInfo geEventNoDisplayInfo() {
		
		EventNoDisplayInfo info = new EventNoDisplayInfo();
		info.setDisplayEnable(HinemosPropertyCommon.monitor_event_eventno_display_enable.getBooleanValue());
		info.setExportEnable(HinemosPropertyCommon.monitor_event_eventno_export_enable.getBooleanValue());
			
		return info;
	}
}
