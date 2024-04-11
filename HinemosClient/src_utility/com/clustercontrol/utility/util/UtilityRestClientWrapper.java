/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.CheckPublishResponse;
import org.openapitools.client.model.GetImportUnitNumberResponse;
import org.openapitools.client.model.ImportCalendarPatternRequest;
import org.openapitools.client.model.ImportCalendarPatternResponse;
import org.openapitools.client.model.ImportCalendarRequest;
import org.openapitools.client.model.ImportCalendarResponse;
import org.openapitools.client.model.ImportCloudScopeRequest;
import org.openapitools.client.model.ImportCloudScopeResponse;
import org.openapitools.client.model.ImportEventFilterSettingRequest;
import org.openapitools.client.model.ImportFileCheckRequest;
import org.openapitools.client.model.ImportFileCheckResponse;
import org.openapitools.client.model.ImportFilterSettingResponse;
import org.openapitools.client.model.ImportHinemosPropertyRequest;
import org.openapitools.client.model.ImportHinemosPropertyResponse;
import org.openapitools.client.model.ImportInfraManagementInfoRequest;
import org.openapitools.client.model.ImportInfraManagementInfoResponse;
import org.openapitools.client.model.ImportJmxMasterRequest;
import org.openapitools.client.model.ImportJmxMasterResponse;
import org.openapitools.client.model.ImportJobHistoryFilterSettingRequest;
import org.openapitools.client.model.ImportJobLinkRcvRequest;
import org.openapitools.client.model.ImportJobLinkRcvResponse;
import org.openapitools.client.model.ImportJobLinkSendRequest;
import org.openapitools.client.model.ImportJobLinkSendResponse;
import org.openapitools.client.model.ImportJobManualRequest;
import org.openapitools.client.model.ImportJobManualResponse;
import org.openapitools.client.model.ImportJobMasterRequest;
import org.openapitools.client.model.ImportJobMasterResponse;
import org.openapitools.client.model.ImportJobQueueRequest;
import org.openapitools.client.model.ImportJobQueueResponse;
import org.openapitools.client.model.ImportLogFormatRequest;
import org.openapitools.client.model.ImportLogFormatResponse;
import org.openapitools.client.model.ImportMailTemplateRequest;
import org.openapitools.client.model.ImportMailTemplateResponse;
import org.openapitools.client.model.ImportMaintenanceRequest;
import org.openapitools.client.model.ImportMaintenanceResponse;
import org.openapitools.client.model.ImportMonitorCommonRequest;
import org.openapitools.client.model.ImportMonitorCommonResponse;
import org.openapitools.client.model.ImportNodeConfigSettingRequest;
import org.openapitools.client.model.ImportNodeConfigSettingResponse;
import org.openapitools.client.model.ImportNodeMapModelRequest;
import org.openapitools.client.model.ImportNodeMapModelResponse;
import org.openapitools.client.model.ImportNodeRequest;
import org.openapitools.client.model.ImportNodeResponse;
import org.openapitools.client.model.ImportNotifyRequest;
import org.openapitools.client.model.ImportNotifyResponse;
import org.openapitools.client.model.ImportObjectPrivilegeInfoRequest;
import org.openapitools.client.model.ImportObjectPrivilegeInfoResponse;
import org.openapitools.client.model.ImportPlatformMasterRequest;
import org.openapitools.client.model.ImportPlatformMasterResponse;
import org.openapitools.client.model.ImportReportingInfoRequest;
import org.openapitools.client.model.ImportReportingInfoResponse;
import org.openapitools.client.model.ImportReportingTemplateSetRequest;
import org.openapitools.client.model.ImportReportingTemplateSetResponse;
import org.openapitools.client.model.ImportRestAccessInfoRequest;
import org.openapitools.client.model.ImportRestAccessInfoResponse;
import org.openapitools.client.model.ImportRoleRequest;
import org.openapitools.client.model.ImportRoleResponse;
import org.openapitools.client.model.ImportRoleUserRequest;
import org.openapitools.client.model.ImportRoleUserResponse;
import org.openapitools.client.model.ImportRpaManagementToolAccountRequest;
import org.openapitools.client.model.ImportRpaManagementToolAccountResponse;
import org.openapitools.client.model.ImportRpaScenarioCoefficientPatternRequest;
import org.openapitools.client.model.ImportRpaScenarioCoefficientPatternResponse;
import org.openapitools.client.model.ImportRpaScenarioOperationResultCreateSettingRequest;
import org.openapitools.client.model.ImportRpaScenarioOperationResultCreateSettingResponse;
import org.openapitools.client.model.ImportRpaScenarioTagRequest;
import org.openapitools.client.model.ImportRpaScenarioTagResponse;
import org.openapitools.client.model.ImportScheduleRequest;
import org.openapitools.client.model.ImportScheduleResponse;
import org.openapitools.client.model.ImportScopeRequest;
import org.openapitools.client.model.ImportScopeResponse;
import org.openapitools.client.model.ImportStatusFilterSettingRequest;
import org.openapitools.client.model.ImportSystemPrivilegeInfoRequest;
import org.openapitools.client.model.ImportSystemPrivilegeInfoResponse;
import org.openapitools.client.model.ImportTransferRequest;
import org.openapitools.client.model.ImportTransferResponse;
import org.openapitools.client.model.ImportUserRequest;
import org.openapitools.client.model.ImportUserResponse;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.ICheckPublishRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestUrlSequentialExecuter;

public class UtilityRestClientWrapper implements ICheckPublishRestClientWrapper {
	private static Log m_log = LogFactory.getLog(UtilityRestClientWrapper.class);
	private RestConnectUnit connectUnit;
	
	private final RestKind restKind = RestKind.UtilityRestEndpoints;

	public static UtilityRestClientWrapper getWrapper(String managerName){
		return new UtilityRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public UtilityRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}
	
	@Override
	public  CheckPublishResponse checkPublish() throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter< CheckPublishResponse> proxy = new RestUrlSequentialExecuter< CheckPublishResponse>(this.connectUnit,this.restKind){
			@Override
			public  CheckPublishResponse executeMethod( DefaultApi apiClient) throws Exception{
				 CheckPublishResponse result =  apiClient.utilityCheckPublish();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed def) {//通信異常
			throw new RestConnectFailed(Messages.getString("message.hinemos.failure.transfer") + ", " + HinemosMessage.replace(def.getMessage()), def);
		} catch (InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch (UrlNotFound e) {
			// UrlNotFoundが返された場合エンドポイントがPublishされていないためメッセージを設定する
			throw new HinemosUnknown(Messages.getString("message.expiration.term"), e);
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(Messages.getString("message.hinemos.failure.unexpected") + "," + HinemosMessage.replace(unknown.getMessage()), unknown);
		}
	}

	public  List<GetImportUnitNumberResponse> getImportUnitNumber(String functionIds) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter< List<GetImportUnitNumberResponse>> proxy = new RestUrlSequentialExecuter< List<GetImportUnitNumberResponse>>(this.connectUnit,this.restKind){
			@Override
			public  List<GetImportUnitNumberResponse> executeMethod( DefaultApi apiClient) throws Exception{
				 List<GetImportUnitNumberResponse> result =  apiClient.utilityGetImportUnitNumber(functionIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ImportNodeResponse importNode(ImportNodeRequest importNodeRequest )
			throws RestConnectFailed, InvalidUserPass, InvalidRole,HinemosUnknown {
		RestUrlSequentialExecuter<ImportNodeResponse> proxy = new RestUrlSequentialExecuter<ImportNodeResponse>(this.connectUnit,this.restKind){
			@Override
			public ImportNodeResponse executeMethod( DefaultApi apiClient) throws Exception{
				if(m_log.isDebugEnabled()){
					m_log.debug("importNode():call apiClient.utilityImportNode");
				}
				ImportNodeResponse result =  apiClient.utilityImportNode(importNodeRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ImportScopeResponse importScope(ImportScopeRequest importScopeRequest )
			throws RestConnectFailed, InvalidUserPass, InvalidRole,HinemosUnknown {
		RestUrlSequentialExecuter<ImportScopeResponse> proxy = new RestUrlSequentialExecuter<ImportScopeResponse>(this.connectUnit,this.restKind){
			@Override
			public ImportScopeResponse executeMethod( DefaultApi apiClient) throws Exception{
				if(m_log.isTraceEnabled()){
					m_log.trace("importScope():call apiClient.utilityImportNode " + importScopeRequest.toString());
				}
				ImportScopeResponse result =  apiClient.utilityImportScope(importScopeRequest);
				if(m_log.isTraceEnabled()){
					m_log.trace("importScope():return apiClient.utilityImportNode " + result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ImportJobMasterResponse importJobMaster(ImportJobMasterRequest importJobMasterRequest )
			throws RestConnectFailed, InvalidUserPass, InvalidRole,HinemosUnknown {
		RestUrlSequentialExecuter<ImportJobMasterResponse> proxy = new RestUrlSequentialExecuter<ImportJobMasterResponse>(this.connectUnit,this.restKind){
			@Override
			public ImportJobMasterResponse executeMethod( DefaultApi apiClient) throws Exception{
				if(m_log.isTraceEnabled()){
					m_log.trace("importNode():call apiClient.utilityImportNode " + importJobMasterRequest.toString());
				}
				ImportJobMasterResponse result =  apiClient.utilityImportJobMaster(importJobMasterRequest);
				if(m_log.isTraceEnabled()){
					m_log.trace("importNode():return apiClient.utilityImportNode " + result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ImportRoleResponse importRole(ImportRoleRequest importRoleRequest )
			throws RestConnectFailed, InvalidUserPass, InvalidRole,HinemosUnknown {
		RestUrlSequentialExecuter<ImportRoleResponse> proxy = new RestUrlSequentialExecuter<ImportRoleResponse>(this.connectUnit,this.restKind){
			@Override
			public ImportRoleResponse executeMethod( DefaultApi apiClient) throws Exception{
				ImportRoleResponse result =  apiClient.utilityImportRole(importRoleRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ImportUserResponse importUser(ImportUserRequest importUserRequest )
			throws RestConnectFailed, InvalidUserPass, InvalidRole,HinemosUnknown {
		RestUrlSequentialExecuter<ImportUserResponse> proxy = new RestUrlSequentialExecuter<ImportUserResponse>(this.connectUnit,this.restKind){
			@Override
			public ImportUserResponse executeMethod( DefaultApi apiClient) throws Exception{
				ImportUserResponse result =  apiClient.utilityImportUser(importUserRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ImportRoleUserResponse importRoleUser(ImportRoleUserRequest importRoleUserRequest )
			throws RestConnectFailed, InvalidUserPass, InvalidRole,HinemosUnknown {
		RestUrlSequentialExecuter<ImportRoleUserResponse> proxy = new RestUrlSequentialExecuter<ImportRoleUserResponse>(this.connectUnit,this.restKind){
			@Override
			public ImportRoleUserResponse executeMethod( DefaultApi apiClient) throws Exception{
				ImportRoleUserResponse result =  apiClient.utilityImportRoleUser(importRoleUserRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ImportMonitorCommonResponse importMonitorCommon(ImportMonitorCommonRequest importMonitorCommonRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportMonitorCommonResponse> proxy = new RestUrlSequentialExecuter<ImportMonitorCommonResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportMonitorCommonResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportMonitorCommonResponse result = apiClient.utilityImportMonitorCommon(importMonitorCommonRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportNotifyResponse importNotify(ImportNotifyRequest importNotifyRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportNotifyResponse> proxy = new RestUrlSequentialExecuter<ImportNotifyResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportNotifyResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportNotifyResponse result = apiClient.utilityImportNotify(importNotifyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportLogFormatResponse importLogFormat(ImportLogFormatRequest importLogFormatRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportLogFormatResponse> proxy = new RestUrlSequentialExecuter<ImportLogFormatResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportLogFormatResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportLogFormatResponse result = apiClient.utilityImportLogFormat(importLogFormatRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ImportObjectPrivilegeInfoResponse importObjectPrivilegeInfo(ImportObjectPrivilegeInfoRequest importObjectPrivilegeInfoRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportObjectPrivilegeInfoResponse> proxy = new RestUrlSequentialExecuter<ImportObjectPrivilegeInfoResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportObjectPrivilegeInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportObjectPrivilegeInfoResponse result = apiClient.utilityImportObjectPrivilegeInfo(importObjectPrivilegeInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ImportSystemPrivilegeInfoResponse importSystemPrivilegeInfo(ImportSystemPrivilegeInfoRequest importSystemPrivilegeInfoRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportSystemPrivilegeInfoResponse > proxy = new RestUrlSequentialExecuter<ImportSystemPrivilegeInfoResponse >(
				this.connectUnit, this.restKind) {
			@Override
			public ImportSystemPrivilegeInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportSystemPrivilegeInfoResponse result = apiClient.utilityimportSystemPrivilegeInfo(importSystemPrivilegeInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ImportInfraManagementInfoResponse importInfraManagementInfo(ImportInfraManagementInfoRequest importImportInfraManagementInfoRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportInfraManagementInfoResponse > proxy = new RestUrlSequentialExecuter<ImportInfraManagementInfoResponse >(
				this.connectUnit, this.restKind) {
			@Override
			public ImportInfraManagementInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportInfraManagementInfoResponse result = apiClient.utilityImportInfraManagementInfo(importImportInfraManagementInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ImportReportingInfoResponse importReportingInfo(ImportReportingInfoRequest importReportingInfoRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportReportingInfoResponse > proxy = new RestUrlSequentialExecuter<ImportReportingInfoResponse >(
				this.connectUnit, this.restKind) {
			@Override
			public ImportReportingInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				
				ImportReportingInfoResponse result = apiClient.utilityImportReportingInfo(importReportingInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ImportReportingTemplateSetResponse importReportingTemplateSet(ImportReportingTemplateSetRequest importReportingTemplateSetRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportReportingTemplateSetResponse > proxy = new RestUrlSequentialExecuter<ImportReportingTemplateSetResponse >(
				this.connectUnit, this.restKind) {
			@Override
			public ImportReportingTemplateSetResponse executeMethod(DefaultApi apiClient) throws Exception {
				
				ImportReportingTemplateSetResponse result = apiClient.utilityImportReportingTemplateSet(importReportingTemplateSetRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ImportCloudScopeResponse importCloudScope(ImportCloudScopeRequest importCloudScopeRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportCloudScopeResponse > proxy = new RestUrlSequentialExecuter<ImportCloudScopeResponse >(
				this.connectUnit, this.restKind) {
			@Override
			public ImportCloudScopeResponse executeMethod(DefaultApi apiClient) throws Exception {
				
				ImportCloudScopeResponse result = apiClient.utilityImportCloudScope(importCloudScopeRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	public ImportNodeConfigSettingResponse importNodeConfigSetting(ImportNodeConfigSettingRequest importNodeConfigSettingRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportNodeConfigSettingResponse> proxy = new RestUrlSequentialExecuter<ImportNodeConfigSettingResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportNodeConfigSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportNodeConfigSettingResponse result = apiClient.utilityImportNodeConfigSetting(importNodeConfigSettingRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ImportMailTemplateResponse importMailTemplate(ImportMailTemplateRequest importMailTemplateRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportMailTemplateResponse> proxy = new RestUrlSequentialExecuter<ImportMailTemplateResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportMailTemplateResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportMailTemplateResponse result = apiClient.utilityImportMailTemplate(importMailTemplateRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportCalendarResponse importCalendar(ImportCalendarRequest importCalendarRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportCalendarResponse> proxy = new RestUrlSequentialExecuter<ImportCalendarResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportCalendarResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportCalendarResponse result = apiClient.utilityImportCalendar(importCalendarRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportCalendarPatternResponse importCalendarPattern(ImportCalendarPatternRequest importCalendarPatternRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportCalendarPatternResponse> proxy = new RestUrlSequentialExecuter<ImportCalendarPatternResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportCalendarPatternResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportCalendarPatternResponse result = apiClient.utilityImportCalendarPattern(importCalendarPatternRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportMaintenanceResponse importMaintenance(ImportMaintenanceRequest importMaintenanceRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportMaintenanceResponse> proxy = new RestUrlSequentialExecuter<ImportMaintenanceResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportMaintenanceResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportMaintenanceResponse result = apiClient.utilityImportMaintenance(importMaintenanceRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportHinemosPropertyResponse importHinemosProperty(ImportHinemosPropertyRequest importHinemosPropertyRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportHinemosPropertyResponse> proxy = new RestUrlSequentialExecuter<ImportHinemosPropertyResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportHinemosPropertyResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportHinemosPropertyResponse result = apiClient.utilityImportHinemosProperty(importHinemosPropertyRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportFileCheckResponse importFileCheck(ImportFileCheckRequest importFileCheckRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportFileCheckResponse> proxy = new RestUrlSequentialExecuter<ImportFileCheckResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportFileCheckResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportFileCheckResponse result = apiClient.utilityImportFileCheck(importFileCheckRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportJobLinkRcvResponse importJobLinkRcv(ImportJobLinkRcvRequest importJobLinkRcvRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportJobLinkRcvResponse> proxy = new RestUrlSequentialExecuter<ImportJobLinkRcvResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportJobLinkRcvResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportJobLinkRcvResponse result = apiClient.utilityImportJobLinkRcv(importJobLinkRcvRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportScheduleResponse importSchedule(ImportScheduleRequest importScheduleRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportScheduleResponse> proxy = new RestUrlSequentialExecuter<ImportScheduleResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportScheduleResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportScheduleResponse result = apiClient.utilityImportSchedule(importScheduleRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportJobManualResponse importJobManual(ImportJobManualRequest importJobManualRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportJobManualResponse> proxy = new RestUrlSequentialExecuter<ImportJobManualResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportJobManualResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportJobManualResponse result = apiClient.utilityImportJobManual(importJobManualRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportJobQueueResponse importJobQueue(ImportJobQueueRequest importJobQueueRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportJobQueueResponse> proxy = new RestUrlSequentialExecuter<ImportJobQueueResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportJobQueueResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportJobQueueResponse result = apiClient.utilityImportJobQueue(importJobQueueRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportJobLinkSendResponse importJobLinkSend(ImportJobLinkSendRequest importJobLinkSendRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportJobLinkSendResponse> proxy = new RestUrlSequentialExecuter<ImportJobLinkSendResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportJobLinkSendResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportJobLinkSendResponse result = apiClient.utilityImportJobLinkSend(importJobLinkSendRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportTransferResponse importTransfer(ImportTransferRequest importTransferRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportTransferResponse> proxy = new RestUrlSequentialExecuter<ImportTransferResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportTransferResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportTransferResponse result = apiClient.utilityImportTransfer(importTransferRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportPlatformMasterResponse importPlatformMaster(ImportPlatformMasterRequest importPlatformMasterRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportPlatformMasterResponse> proxy = new RestUrlSequentialExecuter<ImportPlatformMasterResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportPlatformMasterResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportPlatformMasterResponse result = apiClient.utilityImportPlatformMaster(importPlatformMasterRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportNodeMapModelResponse importNodeMapModel(ImportNodeMapModelRequest importNodeMapModelRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportNodeMapModelResponse> proxy = new RestUrlSequentialExecuter<ImportNodeMapModelResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportNodeMapModelResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportNodeMapModelResponse result = apiClient.utilityImportNodeMapModel(importNodeMapModelRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportJmxMasterResponse importJmxMaster(ImportJmxMasterRequest importJmxMasterRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportJmxMasterResponse> proxy = new RestUrlSequentialExecuter<ImportJmxMasterResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportJmxMasterResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportJmxMasterResponse result = apiClient.utilityImportJmxMaster(importJmxMasterRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportRestAccessInfoResponse importRestAccessInfo(ImportRestAccessInfoRequest importRestAccessInfoRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportRestAccessInfoResponse> proxy = new RestUrlSequentialExecuter<ImportRestAccessInfoResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportRestAccessInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportRestAccessInfoResponse result = apiClient.utilityImportRestAccessInfo(importRestAccessInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public ImportRpaScenarioTagResponse importRpaScenarioTag(ImportRpaScenarioTagRequest importRpaScenarioTagRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportRpaScenarioTagResponse > proxy = new RestUrlSequentialExecuter<ImportRpaScenarioTagResponse >(
				this.connectUnit, this.restKind) {
			@Override
			public ImportRpaScenarioTagResponse executeMethod(DefaultApi apiClient) throws Exception {
				
				ImportRpaScenarioTagResponse result = apiClient.utilityImportRpaScenarioTag(importRpaScenarioTagRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportRpaScenarioOperationResultCreateSettingResponse 
		importRpaScenarioOperationResultCreateSetting(ImportRpaScenarioOperationResultCreateSettingRequest request)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportRpaScenarioOperationResultCreateSettingResponse > proxy = new RestUrlSequentialExecuter<ImportRpaScenarioOperationResultCreateSettingResponse >(
				this.connectUnit, this.restKind) {
			@Override
			public ImportRpaScenarioOperationResultCreateSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				
				ImportRpaScenarioOperationResultCreateSettingResponse result = apiClient.utilityImportRpaScenarioOperationResultCreateSetting(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportRpaManagementToolAccountResponse 
		importRpaManagementToolAccount(ImportRpaManagementToolAccountRequest request)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportRpaManagementToolAccountResponse > proxy = new RestUrlSequentialExecuter<ImportRpaManagementToolAccountResponse >(
				this.connectUnit, this.restKind) {
			@Override
			public ImportRpaManagementToolAccountResponse executeMethod(DefaultApi apiClient) throws Exception {
				
				ImportRpaManagementToolAccountResponse result = apiClient.utilityImportRpaManagementToolAccount(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportRpaScenarioCoefficientPatternResponse 
		importRpaScenarioCoefficientPattern(ImportRpaScenarioCoefficientPatternRequest request)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportRpaScenarioCoefficientPatternResponse > proxy = new RestUrlSequentialExecuter<ImportRpaScenarioCoefficientPatternResponse >(
				this.connectUnit, this.restKind) {
			@Override
			public ImportRpaScenarioCoefficientPatternResponse executeMethod(DefaultApi apiClient) throws Exception {
				
				ImportRpaScenarioCoefficientPatternResponse result = apiClient.utilityImportRpaScenarioCoefficientPattern(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportFilterSettingResponse importFilterSettingMonitorHistoryEvent(ImportEventFilterSettingRequest request)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportFilterSettingResponse> proxy = new RestUrlSequentialExecuter<ImportFilterSettingResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportFilterSettingResponse result = apiClient.utilityImportFilterSettingMonitorHistoryEvent(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportFilterSettingResponse importFilterSettingMonitorHistoryStatus(ImportStatusFilterSettingRequest request)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportFilterSettingResponse> proxy = new RestUrlSequentialExecuter<ImportFilterSettingResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportFilterSettingResponse result = apiClient.utilityImportFilterSettingMonitorHistoryStatus(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public ImportFilterSettingResponse importFilterSettingJobHistory(ImportJobHistoryFilterSettingRequest request)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<ImportFilterSettingResponse> proxy = new RestUrlSequentialExecuter<ImportFilterSettingResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportFilterSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				ImportFilterSettingResponse result = apiClient.utilityImportFilterSettingJobHistory(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
}