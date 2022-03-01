/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openapitools.client.model.EventLogInfoRequest;
import org.openapitools.client.model.EventLogInfoResponse;
import org.openapitools.client.model.GetEventListResponse;
import org.openapitools.client.model.ScopeDataInfoResponse;
import org.openapitools.client.model.StatusInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.monitor.action.GetEventListTableDefine;
import com.clustercontrol.monitor.action.GetScopeListTableDefine;
import com.clustercontrol.monitor.action.GetStatusListTableDefine;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.preference.MonitorPreferencePage;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.RestClientBeanUtil;

/**
 * ConvertListUtil
 * 
 * @version 5.0.0
 * @since 4.0.0
 */
public class ConvertListUtil {

	public static List<EventLogInfoResponse> eventLogDataMap2SortedList(Map<String, GetEventListResponse> map){
		List<EventLogInfoResponse> eventList = new ArrayList<>();
		List<String> keys = new ArrayList<>(map.keySet());
		GetEventListResponse infoList;
		for( String managerName : keys ){
			infoList = map.get(managerName);
			for( EventLogInfoResponse eventLogData : infoList.getEventList() ){
				eventLogData.setManagerName(managerName);
				eventList.add(eventLogData);
			}
		}

		// Sort - OutputDate, 降順で並べ替え
		Collections.sort(eventList, new Comparator<EventLogInfoResponse>() {
			@Override
			public int compare(EventLogInfoResponse o1, EventLogInfoResponse o2) {
				return o2.getOutputDate().compareTo(o1.getOutputDate());
			}
		});

		// Slice array
		int max = ClusterControlPlugin.getDefault().getPreferenceStore().getInt(MonitorPreferencePage.P_EVENT_MAX);
		int len = eventList.size();
		if( len > max ){
			eventList.subList(max, len).clear();
		}
		return eventList;
	}

	/**
	 *
	 * SessionBean経由で取得されたEventLogDataのマップを、
	 * 表示用のリスト（ArrayListの二次元配列）に変換するためのメソッドです。
	 *
	 * @param map
	 * @return
	 * @throws ParseException 
	 * @throws HinemosUnknown 
	 */
	public static ArrayList<ArrayList<Object>> eventLogList2Input(List<EventLogInfoResponse> eventList) throws ParseException, HinemosUnknown{

		ArrayList<ArrayList<Object>> ret = new ArrayList<ArrayList<Object>>();
		for(Iterator<EventLogInfoResponse> it = eventList.iterator(); it.hasNext();) {
			ArrayList<Object> list = new ArrayList<Object>();
			EventLogInfoResponse eventLogData = it.next();
			
			Long receiveTime = null;
			Long outputDate = null;
			Date tmpDate;
			
			list.add(GetEventListTableDefine.MANAGER_NAME, eventLogData.getManagerName());
			list.add(GetEventListTableDefine.PRIORITY, eventLogData.getPriority());
			
			tmpDate = MonitorResultRestClientWrapper.parseDate(eventLogData.getOutputDate());
			receiveTime = tmpDate.getTime();
			list.add(GetEventListTableDefine.RECEIVE_TIME, new Date(receiveTime));
			
			tmpDate = MonitorResultRestClientWrapper.parseDate(eventLogData.getGenerationDate());
			outputDate = tmpDate.getTime();
			list.add(GetEventListTableDefine.OUTPUT_DATE, new Date(outputDate));
			
			list.add(GetEventListTableDefine.PLUGIN_ID, eventLogData.getPluginId());
			list.add(GetEventListTableDefine.MONITOR_ID, eventLogData.getMonitorId());
			list.add(GetEventListTableDefine.MONITOR_DETAIL_ID, eventLogData.getMonitorDetailId());
			list.add(GetEventListTableDefine.FACILITY_ID, eventLogData.getFacilityId());
			list.add(GetEventListTableDefine.SCOPE, HinemosMessage.replace(eventLogData.getScopeText()));
			list.add(GetEventListTableDefine.APPLICATION, HinemosMessage.replace(eventLogData.getApplication()));
			list.add(GetEventListTableDefine.MESSAGE, HinemosMessage.replace(eventLogData.getMessage()));
			list.add(GetEventListTableDefine.CONFIRMED, eventLogData.getConfirmed());
			list.add(GetEventListTableDefine.CONFIRM_USER, eventLogData.getConfirmUser());
			list.add(GetEventListTableDefine.COMMENT, eventLogData.getComment());
			list.add(GetEventListTableDefine.OWNER_ROLE, eventLogData.getOwnerRoleId());
			list.add(GetEventListTableDefine.NOTIFY_UUID, eventLogData.getNotifyUUID());
			for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
				EventLogInfoRequest eventLogInfoReqest = new EventLogInfoRequest();
				RestClientBeanUtil.convertBean(eventLogData, eventLogInfoReqest);
				list.add(GetEventListTableDefine.getUserItemIndex(i), EventUtil.getUserItemValue(eventLogInfoReqest, i));
			}
			list.add(GetEventListTableDefine.EVENT_NO, eventLogData.getPosition());
			
			list.add(GetEventListTableDefine.DUMMY, null);

			ret.add(list);
		}
		return ret;
	}
	
	/**
	 *
	 * 監視[イベント]ビューに表示される内容を基にして、
	 * EventLogDataのリストに変換するためのメソッドです。
	 *
	 * オリジナルメッセージのように、表示されていない項目については、
	 * セットされないので注意してください。
	 *
	 * @param list
	 * @return
	 */
	public static List<EventLogInfoRequest> listToEventLogDataList(List<?> list) {
		List<EventLogInfoRequest> eventLogDataList = new ArrayList<EventLogInfoRequest>();
		Iterator<?> itr = list.iterator();
		while(itr.hasNext()) {
			ArrayList<?> event = (ArrayList<?>) itr.next();
			
			String managerName = (String) event.get(GetEventListTableDefine.MANAGER_NAME);
			Integer priority = (Integer) event.get(GetEventListTableDefine.PRIORITY);
			Timestamp outputDate = new Timestamp (((Date) event.get(GetEventListTableDefine.RECEIVE_TIME)).getTime());
			Timestamp generationDate = new Timestamp (((Date) event.get(GetEventListTableDefine.OUTPUT_DATE)).getTime());
			String pluginId = (String) event.get(GetEventListTableDefine.PLUGIN_ID);
			String monitorId = (String) event.get(GetEventListTableDefine.MONITOR_ID);
			String monitorDetailId = (String) event.get(GetEventListTableDefine.MONITOR_DETAIL_ID);
			String facilityId = (String) event.get(GetEventListTableDefine.FACILITY_ID);
			String scopeText = (String) event.get(GetEventListTableDefine.SCOPE);
			String application = (String) event.get(GetEventListTableDefine.APPLICATION);
			String message = (String) event.get(GetEventListTableDefine.MESSAGE);
			Integer confirmFlg = (Integer) event.get(GetEventListTableDefine.CONFIRMED);
			String confirmUser = (String) event.get(GetEventListTableDefine.CONFIRM_USER);
			String comment = (String) event.get(GetEventListTableDefine.COMMENT);
			String ownerRoleId = (String) event.get(GetEventListTableDefine.OWNER_ROLE);
			String notifyUUID = (String) event.get(GetEventListTableDefine.NOTIFY_UUID);

			EventLogInfoRequest eventLogData = new EventLogInfoRequest();

			eventLogData.setManagerName(managerName);
			eventLogData.setPriority(priority);

			Long outputDateLong = outputDate.getTime();
			String outputDateString = MonitorResultRestClientWrapper.formatDate(new Date(outputDateLong));
			eventLogData.setOutputDate(outputDateString);

			Long generationDateLong = generationDate.getTime();
			String generationDateString = MonitorResultRestClientWrapper.formatDate(new Date(generationDateLong));
			eventLogData.setGenerationDate(generationDateString);
			
			eventLogData.setPluginId(pluginId);
			eventLogData.setMonitorId(monitorId);
			eventLogData.setMonitorDetailId(monitorDetailId);
			eventLogData.setFacilityId(facilityId);
			eventLogData.setScopeText(scopeText);
			eventLogData.setApplication(application);
			eventLogData.setMessage(message);
			eventLogData.setConfirmed(confirmFlg);
			eventLogData.setConfirmUser(confirmUser);
			eventLogData.setComment(comment);
			eventLogData.setOwnerRoleId(ownerRoleId);
			for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
				String userItem = (String) event.get(GetEventListTableDefine.getUserItemIndex(i));
				EventUtil.setUserItemValue(eventLogData, i, userItem);
			}
			eventLogData.setNotifyUUID(notifyUUID);
			Long position = (Long) event.get(GetEventListTableDefine.EVENT_NO);
			eventLogData.setPosition(position);

			eventLogDataList.add(eventLogData);
		}

		return eventLogDataList;
	}

	public static ArrayList<ArrayList<Object>> statusInfoDataListToArrayList(String managerName, List<StatusInfoResponse> list) throws ParseException {
		ArrayList<ArrayList<Object>> ret  = new ArrayList<ArrayList<Object>>();
		Iterator<StatusInfoResponse> itr = list.iterator();

		Date tmpDate;
		while(itr.hasNext()) {
			ArrayList<Object> status = new ArrayList<Object>();
			StatusInfoResponse statusInfoData = itr.next();
			status.add(GetStatusListTableDefine.MANAGER_NAME, managerName);
			status.add(GetStatusListTableDefine.PRIORITY, statusInfoData.getPriority());
			status.add(GetStatusListTableDefine.PLUGIN_ID, statusInfoData.getPluginId());
			status.add(GetStatusListTableDefine.MONITOR_ID, statusInfoData.getMonitorId());
			status.add(GetStatusListTableDefine.MONITOR_DETAIL_ID, statusInfoData.getMonitorDetailId());
			status.add(GetStatusListTableDefine.FACILITY_ID, statusInfoData.getFacilityId());
			status.add(GetStatusListTableDefine.SCOPE, HinemosMessage.replace(statusInfoData.getFacilityPath()));
			status.add(GetStatusListTableDefine.APPLICATION, HinemosMessage.replace(statusInfoData.getApplication()));
			
			tmpDate = MonitorResultRestClientWrapper.parseDate(statusInfoData.getOutputDate());
			Long outputDateLong = tmpDate.getTime();
			status.add(GetStatusListTableDefine.UPDATE_TIME, new Date(outputDateLong));
			
			tmpDate = MonitorResultRestClientWrapper.parseDate(statusInfoData.getGenerationDate());
			Long generationDateLong = tmpDate.getTime();
			status.add(GetStatusListTableDefine.OUTPUT_TIME, new Date(generationDateLong));
			
			status.add(GetStatusListTableDefine.MESSAGE, HinemosMessage.replace(statusInfoData.getMessage()));
			status.add(GetStatusListTableDefine.OWNER_ROLE, statusInfoData.getOwnerRoleId());
			status.add(GetStatusListTableDefine.DUMMY, null);

			ret.add(status);
		}

		return ret;

	}

	public static ArrayList<StatusInfoResponse> listToStatusInfoDataList(List<?> list) {
		ArrayList<StatusInfoResponse> ret = new ArrayList<StatusInfoResponse>();
		Iterator<?> itr = list.iterator();
		while (itr.hasNext()) {
			ArrayList<?> status = (ArrayList<?>) itr.next();

			String monitorId = (String) status.get(GetStatusListTableDefine.MONITOR_ID);
			String monitorDetailId = (String) status.get(GetStatusListTableDefine.MONITOR_DETAIL_ID);
			String pluginId = (String) status.get(GetStatusListTableDefine.PLUGIN_ID);
			String facilityId = (String) status.get(GetStatusListTableDefine.FACILITY_ID);
			String application = (String) status.get(GetStatusListTableDefine.APPLICATION);
			Long generationDate = ((Date) status.get(GetStatusListTableDefine.OUTPUT_TIME)).getTime();
			String message = (String) status.get(GetStatusListTableDefine.MESSAGE);
			Integer priority = (Integer) status.get(GetStatusListTableDefine.PRIORITY);
			String ownerRoleId = (String) status.get(GetStatusListTableDefine.OWNER_ROLE);

			StatusInfoResponse info = new StatusInfoResponse();
			info.setMonitorId(monitorId);
			info.setMonitorDetailId(monitorDetailId);
			info.setPluginId(pluginId);
			info.setFacilityId(facilityId);
			info.setApplication(application);
			info.setGenerationDate( String.valueOf( generationDate ));
			info.setMessage(message);
			info.setPriority(priority);
			info.setOwnerRoleId(ownerRoleId);

			ret.add(info);
		}

		return ret;
	}

	public static ArrayList<ArrayList<Object>> scopeInfoDataListToArrayList(String managerName, List<ScopeDataInfoResponse> list) throws ParseException {
		ArrayList<ArrayList<Object>> ret  = new ArrayList<ArrayList<Object>>();

		Iterator<ScopeDataInfoResponse> itr = list.iterator();
		while(itr.hasNext()) {
			ArrayList<Object> status = new ArrayList<Object>();
			ScopeDataInfoResponse scopeInfoData = itr.next();
			status.add(GetScopeListTableDefine.MANAGER_NAME, managerName);
			status.add(GetScopeListTableDefine.PRIORITY, scopeInfoData.getPriority());
			status.add(GetScopeListTableDefine.FACILITY_ID, scopeInfoData.getFacilityId());
			status.add(GetScopeListTableDefine.SCOPE, HinemosMessage.replace(scopeInfoData.getFacilityPath()));
			status.add(null);
			Date tmpDate = MonitorResultRestClientWrapper.parseDate(scopeInfoData.getOutputDate());
			Long outputDateLong = tmpDate.getTime();
			status.add(GetScopeListTableDefine.UPDATE_TIME, new Date(outputDateLong));
			String facilityId = scopeInfoData.getFacilityId();
			Integer sortVal = scopeInfoData.getSortValue();
			String order = sortVal.toString() + facilityId;
			status.add(GetScopeListTableDefine.SORT_VALUE, order);
			status.add(null);

			ret.add(status);
		}
		return ret;
	}

	public static ArrayList<ArrayList<Object>> statusInfoData2List(Map<String, ArrayList<ArrayList<Object>>> map) {
		ArrayList<ArrayList<Object>> statusList = new ArrayList<>();
		List<String> keys = new ArrayList<>(map.keySet());
		for( String managerName : keys ){
			for(ArrayList<Object> list : map.get(managerName)) {
				statusList.add(list);
			}
		}
		return statusList;
	}

}