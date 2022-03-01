/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.filtersetting.conv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.EventFilterBaseResponse;
import org.openapitools.client.model.EventFilterBaseResponse.FacilityTargetEnum;
import org.openapitools.client.model.EventFilterConditionResponse;
import org.openapitools.client.model.EventFilterSettingResponse;

import com.clustercontrol.fault.FilterSettingNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.filtersetting.xml.FilterMonitorHistoryEvent;
import com.clustercontrol.utility.settings.filtersetting.xml.Filtersetting;
import com.clustercontrol.utility.settings.filtersetting.xml.FiltersettingMonitorHistoryEvents;
import com.clustercontrol.utility.settings.filtersetting.xml.MonitorHistoryEventConditions;
import com.clustercontrol.utility.settings.filtersetting.xml.MonitorHistoryEventInfo;
import com.clustercontrol.utility.settings.filtersetting.xml.UserItems;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * 監視設定[イベント]フィルタ設定情報をXMLのBeanとHinemosとDTOとで変換します。<BR>
 *
 */
public class MonitorHistoryEventConv {
	// ロガー
	private static Log log = LogFactory.getLog(MonitorHistoryEventConv.class);

	// 対応スキーマバージョン
	private static final String schemaType = "K";
	private static final String schemaVersion = "1";
	private static final String schemaRevision = "1";

	/** 共通フィルタ設定の所有者を表すコード */
	private static final String COMMON_FILTER_OWNER_VALUE = "TARGET:ALL_USERS";
	
	public static String getCommonFilterOwnerValue(){
		return COMMON_FILTER_OWNER_VALUE;
	}
	
	/**
	 * パラメータに対応する文字列値を返します。
	 */
	public static String filterOwnerResolve(boolean common, String ownerUserId) {
		if (common) {
			return COMMON_FILTER_OWNER_VALUE;
		} else {
			return ownerUserId;
		}
	}
	
	/**
	 * Versionなどの共通部分について、DTOからXMLのBeanに変換します。
	 * 
	 * @param ver　Version情報などのハッシュテーブル。
	 * @return
	 */
	public static com.clustercontrol.utility.settings.filtersetting.xml.Common
			versionFilterSettingDto2Xml(Hashtable<String,String> ver){
	
		com.clustercontrol.utility.settings.filtersetting.xml.Common com =
				new com.clustercontrol.utility.settings.filtersetting.xml.Common();
				
		com.setHinemosVersion(ver.get("hinemosVersion"));
		com.setToolVersion(ver.get("toolVersion"));
		com.setGenerator(ver.get("generator"));
		com.setAuthor(System.getProperty("user.name"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		com.setGenerateDate(dateFormat.format(new Date()));
		com.setRuntimeHost(ver.get("runtimeHost"));
		com.setConnectedManager(ver.get("connectedManager"));
		
		return com;
	}

	
	/**
	 * XMLとツールの対応バージョンをチェック します。
	 * @param type
	 * @param version
	 * @param revision
	 * @return
	 */
	static public int checkSchemaVersion(String type, String version, String revision) {
		return BaseConv.checkSchemaVersion(schemaType, schemaVersion, schemaRevision, 
				type, version, revision);
	}

	/**
	 * スキーマのバージョンを返します。
	 *
	 * @return スキーマのバージョンを示すオブジェクト
	 */
	static public com.clustercontrol.utility.settings.filtersetting.xml.SchemaInfo getSchemaVersion() {

		com.clustercontrol.utility.settings.filtersetting.xml.SchemaInfo schema = 
				new com.clustercontrol.utility.settings.filtersetting.xml.SchemaInfo();

		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);

		return schema;
	}
	
	public static FilterMonitorHistoryEvent getFilterSettingMonitorHistoryEvent(EventFilterSettingResponse response)
			throws IndexOutOfBoundsException, ParseException {
	
		FilterMonitorHistoryEvent filter = new FilterMonitorHistoryEvent();
		
		// 共通部分
		Filtersetting filterInfo = new Filtersetting();
		filterInfo.setFilterId(response.getFilterId());
		filterInfo.setFilterName(response.getFilterName());
		filterInfo.setObjectId(response.getObjectId());
		filterInfo.setOwnerRoleId(response.getOwnerRoleId());
		filterInfo.setOwnerUserId(filterOwnerResolve(response.getCommon(), response.getOwnerUserId()));
		filter.setFiltersetting(filterInfo);
		
		// 固有部分
		MonitorHistoryEventInfo eventInfo = new MonitorHistoryEventInfo();
		eventInfo.setFacilityId(response.getEventFilter().getFacilityId());
		if(response.getEventFilter().getFacilityTarget() != null){
			if (response.getEventFilter().getFacilityTarget().equals(EventFilterBaseResponse.FacilityTargetEnum.ONE_LEVEL)){
				eventInfo.setFacilityTarget(0);
			} else if (response.getEventFilter().getFacilityTarget().equals(EventFilterBaseResponse.FacilityTargetEnum.ALL)){
				eventInfo.setFacilityTarget(1);
			}
		}
		eventInfo.setEntire(response.getEventFilter().getEntire());
		
		int i = 0;
		for(EventFilterConditionResponse conditionRes : response.getEventFilter().getConditions()){
			MonitorHistoryEventConditions condition = new MonitorHistoryEventConditions();
			condition.setConditionIdx(i);
			condition.setDescription(conditionRes.getDescription());
			if(conditionRes.getNegative() != null){
				condition.setNegative(conditionRes.getNegative());
			}
			if(conditionRes.getPriorityCritical() != null){
				condition.setPriorityCritical(conditionRes.getPriorityCritical());
			}
			if(conditionRes.getPriorityWarning() != null){
				condition.setPriorityWarning(conditionRes.getPriorityWarning());
			}
			if(conditionRes.getPriorityInfo() != null){
				condition.setPriorityInfo(conditionRes.getPriorityInfo());
			}
			if(conditionRes.getPriorityUnknown() != null){
				condition.setPriorityUnknown(conditionRes.getPriorityUnknown());
			}
			condition.setGenerationDateFrom(conditionRes.getGenerationDateFrom());
			condition.setGenerationDateTo(conditionRes.getGenerationDateTo());
			condition.setOutputDateFrom(conditionRes.getOutputDateFrom());
			condition.setOutputDateTo(conditionRes.getOutputDateTo());
			condition.setMonitorId(conditionRes.getMonitorId());
			condition.setMonitorDetail(conditionRes.getMonitorDetail());
			condition.setApplication(conditionRes.getApplication());
			condition.setMessage(conditionRes.getMessage());
			if(conditionRes.getConfirmYet() != null){
				condition.setConfirmYet(conditionRes.getConfirmYet());
			}
			if(conditionRes.getConfirmDoing() != null){
				condition.setConfirmDoing(conditionRes.getConfirmDoing());
			}
			if(conditionRes.getConfirmDone() != null){
				condition.setConfirmDone(conditionRes.getConfirmDone());
			}
			if(conditionRes.getConfirmUser() != null){
				condition.setConfirmUser(conditionRes.getConfirmUser());
			}
			condition.setComment(conditionRes.getComment());
			condition.setCommentUser(conditionRes.getCommentUser());
			if(conditionRes.getGraphFlag() != null){
				condition.setGraphFlag(conditionRes.getGraphFlag());
			}
			condition.setOwnerRoleId(conditionRes.getOwnerRoleId());
			condition.setNotifyUUID(conditionRes.getNotifyUUID());
			if(conditionRes.getPositionFrom() != null){
				condition.setPositionFrom(conditionRes.getPositionFrom());
			}
			if(conditionRes.getPositionTo() != null){
				condition.setPositionTo(conditionRes.getPositionTo());
			}
			
			for(Entry<String, String> itemRes : conditionRes.getUserItems().entrySet()){
				UserItems item = new UserItems();
				item.setKey(itemRes.getKey());
				item.setValue(itemRes.getValue());
				condition.addUserItems(item);
			}
			
			eventInfo.addMonitorHistoryEventConditions(condition);
			i++;
		}
		
		filter.setMonitorHistoryEventInfo(eventInfo);
	
		return filter;
	}
	
	public static List<EventFilterSettingResponse> createMonitorHistoryEventList(
			FiltersettingMonitorHistoryEvents filters, ArrayList<Boolean> filterTypeList, Boolean userRange) 
				throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, 
					FilterSettingNotFound, InvalidSetting, ParseException {
		List<EventFilterSettingResponse> filterList = new LinkedList<EventFilterSettingResponse>();
	
		for (FilterMonitorHistoryEvent filter : filters.getFilterMonitorHistoryEvent()) {
			log.debug("Id : " + filter.getFiltersetting().getFilterId());
			EventFilterSettingResponse filterInfo = createMonitorHistoryEvent(filter, filterTypeList, userRange);
			
			if(filterInfo != null){
				filterList.add(filterInfo);
			}
		}
	
		return filterList;
	}
	
	public static EventFilterSettingResponse createMonitorHistoryEvent(FilterMonitorHistoryEvent filter, 
			ArrayList<Boolean> filterTypeList, Boolean userRange) throws InvalidSetting, HinemosUnknown {
		
		EventFilterSettingResponse ret = new EventFilterSettingResponse();
		
		if(filterTypeList.contains(true) && filter.getFiltersetting().getOwnerUserId().equals(COMMON_FILTER_OWNER_VALUE)){
			ret.setCommon(true);
			createFilterSetting(filter, ret);
			return ret;
		}
		
		if(filterTypeList.contains(false) && !filter.getFiltersetting().getOwnerUserId().equals(COMMON_FILTER_OWNER_VALUE)){
			ret.setCommon(false);
			createFilterSetting(filter, ret);
			
			// ユーザフィルタを実行ユーザの設定としてインポートする場合
			if(!userRange){
				ret.setOwnerUserId(RestConnectManager.getLoginUserId(UtilityManagerUtil.getCurrentManagerName()));
			}
			
			return ret;
		}
		
		return null;
	}
	
	private static EventFilterSettingResponse createFilterSetting(FilterMonitorHistoryEvent filter, 
			EventFilterSettingResponse ret) throws InvalidSetting, HinemosUnknown {
		// 共通部分
		ret.setFilterId(filter.getFiltersetting().getFilterId());
		ret.setFilterName(filter.getFiltersetting().getFilterName());
		ret.setObjectId(filter.getFiltersetting().getObjectId());
		ret.setOwnerRoleId(filter.getFiltersetting().getOwnerRoleId());
		ret.setOwnerUserId(filter.getFiltersetting().getOwnerUserId());
		
		// 固有部分
		EventFilterBaseResponse eventRet = new EventFilterBaseResponse();
		eventRet.setFacilityId(filter.getMonitorHistoryEventInfo().getFacilityId());
		if(filter.getMonitorHistoryEventInfo().hasEntire()){
			eventRet.setEntire(filter.getMonitorHistoryEventInfo().getEntire());
		}
		if(filter.getMonitorHistoryEventInfo().hasFacilityTarget()){
			if(filter.getMonitorHistoryEventInfo().getFacilityTarget() == 0){
				eventRet.setFacilityTarget(FacilityTargetEnum.ONE_LEVEL);
			} else if(filter.getMonitorHistoryEventInfo().getFacilityTarget() == 1){
				eventRet.setFacilityTarget(FacilityTargetEnum.ALL);
			}
		}

		// conditionIdxの昇順に並べかえる
		MonitorHistoryEventConditions[] conditionList = filter.getMonitorHistoryEventInfo()
				.getMonitorHistoryEventConditions();
		Arrays.sort(conditionList, new Comparator<MonitorHistoryEventConditions>() {
			@Override
			public int compare(MonitorHistoryEventConditions obj1, MonitorHistoryEventConditions obj2) {
				return obj1.getConditionIdx() - obj2.getConditionIdx();
			}
		});

		for(MonitorHistoryEventConditions condition : conditionList){
			EventFilterConditionResponse conditionRet = new EventFilterConditionResponse();
			conditionRet.setDescription(condition.getDescription());
			if(condition.hasNegative()){
				conditionRet.setNegative(condition.getNegative());
			}
			if(condition.hasPriorityCritical()){
				conditionRet.setPriorityCritical(condition.getPriorityCritical());
			}
			if(condition.hasPriorityWarning()){
				conditionRet.setPriorityWarning(condition.getPriorityWarning());
			}
			if(condition.hasPriorityInfo()){
				conditionRet.setPriorityInfo(condition.getPriorityInfo());
			}
			if(condition.hasPriorityUnknown()){
				conditionRet.setPriorityUnknown(condition.getPriorityUnknown());
			}
			conditionRet.setGenerationDateFrom(condition.getGenerationDateFrom());
			conditionRet.setGenerationDateTo(condition.getGenerationDateTo());
			conditionRet.setOutputDateFrom(condition.getOutputDateFrom());
			conditionRet.setOutputDateTo(condition.getOutputDateTo());
			conditionRet.setMonitorId(condition.getMonitorId());
			conditionRet.setMonitorDetail(condition.getMonitorDetail());
			conditionRet.setApplication(condition.getApplication());
			conditionRet.setMessage(condition.getMessage());
			if(condition.hasConfirmYet()){
				conditionRet.setConfirmYet(condition.getConfirmYet());
			}
			if(condition.hasConfirmDoing()){
				conditionRet.setConfirmDoing(condition.getConfirmDoing());
			}
			if(condition.hasConfirmDone()){
				conditionRet.setConfirmDone(condition.getConfirmDone());
			}
			conditionRet.setConfirmUser(condition.getConfirmUser());
			conditionRet.setComment(condition.getComment());
			conditionRet.setCommentUser(condition.getCommentUser());
			if(condition.hasGraphFlag()){
				conditionRet.setGraphFlag(condition.getGraphFlag());
			}
			conditionRet.setOwnerRoleId(condition.getOwnerRoleId());
			conditionRet.setNotifyUUID(condition.getNotifyUUID());
			if(condition.hasPositionFrom()){
				conditionRet.setPositionFrom(condition.getPositionFrom());
			}
			if(condition.hasPositionTo()){
				conditionRet.setPositionTo(condition.getPositionTo());
			}
			
			for(UserItems itemRet : condition.getUserItems()){
				conditionRet.putUserItemsItem(itemRet.getKey(), itemRet.getValue());
			}
			
			eventRet.addConditionsItem(conditionRet);
		}
		ret.setEventFilter(eventRet);
		
		return ret;
	}
}
