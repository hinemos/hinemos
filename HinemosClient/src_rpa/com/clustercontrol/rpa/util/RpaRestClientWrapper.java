/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.util;

import java.io.File;
import java.util.List;

import org.openapitools.client.model.AddRpaManagementToolAccountRequest;
import org.openapitools.client.model.AddRpaScenarioOperationResultCreateSettingRequest;
import org.openapitools.client.model.AddRpaScenarioRequest;
import org.openapitools.client.model.AddRpaScenarioTagRequest;
import org.openapitools.client.model.CorrectExecNodeRequest;
import org.openapitools.client.model.DownloadRpaScenarioOperationResultRecordsRequest;
import org.openapitools.client.model.GetRpaScenarioCorrectExecNodeResponse;
import org.openapitools.client.model.GetRpaScenarioListRequest;
import org.openapitools.client.model.GetRpaScenarioListResponse;
import org.openapitools.client.model.GetRpaScenarioOperationResultSummaryForBarResponse;
import org.openapitools.client.model.GetRpaScenarioOperationResultSummaryForPieResponse;
import org.openapitools.client.model.GetRpaScenarioResponse;
import org.openapitools.client.model.ModifyRpaManagementToolAccountRequest;
import org.openapitools.client.model.ModifyRpaScenarioOperationResultCreateSettingRequest;
import org.openapitools.client.model.ModifyRpaScenarioRequest;
import org.openapitools.client.model.ModifyRpaScenarioTagRequest;
import org.openapitools.client.model.RpaManagementToolAccountResponse;
import org.openapitools.client.model.RpaManagementToolEndStatusResponse;
import org.openapitools.client.model.RpaManagementToolResponse;
import org.openapitools.client.model.RpaManagementToolRunParamResponse;
import org.openapitools.client.model.RpaManagementToolRunTypeResponse;
import org.openapitools.client.model.RpaManagementToolStopModeResponse;
import org.openapitools.client.model.RpaScenarioCoefficientPatternResponse;
import org.openapitools.client.model.RpaScenarioOperationResultCreateSettingResponse;
import org.openapitools.client.model.RpaScenarioOperationResultWithDetailResponse;
import org.openapitools.client.model.RpaScenarioResponse;
import org.openapitools.client.model.RpaScenarioTagResponse;
import org.openapitools.client.model.RpaToolEnvResponse;
import org.openapitools.client.model.RpaToolResponse;
import org.openapitools.client.model.RpaToolRunCommandResponse;
import org.openapitools.client.model.SearchRpaScenarioOperationResultRequest;
import org.openapitools.client.model.SearchRpaScenarioOperationResultResponse;
import org.openapitools.client.model.SetRpaScenarioOperationResultCreateSettingValidRequest;
import org.openapitools.client.model.UpdateRpaScenarioOperationResultRequest;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.RpaManagementToolAccountDuplicate;
import com.clustercontrol.fault.RpaManagementToolAccountNotFound;
import com.clustercontrol.fault.RpaScenarioDuplicate;
import com.clustercontrol.fault.RpaScenarioNotFound;
import com.clustercontrol.fault.RpaScenarioOperationResultCreateSettingDuplicate;
import com.clustercontrol.fault.RpaScenarioOperationResultCreateSettingNotFound;
import com.clustercontrol.fault.RpaScenarioOperationResultNotFound;
import com.clustercontrol.fault.RpaScenarioTagDuplicate;
import com.clustercontrol.fault.RpaScenarioTagNotFound;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.rest.endpoint.rpa.dto.SummaryTypeEnum;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestUrlSequentialExecuter;

public class RpaRestClientWrapper {

	private RestConnectUnit connectUnit;
	
	private final RestKind restKind = RestKind.RpaRestEndpoints;

	public static RpaRestClientWrapper getWrapper(String managerName) {
		return new RpaRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public RpaRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}
	
	public RpaManagementToolAccountResponse getRpaManagementToolAccount(String rpaScopeId) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<RpaManagementToolAccountResponse> proxy = new RestUrlSequentialExecuter<RpaManagementToolAccountResponse>(this.connectUnit,this.restKind){
			@Override
			public RpaManagementToolAccountResponse executeMethod(DefaultApi apiClient) throws Exception {
				RpaManagementToolAccountResponse result = apiClient.rpaGetRpaManagementToolAccount(rpaScopeId);
				return result;
			}
		};

		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<RpaManagementToolAccountResponse> getRpaManagementToolAccountList() throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RpaManagementToolAccountResponse>> proxy = new RestUrlSequentialExecuter<List<RpaManagementToolAccountResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<RpaManagementToolAccountResponse> executeMethod(DefaultApi apiClient) throws Exception{
				List<RpaManagementToolAccountResponse> result = apiClient.rpaGetRpaManagementToolAccountList();
				return result;
			}
		};

		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public RpaManagementToolAccountResponse addRpaManagementToolAccount(AddRpaManagementToolAccountRequest request) throws RestConnectFailed, HinemosUnknown, RpaManagementToolAccountDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<RpaManagementToolAccountResponse> proxy = new RestUrlSequentialExecuter<RpaManagementToolAccountResponse>(this.connectUnit,this.restKind){
			@Override
			public RpaManagementToolAccountResponse executeMethod( DefaultApi apiClient) throws Exception{
				RpaManagementToolAccountResponse result = apiClient.rpaAddRpaManagementToolAccount(request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RpaManagementToolAccountDuplicate | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public RpaManagementToolAccountResponse modifyRpaManagementToolAccount(String rpaScopeId, ModifyRpaManagementToolAccountRequest request)
			throws RestConnectFailed, HinemosUnknown, RpaManagementToolAccountNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<RpaManagementToolAccountResponse> proxy = new RestUrlSequentialExecuter<RpaManagementToolAccountResponse>(this.connectUnit,this.restKind){
			@Override
			public RpaManagementToolAccountResponse executeMethod( DefaultApi apiClient) throws Exception{
				RpaManagementToolAccountResponse result =  apiClient.rpaModifyRpaManagementToolAccount(rpaScopeId, request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RpaManagementToolAccountNotFound | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<RpaManagementToolAccountResponse> deleteRpaManagementToolAccount(String rpaScopeIds)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, RpaManagementToolAccountNotFound {
		RestUrlSequentialExecuter<List<RpaManagementToolAccountResponse>> proxy = new RestUrlSequentialExecuter<List<RpaManagementToolAccountResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<RpaManagementToolAccountResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<RpaManagementToolAccountResponse> result =  apiClient.rpaDeleteRpaManagementToolAccount(rpaScopeIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | RpaManagementToolAccountNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<RpaManagementToolResponse> getRpaManagementTool() throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RpaManagementToolResponse>> proxy = new RestUrlSequentialExecuter<List<RpaManagementToolResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<RpaManagementToolResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<RpaManagementToolResponse> result = apiClient.rpaGetRpaManagementTool();
				return result;
			}
		};

		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<RpaManagementToolRunTypeResponse> getRpaManagementToolRunType(String rpaManagementToolId) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RpaManagementToolRunTypeResponse>> proxy = new RestUrlSequentialExecuter<List<RpaManagementToolRunTypeResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<RpaManagementToolRunTypeResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<RpaManagementToolRunTypeResponse> result = apiClient.rpaGetRpaManagementToolRunType(rpaManagementToolId);
				return result;
			}
		};

		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<RpaManagementToolStopModeResponse> getRpaManagementToolStopMode(String rpaManagementToolId) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RpaManagementToolStopModeResponse>> proxy = new RestUrlSequentialExecuter<List<RpaManagementToolStopModeResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<RpaManagementToolStopModeResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<RpaManagementToolStopModeResponse> result = apiClient.rpaGetRpaManagementToolStopMode(rpaManagementToolId);
				return result;
			}
		};

		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<RpaManagementToolRunParamResponse> getRpaManagementToolRunParam(String rpaManagementToolId, Integer runType) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RpaManagementToolRunParamResponse>> proxy = new RestUrlSequentialExecuter<List<RpaManagementToolRunParamResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<RpaManagementToolRunParamResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<RpaManagementToolRunParamResponse> result = apiClient.rpaGetRpaManagementToolRunParam(rpaManagementToolId, runType);
				return result;
			}
		};

		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<RpaManagementToolEndStatusResponse> getRpaManagementToolEndStatus(String rpaManagementToolId) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RpaManagementToolEndStatusResponse>> proxy = new RestUrlSequentialExecuter<List<RpaManagementToolEndStatusResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<RpaManagementToolEndStatusResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<RpaManagementToolEndStatusResponse> result = apiClient.rpaGetRpaManagementToolEndStatus(rpaManagementToolId);
				return result;
			}
		};

		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<RpaToolResponse> getRpaTool() throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RpaToolResponse>> proxy = new RestUrlSequentialExecuter<List<RpaToolResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<RpaToolResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<RpaToolResponse> result = apiClient.rpaGetRpaTool();
				return result;
			}
		};

		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<RpaToolEnvResponse> getRpaToolEnv() throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RpaToolEnvResponse>> proxy = new RestUrlSequentialExecuter<List<RpaToolEnvResponse>>(this.connectUnit, this.restKind) {
			@Override
			public List<RpaToolEnvResponse> executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.rpaGetRpaToolEnv();
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<RpaToolRunCommandResponse> getRpaToolRunCommand() throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RpaToolRunCommandResponse>> proxy = new RestUrlSequentialExecuter<List<RpaToolRunCommandResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<RpaToolRunCommandResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<RpaToolRunCommandResponse> result = apiClient.rpaGetRpaToolRunCommand();
				return result;
			}
		};

		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public RpaScenarioTagResponse addRpaScenarioTag(AddRpaScenarioTagRequest addRpaScenarioTagRequest) throws RestConnectFailed, HinemosUnknown, RpaScenarioTagDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<RpaScenarioTagResponse> proxy = new RestUrlSequentialExecuter<RpaScenarioTagResponse>(this.connectUnit,this.restKind){
			@Override
			public RpaScenarioTagResponse executeMethod( DefaultApi apiClient) throws Exception{
				RpaScenarioTagResponse result = apiClient.rpaAddRpaScenarioTag(addRpaScenarioTagRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RpaScenarioTagDuplicate | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public RpaScenarioTagResponse modifyRpaScenarioTag(String scenarioTagId, ModifyRpaScenarioTagRequest modifyRpaScenarioTagRequest) throws RestConnectFailed,HinemosUnknown, RpaScenarioTagNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<RpaScenarioTagResponse> proxy = new RestUrlSequentialExecuter<RpaScenarioTagResponse>(this.connectUnit,this.restKind){
			@Override
			public RpaScenarioTagResponse executeMethod( DefaultApi apiClient) throws Exception{
				RpaScenarioTagResponse result =  apiClient.rpaModifyRpaScenarioTag(scenarioTagId, modifyRpaScenarioTagRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RpaScenarioTagNotFound | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<RpaScenarioTagResponse> deleteRpaScenarioTag(String scenarioTagIdList) throws RestConnectFailed,HinemosUnknown, RpaScenarioTagNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RpaScenarioTagResponse>> proxy = new RestUrlSequentialExecuter<List<RpaScenarioTagResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<RpaScenarioTagResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<RpaScenarioTagResponse> result =  apiClient.rpaDeleteRpaScenarioTag(scenarioTagIdList);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RpaScenarioTagNotFound | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<RpaScenarioTagResponse> getRpaScenarioTagList(String ownerRoleId) throws RestConnectFailed, HinemosUnknown, RpaScenarioTagNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RpaScenarioTagResponse>> proxy = new RestUrlSequentialExecuter<List<RpaScenarioTagResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<RpaScenarioTagResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<RpaScenarioTagResponse> result =  apiClient.rpaGetRpaSinarioTagList(ownerRoleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RpaScenarioTagNotFound | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public RpaScenarioTagResponse getRpaScenarioTag(String tagId) throws RestConnectFailed, HinemosUnknown, RpaScenarioTagNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<RpaScenarioTagResponse> proxy = new RestUrlSequentialExecuter<RpaScenarioTagResponse>(this.connectUnit,this.restKind){
			@Override
			public RpaScenarioTagResponse executeMethod( DefaultApi apiClient) throws Exception{
				RpaScenarioTagResponse result =  apiClient.rpaGetScenarioTag(tagId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RpaScenarioTagNotFound | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public SearchRpaScenarioOperationResultResponse searchRpaScenarioOperationResultList(SearchRpaScenarioOperationResultRequest searchRpaScenarioOperationResultRequest)
			throws RestConnectFailed, HinemosUnknown, RpaScenarioOperationResultNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<SearchRpaScenarioOperationResultResponse> proxy = new RestUrlSequentialExecuter<SearchRpaScenarioOperationResultResponse>(this.connectUnit,this.restKind){
			@Override
			public SearchRpaScenarioOperationResultResponse executeMethod( DefaultApi apiClient) throws Exception{
				SearchRpaScenarioOperationResultResponse result =  apiClient.rpaSearchRpaScenarioOperationResult(searchRpaScenarioOperationResultRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RpaScenarioOperationResultNotFound | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public RpaScenarioOperationResultWithDetailResponse getRpaScenarioOperationResultWithDetail(Long resultId) throws RestConnectFailed, HinemosUnknown, RpaScenarioOperationResultNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<RpaScenarioOperationResultWithDetailResponse> proxy = new RestUrlSequentialExecuter<RpaScenarioOperationResultWithDetailResponse>(this.connectUnit,this.restKind){
			@Override
			public RpaScenarioOperationResultWithDetailResponse executeMethod( DefaultApi apiClient) throws Exception{
				RpaScenarioOperationResultWithDetailResponse result =  apiClient.rpaGetRpaScenarioOperationResultWithDetail(resultId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RpaScenarioOperationResultNotFound | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<GetRpaScenarioListResponse> getRpaScenarioList(GetRpaScenarioListRequest getRpaScenarioListRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<GetRpaScenarioListResponse>> proxy = new RestUrlSequentialExecuter<List<GetRpaScenarioListResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<GetRpaScenarioListResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<GetRpaScenarioListResponse> result =  apiClient.rpaGetRpaScenarioList(getRpaScenarioListRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public RpaScenarioResponse addRpaScenario(AddRpaScenarioRequest addRpaScenarioRequest) throws RestConnectFailed, HinemosUnknown, RpaScenarioDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<RpaScenarioResponse> proxy = new RestUrlSequentialExecuter<RpaScenarioResponse>(this.connectUnit,this.restKind){
			@Override
			public RpaScenarioResponse executeMethod( DefaultApi apiClient) throws Exception{
				RpaScenarioResponse result = apiClient.rpaAddRpaScenario(addRpaScenarioRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RpaScenarioDuplicate | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public RpaScenarioResponse modifyRpaScenario(String scenarioId, ModifyRpaScenarioRequest modifyRpaScenarioRequest) throws RestConnectFailed,HinemosUnknown, RpaScenarioNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<RpaScenarioResponse> proxy = new RestUrlSequentialExecuter<RpaScenarioResponse>(this.connectUnit,this.restKind){
			@Override
			public RpaScenarioResponse executeMethod( DefaultApi apiClient) throws Exception{
				RpaScenarioResponse result =  apiClient.rpaModifyRpaScenario(scenarioId, modifyRpaScenarioRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RpaScenarioNotFound | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<RpaScenarioResponse> deleteRpaScenario(String scenarioIdList) throws RestConnectFailed,HinemosUnknown, RpaScenarioNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RpaScenarioResponse>> proxy = new RestUrlSequentialExecuter<List<RpaScenarioResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<RpaScenarioResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<RpaScenarioResponse> result =  apiClient.rpaDeleteRpaScenario(scenarioIdList);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RpaScenarioNotFound | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public RpaScenarioOperationResultCreateSettingResponse getRpascenarioOperationResultCreateSetting(String settingId)
			throws RestConnectFailed, HinemosUnknown, RpaScenarioOperationResultCreateSettingNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<RpaScenarioOperationResultCreateSettingResponse> proxy = new RestUrlSequentialExecuter<RpaScenarioOperationResultCreateSettingResponse>(this.connectUnit,this.restKind) {
			@Override
			public RpaScenarioOperationResultCreateSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.rpaGetRpaScenarioOperationResultCreateSetting(settingId);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RpaScenarioOperationResultCreateSettingNotFound | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public RpaScenarioOperationResultCreateSettingResponse addRpascenarioOperationResultCreateSetting(AddRpaScenarioOperationResultCreateSettingRequest request)
			throws RestConnectFailed, HinemosUnknown, RpaScenarioOperationResultCreateSettingDuplicate, InvalidSetting, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<RpaScenarioOperationResultCreateSettingResponse> proxy = new RestUrlSequentialExecuter<RpaScenarioOperationResultCreateSettingResponse>(this.connectUnit,this.restKind) {
			@Override
			public RpaScenarioOperationResultCreateSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.rpaAddRpaScenarioOperationResultCreateSetting(request);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RpaScenarioOperationResultCreateSettingDuplicate | InvalidSetting | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public RpaScenarioOperationResultCreateSettingResponse modifyRpascenarioOperationResultCreateSetting(String settingId, ModifyRpaScenarioOperationResultCreateSettingRequest request)
			throws RestConnectFailed, HinemosUnknown, RpaScenarioOperationResultNotFound, InvalidSetting, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<RpaScenarioOperationResultCreateSettingResponse> proxy = new RestUrlSequentialExecuter<RpaScenarioOperationResultCreateSettingResponse>(this.connectUnit,this.restKind) {
			@Override
			public RpaScenarioOperationResultCreateSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				return apiClient.rpaModifyRpaScenarioOperationResultCreateSetting(settingId, request);
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RpaScenarioOperationResultNotFound | InvalidSetting | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<RpaScenarioOperationResultCreateSettingResponse> deleteRpaScenarioOperationResultCreateSetting(String settingIds) throws RestConnectFailed, HinemosUnknown, RpaScenarioOperationResultCreateSettingNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RpaScenarioOperationResultCreateSettingResponse>> proxy = new RestUrlSequentialExecuter<List<RpaScenarioOperationResultCreateSettingResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<RpaScenarioOperationResultCreateSettingResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<RpaScenarioOperationResultCreateSettingResponse> result = apiClient.rpaDeleteRpaScenarioOperationResultCreateSetting(settingIds);
				return result;
			}
		};

		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RpaScenarioOperationResultCreateSettingNotFound | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<RpaScenarioOperationResultCreateSettingResponse> setRpaScenarioOperationResultCreateSettingValid(SetRpaScenarioOperationResultCreateSettingValidRequest request) throws RestConnectFailed, HinemosUnknown, RpaScenarioOperationResultNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RpaScenarioOperationResultCreateSettingResponse>> proxy = new RestUrlSequentialExecuter<List<RpaScenarioOperationResultCreateSettingResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<RpaScenarioOperationResultCreateSettingResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<RpaScenarioOperationResultCreateSettingResponse> result = apiClient.rpaSetScenarioOperationResultCreateSettingValid(request);
				return result;
			}
		};

		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RpaScenarioOperationResultNotFound | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public void correctExecNode(CorrectExecNodeRequest request) 
			 throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(this.connectUnit,this.restKind) {
			@Override
			public Void executeMethod(DefaultApi apiClient) throws Exception {
				apiClient.rpaCorrectExecNode(request);
				return null;
			}
		};
			try {
				proxy.proxyExecute();
			} catch (RestConnectFailed | HinemosUnknown  | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
				throw def;
			} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
				throw new HinemosUnknown(unknown);
			}
			
		}

	public void updateOperationResult(UpdateRpaScenarioOperationResultRequest request) 
			 throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(this.connectUnit,this.restKind) {
			@Override
			public Void executeMethod(DefaultApi apiClient) throws Exception {
				apiClient.rpaUpdateOperationResult(request);
				return null;
			}
		};
			try {
				proxy.proxyExecute();
			} catch (RestConnectFailed | HinemosUnknown  | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
				throw def;
			} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
				throw new HinemosUnknown(unknown);
			}
			
		}

	public List<RpaScenarioOperationResultCreateSettingResponse> getRpaScenarioOperationResultCreateSettingList() throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RpaScenarioOperationResultCreateSettingResponse>> proxy = new RestUrlSequentialExecuter<List<RpaScenarioOperationResultCreateSettingResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<RpaScenarioOperationResultCreateSettingResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<RpaScenarioOperationResultCreateSettingResponse> result = apiClient.rpaGetRpaScenarioOperationResultCreateSettingList();
				return result;
			}
		};

		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public GetRpaScenarioResponse getRpaScenario(String scenarioId) throws RestConnectFailed, HinemosUnknown, RpaScenarioNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<GetRpaScenarioResponse> proxy = new RestUrlSequentialExecuter<GetRpaScenarioResponse>(this.connectUnit,this.restKind){
			@Override
			public GetRpaScenarioResponse executeMethod( DefaultApi apiClient) throws Exception{
				GetRpaScenarioResponse result =  apiClient.rpaGetRpaScenario(scenarioId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RpaScenarioNotFound | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public GetRpaScenarioCorrectExecNodeResponse getRpaScenarioCorrectExecNode(String scenarioId) throws RestConnectFailed, HinemosUnknown, RpaScenarioNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<GetRpaScenarioCorrectExecNodeResponse> proxy = 
				new RestUrlSequentialExecuter<GetRpaScenarioCorrectExecNodeResponse>(this.connectUnit,this.restKind){
			@Override
			public GetRpaScenarioCorrectExecNodeResponse executeMethod( DefaultApi apiClient) throws Exception{
				GetRpaScenarioCorrectExecNodeResponse result =  apiClient.rpaGetRpaScenarioCorrectExecNode(scenarioId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | RpaScenarioNotFound | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public GetRpaScenarioOperationResultSummaryForBarResponse getRpaScenarioOperationResultSummaryForBar(String facilityId, Long targetMonth, SummaryTypeEnum dataType, Integer limit) throws RestConnectFailed, HinemosUnknown, InvalidRole {
		RestUrlSequentialExecuter<GetRpaScenarioOperationResultSummaryForBarResponse> proxy = 
				new RestUrlSequentialExecuter<GetRpaScenarioOperationResultSummaryForBarResponse>(this.connectUnit,this.restKind){
			@Override
			public GetRpaScenarioOperationResultSummaryForBarResponse executeMethod( DefaultApi apiClient) throws Exception{
				GetRpaScenarioOperationResultSummaryForBarResponse result =  apiClient.rpaGetRpaScenarioOperationResultSummaryForBar(facilityId, targetMonth, dataType.name(), limit);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public GetRpaScenarioOperationResultSummaryForPieResponse getRpaScenarioOperationResultSummaryForPie(String facilityId, Long targetMonth, SummaryTypeEnum dataType) throws RestConnectFailed, HinemosUnknown, InvalidRole {
		RestUrlSequentialExecuter<GetRpaScenarioOperationResultSummaryForPieResponse> proxy = 
				new RestUrlSequentialExecuter<GetRpaScenarioOperationResultSummaryForPieResponse>(this.connectUnit,this.restKind){
			@Override
			public GetRpaScenarioOperationResultSummaryForPieResponse executeMethod( DefaultApi apiClient) throws Exception{
				GetRpaScenarioOperationResultSummaryForPieResponse result =  apiClient.rpaGetRpaScenarioOperationResultSummaryForPie(facilityId, targetMonth, dataType.name());
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public File downloadRecords(DownloadRpaScenarioOperationResultRecordsRequest downloadRecordsRequest)
			throws RestConnectFailed, InvalidSetting, HinemosUnknown {
		RestUrlSequentialExecuter<File> proxy = new RestUrlSequentialExecuter<File>(this.connectUnit,this.restKind){
			@Override
			public File executeMethod( DefaultApi apiClient) throws Exception{
				File result =  apiClient.rpaDownloadRpaScenarioOperationResultRecords(downloadRecordsRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidSetting | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<RpaScenarioCoefficientPatternResponse> getRpaScenarioCoefficientPatternList() throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RpaScenarioCoefficientPatternResponse>> proxy = new RestUrlSequentialExecuter<List<RpaScenarioCoefficientPatternResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<RpaScenarioCoefficientPatternResponse> executeMethod(DefaultApi apiClient) throws Exception{
				List<RpaScenarioCoefficientPatternResponse> result = apiClient.rpaGetRpaScenarioCoefficientPatternList();
				return result;
			}
		};

		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public RpaScenarioCoefficientPatternResponse deleteRpaScenarioCoefficientPattern(String rpaToolEnvId, Integer orderNo)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<RpaScenarioCoefficientPatternResponse> proxy =
				new RestUrlSequentialExecuter<RpaScenarioCoefficientPatternResponse>(this.connectUnit,this.restKind){
			@Override
			public RpaScenarioCoefficientPatternResponse executeMethod( DefaultApi apiClient) throws Exception{
				RpaScenarioCoefficientPatternResponse result =  apiClient.rpaDeleteRpaScenarioCoefficientPattern(rpaToolEnvId, orderNo);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
}
