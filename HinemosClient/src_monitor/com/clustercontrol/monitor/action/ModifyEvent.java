/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.action;

import java.util.ArrayList;
import java.util.Date;

import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.EventLogInfoRequest;
import org.openapitools.client.model.EventLogInfoResponse;
import org.openapitools.client.model.ModifyEventInfoRequest;

import com.clustercontrol.bean.Property;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.bean.EventInfoConstant;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo.UserItemDisplayInfo;
import com.clustercontrol.monitor.util.EventUtil;
import com.clustercontrol.monitor.util.MonitorResultRestClientWrapper;
import com.clustercontrol.util.DateTimeStringConverter;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;

public class ModifyEvent {
	
	/**
	 * イベント情報のコメントを更新するクライアント側アクションクラス<BR>
	 *
	 * マネージャにSessionBean経由でアクセスし、イベント情報を更新します。
	 *
	 */
	// ----- コンストラクタ ----- //

	// ----- instance メソッド ----- //

	/**
	 * マネージャにSessionBean経由でアクセスし、引数で指定されたイベント情報を更新します。<BR>
	 *
	 * @param managerName マネージャ
	 * @param property イベント情報プロパティ
	 * @return 更新に成功した場合、</code> true </code>
	 *
	 */
	public boolean updateModify(String managerName, EventLogInfoResponse initEventInfo, Property property, MultiManagerEventDisplaySettingInfo eventDspSetting) {

		ArrayList<?> value = null;

		EventLogInfoRequest eventData = new EventLogInfoRequest();

		value = PropertyUtil.getPropertyValue(property,
				EventInfoConstant.MONITOR_ID);
		if (value.get(0) instanceof String) {
			eventData.setMonitorId((String) value.get(0));
		}

		value = PropertyUtil.getPropertyValue(property,
				EventInfoConstant.MONITOR_DETAIL_ID);
		if (value.get(0) instanceof String) {
			eventData.setMonitorDetailId((String) value.get(0));
		}

		value = PropertyUtil.getPropertyValue(property,
				EventInfoConstant.PLUGIN_ID);
		if (value.get(0) instanceof String) {
			eventData.setPluginId((String) value.get(0));
		}

		value = PropertyUtil.getPropertyValue(property,
				EventInfoConstant.FACILITY_ID);
		if (value.get(0) instanceof String) {
			eventData.setFacilityId((String) value.get(0));
		}

		value = PropertyUtil.getPropertyValue(property,
				EventInfoConstant.OUTPUT_DATE);
		if (value.get(0) instanceof Date) {
			Long outputFromDateLong = ((Date) value.get(0)).getTime();
			String outputDateString = DateTimeStringConverter.formatLongDate(outputFromDateLong,
					MonitorResultRestClientWrapper.DATETIME_FORMAT);
			eventData.setOutputDate(outputDateString);
		}

		value = PropertyUtil.getPropertyValue(property,
				EventInfoConstant.COMMENT);
		if (value.get(0) instanceof String) {
			eventData.setComment((String) value.get(0));
		}

		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			UserItemDisplayInfo userItemInfo = eventDspSetting.getUserItemDisplayInfo(managerName, i);

			if (!userItemInfo.getDisplayEnable() || !userItemInfo.isModifyClientEnable()) {
				//画面表示しない場合　または　変更できない場合
				continue;
			}
			
			//画面表示　かつ　変更可能な場合
			
			String valueStr = null;
			
			value = PropertyUtil.getPropertyValue(property, EventInfoConstant.getUserItemConst(i));
			
			if (value.get(0) instanceof String) {
				valueStr = (String)value.get(0);
			}
			
			if (isChangeValue(valueStr, EventUtil.getUserItemValue(initEventInfo, i))) {
				//画面表示　かつ　変更可能 　かつ　値が変更されている場合のみ、更新パラメータを設定
				EventUtil.setUserItemValue(eventData, i, valueStr);
			}
		}
		
		try {
			MonitorResultRestClientWrapper wrapper =MonitorResultRestClientWrapper.getWrapper(managerName);
			ModifyEventInfoRequest modifyEventInfoRequest = new ModifyEventInfoRequest();
			modifyEventInfoRequest.setInfo(eventData);
			wrapper.modifyEventInfo(modifyEventInfoRequest);

			return true;

		} catch (InvalidRole e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.monitor.58") + " " + HinemosMessage.replace(e.getMessage()));
		} 
		return false;
	}
	
	private static boolean isChangeValue(String inputValue, String initValue) {
		if (inputValue == null) {
			inputValue = "";
		}
		if (initValue == null) {
			initValue = "";
		}
		
		return !inputValue.equals(initValue);
	}
	

}
