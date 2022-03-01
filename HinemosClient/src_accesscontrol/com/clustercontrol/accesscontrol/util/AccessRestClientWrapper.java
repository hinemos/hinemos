/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.accesscontrol.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AddRoleInfoRequest;
import org.openapitools.client.model.AddUserInfoRequest;
import org.openapitools.client.model.AssignUserWithRoleRequest;
import org.openapitools.client.model.ChangeOwnPasswordRequest;
import org.openapitools.client.model.ChangePasswordRequest;
import org.openapitools.client.model.ConnectCheckResponse;
import org.openapitools.client.model.HasSystemPrivilegeRequest;
import org.openapitools.client.model.HasSystemPrivilegeResponse;
import org.openapitools.client.model.LoginRequest;
import org.openapitools.client.model.LoginResponse;
import org.openapitools.client.model.LogoutResponse;
import org.openapitools.client.model.ModifyRoleInfoRequest;
import org.openapitools.client.model.ModifyUserInfoRequest;
import org.openapitools.client.model.ObjectPrivilegeInfoResponse;
import org.openapitools.client.model.ObjectPrivilegeInfoResponseP1;
import org.openapitools.client.model.ReplaceObjectPrivilegeRequest;
import org.openapitools.client.model.ReplaceSystemPrivilegeWithRoleRequest;
import org.openapitools.client.model.RoleInfoResponse;
import org.openapitools.client.model.RoleInfoResponseP2;
import org.openapitools.client.model.RoleInfoResponseP3;
import org.openapitools.client.model.RoleTreeItemResponseP1;
import org.openapitools.client.model.SystemPrivilegeInfoResponse;
import org.openapitools.client.model.SystemPrivilegeInfoResponseP1;
import org.openapitools.client.model.UserInfoResponse;
import org.openapitools.client.model.UserInfoResponseP1;
import org.openapitools.client.model.UserInfoResponseP2;
import org.openapitools.client.model.UserInfoResponseP3;
import org.openapitools.client.model.Version;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.PrivilegeDuplicate;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.RoleDuplicate;
import com.clustercontrol.fault.RoleNotFound;
import com.clustercontrol.fault.UnEditableRole;
import com.clustercontrol.fault.UnEditableUser;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.fault.UsedObjectPrivilege;
import com.clustercontrol.fault.UsedOwnerRole;
import com.clustercontrol.fault.UsedRole;
import com.clustercontrol.fault.UsedUser;
import com.clustercontrol.fault.UserDuplicate;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.rest.ApiException;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.util.ExceptionUtil;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestConnectUnit.RestUrlSetting;
import com.clustercontrol.util.RestUrlSequentialExecuter;

public class AccessRestClientWrapper {

	private static Log m_log = LogFactory.getLog( AccessRestClientWrapper.class );
	private RestConnectUnit connectUnit;
	
	private final RestKind restKind = RestKind.AccessRestEndpoints;

	public static AccessRestClientWrapper getWrapper(String managerName) {
		return new AccessRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public AccessRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}
	
	public LoginResponse loginByUrl( RestUrlSetting urlSetting ,LoginRequest req) throws RestConnectFailed ,HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting {
		try {
			DefaultApi apiClient = new DefaultApi(urlSetting.getApiClientForAuthorization());
			LoginResponse res = apiClient.accessLogin(req);
			//URLに対応したログイン関連情報を保管
			connectUnit.setHinemosToken(urlSetting.getUrlPrefix(), res.getToken());
			if( m_log.isDebugEnabled() ){
				m_log.debug("loginByUrl : login success. BaseUrl="+urlSetting.getUrlPrefix() + " token="+res.getToken() );
			}
			return res;
		} catch (ApiException e) {
			try {
				throw ExceptionUtil.conversionApiException(e);
			} catch (RestConnectFailed connectFail) { //マネージャ接続エラー
				throw connectFail;
			} catch ( HinemosUnknown | InvalidRole| InvalidUserPass | InvalidSetting def) {//想定内例外
				throw def;
			} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
				throw new HinemosUnknown(unknown);
			}
		}
	}

	public LoginResponse login( LoginRequest req) throws RestConnectFailed ,HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting {
		RestConnectFailed ape = null;
		for (RestUrlSetting urlSetting : connectUnit.getUrlSettingList(this.restKind) ) {
			try {
				return loginByUrl(urlSetting,req);
			} catch (RestConnectFailed e) {
				ape=e;
				connectUnit.setUnreached(urlSetting);
			}
		}
		throw ape;
	}

	public ConnectCheckResponse connectCheckByUrl(RestUrlSetting urlSetting ) throws RestConnectFailed ,HinemosUnknown , InvalidRole, InvalidUserPass {
		try {
			DefaultApi apiClient = new DefaultApi(urlSetting.getApiClient());
			ConnectCheckResponse result = apiClient.accessConnectCheck();
			return result;
		} catch (ApiException e) {
			try {
				throw ExceptionUtil.conversionApiException(e);
			} catch (RestConnectFailed connectFail) { //マネージャ接続エラー
				throw connectFail;
			} catch ( HinemosUnknown | InvalidRole| InvalidUserPass def) {//想定内例外 API個別に判断
				throw def;
			} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
				throw new HinemosUnknown(unknown);
			}
		}
	}

	public ConnectCheckResponse connectCheck() throws RestConnectFailed ,HinemosUnknown , InvalidRole, InvalidUserPass {
		RestConnectFailed ape = null;
		for (RestUrlSetting urlSetting : connectUnit.getUrlSettingList(this.restKind) ) {
			try {
				return connectCheckByUrl(urlSetting);
			} catch (RestConnectFailed e) {
				ape=e;
				m_log.warn("connectCheck(), " + e.getMessage());
				connectUnit.setUnreached(urlSetting);
			}
		}
		throw ape;
	}

	
	public LogoutResponse logout() throws RestConnectFailed ,HinemosUnknown, InvalidRole, InvalidUserPass {
		m_log.debug("logout : start");
		RestUrlSequentialExecuter<LogoutResponse> proxy = new RestUrlSequentialExecuter<LogoutResponse>(this.connectUnit,this.restKind){
			@Override
			public LogoutResponse executeMethod( DefaultApi apiClient) throws Exception{
				LogoutResponse result = apiClient.accessLogout();
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

	public LoginResponse relogin() throws RestConnectFailed ,HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<LoginResponse> proxy = new RestUrlSequentialExecuter<LoginResponse>(this.connectUnit,this.restKind){
			@Override
			public LoginResponse executeMethod( DefaultApi apiClient) throws Exception{
				LoginResponse result = apiClient.accessRelogin();
				//URLに対応したログイン関連情報を保管
				connectUnit.setHinemosToken(this.getTargetUrl().getUrlPrefix(), result.getToken());
				if( m_log.isDebugEnabled() ){
					m_log.debug("relogin : relogin success. BaseUrl="+this.getTargetUrl().getUrlPrefix() + " token="+result.getToken() );
				}
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

	public RoleInfoResponse getRoleInfo (String roleId ) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, RoleNotFound {
		RestUrlSequentialExecuter<RoleInfoResponse> proxy = new RestUrlSequentialExecuter<RoleInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public RoleInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				RoleInfoResponse result =  apiClient.accessGetRoleInfo(roleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole| InvalidUserPass| RoleNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<RoleInfoResponse> getRoleInfoList() throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<List<RoleInfoResponse>> proxy = new RestUrlSequentialExecuter<List<RoleInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<RoleInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<RoleInfoResponse> result =  apiClient.accessGetRoleInfoList();
				
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

	public RoleInfoResponse addRoleInfo( AddRoleInfoRequest addRoleInfoRequest ) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, RoleDuplicate, FacilityDuplicate, InvalidSetting {
		RestUrlSequentialExecuter<RoleInfoResponse> proxy = new RestUrlSequentialExecuter<RoleInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public RoleInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				RoleInfoResponse result =  apiClient.accessAddRoleInfo( addRoleInfoRequest );
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole| InvalidUserPass | RoleDuplicate | FacilityDuplicate| InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public UserInfoResponse addUserInfo( AddUserInfoRequest addUserInfoRequest ) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass,  UserDuplicate, InvalidSetting {
		RestUrlSequentialExecuter<UserInfoResponse> proxy = new RestUrlSequentialExecuter<UserInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public UserInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				UserInfoResponse  result =  apiClient.accessAddUserInfo( addUserInfoRequest );
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole| InvalidUserPass |UserDuplicate |InvalidSetting  def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public RoleInfoResponse assignUserWithRole(String roleId, AssignUserWithRoleRequest assignUserWithRoleRequest)  throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, RoleDuplicate, InvalidSetting, UnEditableRole {
		RestUrlSequentialExecuter<RoleInfoResponse> proxy = new RestUrlSequentialExecuter<RoleInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public RoleInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				RoleInfoResponse  result =  apiClient.accessAssignUserWithRole(roleId, assignUserWithRoleRequest );
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole |InvalidUserPass |UnEditableRole |InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public UserInfoResponseP1 changeOwnPassword(ChangeOwnPasswordRequest changeOwnPasswordRequest)  throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting,UserNotFound {
		RestUrlSequentialExecuter<UserInfoResponseP1> proxy = new RestUrlSequentialExecuter<UserInfoResponseP1>(this.connectUnit,this.restKind){
			@Override
			public UserInfoResponseP1 executeMethod( DefaultApi apiClient) throws Exception{
				UserInfoResponseP1  result =  apiClient.accessChangeOwnPassword(changeOwnPasswordRequest) ;
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed |HinemosUnknown |InvalidRole |InvalidUserPass | InvalidSetting| UserNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public UserInfoResponseP1 changePassword(String userId, ChangePasswordRequest changePasswordRequest)  throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, UserNotFound, InvalidSetting {
		RestUrlSequentialExecuter<UserInfoResponseP1> proxy = new RestUrlSequentialExecuter<UserInfoResponseP1>(this.connectUnit,this.restKind){
			@Override
			public UserInfoResponseP1 executeMethod( DefaultApi apiClient) throws Exception{
				UserInfoResponseP1  result =  apiClient.accessChangePassword( userId, changePasswordRequest ) ;
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole| InvalidUserPass| UserNotFound | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<RoleInfoResponse> deleteRoleInfo( String roleIds)  throws RestConnectFailed ,InvalidUserPass, InvalidRole, HinemosUnknown, RoleNotFound, UnEditableRole, UsedRole, FacilityNotFound, UsedFacility, UsedOwnerRole {
		RestUrlSequentialExecuter<List<RoleInfoResponse>> proxy = new RestUrlSequentialExecuter<List<RoleInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public  List<RoleInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<RoleInfoResponse>  result =  apiClient.accessDeleteRoleInfo(roleIds) ;
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed |InvalidUserPass |InvalidRole |HinemosUnknown |RoleNotFound |UnEditableRole |UsedRole |FacilityNotFound |UsedFacility |UsedOwnerRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<UserInfoResponse> deleteUserInfo(String userIds)  throws RestConnectFailed, HinemosUnknown ,InvalidRole, InvalidUserPass, UserNotFound, UsedUser, UnEditableUser {
		RestUrlSequentialExecuter<List<UserInfoResponse>> proxy = new RestUrlSequentialExecuter<List<UserInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public  List<UserInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<UserInfoResponse> result =  apiClient.accessDeleteUserInfo(userIds) ;
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed |HinemosUnknown |InvalidRole |InvalidUserPass |UserNotFound |UsedUser |UnEditableUser def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<ObjectPrivilegeInfoResponse> getObjectPrivilegeInfoList(String objectType, String objectId, String roleId, String objectPrivilege) 
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<List<ObjectPrivilegeInfoResponse>> proxy = new RestUrlSequentialExecuter<List<ObjectPrivilegeInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<ObjectPrivilegeInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<ObjectPrivilegeInfoResponse> result =  apiClient.accessGetObjectPrivilegeInfoList(objectType, objectId, roleId, objectPrivilege) ;
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
	
	public UserInfoResponseP3 getOwnerRoleIdList() throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<UserInfoResponseP3> proxy = new RestUrlSequentialExecuter<UserInfoResponseP3>(this.connectUnit,this.restKind){
			@Override
			public UserInfoResponseP3 executeMethod( DefaultApi apiClient) throws Exception{
				UserInfoResponseP3 result =  apiClient.accessGetOwnerRoleIdList() ;
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

	public UserInfoResponse getOwnUserInfo() throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<UserInfoResponse> proxy = new RestUrlSequentialExecuter<UserInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public UserInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				UserInfoResponse result =  apiClient.accessGetOwnUserInfo() ;
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

	public RoleTreeItemResponseP1 getRoleTree() throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, UserNotFound {
		RestUrlSequentialExecuter<RoleTreeItemResponseP1> proxy = new RestUrlSequentialExecuter<RoleTreeItemResponseP1>(this.connectUnit,this.restKind){
			@Override
			public RoleTreeItemResponseP1 executeMethod( DefaultApi apiClient) throws Exception{
				RoleTreeItemResponseP1 result =  apiClient.accessGetRoleTree() ;
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole| InvalidUserPass| UserNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<SystemPrivilegeInfoResponse> getSystemPrivilegeInfoList(String editType) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<List<SystemPrivilegeInfoResponse>> proxy = new RestUrlSequentialExecuter<List<SystemPrivilegeInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<SystemPrivilegeInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<SystemPrivilegeInfoResponse> result =  apiClient.accessGetSystemPrivilegeInfoList(editType) ;
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

	public List<SystemPrivilegeInfoResponse> getSystemPrivilegeInfoListByRoleId(String roleId) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<List<SystemPrivilegeInfoResponse>> proxy = new RestUrlSequentialExecuter<List<SystemPrivilegeInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<SystemPrivilegeInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<SystemPrivilegeInfoResponse> result =  apiClient.accessGetSystemPrivilegeInfoListByRoleId(roleId) ;
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

	public List<SystemPrivilegeInfoResponseP1> getSystemPrivilegeInfoListByUserId(String userId) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<List<SystemPrivilegeInfoResponseP1>> proxy = new RestUrlSequentialExecuter<List<SystemPrivilegeInfoResponseP1>>(this.connectUnit,this.restKind){
			@Override
			public List<SystemPrivilegeInfoResponseP1> executeMethod( DefaultApi apiClient) throws Exception{
				List<SystemPrivilegeInfoResponseP1> result =  apiClient.accessGetSystemPrivilegeInfoListByUserId(userId) ;
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

	public UserInfoResponse getUserInfo(String userId) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<UserInfoResponse> proxy = new RestUrlSequentialExecuter<UserInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public UserInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				UserInfoResponse result =  apiClient.accessGetUserInfo(userId) ;
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

	public List<UserInfoResponse> getUserInfoList() throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<List<UserInfoResponse>> proxy = new RestUrlSequentialExecuter<List<UserInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<UserInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<UserInfoResponse> result =  apiClient.accessGetUserInfoList() ;
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

	public UserInfoResponseP2 getUserName() throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, UserNotFound {
		RestUrlSequentialExecuter<UserInfoResponseP2> proxy = new RestUrlSequentialExecuter<UserInfoResponseP2>(this.connectUnit,this.restKind){
			@Override
			public UserInfoResponseP2 executeMethod( DefaultApi apiClient) throws Exception{
				UserInfoResponseP2 result =  apiClient.accessGetUserName() ;
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole| InvalidUserPass| UserNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public Version getVersion() throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<Version> proxy = new RestUrlSequentialExecuter<Version>(this.connectUnit,this.restKind){
			@Override
			public Version executeMethod( DefaultApi apiClient) throws Exception{
				Version result =  apiClient.accessGetVersion();
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

	public HasSystemPrivilegeResponse hasSystemPrivilege(HasSystemPrivilegeRequest hasSystemPrivilegeRequest)  throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting {
		RestUrlSequentialExecuter<HasSystemPrivilegeResponse> proxy = new RestUrlSequentialExecuter<HasSystemPrivilegeResponse>(this.connectUnit,this.restKind){
			@Override
			public HasSystemPrivilegeResponse executeMethod( DefaultApi apiClient) throws Exception{
				HasSystemPrivilegeResponse result =  apiClient.accessHasSystemPrivilege(hasSystemPrivilegeRequest) ;
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole| InvalidUserPass |InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public RoleInfoResponse modifyRoleInfo(String roleId, ModifyRoleInfoRequest modifyRoleInfoRequest) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass,  RoleNotFound, FacilityNotFound, InvalidSetting, UnEditableRole {
		RestUrlSequentialExecuter<RoleInfoResponse> proxy = new RestUrlSequentialExecuter<RoleInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public RoleInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				RoleInfoResponse result =  apiClient.accessModifyRoleInfo(roleId, modifyRoleInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole| InvalidUserPass | RoleNotFound | FacilityNotFound| InvalidSetting| UnEditableRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public UserInfoResponse modifyUserInfo(String userId, ModifyUserInfoRequest modifyUserInfoRequest) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass ,UserNotFound, UnEditableUser, InvalidSetting {
		RestUrlSequentialExecuter<UserInfoResponse> proxy = new RestUrlSequentialExecuter<UserInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public UserInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				UserInfoResponse result =  apiClient.accessModifyUserInfo (userId, modifyUserInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole| InvalidUserPass |UserNotFound |UnEditableUser |InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<ObjectPrivilegeInfoResponse> replaceObjectPrivilege(ReplaceObjectPrivilegeRequest replaceObjectPrivilegeRequest ) 
			throws RestConnectFailed, PrivilegeDuplicate, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, UsedObjectPrivilege, JobMasterNotFound  {
		RestUrlSequentialExecuter<List<ObjectPrivilegeInfoResponse>> proxy = new RestUrlSequentialExecuter<List<ObjectPrivilegeInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<ObjectPrivilegeInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<ObjectPrivilegeInfoResponse> result =  apiClient.accessReplaceObjectPrivilege( replaceObjectPrivilegeRequest ) ;
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed |HinemosUnknown |InvalidRole |InvalidUserPass | InvalidSetting |UsedObjectPrivilege |JobMasterNotFound| PrivilegeDuplicate def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<SystemPrivilegeInfoResponse> replaceSystemPrivilegeWithRole(String roleId, ReplaceSystemPrivilegeWithRoleRequest replaceSystemPrivilegeWithRoleRequest) 
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, UnEditableRole {
		RestUrlSequentialExecuter<List<SystemPrivilegeInfoResponse>> proxy = new RestUrlSequentialExecuter<List<SystemPrivilegeInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<SystemPrivilegeInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<SystemPrivilegeInfoResponse> result =  apiClient.accessReplaceSystemPrivilegeWithRole( roleId, replaceSystemPrivilegeWithRoleRequest ) ;
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed |HinemosUnknown |InvalidRole |InvalidUserPass | InvalidSetting| UnEditableRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<ObjectPrivilegeInfoResponseP1> getRoleIdListWithReadObjectPrivilege(String objectId) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<ObjectPrivilegeInfoResponseP1>> proxy = new RestUrlSequentialExecuter<List<ObjectPrivilegeInfoResponseP1>>(this.connectUnit,this.restKind){
			@Override
			public List<ObjectPrivilegeInfoResponseP1> executeMethod( DefaultApi apiClient) throws Exception{
				List<ObjectPrivilegeInfoResponseP1> result =  apiClient.accessGetRoleIdListWithReadObjectPrivilege(objectId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed |HinemosUnknown |InvalidRole |InvalidUserPass def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public RoleInfoResponseP3 getUserIdListBelongToRoleId(String roleId) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<RoleInfoResponseP3> proxy = new RestUrlSequentialExecuter<RoleInfoResponseP3>(this.connectUnit,this.restKind){
			@Override
			public RoleInfoResponseP3 executeMethod( DefaultApi apiClient) throws Exception{
				RoleInfoResponseP3 result =  apiClient.accessGetUserIdListBelongToRoleId(roleId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed |HinemosUnknown |InvalidRole |InvalidUserPass def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<RoleInfoResponseP2> getAvailableRoles() throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<RoleInfoResponseP2>> proxy = new RestUrlSequentialExecuter<List<RoleInfoResponseP2>>(this.connectUnit,this.restKind){
			@Override
			public List<RoleInfoResponseP2> executeMethod( DefaultApi apiClient) throws Exception{
				List<RoleInfoResponseP2> result =  apiClient.accessGetAvailableRoles();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed |HinemosUnknown |InvalidRole |InvalidUserPass def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
}
