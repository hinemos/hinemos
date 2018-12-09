/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.conv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.HttpScenarioInfo;
import com.clustercontrol.utility.settings.monitor.xml.HttpScenarioMonitor;
import com.clustercontrol.utility.settings.monitor.xml.HttpScenarioMonitors;
import com.clustercontrol.utility.settings.monitor.xml.PageValue;
import com.clustercontrol.utility.settings.monitor.xml.PatternValue;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.VariableValue;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.HttpScenarioCheckInfo;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.Page;
import com.clustercontrol.ws.monitor.PagePK;
import com.clustercontrol.ws.monitor.Pattern;
import com.clustercontrol.ws.monitor.Variable;

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
	
	static private String SCHEMA_TYPE = "H";
	static private String SCHEMA_VERSION = "1";
	static private String SCHEMA_REVISION = "2";
	
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
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static HttpScenarioMonitors createHttpScenarioMonitors(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		HttpScenarioMonitors HttpScenarioMonitors = new HttpScenarioMonitors();
		
		for (MonitorInfo monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo =  MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitor(monitorInfo.getMonitorId());

			HttpScenarioMonitor HttpScenarioMonitor = new HttpScenarioMonitor();
			HttpScenarioMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			HttpScenarioMonitor.setHttpScenarioInfo(createHttpScenarioInfo(monitorInfo.getHttpScenarioCheckInfo()));
			HttpScenarioMonitors.addHttpScenarioMonitor(HttpScenarioMonitor);
		}
		
		HttpScenarioMonitors.setCommon(MonitorConv.versionDto2Xml());
		HttpScenarioMonitors.setSchemaInfo(getSchemaVersion());
		
		return HttpScenarioMonitors;
	}
	
	public static List<MonitorInfo> createMonitorInfoList(HttpScenarioMonitors HttpScenarioMonitors) throws ConvertorException {
		List<MonitorInfo> monitorInfoList = new LinkedList<MonitorInfo>();
		
		for (HttpScenarioMonitor HttpScenarioMonitor : HttpScenarioMonitors.getHttpScenarioMonitor()) {
			logger.debug("Monitor Id : " + HttpScenarioMonitor.getMonitor().getMonitorId());

			MonitorInfo monitorInfo = MonitorConv.createMonitorInfo(HttpScenarioMonitor.getMonitor());
			
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
	private static HttpScenarioInfo createHttpScenarioInfo(HttpScenarioCheckInfo httpScenarioCheckInfo) {
		HttpScenarioInfo httpScenarioInfo = new HttpScenarioInfo();
		httpScenarioInfo.setMonitorTypeId("");
		httpScenarioInfo.setMonitorId(httpScenarioCheckInfo.getMonitorId());
		httpScenarioInfo.setMonitoringPerPageFlg(httpScenarioCheckInfo.isMonitoringPerPageFlg());
		httpScenarioInfo.setAuthPassword(httpScenarioCheckInfo.getAuthPassword());
		httpScenarioInfo.setAuthType(httpScenarioCheckInfo.getAuthType());
		httpScenarioInfo.setAuthUser(httpScenarioCheckInfo.getAuthUser());
		httpScenarioInfo.setConnectTimeout(httpScenarioCheckInfo.getConnectTimeout());
		httpScenarioInfo.setProxyPassword(httpScenarioCheckInfo.getProxyPassword());
		httpScenarioInfo.setProxyUrl(httpScenarioCheckInfo.getProxyUrl());
		httpScenarioInfo.setProxyUser(httpScenarioCheckInfo.getProxyUser());
		httpScenarioInfo.setProxyPort(httpScenarioCheckInfo.getProxyPort());
		httpScenarioInfo.setProxyFlg(httpScenarioCheckInfo.isProxyFlg());
		httpScenarioInfo.setRequestTimeout(httpScenarioCheckInfo.getRequestTimeout());
		httpScenarioInfo.setUserAgent(httpScenarioCheckInfo.getUserAgent());

		List<PageValue> pages = new ArrayList<>();
		PageValue pageInfo = null;
		PatternValue pattern = null;
		VariableValue variable = null;
		int orderNo = 0;
		for(com.clustercontrol.ws.monitor.Page page: httpScenarioCheckInfo.getPages()){
			pageInfo = new PageValue();
			pageInfo.setMonitorId(httpScenarioCheckInfo.getMonitorId());
			pageInfo.setMonitorTypeId("");
			pageInfo.setPageOrderNo(++orderNo);
			pageInfo.setDescription(page.getDescription());
			pageInfo.setMessage(page.getMessage());
			pageInfo.setPost(page.getPost());
			pageInfo.setPriority(page.getPriority());
			pageInfo.setStatusCode(page.getStatusCode());
			pageInfo.setUrl(page.getUrl());
			
			List<PatternValue> patterns = new ArrayList<>();
			int orderNo2 = 0;
			for(Pattern pat: page.getPatterns()){
				pattern = new PatternValue();
				pattern.setMonitorId(httpScenarioCheckInfo.getMonitorId());
				pattern.setCaseSensitivityFlg(pat.isCaseSensitivityFlg());
				pattern.setPatternOrderNo(++orderNo2);
				pattern.setValidFlg(pat.isValidFlg());
				pattern.setDescription(pat.getDescription());
				pattern.setPattern(pat.getPattern());
				pattern.setProcessType(pat.isProcessType());
				patterns.add(pattern);
			};
			pageInfo.setPatternValue(patterns.toArray(new PatternValue[0]));
			
			
			List<VariableValue> variables = new ArrayList<>();
			for(Variable val: page.getVariables()){
				variable = new VariableValue();
				variable.setMonitorId(httpScenarioCheckInfo.getMonitorId());
				variable.setMatchingWithResponseFlg(val.isMatchingWithResponseFlg());
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
	 */
	private static HttpScenarioCheckInfo createHttpScenarioCheckInfo(HttpScenarioInfo httpScenarioInfo) {
		HttpScenarioCheckInfo httpScenarioCheckInfo = new HttpScenarioCheckInfo();
		httpScenarioCheckInfo.setMonitorId(httpScenarioInfo.getMonitorId());
		httpScenarioCheckInfo.setMonitorTypeId(httpScenarioInfo.getMonitorTypeId());
		
		httpScenarioCheckInfo.setMonitoringPerPageFlg(httpScenarioInfo.getMonitoringPerPageFlg());
		httpScenarioCheckInfo.setAuthPassword(httpScenarioInfo.getAuthPassword());
		if(httpScenarioInfo.getAuthType() != null && !"".equals(httpScenarioInfo.getAuthType())){
			httpScenarioCheckInfo.setAuthType(httpScenarioInfo.getAuthType());
		}
		httpScenarioCheckInfo.setAuthUser(httpScenarioInfo.getAuthUser());
		httpScenarioCheckInfo.setConnectTimeout(httpScenarioInfo.getConnectTimeout());
		httpScenarioCheckInfo.setProxyPassword(httpScenarioInfo.getProxyPassword());
		httpScenarioCheckInfo.setProxyUrl(httpScenarioInfo.getProxyUrl());
		httpScenarioCheckInfo.setProxyUser(httpScenarioInfo.getProxyUser());
		httpScenarioCheckInfo.setProxyPort(httpScenarioInfo.getProxyPort());
		httpScenarioCheckInfo.setProxyFlg(httpScenarioInfo.getProxyFlg());
		httpScenarioCheckInfo.setRequestTimeout(httpScenarioInfo.getRequestTimeout());
		httpScenarioCheckInfo.setUserAgent(httpScenarioInfo.getUserAgent());

		List<Page> pages = new ArrayList<>();
		Page pageInfo = null;
		Pattern pattern = null;
		Variable variable = null;
		PageValue[] pageValues = httpScenarioInfo.getPageValue();
		sort(pageValues);
		for(PageValue page: pageValues){
			pageInfo = new Page();
			pageInfo.setDescription(page.getDescription());
			pageInfo.setMessage(page.getMessage());
			pageInfo.setId(new PagePK());
			pageInfo.getId().setMonitorId(page.getMonitorId());
			pageInfo.setPost(page.getPost());
			pageInfo.setPriority(page.getPriority());
			pageInfo.setStatusCode(page.getStatusCode());
			pageInfo.setUrl(page.getUrl());
			
			List<Pattern> patterns = new ArrayList<>();
			PatternValue[] patternValues = page.getPatternValue();
			sort(patternValues);
			for(PatternValue pat: patternValues){
				pattern = new Pattern();
				pattern.setCaseSensitivityFlg(pat.getCaseSensitivityFlg());
				pattern.setValidFlg(pat.getValidFlg());
				pattern.setDescription(pat.getDescription());
				pattern.setPattern(pat.getPattern());
				pattern.setProcessType(pat.getProcessType());
				patterns.add(pattern);
			};
			pageInfo.getPatterns().clear();
			pageInfo.getPatterns().addAll(patterns);
			
			
			List<Variable> variables = new ArrayList<>();
			for(VariableValue val: page.getVariableValue()){
				variable = new Variable();
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