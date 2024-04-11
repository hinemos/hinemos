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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.StatusFilterBaseResponse;
import org.openapitools.client.model.StatusFilterBaseResponse.FacilityTargetEnum;
import org.openapitools.client.model.StatusFilterConditionResponse;
import org.openapitools.client.model.StatusFilterSettingResponse;

import com.clustercontrol.fault.FilterSettingNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.filtersetting.xml.FilterMonitorHistoryStatus;
import com.clustercontrol.utility.settings.filtersetting.xml.Filtersetting;
import com.clustercontrol.utility.settings.filtersetting.xml.FiltersettingMonitorHistoryStatuses;
import com.clustercontrol.utility.settings.filtersetting.xml.MonitorHistoryStatusConditions;
import com.clustercontrol.utility.settings.filtersetting.xml.MonitorHistoryStatusInfo;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.version.util.VersionUtil;

/**
 * 監視設定[イベント]フィルタ設定情報をXMLのBeanとHinemosとDTOとで変換します。<BR>
 *
 */
public class MonitorHistoryStatusConv {
	// ロガー
	private static Log log = LogFactory.getLog(MonitorHistoryStatusConv.class);

	// 対応スキーマバージョン
	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	private static final String schemaType = VersionUtil.getSchemaProperty("FILTERSETTING.MONITORHISTORYSTATUS.SCHEMATYPE");
	private static final String schemaVersion = VersionUtil.getSchemaProperty("FILTERSETTING.MONITORHISTORYSTATUS.SCHEMAVERSION");
	private static final String schemaRevision =VersionUtil.getSchemaProperty("FILTERSETTING.MONITORHISTORYSTATUS.SCHEMAREVISION");

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
	
	public static FilterMonitorHistoryStatus getFilterSettingMonitorHistoryStatus(StatusFilterSettingResponse response)
			throws IndexOutOfBoundsException, ParseException {
	
		FilterMonitorHistoryStatus filter = new FilterMonitorHistoryStatus();
		
		// 共通部分
		Filtersetting filterInfo = new Filtersetting();
		filterInfo.setFilterId(response.getFilterId());
		filterInfo.setFilterName(response.getFilterName());
		filterInfo.setObjectId(response.getObjectId());
		filterInfo.setOwnerRoleId(response.getOwnerRoleId());
		filterInfo.setOwnerUserId(filterOwnerResolve(response.getCommon(), response.getOwnerUserId()));
		filter.setFiltersetting(filterInfo);
		
		// 固有部分
		MonitorHistoryStatusInfo statusInfo = new MonitorHistoryStatusInfo();
		statusInfo.setFacilityId(response.getStatusFilter().getFacilityId());
		if(response.getStatusFilter().getFacilityTarget() != null){
			if (response.getStatusFilter().getFacilityTarget().equals(StatusFilterBaseResponse.FacilityTargetEnum.ONE_LEVEL)){
				statusInfo.setFacilityTarget(0);
			} else if (response.getStatusFilter().getFacilityTarget().equals(StatusFilterBaseResponse.FacilityTargetEnum.ALL)){
				statusInfo.setFacilityTarget(1);
			}
		}
		
		int i = 0;
		for(StatusFilterConditionResponse conditionRes : response.getStatusFilter().getConditions()){
			MonitorHistoryStatusConditions condition = new MonitorHistoryStatusConditions();
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
			condition.setOwnerRoleId(conditionRes.getOwnerRoleId());
			
			statusInfo.addMonitorHistoryStatusConditions(condition);
			i++;
		}
		
		filter.setMonitorHistoryStatusInfo(statusInfo);
	
		return filter;
	}
	
	public static List<StatusFilterSettingResponse> 
		createMonitorHistoryStatusList(FiltersettingMonitorHistoryStatuses filters, ArrayList<Boolean> filterTypeList, Boolean userRange) 
				throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, 
					FilterSettingNotFound, InvalidSetting, ParseException {
		List<StatusFilterSettingResponse> filterList = new LinkedList<StatusFilterSettingResponse>();
	
		for (FilterMonitorHistoryStatus filter : filters.getFilterMonitorHistoryStatus()) {
			log.debug("Id : " + filter.getFiltersetting().getFilterId());
			StatusFilterSettingResponse filterInfo = createMonitorHistoryStatus(filter, filterTypeList, userRange);
			
			if(filterInfo != null){
				filterList.add(filterInfo);
			}
		}
	
		return filterList;
	}
	
	public static StatusFilterSettingResponse createMonitorHistoryStatus(FilterMonitorHistoryStatus filter, 
			ArrayList<Boolean> filterTypeList, Boolean userRange) throws InvalidSetting, HinemosUnknown {
		
		StatusFilterSettingResponse ret = new StatusFilterSettingResponse();
		
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
	
	private static StatusFilterSettingResponse createFilterSetting(FilterMonitorHistoryStatus filter, 
			StatusFilterSettingResponse ret) throws InvalidSetting, HinemosUnknown {
		// 共通部分
		ret.setFilterId(filter.getFiltersetting().getFilterId());
		ret.setFilterName(filter.getFiltersetting().getFilterName());
		ret.setObjectId(filter.getFiltersetting().getObjectId());
		ret.setOwnerRoleId(filter.getFiltersetting().getOwnerRoleId());
		ret.setOwnerUserId(filter.getFiltersetting().getOwnerUserId());
		
		// 固有部分
		StatusFilterBaseResponse statusRet = new StatusFilterBaseResponse();
		statusRet.setFacilityId(filter.getMonitorHistoryStatusInfo().getFacilityId());
		if(filter.getMonitorHistoryStatusInfo().hasFacilityTarget()){
			if(filter.getMonitorHistoryStatusInfo().getFacilityTarget() == 0){
				statusRet.setFacilityTarget(FacilityTargetEnum.ONE_LEVEL);
			} else if(filter.getMonitorHistoryStatusInfo().getFacilityTarget() == 1){
				statusRet.setFacilityTarget(FacilityTargetEnum.ALL);
			}
		}

		// conditionIdxの昇順に並べかえる
		MonitorHistoryStatusConditions[] conditionList = filter.getMonitorHistoryStatusInfo()
				.getMonitorHistoryStatusConditions();
		Arrays.sort(conditionList, new Comparator<MonitorHistoryStatusConditions>() {
			@Override
			public int compare(MonitorHistoryStatusConditions obj1, MonitorHistoryStatusConditions obj2) {
				return obj1.getConditionIdx() - obj2.getConditionIdx();
			}
		});

		for(MonitorHistoryStatusConditions condition : conditionList){
			StatusFilterConditionResponse conditionRet = new StatusFilterConditionResponse();
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
			conditionRet.setOwnerRoleId(condition.getOwnerRoleId());
			
			statusRet.addConditionsItem(conditionRet);
		}
		ret.setStatusFilter(statusRet);
		
		return ret;
	}
}
