/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AddCloudLoginUserRequest;
import org.openapitools.client.model.AddCloudScopeRequest;
import org.openapitools.client.model.AttachStorageRequest;
import org.openapitools.client.model.AutoAssignNodePatternEntryInfoResponse;
import org.openapitools.client.model.BillingResultResponse;
import org.openapitools.client.model.CheckPublishResponse;
import org.openapitools.client.model.CloneBackupedInstanceRequest;
import org.openapitools.client.model.CloneBackupedStorageRequest;
import org.openapitools.client.model.CloudLoginUserInfoResponse;
import org.openapitools.client.model.CloudLoginUserInfoResponseP1;
import org.openapitools.client.model.CloudPlatformInfoResponse;
import org.openapitools.client.model.CloudScopeInfoResponse;
import org.openapitools.client.model.CloudScopeInfoResponseP1;
import org.openapitools.client.model.CreateStorageSnapshotRequest;
import org.openapitools.client.model.DeleteStorageSnapshotRequest;
import org.openapitools.client.model.DetachStorageRequest;
import org.openapitools.client.model.GetCloudScopesResponse;
import org.openapitools.client.model.GetInstanceWithStorageResponse;
import org.openapitools.client.model.GetPlatformServiceForLoginUserResponse;
import org.openapitools.client.model.GetPlatformServicesOnlyServiceNameResponse;
import org.openapitools.client.model.GetPlatformServicesResponse;
import org.openapitools.client.model.HRepositoryResponse;
import org.openapitools.client.model.InstanceBackupEntryResponse;
import org.openapitools.client.model.InstanceBackupResponse;
import org.openapitools.client.model.InstanceInfoResponse;
import org.openapitools.client.model.ModifyBillingSettingRequest;
import org.openapitools.client.model.ModifyCloudLoginUserPriorityRequest;
import org.openapitools.client.model.ModifyCloudLoginUserRequest;
import org.openapitools.client.model.ModifyCloudLoginUserRoleRelationRequest;
import org.openapitools.client.model.ModifyCloudScopeRequest;
import org.openapitools.client.model.ModifyInstanceRequest;
import org.openapitools.client.model.ModifyPlatformServiceConditionRequest;
import org.openapitools.client.model.NetworkInfoResponse;
import org.openapitools.client.model.PlatformServiceConditionResponse;
import org.openapitools.client.model.PowerOffInstances;
import org.openapitools.client.model.PowerOffInstancesRequest;
import org.openapitools.client.model.PowerOnInstances;
import org.openapitools.client.model.PowerOnInstancesRequest;
import org.openapitools.client.model.RebootInstances;
import org.openapitools.client.model.RebootInstancesRequest;
import org.openapitools.client.model.RegistAutoAssigneNodePatternRequest;
import org.openapitools.client.model.SnapshotInstanceRequest;
import org.openapitools.client.model.StorageBackupEntryResponse;
import org.openapitools.client.model.StorageBackupInfoResponse;
import org.openapitools.client.model.StorageInfoResponse;
import org.openapitools.client.model.SuspendInstances;
import org.openapitools.client.model.SuspendInstancesRequest;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
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
import com.clustercontrol.xcloud.CloudManagerException;

public class CloudRestClientWrapper implements ICheckPublishRestClientWrapper {

	private static Log m_log = LogFactory.getLog(CloudRestClientWrapper.class);
	private RestConnectUnit connectUnit;
	
	private final RestKind restKind = RestKind.CloudRestEndpoints;

	public static CloudRestClientWrapper getWrapper(String managerName) {
		return new CloudRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public CloudRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}

	@Override
	public CheckPublishResponse checkPublish() throws RestConnectFailed ,HinemosUnknown, CloudManagerException, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<CheckPublishResponse> proxy = new RestUrlSequentialExecuter<CheckPublishResponse>(this.connectUnit,this.restKind){
			@Override
			public CheckPublishResponse executeMethod( DefaultApi apiClient) throws Exception{
				CheckPublishResponse result = apiClient.xcloudCheckPublish();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed def) {//通信異常
			throw new RestConnectFailed(Messages.getString("message.hinemos.failure.transfer") + ", " + HinemosMessage.replace(def.getMessage()), def);
		} catch (CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch (UrlNotFound e) {
			// UrlNotFoundが返された場合エンドポイントがPublishされていないためメッセージを設定する
			throw new HinemosUnknown(Messages.getString("message.expiration.term"), e);
		} catch ( Exception unknown ){
			throw new HinemosUnknown(Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(unknown.getMessage()), unknown);
		}
	}

	public List<AutoAssignNodePatternEntryInfoResponse> registAutoAssigneNodePattern(String cloudScopeId, RegistAutoAssigneNodePatternRequest req) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<AutoAssignNodePatternEntryInfoResponse>> proxy = new RestUrlSequentialExecuter<List<AutoAssignNodePatternEntryInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<AutoAssignNodePatternEntryInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<AutoAssignNodePatternEntryInfoResponse> result = apiClient.xcloudRegistAutoAssigneNodePattern(cloudScopeId, req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass | InvalidSetting def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	public List<AutoAssignNodePatternEntryInfoResponse> getAutoAssigneNodePatterns(String cloudScopeId) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<AutoAssignNodePatternEntryInfoResponse>> proxy = new RestUrlSequentialExecuter<List<AutoAssignNodePatternEntryInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<AutoAssignNodePatternEntryInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<AutoAssignNodePatternEntryInfoResponse> result = apiClient.xcloudGetAutoAssigneNodePatterns(cloudScopeId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass | InvalidSetting def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	public List<AutoAssignNodePatternEntryInfoResponse> clearAutoAssigneNodePattern(String cloudScopeId) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<AutoAssignNodePatternEntryInfoResponse>> proxy = new RestUrlSequentialExecuter<List<AutoAssignNodePatternEntryInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<AutoAssignNodePatternEntryInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<AutoAssignNodePatternEntryInfoResponse> result = apiClient.xcloudClearAutoAssigneNodePattern(cloudScopeId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass | InvalidSetting def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public CloudPlatformInfoResponse getCloudPlatform(String cloudPlatformId) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<CloudPlatformInfoResponse> proxy = new RestUrlSequentialExecuter<CloudPlatformInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public CloudPlatformInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				CloudPlatformInfoResponse result = apiClient.xcloudGetCloudPlatform(cloudPlatformId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass | InvalidSetting def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<CloudPlatformInfoResponse> getAllCloudPlatforms() throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<CloudPlatformInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CloudPlatformInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<CloudPlatformInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<CloudPlatformInfoResponse> result = apiClient.xcloudGetAllCloudPlatforms();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	
	public CloudScopeInfoResponse addCloudScope(AddCloudScopeRequest req) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<CloudScopeInfoResponse> proxy = new RestUrlSequentialExecuter<CloudScopeInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public CloudScopeInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				CloudScopeInfoResponse result = apiClient.xcloudAddCloudScope(req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass | InvalidSetting def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<CloudScopeInfoResponse> removeCloudScope(String cloudScopeIds) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<CloudScopeInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CloudScopeInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<CloudScopeInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<CloudScopeInfoResponse> result = apiClient.xcloudRemoveCloudScope(cloudScopeIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	
	public CloudScopeInfoResponse modifyCloudScope(String cloudScopeId, ModifyCloudScopeRequest req) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<CloudScopeInfoResponse> proxy = new RestUrlSequentialExecuter<CloudScopeInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public CloudScopeInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				CloudScopeInfoResponse result = apiClient.xcloudModifyCloudScope(cloudScopeId, req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	
	public CloudScopeInfoResponse getCloudScope(String cloudScopeId) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<CloudScopeInfoResponse> proxy = new RestUrlSequentialExecuter<CloudScopeInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public CloudScopeInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				CloudScopeInfoResponse result = apiClient.xcloudGetCloudScope(cloudScopeId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<CloudScopeInfoResponse> getCloudScopes(String ownerRoleId) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<CloudScopeInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CloudScopeInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<CloudScopeInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<CloudScopeInfoResponse> result = null;
				GetCloudScopesResponse dtoRes = apiClient.xcloudGetCloudScopes(ownerRoleId, null);
				if (dtoRes != null) {
					result = dtoRes.getCloudScopeInfoList();
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass | InvalidSetting def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<CloudScopeInfoResponseP1> getAvailablePlatformUsers(String cloudScopeId) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<CloudScopeInfoResponseP1>> proxy = new RestUrlSequentialExecuter<List<CloudScopeInfoResponseP1>>(this.connectUnit,this.restKind){
			@Override
			public List<CloudScopeInfoResponseP1> executeMethod( DefaultApi apiClient) throws Exception{
				List<CloudScopeInfoResponseP1> result = apiClient.xcloudGetAvailablePlatformUsers(cloudScopeId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	
	public CloudLoginUserInfoResponse addCloudLoginUser(String cloudScopeId, AddCloudLoginUserRequest req) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<CloudLoginUserInfoResponse> proxy = new RestUrlSequentialExecuter<CloudLoginUserInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public CloudLoginUserInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				CloudLoginUserInfoResponse result = apiClient.xcloudAddCloudLoginUser(cloudScopeId, req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass | InvalidSetting def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	
	public CloudLoginUserInfoResponse getCloudLoginUser(String cloudScopeId, String cloudLoginUserId) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<CloudLoginUserInfoResponse> proxy = new RestUrlSequentialExecuter<CloudLoginUserInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public CloudLoginUserInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				CloudLoginUserInfoResponse result = apiClient.xcloudGetCloudLoginUser(cloudScopeId, cloudLoginUserId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass | InvalidSetting def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	
	public CloudLoginUserInfoResponse removeCloudLoginUser(String cloudScopeId, String cloudLoginUserId) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<CloudLoginUserInfoResponse> proxy = new RestUrlSequentialExecuter<CloudLoginUserInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public CloudLoginUserInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				CloudLoginUserInfoResponse result = apiClient.xcloudRemoveCloudLoginUser(cloudScopeId, cloudLoginUserId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass | InvalidSetting def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<CloudLoginUserInfoResponse> getAllCloudLoginUsers(String cloudScopeId) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<CloudLoginUserInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CloudLoginUserInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<CloudLoginUserInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<CloudLoginUserInfoResponse> result = apiClient.xcloudGetAllCloudLoginUsers(cloudScopeId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass | InvalidSetting def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	public CloudLoginUserInfoResponse modifyCloudLoginUser(String cloudScopeId, String cloudLoginUserId, ModifyCloudLoginUserRequest req) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<CloudLoginUserInfoResponse> proxy = new RestUrlSequentialExecuter<CloudLoginUserInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public CloudLoginUserInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				CloudLoginUserInfoResponse result = apiClient.xcloudModifyCloudLoginUser(cloudScopeId, cloudLoginUserId, req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass | InvalidSetting def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public List<CloudLoginUserInfoResponseP1> getCloudLoginUserOrderPriority(String cloudScopeId) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<CloudLoginUserInfoResponseP1>> proxy = new RestUrlSequentialExecuter<List<CloudLoginUserInfoResponseP1>>(this.connectUnit,this.restKind){
			@Override
			public List<CloudLoginUserInfoResponseP1> executeMethod( DefaultApi apiClient) throws Exception{
				List<CloudLoginUserInfoResponseP1> result = apiClient.xcloudGetCloudLoginUserOrderPriority(cloudScopeId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass | InvalidSetting def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<CloudLoginUserInfoResponse> modifyCloudLoginUserPriority(String cloudScopeId, ModifyCloudLoginUserPriorityRequest req) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<CloudLoginUserInfoResponse>> proxy = new RestUrlSequentialExecuter<List<CloudLoginUserInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<CloudLoginUserInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<CloudLoginUserInfoResponse> result = apiClient.xcloudModifyCloudLoginUserPriority(cloudScopeId, req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass | InvalidSetting def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	
	public CloudLoginUserInfoResponse modifyCloudLoginUserRoleRelation(String cloudScopeId, String cloudLoginUserId, ModifyCloudLoginUserRoleRelationRequest req) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<CloudLoginUserInfoResponse> proxy = new RestUrlSequentialExecuter<CloudLoginUserInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public CloudLoginUserInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				CloudLoginUserInfoResponse result = apiClient.xcloudModifyCloudLoginUserRoleRelation(cloudScopeId, cloudLoginUserId, req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass | InvalidSetting def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public List<InstanceInfoResponse> removeInstances(String cloudScopeId, String locationId, String instanceIds) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<InstanceInfoResponse>> proxy = new RestUrlSequentialExecuter<List<InstanceInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<InstanceInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<InstanceInfoResponse> result = apiClient.xcloudRemoveInstances(cloudScopeId, locationId, instanceIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	public List<InstanceInfoResponse> getInstances(String cloudScopeId, String locationId, String instanceIds) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<InstanceInfoResponse>> proxy = new RestUrlSequentialExecuter<List<InstanceInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<InstanceInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<InstanceInfoResponse> result = apiClient.xcloudGetInstances(cloudScopeId, locationId, instanceIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	public PowerOnInstances powerOnInstances(String cloudScopeId, String locationId, String instanceIds) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<PowerOnInstances> proxy = new RestUrlSequentialExecuter<PowerOnInstances>(this.connectUnit,this.restKind){
			@Override
			public PowerOnInstances executeMethod( DefaultApi apiClient) throws Exception{
				PowerOnInstancesRequest dtoReq =  new PowerOnInstancesRequest();
				dtoReq.setInstanceIds(Arrays.asList(instanceIds.split(",")));
				PowerOnInstances result = apiClient.xcloudPowerOnInstances(cloudScopeId, locationId, dtoReq);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	public PowerOffInstances powerOffInstances(String cloudScopeId, String locationId, String instanceIds) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<PowerOffInstances> proxy = new RestUrlSequentialExecuter<PowerOffInstances>(this.connectUnit,this.restKind){
			@Override
			public PowerOffInstances executeMethod( DefaultApi apiClient) throws Exception{
				PowerOffInstancesRequest dtoReq =  new PowerOffInstancesRequest();
				dtoReq.setInstanceIds(Arrays.asList(instanceIds.split(",")));
				PowerOffInstances result = apiClient.xcloudPowerOffInstances(cloudScopeId, locationId, dtoReq);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	public SuspendInstances suspendInstances(String cloudScopeId, String locationId, String instanceIds) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<SuspendInstances> proxy = new RestUrlSequentialExecuter<SuspendInstances>(this.connectUnit,this.restKind){
			@Override
			public SuspendInstances executeMethod( DefaultApi apiClient) throws Exception{
				SuspendInstancesRequest dtoReq =  new SuspendInstancesRequest();
				dtoReq.setInstanceIds(Arrays.asList(instanceIds.split(",")));
				SuspendInstances result = apiClient.xcloudSuspendInstances(cloudScopeId, locationId, dtoReq);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	public RebootInstances rebootInstances(String cloudScopeId, String locationId, String instanceIds) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<RebootInstances> proxy = new RestUrlSequentialExecuter<RebootInstances>(this.connectUnit,this.restKind){
			@Override
			public RebootInstances executeMethod( DefaultApi apiClient) throws Exception{
				RebootInstancesRequest dtoReq =  new RebootInstancesRequest();
				dtoReq.setInstanceIds(Arrays.asList(instanceIds.split(",")));
				RebootInstances result = apiClient.xcloudRebootInstances(cloudScopeId, locationId, dtoReq);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	public InstanceBackupResponse snapshotInstance(String cloudScopeId, String locationId, SnapshotInstanceRequest req) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<InstanceBackupResponse> proxy = new RestUrlSequentialExecuter<InstanceBackupResponse>(this.connectUnit,this.restKind){
			@Override
			public InstanceBackupResponse executeMethod( DefaultApi apiClient) throws Exception{
				InstanceBackupResponse result = apiClient.xcloudSnapshotInstance(cloudScopeId, locationId, req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	public List<InstanceBackupEntryResponse> deleteInstanceSnapshots(String cloudScopeId, String locationId, String instanceId, String instanceSnapshotIds) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<InstanceBackupEntryResponse>> proxy = new RestUrlSequentialExecuter<List<InstanceBackupEntryResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<InstanceBackupEntryResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<InstanceBackupEntryResponse> result = apiClient.xcloudDeleteInstanceSnapshots(cloudScopeId, locationId, instanceId, instanceSnapshotIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	public InstanceInfoResponse cloneBackupedInstance(String cloudScopeId, String locationId, CloneBackupedInstanceRequest req) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<InstanceInfoResponse> proxy = new RestUrlSequentialExecuter<InstanceInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public InstanceInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				InstanceInfoResponse result = apiClient.xcloudCloneBackupedInstance(cloudScopeId, locationId, req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public List<InstanceBackupResponse> getInstanceBackups(String cloudScopeId, String locationId, String instanceIds) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<InstanceBackupResponse>> proxy = new RestUrlSequentialExecuter<List<InstanceBackupResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<InstanceBackupResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<InstanceBackupResponse> result = apiClient.xcloudGetInstanceBackups(cloudScopeId, locationId, instanceIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	public InstanceInfoResponse modifyInstance(String cloudScopeId, String locationId, String instanceId, ModifyInstanceRequest req) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<InstanceInfoResponse> proxy = new RestUrlSequentialExecuter<InstanceInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public InstanceInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				InstanceInfoResponse result = apiClient.xcloudModifyInstance(cloudScopeId, locationId, instanceId, req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	
	public void attachStorage(String cloudScopeId, String locationId, String instanceId, AttachStorageRequest req) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(this.connectUnit,this.restKind){
			@Override
			public Void executeMethod( DefaultApi apiClient) throws Exception{
				apiClient.xcloudAttachStorage(cloudScopeId, locationId, instanceId, req);
				return null;
			}
		};
		try {
			proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public void detachStorage(String cloudScopeId, String locationId, DetachStorageRequest req) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(this.connectUnit,this.restKind){
			@Override
			public Void executeMethod( DefaultApi apiClient) throws Exception{
				apiClient.xcloudDetachStorage(cloudScopeId, locationId, req);
				return null;
			}
		};
		try {
			proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public List<StorageInfoResponse> removeStorages(String cloudScopeId, String locationId, String storageIds) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<StorageInfoResponse>> proxy = new RestUrlSequentialExecuter<List<StorageInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<StorageInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<StorageInfoResponse> result = apiClient.xcloudRemoveStorages(cloudScopeId, locationId, storageIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public List<StorageInfoResponse> getStorages(String cloudScopeId, String locationId, String storageIds) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<StorageInfoResponse>> proxy = new RestUrlSequentialExecuter<List<StorageInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<StorageInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<StorageInfoResponse> result = apiClient.xcloudGetStorages(cloudScopeId, locationId, storageIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	public StorageBackupInfoResponse snapshotStorage(String cloudScopeId, String locationId, CreateStorageSnapshotRequest req) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<StorageBackupInfoResponse> proxy = new RestUrlSequentialExecuter<StorageBackupInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public StorageBackupInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				StorageBackupInfoResponse result = apiClient.xcloudSnapshotStorage(cloudScopeId, locationId, req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public List<StorageBackupEntryResponse> deleteStorageSnapshots(String cloudScopeId, String locationId, DeleteStorageSnapshotRequest request)
			throws RestConnectFailed, HinemosUnknown, CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<StorageBackupEntryResponse>> proxy = new RestUrlSequentialExecuter<List<StorageBackupEntryResponse>>(this.connectUnit,
				this.restKind) {
			@Override
			public List<StorageBackupEntryResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<StorageBackupEntryResponse> result = apiClient.xcloudDeleteStorageSnapshots(cloudScopeId, locationId, request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public StorageInfoResponse cloneBackupedStorage(String cloudScopeId, String locationId, CloneBackupedStorageRequest req) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<StorageInfoResponse> proxy = new RestUrlSequentialExecuter<StorageInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public StorageInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				StorageInfoResponse result = apiClient.xcloudCloneBackupedStorage(cloudScopeId, locationId, req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	public List<StorageBackupInfoResponse> getStorageBackups(String cloudScopeId, String locationId, String storageIds) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<StorageBackupInfoResponse>> proxy = new RestUrlSequentialExecuter<List<StorageBackupInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<StorageBackupInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<StorageBackupInfoResponse> result = apiClient.xcloudGetStorageBackups(cloudScopeId, locationId, storageIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	public HRepositoryResponse updateLocationRepository(String cloudScopeId, String locationId) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<HRepositoryResponse> proxy = new RestUrlSequentialExecuter<HRepositoryResponse>(this.connectUnit,this.restKind){
			@Override
			public HRepositoryResponse executeMethod( DefaultApi apiClient) throws Exception{
				HRepositoryResponse result = apiClient.xcloudUpdateLocationRepository(cloudScopeId, locationId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	
	public HRepositoryResponse getRepository(String roleId) throws RestConnectFailed, HinemosUnknown, CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<HRepositoryResponse> proxy = new RestUrlSequentialExecuter<HRepositoryResponse>(this.connectUnit,this.restKind){
			@Override
			public HRepositoryResponse executeMethod( DefaultApi apiClient) throws Exception{
				HRepositoryResponse result = apiClient.xcloudGetRepository(roleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public List<PlatformServiceConditionResponse> getPlatformServiceConditions(String cloudScopeId, String locationId, String roleId) throws RestConnectFailed, HinemosUnknown, CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<PlatformServiceConditionResponse>> proxy = new RestUrlSequentialExecuter<List<PlatformServiceConditionResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<PlatformServiceConditionResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<PlatformServiceConditionResponse> result = apiClient.xcloudGetPlatformServiceConditions(cloudScopeId, locationId, roleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public List<NetworkInfoResponse> getAllNetworks(String cloudScopeId, String locationId) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<NetworkInfoResponse>> proxy = new RestUrlSequentialExecuter<List<NetworkInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<NetworkInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<NetworkInfoResponse> result = apiClient.xcloudGetAllNetworks(cloudScopeId, locationId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	public CloudScopeInfoResponse modifyBillingSetting(String cloudScopeId, ModifyBillingSettingRequest req) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<CloudScopeInfoResponse> proxy = new RestUrlSequentialExecuter<CloudScopeInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public CloudScopeInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				CloudScopeInfoResponse result = apiClient.modifyBillingSetting(cloudScopeId, req);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	public BillingResultResponse getBillingDetailsByCloudScope(String cloudScopeId, int year, int month) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<BillingResultResponse> proxy = new RestUrlSequentialExecuter<BillingResultResponse>(this.connectUnit,this.restKind){
			@Override
			public BillingResultResponse executeMethod( DefaultApi apiClient) throws Exception{
				BillingResultResponse result = apiClient.xcloudGetBillingDetailsByCloudScope(cloudScopeId, String.valueOf(year), String.valueOf(month));
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	
	public File downloadBillingDetailsByCloudScope(String cloudScopeId, int year, int month) throws RestConnectFailed, CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidUserPass {
		RestUrlSequentialExecuter<File> proxy = new RestUrlSequentialExecuter<File>(this.connectUnit,this.restKind){
			@Override
			public File executeMethod(DefaultApi apiClient) throws Exception {
				File result = apiClient.xcloudDownloadBillingDetailsByCloudScope(cloudScopeId, String.valueOf(year), String.valueOf(month));
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | CloudManagerException | InvalidRole | HinemosUnknown | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public File downloadBillingDetailsByFacility(String facilityId, int year, int month) throws RestConnectFailed, CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidUserPass {
		RestUrlSequentialExecuter<File> proxy = new RestUrlSequentialExecuter<File>(this.connectUnit,this.restKind){
			@Override
			public File executeMethod(DefaultApi apiClient) throws Exception {
				File result = apiClient.xcloudDownloadBillingDetailsByFacility(facilityId, String.valueOf(year), String.valueOf(month));
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | CloudManagerException | InvalidRole | HinemosUnknown | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public BillingResultResponse getBillingDetailsByFacility(String facilityId, int year, int month) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<BillingResultResponse> proxy = new RestUrlSequentialExecuter<BillingResultResponse>(this.connectUnit,this.restKind){
			@Override
			public BillingResultResponse executeMethod( DefaultApi apiClient) throws Exception{
				BillingResultResponse result = apiClient.xcloudGetBillingDetailsByFacility(facilityId, String.valueOf(year), String.valueOf(month));
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	public GetPlatformServicesOnlyServiceNameResponse getPlatformServicesOnlyServiceName(String cloudScopeId) throws RestConnectFailed, HinemosUnknown,  CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<GetPlatformServicesOnlyServiceNameResponse> proxy = new RestUrlSequentialExecuter<GetPlatformServicesOnlyServiceNameResponse>(this.connectUnit,this.restKind){
			@Override
			public GetPlatformServicesOnlyServiceNameResponse executeMethod( DefaultApi apiClient) throws Exception{
				GetPlatformServicesOnlyServiceNameResponse result = apiClient.xcloudGetPlatformServicesOnlyServiceName(cloudScopeId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public GetPlatformServiceForLoginUserResponse getPlatformServiceForLoginUser(String facilityId, String roleId)
			throws RestConnectFailed, HinemosUnknown, CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<GetPlatformServiceForLoginUserResponse> proxy = new RestUrlSequentialExecuter<GetPlatformServiceForLoginUserResponse>(this.connectUnit,this.restKind){
			@Override
			public GetPlatformServiceForLoginUserResponse executeMethod( DefaultApi apiClient) throws Exception{
				GetPlatformServiceForLoginUserResponse result = apiClient.xcloudGetPlatformServiceForLoginUser(facilityId, roleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}

	public GetPlatformServicesResponse getPlatformServices(String facilityId, String roleId)
			throws RestConnectFailed, HinemosUnknown, CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<GetPlatformServicesResponse> proxy = new RestUrlSequentialExecuter<GetPlatformServicesResponse>(this.connectUnit,
				this.restKind) {
			@Override
			public GetPlatformServicesResponse executeMethod(DefaultApi apiClient) throws Exception {
				GetPlatformServicesResponse result = apiClient.xcloudGetPlatformServices(facilityId, roleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public GetInstanceWithStorageResponse getInstancesWithStorage(String cloudScopeId, String locationId, String instanceId)
			throws RestConnectFailed, HinemosUnknown, CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<GetInstanceWithStorageResponse> proxy = new RestUrlSequentialExecuter<GetInstanceWithStorageResponse>(this.connectUnit,this.restKind){
			@Override
			public GetInstanceWithStorageResponse executeMethod( DefaultApi apiClient) throws Exception{
				GetInstanceWithStorageResponse result = apiClient.xcloudGetInstanceWithStorage(cloudScopeId, locationId, instanceId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch ( Exception unknown ){
			throw new HinemosUnknown(unknown);
		}
	}
	
	public File downloadBillingDetailsByCloudScope(String facilityId, Integer year, Integer month)
			throws RestConnectFailed, HinemosUnknown, InvalidSetting, CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<File> proxy = new RestUrlSequentialExecuter<File>(this.connectUnit,
				this.restKind) {
			@Override
			public File executeMethod(DefaultApi apiClient) throws Exception {
				File result = apiClient.xcloudDownloadBillingDetailsByCloudScope(facilityId, String.valueOf(year), String.valueOf(month));
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidSetting | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<PlatformServiceConditionResponse> modifyPlatformServiceCondition(String cloudScopeId, String locationId, ModifyPlatformServiceConditionRequest request)
			throws RestConnectFailed, HinemosUnknown, InvalidSetting, CloudManagerException, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<PlatformServiceConditionResponse>> proxy = new RestUrlSequentialExecuter<List<PlatformServiceConditionResponse>>(this.connectUnit,
				this.restKind) {
			@Override
			public List<PlatformServiceConditionResponse> executeMethod(DefaultApi apiClient) throws Exception {
				request.setLocationId(locationId);
				List<PlatformServiceConditionResponse> result = apiClient.xcloudModifyPlatformServiceCondition(cloudScopeId, request);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidSetting | CloudManagerException | InvalidRole | InvalidUserPass def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}	
}
