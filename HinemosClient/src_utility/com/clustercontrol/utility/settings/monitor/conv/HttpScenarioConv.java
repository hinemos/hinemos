/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.conv;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.HttpScenarioCheckInfoResponse;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.PageResponse;
import org.openapitools.client.model.PageResponse.PriorityEnum;
import org.openapitools.client.model.PatternResponse;
import org.openapitools.client.model.VariableResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
//import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.HttpScenarioInfo;
import com.clustercontrol.utility.settings.monitor.xml.HttpScenarioMonitor;
import com.clustercontrol.utility.settings.monitor.xml.HttpScenarioMonitors;
import com.clustercontrol.utility.settings.monitor.xml.PageValue;
import com.clustercontrol.utility.settings.monitor.xml.PatternValue;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.VariableValue;
import com.clustercontrol.utility.util.OpenApiEnumConverter;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.version.util.VersionUtil;

/**
 * HTTP監視(シナリオ)設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 * 
 * @version 6.1.0
 * @since 5.0.a
 * 
 * 
 */
public class HttpScenarioConv {
	private final static Log logger = LogFactory.getLog(HttpScenarioConv.class);
	
	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	static private String SCHEMA_TYPE = VersionUtil.getSchemaProperty("MONITOR.HTTPSCENARIO.SCHEMATYPE");
	static private String SCHEMA_VERSION = VersionUtil.getSchemaProperty("MONITOR.HTTPSCENARIO.SCHEMAVERSION");
	static private String SCHEMA_REVISION =VersionUtil.getSchemaProperty("MONITOR.HTTPSCENARIO.SCHEMAREVISION");
	
	/**
	 * <BR>
	 * 
	 * @return
	 */
	public static SchemaInfo getSchemaVersion(){
		SchemaInfo schema = new SchemaInfo();
		
		schema.setSchemaType(SCHEMA_TYPE);
		schema.setSchemaVersion(SCHEMA_VERSION);
		schema.setSchemaRevision(SCHEMA_REVISION);
		
		return schema;
	}
	
	/*スキーマのバージョンチェック*/
	public static int checkSchemaVersion(SchemaInfo schemaInfo) {
		return BaseConv.checkSchemaVersion(
				SCHEMA_TYPE,
				SCHEMA_VERSION,
				SCHEMA_REVISION,
				schemaInfo.getSchemaType(),
				schemaInfo.getSchemaVersion(),
				schemaInfo.getSchemaRevision()
				);
	}
	
	/**
	 * <BR>
	 * 
	 * @return
	 * @throws RestConnectFailed 
	 * @throws ParseException 
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static HttpScenarioMonitors createHttpScenarioMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		HttpScenarioMonitors HttpScenarioMonitors = new HttpScenarioMonitors();
		
		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo =  MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitor(monitorInfo.getMonitorId());

			HttpScenarioMonitor HttpScenarioMonitor = new HttpScenarioMonitor();
			HttpScenarioMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			HttpScenarioMonitor.setHttpScenarioInfo(createHttpScenarioInfo(monitorInfo));
			HttpScenarioMonitors.addHttpScenarioMonitor(HttpScenarioMonitor);
		}
		
		HttpScenarioMonitors.setCommon(MonitorConv.versionDto2Xml());
		HttpScenarioMonitors.setSchemaInfo(getSchemaVersion());
		
		return HttpScenarioMonitors;
	}
	
	public static List<MonitorInfoResponse> createMonitorInfoList(HttpScenarioMonitors HttpScenarioMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();
		
		for (HttpScenarioMonitor HttpScenarioMonitor : HttpScenarioMonitors.getHttpScenarioMonitor()) {
			logger.debug("Monitor Id : " + HttpScenarioMonitor.getMonitor().getMonitorId());

			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(HttpScenarioMonitor.getMonitor());
			
			monitorInfo.setHttpScenarioCheckInfo(createHttpScenarioCheckInfo(HttpScenarioMonitor.getHttpScenarioInfo()));
			monitorInfoList.add(monitorInfo);
		}
		
		return monitorInfoList;
	}
	
	/**
	 * <BR>
	 * 
	 * @return
	 */
	private static HttpScenarioInfo createHttpScenarioInfo(MonitorInfoResponse monitorInfoResponse) {
		HttpScenarioInfo httpScenarioInfo = new HttpScenarioInfo();
		httpScenarioInfo.setMonitorTypeId("");
		httpScenarioInfo.setMonitorId(monitorInfoResponse.getMonitorId());
		HttpScenarioCheckInfoResponse  httpScenarioCheckInfo  = monitorInfoResponse.getHttpScenarioCheckInfo();
		httpScenarioInfo.setMonitoringPerPageFlg(httpScenarioCheckInfo.getMonitoringPerPageFlg());
		httpScenarioInfo.setAuthPassword(httpScenarioCheckInfo.getAuthPassword());
		httpScenarioInfo.setAuthType(httpScenarioCheckInfo.getAuthType());
		httpScenarioInfo.setAuthUser(httpScenarioCheckInfo.getAuthUser());
		httpScenarioInfo.setConnectTimeout(httpScenarioCheckInfo.getConnectTimeout());
		httpScenarioInfo.setProxyPassword(httpScenarioCheckInfo.getProxyPassword());
		httpScenarioInfo.setProxyUrl(httpScenarioCheckInfo.getProxyUrl());
		httpScenarioInfo.setProxyUser(httpScenarioCheckInfo.getProxyUser());
		if (httpScenarioCheckInfo.getProxyPort() != null) {
			httpScenarioInfo.setProxyPort(httpScenarioCheckInfo.getProxyPort());
		}
		httpScenarioInfo.setProxyFlg(httpScenarioCheckInfo.getProxyFlg());
		httpScenarioInfo.setRequestTimeout(httpScenarioCheckInfo.getRequestTimeout());
		httpScenarioInfo.setUserAgent(httpScenarioCheckInfo.getUserAgent());

		List<PageValue> pages = new ArrayList<>();
		PageValue pageInfo = null;
		PatternValue pattern = null;
		VariableValue variable = null;
		int orderNo = 0;
		for(PageResponse page: httpScenarioCheckInfo.getPages()){
			pageInfo = new PageValue();
			pageInfo.setMonitorId(monitorInfoResponse.getMonitorId());
			pageInfo.setMonitorTypeId("");
			pageInfo.setPageOrderNo(++orderNo);
			pageInfo.setDescription(page.getDescription());
			pageInfo.setMessage(page.getMessage());
			pageInfo.setPost(page.getPost());
			int priorityInt = OpenApiEnumConverter.enumToInteger(page.getPriority());
			pageInfo.setPriority(priorityInt);
			pageInfo.setStatusCode(page.getStatusCode());
			pageInfo.setUrl(page.getUrl());
			
			List<PatternValue> patterns = new ArrayList<>();
			int orderNo2 = 0;
			for(PatternResponse pat: page.getPatterns()){
				pattern = new PatternValue();
				pattern.setMonitorId(monitorInfoResponse.getMonitorId());
				pattern.setCaseSensitivityFlg(pat.getCaseSensitivityFlg());
				pattern.setPatternOrderNo(++orderNo2);
				pattern.setValidFlg(pat.getValidFlg());
				pattern.setDescription(pat.getDescription());
				pattern.setPattern(pat.getPattern());
				pattern.setProcessType(pat.getProcessType());
				patterns.add(pattern);
			};
			pageInfo.setPatternValue(patterns.toArray(new PatternValue[0]));
			
			
			List<VariableValue> variables = new ArrayList<>();
			for(VariableResponse val: page.getVariables()){
				variable = new VariableValue();
				variable.setMonitorId(monitorInfoResponse.getMonitorId());
				variable.setMatchingWithResponseFlg(val.getMatchingWithResponseFlg());
				variable.setName(val.getName());
				variable.setValue(val.getValue());
				variables.add(variable);
			};
			pageInfo.setVariableValue(variables.toArray(new VariableValue[0]));
			
			pages.add(pageInfo);
		}
		httpScenarioInfo.setPageValue(pages.toArray(new PageValue[0]));
		return httpScenarioInfo;
	}
	
	/**
	 * <BR>
	 * 
	 * @return
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	private static HttpScenarioCheckInfoResponse createHttpScenarioCheckInfo(HttpScenarioInfo httpScenarioInfo) throws InvalidSetting, HinemosUnknown {
		HttpScenarioCheckInfoResponse httpScenarioCheckInfo = new HttpScenarioCheckInfoResponse();
//		httpScenarioCheckInfo.setMonitorId(httpScenarioInfo.getMonitorId());
//		httpScenarioCheckInfo.setMonitorTypeId(httpScenarioInfo.getMonitorTypeId());
		
		httpScenarioCheckInfo.setMonitoringPerPageFlg(httpScenarioInfo.getMonitoringPerPageFlg());
		//認証(Authentication)
		if(httpScenarioInfo.getAuthType() != null && !"".equals(httpScenarioInfo.getAuthType())){
			httpScenarioCheckInfo.setAuthType(httpScenarioInfo.getAuthType());
			httpScenarioCheckInfo.setAuthUser(httpScenarioInfo.getAuthUser());
			httpScenarioCheckInfo.setAuthPassword(httpScenarioInfo.getAuthPassword());
		}
		httpScenarioCheckInfo.setConnectTimeout(httpScenarioInfo.getConnectTimeout());
		//プロキシ設定(Proxy)
		httpScenarioCheckInfo.setProxyPort(httpScenarioInfo.getProxyPort());
		httpScenarioCheckInfo.setProxyFlg(httpScenarioInfo.getProxyFlg());
		httpScenarioCheckInfo.setProxyUser(httpScenarioInfo.getProxyUser());
		httpScenarioCheckInfo.setProxyPassword(httpScenarioInfo.getProxyPassword());
		httpScenarioCheckInfo.setProxyUrl(httpScenarioInfo.getProxyUrl());
		httpScenarioCheckInfo.setRequestTimeout(httpScenarioInfo.getRequestTimeout());
		httpScenarioCheckInfo.setUserAgent(httpScenarioInfo.getUserAgent());

		List<PageResponse> pages = new ArrayList<>();
		PageResponse pageInfo = null;
		PatternResponse pattern = null;
		VariableResponse variable = null;
		PageValue[] pageValues = httpScenarioInfo.getPageValue();
		sort(pageValues);
		for(PageValue page: pageValues){
			pageInfo = new PageResponse();
			pageInfo.setDescription(page.getDescription());
			pageInfo.setMessage(page.getMessage());
//			pageInfo.setId(new PagePK());
//			pageInfo.getId().setMonitorId(page.getMonitorId());
			pageInfo.setPost(page.getPost());
			PriorityEnum priorityEnum = OpenApiEnumConverter.integerToEnum(page.getPriority(), PriorityEnum.class);
			pageInfo.setPriority(priorityEnum);
			pageInfo.setStatusCode(page.getStatusCode());
			pageInfo.setUrl(page.getUrl());
			
			List<PatternResponse> patterns = new ArrayList<>();
			PatternValue[] patternValues = page.getPatternValue();
			sort(patternValues);
			for(PatternValue pat: patternValues){
				pattern = new PatternResponse();
				pattern.setCaseSensitivityFlg(pat.getCaseSensitivityFlg());
				pattern.setValidFlg(pat.getValidFlg());
				pattern.setDescription(pat.getDescription());
				pattern.setPattern(pat.getPattern());
				pattern.setProcessType(pat.getProcessType());
				patterns.add(pattern);
			};
			pageInfo.getPatterns().clear();
			pageInfo.getPatterns().addAll(patterns);
			
			
			List<VariableResponse> variables = new ArrayList<>();
			for(VariableValue val: page.getVariableValue()){
				variable = new VariableResponse();
				variable.setMatchingWithResponseFlg(val.getMatchingWithResponseFlg());
				variable.setName(val.getName());
				variable.setValue(val.getValue());
				variables.add(variable);
			};
			pageInfo.getVariables().clear();
			pageInfo.getVariables().addAll(variables);
			
			pages.add(pageInfo);
		}
		httpScenarioCheckInfo.getPages().clear();
		httpScenarioCheckInfo.getPages().addAll(pages);
		
		return httpScenarioCheckInfo;
	}
	
	private static void sort(PatternValue[] objects) {
		try {
			Arrays.sort(
				objects,
				new Comparator<PatternValue>() {
					@Override
					public int compare(PatternValue obj1, PatternValue obj2) {
						return obj1.getPatternOrderNo() - obj2.getPatternOrderNo();
					}
				});
		}
		catch (Exception e) {
			
		}
	}
	
	private static void sort(PageValue[] objects) {
		try {
			Arrays.sort(
				objects,
				new Comparator<PageValue>() {
					@Override
					public int compare(PageValue obj1, PageValue obj2) {
						return obj1.getPageOrderNo() - obj2.getPageOrderNo();
					}
				});
		}
		catch (Exception e) {
			
		}
	}
}