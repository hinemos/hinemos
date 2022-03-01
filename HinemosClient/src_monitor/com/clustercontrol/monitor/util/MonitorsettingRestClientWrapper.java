/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.monitor.util;

import java.util.List;

import org.openapitools.client.model.*;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.fault.MonitorIdInvalid;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestUrlSequentialExecuter;

public class MonitorsettingRestClientWrapper {

	private RestConnectUnit connectUnit;
	
	private final RestKind restKind = RestKind.MonitorsettingRestEndpoints;

	public static MonitorsettingRestClientWrapper getWrapper(String managerName) {
		return new MonitorsettingRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public MonitorsettingRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}

	public List<MonitorInfoResponse> getMonitorList()
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<MonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<MonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<MonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<MonitorInfoResponse> result =  apiClient.monitorsettingGetMonitorList();
				return result;
			}
		};
		try {
			return (List<MonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<MonitorInfoResponse> getMonitorListByCondition(GetMonitorListRequest getMonitorListRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidSetting, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<MonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<MonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<MonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<MonitorInfoResponse> result =  apiClient.monitorsettingGetMonitorListByCondition(getMonitorListRequest);
				return result;
			}
		};
		try {
			return (List<MonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidSetting | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<MonitorInfoBeanResponse> getMonitorBeanList()
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<MonitorInfoBeanResponse>> proxy = new RestUrlSequentialExecuter<List<MonitorInfoBeanResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<MonitorInfoBeanResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<MonitorInfoBeanResponse> result =  apiClient.monitorsettingGetMonitorBeanList();
				return result;
			}
		};
		try {
			return (List<MonitorInfoBeanResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<MonitorInfoBeanResponse> getMonitorBeanListByCondition(GetMonitorBeanListRequest getMonitorBeanListRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidSetting, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<MonitorInfoBeanResponse>> proxy = new RestUrlSequentialExecuter<List<MonitorInfoBeanResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<MonitorInfoBeanResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<MonitorInfoBeanResponse> result = null;
				GetMonitorBeanListResponse dtoRes = apiClient.monitorsettingGetMonitorBeanListByCondition(getMonitorBeanListRequest);
				if (dtoRes != null) {
					result = dtoRes.getMonitorInfoList();
				}
				return result;
			}
		};
		try {
			return (List<MonitorInfoBeanResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidSetting | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<MonitorInfoResponseP1> getStringMonitoInfoList(String facilityId, String ownerRoleId)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<MonitorInfoResponseP1>> proxy = new RestUrlSequentialExecuter<List<MonitorInfoResponseP1>>(this.connectUnit, this.restKind){
			@Override
			public List<MonitorInfoResponseP1> executeMethod(DefaultApi apiClient) throws Exception {
				List<MonitorInfoResponseP1> result =  apiClient.monitorsettingGetStringMonitoInfoList(facilityId, ownerRoleId);
				return result;
			}
		};
		try {
			return (List<MonitorInfoResponseP1>) proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public HttpScenarioMonitorInfoResponse addHttpScenarioMonitor(AddHttpScenarioMonitorRequest addHttpScenarioMonitorRequest)
			throws RestConnectFailed, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<HttpScenarioMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<HttpScenarioMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public HttpScenarioMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				HttpScenarioMonitorInfoResponse result =  apiClient.monitorsettingAddHttpScenarioMonitor(addHttpScenarioMonitorRequest);
				return result;
			}
		};
		try {
			return (HttpScenarioMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public HttpNumericMonitorInfoResponse addHttpNumericMonitor(AddHttpNumericMonitorRequest addHttpNumericMonitorRequest)
			throws RestConnectFailed, MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<HttpNumericMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<HttpNumericMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public HttpNumericMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				HttpNumericMonitorInfoResponse result =  apiClient.monitorsettingAddHttpNumericMonitor(addHttpNumericMonitorRequest);
				return result;
			}
		};
		try {
			return (HttpNumericMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorIdInvalid | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public HttpStringMonitorInfoResponse addHttpStringMonitor(AddHttpStringMonitorRequest addHttpStringMonitorRequest)
			throws RestConnectFailed, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<HttpStringMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<HttpStringMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public HttpStringMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				HttpStringMonitorInfoResponse result =  apiClient.monitorsettingAddHttpStringMonitor(addHttpStringMonitorRequest);
				return result;
			}
		};
		try {
			return (HttpStringMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public AgentMonitorInfoResponse addAgentMonitor(AddAgentMonitorRequest addAgentMonitorRequest)
			throws RestConnectFailed, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<AgentMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<AgentMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public AgentMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				AgentMonitorInfoResponse result =  apiClient.monitorsettingAddAgentMonitor(addAgentMonitorRequest);
				return result;
			}
		};
		try {
			return (AgentMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JmxMonitorInfoResponse addJmxMonitor(AddJmxMonitorRequest addJmxMonitorRequest)
			throws RestConnectFailed, MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<JmxMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<JmxMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public JmxMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				JmxMonitorInfoResponse result =  apiClient.monitorsettingAddJmxMonitor(addJmxMonitorRequest);
				return result;
			}
		};
		try {
			return (JmxMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorIdInvalid | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public PingMonitorInfoResponse addPingMonitor(AddPingMonitorRequest addPingMonitorRequest)
			throws RestConnectFailed, MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<PingMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<PingMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public PingMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				PingMonitorInfoResponse result =  apiClient.monitorsettingAddPingMonitor(addPingMonitorRequest);
				return result;
			}
		};
		try {
			return (PingMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorIdInvalid | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public SnmptrapMonitorInfoResponse addSnmptrapMonitor(AddSnmptrapMonitorRequest addSnmptrapMonitorRequest)
			throws RestConnectFailed, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<SnmptrapMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<SnmptrapMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public SnmptrapMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				SnmptrapMonitorInfoResponse result =  apiClient.monitorsettingAddSnmptrapMonitor(addSnmptrapMonitorRequest);
				return result;
			}
		};
		try {
			return (SnmptrapMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public SnmpNumericMonitorInfoResponse addSnmpNumericMonitor(AddSnmpNumericMonitorRequest addSnmpNumericMonitorRequest)
			throws RestConnectFailed, MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<SnmpNumericMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<SnmpNumericMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public SnmpNumericMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				SnmpNumericMonitorInfoResponse result =  apiClient.monitorsettingAddSnmpNumericMonitor(addSnmpNumericMonitorRequest);
				return result;
			}
		};
		try {
			return (SnmpNumericMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorIdInvalid | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public SnmpStringMonitorInfoResponse addSnmpStringMonitor(AddSnmpStringMonitorRequest addSnmpStringMonitorRequest)
			throws RestConnectFailed, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<SnmpStringMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<SnmpStringMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public SnmpStringMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				SnmpStringMonitorInfoResponse result =  apiClient.monitorsettingAddSnmpStringMonitor(addSnmpStringMonitorRequest);
				return result;
			}
		};
		try {
			return (SnmpStringMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public SqlNumericMonitorInfoResponse addSqlNumericMonitor(AddSqlNumericMonitorRequest addSqlNumericMonitorRequest)
			throws RestConnectFailed, MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<SqlNumericMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<SqlNumericMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public SqlNumericMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				SqlNumericMonitorInfoResponse result =  apiClient.monitorsettingAddSqlNumericMonitor(addSqlNumericMonitorRequest);
				return result;
			}
		};
		try {
			return (SqlNumericMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorIdInvalid | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public SqlStringMonitorInfoResponse addSqlStringMonitor(AddSqlStringMonitorRequest addSqlStringMonitorRequest)
			throws RestConnectFailed, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<SqlStringMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<SqlStringMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public SqlStringMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				SqlStringMonitorInfoResponse result =  apiClient.monitorsettingAddSqlStringMonitor(addSqlStringMonitorRequest);
				return result;
			}
		};
		try {
			return (SqlStringMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public WineventMonitorInfoResponse addWineventMonitor(AddWineventMonitorRequest addWineventMonitorRequest)
			throws RestConnectFailed, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<WineventMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<WineventMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public WineventMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				WineventMonitorInfoResponse result =  apiClient.monitorsettingAddWineventMonitor(addWineventMonitorRequest);
				return result;
			}
		};
		try {
			return (WineventMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public WinserviceMonitorInfoResponse addWinserviceMonitor(AddWinserviceMonitorRequest addWinserviceMonitorRequest)
			throws RestConnectFailed, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<WinserviceMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<WinserviceMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public WinserviceMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				WinserviceMonitorInfoResponse result =  apiClient.monitorsettingAddWinserviceMonitor(addWinserviceMonitorRequest);
				return result;
			}
		};
		try {
			return (WinserviceMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CustomtrapNumericMonitorInfoResponse addCustomtrapNumericMonitor(AddCustomtrapNumericMonitorRequest addCustomtrapNumericMonitorRequest)
			throws RestConnectFailed, MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<CustomtrapNumericMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<CustomtrapNumericMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public CustomtrapNumericMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CustomtrapNumericMonitorInfoResponse result =  apiClient.monitorsettingAddCustomtrapNumericMonitor(addCustomtrapNumericMonitorRequest);
				return result;
			}
		};
		try {
			return (CustomtrapNumericMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorIdInvalid | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CustomtrapStringMonitorInfoResponse addCustomtrapStringMonitor(AddCustomtrapStringMonitorRequest addCustomtrapStringMonitorRequest)
			throws RestConnectFailed, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<CustomtrapStringMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<CustomtrapStringMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public CustomtrapStringMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CustomtrapStringMonitorInfoResponse result =  apiClient.monitorsettingAddCustomtrapStringMonitor(addCustomtrapStringMonitorRequest);
				return result;
			}
		};
		try {
			return (CustomtrapStringMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CustomNumericMonitorInfoResponse addCustomNumericMonitor(AddCustomNumericMonitorRequest addCustomNumericMonitorRequest)
			throws RestConnectFailed, MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<CustomNumericMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<CustomNumericMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public CustomNumericMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CustomNumericMonitorInfoResponse result =  apiClient.monitorsettingAddCustomNumericMonitor(addCustomNumericMonitorRequest);
				return result;
			}
		};
		try {
			return (CustomNumericMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorIdInvalid | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CustomStringMonitorInfoResponse addCustomStringMonitor(AddCustomStringMonitorRequest addCustomStringMonitorRequest)
			throws RestConnectFailed, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<CustomStringMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<CustomStringMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public CustomStringMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CustomStringMonitorInfoResponse result =  apiClient.monitorsettingAddCustomStringMonitor(addCustomStringMonitorRequest);
				return result;
			}
		};
		try {
			return (CustomStringMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CloudserviceMonitorInfoResponse addCloudserviceMonitor(AddCloudserviceMonitorRequest addCloudserviceMonitorRequest)
			throws RestConnectFailed, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<CloudserviceMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<CloudserviceMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public CloudserviceMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CloudserviceMonitorInfoResponse result =  apiClient.monitorsettingAddCloudserviceMonitor(addCloudserviceMonitorRequest);
				return result;
			}
		};
		try {
			return (CloudserviceMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CloudserviceBillingMonitorInfoResponse addCloudservicebillingMonitor(AddCloudserviceBillingMonitorRequest addCloudserviceBillingMonitorRequest)
			throws RestConnectFailed, MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<CloudserviceBillingMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<CloudserviceBillingMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public CloudserviceBillingMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CloudserviceBillingMonitorInfoResponse result =  apiClient.monitorsettingAddCloudservicebillingMonitor(addCloudserviceBillingMonitorRequest);
				return result;
			}
		};
		try {
			return (CloudserviceBillingMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorIdInvalid | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CloudserviceBillingDetailMonitorInfoResponse addCloudservicebillingdetailMonitor(AddCloudserviceBillingDetailMonitorRequest addCloudserviceBillingDetailMonitorRequest)
			throws RestConnectFailed, MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<CloudserviceBillingDetailMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<CloudserviceBillingDetailMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public CloudserviceBillingDetailMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CloudserviceBillingDetailMonitorInfoResponse result =  apiClient.monitorsettingAddCloudservicebillingdetailMonitor(addCloudserviceBillingDetailMonitorRequest);
				return result;
			}
		};
		try {
			return (CloudserviceBillingDetailMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorIdInvalid | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ServiceportMonitorInfoResponse addServiceportMonitor(AddServiceportMonitorRequest addServiceportMonitorRequest)
			throws RestConnectFailed, MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<ServiceportMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<ServiceportMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public ServiceportMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				ServiceportMonitorInfoResponse result =  apiClient.monitorsettingAddServiceportMonitor(addServiceportMonitorRequest);
				return result;
			}
		};
		try {
			return (ServiceportMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorIdInvalid | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public SystemlogMonitorInfoResponse addSystemlogMonitor(AddSystemlogMonitorRequest addSystemlogMonitorRequest)
			throws RestConnectFailed, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<SystemlogMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<SystemlogMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public SystemlogMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				SystemlogMonitorInfoResponse result =  apiClient.monitorsettingAddSystemlogMonitor(addSystemlogMonitorRequest);
				return result;
			}
		};
		try {
			return (SystemlogMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public BinaryfileMonitorInfoResponse addBinaryfileMonitor(AddBinaryfileMonitorRequest addBinaryfileMonitorRequest)
			throws RestConnectFailed, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<BinaryfileMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<BinaryfileMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public BinaryfileMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				BinaryfileMonitorInfoResponse result =  apiClient.monitorsettingAddBinaryfileMonitor(addBinaryfileMonitorRequest);
				return result;
			}
		};
		try {
			return (BinaryfileMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public PacketcaptureMonitorInfoResponse addPacketcaptureMonitor(AddPacketcaptureMonitorRequest addPacketcaptureMonitorRequest)
			throws RestConnectFailed, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<PacketcaptureMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<PacketcaptureMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public PacketcaptureMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				PacketcaptureMonitorInfoResponse result =  apiClient.monitorsettingAddPacketcaptureMonitor(addPacketcaptureMonitorRequest);
				return result;
			}
		};
		try {
			return (PacketcaptureMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ProcessMonitorInfoResponse addProcessMonitor(AddProcessMonitorRequest addProcessMonitorRequest)
			throws RestConnectFailed, MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<ProcessMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<ProcessMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public ProcessMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				ProcessMonitorInfoResponse result =  apiClient.monitorsettingAddProcessMonitor(addProcessMonitorRequest);
				return result;
			}
		};
		try {
			return (ProcessMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorIdInvalid | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public PerformanceMonitorInfoResponse addPerformanceMonitor(AddPerformanceMonitorRequest addPerformanceMonitorRequest)
			throws RestConnectFailed, MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<PerformanceMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<PerformanceMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public PerformanceMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				PerformanceMonitorInfoResponse result =  apiClient.monitorsettingAddPerformanceMonitor(addPerformanceMonitorRequest);
				return result;
			}
		};
		try {
			return (PerformanceMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorIdInvalid | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public LogfileMonitorInfoResponse addLogfileMonitor(AddLogfileMonitorRequest AddLogfileMonitorRequest)
			throws RestConnectFailed, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<LogfileMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<LogfileMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public LogfileMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				LogfileMonitorInfoResponse result =  apiClient.monitorsettingAddLogfileMonitor(AddLogfileMonitorRequest);
				return result;
			}
		};
		try {
			return (LogfileMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public LogcountMonitorInfoResponse addLogcountMonitor(AddLogcountMonitorRequest addLogcountMonitorRequest)
			throws RestConnectFailed, MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<LogcountMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<LogcountMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public LogcountMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				LogcountMonitorInfoResponse result =  apiClient.monitorsettingAddLogcountMonitor(addLogcountMonitorRequest);
				return result;
			}
		};
		try {
			return (LogcountMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorIdInvalid | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CorrelationMonitorInfoResponse addCorrelationMonitor(AddCorrelationMonitorRequest addCorrelationMonitorRequest)
			throws RestConnectFailed, MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<CorrelationMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<CorrelationMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public CorrelationMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CorrelationMonitorInfoResponse result =  apiClient.monitorsettingAddCorrelationMonitor(addCorrelationMonitorRequest);
				return result;
			}
		};
		try {
			return (CorrelationMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorIdInvalid | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public IntegrationMonitorInfoResponse addIntegrationMonitor(AddIntegrationMonitorRequest addIntegrationMonitorRequest)
			throws RestConnectFailed, MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<IntegrationMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<IntegrationMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public IntegrationMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				IntegrationMonitorInfoResponse result =  apiClient.monitorsettingAddIntegrationMonitor(addIntegrationMonitorRequest);
				return result;
			}
		};
		try {
			return (IntegrationMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorIdInvalid | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public CloudLogMonitorInfoResponse addCloudLogMonitor(AddCloudLogMonitorRequest addCloudLogMonitorRequest)
			throws RestConnectFailed, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<CloudLogMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<CloudLogMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public CloudLogMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CloudLogMonitorInfoResponse result =  apiClient.monitorsettingAddCloudLogMonitor(addCloudLogMonitorRequest);
				return result;
			}
		};
		try {
			return (CloudLogMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public RpaLogfileMonitorInfoResponse addRpaLogFileMonitor(AddRpaLogfileMonitorRequest request)
			throws RestConnectFailed, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<RpaLogfileMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<RpaLogfileMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public RpaLogfileMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				RpaLogfileMonitorInfoResponse result =  apiClient.monitorsettingAddRpaLogfileMonitor(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public RpaManagementToolMonitorInfoResponse addRpaManagementToolMonitor(AddRpaManagementToolMonitorRequest request)
			throws RestConnectFailed, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<RpaManagementToolMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<RpaManagementToolMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public RpaManagementToolMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				RpaManagementToolMonitorInfoResponse result =  apiClient.monitorsettingAddRpaManagementToolMonitor(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorDuplicate | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public HttpScenarioMonitorInfoResponse modifyHttpScenarioMonitor(String monitorId, ModifyHttpScenarioMonitorRequest modifyHttpScenarioMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<HttpScenarioMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<HttpScenarioMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public HttpScenarioMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				HttpScenarioMonitorInfoResponse result =  apiClient.monitorsettingModifyHttpScenarioMonitor(monitorId, modifyHttpScenarioMonitorRequest);
				return result;
			}
		};
		try {
			return (HttpScenarioMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public HttpNumericMonitorInfoResponse modifyHttpNumericMonitor(String monitorId, ModifyHttpNumericMonitorRequest modifyHttpNumericMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<HttpNumericMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<HttpNumericMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public HttpNumericMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				HttpNumericMonitorInfoResponse result =  apiClient.monitorsettingModifyHttpNumericMonitor(monitorId, modifyHttpNumericMonitorRequest);
				return result;
			}
		};
		try {
			return (HttpNumericMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public HttpStringMonitorInfoResponse modifyHttpStringMonitor(String monitorId, ModifyHttpStringMonitorRequest modifyHttpStringMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<HttpStringMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<HttpStringMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public HttpStringMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				HttpStringMonitorInfoResponse result =  apiClient.monitorsettingModifyHttpStringMonitor(monitorId, modifyHttpStringMonitorRequest);
				return result;
			}
		};
		try {
			return (HttpStringMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public AgentMonitorInfoResponse modifyAgentMonitor(String monitorId, ModifyAgentMonitorRequest modifyAgentMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<AgentMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<AgentMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public AgentMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				AgentMonitorInfoResponse result =  apiClient.monitorsettingModifyAgentMonitor(monitorId, modifyAgentMonitorRequest);
				return result;
			}
		};
		try {
			return (AgentMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JmxMonitorInfoResponse modifyJmxMonitor(String monitorId, ModifyJmxMonitorRequest modifyJmxMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<JmxMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<JmxMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public JmxMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				JmxMonitorInfoResponse result =  apiClient.monitorsettingModifyJmxMonitor(monitorId, modifyJmxMonitorRequest);
				return result;
			}
		};
		try {
			return (JmxMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public PingMonitorInfoResponse modifyPingMonitor(String monitorId, ModifyPingMonitorRequest modifyPingMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<PingMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<PingMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public PingMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				PingMonitorInfoResponse result =  apiClient.monitorsettingModifyPingMonitor(monitorId, modifyPingMonitorRequest);
				return result;
			}
		};
		try {
			return (PingMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public SnmptrapMonitorInfoResponse modifySnmptrapMonitor(String monitorId, ModifySnmptrapMonitorRequest modifySnmptrapMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<SnmptrapMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<SnmptrapMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public SnmptrapMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				SnmptrapMonitorInfoResponse result =  apiClient.monitorsettingModifySnmptrapMonitor(monitorId, modifySnmptrapMonitorRequest);
				return result;
			}
		};
		try {
			return (SnmptrapMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public SnmpNumericMonitorInfoResponse modifySnmpNumericMonitor(String monitorId, ModifySnmpNumericMonitorRequest modifySnmpNumericMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<SnmpNumericMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<SnmpNumericMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public SnmpNumericMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				SnmpNumericMonitorInfoResponse result =  apiClient.monitorsettingModifySnmpNumericMonitor(monitorId, modifySnmpNumericMonitorRequest);
				return result;
			}
		};
		try {
			return (SnmpNumericMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

 	public SnmpStringMonitorInfoResponse modifySnmpStringMonitor(String monitorId, ModifySnmpStringMonitorRequest modifySnmpStringMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<SnmpStringMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<SnmpStringMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public SnmpStringMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				SnmpStringMonitorInfoResponse result =  apiClient.monitorsettingModifySnmpStringMonitor(monitorId, modifySnmpStringMonitorRequest);
				return result;
			}
		};
		try {
			return (SnmpStringMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public SqlNumericMonitorInfoResponse modifySqlNumericMonitor(String monitorId, ModifySqlNumericMonitorRequest modifySqlNumericMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<SqlNumericMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<SqlNumericMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public SqlNumericMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				SqlNumericMonitorInfoResponse result =  apiClient.monitorsettingModifySqlNumericMonitor(monitorId, modifySqlNumericMonitorRequest);
				return result;
			}
		};
		try {
			return (SqlNumericMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public SqlStringMonitorInfoResponse modifySqlStringMonitor(String monitorId, ModifySqlStringMonitorRequest modifySqlStringMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<SqlStringMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<SqlStringMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public SqlStringMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				SqlStringMonitorInfoResponse result =  apiClient.monitorsettingModifySqlStringMonitor(monitorId, modifySqlStringMonitorRequest);
				return result;
			}
		};
		try {
			return (SqlStringMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public WineventMonitorInfoResponse modifyWineventMonitor(String monitorId, ModifyWineventMonitorRequest modifyWineventMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<WineventMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<WineventMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public WineventMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				WineventMonitorInfoResponse result =  apiClient.monitorsettingModifyWineventMonitor(monitorId, modifyWineventMonitorRequest);
				return result;
			}
		};
		try {
			return (WineventMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public WinserviceMonitorInfoResponse modifyWinserviceMonitor(String monitorId, ModifyWinserviceMonitorRequest modifyWinserviceMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<WinserviceMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<WinserviceMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public WinserviceMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				WinserviceMonitorInfoResponse result =  apiClient.monitorsettingModifyWinserviceMonitor(monitorId, modifyWinserviceMonitorRequest);
				return result;
			}
		};
		try {
			return (WinserviceMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CustomtrapNumericMonitorInfoResponse modifyCustomtrapNumericMonitor(String monitorId, ModifyCustomtrapNumericMonitorRequest modifyCustomtrapNumericMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<CustomtrapNumericMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<CustomtrapNumericMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public CustomtrapNumericMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CustomtrapNumericMonitorInfoResponse result =  apiClient.monitorsettingModifyCustomtrapNumericMonitor(monitorId, modifyCustomtrapNumericMonitorRequest);
				return result;
			}
		};
		try {
			return (CustomtrapNumericMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CustomtrapStringMonitorInfoResponse modifyCustomtrapStringMonitor(String monitorId, ModifyCustomtrapStringMonitorRequest modifyCustomtrapStringMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<CustomtrapStringMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<CustomtrapStringMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public CustomtrapStringMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CustomtrapStringMonitorInfoResponse result =  apiClient.monitorsettingModifyCustomtrapStringMonitor(monitorId, modifyCustomtrapStringMonitorRequest);
				return result;
			}
		};
		try {
			return (CustomtrapStringMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CustomNumericMonitorInfoResponse modifyCustomNumericMonitor(String monitorId, ModifyCustomNumericMonitorRequest modifyCustomNumericMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<CustomNumericMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<CustomNumericMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public CustomNumericMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CustomNumericMonitorInfoResponse result =  apiClient.monitorsettingModifyCustomNumericMonitor(monitorId, modifyCustomNumericMonitorRequest);
				return result;
			}
		};
		try {
			return (CustomNumericMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CustomStringMonitorInfoResponse modifyCustomStringMonitor(String monitorId, ModifyCustomStringMonitorRequest modifyCustomStringMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<CustomStringMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<CustomStringMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public CustomStringMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CustomStringMonitorInfoResponse result =  apiClient.monitorsettingModifyCustomStringMonitor(monitorId, modifyCustomStringMonitorRequest);
				return result;
			}
		};
		try {
			return (CustomStringMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CloudserviceMonitorInfoResponse modifyCloudserviceMonitor(String monitorId, ModifyCloudserviceMonitorRequest modifyCloudserviceMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<CloudserviceMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<CloudserviceMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public CloudserviceMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CloudserviceMonitorInfoResponse result =  apiClient.monitorsettingModifyCloudserviceMonitor(monitorId, modifyCloudserviceMonitorRequest);
				return result;
			}
		};
		try {
			return (CloudserviceMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public CloudLogMonitorInfoResponse modifyCloudLogMonitor(String monitorId, ModifyCloudLogMonitorRequest modifyCloudLogMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<CloudLogMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<CloudLogMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public CloudLogMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CloudLogMonitorInfoResponse result =  apiClient.monitorsettingModifyCloudLogMonitor(monitorId, modifyCloudLogMonitorRequest);
				return result;
			}
		};
		try {
			return (CloudLogMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CloudserviceBillingMonitorInfoResponse modifyCloudservicebillingMonitor(String monitorId, ModifyCloudserviceBillingMonitorRequest modifyCloudserviceBillingMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		RestUrlSequentialExecuter<CloudserviceBillingMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<CloudserviceBillingMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public CloudserviceBillingMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CloudserviceBillingMonitorInfoResponse result =  apiClient.monitorsettingModifyCloudservicebillingMonitor(monitorId, modifyCloudserviceBillingMonitorRequest);
				return result;
			}
		};
		try {
			return (CloudserviceBillingMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CloudserviceBillingDetailMonitorInfoResponse modifyCloudservicebillingdetailMonitor(String monitorId, ModifyCloudserviceBillingDetailMonitorRequest modifyCloudserviceBillingDetailMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<CloudserviceBillingDetailMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<CloudserviceBillingDetailMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public CloudserviceBillingDetailMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CloudserviceBillingDetailMonitorInfoResponse result =  apiClient.monitorsettingModifyCloudservicebillingdetailMonitor(monitorId, modifyCloudserviceBillingDetailMonitorRequest);
				return result;
			}
		};
		try {
			return (CloudserviceBillingDetailMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ServiceportMonitorInfoResponse modifyServiceportMonitor(String monitorId, ModifyServiceportMonitorRequest modifyServiceportMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<ServiceportMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<ServiceportMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public ServiceportMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				ServiceportMonitorInfoResponse result =  apiClient.monitorsettingModifyServiceportMonitor(monitorId, modifyServiceportMonitorRequest);
				return result;
			}
		};
		try {
			return (ServiceportMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public SystemlogMonitorInfoResponse modifySystemlogMonitor(String monitorId, ModifySystemlogMonitorRequest modifySystemlogMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<SystemlogMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<SystemlogMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public SystemlogMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				SystemlogMonitorInfoResponse result =  apiClient.monitorsettingModifySystemlogMonitor(monitorId, modifySystemlogMonitorRequest);
				return result;
			}
		};
		try {
			return (SystemlogMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public BinaryfileMonitorInfoResponse modifyBinaryfileMonitor(String monitorId, ModifyBinaryfileMonitorRequest modifyBinaryfileMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<BinaryfileMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<BinaryfileMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public BinaryfileMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				BinaryfileMonitorInfoResponse result =  apiClient.monitorsettingModifyBinaryfileMonitor(monitorId, modifyBinaryfileMonitorRequest);
				return result;
			}
		};
		try {
			return (BinaryfileMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public PacketcaptureMonitorInfoResponse modifyPacketcaptureMonitor(String monitorId, ModifyPacketcaptureMonitorRequest modifyPacketcaptureMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<PacketcaptureMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<PacketcaptureMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public PacketcaptureMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				PacketcaptureMonitorInfoResponse result =  apiClient.monitorsettingModifyPacketcaptureMonitor(monitorId, modifyPacketcaptureMonitorRequest);
				return result;
			}
		};
		try {
			return (PacketcaptureMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ProcessMonitorInfoResponse modifyProcessMonitor(String monitorId, ModifyProcessMonitorRequest modifyProcessMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<ProcessMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<ProcessMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public ProcessMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				ProcessMonitorInfoResponse result =  apiClient.monitorsettingModifyProcessMonitor(monitorId, modifyProcessMonitorRequest);
				return result;
			}
		};
		try {
			return (ProcessMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public PerformanceMonitorInfoResponse modifyPerformanceMonitor(String monitorId, ModifyPerformanceMonitorRequest modifyPerformanceMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<PerformanceMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<PerformanceMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public PerformanceMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				PerformanceMonitorInfoResponse result =  apiClient.monitorsettingModifyPerformanceMonitor(monitorId, modifyPerformanceMonitorRequest);
				return result;
			}
		};
		try {
			return (PerformanceMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public LogfileMonitorInfoResponse modifyLogfileMonitor(String monitorId, ModifyLogfileMonitorRequest modifyLogfileMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<LogfileMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<LogfileMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public LogfileMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				LogfileMonitorInfoResponse result =  apiClient.monitorsettingModifyLogfileMonitor(monitorId, modifyLogfileMonitorRequest);
				return result;
			}
		};
		try {
			return (LogfileMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public LogcountMonitorInfoResponse modifyLogcountMonitor(String monitorId, ModifyLogcountMonitorRequest modifyLogcountMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<LogcountMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<LogcountMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public LogcountMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				LogcountMonitorInfoResponse result =  apiClient.monitorsettingModifyLogcountMonitor(monitorId, modifyLogcountMonitorRequest);
				return result;
			}
		};
		try {
			return (LogcountMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public CorrelationMonitorInfoResponse modifyCorrelationMonitor(String monitorId, ModifyCorrelationMonitorRequest modifyCorrelationMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<CorrelationMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<CorrelationMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public CorrelationMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				CorrelationMonitorInfoResponse result =  apiClient.monitorsettingModifyCorrelationMonitor(monitorId, modifyCorrelationMonitorRequest);
				return result;
			}
		};
		try {
			return (CorrelationMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public IntegrationMonitorInfoResponse modifyIntegrationMonitor(String monitorId, ModifyIntegrationMonitorRequest modifyIntegrationMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<IntegrationMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<IntegrationMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public IntegrationMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				IntegrationMonitorInfoResponse result =  apiClient.monitorsettingModifyIntegrationMonitor(monitorId, modifyIntegrationMonitorRequest);
				return result;
			}
		};
		try {
			return (IntegrationMonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public RpaLogfileMonitorInfoResponse modifyRpaLogfileMonitorInfo(String monitorId, ModifyRpaLogfileMonitorRequest request)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<RpaLogfileMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<RpaLogfileMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public RpaLogfileMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				RpaLogfileMonitorInfoResponse result =  apiClient.monitorsettingModifyRpaLogfileMonitor(monitorId, request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public RpaManagementToolMonitorInfoResponse modifyRpaManagementToolMonitorInfo(String monitorId, ModifyRpaManagementToolMonitorRequest request)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<RpaManagementToolMonitorInfoResponse> proxy = new RestUrlSequentialExecuter<RpaManagementToolMonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public RpaManagementToolMonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				RpaManagementToolMonitorInfoResponse result =  apiClient.monitorsettingModifyRpaManagementToolMonitor(monitorId, request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<MonitorInfoBeanResponse> deleteMonitor(String monitorIds)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RestUrlSequentialExecuter<List<MonitorInfoBeanResponse>> proxy = new RestUrlSequentialExecuter<List<MonitorInfoBeanResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<MonitorInfoBeanResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<MonitorInfoBeanResponse> result =  apiClient.monitorsettingDeleteMonitor(monitorIds);
				return result;
			}
		};
		try {
			return (List<MonitorInfoBeanResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public MonitorInfoResponse getMonitor(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<MonitorInfoResponse> proxy = new RestUrlSequentialExecuter<MonitorInfoResponse>(this.connectUnit, this.restKind){
			@Override
			public MonitorInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				MonitorInfoResponse result =  apiClient.monitorsettingGetMonitor(monitorId);
				return result;
			}
		};
		try {
			return (MonitorInfoResponse) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public MonitorInfoResponseP3 getMonitorInfoForGraph(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<MonitorInfoResponseP3> proxy = new RestUrlSequentialExecuter<MonitorInfoResponseP3>(this.connectUnit, this.restKind){
			@Override
			public MonitorInfoResponseP3 executeMethod(DefaultApi apiClient) throws Exception {
				MonitorInfoResponseP3 result =  apiClient.monitorsettingGetMonitorInfoForGraph(monitorId);
				return result;
			}
		};
		try {
			return (MonitorInfoResponseP3) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<MonitorInfoBeanResponse> setStatusMonitor(SetStatusMonitorRequest setStatusMonitorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidSetting, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<MonitorInfoBeanResponse>> proxy = new RestUrlSequentialExecuter<List<MonitorInfoBeanResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<MonitorInfoBeanResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<MonitorInfoBeanResponse> result =  apiClient.monitorsettingSetStatusMonitor(setStatusMonitorRequest);
				return result;
			}
		};
		try {
			return (List<MonitorInfoBeanResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidSetting | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<MonitorInfoBeanResponse> setStatusCollector(SetStatusCollectorRequest setStatusCollectorRequest)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidSetting, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<MonitorInfoBeanResponse>> proxy = new RestUrlSequentialExecuter<List<MonitorInfoBeanResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<MonitorInfoBeanResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<MonitorInfoBeanResponse> result =  apiClient.monitorsettingSetStatusCollector(setStatusCollectorRequest);
				return result;
			}
		};
		try {
			return (List<MonitorInfoBeanResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidSetting | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JdbcDriverInfoResponse> getJdbcDriverList()
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<JdbcDriverInfoResponse>> proxy = new RestUrlSequentialExecuter<List<JdbcDriverInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<JdbcDriverInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<JdbcDriverInfoResponse> result =  apiClient.monitorsettingGetJdbcDriverList();
				return result;
			}
		};
		try {
			return (List<JdbcDriverInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole| HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<AgentMonitorInfoResponse> getAgentList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<AgentMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<AgentMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<AgentMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<AgentMonitorInfoResponse> result =  apiClient.monitorsettingGetAgentList(monitorId);
				return result;
			}
		};
		try {
			return (List<AgentMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<HttpScenarioMonitorInfoResponse> getHttpScenarioList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<HttpScenarioMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<HttpScenarioMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<HttpScenarioMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				 List<HttpScenarioMonitorInfoResponse> result =  apiClient.monitorsettingGetHttpScenarioList(monitorId);
				return result;
			}
		};
		try {
			return (List<HttpScenarioMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<HttpNumericMonitorInfoResponse> getHttpNumericList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<HttpNumericMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<HttpNumericMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<HttpNumericMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<HttpNumericMonitorInfoResponse> result =  apiClient.monitorsettingGetHttpNumericList(monitorId);
				return result;
			}
		};
		try {
			return (List<HttpNumericMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<HttpStringMonitorInfoResponse> getHttpStringList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<HttpStringMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<HttpStringMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<HttpStringMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<HttpStringMonitorInfoResponse> result =  apiClient.monitorsettingGetHttpStringList(monitorId);
				return result;
			}
		};
		try {
			return (List<HttpStringMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JmxMonitorInfoResponse> getJmxList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<JmxMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<JmxMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<JmxMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<JmxMonitorInfoResponse> result =  apiClient.monitorsettingGetJmxList(monitorId);
				return result;
			}
		};
		try {
			return (List<JmxMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<LogfileMonitorInfoResponse> getLogfileList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<LogfileMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<LogfileMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<LogfileMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<LogfileMonitorInfoResponse> result =  apiClient.monitorsettingGetLogfileList(monitorId);
				return result;
			}
		};
		try {
			return (List<LogfileMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<PerformanceMonitorInfoResponse> getPerformanceList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<PerformanceMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<PerformanceMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<PerformanceMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<PerformanceMonitorInfoResponse> result =  apiClient.monitorsettingGetPerformanceList(monitorId);
				return result;
			}
		};
		try {
			return (List<PerformanceMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<PingMonitorInfoResponse> getPingList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<PingMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<PingMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<PingMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<PingMonitorInfoResponse> result =  apiClient.monitorsettingGetPingList(monitorId);
				return result;
			}
		};
		try {
			return (List<PingMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<ServiceportMonitorInfoResponse> getPortList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<ServiceportMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<ServiceportMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<ServiceportMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<ServiceportMonitorInfoResponse> result =  apiClient.monitorsettingGetPortList(monitorId);
				return result;
			}
		};
		try {
			return (List<ServiceportMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<ProcessMonitorInfoResponse> getProcessList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<ProcessMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<ProcessMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<ProcessMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<ProcessMonitorInfoResponse> result =  apiClient.monitorsettingGetProcessList(monitorId);
				return result;
			}
		};
		try {
			return (List<ProcessMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<SnmptrapMonitorInfoResponse> getSnmptrapList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<SnmptrapMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<SnmptrapMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<SnmptrapMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<SnmptrapMonitorInfoResponse> result =  apiClient.monitorsettingGetSnmptrapList(monitorId);
				return result;
			}
		};
		try {
			return (List<SnmptrapMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<SnmpNumericMonitorInfoResponse> getSnmpNumericList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<SnmpNumericMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<SnmpNumericMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<SnmpNumericMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<SnmpNumericMonitorInfoResponse> result =  apiClient.monitorsettingGetSnmpNumericList(monitorId);
				return result;
			}
		};
		try {
			return (List<SnmpNumericMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<SnmpStringMonitorInfoResponse> getSnmpStringList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<SnmpStringMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<SnmpStringMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<SnmpStringMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<SnmpStringMonitorInfoResponse> result =  apiClient.monitorsettingGetSnmpStringList(monitorId);
				return result;
			}
		};
		try {
			return (List<SnmpStringMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<SqlNumericMonitorInfoResponse> getSqlNumericList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<SqlNumericMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<SqlNumericMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<SqlNumericMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<SqlNumericMonitorInfoResponse> result =  apiClient.monitorsettingGetSqlNumericList(monitorId);
				return result;
			}
		};
		try {
			return (List<SqlNumericMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<SqlStringMonitorInfoResponse> getSqlStringList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<SqlStringMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<SqlStringMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<SqlStringMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<SqlStringMonitorInfoResponse> result =  apiClient.monitorsettingGetSqlStringList(monitorId);
				return result;
			}
		};
		try {
			return (List<SqlStringMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<SystemlogMonitorInfoResponse> getSystemlogList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<SystemlogMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<SystemlogMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<SystemlogMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<SystemlogMonitorInfoResponse> result =  apiClient.monitorsettingGetSystemlogList(monitorId);
				return result;
			}
		};
		try {
			return (List<SystemlogMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CustomNumericMonitorInfoResponse> getCustomNumericList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<CustomNumericMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CustomNumericMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<CustomNumericMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CustomNumericMonitorInfoResponse> result =  apiClient.monitorsettingGetCustomNumericList(monitorId);
				return result;
			}
		};
		try {
			return (List<CustomNumericMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CustomStringMonitorInfoResponse> getCustomStringList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<CustomStringMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CustomStringMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<CustomStringMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CustomStringMonitorInfoResponse> result =  apiClient.monitorsettingGetCustomStringList(monitorId);
				return result;
			}
		};
		try {
			return (List<CustomStringMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<WinserviceMonitorInfoResponse> getWinServiceList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<WinserviceMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<WinserviceMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<WinserviceMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<WinserviceMonitorInfoResponse> result =  apiClient.monitorsettingGetWinServiceList(monitorId);
				return result;
			}
		};
		try {
			return (List<WinserviceMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<WineventMonitorInfoResponse> getWinEventList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<WineventMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<WineventMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<WineventMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<WineventMonitorInfoResponse> result =  apiClient.monitorsettingGetWinEventList(monitorId);
				return result;
			}
		};
		try {
			return (List<WineventMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CustomtrapNumericMonitorInfoResponse> getCustomtrapNumericList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<CustomtrapNumericMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CustomtrapNumericMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<CustomtrapNumericMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CustomtrapNumericMonitorInfoResponse> result =  apiClient.monitorsettingGetCustomtrapNumericList(monitorId);
				return result;
			}
		};
		try {
			return (List<CustomtrapNumericMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CustomtrapStringMonitorInfoResponse> getCustomtrapStringList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<CustomtrapStringMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CustomtrapStringMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<CustomtrapStringMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CustomtrapStringMonitorInfoResponse> result =  apiClient.monitorsettingGetCustomtrapStringList(monitorId);
				return result;
			}
		};
		try {
			return (List<CustomtrapStringMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<MonitorInfoBeanResponse> getStringAndTrapMonitorInfoList(String facilityId, String ownerRoleId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<MonitorInfoBeanResponse>> proxy = new RestUrlSequentialExecuter<List<MonitorInfoBeanResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<MonitorInfoBeanResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<MonitorInfoBeanResponse> result =  apiClient.monitorsettingGetStringAndTrapMonitorInfoList(facilityId, ownerRoleId);
				return result;
			}
		};
		try {
			return (List<MonitorInfoBeanResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CloudserviceMonitorInfoResponse> getCloudServiceList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<CloudserviceMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CloudserviceMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<CloudserviceMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CloudserviceMonitorInfoResponse> result =  apiClient.monitorsettingGetCloudServiceList(monitorId);
				return result;
			}
		};
		try {
			return (List<CloudserviceMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CloudserviceBillingMonitorInfoResponse> getCloudserviceBillingList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<CloudserviceBillingMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CloudserviceBillingMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<CloudserviceBillingMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CloudserviceBillingMonitorInfoResponse> result =  apiClient.monitorsettingGetCloudserviceBillingList(monitorId);
				return result;
			}
		};
		try {
			return (List<CloudserviceBillingMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CloudserviceBillingDetailMonitorInfoResponse> getCloudserviceBillingDetailList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<CloudserviceBillingDetailMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CloudserviceBillingDetailMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<CloudserviceBillingDetailMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CloudserviceBillingDetailMonitorInfoResponse> result =  apiClient.monitorsettingGetCloudserviceBillingDetailList(monitorId);
				return result;
			}
		};
		try {
			return (List<CloudserviceBillingDetailMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CloudLogMonitorInfoResponse> getCloudLogList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<CloudLogMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CloudLogMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<CloudLogMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CloudLogMonitorInfoResponse> result =  apiClient.monitorsettingGetCloudLogList(monitorId);
				return result;
			}
		};
		try {
			return (List<CloudLogMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<BinaryfileMonitorInfoResponse> getBinaryFileList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<BinaryfileMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<BinaryfileMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<BinaryfileMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<BinaryfileMonitorInfoResponse> result =  apiClient.monitorsettingGetBinaryFileList(monitorId);
				return result;
			}
		};
		try {
			return (List<BinaryfileMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<PacketcaptureMonitorInfoResponse> getPacketCaptureList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<PacketcaptureMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<PacketcaptureMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<PacketcaptureMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<PacketcaptureMonitorInfoResponse> result =  apiClient.monitorsettingGetPacketCaptureList(monitorId);
				return result;
			}
		};
		try {
			return (List<PacketcaptureMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CorrelationMonitorInfoResponse> getCorrelationList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<CorrelationMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CorrelationMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<CorrelationMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<CorrelationMonitorInfoResponse> result =  apiClient.monitorsettingGetCorrelationList(monitorId);
				return result;
			}
		};
		try {
			return (List<CorrelationMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<IntegrationMonitorInfoResponse> getIntegrationList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<IntegrationMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<IntegrationMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<IntegrationMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<IntegrationMonitorInfoResponse> result =  apiClient.monitorsettingGetIntegrationList(monitorId);
				return result;
			}
		};
		try {
			return (List<IntegrationMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<LogcountMonitorInfoResponse> getLogcountList(String monitorId)
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<LogcountMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<LogcountMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<LogcountMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<LogcountMonitorInfoResponse> result =  apiClient.monitorsettingGetLogcountList(monitorId);
				return result;
			}
		};
		try {
			return (List<LogcountMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JmxMasterInfoResponse> addJmxMasterList(AddJmxMasterListRequest addJmxMasterListRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {

		RestUrlSequentialExecuter<List<JmxMasterInfoResponse>> proxy = new RestUrlSequentialExecuter<List<JmxMasterInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<JmxMasterInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<JmxMasterInfoResponse> result =  apiClient.monitorsettingAddJmxMasterList(addJmxMasterListRequest);
				return result;
			}
		};
		try {
			return (List<JmxMasterInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JmxMasterInfoResponse> deleteJmxMasterAll()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {

		RestUrlSequentialExecuter<List<JmxMasterInfoResponse>> proxy = new RestUrlSequentialExecuter<List<JmxMasterInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<JmxMasterInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<JmxMasterInfoResponse> result =  apiClient.monitorsettingDeleteJmxMasterAll();
				return result;
			}
		};
		try {
			return (List<JmxMasterInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JmxMasterInfoResponse> deleteJmxMaster(String jmxMasterIds)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {

		RestUrlSequentialExecuter<List<JmxMasterInfoResponse>> proxy = new RestUrlSequentialExecuter<List<JmxMasterInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<JmxMasterInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<JmxMasterInfoResponse> result =  apiClient.monitorsettingDeleteJmxMaster(jmxMasterIds);
				return result;
			}
		};
		try {
			return (List<JmxMasterInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JmxMasterInfoResponse> getJmxMasterInfoList()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {

		RestUrlSequentialExecuter<List<JmxMasterInfoResponse>> proxy = new RestUrlSequentialExecuter<List<JmxMasterInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<JmxMasterInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<JmxMasterInfoResponse> result =  apiClient.monitorsettingGetJmxMasterInfoList();
				return result;
			}
		};
		try {
			return (List<JmxMasterInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JmxMasterInfoResponseP1> getJmxMonitorItemList()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {

		RestUrlSequentialExecuter<List<JmxMasterInfoResponseP1>> proxy = new RestUrlSequentialExecuter<List<JmxMasterInfoResponseP1>>(this.connectUnit, this.restKind){
			@Override
			public List<JmxMasterInfoResponseP1> executeMethod(DefaultApi apiClient) throws Exception {
				List<JmxMasterInfoResponseP1> result =  apiClient.monitorsettingGetJmxMonitorItemList();
				return result;
			}
		};
		try {
			return (List<JmxMasterInfoResponseP1>) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JmxUrlFormatInfoResponse> getJmxUrlFormatList()
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {

		RestUrlSequentialExecuter<List<JmxUrlFormatInfoResponse>> proxy = new RestUrlSequentialExecuter<List<JmxUrlFormatInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<JmxUrlFormatInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<JmxUrlFormatInfoResponse> result =  apiClient.monitorsettingGetJmxUrlFormatList();
				return result;
			}
		};
		try {
			return (List<JmxUrlFormatInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<MonitorInfoBeanResponse> getMonitorListForJobMonitor(String ownerRoleId)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<MonitorInfoBeanResponse>> proxy = new RestUrlSequentialExecuter<List<MonitorInfoBeanResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<MonitorInfoBeanResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<MonitorInfoBeanResponse> result =  apiClient.monitorsettingGetMonitorListForJobMonitor(ownerRoleId);
				return result;
			}
		};
		try {
			return (List<MonitorInfoBeanResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<BinaryCheckInfoResponse> getBinaryPresetList()
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {

		RestUrlSequentialExecuter<List<BinaryCheckInfoResponse>> proxy = new RestUrlSequentialExecuter<List<BinaryCheckInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<BinaryCheckInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<BinaryCheckInfoResponse> result =  apiClient.monitorsettingGetBinaryPresetList();
				return result;
			}
		};
		try {
			return (List<BinaryCheckInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<GetMonitorStringTagListResponse> getMonitorStringTagList(String monitorId, String ownerRoleId)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {

		RestUrlSequentialExecuter<List<GetMonitorStringTagListResponse>> proxy = new RestUrlSequentialExecuter<List<GetMonitorStringTagListResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<GetMonitorStringTagListResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<GetMonitorStringTagListResponse> result =  apiClient.monitorsettingGetMonitorStringTagList(monitorId, ownerRoleId);
				return result;
			}
		};
		try {
			return (List<GetMonitorStringTagListResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<RpaLogfileMonitorInfoResponse> getRpaLogfileList()
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<RpaLogfileMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<RpaLogfileMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<RpaLogfileMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<RpaLogfileMonitorInfoResponse> result =  apiClient.monitorsettingGetRpaLogfileList();
				return result;
			}
		};
		try {
			return (List<RpaLogfileMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<RpaManagementToolMonitorInfoResponse> getRpaManagementToolServiceList()
			throws RestConnectFailed, MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		RestUrlSequentialExecuter<List<RpaManagementToolMonitorInfoResponse>> proxy = new RestUrlSequentialExecuter<List<RpaManagementToolMonitorInfoResponse>>(this.connectUnit, this.restKind){
			@Override
			public List<RpaManagementToolMonitorInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<RpaManagementToolMonitorInfoResponse> result =  apiClient.monitorsettingGetRpaManagementToolList();
				return result;
			}
		};
		try {
			return (List<RpaManagementToolMonitorInfoResponse>) proxy.proxyExecute();
		} catch (RestConnectFailed | MonitorNotFound | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown){//想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
}
