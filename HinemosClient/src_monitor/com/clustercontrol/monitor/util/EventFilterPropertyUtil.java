/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.bean.EventInfoConstant;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo.UserItemDisplayInfo;
import com.clustercontrol.util.Messages;

public class EventFilterPropertyUtil {

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
					DataRangeConstant.VARCHAR_4096);
			
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
					new Property (EventInfoConstant.EVENT_NO_FROM, Messages.getString("start", locale), PropertyDefineConstant.EDITOR_NUM_LONG, DataRangeConstant.LONG_HIGH, 0);
			
			Property eventNoTo =  
					new Property (EventInfoConstant.EVENT_NO_TO, Messages.getString("end", locale), PropertyDefineConstant.EDITOR_NUM_LONG, DataRangeConstant.LONG_HIGH, 0);
			
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
		targetIdList.add(EventInfoConstant.NOTIFY_UUID);
		
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
