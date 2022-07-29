/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.maintenance.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AddMaintenanceRequest;
import org.openapitools.client.model.MaintenanceInfoResponse;
import org.openapitools.client.model.MaintenanceTypeInfoResponse;
import org.openapitools.client.model.ModifyMaintenanceRequest;
import org.openapitools.client.model.SetMaintenanceStatusRequest;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MaintenanceDuplicate;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestUrlSequentialExecuter;

public class MaintenanceRestClientWrapper {

	private static Log m_log = LogFactory.getLog( MaintenanceRestClientWrapper.class );
	private RestConnectUnit connectUnit;
	
	private final RestKind restKind = RestKind.MaintenanceRestEndpoints;

	public MaintenanceRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}
	
	public static MaintenanceRestClientWrapper getWrapper(String managerName) {
		return new MaintenanceRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public MaintenanceInfoResponse addMaintenance(AddMaintenanceRequest req) throws RestConnectFailed, HinemosUnknown, MaintenanceDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<MaintenanceInfoResponse> proxy = new RestUrlSequentialExecuter<MaintenanceInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public MaintenanceInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				MaintenanceInfoResponse result =  apiClient.maintenanceAddMaintenance(req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | MaintenanceDuplicate | InvalidRole| InvalidUserPass | InvalidSetting def) {
			m_log.warn("addMaintenance(), " + def.getMessage());
			throw def;
		} catch ( Exception unknown ){
			m_log.warn("addMaintenance(), " + unknown.getMessage());
			throw new HinemosUnknown(unknown);
		}
	}
	


	public List<MaintenanceInfoResponse> getMaintenanceList() throws RestConnectFailed, HinemosUnknown, NotifyNotFound, MaintenanceNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<MaintenanceInfoResponse>> proxy = new RestUrlSequentialExecuter<List<MaintenanceInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<MaintenanceInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<MaintenanceInfoResponse> result =  apiClient.maintenanceGetMaintenanceList();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | NotifyNotFound | MaintenanceNotFound | InvalidUserPass | InvalidRole | InvalidSetting def) {
			m_log.warn("getMaintenanceList(), " + def.getMessage());
			throw def;
		} catch ( Exception unknown ){
			m_log.warn("getMaintenanceList(), " + unknown.getMessage());
			throw new HinemosUnknown(unknown);
		}
	}

	public MaintenanceInfoResponse getMaintenanceInfo(String maintenanceId) throws RestConnectFailed, HinemosUnknown, NotifyNotFound, MaintenanceNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<MaintenanceInfoResponse> proxy = new RestUrlSequentialExecuter<MaintenanceInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public MaintenanceInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				MaintenanceInfoResponse result =  apiClient.maintenanceGetMaintenanceInfo(maintenanceId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | NotifyNotFound | MaintenanceNotFound | InvalidRole| InvalidUserPass def) {
			m_log.warn("getMaintenanceInfo(), " + def.getMessage());
			throw def;
		} catch ( Exception unknown ){
			m_log.warn("getMaintenanceInfo(), " + unknown.getMessage());
			throw new HinemosUnknown(unknown);
		}
	}

	public List<MaintenanceInfoResponse> deleteMaintenance(String maintenanceIds) throws RestConnectFailed, HinemosUnknown, NotifyNotFound, MaintenanceNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<MaintenanceInfoResponse>> proxy = new RestUrlSequentialExecuter<List<MaintenanceInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<MaintenanceInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<MaintenanceInfoResponse> result =  apiClient.maintenanceDeleteMaintenance(maintenanceIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole| InvalidUserPass def) {
			m_log.warn("deleteMaintenance(), " + def.getMessage());
			throw def;
		} catch ( Exception unknown ){
			m_log.warn("deleteMaintenance(), " + unknown.getMessage());
			throw new HinemosUnknown(unknown);
		}
	}

	public MaintenanceInfoResponse modifyMaintenance(String maintenanceId, ModifyMaintenanceRequest req) throws RestConnectFailed, HinemosUnknown, NotifyNotFound, MaintenanceNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<MaintenanceInfoResponse> proxy = new RestUrlSequentialExecuter<MaintenanceInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public MaintenanceInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				MaintenanceInfoResponse result =  apiClient.maintenanceModifyMaintenance(maintenanceId, req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | NotifyNotFound | MaintenanceNotFound | InvalidSetting| InvalidRole| InvalidUserPass def) {
			m_log.warn("modifyMaintenance(), " + def.getMessage());
			throw def;
		} catch ( Exception unknown ){
			m_log.warn("modifyMaintenance(), " + unknown.getMessage());
			throw new HinemosUnknown(unknown);
		}
	}
	public List<MaintenanceTypeInfoResponse> getMaintenanceTypeList() throws RestConnectFailed, MaintenanceNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<MaintenanceTypeInfoResponse>> proxy = new RestUrlSequentialExecuter<List<MaintenanceTypeInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<MaintenanceTypeInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<MaintenanceTypeInfoResponse> result =  apiClient.maintenanceGetMaintenanceTypeList();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | MaintenanceNotFound | InvalidSetting | InvalidRole| InvalidUserPass def) {
			m_log.warn("getMaintenanceTypeList(), " + def.getMessage());
			throw def;
		} catch ( Exception unknown ){
			m_log.warn("getMaintenanceTypeList(), " + unknown.getMessage());
			throw new HinemosUnknown(unknown);
		}
	}
	public List<MaintenanceInfoResponse> setMaintenanceStatus(SetMaintenanceStatusRequest req) throws RestConnectFailed, NotifyNotFound, MaintenanceNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<MaintenanceInfoResponse>> proxy = new RestUrlSequentialExecuter<List<MaintenanceInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<MaintenanceInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<MaintenanceInfoResponse> result =  apiClient.maintenanceSetMaintenanceStatus(req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | NotifyNotFound | MaintenanceNotFound | InvalidSetting | InvalidRole| InvalidUserPass def) {
			m_log.warn("setMaintenanceStatus(), " + def.getMessage());
			throw def;
		} catch ( Exception unknown ){
			m_log.warn("setMaintenanceStatus(), " + unknown.getMessage());
			throw new HinemosUnknown(unknown);
		}
	}
}
