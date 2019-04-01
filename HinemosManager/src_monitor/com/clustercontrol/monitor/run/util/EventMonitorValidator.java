/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.util;


import java.util.Map;

import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.bean.EventUserExtensionItemInfo;
import com.clustercontrol.notify.util.NotifyValidator;
import com.clustercontrol.util.MessageConstant;
/**
 * イベント監視の入力チェッククラス
 * 
 */
public class EventMonitorValidator {

	/**
	 * イベント変更のイベント情報の内容をバリデーションします。
	 * 
	 * @param info
	 * @param userExtenstionItemInfoMap
	 * @return
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static boolean validateModifyEventInfo(EventDataInfo info, Map<Integer, EventUserExtensionItemInfo> userExtenstionItemInfoMap) throws InvalidSetting, InvalidRole {
		
		CommonValidator.validateNull("info", info);
		CommonValidator.validateNull(MessageConstant.MONITOR_ID.getMessage(), info.getMonitorId());
		CommonValidator.validateNull(MessageConstant.MONITOR_DETAIL_ID.getMessage(), info.getMonitorDetailId());
		CommonValidator.validateNull(MessageConstant.PLUGIN_ID.getMessage(), info.getPluginId());
		CommonValidator.validateNull(MessageConstant.OUTPUT_DATE.getMessage(), info.getOutputDate());
		CommonValidator.validateNull(MessageConstant.FACILITY_ID.getMessage(), info.getFacilityId());
		CommonValidator.validateString(MessageConstant.COMMENT.getMessage(), 
				info.getComment(), false, 0, 2048);
		
		
		if (info.getConfirmed() != null) {
			CommonValidator.validateConfirm(MessageConstant.CONFIRMED.getMessage(), info.getConfirmed());
		}
		
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			NotifyValidator.validateUserExtensionItem(
					EventUtil.getUserItemValue(info, i), 
					userExtenstionItemInfoMap.get(i), i, true);
		}
		
		return true;
	}
	



}
