/* Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.cloud;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_404;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_409;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeFunction;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.bean.ActivationKeyConstant;
import com.clustercontrol.commons.bean.SettingUpdateInfo;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.rest.annotation.RestLog;
import com.clustercontrol.rest.annotation.RestLog.LogAction;
import com.clustercontrol.rest.annotation.RestLog.LogTarget;
import com.clustercontrol.rest.annotation.RestLog.LogType;
import com.clustercontrol.rest.annotation.RestLogFunc;
import com.clustercontrol.rest.annotation.RestLogFunc.LogFuncName;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.ArrayTypeParam;
import com.clustercontrol.rest.endpoint.cloud.dto.AddCloudLoginUserRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.AddCloudScopeRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.AttachStorageRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.AutoAssignNodePatternEntryInfoRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.AutoAssignNodePatternEntryInfoResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.BillingResultResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.CloneBackupedInstanceRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.CloneBackupedStorageRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.CloudLoginUserInfoResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.CloudLoginUserInfoResponseP1;
import com.clustercontrol.rest.endpoint.cloud.dto.CloudPlatformInfoResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.CloudScopeInfoResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.CloudScopeInfoResponseP1;
import com.clustercontrol.rest.endpoint.cloud.dto.CloudSpecResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.CreateStorageSnapshotRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.CredentialResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.DeleteStorageSnapshotRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.DetachStorageRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.GetCloudScopesResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.GetInstanceWithStorageResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.GetPlatformServiceForLoginUserResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.GetPlatformServicesOnlyServiceNameResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.GetPlatformServicesResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.HFacilityResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.HRepositoryResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.InstanceBackupEntryResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.InstanceBackupResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.InstanceInfoResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.ModifyBillingSettingRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.ModifyCloudLoginUserPriorityRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.ModifyCloudLoginUserRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.ModifyCloudLoginUserRoleRelationRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.ModifyCloudScopeRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.ModifyInstanceRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.ModifyPlatformServiceConditionRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.NetworkInfoResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.OptionRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.PlatformServiceConditionResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.PlatformServicesResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.PowerOffInstances;
import com.clustercontrol.rest.endpoint.cloud.dto.PowerOffInstancesRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.PowerOnInstances;
import com.clustercontrol.rest.endpoint.cloud.dto.PowerOnInstancesRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.RebootInstances;
import com.clustercontrol.rest.endpoint.cloud.dto.RebootInstancesRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.RefreshBillingDetailsRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.RegistAutoAssigneNodePatternRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.RoleRelationRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.SnapshotInstanceRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.StorageBackupEntryResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.StorageBackupInfoResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.StorageInfoResponse;
import com.clustercontrol.rest.endpoint.cloud.dto.SuspendInstances;
import com.clustercontrol.rest.endpoint.cloud.dto.SuspendInstancesRequest;
import com.clustercontrol.rest.endpoint.cloud.util.AuthorizingValidator;
import com.clustercontrol.rest.endpoint.cloud.util.RestValidationUtil;
import com.clustercontrol.rest.endpoint.jobmap.dto.CheckPublishResponse;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonConverter;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.rest.util.RestTempFileType;
import com.clustercontrol.rest.util.RestTempFileUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.KeyCheck;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.bean.AccessKeyCredential;
import com.clustercontrol.xcloud.bean.AddCloudScopeRequest.ITransformer;
import com.clustercontrol.xcloud.bean.AddPrivateCloudScopeRequest;
import com.clustercontrol.xcloud.bean.AddPublicCloudScopeRequest;
import com.clustercontrol.xcloud.bean.AutoAssignNodePatternEntry;
import com.clustercontrol.xcloud.bean.BillingResult;
import com.clustercontrol.xcloud.bean.CloudLoginUser;
import com.clustercontrol.xcloud.bean.CloudPlatform;
import com.clustercontrol.xcloud.bean.CloudScope;
import com.clustercontrol.xcloud.bean.Credential;
import com.clustercontrol.xcloud.bean.GenericCredential;
import com.clustercontrol.xcloud.bean.HRepository;
import com.clustercontrol.xcloud.bean.Instance;
import com.clustercontrol.xcloud.bean.InstanceBackup;
import com.clustercontrol.xcloud.bean.InstanceBackupEntry;
import com.clustercontrol.xcloud.bean.ModifyPrivateCloudScopeRequest;
import com.clustercontrol.xcloud.bean.ModifyPublicCloudScopeRequest;
import com.clustercontrol.xcloud.bean.Network;
import com.clustercontrol.xcloud.bean.Option;
import com.clustercontrol.xcloud.bean.PlatformServiceCondition;
import com.clustercontrol.xcloud.bean.PlatformUser;
import com.clustercontrol.xcloud.bean.PrivateCloudScope;
import com.clustercontrol.xcloud.bean.PrivateLocation;
import com.clustercontrol.xcloud.bean.PublicCloudScope;
import com.clustercontrol.xcloud.bean.RoleRelation;
import com.clustercontrol.xcloud.bean.Storage;
import com.clustercontrol.xcloud.bean.StorageBackup;
import com.clustercontrol.xcloud.bean.StorageBackupEntry;
import com.clustercontrol.xcloud.bean.UserCredential;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.factory.ActionMode;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.factory.IBillings;
import com.clustercontrol.xcloud.factory.ICloudOption;
import com.clustercontrol.xcloud.factory.ICloudScopes;
import com.clustercontrol.xcloud.factory.IInstances;
import com.clustercontrol.xcloud.factory.IPrivateCloudOption;
import com.clustercontrol.xcloud.factory.IPublicCloudOption;
import com.clustercontrol.xcloud.factory.IUserManagement;
import com.clustercontrol.xcloud.factory.monitors.CloudLogManagerUtil;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudPlatformEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.InstanceBackupEntity;
import com.clustercontrol.xcloud.model.InstanceBackupEntryEntity;
import com.clustercontrol.xcloud.model.InstanceEntity;
import com.clustercontrol.xcloud.model.PrivateCloudScopeEntity;
import com.clustercontrol.xcloud.model.PublicCloudScopeEntity;
import com.clustercontrol.xcloud.model.StorageBackupEntity;
import com.clustercontrol.xcloud.model.StorageBackupEntryEntity;
import com.clustercontrol.xcloud.model.StorageEntity;
import com.clustercontrol.xcloud.util.AuthorizingUtil;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.util.CollectionComparator;
import com.clustercontrol.xcloud.util.FacilityIdUtil;
import com.clustercontrol.xcloud.util.RepositoryControllerBeanWrapper;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

@Path("/xcloud")
@RestLogFunc(name = LogFuncName.xCloud)
public class CloudRestEndpoints {
	private static Log m_log = LogFactory.getLog(CloudRestEndpoints.class);
	private static final String ENDPOINT_OPERATION_ID_PREFIX = "xcloud";

	private static final String XCLOUD_CORE_ROLE_ID = "XCLOUD_CORE_ROLE_ID";
	private static final String XCLOUD_CORE_FACILITY_ID = "XCLOUD_CORE_FACILITY_ID";
	private static final String XCLOUD_CORE_YEAR = "XCLOUD_CORE_YEAR";
	private static final String XCLOUD_CORE_MONTH = "XCLOUD_CORE_MONTH";

	/**
	 * スコープ割当ルールを設定するAPI
	 */
	@PUT
	@Path("/scopeAssignRule/cloudScope/{cloudScopeId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "RegistAutoAssigneNodePattern")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = AutoAssignNodePatternEntryInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.ScopeAssignRule, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.MODIFY,
			SystemPrivilegeMode.READ })
	public Response registAutoAssigneNodePattern(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "RegistAutoAssigneNodePatternRequestBody", content = @Content(schema = @Schema(implementation = RegistAutoAssigneNodePatternRequest.class))) String requestBody)
			throws CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			AuthorizingValidator.validateScopeAdmin(cloudScopeId);

			RegistAutoAssigneNodePatternRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					RegistAutoAssigneNodePatternRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();

			List<AutoAssignNodePatternEntry> infoReqList = new ArrayList<>();
			for (AutoAssignNodePatternEntryInfoRequest dto : dtoReq.getPatterns()) {
				AutoAssignNodePatternEntry infoReq = new AutoAssignNodePatternEntry();
				RestBeanUtil.convertBean(dto, infoReq);
				infoReqList.add(infoReq);
			}

			ICloudScopes scopes = CloudManager.singleton().getCloudScopes();
			List<AutoAssignNodePatternEntry> infoResList = AutoAssignNodePatternEntry
					.convertWebEntities(scopes.registAutoAssigneNodePattern(cloudScopeId, infoReqList));

			List<AutoAssignNodePatternEntryInfoResponse> dtoResList = new ArrayList<>();
			for (AutoAssignNodePatternEntry infoRes : infoResList) {
				AutoAssignNodePatternEntryInfoResponse dtoRes = new AutoAssignNodePatternEntryInfoResponse();
				RestBeanUtil.convertBean(infoRes, dtoRes);
				dtoResList.add(dtoRes);
			}
			RestLanguageConverter.convertMessages(dtoResList);

			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * スコープ割当ルールを取得するAPI
	 */
	@GET
	@Path("/scopeAssignRule/cloudScope/{cloudScopeId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetAutoAssigneNodePatterns")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = AutoAssignNodePatternEntryInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.ScopeAssignRule, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getAutoAssigneNodePatterns(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			RestValidationUtil.identityValidate(MessageConstant.XCLOUD_CORE_CLOUDSCOPE_ID.name(), cloudScopeId);
			AuthorizingValidator.validateScope(cloudScopeId);
			ICloudScopes scopes = CloudManager.singleton().getCloudScopes();
			List<AutoAssignNodePatternEntry> infoResList = AutoAssignNodePatternEntry
					.convertWebEntities(scopes.getAutoAssigneNodePatterns(cloudScopeId));

			List<AutoAssignNodePatternEntryInfoResponse> dtoResList = new ArrayList<>();
			for (AutoAssignNodePatternEntry infoRes : infoResList) {
				AutoAssignNodePatternEntryInfoResponse dtoRes = new AutoAssignNodePatternEntryInfoResponse();
				RestBeanUtil.convertBean(infoRes, dtoRes);
				dtoResList.add(dtoRes);
			}

			RestLanguageConverter.convertMessages(dtoResList);
			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * スコープ割当ルールを削除するAPI
	 */
	@DELETE
	@Path("/scopeAssignRule/cloudScope/{cloudScopeId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ClearAutoAssigneNodePattern")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = AutoAssignNodePatternEntryInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.ScopeAssignRule, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.MODIFY,
			SystemPrivilegeMode.READ })
	public Response clearAutoAssigneNodePattern(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			AuthorizingValidator.validateScope(cloudScopeId);
			ICloudScopes scopes = CloudManager.singleton().getCloudScopes();
			List<AutoAssignNodePatternEntry> infoResList = AutoAssignNodePatternEntry
					.convertWebEntities(scopes.clearAutoAssigneNodePattern(cloudScopeId));

			List<AutoAssignNodePatternEntryInfoResponse> dtoResList = new ArrayList<>();
			for (AutoAssignNodePatternEntry infoRes : infoResList) {
				AutoAssignNodePatternEntryInfoResponse dtoRes = new AutoAssignNodePatternEntryInfoResponse();
				RestBeanUtil.convertBean(infoRes, dtoRes);
				dtoResList.add(dtoRes);
			}

			RestLanguageConverter.convertMessages(dtoResList);
			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * プラットフォームを取得するAPI
	 */
	@GET
	@Path("/cloudPlatform/{cloudPlatformId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCloudPlatform")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudPlatformInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.CloudPlatform, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getCloudPlatform(@PathParam(value = "cloudPlatformId") String cloudPlatformId,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			CloudPlatform infoRes = new CloudPlatform(
					CloudManager.singleton().getPlatforms().getCloudPlatform(cloudPlatformId));

			CloudPlatformInfoResponse dtoRes = new CloudPlatformInfoResponse();
			RestBeanUtil.convertBean(infoRes, dtoRes);
			// cloudSpec変換
			convertCloudSpec(infoRes, dtoRes);
			RestLanguageConverter.convertMessages(dtoRes);
			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * 全プラットフォームを取得するAPI
	 */
	@GET
	@Path("/cloudPlatform")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetAllCloudPlatforms")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudPlatformInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.CloudPlatform, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getAllCloudPlatforms(@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			List<CloudPlatformEntity> entities = CloudManager.singleton().getPlatforms().getAllCloudPlatforms();
			List<CloudPlatform> infoResList = CloudPlatform.convertWebEntities(entities);
			List<CloudPlatformInfoResponse> dtoResList = new ArrayList<>();
			for (CloudPlatform infoRes : infoResList) {
				CloudPlatformInfoResponse dtoRes = new CloudPlatformInfoResponse();
				RestBeanUtil.convertBean(infoRes, dtoRes);
				// cloudSpec変換
				convertCloudSpec(infoRes, dtoRes);

				dtoResList.add(dtoRes);
			}
			RestLanguageConverter.convertMessages(dtoResList);
			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * クラウドスコープを作成するAPI
	 */
	@POST
	@Path("/cloudScope")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCloudScope")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudScopeInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.CloudScope, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.ADD,
			SystemPrivilegeMode.READ })
	public Response addCloudScope(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "AddCloudScopeRequestBody", content = @Content(schema = @Schema(implementation = AddCloudScopeRequest.class))) String requestBody)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			AuthorizingValidator.validateAdmin();
			AddCloudScopeRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					AddCloudScopeRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);

			CloudPlatformEntity platform = Session.current().getEntityManager().find(CloudPlatformEntity.class,
					dtoReq.getPlatformId(), ObjectPrivilegeMode.READ);
			if (platform == null) {
				throw new InvalidSetting(ErrorCode.CLOUD_PLATFORM_NOT_FOUND.cloudManagerFault(dtoReq.getPlatformId()));
			}
			Boolean isPublic = isPublic(dtoReq.getPlatformId());
			dtoReq.getAccount().setPublic(isPublic);

			CloudManager.singleton().optionExecute(dtoReq.getPlatformId(), new CloudManager.OptionExecutor() {
				@Override
				public void execute(ICloudOption option) throws CloudManagerException {
					if (isPublic) {
						option.visit(new ICloudOption.IVisitor() {
							@Override
							public void visit(IPrivateCloudOption cloudOption) throws CloudManagerException {
								throw new CloudManagerException();
							}

							@Override
							public void visit(IPublicCloudOption cloudOption) throws CloudManagerException {
								cloudOption.validCredentialAsAccount(dtoReq.getAccount().getCredential());
							}
						});

					} else {
						option.visit(new ICloudOption.IVisitor() {
							@Override
							public void visit(IPrivateCloudOption cloudOption) throws CloudManagerException {
								List<PrivateLocation> location = new ArrayList<>();
								try {
									RestBeanUtil.convertBean(dtoReq.getPrivateLocations(), location);
									cloudOption.validCredentialAsAccount(dtoReq.getAccount().getCredential(), location);
								} catch (Exception e) {
									throw new CloudManagerException(e.getMessage());
								}
							}

							@Override
							public void visit(IPublicCloudOption cloudOption) throws CloudManagerException {
								throw new CloudManagerException();
							}
						});
					}
				}
			});

			com.clustercontrol.xcloud.bean.AddCloudScopeRequest infoReq;
			if (isPublic) {
				infoReq = new AddPublicCloudScopeRequest();
			} else {
				infoReq = new AddPrivateCloudScopeRequest();
			}
			RestBeanUtil.convertBean(dtoReq, infoReq);

			CloudScope infoRes = infoReq.transform(new ITransformer<CloudScope>() {
				@Override
				public CloudScope transform(AddPublicCloudScopeRequest request) throws CloudManagerException {
					try {
						return new PublicCloudScope(
								CloudManager.singleton().getCloudScopes().addPublicCloudScope(request));
					} catch (Exception e) {
						throw new CloudManagerException(e);
					}
				}

				@Override
				public CloudScope transform(AddPrivateCloudScopeRequest request) throws CloudManagerException {
					try {
						return new PrivateCloudScope(
								CloudManager.singleton().getCloudScopes().addPrivateCloudScope(request));
					} catch (Exception e) {
						throw new CloudManagerException(e);
					}
				}
			});
			CloudScopeInfoResponse dtoRes = new CloudScopeInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoRes.getEntity().setPublic(isPublic);

			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * クラウドスコープを削除するAPI
	 */
	@DELETE
	@Path("/cloudScope")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "RemoveCloudScope")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudScopeInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.CloudScope, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.MODIFY,
			SystemPrivilegeMode.READ })
	public Response removeCloudScope(@QueryParam(value = "cloudScopeIds") @ArrayTypeParam String cloudScopeIds,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			RestValidationUtil.notNullValidate(MessageConstant.XCLOUD_CORE_CLOUDSCOPE_ID.name(), cloudScopeIds);
			AuthorizingValidator.validateAdmin();
			List<CloudScope> infoResList = new ArrayList<>();
			for (String cloudScopeId : cloudScopeIds.split(",")) {
				RestValidationUtil.modifiableCloudScopeValidate(cloudScopeId);
				infoResList.add(CloudManager.singleton().getCloudScopes().removeCloudScope(cloudScopeId)
						.transform(new CloudScopeEntity.ITransformer<CloudScope>() {
							@Override
							public CloudScope transform(PublicCloudScopeEntity scope) throws CloudManagerException {
								return new PublicCloudScope(scope);
							}

							@Override
							public CloudScope transform(PrivateCloudScopeEntity scope) throws CloudManagerException {
								return new PrivateCloudScope(scope);
							}
						}));
			}

			List<CloudScopeInfoResponse> dtoResList = new ArrayList<>();
			for (CloudScope infoRes : infoResList) {
				CloudScopeInfoResponse dtoRes = new CloudScopeInfoResponse();
				RestBeanUtil.convertBean(infoRes, dtoRes);
				dtoRes.getEntity().setPublic(isPublic(infoRes.getPlatformId()));
				dtoResList.add(dtoRes);
			}
			RestLanguageConverter.convertMessages(dtoResList);

			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * クラウドスコープを更新するAPI
	 */
	@PUT
	@Path("/cloudScope/{cloudScopeId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCloudScope")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudScopeInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.CloudScope, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.MODIFY,
			SystemPrivilegeMode.READ })
	public Response modifyCloudScope(@Context Request request, @Context UriInfo uriInfo,
			@PathParam(value = "cloudScopeId") String cloudScopeId,
			@RequestBody(description = "ModifyCloudScopeRequestBody", content = @Content(schema = @Schema(implementation = ModifyCloudScopeRequest.class))) String requestBody)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			ModifyCloudScopeRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					ModifyCloudScopeRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();
			CloudScopeEntity scope;
			HinemosEntityManager em = Session.current().getEntityManager();
			TypedQuery<CloudScopeEntity> query = em.createNamedQuery("findCloudScopeByHinemosUserAsAdmin",
					CloudScopeEntity.class);
			query.setParameter("userId", Session.current().getHinemosCredential().getUserId());
			query.setParameter("cloudScopeId", cloudScopeId);
			query.setParameter("ADMINISTRATORS", RoleIdConstant.ADMINISTRATORS);
			query.setParameter("accountType", CloudLoginUserEntity.CloudUserType.account);

			try {
				scope = query.getSingleResult();
			} catch (NoResultException e) {
				throw ErrorCode.NEED_ADMINISTRATORS_ROLE_OR_ACCOUNT_USER
						.cloudManagerFault(Session.current().getHinemosCredential().getUserId(), cloudScopeId);
			}

			Boolean isPublic = isPublic(scope.getPlatformId());
			com.clustercontrol.xcloud.bean.ModifyCloudScopeRequest infoReq;
			if (isPublic) {
				infoReq = new ModifyPublicCloudScopeRequest();
			} else {
				infoReq = new ModifyPrivateCloudScopeRequest();
			}
			RestBeanUtil.convertBean(dtoReq, infoReq);
			infoReq.setCloudScopeId(cloudScopeId);

			CloudScope infoRes = CloudManager.singleton().getCloudScopes().modifyCloudScope(infoReq)
					.transform(new CloudScopeEntity.ITransformer<CloudScope>() {
						@Override
						public CloudScope transform(PublicCloudScopeEntity scope) throws CloudManagerException {
							return new PublicCloudScope(scope);
						}

						@Override
						public CloudScope transform(PrivateCloudScopeEntity scope) throws CloudManagerException {
							return new PrivateCloudScope(scope);
						}
					});
			CloudScopeInfoResponse dtoRes = new CloudScopeInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoRes.getEntity().setPublic(isPublic);
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * クラウドスコープを取得するAPI
	 */
	@GET
	@Path("/cloudScope/{cloudScopeId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCloudScope")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudScopeInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.CloudScope, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getCloudScope(@PathParam(value = "cloudScopeId") String cloudScopeId, @Context Request request,
			@Context UriInfo uriInfo) throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			RestValidationUtil.identityValidate(MessageConstant.XCLOUD_CORE_CLOUDSCOPE_ID.name(), cloudScopeId);
			AuthorizingValidator.validateScope(cloudScopeId);
			CloudScope infoRes = CloudManager.singleton().getCloudScopes()
					.getCloudScopeByCurrentHinemosUser(cloudScopeId)
					.transform(new CloudScopeEntity.ITransformer<CloudScope>() {
						@Override
						public CloudScope transform(PublicCloudScopeEntity scope) throws CloudManagerException {
							return new PublicCloudScope(scope);
						}

						@Override
						public CloudScope transform(PrivateCloudScopeEntity scope) throws CloudManagerException {
							return new PrivateCloudScope(scope);
						}
					});
			CloudScopeInfoResponse dtoRes = new CloudScopeInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoRes.getEntity().setPublic(isPublic(infoRes.getPlatformId()));
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * オーナーロールIDを指定してクラウドスコープを取得するAPI
	 * 
	 * @throws InvalidSetting
	 */
	@GET
	@Path("/cloudScope")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCloudScopes")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetCloudScopesResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.CloudScope, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getCloudScopes(@QueryParam(value = "ownerRoleId") String ownerRoleId, @Context Request request,
			@QueryParam(value = "size") String sizeStr, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			Integer size = RestCommonConverter.convertInteger(MessageConstant.SIZE.getMessage(), sizeStr, false, 1,
					null);
			List<CloudScopeInfoResponse> dtoResList = new ArrayList<>();
			if (ownerRoleId != null) {
				RestValidationUtil.identityValidate(XCLOUD_CORE_ROLE_ID, ownerRoleId);
				List<CloudLoginUserEntity> users = CloudManager.singleton().getLoginUsers()
						.getCloudLoginUserByRole(ownerRoleId);
				Map<String, CloudScope> cloudScopes = new HashMap<>();
				for (CloudLoginUserEntity user : users) {
					if (!cloudScopes.containsKey(user.getCloudScopeId())) {
						cloudScopes.put(user.getCloudScopeId(),
								user.getCloudScope().transform(new CloudScopeEntity.ITransformer<CloudScope>() {
									@Override
									public CloudScope transform(PublicCloudScopeEntity scope)
											throws CloudManagerException {
										return new PublicCloudScope(scope);
									}

									@Override
									public CloudScope transform(PrivateCloudScopeEntity scope)
											throws CloudManagerException {
										return new PrivateCloudScope(scope);
									}
								}));
						CloudScopeInfoResponse dtoRes = new CloudScopeInfoResponse();
						RestBeanUtil.convertBeanNoInvalid(cloudScopes.get(user.getCloudScopeId()), dtoRes);
						dtoRes.getEntity().setPublic(isPublic(cloudScopes.get(user.getCloudScopeId()).getPlatformId()));
						dtoResList.add(dtoRes);
					}
				}
			} else {
				AuthorizingValidator.validateAdmin();
				List<CloudScopeEntity> entities = CloudManager.singleton().getCloudScopes().getAllCloudScopes();

				for (CloudScopeEntity entity : entities) {
					CloudScopeInfoResponse dtoRes = new CloudScopeInfoResponse();
					CloudScope infoRes = entity.transform(new CloudScopeEntity.ITransformer<CloudScope>() {
						@Override
						public CloudScope transform(PublicCloudScopeEntity scope) throws CloudManagerException {
							return new PublicCloudScope(scope);
						}

						@Override
						public CloudScope transform(PrivateCloudScopeEntity scope) throws CloudManagerException {
							return new PrivateCloudScope(scope);
						}
					});
					RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
					dtoRes.getEntity().setPublic(isPublic(infoRes.getPlatformId()));
					dtoResList.add(dtoRes);
				}
			}
			List<CloudScopeInfoResponse> dtoResListZap = new ArrayList<>();
			if (sizeStr != null) {
				int recCount = 0;
				for (CloudScopeInfoResponse rec : dtoResList) {
					dtoResListZap.add(rec);
					recCount++;
					if (recCount >= size) {
						break;
					}
				}
			} else {
				dtoResListZap = dtoResList;
			}

			RestLanguageConverter.convertMessages(dtoResListZap);
			GetCloudScopesResponse dtoRes = new GetCloudScopesResponse();
			dtoRes.setCloudScopeInfoList(dtoResListZap);
			dtoRes.setTotal(dtoResList.size());
			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * クラウドスコープで使用可能なアカウントを取得するAPI
	 */
	@GET
	@Path("/xcloud_cloudScope_platformUser/{cloudScopeId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetAvailablePlatformUsers")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudScopeInfoResponseP1.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.CloudScope, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getAvailablePlatformUsers(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			AuthorizingValidator.validateScopeAdmin(cloudScopeId);
			CloudScopeEntity scope = CloudManager.singleton().getCloudScopes().getCloudScope(cloudScopeId);
			List<PlatformUser> infoResList = scope
					.optionCall(new CloudScopeEntity.OptionCallable<List<PlatformUser>>() {
						@Override
						public List<PlatformUser> call(CloudScopeEntity scope, ICloudOption option)
								throws CloudManagerException {
							IUserManagement um = option.getUserManagement(scope);
							final List<PlatformUser> users = new ArrayList<>();
							CollectionComparator.compare(um.getAvailableUsers(),
									CloudManager.singleton().getLoginUsers()
											.getCloudLoginUserByCloudScope(scope.getId()),
									new CollectionComparator.Comparator<PlatformUser, CloudLoginUserEntity>() {
										@Override
										public boolean match(PlatformUser o1, CloudLoginUserEntity o2)
												throws CloudManagerException {
											return o1.getCredential().match(o2.getCredential().convertWebElement());
										}

										@Override
										public void afterO1(PlatformUser o1) throws CloudManagerException {
											users.add(o1);
										}
									});
							return users;
						}
					});

			List<CloudScopeInfoResponseP1> dtoResList = new ArrayList<>();
			for (PlatformUser infoRes : infoResList) {
				CloudScopeInfoResponseP1 dtoRes = new CloudScopeInfoResponseP1();
				RestBeanUtil.convertBean(infoRes, dtoRes);
				CredentialResponse credRes = new CredentialResponse();
				infoRes.getCredential().visit(new Credential.IVisitor() {
					@Override
					public void visit(AccessKeyCredential credential) throws CloudManagerException {
						credRes.setAccessKey(credential.getAccessKey());
						credRes.setSecretKey(credential.getSecretKey());
					}

					@Override
					public void visit(UserCredential credential) throws CloudManagerException {
						credRes.setUser(credential.getUser());
						credRes.setPassword(credential.getPassword());
					}

					@Override
					public void visit(GenericCredential credential) throws CloudManagerException {
						credRes.setPlatform(credential.getPlatform());
						credRes.setJsonCredentialInfo(credential.getJsonCredentialInfo());
						
					}
				});
				dtoRes.setCredential(credRes);
				dtoResList.add(dtoRes);
			}
			RestLanguageConverter.convertMessages(dtoResList);
			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * クラウドスコープのサブアカウントを作成するAPI
	 */
	@POST
	@Path("/cloudLoginUser/cloudScope/{cloudScopeId}/cloudLoginUser")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCloudLoginUser")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudLoginUserInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.CloudLoginUser, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.ADD,
			SystemPrivilegeMode.READ })
	public Response addCloudLoginUser(@PathParam(value = "cloudScopeId") String cloudScopeId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "AddCloudLoginUserRequestBody", content = @Content(schema = @Schema(implementation = AddCloudLoginUserRequest.class))) String requestBody)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			AddCloudLoginUserRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					AddCloudLoginUserRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			if (!AuthorizingUtil.checkHinemousUser_administrators_account(
					Session.current().getHinemosCredential().getUserId(), cloudScopeId)) {
				throw ErrorCode.NEED_ADMINISTRATORS_ROLE_OR_ACCOUNT_USER
						.cloudManagerFault(Session.current().getHinemosCredential().getUserId(), cloudScopeId);
			}
			CloudScopeEntity scope = CloudManager.singleton().getCloudScopes().getCloudScope(cloudScopeId);
			Boolean isPublic = isPublic(scope.getPlatformId());
			dtoReq.setPublic(isPublic);
			dtoReq.correlationCheck();

			// クラウド側にユーザー情報が存在するか確認。
			scope.optionExecute(new CloudScopeEntity.OptionExecutor() {
				@Override
				public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
					option.getUserManagement(scope).validCredentialAsUser(dtoReq.getCredential());
				}
			});

			com.clustercontrol.xcloud.bean.AddCloudLoginUserRequest infoReq = new com.clustercontrol.xcloud.bean.AddCloudLoginUserRequest();
			RestBeanUtil.convertBean(dtoReq, infoReq);
			infoReq.setCloudScopeId(cloudScopeId);
			CloudLoginUser infoRes = new CloudLoginUser(CloudManager.singleton().getLoginUsers().addUser(infoReq));

			CloudLoginUserInfoResponse dtoRes = new CloudLoginUserInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * クラウドスコープのサブアカウントを取得するAPI
	 */
	@GET
	@Path("/cloudLoginUser/cloudScope/{cloudScopeId}/cloudLoginUser/{cloudLoginUserId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCloudLoginUser")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudLoginUserInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.CloudLoginUser, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getCloudLoginUser(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "cloudLoginUserId") String cloudLoginUserId, @Context Request request,
			@Context UriInfo uriInfo) throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			RestValidationUtil.identityValidate(MessageConstant.XCLOUD_CORE_CLOUDSCOPE_ID.name(), cloudScopeId);
			RestValidationUtil.identityValidate(MessageConstant.XCLOUD_CORE_CLOUDLOGINUSER_ID.name(), cloudLoginUserId);
			AuthorizingValidator.validateLoginUser(cloudScopeId, cloudLoginUserId);
			CloudLoginUser infoRes = new CloudLoginUser(
					CloudManager.singleton().getLoginUsers().getCloudLoginUser(cloudScopeId, cloudLoginUserId));

			CloudLoginUserInfoResponse dtoRes = new CloudLoginUserInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * クラウドスコープのサブアカウントを削除するAPI
	 */
	@DELETE
	@Path("/cloudLoginUser/cloudScope/{cloudScopeId}/cloudLoginUser/{cloudLoginUserId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "RemoveCloudLoginUser")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudLoginUserInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.CloudLoginUser, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	public Response removeCloudLoginUser(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "cloudLoginUserId") String cloudLoginUserId, @Context Request request,
			@Context UriInfo uriInfo) throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			RestValidationUtil.identityValidate(MessageConstant.XCLOUD_CORE_CLOUDSCOPE_ID.name(), cloudScopeId);
			RestValidationUtil.identityValidate(MessageConstant.XCLOUD_CORE_CLOUDLOGINUSER_ID.name(), cloudLoginUserId);
			AuthorizingValidator.validateLoginUser(cloudScopeId, cloudLoginUserId);
			CloudLoginUser infoRes = new CloudLoginUser(
					CloudManager.singleton().getLoginUsers().removeCloudLoginUser(cloudScopeId, cloudLoginUserId));

			CloudLoginUserInfoResponse dtoRes = new CloudLoginUserInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * クラウドスコープの全サブアカウントを取得するAPI
	 */
	@GET
	@Path("/cloudLoginUser/cloudScope/{cloudScopeId}/cloudLoginUser")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetAllCloudLoginUsers")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudLoginUserInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.CloudLoginUser, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getAllCloudLoginUsers(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			AuthorizingValidator.validateScope(cloudScopeId);
			List<CloudLoginUserEntity> infoResList = CloudManager.singleton().getLoginUsers()
					.getCloudLoginUserByCloudScopeAndHinemosUser(cloudScopeId,
							Session.current().getHinemosCredential().getUserId());

			List<CloudLoginUserInfoResponse> dtoResList = new ArrayList<>();
			for (CloudLoginUserEntity entity : infoResList) {
				CloudLoginUserInfoResponse dtoRes = new CloudLoginUserInfoResponse();
				RestBeanUtil.convertBeanNoInvalid(new CloudLoginUser(entity), dtoRes);
				dtoResList.add(dtoRes);
			}
			RestLanguageConverter.convertMessages(dtoResList);

			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * クラウドスコープのサブアカウントを変更するAPI
	 */
	@PUT
	@Path("/cloudLoginUser/cloudScope/{cloudScopeId}/cloudLoginUser/{cloudLoginUserId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCloudLoginUser")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudLoginUserInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.CloudLoginUser, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.MODIFY,
			SystemPrivilegeMode.READ })
	public Response modifyCloudLoginUser(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "cloudLoginUserId") String cloudLoginUserId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "ModifyCloudLoginUserRequestBody", content = @Content(schema = @Schema(implementation = ModifyCloudLoginUserRequest.class))) String requestBody)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		Response res = null;
		Boolean isPublic = false;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			ModifyCloudLoginUserRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					ModifyCloudLoginUserRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);

			if (!AuthorizingUtil.checkHinemousUser_administrators_account_self(
					Session.current().getHinemosCredential().getUserId(), cloudScopeId, cloudLoginUserId)) {
				throw ErrorCode.NEED_ADMINISTRATORS_ROLE_OR_ACCOUT_USER_OR_SELF.cloudManagerFault(
						Session.current().getHinemosCredential().getUserId(), cloudScopeId, cloudLoginUserId);
			}

			CloudScopeEntity scope = CloudManager.singleton().getCloudScopes().getCloudScope(cloudScopeId);
			isPublic = isPublic(scope.getPlatformId());
			dtoReq.setPublic(isPublic);
			dtoReq.correlationCheck();
			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers().getCloudLoginUser(cloudScopeId,
					cloudLoginUserId);
			switch (user.getCloudUserType()) {
			case account:
				user.getCloudScope().optionExecuteEx(new CloudScopeEntity.OptionExecutorEx() {
					@Override
					public void execute(PublicCloudScopeEntity scope, IPublicCloudOption option)
							throws CloudManagerException {
						option.validCredentialAsAccount(dtoReq.getCredential());
					}

					@Override
					public void execute(PrivateCloudScopeEntity scope, IPrivateCloudOption option)
							throws CloudManagerException {
						option.validCredentialEntityAsAccount(dtoReq.getCredential(),
								new ArrayList<>(scope.getPrivateLocations().values()));
					}
				});
				break;
			case user:
				user.getCloudScope().optionExecute(new CloudScopeEntity.OptionExecutor() {
					@Override
					public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
						IUserManagement userManager = option.getUserManagement(scope);
						userManager.validCredentialAsUser(dtoReq.getCredential());
					}
				});
				break;
			}

			com.clustercontrol.xcloud.bean.ModifyCloudLoginUserRequest infoReq = new com.clustercontrol.xcloud.bean.ModifyCloudLoginUserRequest();
			RestBeanUtil.convertBean(dtoReq, infoReq);
			infoReq.setCloudScopeId(cloudScopeId);
			infoReq.setLoginUserId(cloudLoginUserId);
			CloudLoginUser infoRes = new CloudLoginUser(
					CloudManager.singleton().getLoginUsers().modifyCloudLoginUser(infoReq));

			CloudLoginUserInfoResponse dtoRes = new CloudLoginUserInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		// パブリッククラウドの場合、認証情報の変更をクラウドログ監視を実行している
		// エージェントに伝達する
		// ログインユーザの変更がコミットされた後の必要があるため、ここで実行
		if (isPublic) {
			SettingUpdateInfo.getInstance().setCloudLogMonitorUpdateTime(HinemosTime.currentTimeMillis());
			CloudLogManagerUtil.broadcastConfiguredFlowControl();
		}
		return res;
	}

	/**
	 * クラウドスコープのサブアカウント優先度を取得するAPI
	 */
	@GET
	@Path("/cloudLoginUser_idOrderbyPriority/cloudScope/{cloudScopeId}/cloudLoginUser")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCloudLoginUserOrderPriority")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudLoginUserInfoResponseP1.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.CloudLoginUser, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getCloudLoginUserOrderPriority(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			AuthorizingValidator.validateScopeAdmin(cloudScopeId);
			List<String> infoResList = CloudManager.singleton().getLoginUsers().getCloudLoginUserPriority(cloudScopeId);

			List<CloudLoginUserInfoResponseP1> dtoResList = new ArrayList<>();
			for (String infoRes : infoResList) {
				CloudLoginUserInfoResponseP1 dtoRes = new CloudLoginUserInfoResponseP1();
				dtoRes.setLoginUserId(infoRes);
				dtoResList.add(dtoRes);
			}
			RestLanguageConverter.convertMessages(dtoResList);
			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * クラウドスコープのサブアカウント優先度を変更するAPI
	 */
	@PUT
	@Path("/cloudLoginUser_priority/cloudScope/{cloudScopeId}/cloudLoginUser")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCloudLoginUserPriority")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudLoginUserInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.CloudLoginUser, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.MODIFY,
			SystemPrivilegeMode.READ })
	public Response modifyCloudLoginUserPriority(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "ModifyCloudLoginUserPriorityRequestBody", content = @Content(schema = @Schema(implementation = ModifyCloudLoginUserPriorityRequest.class))) String requestBody)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			AuthorizingValidator.validateScopeAdmin(cloudScopeId);
			ModifyCloudLoginUserPriorityRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					ModifyCloudLoginUserPriorityRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();

			CloudManager.singleton().getLoginUsers().modifyCloudLoginUserPriority(cloudScopeId,
					dtoReq.getCloudLoginUserIdList());

			List<CloudLoginUserInfoResponse> dtoResList = new ArrayList<>();
			for (CloudLoginUserEntity entity : CloudManager.singleton().getLoginUsers()
					.getCloudLoginUserByCloudScope(cloudScopeId)) {
				CloudLoginUserInfoResponse dtoRes = new CloudLoginUserInfoResponse();
				RestBeanUtil.convertBeanNoInvalid(new CloudLoginUser(entity), dtoRes);
				dtoResList.add(dtoRes);
			}
			RestLanguageConverter.convertMessages(dtoResList);
			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * ロールとクラウドアカウントの対応関係を更新するAPI
	 */
	@PUT
	@Path("/cloudLoginUser_roleRelation/cloudScope/{cloudScopeId}/cloudLoginUser/{cloudLoginUserId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCloudLoginUserRoleRelation")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudLoginUserInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.CloudLoginUser, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.MODIFY,
			SystemPrivilegeMode.READ })
	public Response modifyCloudLoginUserRoleRelation(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "cloudLoginUserId") String cloudLoginUserId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "ModifyCloudLoginUserRoleRelationRequestBody", content = @Content(schema = @Schema(implementation = ModifyCloudLoginUserRoleRelationRequest.class))) String requestBody)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			AuthorizingValidator.validateScopeAdmin(cloudScopeId);
			ModifyCloudLoginUserRoleRelationRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					ModifyCloudLoginUserRoleRelationRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();

			List<RoleRelation> infoReqList = new ArrayList<>();
			for (RoleRelationRequest dtoRoleReq : dtoReq.getRoleRelations()) {
				RoleRelation infoDto = new RoleRelation();
				RestBeanUtil.convertBean(dtoRoleReq, infoDto);
				infoReqList.add(infoDto);
			}

			CloudLoginUserEntity infoRes = CloudManager.singleton().getLoginUsers().modifyRoleRelation(cloudScopeId,
					cloudLoginUserId, infoReqList);

			CloudLoginUserInfoResponse dtoRes = new CloudLoginUserInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(new CloudLoginUser(infoRes), dtoRes);
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * コンピュートノードを削除するAPI
	 */
	@DELETE
	@Path("/xcloud_instance/cloudScope/{cloudScopeId}/location/{locationId}/resource")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "RemoveInstances")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InstanceInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.Instance, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.EXEC,
			SystemPrivilegeMode.READ })
	public Response removeInstances(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId,
			@QueryParam(value = "instanceIds") @ArrayTypeParam String instanceIds, @Context Request request,
			@Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			if (instanceIds == null) {
				return res;
			}
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);
			List<String> instanceIdList = Arrays.asList(instanceIds.split(","));

			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
			List<InstanceEntity> infoResList = CloudManager.singleton()
					.getInstances(user, user.getCloudScope().getLocation(locationId)).removeInstances(instanceIdList);

			List<InstanceInfoResponse> dtoResList = new ArrayList<>();
			for (InstanceEntity infoRes : infoResList) {
				InstanceInfoResponse dtoRes = new InstanceInfoResponse();
				RestBeanUtil.convertBean(new Instance(infoRes), dtoRes);
				dtoResList.add(dtoRes);
			}
			RestLanguageConverter.convertMessages(dtoResList);

			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * コンピュートノードを取得するAPI
	 */
	@GET
	@Path("/xcloud_instance/cloudScope/{cloudScopeId}/location/{locationId}/resource")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetInstances")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InstanceInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Instance, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getInstances(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId,
			@QueryParam(value = "instanceIds") @ArrayTypeParam String instanceIds, @Context Request request,
			@Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);
			List<Instance> infoResList = null;
			List<String> instanceIdList = new ArrayList<>();
			if (instanceIds != null && !instanceIds.isEmpty()) {
				instanceIdList = Arrays.asList(instanceIds.split(","));
			}
			try {
				ActionMode.enterAutoDetection();
				CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
						.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
				List<InstanceEntity> instanceEntities = CloudManager.singleton()
						.getInstances(user, user.getCloudScope().getLocation(locationId))
						.updateInstances(instanceIdList);
				infoResList = Instance.convertWebEntities(instanceEntities);
			} finally {
				ActionMode.leaveAutoDetection();
			}

			List<InstanceInfoResponse> dtoResList = new ArrayList<>();
			for (Instance infoRes : infoResList) {
				InstanceInfoResponse dtoRes = new InstanceInfoResponse();
				RestBeanUtil.convertBean(infoRes, dtoRes);
				dtoResList.add(dtoRes);
			}
			RestLanguageConverter.convertMessages(dtoResList);

			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * コンピュートノードを起動するAPI (対象となるインスタンスの起動を実施するため、POSTリクエスト)
	 */
	@POST
	@Path("/xcloud_instance_powerOn/cloudScope/{cloudScopeId}/location/{locationId}/resource")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "PowerOnInstances")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PowerOnInstances.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.PowerOn, target = LogTarget.Instance, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.EXEC })
	public Response powerOnInstances(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId,
			@RequestBody(description = "powerOnInstancesBody", content = @Content(schema = @Schema(implementation = PowerOnInstancesRequest.class))) String requestBody,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {

			PowerOnInstancesRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					PowerOnInstancesRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();

			if (dtoReq.getInstanceIds() == null || dtoReq.getInstanceIds().isEmpty()) {
				return res;
			}
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);
			List<String> instanceIdList = dtoReq.getInstanceIds();

			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
			CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId))
					.powerOnInstances(instanceIdList);

			PowerOnInstances dtoRes = new PowerOnInstances();
			dtoRes.setInstanceIds(dtoReq.getInstanceIds());
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * コンピュートノードを停止するAPI (対象となるインスタンスの停止を実施するため、POSTリクエスト)
	 */
	@POST
	@Path("/xcloud_instance_powerOff/cloudScope/{cloudScopeId}/location/{locationId}/resource")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "PowerOffInstances")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PowerOffInstances.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.PowerOff, target = LogTarget.Instance, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.EXEC })
	public Response powerOffInstances(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId,
			@RequestBody(description = "powerOffInstancesBody", content = @Content(schema = @Schema(implementation = PowerOffInstancesRequest.class))) String requestBody,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {

			PowerOffInstancesRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					PowerOffInstancesRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();

			if (dtoReq.getInstanceIds() == null || dtoReq.getInstanceIds().isEmpty()) {
				return res;
			}
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);
			List<String> instanceIdList = dtoReq.getInstanceIds();

			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
			CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId))
					.powerOffInstances(instanceIdList);

			PowerOffInstances dtoRes = new PowerOffInstances();
			dtoRes.setInstanceIds(instanceIdList);
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * コンピュートノードを一時停止するAPI (対象となるインスタンスの一時停止を実施するため、POSTリクエスト)
	 */
	@POST
	@Path("/xcloud_instance_suspend/cloudScope/{cloudScopeId}/location/{locationId}/resource")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SuspendInstances")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SuspendInstances.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Suspend, target = LogTarget.Instance, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.EXEC })
	public Response suspendInstances(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId,
			@RequestBody(description = "suspendInstancesBody", content = @Content(schema = @Schema(implementation = SuspendInstancesRequest.class))) String requestBody,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			SuspendInstancesRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					SuspendInstancesRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();

			// Validation
			// instanceIdsは未指定なら何もしない
			if (dtoReq.getInstanceIds() == null || dtoReq.getInstanceIds().isEmpty()) {
				return res;
			}
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);

			List<String> instanceIdList = dtoReq.getInstanceIds();

			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
			CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId))
					.suspendInstances(instanceIdList);

			SuspendInstances dtoRes = new SuspendInstances();
			dtoRes.setInstanceIds(instanceIdList);
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * コンピュートノードを再起動するAPI (対象となるインスタンスの再起動を実施するため、POSTリクエスト)
	 */
	@POST
	@Path("/xcloud_instance_reboot/cloudScope/{cloudScopeId}/location/{locationId}/resource")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "RebootInstances")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RebootInstances.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Reboot, target = LogTarget.Instance, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.EXEC })
	public Response rebootInstances(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId,
			@RequestBody(description = "rebootInstancesBody", content = @Content(schema = @Schema(implementation = RebootInstancesRequest.class))) String requestBody,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {

			RebootInstancesRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					RebootInstancesRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();

			// Validation
			// instanceIdsは未指定なら何もしない
			if (dtoReq.getInstanceIds() == null || dtoReq.getInstanceIds().isEmpty()) {
				return res;
			}
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);

			List<String> instanceIdList = dtoReq.getInstanceIds();

			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
			CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId))
					.rebootInstances(instanceIdList);

			RebootInstances dtoRes = new RebootInstances();
			dtoRes.setInstanceIds(dtoReq.getInstanceIds());
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * コンピュートノードのスナップショットを作成するAPI
	 */
	@POST
	@Path("/xcloud_instanceBackup/cloudScope/{cloudScopeId}/location/{locationId}/resource")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SnapshotInstance")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InstanceBackupResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.InstanceBackup, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.EXEC,
			SystemPrivilegeMode.READ })
	public Response snapshotInstance(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId,
			@RequestBody(description = "snapshotInstanceBody", content = @Content(schema = @Schema(implementation = SnapshotInstanceRequest.class))) String requestBody,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);

			SnapshotInstanceRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					SnapshotInstanceRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();

			com.clustercontrol.xcloud.bean.CreateInstanceSnapshotRequest infoReq = new com.clustercontrol.xcloud.bean.CreateInstanceSnapshotRequest();
			RestBeanUtil.convertBean(dtoReq, infoReq);

			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
			InstanceBackupEntity infoRes = CloudManager.singleton()
					.getInstances(user, user.getCloudScope().getLocation(locationId)).takeInstanceSnapshot(
							infoReq.getInstanceId(), infoReq.getName(), infoReq.getDescription(), infoReq.getOptions());

			InstanceBackupResponse dtoRes = new InstanceBackupResponse();
			RestBeanUtil.convertBeanNoInvalid(InstanceBackup.convertWebEntity(infoRes), dtoRes);
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * コンピュートノードのスナップショットを削除するAPI
	 */
	@DELETE
	@Path("/xcloud_instanceBackup/cloudScope/{cloudScopeId}/location/{locationId}/resource/{instanceId}/snapshot")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteInstanceSnapshots")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InstanceBackupEntryResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.InstanceBackup, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.EXEC,
			SystemPrivilegeMode.READ })
	public Response deleteInstanceSnapshots(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId, @PathParam(value = "instanceId") String instanceId,
			@QueryParam(value = "instanceSnapshotIds") String instanceSnapshotIds, @Context Request request,
			@Context UriInfo uriInfo) throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);

			List<String> instanceSnapshotIdList = new ArrayList<>();
			if (instanceSnapshotIds != null) {
				instanceSnapshotIdList = Arrays.asList(instanceSnapshotIds.split(","));
			}

			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
			List<InstanceBackupEntryEntity> entities = CloudManager.singleton()
					.getInstances(user, user.getCloudScope().getLocation(locationId))
					.deletInstanceSnapshots(instanceId, instanceSnapshotIdList);
			List<InstanceBackupEntry> infoResList = InstanceBackupEntry.convertWebEntities(entities);

			List<InstanceBackupEntryResponse> dtoResList = new ArrayList<>();
			for (InstanceBackupEntry infoRes : infoResList) {
				InstanceBackupEntryResponse dtoRes = new InstanceBackupEntryResponse();
				RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
				dtoResList.add(dtoRes);
			}
			RestLanguageConverter.convertMessages(dtoResList);

			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * スナップショットからコンピュートノードを複製するAPI
	 */
	@POST
	@Path("/xcloud_instance/cloudScope/{cloudScopeId}/location/{locationId}/resource")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "CloneBackupedInstance")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InstanceInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.Instance, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.EXEC,
			SystemPrivilegeMode.READ })
	public Response cloneBackupedInstance(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId,
			@RequestBody(description = "cloneBackupedInstanceBody", content = @Content(schema = @Schema(implementation = CloneBackupedInstanceRequest.class))) String requestBody,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);

			CloneBackupedInstanceRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					CloneBackupedInstanceRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();

			com.clustercontrol.xcloud.bean.CloneBackupedInstanceRequest infoReq = new com.clustercontrol.xcloud.bean.CloneBackupedInstanceRequest();
			RestBeanUtil.convertBean(dtoReq, infoReq);

			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
			InstanceEntity infoRes = CloudManager.singleton()
					.getInstances(user, user.getCloudScope().getLocation(locationId)).cloneBackupedInstance(infoReq);

			InstanceInfoResponse dtoRes = new InstanceInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(new Instance(infoRes), dtoRes);
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * コンピュートノードのスナップショット一覧を取得するAPI
	 */
	@GET
	@Path("/xcloud_instanceBackup/cloudScope/{cloudScopeId}/location/{locationId}/resource")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetInstanceBackups")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InstanceBackupResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.InstanceBackup, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getInstanceBackups(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId,
			@ArrayTypeParam @QueryParam(value = "instanceIds") String instanceIds, @Context Request request,
			@Context UriInfo uriInfo) throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);

			List<String> instanceIdList = new ArrayList<>();
			if (instanceIds != null) {
				instanceIdList = Arrays.asList(instanceIds.split(","));
			}

			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
			List<InstanceBackupEntity> entities = CloudManager.singleton()
					.getInstances(user, user.getCloudScope().getLocation(locationId))
					.updateInstanceBackups(instanceIdList);
			List<InstanceBackup> infoResList = InstanceBackup.convertWebEntities(entities);

			List<InstanceBackupResponse> dtoResList = new ArrayList<>();
			for (InstanceBackup infoRes : infoResList) {
				InstanceBackupResponse dtoRes = new InstanceBackupResponse();
				RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
				dtoResList.add(dtoRes);
			}
			RestLanguageConverter.convertMessages(dtoResList);

			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * コンピュートノードの構成を変更するAPI
	 */
	@POST
	@Path("/xcloud_instance/cloudScope/{cloudScopeId}/location/{locationId}/resource/{instanceId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyInstance")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InstanceInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Instance, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.MODIFY,
			SystemPrivilegeMode.READ })
	public Response modifyInstance(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId, @PathParam(value = "instanceId") String instanceId,
			@RequestBody(description = "modifyInstanceBody", content = @Content(schema = @Schema(implementation = ModifyInstanceRequest.class))) String requestBody,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			RestValidationUtil.identityValidate(MessageConstant.XCLOUD_CORE_INSTANCE_ID.name(), instanceId);
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);

			ModifyInstanceRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					ModifyInstanceRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();

			com.clustercontrol.xcloud.bean.ModifyInstanceRequest infoReq = new com.clustercontrol.xcloud.bean.ModifyInstanceRequest();
			RestBeanUtil.convertBean(dtoReq, infoReq);
			infoReq.setInstanceId(instanceId);

			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
			IInstances instances = CloudManager.singleton().getInstances(user,
					user.getCloudScope().getLocation(locationId));
			Instance infoRes = new Instance(instances.modifyInstance(infoReq));

			InstanceInfoResponse dtoRes = new InstanceInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * ストレージをコンピュートノードにアタッチするAPI
	 */
	@POST
	@Path("/xcloud_instacne_attachStorage/cloudScope/{cloudScopeId}/location/{locationId}/resource/{instanceId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AttachStorage")
	@APIResponses(value = { @APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Attach, target = LogTarget.Instance, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.MODIFY })
	public Response attachStorage(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId, @PathParam(value = "instanceId") String instanceId,
			@RequestBody(description = "attachStorageBody", content = @Content(schema = @Schema(implementation = AttachStorageRequest.class))) String requestBody,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);

			AttachStorageRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					AttachStorageRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();

			List<Option> options = new ArrayList<>();
			for (OptionRequest dtoOption : dtoReq.getOption()) {
				Option option = new Option();
				RestBeanUtil.convertBean(dtoOption, option);
				options.add(option);
			}

			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
			CloudManager.singleton().getStorages(user, user.getCloudScope().getLocation(locationId))
					.attachStorage(instanceId, dtoReq.getStorageId(), options);
		}

		return Response.status(Status.OK).build();
	}

	/**
	 * ストレージをコンピュートノードからデタッチするAPI
	 */
	@POST
	@Path("/xcloud_instacne_detachStorage/cloudScope/{cloudScopeId}/location/{locationId}/resource")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DetachStorage")
	@APIResponses(value = { @APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Detach, target = LogTarget.Instance, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.MODIFY })
	public Response detachStorage(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId,
			@RequestBody(description = "detachStorageBody", content = @Content(schema = @Schema(implementation = DetachStorageRequest.class))) String requestBody,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);

			DetachStorageRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					DetachStorageRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();

			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
			CloudManager.singleton().getStorages(user, user.getCloudScope().getLocation(locationId))
					.detachStorage(dtoReq.getStorageIds());
		}

		return Response.status(Status.OK).build();
	}

	/**
	 * ストレージを削除するAPI
	 */
	@DELETE
	@Path("/xcloud_storage/cloudScope/{cloudScopeId}/location/{locationId}/resource")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "RemoveStorages")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StorageInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.Storage, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.EXEC,
			SystemPrivilegeMode.READ })
	public Response removeStorages(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId,
			@ArrayTypeParam @QueryParam(value = "storageIds") String storageIds, @Context Request request,
			@Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			RestValidationUtil.notNullValidate(MessageConstant.XCLOUD_CORE_STORAGE_IDS.name(), storageIds);
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);

			List<String> storageIdList = Arrays.asList(storageIds.split(","));

			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
			List<StorageEntity> entityList = CloudManager.singleton()
					.getStorages(user, user.getCloudScope().getLocation(locationId)).removeStorages(storageIdList);
			List<Storage> infoResList = Storage.convertWebEntities(entityList);

			List<StorageInfoResponse> dtoResList = new ArrayList<>();
			for (Storage infoRes : infoResList) {
				StorageInfoResponse dtoRes = new StorageInfoResponse();
				RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
				dtoResList.add(dtoRes);
			}
			RestLanguageConverter.convertMessages(dtoResList);

			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * ストレージを取得するAPI
	 */
	@GET
	@Path("/xcloud_storage/cloudScope/{cloudScopeId}/location/{locationId}/resource")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetStorages")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StorageInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Storage, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getStorages(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId,
			@ArrayTypeParam @QueryParam(value = "storageIds") String storageIds, @Context Request request,
			@Context UriInfo uriInfo) throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);

			List<String> storageIdList = new ArrayList<>();
			if (storageIds != null) {
				storageIdList = Arrays.asList(storageIds.split(","));
			}

			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);

			List<StorageEntity> entityList = null;
			ILock lock = CloudUtil.getLock(CloudRestEndpoints.class.getName() + "getStorages", cloudScopeId,
					locationId);
			boolean hasLock = false;
			try {
				// 複数クライアントから同時に更新処理が走るとエンドポイントが枯渇する危険があるので、
				// クラウドスコープID、ロケーションID単位でロックを取得し排他する
				hasLock = lock.tryWriteLock();
				if (hasLock) {
					m_log.debug(String.format("getStorages(): grant lock cloudScopeId=%s, locationId=%s", cloudScopeId,
							locationId));
					entityList = CloudManager.singleton()
							.getStorages(user, user.getCloudScope().getLocation(locationId))
							.updateStorages(storageIdList);
				} else {
					m_log.debug(String.format(
							"getStorages(): lock granted by other. Notify Client. cloudScopeId=%s, locationId=%s",
							cloudScopeId, locationId));
					String[] args = { cloudScopeId, locationId };
					throw ErrorCode.UPDATE_ALREADY_RUNNING_FOR_RESOURCE.cloudManagerFault(args);
				}
			} finally {
				if (hasLock && lock != null) {
					m_log.debug(String.format("getStorages(): unlocked. cloudScopeId=%s, locationId=%s", cloudScopeId,
							locationId));
					lock.writeUnlock();
				}
			}
			List<Storage> infoResList = Storage.convertWebEntities(entityList);

			List<StorageInfoResponse> dtoResList = new ArrayList<>();
			for (Storage infoRes : infoResList) {
				StorageInfoResponse dtoRes = new StorageInfoResponse();
				RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
				dtoResList.add(dtoRes);
			}
			RestLanguageConverter.convertMessages(dtoResList);

			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * ストレージのスナップショットを作成するAPI
	 */
	@POST
	@Path("/xcloud_storageBackup/cloudScope/{cloudScopeId}/location/{locationId}/resource")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SnapshotStorage")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StorageBackupInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.StorageBackup, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.EXEC,
			SystemPrivilegeMode.READ })
	public Response snapshotStorage(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId,
			@RequestBody(description = "cloneBackupedStorageBody", content = @Content(schema = @Schema(implementation = CreateStorageSnapshotRequest.class))) String requestBody,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);

			CreateStorageSnapshotRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					CreateStorageSnapshotRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();

			com.clustercontrol.xcloud.bean.CreateStorageSnapshotRequest infoReq = new com.clustercontrol.xcloud.bean.CreateStorageSnapshotRequest();
			RestBeanUtil.convertBean(dtoReq, infoReq);

			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
			StorageBackupEntity infoRes = CloudManager.singleton()
					.getStorages(user, user.getCloudScope().getLocation(locationId)).takeStorageSnapshot(
							infoReq.getStorageId(), infoReq.getName(), infoReq.getDescription(), infoReq.getOptions());

			StorageBackupInfoResponse dtoRes = new StorageBackupInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(StorageBackup.convertWebEntity(infoRes), dtoRes);
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * ストレージのスナップショットを削除するAPI
	 */
	// ストレージIDに『/』が利用されているため、IDをURLに含めてDELETEメソッドで実行することが出来ない。
	// ユーザに非公開のAPIで、大きなデメリットも無いためPOSTメソッドとする
	// @see http://172.16.54.255/redmine/issues/6043
	@POST
	@Path("/xcloud_storageBackup/cloudScope/{cloudScopeId}/location/{locationId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteStorageSnapshots")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StorageBackupEntryResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.StorageBackup, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.EXEC,
			SystemPrivilegeMode.READ })
	public Response deleteStorageSnapshots(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId,
			@RequestBody(description = "deleteStorageSnapshotRequestBody", content = @Content(schema = @Schema(implementation = DeleteStorageSnapshotRequest.class))) String requestBody,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);

			DeleteStorageSnapshotRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					DeleteStorageSnapshotRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();

			String storageId = dtoReq.getStorageId();
			List<String> storageSnapshotIdList = dtoReq.getStorageSnapshotIds();

			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
			List<StorageBackupEntryEntity> entityList = CloudManager.singleton()
					.getStorages(user, user.getCloudScope().getLocation(locationId))
					.deleteStorageSnapshots(storageId, storageSnapshotIdList);
			List<StorageBackupEntry> infoResList = StorageBackupEntry.convertWebEntities(entityList);

			List<StorageBackupEntryResponse> dtoResList = new ArrayList<>();
			for (StorageBackupEntry infoRes : infoResList) {
				StorageBackupEntryResponse dtoRes = new StorageBackupEntryResponse();
				RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
				dtoResList.add(dtoRes);
			}
			RestLanguageConverter.convertMessages(dtoResList);
			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * スナップショットからストレージを複製するAPI
	 */
	@POST
	@Path("/xcloud_storage/cloudScope/{cloudScopeId}/location/{locationId}/resource")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "CloneBackupedStorage")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StorageInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.Storage, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.EXEC,
			SystemPrivilegeMode.READ })
	public Response cloneBackupedStorage(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId,
			@RequestBody(description = "cloneBackupedStorageBody", content = @Content(schema = @Schema(implementation = CloneBackupedStorageRequest.class))) String requestBody,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);

			CloneBackupedStorageRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					CloneBackupedStorageRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();

			com.clustercontrol.xcloud.bean.CloneBackupedStorageRequest infoReq = new com.clustercontrol.xcloud.bean.CloneBackupedStorageRequest();
			RestBeanUtil.convertBean(dtoReq, infoReq);

			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
			StorageEntity infoRes = CloudManager.singleton()
					.getStorages(user, user.getCloudScope().getLocation(locationId)).cloneBackupedStorage(infoReq);

			StorageInfoResponse dtoRes = new StorageInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(Storage.convertWebEntity(infoRes), dtoRes);
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * ストレージのスナップショットを取得するAPI
	 */
	@GET
	@Path("/xcloud_storageBackup/cloudScope/{cloudScopeId}/location/{locationId}/resource")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetStorageBackups")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StorageBackupInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.StorageBackup, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getStorageBackups(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId,
			@ArrayTypeParam @QueryParam(value = "storageIds") String storageIds, @Context Request request,
			@Context UriInfo uriInfo) throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);

			List<String> storageIdList = new ArrayList<>();
			if (storageIds != null) {
				storageIdList = Arrays.asList(storageIds.split(","));
			}

			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
			List<StorageBackupEntity> entityList = CloudManager.singleton()
					.getStorages(user, user.getCloudScope().getLocation(locationId))
					.updateStorageBackups(storageIdList);
			List<StorageBackup> infoResList = StorageBackup.convertWebEntities(entityList);

			List<StorageBackupInfoResponse> dtoResList = new ArrayList<>();
			for (StorageBackup infoRes : infoResList) {
				StorageBackupInfoResponse dtoRes = new StorageBackupInfoResponse();
				RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
				dtoResList.add(dtoRes);
			}
			RestLanguageConverter.convertMessages(dtoResList);

			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * クラウドスコープ内のリソース情報を最新化するAPI
	 */
	@GET
	@Path("/xcloudRepository/cloudScope/{cloudScopeId}/location/{locationId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "UpdateLocationRepository")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HRepositoryResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.XcloudRepository, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response updateLocationRepository(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId, @Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);

			HRepository infoRes = null;
			ILock lock = null;
			Boolean hasLock = false;
			try {
				// 複数クライアントから同時に更新処理が走るとエンドポイントが枯渇する危険があるので、
				// クラウドスコープID、ロケーションID単位でロックを取得し排他する
				lock = CloudUtil.getLock(CloudRestEndpoints.class.getName() + "updateLocationRepository", cloudScopeId,
						locationId);
				hasLock = lock.tryWriteLock();
				if (hasLock) {
					m_log.debug(String.format("updateLocationRepository(): grant lock cloudScopeId=%s, locationId=%s",
							cloudScopeId, locationId));
					ActionMode.enterAutoDetection();
					infoRes = CloudManager.singleton().getRepository().updateLocationRepository(cloudScopeId,
							locationId);
				} else {
					m_log.debug(String.format(
							"updateLocationRepository(): lock granted by other. Notify Client. cloudScopeId=%s, locationId=%s",
							cloudScopeId, locationId));
					String[] args = { cloudScopeId, locationId };
					throw ErrorCode.UPDATE_ALREADY_RUNNING_FOR_RESOURCE.cloudManagerFault(args);
				}
			} finally {
				ActionMode.leaveAutoDetection();
				if (hasLock && lock != null) {
					m_log.debug(String.format("updateLocationRepository(): unkocked. cloudScopeId=%s, locationId=%s",
							cloudScopeId, locationId));
					lock.writeUnlock();
				}
			}

			HRepositoryResponse dtoRes = new HRepositoryResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

			// 各Facilityに親クラウドスコープID、プラットフォームをセット
			for (HFacilityResponse rootFacility : dtoRes.getFacilities()) {
				for (HFacilityResponse cloudScopeFacility : rootFacility.getFacilities()) {
					recursiveSetParentCloudScopeIdwithPlatform(
							cloudScopeFacility.getCloudScope().getEntity().getCloudScopeId(),
							cloudScopeFacility.getCloudScope().getEntity().getPlatformId(), cloudScopeFacility);
					cloudScopeFacility.getCloudScope().getEntity()
							.setPublic(isPublic(cloudScopeFacility.getCloudScope().getEntity().getPlatformId()));
				}
			}
			for (CloudScopeInfoResponse dtoCloudScope : dtoRes.getCloudScopes()) {
				dtoCloudScope.getEntity().setPublic(isPublic(dtoCloudScope.getEntity().getPlatformId()));
			}

			// CloudSpecを変換
			List<CloudPlatform> infoCloudPlatforms = infoRes.getPlatforms();
			for (CloudPlatform infoCloudPlatform : infoCloudPlatforms) {
				for (CloudPlatformInfoResponse dtoCloudPlatform : dtoRes.getPlatforms()) {
					if (infoCloudPlatform.getId().equals(dtoCloudPlatform.getEntity().getPlatformId())) {
						convertCloudSpec(infoCloudPlatform, dtoCloudPlatform);
						break;
					}
				}
			}
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * クラウドスコープ内のリソース情報を取得するAPI
	 */
	@GET
	@Path("/xcloudRepository")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRepository")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HRepositoryResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.XcloudRepository, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getRepository(@QueryParam(value = "roleId") String roleId, @Context Request request,
			@Context UriInfo uriInfo) throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			HRepository infoRes;
			if (roleId == null) {
				infoRes = CloudManager.singleton().getRepository().getRepository();
			} else {
				infoRes = CloudManager.singleton().getRepository().getRepository(roleId);
			}

			HRepositoryResponse dtoRes = new HRepositoryResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

			// 各Facilityに親クラウドスコープID、プラットフォームをセット
			for (HFacilityResponse rootFacility : dtoRes.getFacilities()) {
				for (HFacilityResponse cloudScopeFacility : rootFacility.getFacilities()) {
					recursiveSetParentCloudScopeIdwithPlatform(
							cloudScopeFacility.getCloudScope().getEntity().getCloudScopeId(),
							cloudScopeFacility.getCloudScope().getEntity().getPlatformId(), cloudScopeFacility);
					cloudScopeFacility.getCloudScope().getEntity()
							.setPublic(isPublic(cloudScopeFacility.getCloudScope().getEntity().getPlatformId()));
				}
			}
			for (CloudScopeInfoResponse cloudScope : dtoRes.getCloudScopes()) {
				cloudScope.getEntity().setPublic(isPublic(cloudScope.getEntity().getPlatformId()));
			}
			// CloudSpecを変換
			List<CloudPlatform> infoCloudPlatforms = infoRes.getPlatforms();
			for (CloudPlatform infoCloudPlatform : infoCloudPlatforms) {
				for (CloudPlatformInfoResponse dtoCloudPlatform : dtoRes.getPlatforms()) {
					if (infoCloudPlatform.getId().equals(dtoCloudPlatform.getEntity().getPlatformId())) {
						convertCloudSpec(infoCloudPlatform, dtoCloudPlatform);
						break;
					}
				}
			}
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * クラウドサービスが提供する各種サービスの状態を取得するAPI
	 */
	@GET
	@Path("/xcloud_serviceCondition")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetPlatformServiceConditions")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PlatformServiceConditionResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.ServiceCondition, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getPlatformServiceConditions(@QueryParam(value = "cloudScopeId") String cloudScopeId,
			@QueryParam(value = "locationId") String locationId, @QueryParam(value = "roleId") String roleId,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			// cloudScopeIdは空なら何も返さない。
			if (cloudScopeId == null) {
				return res;
			}

			if (locationId == null && roleId == null) {
				AuthorizingValidator.validateScope(cloudScopeId);
			} else if (locationId != null && roleId == null) {
				AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);
			} else if (locationId == null && roleId != null) {
				AuthorizingValidator.validateScopeRole(cloudScopeId, roleId);
			} else {
				AuthorizingValidator.validateScopeLocationRole(cloudScopeId, roleId, locationId);
			}

			List<PlatformServiceCondition> infoResList;

			if (locationId == null) {
				infoResList = CloudManager.singleton().getCloudScopes().getPlatformServiceConditions(cloudScopeId);
			} else {
				infoResList = CloudManager.singleton().getCloudScopes().getPlatformServiceConditions(cloudScopeId,
						locationId);
			}

			List<PlatformServiceConditionResponse> dtoResList = new ArrayList<>();

			for (PlatformServiceCondition infoRes : infoResList) {
				PlatformServiceConditionResponse dtoRes = new PlatformServiceConditionResponse();
				RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
				dtoResList.add(dtoRes);
			}
			RestLanguageConverter.convertMessages(dtoResList);

			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * クラウドサービスが提供する各種サービスの状態を変更するAPI
	 */
	@PUT
	@Path("/xcloud_serviceCondition/cloudScope/{cloudScopeId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyPlatformServiceCondition")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PlatformServiceConditionResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Role, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.EXEC })
	public Response modifyPlatformServiceCondition(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@RequestBody(description = "modifyPlatformServiceConditionBody", content = @Content(schema = @Schema(implementation = ModifyPlatformServiceConditionRequest.class))) String requestBody,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			ModifyPlatformServiceConditionRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					ModifyPlatformServiceConditionRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();
			String locationId = dtoReq.getLocationId();

			// Validation
			if (locationId == null) {
				AuthorizingValidator.validateScope(cloudScopeId);
			} else {
				AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);
			}

			com.clustercontrol.xcloud.bean.ModifyPlatformServiceConditionRequest infoReq = new com.clustercontrol.xcloud.bean.ModifyPlatformServiceConditionRequest();
			RestBeanUtil.convertBeanNoInvalid(dtoReq, infoReq);
			infoReq.setCloudScopeId(cloudScopeId);
			infoReq.setLocationId(locationId);
			List<PlatformServiceCondition> infoResList = CloudManager.singleton().getCloudScopes()
					.modifyPlatformServiceCondition(infoReq);

			List<PlatformServiceConditionResponse> dtoResList = new ArrayList<>();
			for (PlatformServiceCondition infoRes : infoResList) {
				PlatformServiceConditionResponse dtoRes = new PlatformServiceConditionResponse();
				RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
				dtoResList.add(dtoRes);
			}
			RestLanguageConverter.convertMessages(dtoResList);

			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * 全ネットワークを取得するAPI
	 */
	@GET
	@Path("/xcloud_network/cloudScope/{cloudScopeId}/location/{locationId}/resource")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetAllNetworks")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NetworkInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Network, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getAllNetworks(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId, @Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);

			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);

			List<Network> infoResList = null;
			ILock lock = CloudUtil.getLock(CloudRestEndpoints.class.getName() + "getAllNetworks", cloudScopeId,
					locationId);
			boolean hasLock = false;
			try {
				// 複数クライアントから同時に更新処理が走るとエンドポイントが枯渇する危険があるので、
				// クラウドスコープID、ロケーションID単位でロックを取得し排他する
				hasLock = lock.tryWriteLock();
				if (hasLock) {
					m_log.debug(String.format("getAllNetworks(): grant lock cloudScopeId=%s, locationId=%s",
							cloudScopeId, locationId));
					infoResList = CloudManager.singleton()
							.getNetworks(user, user.getCloudScope().getLocation(locationId)).getAllNetwork();
				} else {
					m_log.debug(String.format(
							"getAllNetworks(): lock granted by other. Notify Client. cloudScopeId=%s, locationId=%s",
							cloudScopeId, locationId));
					String[] args = { cloudScopeId, locationId };
					throw ErrorCode.UPDATE_ALREADY_RUNNING_FOR_RESOURCE.cloudManagerFault(args);
				}
			} finally {
				if (hasLock && lock != null) {
					m_log.debug(String.format("getAllNetworks(): unlocked. cloudScopeId=%s, locationId=%s",
							cloudScopeId, locationId));
					lock.writeUnlock();
				}
			}

			List<NetworkInfoResponse> dtoResList = new ArrayList<>();

			for (Network infoRes : infoResList) {
				NetworkInfoResponse dtoRes = new NetworkInfoResponse();
				RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
				dtoResList.add(dtoRes);
			}
			RestLanguageConverter.convertMessages(dtoResList);

			res = Response.status(Status.OK).entity(dtoResList).build();
		}
		return res;
	}

	/**
	 * 課金詳細情報の収集設定を更新するAPI
	 */
	@POST
	@Path("/cloudScope_billing/{cloudScopeId}")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudScopeInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Billing, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.MODIFY,
			SystemPrivilegeMode.READ })
	public Response modifyBillingSetting(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@RequestBody(description = "modifyBillingSettingBody", content = @Content(schema = @Schema(implementation = ModifyBillingSettingRequest.class))) String requestBody,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			RestValidationUtil.identityValidate(MessageConstant.XCLOUD_CORE_CLOUDSCOPE_ID.name(), cloudScopeId);
			if (!AuthorizingUtil.checkHinemousUser_administrators_account(
					Session.current().getHinemosCredential().getUserId(), cloudScopeId)) {
				throw ErrorCode.NEED_ADMINISTRATORS_ROLE_OR_ACCOUNT_USER
						.cloudManagerFault(Session.current().getHinemosCredential().getUserId(), cloudScopeId);
			}

			ModifyBillingSettingRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					ModifyBillingSettingRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();

			com.clustercontrol.xcloud.bean.ModifyBillingSettingRequest infoReq = new com.clustercontrol.xcloud.bean.ModifyBillingSettingRequest();

			RestBeanUtil.convertBean(dtoReq, infoReq);
			infoReq.setCloudScopeId(cloudScopeId);
			CloudScopeEntity infoRes = CloudManager.singleton().getCloudScopes().modifyBillingSetting(infoReq);

			CloudScopeInfoResponse dtoRes = new CloudScopeInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(CloudScope.convertWebEntity(infoRes), dtoRes);
			dtoRes.getEntity().setPublic(isPublic(infoRes.getPlatformId()));
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * クラウドスコープの収集済み課金詳細情報を取得するAPI
	 */
	@GET
	@Path("/billingDetail_byCloudScope")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetBillingDetailsByCloudScope")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = BillingResultResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.BillingDetail, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getBillingDetailsByCloudScope(@QueryParam(value = "cloudScopeId") String cloudScopeId,
			@QueryParam(value = "year") String year, @QueryParam(value = "month") String month,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			RestValidationUtil.identityValidate(MessageConstant.XCLOUD_CORE_CLOUDSCOPE_ID.name(), cloudScopeId);
			RestValidationUtil.notNullIntegerValidate(XCLOUD_CORE_YEAR, year);
			RestValidationUtil.notNullIntegerValidate(XCLOUD_CORE_MONTH, month);
			AuthorizingValidator.validateScope(cloudScopeId);

			BillingResult infores = CloudManager.singleton().getBillings().getBillingDetailsByCloudScope(cloudScopeId,
					Integer.valueOf(year), Integer.valueOf(month));

			BillingResultResponse dtoRes = new BillingResultResponse();
			RestBeanUtil.convertBeanNoInvalid(infores, dtoRes);
			RestLanguageConverter.convertMessages(dtoRes);
			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * スコープ単位の収集済み課金詳細情報を取得するAPI
	 */
	@GET
	@Path("/billingDetail_byFacility")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetBillingDetailsByFacility")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = BillingResultResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.BillingDetail, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getBillingDetailsByFacility(@QueryParam(value = "facilityId") String facilityId,
			@QueryParam(value = "year") String year, @QueryParam(value = "month") String month,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			RestValidationUtil.identityValidate(XCLOUD_CORE_FACILITY_ID, facilityId);
			RestValidationUtil.notNullIntegerValidate(XCLOUD_CORE_YEAR, year);
			RestValidationUtil.notNullIntegerValidate(XCLOUD_CORE_MONTH, month);
			AuthorizingValidator.validateFacility(facilityId);

			BillingResult infoRes = CloudManager.singleton().getBillings().getBillingDetailsByFacility(facilityId,
					Integer.valueOf(year), Integer.valueOf(month));

			BillingResultResponse dtoRes = new BillingResultResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			RestLanguageConverter.convertMessages(dtoRes);
			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * クラウドスコープの収集済み課金詳細情報をダウンロードするAPI
	 */
	@GET
	@Path("/billingDetail_byCloudScope_download")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DownloadBillingDetailsByCloudScope")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON })
	@RestLog(action = LogAction.Download, target = LogTarget.BillingDetail, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response downloadBillingDetailsByCloudScope(@QueryParam(value = "cloudScopeId") String cloudScopeId,
			@QueryParam(value = "year") String year, @QueryParam(value = "month") String month,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			RestValidationUtil.identityValidate(MessageConstant.XCLOUD_CORE_CLOUDSCOPE_ID.name(), cloudScopeId);
			RestValidationUtil.notNullIntegerValidate(XCLOUD_CORE_YEAR, year);
			RestValidationUtil.notNullIntegerValidate(XCLOUD_CORE_MONTH, month);
			AuthorizingValidator.validateScope(cloudScopeId);

			File tempFile;

			try {
				tempFile = RestTempFileUtil.createTempFile(RestTempFileType.CLOUD).toFile();
			} catch (IOException e) {
				m_log.warn("Failed to create tempFile.");
				throw new HinemosUnknown(e);
			}

			IBillings billings = CloudManager.singleton().getBillings();
			String fileName = billings.writeBillingDetailsByCloudScope(cloudScopeId, Integer.valueOf(year),
					Integer.valueOf(month), tempFile);

			res = Response.ok(RestTempFileUtil.getTempFileStream(tempFile))
					.header("Content-Disposition", "filename=\"" + fileName + "\"").build();
		}
		return res;
	}

	/**
	 * スコープ単位の収集済み課金詳細情報をダウンロードするAPI
	 */
	@GET
	@Path("/billingDetail_byFacility_download")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DownloadBillingDetailsByFacility")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON })
	@RestLog(action = LogAction.Download, target = LogTarget.BillingDetail, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response downloadBillingDetailsByFacility(@QueryParam(value = "facilityId") String facilityId,
			@QueryParam(value = "year") String year, @QueryParam(value = "month") String month,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			RestValidationUtil.identityValidate(XCLOUD_CORE_FACILITY_ID, facilityId);
			RestValidationUtil.notNullIntegerValidate(XCLOUD_CORE_YEAR, year);
			RestValidationUtil.notNullIntegerValidate(XCLOUD_CORE_MONTH, month);
			AuthorizingValidator.validateFacility(facilityId);

			File tempFile;

			try {
				tempFile = RestTempFileUtil.createTempFile(RestTempFileType.CLOUD).toFile();
			} catch (IOException e) {
				m_log.warn("Failed to create tempFile.");
				throw new HinemosUnknown(e);
			}

			IBillings billings = CloudManager.singleton().getBillings();
			String fileName = billings.writeBillingDetailsByFacility(facilityId, Integer.valueOf(year),
					Integer.valueOf(month), tempFile);

			res = Response.ok(RestTempFileUtil.getTempFileStream(tempFile))
					.header("Content-Disposition", "filename=\"" + fileName + "\"").build();
		}
		return res;
	}

	/**
	 * クラウドサービスから課金情報を再収集するAPI
	 */
	@POST
	@Path("/billingDetail")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "RefreshBillingDetails")
	@APIResponses(value = { @APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Get, target = LogTarget.BillingDetail, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.MODIFY })
	public Response refreshBillingDetails(
			@RequestBody(description = "refreshBillingDetailsBody", content = @Content(schema = @Schema(implementation = RefreshBillingDetailsRequest.class))) String requestBody,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {

		RefreshBillingDetailsRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				RefreshBillingDetailsRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();
		String cloudScopeId = dtoReq.getCloudScopeId();
		// Validation
		RestValidationUtil.identityValidate(MessageConstant.XCLOUD_CORE_CLOUDSCOPE_ID.name(), cloudScopeId);

		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			CloudManager.singleton().getBillings().refreshBillingDetails(cloudScopeId);
		}

		return Response.status(Status.OK).build();
	}

	/**
	 * クラウドサービスが提供するサービスを取得するAPI
	 */
	@GET
	@Path("/cloudService_serviceName")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetPlatformServicesOnlyServiceName")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetPlatformServicesOnlyServiceNameResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Cloudservice, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getPlatformServicesOnlyServiceName(@QueryParam(value = "cloudScopeId") String cloudScopeId,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole {

		// Validation
		RestValidationUtil.identityValidate(MessageConstant.XCLOUD_CORE_CLOUDSCOPE_ID.name(), cloudScopeId);

		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			List<String> platformServices = CloudManager.singleton().getBillings().getPlatformServices(cloudScopeId);

			GetPlatformServicesOnlyServiceNameResponse dtoRes = new GetPlatformServicesOnlyServiceNameResponse();
			dtoRes.setPlatformServices(platformServices);
			RestLanguageConverter.convertMessages(dtoRes);
			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * ログインユーザが参照可能なクラウドサービスが提供するサービスを取得するAPI
	 */
	@GET
	@Path("/cloudService_forLoginUser")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetPlatformServiceForLoginUser")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetPlatformServiceForLoginUserResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.CloudService, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getPlatformServiceForLoginUser(@QueryParam(value = "facilityId") String facilityId,
			@QueryParam(value = "roleId") String roleId, @Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, FacilityNotFound, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			RestValidationUtil.identityValidate(XCLOUD_CORE_FACILITY_ID, facilityId);
			RestValidationUtil.notNullValidate(XCLOUD_CORE_ROLE_ID, roleId);

			List<String> nodeFacilities;
			if (RepositoryControllerBeanWrapper.bean().isNode(facilityId)) {
				nodeFacilities = Arrays.asList(facilityId);
			} else {
				nodeFacilities = RepositoryControllerBeanWrapper.bean().getNodeFacilityIdList(facilityId, roleId, 0);// 0は配下すべて
			}

			CloudManager cManager = CloudManager.singleton();
			List<CloudScopeEntity> cloudScopes = cManager.getCloudScopes().getCloudScopesByCurrentHinemosUser();
			Set<PlatformServicesResponse> platiformServices = new HashSet<>();
			for (CloudScopeEntity entity : cloudScopes) {
				String nodeId = FacilityIdUtil.getCloudScopeNodeId(entity.getPlatformId(), entity.getId());
				if (nodeFacilities.contains(nodeId)) {
					List<String> services = cManager.getBillings().getPlatformServices(entity.getId());
					for (String service : services) {
						PlatformServicesResponse platiformService = new PlatformServicesResponse();
						platiformService.setPlatformId(entity.getPlatformId());
						platiformService.setServiceId(service);
						platiformServices.add(platiformService);
					}
				}
			}

			GetPlatformServiceForLoginUserResponse dtoRes = new GetPlatformServiceForLoginUserResponse();
			dtoRes.setPlatiformServices(new ArrayList<>(platiformServices));
			RestLanguageConverter.convertMessages(dtoRes);
			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * クラウドサービスが提供する全サービスを取得するAPI
	 */
	@GET
	@Path("/cloudService")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetPlatformServices")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetPlatformServicesResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.CloudService, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getPlatformServices(@QueryParam(value = "facilityId") String facilityId,
			@QueryParam(value = "roleId") String roleId, @Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, FacilityNotFound, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			RestValidationUtil.identityValidate(XCLOUD_CORE_FACILITY_ID, facilityId);
			RestValidationUtil.notNullValidate(XCLOUD_CORE_ROLE_ID, roleId);

			List<String> nodeFacilities;
			Map<String, CloudScopeEntity> cloudScopes = new HashMap<>();
			if (RepositoryControllerBeanWrapper.bean().isNode(facilityId)) {
				nodeFacilities = Arrays.asList(facilityId);
			} else {
				nodeFacilities = RepositoryControllerBeanWrapper.bean().getNodeFacilityIdList(facilityId, roleId, 0);// 0は配下すべて
			}

			if (RoleIdConstant.isAdministratorRole(roleId)) {
				CloudManager.singleton().getCloudScopes().getAllCloudScopes().stream()
						.forEach(s -> cloudScopes.put(s.getCloudScopeId(), s));
			} else {
				List<CloudLoginUserEntity> users = CloudManager.singleton().getLoginUsers()
						.getCloudLoginUserByRole(roleId);
				for (CloudLoginUserEntity user : users) {
					if (!cloudScopes.containsKey(user.getCloudScopeId())) {
						cloudScopes.put(user.getCloudScopeId(), user.getCloudScope());
					}
				}
			}

			CloudManager cManager = CloudManager.singleton();
			Set<PlatformServicesResponse> platiformServices = new HashSet<>();
			for (CloudScopeEntity entity : cloudScopes.values()) {
				String nodeId = FacilityIdUtil.getCloudScopeNodeId(entity.getPlatformId(), entity.getId());
				if (nodeFacilities.contains(nodeId)) {
					List<String> services = cManager.getBillings().getPlatformServices(entity.getId());
					for (String service : services) {
						PlatformServicesResponse platiformService = new PlatformServicesResponse();
						platiformService.setPlatformId(entity.getPlatformId());
						platiformService.setServiceId(service);
						platiformServices.add(platiformService);
					}
				}
			}

			GetPlatformServicesResponse dtoRes = new GetPlatformServicesResponse();
			dtoRes.setPlatiformServices(new ArrayList<>(platiformServices));
			RestLanguageConverter.convertMessages(dtoRes);
			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * VM/クラウド管理機能が有効かチェックするAPI
	 */
	@GET
	@Path("/checkPublish")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "CheckPublish")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CheckPublishResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Version, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response checkPublish(@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			boolean publish = KeyCheck.checkKey(ActivationKeyConstant.TYPE_XCLOUD);

			if (!publish) {
				throw new CloudManagerException("expiration of a term", ErrorCode.UNEXPECTED.getMessage());
			}

			CheckPublishResponse dtoRes = new CheckPublishResponse();
			dtoRes.setPublish(publish);

			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * ストレージ情報を含むコンピュートノードを取得するAPI
	 */
	@GET
	@Path("/xcloud_instance_withStorage/cloudScope/{cloudScopeId}/location/{locationId}/resource/{instanceId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetInstanceWithStorage")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetInstanceWithStorageResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Instance, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getInstanceWithStorage(@PathParam(value = "cloudScopeId") String cloudScopeId,
			@PathParam(value = "locationId") String locationId, @PathParam(value = "instanceId") String instanceId,
			@Context Request request, @Context UriInfo uriInfo)
			throws CloudManagerException, InvalidUserPass, InvalidRole, HinemosUnknown {
		Response res = null;
		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			// Validation
			AuthorizingValidator.validateScopeLocation(cloudScopeId, locationId);

			InstanceEntity entity = null;
			Instance infoRes = null;

			try {
				ActionMode.enterAutoDetection();
				CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
						.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
				entity = CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId))
						.updateInstances(Arrays.asList(instanceId)).get(0);
				infoRes = Instance.convertWebEntity(entity);
			} finally {
				ActionMode.leaveAutoDetection();
			}

			GetInstanceWithStorageResponse dtoRes = new GetInstanceWithStorageResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

			// backupをDTO変換
			InstanceBackup infoBackup = InstanceBackup.convertWebEntity(entity.getBackup());
			InstanceBackupResponse dtoBackup = new InstanceBackupResponse();
			RestBeanUtil.convertBeanNoInvalid(infoBackup, dtoBackup);
			dtoRes.setBackup(dtoBackup);
			RestLanguageConverter.convertMessages(dtoRes);

			res = Response.status(Status.OK).entity(dtoRes).build();
		}
		return res;
	}

	/**
	 * RestBeanUtilではCloudPlatformInfoResponse#cloudSpecに値をセット出来ないので、個別に変換を行う。
	 * 
	 * @param infoCloudPlatform
	 * @param dtoCloudPlatform
	 * @throws HinemosUnknown
	 */
	private void convertCloudSpec(CloudPlatform infoCloudPlatform, CloudPlatformInfoResponse dtoCloudPlatform)
			throws HinemosUnknown {
		if (infoCloudPlatform == null) {
			return;
		}
		CloudSpecResponse dtoCloudSpec = new CloudSpecResponse();
		RestBeanUtil.convertBeanNoInvalid(infoCloudPlatform.getCloudSpec(), dtoCloudSpec);
		dtoCloudPlatform.setCloudSpec(dtoCloudSpec);
	}

	protected interface InstancesExecutor {
		void execute(IInstances instances, List<String> instanceIds) throws CloudManagerException;

		void throwException(List<String> failedLocations) throws CloudManagerException;
	}

	private void recursiveSetParentCloudScopeIdwithPlatform(String parentCloudScopeId, String platformId,
			HFacilityResponse facility) {
		facility.setParentCloudScopeId(parentCloudScopeId);
		facility.setPlatformId(platformId);
		for (HFacilityResponse child : facility.getFacilities()) {
			recursiveSetParentCloudScopeIdwithPlatform(parentCloudScopeId, platformId, child);
		}
	}

	public static Boolean isPublic(String platformId) throws CloudManagerException {
		return CloudManager.singleton().optionCall(platformId, new CloudManager.OptionCallable<Boolean>() {
			@Override
			public Boolean call(ICloudOption option) throws CloudManagerException {
				return option.getCloudSpec().isPublicCloud();
			}
		});
	}
}
