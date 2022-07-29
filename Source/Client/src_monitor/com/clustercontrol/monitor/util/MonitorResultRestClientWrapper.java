/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.monitor.util;

import java.io.File;
import java.util.List;

import org.openapitools.client.model.DeleteStatusRequest;
import org.openapitools.client.model.DownloadEventFileRequest;
import org.openapitools.client.model.EventLogInfoResponse;
import org.openapitools.client.model.ExecEventCustomCommandRequest;
import org.openapitools.client.model.ExecEventCustomCommandResponse;
import org.openapitools.client.model.GetEventCustomCommandResultResponse;
import org.openapitools.client.model.GetEventDataMapResponse;
import org.openapitools.client.model.GetEventInfoRequest;
import org.openapitools.client.model.GetEventInfoResponse;
import org.openapitools.client.model.GetEventListRequest;
import org.openapitools.client.model.GetEventListResponse;
import org.openapitools.client.model.GetStatusListRequest;
import org.openapitools.client.model.GetStatusListResponse;
import org.openapitools.client.model.ModifyBatchConfirmRequest;
import org.openapitools.client.model.ModifyCollectGraphFlgRequest;
import org.openapitools.client.model.ModifyCommnetRequest;
import org.openapitools.client.model.ModifyConfirmRequest;
import org.openapitools.client.model.ModifyEventInfoRequest;
import org.openapitools.client.model.ScopeDataInfoResponse;
import org.openapitools.client.model.StatusInfoResponse;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.EventLogNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.util.DateTimeStringConverter;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestUrlSequentialExecuter;

public class MonitorResultRestClientWrapper {
	public static final String DATETIME_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";

	private RestConnectUnit connectUnit;

	private final RestKind restKind = RestKind.MonitorResultRestEndpoints;

	public static MonitorResultRestClientWrapper getWrapper(String managerName){
		return new MonitorResultRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public MonitorResultRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}

	public GetEventListResponse getEventList(GetEventListRequest getEventListRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting {
		RestUrlSequentialExecuter<GetEventListResponse> proxy = new RestUrlSequentialExecuter<GetEventListResponse>(this.connectUnit, this.restKind, DATETIME_FORMAT) {
			@Override
			public GetEventListResponse executeMethod(DefaultApi apiClient) throws Exception {
				GetEventListResponse result = apiClient.monitorresultGetEventList(getEventListRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<ScopeDataInfoResponse> getScopeList ( String facilityId, boolean statusFlag, boolean eventFlag, boolean orderFlg ) 
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound {
		RestUrlSequentialExecuter< List<ScopeDataInfoResponse> > proxy = new RestUrlSequentialExecuter< List<ScopeDataInfoResponse> >(this.connectUnit,this.restKind, DATETIME_FORMAT){
			@Override
			public  List<ScopeDataInfoResponse>  executeMethod( DefaultApi apiClient) throws Exception{
				List<ScopeDataInfoResponse>  result =  apiClient.monitorresultGetScopeList(facilityId, statusFlag, eventFlag, orderFlg);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | MonitorNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<StatusInfoResponse> getStatusList(GetStatusListRequest getStatusListRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting {
		RestUrlSequentialExecuter<List<StatusInfoResponse>> proxy =
				new RestUrlSequentialExecuter<List<StatusInfoResponse>>(this.connectUnit, this.restKind, DATETIME_FORMAT) {
			@Override
			public List<StatusInfoResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<StatusInfoResponse> result = null;
				GetStatusListResponse dtoRes =apiClient.monitorresultGetStatusList(getStatusListRequest);
				if(dtoRes != null){
					result = dtoRes.getStatusList();
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<StatusInfoResponse> deleteStatus ( DeleteStatusRequest deleteStatusRequest ) 
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, InvalidSetting {
		RestUrlSequentialExecuter< List<StatusInfoResponse> > proxy = new RestUrlSequentialExecuter<List<StatusInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<StatusInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<StatusInfoResponse>  result =  apiClient.monitorresultDeleteStatus(deleteStatusRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole| InvalidUserPass | MonitorNotFound | InvalidSetting  def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public File downloadEventFile ( DownloadEventFileRequest downloadEventFileRequest) 
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting {
		RestUrlSequentialExecuter< File > proxy = new RestUrlSequentialExecuter< File >(this.connectUnit,this.restKind,DATETIME_FORMAT){
			@Override
			public  File  executeMethod( DefaultApi apiClient) throws Exception{
				File  result =  apiClient.monitorresultDownloadEventFile(downloadEventFileRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole| InvalidUserPass | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public GetEventInfoResponse getEventInfo( GetEventInfoRequest getEventInfoRequest ) 
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, MonitorNotFound {
		RestUrlSequentialExecuter< GetEventInfoResponse > proxy = new RestUrlSequentialExecuter< GetEventInfoResponse >(this.connectUnit,this.restKind,DATETIME_FORMAT){
			@Override
			public  GetEventInfoResponse  executeMethod( DefaultApi apiClient) throws Exception{
				GetEventInfoResponse  result =  apiClient.monitorresultGetEventInfo(getEventInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole| InvalidUserPass | InvalidSetting | MonitorNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public EventLogInfoResponse modifyEventInfo(ModifyEventInfoRequest modifyEventInfoRequest) 
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, EventLogNotFound, InvalidSetting, MonitorNotFound {
			RestUrlSequentialExecuter< EventLogInfoResponse > proxy = new RestUrlSequentialExecuter< EventLogInfoResponse >(this.connectUnit,this.restKind,DATETIME_FORMAT){
				@Override
				public  EventLogInfoResponse  executeMethod( DefaultApi apiClient) throws Exception{
					EventLogInfoResponse  result =  apiClient.monitorresultModifyEventInfo(modifyEventInfoRequest);
					return result;
				}
			};
			try {
				return proxy.proxyExecute();
			} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | EventLogNotFound | InvalidSetting | MonitorNotFound def) {//想定内例外 API個別に判断
				throw def;
			} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
				throw new HinemosUnknown(unknown);
			}
	}

	public EventLogInfoResponse modifyComment ( ModifyCommnetRequest modifyCommnetRequest ) 
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, EventLogNotFound, InvalidSetting, MonitorNotFound {
		RestUrlSequentialExecuter< EventLogInfoResponse > proxy = new RestUrlSequentialExecuter< EventLogInfoResponse >(this.connectUnit,this.restKind){
			@Override
			public  EventLogInfoResponse  executeMethod( DefaultApi apiClient) throws Exception{
				EventLogInfoResponse  result =  apiClient.monitorresultModifyComment(modifyCommnetRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | EventLogNotFound | InvalidSetting | MonitorNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<EventLogInfoResponse> modifyConfirm ( ModifyConfirmRequest modifyConfirmRequest ) 
			throws RestConnectFailed, HinemosUnknown, InvalidRole, MonitorNotFound, InvalidSetting {
		RestUrlSequentialExecuter< List<EventLogInfoResponse> > proxy = new RestUrlSequentialExecuter< List<EventLogInfoResponse> >(this.connectUnit,this.restKind,DATETIME_FORMAT){
			@Override
			public  List<EventLogInfoResponse>  executeMethod( DefaultApi apiClient) throws Exception{
				List<EventLogInfoResponse>  result =  apiClient.monitorresultModifyConfirm(modifyConfirmRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole| MonitorNotFound | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<EventLogInfoResponse> modifyBatchConfirm ( ModifyBatchConfirmRequest modifyBatchConfirmRequest ) 
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass,InvalidSetting {
		RestUrlSequentialExecuter< List<EventLogInfoResponse> > proxy = new RestUrlSequentialExecuter< List<EventLogInfoResponse> >(this.connectUnit,this.restKind,DATETIME_FORMAT){
			@Override
			public  List<EventLogInfoResponse>  executeMethod( DefaultApi apiClient) throws Exception{
				List<EventLogInfoResponse>  result =  apiClient.monitorresultModifyBatchConfirm(modifyBatchConfirmRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole| InvalidUserPass | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<EventLogInfoResponse> modifyCollectGraphFlg ( ModifyCollectGraphFlgRequest modifyCollectGraphFlgRequest ) 
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, InvalidSetting  {
		RestUrlSequentialExecuter< List<EventLogInfoResponse> > proxy = new RestUrlSequentialExecuter< List<EventLogInfoResponse> >(this.connectUnit,this.restKind,DATETIME_FORMAT){
			@Override
			public  List<EventLogInfoResponse>  executeMethod( DefaultApi apiClient) throws Exception{
				List<EventLogInfoResponse>  result =  apiClient.monitorresultModifyCollectGraphFlg(modifyCollectGraphFlgRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole| InvalidUserPass | MonitorNotFound | InvalidSetting  def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ExecEventCustomCommandResponse execEventCustomCommand ( ExecEventCustomCommandRequest execEventCustomCommandRequest ) 
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass ,InvalidSetting {
		RestUrlSequentialExecuter< ExecEventCustomCommandResponse > proxy = new RestUrlSequentialExecuter< ExecEventCustomCommandResponse >(this.connectUnit,this.restKind,DATETIME_FORMAT){
			@Override
			public  ExecEventCustomCommandResponse  executeMethod( DefaultApi apiClient) throws Exception{
				ExecEventCustomCommandResponse  result =  apiClient.monitorresultExecEventCustomCommand(execEventCustomCommandRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole| InvalidUserPass  | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public GetEventCustomCommandResultResponse getEventCustomCommandResult(String commandResultID) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter< GetEventCustomCommandResultResponse > proxy = new RestUrlSequentialExecuter< GetEventCustomCommandResultResponse >(this.connectUnit,this.restKind){
			@Override
			public  GetEventCustomCommandResultResponse  executeMethod( DefaultApi apiClient) throws Exception{
				GetEventCustomCommandResultResponse  result =  apiClient.monitorresultGetEventCustomCommandResult(commandResultID);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole| InvalidUserPass def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public GetEventDataMapResponse getEventDataMap (String facilityIdList) throws RestConnectFailed, HinemosUnknown {
		RestUrlSequentialExecuter< GetEventDataMapResponse > proxy = new RestUrlSequentialExecuter< GetEventDataMapResponse >(this.connectUnit,this.restKind, DATETIME_FORMAT){
			@Override
			public  GetEventDataMapResponse  executeMethod( DefaultApi apiClient) throws Exception{
				GetEventDataMapResponse  result =  apiClient.monitorresultGetEventDataMap(facilityIdList);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static java.util.Date parseDate (String dateString) {
		return DateTimeStringConverter.parseDateString(dateString, DATETIME_FORMAT);
	}

	public static String formatDate(java.util.Date date) {
		return DateTimeStringConverter.formatDate(date, DATETIME_FORMAT);
	}

}
