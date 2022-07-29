/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openapitools.client.model.EventFilterSettingResponse;
import org.openapitools.client.model.JobHistoryFilterSettingResponse;
import org.openapitools.client.model.StatusFilterSettingResponse;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.MessageConstant;

public class FilterSettingUtil {
	//キーの一意確認
	public static void confirmUniq(List<String> keyList) throws InvalidSetting {
		if (keyList == null) {
			return;
		}
		Set<String> listedIdSet = new HashSet<String>();
		for (String uniqSet :keyList) {
			if(listedIdSet.contains(uniqSet)){
				String[] args = {
					"[" + MessageConstant.USER_ID.getMessage() +"-"+ MessageConstant.FILTER_ID.getMessage() + "]"
				,	uniqSet
				};
				String message = HinemosMessage.replace(MessageConstant.MESSAGE_DUPLICATED.getMessage(args));
				throw new InvalidSetting(message);
			}
			listedIdSet.add(uniqSet);
		}
	}

	//キーリスト取得(Event)
	public static List<String> getKeyListEvent(List<JobHistoryFilterSettingResponse> data) {
		if (data == null) {
			return null;
		}
		List<String> retList = new ArrayList<String>();
		for (JobHistoryFilterSettingResponse filter :data) {
			retList.add(filter.getOwnerUserId() + "-" + filter.getFilterId());
		}
		return retList;
	}

	//キーリスト取得(Status)
	public static List<String> getKeyListStatus(List<StatusFilterSettingResponse> data) {
		if (data == null) {
			return null;
		}
		List<String> retList = new ArrayList<String>();
		for (StatusFilterSettingResponse filter :data) {
			retList.add(filter.getOwnerUserId() + "-" + filter.getFilterId());
		}
		return retList;
	}

	//キーリスト取得(JobHistory)
	public static List<String> getKeyListJobHis(List<EventFilterSettingResponse> data) {
		if (data == null) {
			return null;
		}
		List<String> retList = new ArrayList<String>();
		for (EventFilterSettingResponse filter :data) {
			retList.add(filter.getOwnerUserId() + "-" + filter.getFilterId());
		}
		return retList;
	}

}
