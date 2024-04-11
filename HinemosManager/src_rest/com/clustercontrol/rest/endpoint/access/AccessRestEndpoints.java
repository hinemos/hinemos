/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.access;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_404;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_409;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.glassfish.grizzly.http.server.Request;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.ObjectPrivilegeFilterInfo;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeFunction;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleTreeItem;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.RoleInfo;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.UserInfo;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.accesscontrol.util.OptionManager;
import com.clustercontrol.accesscontrol.util.PasswordHashUtil;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.RestHeaderConstant;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.PrivilegeDuplicate;
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
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.rest.annotation.RestLog;
import com.clustercontrol.rest.annotation.RestLog.LogAction;
import com.clustercontrol.rest.annotation.RestLog.LogTarget;
import com.clustercontrol.rest.annotation.RestLog.LogType;
import com.clustercontrol.rest.annotation.RestLogFunc;
import com.clustercontrol.rest.annotation.RestLogFunc.LogFuncName;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.ArrayTypeParam;
import com.clustercontrol.rest.annotation.cmdtool.IgnoreCommandline;
import com.clustercontrol.rest.annotation.cmdtool.IgnoreReference;
import com.clustercontrol.rest.endpoint.access.dto.AddRoleInfoRequest;
import com.clustercontrol.rest.endpoint.access.dto.AddUserInfoRequest;
import com.clustercontrol.rest.endpoint.access.dto.AssignUserWithRoleRequest;
import com.clustercontrol.rest.endpoint.access.dto.ChangeOwnPasswordRequest;
import com.clustercontrol.rest.endpoint.access.dto.ChangePasswordRequest;
import com.clustercontrol.rest.endpoint.access.dto.ConnectCheckResponse;
import com.clustercontrol.rest.endpoint.access.dto.HasSystemPrivilegeRequest;
import com.clustercontrol.rest.endpoint.access.dto.HasSystemPrivilegeResponse;
import com.clustercontrol.rest.endpoint.access.dto.HinemosToken;
import com.clustercontrol.rest.endpoint.access.dto.LoginRequest;
import com.clustercontrol.rest.endpoint.access.dto.LoginResponse;
import com.clustercontrol.rest.endpoint.access.dto.LogoutResponse;
import com.clustercontrol.rest.endpoint.access.dto.ManagerInfoResponse;
import com.clustercontrol.rest.endpoint.access.dto.ModifyRoleInfoRequest;
import com.clustercontrol.rest.endpoint.access.dto.ModifyUserInfoRequest;
import com.clustercontrol.rest.endpoint.access.dto.ObjectPrivilegeInfoRequestP1;
import com.clustercontrol.rest.endpoint.access.dto.ObjectPrivilegeInfoResponse;
import com.clustercontrol.rest.endpoint.access.dto.ObjectPrivilegeInfoResponseP1;
import com.clustercontrol.rest.endpoint.access.dto.ReplaceObjectPrivilegeRequest;
import com.clustercontrol.rest.endpoint.access.dto.ReplaceSystemPrivilegeWithRoleRequest;
import com.clustercontrol.rest.endpoint.access.dto.RoleInfoResponse;
import com.clustercontrol.rest.endpoint.access.dto.RoleInfoResponseP1;
import com.clustercontrol.rest.endpoint.access.dto.RoleInfoResponseP2;
import com.clustercontrol.rest.endpoint.access.dto.RoleInfoResponseP3;
import com.clustercontrol.rest.endpoint.access.dto.RoleTreeItemResponseP1;
import com.clustercontrol.rest.endpoint.access.dto.SystemPrivilegeInfoRequestP1;
import com.clustercontrol.rest.endpoint.access.dto.SystemPrivilegeInfoResponse;
import com.clustercontrol.rest.endpoint.access.dto.SystemPrivilegeInfoResponseP1;
import com.clustercontrol.rest.endpoint.access.dto.UserInfoResponse;
import com.clustercontrol.rest.endpoint.access.dto.UserInfoResponseP1;
import com.clustercontrol.rest.endpoint.access.dto.UserInfoResponseP2;
import com.clustercontrol.rest.endpoint.access.dto.UserInfoResponseP3;
import com.clustercontrol.rest.endpoint.access.dto.UserInfoResponseP4;
import com.clustercontrol.rest.endpoint.access.dto.Version;
import com.clustercontrol.rest.endpoint.access.dto.enumtype.RoleTreeDataTypeEnum;
import com.clustercontrol.rest.endpoint.access.dto.enumtype.SystemPrivilegeEditTypeEnum;
import com.clustercontrol.rest.endpoint.cloud.RestSessionScope;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonConverter;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestHttpBearerAuthenticator;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.xcloud.bean.AvailableRole;

@Path("/access")
@RestLogFunc(name = LogFuncName.Access)
public class AccessRestEndpoints {

	private static Log m_log = LogFactory.getLog(AccessRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "access";

	/**
	 * ログイン用API
	 * 
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws InvalidSetting
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	@POST
	@Path("/login")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "Login")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = LoginResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Login, target = LogTarget.Null, type = LogType.LOGIN )
	@IgnoreCommandline
	public Response login(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "loginBody", content = @Content(schema = @Schema(implementation = LoginRequest.class))) String requestBody)
			throws InvalidSetting, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call login()");

		LoginRequest loginReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, LoginRequest.class);
		RestCommonValitater.checkRequestDto(loginReq);
		loginReq.correlationCheck();

		// 認証
		String token = RestHttpBearerAuthenticator.getInstance().login(loginReq.getUserId(), loginReq.getPassword());
		String expire = RestCommonConverter
				.convertHinemosTimeToDTString(RestHttpBearerAuthenticator.getInstance().getExpireTime(token));
		Long validTerm = Long.valueOf(RestHttpBearerAuthenticator.getInstance().getLoginValidTerm()); 

		// 応答編集
		LoginResponse loginRes = new LoginResponse();
		loginRes.setToken(new HinemosToken(token, expire,validTerm));
		loginRes.setManagerInfo(new ManagerInfoResponse(HinemosTime.getTimeZoneOffset(),
				HinemosTime.getTimeOffsetMillis(), OptionManager.getOptions()));

		RestLanguageConverter.convertMessages(loginRes);

		return Response.status(Status.OK).entity(loginRes).build();
	}

	/**
	 * 再ログイン用API
	 * 
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/relogin")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "Relogin")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = LoginResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Login, target = LogTarget.Null, type = LogType.LOGIN )
	@IgnoreCommandline
	public Response relogin(@Context Request request, @Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call relogin()");

		String token = RestHttpBearerAuthenticator.getInstance().relogin(HinemosSessionContext.getAuthToken());
		String expire = null;
		try {
			expire = RestCommonConverter
					.convertHinemosTimeToDTString(RestHttpBearerAuthenticator.getInstance().getExpireTime(token));
		} catch (InvalidSetting e) {// ここには来ない想定
			throw new HinemosUnknown(e);
		}
		Long validTerm = Long.valueOf(RestHttpBearerAuthenticator.getInstance().getLoginValidTerm()); 
		LoginResponse loginRes = new LoginResponse();
		loginRes.setToken(new HinemosToken(token, expire,validTerm));
		loginRes.setManagerInfo(new ManagerInfoResponse(HinemosTime.getTimeZoneOffset(),
				HinemosTime.getTimeOffsetMillis(), OptionManager.getOptions()));

		RestLanguageConverter.convertMessages(loginRes);

		return Response.status(Status.OK).entity(loginRes).build();
	}

	/**
	 * ログアウト用API
	 * 
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/logout")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "Logout")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = LogoutResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Logout, target = LogTarget.Null, type = LogType.LOGIN )
	@IgnoreCommandline
	public Response logout(@Context Request request, @Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call logout()");

		boolean ret = RestHttpBearerAuthenticator.getInstance().logout(HinemosSessionContext.getAuthToken());

		LogoutResponse result = new LogoutResponse();
		result.setResult(ret);
		result.setMessage("logout success.");

		RestLanguageConverter.convertMessages(result);

		return Response.status(Status.OK).entity(result).build();
	}

	/**
	 * ヘルスチェック用API
	 * 
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/manager/connect")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ConnectCheck")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ConnectCheckResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Null, type = LogType.REFERENCE )
	@IgnoreCommandline
	@IgnoreReference
	public Response connectCheck(@Context Request request, @Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call connectCheck()");

		ConnectCheckResponse result = new ConnectCheckResponse();
		result.setResult(Boolean.TRUE);
		result.setMessage("connect ok.");

		RestLanguageConverter.convertMessages(result);

		return Response.status(Status.OK).entity(result).build();
	}

	/**
	 * オブジェクト権限検索条件に基づき、オブジェクト権限一覧情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 * 
	 * @param objectType 下記が設定される値一覧 
	 *         HinemosModuleConstant.PLATFORM_CALENDAR
	 *         HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN
	 *         HinemosModuleConstant.INFRA_FILE
	 *         HinemosModuleConstant.INFRA
	 *         HinemosModuleConstant.JOB_KICK
	 *         HinemosModuleConstant.JOB
	 *         HinemosModuleConstant.JOBMAP_IMAGE_FILE
	 *         HinemosModuleConstant.JOB_QUEUE
	 *         HinemosModuleConstant.HUB_LOGFORMAT
	 *         HinemosModuleConstant.HUB_TRANSFER
	 *         HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE
	 *         HinemosModuleConstant.SYSYTEM_MAINTENANCE
	 *         HinemosModuleConstant.MONITOR
	 *         HinemosModuleConstant.PLATFORM_NOTIFY
	 *         HinemosModuleConstant.REPORTING
	 *         HinemosModuleConstant.PLATFORM_REPOSITORY
	 * @param objectId
	 * @param roleId
	 * @param objectPrivilege
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/objectPrivilege")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetObjectPrivilegeInfoList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ObjectPrivilegeInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.ObjectPrivilege, type = LogType.REFERENCE )
	public Response getObjectPrivilegeInfoList(@QueryParam("objectType") String objectType,
			@QueryParam("objectId") String objectId, @QueryParam("roleId") String roleId,
			@QueryParam("objectPrivilege") String objectPrivilege, @Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getObjectPrivilegeInfoList()");

		ObjectPrivilegeFilterInfo filter = new ObjectPrivilegeFilterInfo();
		filter.setObjectType(objectType);
		filter.setObjectId(objectId);
		filter.setRoleId(roleId);
		filter.setObjectPrivilege(objectPrivilege);
		List<ObjectPrivilegeInfo> infoResList = new AccessControllerBean().getObjectPrivilegeInfoList(filter);

		List<ObjectPrivilegeInfoResponse> dtoResList = new ArrayList<>();
		for (ObjectPrivilegeInfo info : infoResList) {
			ObjectPrivilegeInfoResponse dto = new ObjectPrivilegeInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * オブジェクト種別、オブジェクトIDに紐づくオブジェクト権限情報を差し替える。<BR>
	 *
	 * AccessControlWrite権限が必要
	 * 
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws PrivilegeDuplicate
	 * @throws UsedObjectPrivilege
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws JobMasterNotFound
	 */
	@PUT
	@Path("/objectPrivilege_replace")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ReplaceObjectPrivilege")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ObjectPrivilegeInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.ObjectPrivilege, type = LogType.UPDATE )
	public Response replaceObjectPrivilege(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "replaceObjectPrivilegeBody", content = @Content(schema = @Schema(implementation = ReplaceObjectPrivilegeRequest.class))) String requestBody)
			throws PrivilegeDuplicate, UsedObjectPrivilege, HinemosUnknown, InvalidSetting, InvalidRole, InvalidUserPass, JobMasterNotFound {
		m_log.info("call replaceObjectPrivilege()");

		ReplaceObjectPrivilegeRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ReplaceObjectPrivilegeRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		String objectType = dtoReq.getObjectType();
		String objectId = dtoReq.getObjectId();
		List<ObjectPrivilegeInfo> infoReqList = new ArrayList<>();
		for (ObjectPrivilegeInfoRequestP1 dto : dtoReq.getObjectPrigilegeInfoList()) {
			ObjectPrivilegeInfo info = new ObjectPrivilegeInfo();
			info.setRoleId(dto.getRoleId());
			info.setObjectPrivilege(dto.getObjectPrivilege());
			infoReqList.add(info);
		}
		//本メソッドは送られてくるデータによって必要な権限の種類が変わるので、アノテーション指定ではなく個別にチェックを実装
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		if (HinemosModuleConstant.PLATFORM_CALENDAR.equals(objectType)
				|| HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN.equals(objectType)) {
			// カレンダ、カレンダパターン
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CALENDAR, SystemPrivilegeMode.MODIFY));
		} else if (HinemosModuleConstant.JOB.equals(objectType)
				|| HinemosModuleConstant.JOB_KICK.equals(objectType)
				|| HinemosModuleConstant.JOBMAP_IMAGE_FILE.equals(objectType)
				|| HinemosModuleConstant.JOB_QUEUE.equals(objectType)
				|| HinemosModuleConstant.JOB_LINK_SEND.equals(objectType)) {
			// ジョブ、ジョブ実行契機
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		} else if (HinemosModuleConstant.MONITOR.equals(objectType)
				|| HinemosModuleConstant.HUB_LOGFORMAT.equals(objectType)) {
			// 監視設定、ログフォーマット
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.MODIFY));
		} else if (HinemosModuleConstant.PLATFORM_NOTIFY.equals(objectType)
				|| HinemosModuleConstant.PLATFORM_COMMAND_TEMPLATE .equals(objectType)
				|| HinemosModuleConstant.PLATFORM_REST_ACCESS.equals(objectType)
				|| HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE.equals(objectType)) {
			// 通知、メールテンプレート
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.MODIFY));
		} else if (HinemosModuleConstant.PLATFORM_REPOSITORY.equals(objectType)) {
			// スコープ
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.MODIFY));
		} else if (HinemosModuleConstant.SYSYTEM_MAINTENANCE.equals(objectType)) {
			// メンテナンス
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MAINTENANCE, SystemPrivilegeMode.MODIFY));
		} else if (HinemosModuleConstant.INFRA.equals(objectType)
				|| HinemosModuleConstant.INFRA_FILE.equals(objectType)) {
			// 環境構築
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.MODIFY));
		} else if (HinemosModuleConstant.HUB_TRANSFER.equals(objectType)) {
			// 収集蓄積
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HUB, SystemPrivilegeMode.MODIFY));
		} else if (HinemosModuleConstant.SDML_CONTROL .equals(objectType)) {
			// SDML
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.SDML_SETTING , SystemPrivilegeMode.MODIFY));
		} else if (HinemosModuleConstant.RPA.equals(objectType)
				|| HinemosModuleConstant.RPA_ACCOUNT.equals(objectType)	
				|| HinemosModuleConstant.RPA_SCENARIO_TAG.equals(objectType)	
				|| HinemosModuleConstant.RPA_SCENARIO.equals(objectType)	
				|| HinemosModuleConstant.RPA_SCENARIO_CREATE.equals(objectType)	
				|| HinemosModuleConstant.RPA_SCENARIO_CORRECT.equals(objectType)	) {
			// RPA管理
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.RPA , SystemPrivilegeMode.MODIFY));
		} else if (HinemosModuleConstant.FILTER_SETTING .equals(objectType)) {
			// フィルタ管理
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.FILTER_SETTING , SystemPrivilegeMode.MODIFY));
		} else {
			m_log.info("replaceObjectPrivilegeInfo " + objectType);
		}
		String bearerAuthHeader = RestHeaderConstant.AUTH_BEARER + " "+ HinemosSessionContext.getAuthToken();
		RestHttpBearerAuthenticator.getInstance().authCheck(bearerAuthHeader, systemPrivilegeList, false);
		
		
		List<ObjectPrivilegeInfo> infoResList = new AccessControllerBean().replaceObjectPrivilegeInfo(objectType,
				objectId, infoReqList);
		List<ObjectPrivilegeInfoResponse> dtoResList = new ArrayList<>();
		for (ObjectPrivilegeInfo info : infoResList) {
			ObjectPrivilegeInfoResponse dto = new ObjectPrivilegeInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * ロールを追加する。<BR>
	 *
	 * AccessControlAdd権限が必要
	 * 
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws RoleDuplicate
	 * @throws FacilityDuplicate
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	@POST
	@Path("/role")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddRoleInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RoleInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.Role, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.ADD })
	public Response addRoleInfo(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addRoleInfoBody", content = @Content(schema = @Schema(implementation = AddRoleInfoRequest.class))) String requestBody)
			throws RoleDuplicate, FacilityDuplicate, InvalidSetting, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call addRoleInfo()");

		// JSONからDTOへ変換
		AddRoleInfoRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddRoleInfoRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		RoleInfo infoReq = new RoleInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		RoleInfo infoRes = new AccessControllerBean().addRoleInfo(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		RoleInfoResponse dtoRes = new RoleInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ロール情報を削除する。<BR>
	 *
	 * AccessControlWrite権限が必要
	 *
	 * @param roleIds
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws UsedFacility
	 * @throws RoleNotFound
	 * @throws UnEditableRole
	 * @throws UsedRole
	 * @throws UsedOwnerRole
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	@DELETE
	@Path("/role")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteRoleInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RoleInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.Role, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.MODIFY })
	public Response deleteRoleInfo(@ArrayTypeParam @QueryParam(value = "roleIds") String roleIds,
			@Context Request request, @Context UriInfo uriInfo) throws UsedFacility, RoleNotFound, UnEditableRole,
			UsedRole, UsedOwnerRole, FacilityNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call deleteRoleInfo()");

		List<String> roleIdList = new ArrayList<>();
		if(roleIds != null && !roleIds.isEmpty()) {
			roleIdList = Arrays.asList(roleIds.split(","));
		}
		
		List<RoleInfo> infoResList = new AccessControllerBean().deleteRoleInfo(roleIdList);
		List<RoleInfoResponse> dtoResList = new ArrayList<>();
		for (RoleInfo info : infoResList) {
			RoleInfoResponse dto = new RoleInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * ロール検索条件に基づき、ロール一覧情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 *
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/role")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRoleInfoList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RoleInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Role, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.READ })
	public Response getRoleInfoList(@Context Request request, @Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getRoleInfoList()");

		List<RoleInfo> infoResList = new AccessControllerBean().getRoleInfoList();
		List<RoleInfoResponse> dtoResList = new ArrayList<>();
		for (RoleInfo info : infoResList) {
			RoleInfoResponse dto = new RoleInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * ロール情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 *
	 * @param roleId
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws RoleNotFound
	 */
	@GET
	@Path("/role/{roleId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRoleInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RoleInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Role, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.READ })
	public Response getRoleInfo(@PathParam(value = "roleId") String roleId, @Context Request request,
			@Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, RoleNotFound, HinemosUnknown {
		m_log.info("call getRoleInfo()");

		RoleInfo infoRes = new AccessControllerBean().getRoleInfo(roleId);
		RoleInfoResponse dtoRes = new RoleInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ロール情報を変更する。<BR>
	 *
	 * AccessControlWrite権限が必要
	 *
	 * @param roleId
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws RoleNotFound
	 * @throws UnEditableRole
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	@PUT
	@Path("/role/{roleId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyRoleInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RoleInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Role, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.MODIFY })
	public Response modifyRoleInfo(@PathParam("roleId") String roleId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyRoleInfoBody", content = @Content(schema = @Schema(implementation = ModifyRoleInfoRequest.class))) String requestBody)
			throws InvalidSetting, InvalidUserPass, InvalidRole, RoleNotFound, UnEditableRole, FacilityNotFound, HinemosUnknown {
		m_log.info("call modifyRoleInfo()");

		ModifyRoleInfoRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyRoleInfoRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		RoleInfo infoReq = new RoleInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setRoleId(roleId);
		RoleInfo infoRes = new AccessControllerBean().modifyRoleInfo(infoReq);

		RoleInfoResponse dtoRes = new RoleInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 指定されたロールIDを条件としてシステム権限一覧情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 *
	 * @param roleId
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/systemPrivilegeRole_systemPrevilige/{roleId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetSystemPrivilegeInfoListByRoleId")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SystemPrivilegeInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.SystemPrivilegeRole, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.READ })
	public Response getSystemPrivilegeInfoListByRoleId(@PathParam(value = "roleId") String roleId,
			@Context Request request, @Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getSystemPrivilegeInfoListByRoleId()");

		List<SystemPrivilegeInfo> infoResList = new AccessControllerBean().getSystemPrivilegeInfoListByRoleId(roleId);
		List<SystemPrivilegeInfoResponse> dtoResList = new ArrayList<>();
		for (SystemPrivilegeInfo info : infoResList) {
			SystemPrivilegeInfoResponse dto = new SystemPrivilegeInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * ロールIDに紐づくシステム権限情報を差し替える。<BR>
	 *
	 * AccessControlWrite権限が必要
	 *
	 * @param roleId
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws InvalidSetting
	 * @throws UnEditableRole
	 * @throws HinemosUnknown
	 */
	@PUT
	@Path("/systemPrivilegeRole_systemPrevilige/{roleId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ReplaceSystemPrivilegeWithRole")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SystemPrivilegeInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.SystemPrivilegeRole, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.MODIFY })
	public Response replaceSystemPrivilegeWithRole(@PathParam("roleId") String roleId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "replaceSystemPrivilegeWithRoleBody", content = @Content(schema = @Schema(implementation = ReplaceSystemPrivilegeWithRoleRequest.class))) String requestBody)
			throws InvalidSetting, UnEditableRole, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call replaceSystemPrivilegeWithRole()");

		ReplaceSystemPrivilegeWithRoleRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ReplaceSystemPrivilegeWithRoleRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		List<SystemPrivilegeInfo> infoReqList = new ArrayList<>();
		for (SystemPrivilegeInfoRequestP1 dto : dtoReq.getSystemPrivilegeList()) {
			SystemPrivilegeInfo info = new SystemPrivilegeInfo();
			RestBeanUtil.convertBean(dto, info);
			infoReqList.add(info);
		}

		List<SystemPrivilegeInfo> infoResList = new AccessControllerBean().replaceSystemPrivilegeRole(roleId,
				infoReqList);

		List<SystemPrivilegeInfoResponse> dtoResList = new ArrayList<>();
		for (SystemPrivilegeInfo info : infoResList) {
			SystemPrivilegeInfoResponse dto = new SystemPrivilegeInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * ロールツリー情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 *
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws UserNotFound
	 * @throws InvalidRole
	 */
	@GET
	@Path("/role_tree")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRoleTree")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RoleTreeItemResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Role, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.READ })
	public Response getRoleTree(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, UserNotFound, InvalidUserPass, InvalidRole {
		m_log.info("call getRoleTree()");

		// ControllerBean呼び出し
		RoleTreeItem infoRes = new AccessControllerBean().getRoleTree(Locale.getDefault());

		// ControllerBeanの情報をDTOに変換
		RoleTreeItemResponseP1 dtoRes = new RoleTreeItemResponseP1();
		RoleTreeItem rootTree = infoRes.getChildren(0);
		RoleInfo root = (RoleInfo) rootTree.getData();
		dtoRes.setId(root.getRoleId());
		dtoRes.setName(root.getRoleName());
		dtoRes.setType(RoleTreeDataTypeEnum.ROLE_INFO);
		convertRoleTreeItem(rootTree, dtoRes);

		// メッセージ変換
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * システム権限一覧情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 *
	 * @param editType
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/systemPrivilege")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetSystemPrivilegeInfoList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SystemPrivilegeInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.SystemPrivilege, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.READ })
	public Response getSystemPrivilegeInfoList(@QueryParam(value = "editType") SystemPrivilegeEditTypeEnum editType,
			@Context Request request, @Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getSystemPrivilegeInfoList()");

		List<SystemPrivilegeInfo> infoResList = null;
		if (editType == null) {
			infoResList = new AccessControllerBean().getSystemPrivilegeInfoList();
		} else {
			infoResList = new AccessControllerBean().getSystemPrivilegeInfoListByEditType(editType.getCode());
		}

		List<SystemPrivilegeInfoResponse> dtoResList = new ArrayList<>();
		for (SystemPrivilegeInfo info : infoResList) {
			SystemPrivilegeInfoResponse dto = new SystemPrivilegeInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * ユーザを追加する。<BR>
	 *
	 * AccessControlAdd権限が必要
	 *
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws UserDuplicate
	 */
	@POST
	@Path("/user")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddUserInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.User, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.ADD })
	public Response addUserInfo(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addUserInfoBody", content = @Content(schema = @Schema(implementation = AddUserInfoRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting, UserDuplicate {
		m_log.info("call addUserInfo()");

		AddUserInfoRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddUserInfoRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		UserInfo infoReq = new UserInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		UserInfo infoRes = new AccessControllerBean().addUserInfo(infoReq);

		UserInfoResponse dtoRes = new UserInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ユーザ情報を削除する。<BR>
	 *
	 * AccessControlWrite権限が必要
	 *
	 * @param userIds
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws UserNotFound
	 * @throws UsedUser
	 * @throws UnEditableUser
	 */
	@DELETE
	@Path("/user")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteUserInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.User, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.MODIFY })
	public Response deleteUserInfo(@ArrayTypeParam @QueryParam(value = "userIds") String userIds,
			@Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, UserNotFound, UsedUser, UnEditableUser {
		m_log.info("call deleteUserInfo()");

		List<String> userIdList = new ArrayList<>();
		if(userIds != null && !userIds.isEmpty()) {
			userIdList = Arrays.asList(userIds.split(","));
		}
		
		List<UserInfo> infoResList = new AccessControllerBean().deleteUserInfo(userIdList);

		List<UserInfoResponse> dtoResList = new ArrayList<>();
		for (UserInfo info : infoResList) {
			UserInfoResponse dto = new UserInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * ユーザ検索条件に基づき、ユーザ一覧情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 *
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/user")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetUserInfoList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.User, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.READ })
	public Response getUserInfoList(@Context Request request, @Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getUserInfoList()");

		List<UserInfo> infoResList = new AccessControllerBean().getUserInfoList();

		List<UserInfoResponse> dtoResList = new ArrayList<>();
		for (UserInfo info : infoResList) {
			UserInfoResponse dto = new UserInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * パスワードを変更する。<BR>
	 *
	 * AccessControlWrite権限が必要
	 *
	 * @param userId
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws UserNotFound
	 */
	@PUT
	@Path("/user_password/{userId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ChangePassword")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserInfoResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.User, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.MODIFY })
	public Response changePassword(@PathParam("userId") String userId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "changePasswordBody", content = @Content(schema = @Schema(implementation = ChangePasswordRequest.class))) String requestBody)
			throws InvalidSetting, InvalidUserPass, InvalidRole, HinemosUnknown, UserNotFound {
		m_log.info("call changePassword()");

		ChangePasswordRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ChangePasswordRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		UserInfo infoReq = new UserInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		UserInfo infoRes = new AccessControllerBean().changePassword(userId, infoReq.getPassword());
		//パスワードが変更されたため、これまでのパスワードで認証されたログインは無効にする（変更者除く）
		RestHttpBearerAuthenticator.getInstance().removePasswordChangeUserToken(userId,HinemosSessionContext.getAuthToken());

		UserInfoResponseP1 dtoRes = new UserInfoResponseP1();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ユーザ情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 *
	 * @param userId
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/user/{userId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetUserInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.User, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.READ })
	public Response getUserInfo(@PathParam("userId") String userId, @Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getUserInfo()");

		UserInfo infoRes = new AccessControllerBean().getUserInfo(userId);

		UserInfoResponse dtoRes = new UserInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ユーザ情報を変更する。<BR>
	 *
	 * AccessControlWrite権限が必要
	 *
	 * @param userId
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws UserNotFound
	 * @throws UnEditableUser
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	@PUT
	@Path("/user/{userId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyUserInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.User, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.MODIFY })
	public Response modifyUserInfo(@PathParam("userId") String userId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyUserInfoBody", content = @Content(schema = @Schema(implementation = ModifyUserInfoRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, UserNotFound, UnEditableUser, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyUserInfo()");

		ModifyUserInfoRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyUserInfoRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		UserInfo infoReq = new UserInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setUserId(userId);

		UserInfo infoRes = null;
		if (infoReq.getPassword() == null || infoReq.getPassword().isEmpty()) {
			infoRes = new AccessControllerBean().modifyUserInfo(infoReq, false);
		} else {
			// パスワードのハッシュ化
			String hashedPassword =PasswordHashUtil.encodePassword(infoReq.getPassword());
			infoReq.setPassword(hashedPassword);
			infoRes = new AccessControllerBean().modifyUserInfo(infoReq, true);
		}

		UserInfoResponse dtoRes = new UserInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ロールへのユーザの割り当てを行います。<BR>
	 *
	 * roleIdで指定されるロールにリクエストボディのuserIdListで指定されるユーザ群を割り当てます。
	 *
	 * AccessControlWrite権限が必要
	 *
	 * @param roleId
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws InvalidSetting
	 * @throws UnEditableRole
	 * @throws HinemosUnknown
	 */
	@PUT
	@Path("/user/role/{roleId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AssignUserWithRole")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RoleInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Role, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.MODIFY })
	public Response assignUserWithRole(@PathParam("roleId") String roleId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "assignUserWithRoleBody", content = @Content(schema = @Schema(implementation = AssignUserWithRoleRequest.class))) String requestBody)
			throws InvalidSetting, UnEditableRole, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call assignUserWithRole()");

		AssignUserWithRoleRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AssignUserWithRoleRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		List<String> userIdList = dtoReq.getUserIdList();
		RoleInfo infoRes = new AccessControllerBean().assignUserRole(roleId,
				dtoReq.getUserIdList().toArray(new String[userIdList.size()]));

		RoleInfoResponse dtoRes = new RoleInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 自身のユーザ情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 * 
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/user_me")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetOwnUserInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.User, type = LogType.REFERENCE )
	public Response getOwnUserInfo(@Context Request request, @Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getOwnUserInfo()");

		UserInfo infoRes = new AccessControllerBean().getOwnUserInfo();

		UserInfoResponse dtoRes = new UserInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 自身の所属するロールID情報を取得する。<BR>
	 *
	 * 権限必要なし（複数機能で使用するため）
	 *
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/user_me/role_id")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetOwnerRoleIdList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserInfoResponseP3.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Role, type = LogType.REFERENCE )
	public Response getOwnerRoleIdList(@Context Request request, @Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getOwnerRoleIdList()");

		List<String> roleIdList = new AccessControllerBean().getOwnerRoleIdList();

		UserInfoResponseP3 dtoRes = new UserInfoResponseP3();
		List<RoleInfoResponseP1> roleInfoResP1List = new ArrayList<>();
		for(String roleId : roleIdList) {
			RoleInfoResponseP1 dto = new RoleInfoResponseP1();
			dto.setRoleId(roleId);
			roleInfoResP1List.add(dto);
		}
		dtoRes.setRoleList(roleInfoResP1List);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ログインしているユーザが指定したユーザ権限を持っているかどうかを確認する。<BR>
	 * 
	 * 権限必要なし
	 * 
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	@POST
	@Path("/user_me_hasSystemPrivilege")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "HasSystemPrivilege")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HasSystemPrivilegeResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.User, type = LogType.REFERENCE )
	public Response hasSystemPrivilege(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "hasSystemPrivilegeBody", content = @Content(schema = @Schema(implementation = HasSystemPrivilegeRequest.class))) String requestBody)
			throws InvalidSetting, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call hasSystemPrivilege()");

		HasSystemPrivilegeRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				HasSystemPrivilegeRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		SystemPrivilegeInfo infoReq = new SystemPrivilegeInfo();
		RestBeanUtil.convertBean(dtoReq.getSystemPrivilegeInfo(), infoReq);

		boolean isPermission = new AccessControllerBean().isPermission(infoReq);

		HasSystemPrivilegeResponse dtoRes = new HasSystemPrivilegeResponse();
		dtoRes.setResult(isPermission);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 自分自身のパスワードを変更する。<BR>
	 * 
	 * 権限必要なし
	 * 
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws UserNotFound
	 */
	@PUT
	@Path("/user_me_password")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ChangeOwnPassword")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserInfoResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.User, type = LogType.UPDATE )
	public Response changeOwnPassword(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "changeOwnPasswordBody", content = @Content(schema = @Schema(implementation = ChangeOwnPasswordRequest.class))) String requestBody)
			throws InvalidSetting, InvalidUserPass, InvalidRole, HinemosUnknown, UserNotFound {
		m_log.info("call changeOwnPassword()");

		ChangeOwnPasswordRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ChangeOwnPasswordRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		UserInfo infoRes = new AccessControllerBean().changeOwnPassword(dtoReq.getPassword());
		//パスワードが変更されたため、これまでのパスワードで認証されたログインは無効にする（変更者除く）
		RestHttpBearerAuthenticator.getInstance().removePasswordChangeUserToken(infoRes.getUserId(),HinemosSessionContext.getAuthToken());

		UserInfoResponseP1 dtoRes = new UserInfoResponseP1();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ログインユーザのユーザ名を取得する。<BR>
	 * 
	 * 権限必要なし
	 * 
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws UserNotFound
	 */
	@GET
	@Path("/user_me_name")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetUserName")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserInfoResponseP2.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.User, type = LogType.REFERENCE )
	public Response getUserName(@Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, UserNotFound {
		m_log.info("call getUserName()");

		String name = new AccessControllerBean().getUserName();

		UserInfoResponseP2 dtoRes = new UserInfoResponseP2();
		dtoRes.setUserName(name);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 指定されたユーザIDを条件としてシステム権限一覧情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 *
	 * @param userId
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/user/{userId}/systemPrivilegeRole_systemPrevilige")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetSystemPrivilegeInfoListByUserId")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SystemPrivilegeInfoResponseP1.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.SystemPrivilegeRole, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.READ })
	public Response getSystemPrivilegeInfoListByUserId(@PathParam("userId") String userId, @Context Request request,
			@Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getSystemPrivilegeInfoListByUserId()");

		List<SystemPrivilegeInfo> infoResList = new AccessControllerBean().getSystemPrivilegeInfoListByUserId(userId);
		List<SystemPrivilegeInfoResponseP1> dtoResList = new ArrayList<>();
		for (SystemPrivilegeInfo info : infoResList) {
			SystemPrivilegeInfoResponseP1 dto = new SystemPrivilegeInfoResponseP1();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * バージョン番号を取得する
	 * 
	 * 権限必要なし
	 * 
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws RoleNotFound
	 */
	@GET
	@Path("/version")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetVersion")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Version.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Version, type = LogType.REFERENCE )
	public Response getVersion(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, RoleNotFound {
		m_log.info("call getVersion()");

		String version = new AccessControllerBean().getVersion();
		Version dtoRes = new Version();
		dtoRes.setVersion(version);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 承認ジョブにおける参照のオブジェクト権限を持つロールIDのリストを取得 <BR>
	 * 
	 * JobManagementRead権限が必要
	 * 
	 * @param objectId
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/objectPrivilege_roleId_forJob")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRoleIdListWithReadObjectPrivilege")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ObjectPrivilegeInfoResponseP1.class, type=SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.ObjectPrivilege, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@IgnoreCommandline
	@IgnoreReference
	public Response getRoleIdListWithReadObjectPrivilege(@QueryParam("objectId") String objectId, 
			@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole  {
		m_log.info("call getRoleIdListWithReadObjectPrivilege()");

		List<String> infoResList = new JobControllerBean().getRoleIdListWithReadObjectPrivilege(objectId);
		List<ObjectPrivilegeInfoResponseP1> dtoResList = new ArrayList<>();
		for(String roleId : infoResList) {
			ObjectPrivilegeInfoResponseP1 dto = new ObjectPrivilegeInfoResponseP1();
			dto.setRoleId(roleId);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * 指定のロールIDに属するユーザIDのリストを取得。<BR>
	 * 
	 * JobManagementRead権限が必要
	 * 
	 * @param roleId
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/role_userId_forJob/{roleId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetUserIdListBelongToRoleId")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RoleInfoResponseP3.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Role, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@IgnoreCommandline
	@IgnoreReference
	public Response getUserIdListBelongToRoleId(@PathParam("roleId") String roleId,
			@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getUserIdListBelongToRoleId()");

		List<String> infoResList = new JobControllerBean().getUserIdListBelongToRoleId(roleId);
		
		List<UserInfoResponseP4> userDtoResP4List = new ArrayList<>();
		for(String userId : infoResList) {
			UserInfoResponseP4 userDtoResP4 = new UserInfoResponseP4();
			userDtoResP4.setUserId(userId);
			userDtoResP4List.add(userDtoResP4);
		}
		RoleInfoResponseP3 dtoRes = new RoleInfoResponseP3();
		dtoRes.setUserInfoList(userDtoResP4List);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * 利用可能なロール一覧を取得する
	 * 
	 * CloudManagementRead権限が必要
	 * 
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/role_availableRoles_forCloud")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetAvailableRoles")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RoleInfoResponseP2.class, type=SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Role, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	@IgnoreCommandline
	@IgnoreReference
	public Response getAvailableRoles(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		Response res = null;
		try(RestSessionScope sessionScope = RestSessionScope.open()){
			List<AvailableRole> roleList = new AccessControllerBean().getAvailableRoles();
			List<RoleInfoResponseP2> dtoResList = new ArrayList<>();
			for(AvailableRole role : roleList) {
				RoleInfoResponseP2 dto = new RoleInfoResponseP2();
				RestBeanUtil.convertBeanNoInvalid(role, dto);
				dtoResList.add(dto);
			}
	
			RestLanguageConverter.convertMessages(dtoResList);
	
				res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}
	
	/**
	 * MC機能のヘルスチェック用API
	 * 
	 * ログイン不要、権限必要なし
	 * 
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/healthCheck_forMC")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "HealthCheck")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Null, type = LogType.REFERENCE )
	@IgnoreCommandline
	@IgnoreReference
	public Response healthCheck(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown {
		// ヘルスチェックのたびに出力されログが増加するためコメントアウトする
		// m_log.info("call healthCheck()");

		return Response.status(Status.OK).build();
	}
	
	/**
	 * RoleTreeItem から DTO へのコンバートメソッド
	 */
	private void convertRoleTreeItem(RoleTreeItem tree, RoleTreeItemResponseP1 dto) {
		List<RoleTreeItemResponseP1> dtoChildList = dto.getChildren();
		for (RoleTreeItem item : tree.getChildren()) {
			RoleTreeItemResponseP1 dtoChild = new RoleTreeItemResponseP1();
			if (item.getData() instanceof RoleInfo) {
				RoleInfo info = (RoleInfo) item.getData();
				dtoChild.setId(info.getRoleId());
				dtoChild.setName(info.getRoleName());
				dtoChild.setType(RoleTreeDataTypeEnum.ROLE_INFO);
				dtoChildList.add(dtoChild);
				dto.setChildren(dtoChildList);
			} else {
				UserInfo info = (UserInfo) item.getData();
				dtoChild.setId(info.getUserId());
				dtoChild.setName(info.getUserName());
				dtoChild.setType(RoleTreeDataTypeEnum.USER_INFO);
				dtoChildList.add(dtoChild);
				dto.setChildren(dtoChildList);
			}
			convertRoleTreeItem(item, dtoChild);
		}
	}
}
