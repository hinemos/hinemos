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
import org.openapitools.client.model.JobHistoryFilterBaseResponse;
import org.openapitools.client.model.JobHistoryFilterConditionResponse;
import org.openapitools.client.model.JobHistoryFilterConditionResponse.EndStatusEnum;
import org.openapitools.client.model.JobHistoryFilterConditionResponse.StatusEnum;
import org.openapitools.client.model.JobHistoryFilterConditionResponse.TriggerTypeEnum;
import org.openapitools.client.model.JobHistoryFilterSettingResponse;

import com.clustercontrol.fault.FilterSettingNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.filtersetting.xml.FilterJobHistory;
import com.clustercontrol.utility.settings.filtersetting.xml.Filtersetting;
import com.clustercontrol.utility.settings.filtersetting.xml.FiltersettingJobHistories;
import com.clustercontrol.utility.settings.filtersetting.xml.JobHistoryConditions;
import com.clustercontrol.utility.settings.filtersetting.xml.JobHistoryInfo;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.util.OpenApiEnumConverter;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * 監視設定[イベント]フィルタ設定情報をXMLのBeanとHinemosとDTOとで変換します。<BR>
 *
 */
public class JobHistoryConv {
	// ロガー
	private static Log log = LogFactory.getLog(JobHistoryConv.class);

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
	
	public static FilterJobHistory getFilterSettingJobHistory(JobHistoryFilterSettingResponse response)
			throws IndexOutOfBoundsException, ParseException {
	
		FilterJobHistory filter = new FilterJobHistory();
		
		// 共通部分
		Filtersetting filterInfo = new Filtersetting();
		filterInfo.setFilterId(response.getFilterId());
		filterInfo.setFilterName(response.getFilterName());
		filterInfo.setObjectId(response.getObjectId());
		filterInfo.setOwnerRoleId(response.getOwnerRoleId());
		filterInfo.setOwnerUserId(filterOwnerResolve(response.getCommon(), response.getOwnerUserId()));
		filter.setFiltersetting(filterInfo);
		
		// 固有部分
		JobHistoryInfo jobHistoryInfo = new JobHistoryInfo();
		
		int i = 0;
		for(JobHistoryFilterConditionResponse conditionRes : response.getJobHistoryFilter().getConditions()){
			JobHistoryConditions condition = new JobHistoryConditions();
			condition.setConditionIdx(i);
			condition.setDescription(conditionRes.getDescription());
			if(conditionRes.getNegative() != null){
				condition.setNegative(conditionRes.getNegative());
			}
			condition.setStartDateFrom(conditionRes.getStartDateFrom());
			condition.setStartDateTo(conditionRes.getStartDateTo());
			condition.setEndDateFrom(conditionRes.getEndDateFrom());
			condition.setEndDateTo(conditionRes.getEndDateTo());
			condition.setSessionId(conditionRes.getSessionId());
			condition.setJobId(conditionRes.getJobId());
			if(conditionRes.getStatus() != null){
				condition.setStatus(String.valueOf(OpenApiEnumConverter.enumToInteger(conditionRes.getStatus())));
			}
			if(conditionRes.getEndStatus() != null){
				condition.setEndStatus(String.valueOf(OpenApiEnumConverter.enumToInteger(conditionRes.getEndStatus())));
			}
			if(conditionRes.getTriggerType() != null){
				condition.setTriggerType(String.valueOf(OpenApiEnumConverter.enumToInteger(conditionRes.getTriggerType())));
			}
			condition.setTriggerInfo(conditionRes.getTriggerInfo());
			condition.setOwnerRoleId(conditionRes.getOwnerRoleId());
			
			jobHistoryInfo.addJobHistoryConditions(condition);
			i++;
		}
		
		filter.setJobHistoryInfo(jobHistoryInfo);
	
		return filter;
	}
	
	public static List<JobHistoryFilterSettingResponse> 
		createJobHistoryList(FiltersettingJobHistories filters, ArrayList<Boolean> filterTypeList, Boolean userRange) 
				throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, 
					FilterSettingNotFound, InvalidSetting, ParseException {
		List<JobHistoryFilterSettingResponse> filterList = new LinkedList<JobHistoryFilterSettingResponse>();
	
		for (FilterJobHistory filter : filters.getFilterJobHistory()) {
			log.debug("Id : " + filter.getFiltersetting().getFilterId());
			JobHistoryFilterSettingResponse filterInfo = createJobHistory(filter, filterTypeList, userRange);
			
			if(filterInfo != null){
				filterList.add(filterInfo);
			}
		}
	
		return filterList;
	}
	
	public static JobHistoryFilterSettingResponse createJobHistory(FilterJobHistory filter, 
			ArrayList<Boolean> filterTypeList, Boolean userRange) throws InvalidSetting, HinemosUnknown {
		
		JobHistoryFilterSettingResponse ret = new JobHistoryFilterSettingResponse();
		
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
	
	private static JobHistoryFilterSettingResponse createFilterSetting(FilterJobHistory filter, 
			JobHistoryFilterSettingResponse ret) throws InvalidSetting, HinemosUnknown {
		// 共通部分
		ret.setFilterId(filter.getFiltersetting().getFilterId());
		ret.setFilterName(filter.getFiltersetting().getFilterName());
		ret.setObjectId(filter.getFiltersetting().getObjectId());
		ret.setOwnerRoleId(filter.getFiltersetting().getOwnerRoleId());
		ret.setOwnerUserId(filter.getFiltersetting().getOwnerUserId());
		
		// 固有部分
		JobHistoryFilterBaseResponse statusRet = new JobHistoryFilterBaseResponse();

		// conditionIdxの昇順に並べかえる
		JobHistoryConditions[] conditionList = filter.getJobHistoryInfo().getJobHistoryConditions();
		Arrays.sort(conditionList, new Comparator<JobHistoryConditions>() {
			@Override
			public int compare(JobHistoryConditions obj1, JobHistoryConditions obj2) {
				return obj1.getConditionIdx() - obj2.getConditionIdx();
			}
		});

		for(JobHistoryConditions condition : conditionList){
			JobHistoryFilterConditionResponse conditionRet = new JobHistoryFilterConditionResponse();
			conditionRet.setDescription(condition.getDescription());
			if(condition.hasNegative()){
				conditionRet.setNegative(condition.getNegative());
			}
			conditionRet.setStartDateFrom(condition.getStartDateFrom());
			conditionRet.setStartDateTo(condition.getStartDateTo());
			conditionRet.setEndDateFrom(condition.getEndDateFrom());
			conditionRet.setEndDateTo(condition.getEndDateTo());
			conditionRet.setSessionId(condition.getSessionId());
			conditionRet.setJobId(condition.getJobId());
			if(condition.getStatus() != null){
				conditionRet.setStatus(OpenApiEnumConverter.integerToEnum(Integer.valueOf(condition.getStatus()), StatusEnum.class));
			}
			if(condition.getEndStatus() != null){
				conditionRet.setEndStatus(OpenApiEnumConverter.integerToEnum(Integer.valueOf(condition.getEndStatus()), EndStatusEnum.class));
			}
			if(condition.getTriggerType() != null){
				conditionRet.setTriggerType(OpenApiEnumConverter.integerToEnum(Integer.valueOf(condition.getTriggerType()), TriggerTypeEnum.class));
			}
			conditionRet.setTriggerInfo(condition.getTriggerInfo());
			conditionRet.setOwnerRoleId(condition.getOwnerRoleId());
			
			statusRet.addConditionsItem(conditionRet);
		}
		ret.setJobHistoryFilter(statusRet);
		
		return ret;
	}
}
