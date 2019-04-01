/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.bean;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.util.EventHinemosPropertyUtil;
import com.clustercontrol.ws.monitor.EventDisplaySettingInfo;
import com.clustercontrol.ws.monitor.EventNoDisplayInfo;
import com.clustercontrol.ws.monitor.EventUserExtensionItemInfo;
import com.clustercontrol.ws.monitor.EventDisplaySettingInfo.UserItemInfoMap.Entry;

/**
 * イベント表示設定情報（マルチマネージャ対応）
 * 
 */
public class MultiManagerEventDisplaySettingInfo {
	
	private Map<String, EventNoDisplayInfo> eventNoInfoMap = null;
	private Map<String, Map<Integer, EventUserExtensionItemInfo>> userItemInfoMap = null;
	
	public MultiManagerEventDisplaySettingInfo() {
		//マネージャの順序を保つため、LinkedHashMapを使用する
		this.eventNoInfoMap = new LinkedHashMap<>();
		this.userItemInfoMap = new LinkedHashMap<>();
	}
	
	public void addDisplayInfo(
			String managerName,
			EventDisplaySettingInfo eventDisplaySettingInfo) {
		this.eventNoInfoMap.put(managerName, eventDisplaySettingInfo.getEventNoInfo());
		
		Map<Integer, EventUserExtensionItemInfo> userItemMap = new HashMap<>();
		for (Entry entry : eventDisplaySettingInfo.getUserItemInfoMap().getEntry()) {
			userItemMap.put(entry.getKey(), entry.getValue());
		}
		
		this.userItemInfoMap.put(managerName, userItemMap);
	}
	
	public boolean isEventNoDisplay(String managerName) {
		if (managerName == null) {
			return isEventNoDisplayImpl();
		} else {
			return isEventNoDisplayImpl(managerName);
		}
		
	}
	
	public boolean isEventNoDisplay() {
		return isEventNoDisplay(null);
	}
	
	private boolean isEventNoDisplayImpl() {
		for (EventNoDisplayInfo info : eventNoInfoMap.values()) {
			if (info.isDisplayEnable() != null 
					&& info.isDisplayEnable().booleanValue()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isEventNoDisplayImpl(String managerName) {
		EventNoDisplayInfo info = eventNoInfoMap.get(managerName);
		Boolean retVal = null;
		if (info != null) {
			retVal = info.isDisplayEnable();
		}
		if (retVal == null) {
			return false;
		}
		return retVal;
	}
	
	public UserItemDisplayInfo getUserItemDisplayInfo(String managerName, int index) {
		if (managerName == null) {
			return getUserItemDisplayInfoImpl(index);
		} else {
			return getUserItemDisplayInfoImpl(managerName, index);
		}
	}
	
	public UserItemDisplayInfo getUserItemDisplayInfo(int index) {
		return getUserItemDisplayInfo(null, index);
	}
	
	public boolean hasHasMultiDisplayName(String mangaerName) {
		if (mangaerName != null) {
			//マネージャが特定されている場合、同じユーザ項目で別名となることはないため、false
			return false;
		}
		for (int i = 0; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			UserItemDisplayInfo dspInfo = getUserItemDisplayInfoImpl(i);
			if (dspInfo.getHasMultiDisplayName()) {
				return true;
			}
		}
		return false;
	}
	
	private UserItemDisplayInfo getUserItemDisplayInfoImpl(int index) {
		final String multiDispName = "*";
		
		UserItemDisplayInfo dspInfo = new UserItemDisplayInfo();
		dspInfo.setDisplayEnable(false);
		dspInfo.setDisplayName("");
		dspInfo.setHasMultiDisplayName(false);
		dspInfo.setToolTipName("");
		dspInfo.setModifyClientEnable(false);
		StringJoiner toolTipName = new StringJoiner("\n");
		
		for (java.util.Map.Entry<String, Map<Integer, EventUserExtensionItemInfo>> userItemInfos : userItemInfoMap.entrySet()) {
			Boolean isDisplay = null;
			String displayName = null;
			EventUserExtensionItemInfo userItemInfo = userItemInfos.getValue().get(index);
			
			if (userItemInfo != null) {
				isDisplay = userItemInfo.isDisplayEnable();
				displayName = EventHinemosPropertyUtil.getDisplayName(
						userItemInfo.getDisplayName(), index);
			}
			
			if (isDisplay != null && isDisplay.booleanValue()) {
				//表示の場合
				
				//ツールチップにマネージャごとの表示名をセット
				//（ツールチップに表示するかは別の表示名がでてきたかで判断）
				toolTipName.add(String.format("%s : %s", userItemInfos.getKey(), displayName));
				
				if (!"".equals(dspInfo.getDisplayName()) 
						&& !displayName.equals(dspInfo.getDisplayName())) {
					//すでに別の表示名がセットされている時、
					//*を表示し、複数表示名のフラグをセット
					dspInfo.setDisplayName(multiDispName);
					dspInfo.setHasMultiDisplayName(true);
				} else {
					//上記以外の場合は表示名を単純セット
					dspInfo.setDisplayEnable(true);
					dspInfo.setDisplayName(displayName);
				}
			}
		}
		if (dspInfo.getHasMultiDisplayName()) {
			//複数表示名がある場合はツールチップの表示内容をセット
			dspInfo.setToolTipName(toolTipName.toString());
		}
		
		return dspInfo;
	}
	
	private UserItemDisplayInfo getUserItemDisplayInfoImpl(String managerName, int index) {
		Boolean isDisplay = null;
		Boolean isModifyClientEnable = null;
		String displayName = null;
		EventUserExtensionItemInfo userItemInfo = null;
		
		if (userItemInfoMap.get(managerName) != null) {
			userItemInfo = userItemInfoMap.get(managerName).get(index);
		}
		if (userItemInfo != null) {
			isDisplay = userItemInfo.isDisplayEnable();
			isModifyClientEnable = userItemInfo.isModifyClientEnable();
			displayName = EventHinemosPropertyUtil.getDisplayName(userItemInfo.getDisplayName(), index);
		}
		
		UserItemDisplayInfo dspInfo = new UserItemDisplayInfo();
		dspInfo.setDisplayEnable(false);
		dspInfo.setDisplayName("");
		dspInfo.setHasMultiDisplayName(false);
		dspInfo.setModifyClientEnable(false);
		dspInfo.setToolTipName("");
		
		if (isDisplay != null) {
			dspInfo.setDisplayEnable(isDisplay);
		}
		if (isModifyClientEnable != null) {
			dspInfo.setModifyClientEnable(isModifyClientEnable);
		}
		if (displayName != null) {
			dspInfo.setDisplayName(displayName);
		}
		
		return dspInfo;
	}
	
	
	public static class UserItemDisplayInfo {
		private String displayName;
		private boolean displayEnable;
		private boolean hasMultiDisplayName;
		private boolean modifyClientEnable;
		private String toolTipName;
		
		public String getDisplayName() {
			return displayName;
		}
		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}
		public boolean getDisplayEnable() {
			return displayEnable;
		}
		public void setDisplayEnable(boolean displayEnable) {
			this.displayEnable = displayEnable;
		}
		public boolean getHasMultiDisplayName() {
			return hasMultiDisplayName;
		}
		public void setHasMultiDisplayName(boolean hasMultiDisplayName) {
			this.hasMultiDisplayName = hasMultiDisplayName;
		}
		public boolean isModifyClientEnable() {
			return modifyClientEnable;
		}
		public void setModifyClientEnable(boolean modifyClientEnable) {
			this.modifyClientEnable = modifyClientEnable;
		}
		public String getToolTipName() {
			return toolTipName;
		}
		public void setToolTipName(String toolTipName) {
			this.toolTipName = toolTipName;
		}
	}
}
