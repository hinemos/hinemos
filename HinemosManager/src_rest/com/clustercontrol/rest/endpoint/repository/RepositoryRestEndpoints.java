/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_404;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_409;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

import com.clustercontrol.HinemosManagerMain;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeFunction;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NodeConfigSettingDuplicate;
import com.clustercontrol.fault.NodeConfigSettingNotFound;
import com.clustercontrol.fault.NodeMapException;
import com.clustercontrol.fault.SnmpResponseError;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.nodemap.bean.Association;
import com.clustercontrol.nodemap.session.NodeMapControllerBean;
import com.clustercontrol.performance.session.PerformanceCollectMasterControllerBean;
import com.clustercontrol.repository.bean.AgentStatusInfo;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityTreeItem;
import com.clustercontrol.repository.bean.NodeConfigFilterInfo;
import com.clustercontrol.repository.bean.NodeInfoDeviceSearch;
import com.clustercontrol.repository.bean.RepositoryTableInfo;
import com.clustercontrol.repository.entity.CollectorPlatformMstData;
import com.clustercontrol.repository.entity.CollectorSubPlatformMstData;
import com.clustercontrol.repository.factory.NodeSearcher;
import com.clustercontrol.repository.factory.SearchNodeBySNMP;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.NodeConfigSettingInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.repository.session.NodeConfigSettingControllerBean;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.repository.util.RepositoryUtil;
import com.clustercontrol.rest.annotation.RestLog;
import com.clustercontrol.rest.annotation.RestLog.LogAction;
import com.clustercontrol.rest.annotation.RestLog.LogTarget;
import com.clustercontrol.rest.annotation.RestLog.LogType;
import com.clustercontrol.rest.annotation.RestLogFunc;
import com.clustercontrol.rest.annotation.RestSystemAdminPrivilege;
import com.clustercontrol.rest.annotation.RestLogFunc.LogFuncName;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.ArrayTypeParam;
import com.clustercontrol.rest.annotation.cmdtool.IgnoreCommandline;
import com.clustercontrol.rest.annotation.cmdtool.IgnoreReference;
import com.clustercontrol.rest.endpoint.cloud.RestSessionScope;
import com.clustercontrol.rest.endpoint.repository.dto.AddCollectPlatformMasterRequest;
import com.clustercontrol.rest.endpoint.repository.dto.AddCollectSubPlatformMasterRequest;
import com.clustercontrol.rest.endpoint.repository.dto.AddFilterScopeRequest;
import com.clustercontrol.rest.endpoint.repository.dto.AddFilterScopeResponse;
import com.clustercontrol.rest.endpoint.repository.dto.AddNodeAndAssignScopeFromInstanceRequest;
import com.clustercontrol.rest.endpoint.repository.dto.AddNodeConfigSettingInfoRequest;
import com.clustercontrol.rest.endpoint.repository.dto.AddNodeRequest;
import com.clustercontrol.rest.endpoint.repository.dto.AddScopeRequest;
import com.clustercontrol.rest.endpoint.repository.dto.AgentStatusInfoResponse;
import com.clustercontrol.rest.endpoint.repository.dto.AssignNodeScopeRequest;
import com.clustercontrol.rest.endpoint.repository.dto.CollectorPlatformInfoResponse;
import com.clustercontrol.rest.endpoint.repository.dto.CollectorSubPlatformInfoResponse;
import com.clustercontrol.rest.endpoint.repository.dto.DeviceSearchMessageInfoResponse;
import com.clustercontrol.rest.endpoint.repository.dto.FacilityInfoResponse;
import com.clustercontrol.rest.endpoint.repository.dto.FacilityInfoResponseP1;
import com.clustercontrol.rest.endpoint.repository.dto.FacilityInfoResponseP2;
import com.clustercontrol.rest.endpoint.repository.dto.FacilityPathResponse;
import com.clustercontrol.rest.endpoint.repository.dto.FacilityRelationInfoResponse;
import com.clustercontrol.rest.endpoint.repository.dto.FacilityTreeItemResponseP1;
import com.clustercontrol.rest.endpoint.repository.dto.GetAgentValidManagerFacilityIdsResponse;
import com.clustercontrol.rest.endpoint.repository.dto.GetFacilityTreeResponse;
import com.clustercontrol.rest.endpoint.repository.dto.GetFilterNodeListRequest;
import com.clustercontrol.rest.endpoint.repository.dto.GetNodeFullByTargetDatetimeRequest;
import com.clustercontrol.rest.endpoint.repository.dto.GetNodeListRequest;
import com.clustercontrol.rest.endpoint.repository.dto.GetNodeListResponse;
import com.clustercontrol.rest.endpoint.repository.dto.GetNodesBySNMPRequest;
import com.clustercontrol.rest.endpoint.repository.dto.IsNodeResponse;
import com.clustercontrol.rest.endpoint.repository.dto.MapAssociationInfoResponse;
import com.clustercontrol.rest.endpoint.repository.dto.ModifyNodeConfigSettingInfoRequest;
import com.clustercontrol.rest.endpoint.repository.dto.ModifyNodeRequest;
import com.clustercontrol.rest.endpoint.repository.dto.ModifyScopeRequest;
import com.clustercontrol.rest.endpoint.repository.dto.NodeConfigFilterInfoRequest;
import com.clustercontrol.rest.endpoint.repository.dto.NodeConfigSettingInfoResponse;
import com.clustercontrol.rest.endpoint.repository.dto.NodeInfoDeviceSearchResponse;
import com.clustercontrol.rest.endpoint.repository.dto.NodeInfoResponse;
import com.clustercontrol.rest.endpoint.repository.dto.NodeInfoResponseP1;
import com.clustercontrol.rest.endpoint.repository.dto.NodeInfoResponseP2;
import com.clustercontrol.rest.endpoint.repository.dto.OperationAgentRequest;
import com.clustercontrol.rest.endpoint.repository.dto.OperationAgentResponse;
import com.clustercontrol.rest.endpoint.repository.dto.PingResultResponse;
import com.clustercontrol.rest.endpoint.repository.dto.ReleaseNodeScopeRequest;
import com.clustercontrol.rest.endpoint.repository.dto.ReplaceNodeVariableRequest;
import com.clustercontrol.rest.endpoint.repository.dto.ReplaceNodeVariableResponse;
import com.clustercontrol.rest.endpoint.repository.dto.RepositoryTableInfoResponse;
import com.clustercontrol.rest.endpoint.repository.dto.RunCollectNodeConfigResponse;
import com.clustercontrol.rest.endpoint.repository.dto.ScopeInfoResponseP1;
import com.clustercontrol.rest.endpoint.repository.dto.SearchNodesBySNMPRequest;
import com.clustercontrol.rest.endpoint.repository.dto.SetStatusNodeConfigSettingRequest;
import com.clustercontrol.rest.endpoint.repository.dto.SetValidRequest;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.AgentUpdateStatusEnum;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.Level;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonConverter;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.StringBinder;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.InstanceEntity;
import com.clustercontrol.xcloud.util.FacilityIdUtil;
import com.clustercontrol.xcloud.util.RepositoryControllerBeanWrapper;
import com.clustercontrol.xcloud.validation.CommonValidatorEx;

@Path("/repository")
@RestLogFunc(name = LogFuncName.Repository)
public class RepositoryRestEndpoints {

	private static Log m_log = LogFactory.getLog(RepositoryRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "repository";

	/**
	 * ファシリティツリーの取得を行うAPI
	 */
	@GET
	@Path("/facility_tree")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetFacilityTree")
	@RestLog(action = LogAction.Get, target = LogTarget.Facility, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetFacilityTreeResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFacilityTree(@QueryParam(value = "ownerRoleId") String ownerRoleId, @QueryParam(value = "size") String sizeStr, @Context Request request,
			@Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {

		m_log.debug("call getFacilityTree()");
		Integer size = RestCommonConverter.convertInteger(MessageConstant.SIZE.getMessage(), sizeStr, false, 1, null);

		// カレントユーザがオーナーロールに所属しているかチェックする
		CommonValidator.validateCurrentUserBelongRole(ownerRoleId);
		
		FacilityTreeItem facilityTreeItem = new RepositoryControllerBean().getFacilityTree(ownerRoleId,
				Locale.getDefault());

		GetFacilityTreeResponse dtoRes = new GetFacilityTreeResponse();
		dtoRes.setData(new FacilityInfoResponseP2());
		dtoRes.setChildren(new ArrayList<FacilityTreeItemResponseP1>());
		RestBeanUtil.convertBeanNoInvalid(facilityTreeItem.getData(), dtoRes.getData());
		if (facilityTreeItem.getChildren() != null) {
			int recCount = 0;
			for (FacilityTreeItem infoChild : facilityTreeItem.getChildren()) {
				FacilityTreeItemResponseP1 dtoRec = new FacilityTreeItemResponseP1();
				RestBeanUtil.convertBeanNoInvalid(infoChild, dtoRec);
				dtoRes.getChildren().add(dtoRec);
				recCount++;
				if (size != null && recCount >= size) {
					break;
				}
			}
			dtoRes.setChildrenTotal(facilityTreeItem.getChildren().size());
		}
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 指定されたスコープ配下のファシリティツリーの取得を行うAPI
	 */
	@GET
	@Path("/facility_tree/{targetFacilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetExecTargetFacilityTreeByFacilityId")
	@RestLog(action = LogAction.Get, target = LogTarget.Facility, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FacilityTreeItemResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExecTargetFacilityTreeByFacilityId(
			@PathParam(value = "targetFacilityId") String targetFacilityId,
			@QueryParam(value = "ownerRoleId") String ownerRoleId, @Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {

		m_log.debug("call getExecTargetFacilityTreeByFacilityId()");

		// カレントユーザがオーナーロールに所属しているかチェックする
		CommonValidator.validateCurrentUserBelongRole(ownerRoleId);
		
		FacilityTreeItem facilityTreeItem = new RepositoryControllerBean().getExecTargetFacilityTree(targetFacilityId,
				ownerRoleId, Locale.getDefault());

		FacilityTreeItemResponseP1 dtoRes = new FacilityTreeItemResponseP1();
		RestBeanUtil.convertBeanNoInvalid(facilityTreeItem, dtoRes);
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ノード情報を含むファシリティツリーの取得を行うAPI
	 */
	@GET
	@Path("/facility_nodeTree")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetNodeFacilityTree")
	@RestLog(action = LogAction.Get, target = LogTarget.Facility, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FacilityTreeItemResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNodeFacilityTree(@QueryParam(value = "ownerRoleId") String ownerRoleId, @Context Request request,
			@Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {

		m_log.debug("call getNodeFacilityTree()");

		// カレントユーザがオーナーロールに所属しているかチェックする
		CommonValidator.validateCurrentUserBelongRole(ownerRoleId);
		
		FacilityTreeItem facilityTreeItem = new RepositoryControllerBean().getNodeFacilityTree(Locale.getDefault(),
				ownerRoleId);

		FacilityTreeItemResponseP1 dtoRes = new FacilityTreeItemResponseP1();
		RestBeanUtil.convertBeanNoInvalid(facilityTreeItem, dtoRes);
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 指定された条件に一致するノードの取得を行うAPI
	 */
	@POST
	@Path("/node_withoutNodeConfigInfo_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetFilterNodeList")
	@RestLog(action = LogAction.Get, target = LogTarget.Node, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NodeInfoResponseP2.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getFilterNodeList(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "getFilterNodeListBody", content = @Content(schema = @Schema(implementation = GetFilterNodeListRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {

		m_log.debug("call getFilterNodeList()");

		GetFilterNodeListRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				GetFilterNodeListRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		NodeInfo infoReq = new NodeInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);

		List<NodeInfo> infoReqList = new RepositoryControllerBean().getFilterNodeList(infoReq);

		List<NodeInfoResponseP2> dtoResList = new ArrayList<>();
		for (NodeInfo nodeInfo : infoReqList) {
			NodeInfoResponseP2 dtoRes = new NodeInfoResponseP2();
			RestBeanUtil.convertBeanNoInvalid(nodeInfo, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 指定されたスコープ配下のファシリティ情報の取得を行うAPI
	 */
	@GET
	@Path("/facility_execTargetId/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetExecTargetFacilityIdList")
	@RestLog(action = LogAction.Get, target = LogTarget.Facility, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FacilityInfoResponseP1.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExecTargetFacilityIdList(@PathParam(value = "facilityId") String facilityId,
			@QueryParam(value = "ownerRoleId") String ownerRoleId, @Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {

		m_log.debug("call getExecTargetFacilityIdList()");

		// カレントユーザがオーナーロールに所属しているかチェックする
		CommonValidator.validateCurrentUserBelongRole(ownerRoleId);
		
		List<String> infoResList = new RepositoryControllerBean().getExecTargetFacilityIdList(facilityId, ownerRoleId);

		List<FacilityInfoResponseP1> dtoResList = new ArrayList<>();
		for (String id : infoResList) {
			FacilityInfoResponseP1 dtoRes = new FacilityInfoResponseP1();
			dtoRes.setFacilityId(id);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 構成情報を除いたノード情報の取得を行うAPI
	 */
	@GET
	@Path("/node_withoutNodeConfigInfo/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetNode")
	@RestLog(action = LogAction.Get, target = LogTarget.Node, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NodeInfoResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNode(@PathParam(value = "facilityId") String facilityId, @Context Request request,
			@Context UriInfo uriInfo) throws FacilityNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		m_log.debug("call getNode()");

		NodeInfo infoRes = new RepositoryControllerBean().getNode(facilityId);

		NodeInfoResponseP1 dtoRes = toNodeInfoResponseP1(infoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 構成情報を含むノード情報の取得を行うAPI
	 */
	@GET
	@Path("/node/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetNodeFull")
	@RestLog(action = LogAction.Get, target = LogTarget.Node, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NodeInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNodeFull(@PathParam(value = "facilityId") String facilityId, @Context Request request,
			@Context UriInfo uriInfo) throws FacilityNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		m_log.debug("call getNodeFull()");

		NodeInfo infoRes = new RepositoryControllerBean().getNodeFull(facilityId);

		NodeInfoResponse dtoRes = toNodeInfoResponse(infoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 指定された時点の構成情報を含むノード情報の取得を行うAPI
	 */
	@POST
	@Path("/node_search/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetNodeFullByTargetDatetime")
	@RestLog(action = LogAction.Get, target = LogTarget.Node, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NodeInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getNodeFullByTargetDatetime(@PathParam(value = "facilityId") String facilityId,
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "getNodeFullByTargetDatetimeBody", content = @Content(schema = @Schema(implementation = GetNodeFullByTargetDatetimeRequest.class))) String requestBody)
			throws FacilityNotFound, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {

		m_log.debug("call getNodeFullByTargetDatetime()");

		GetNodeFullByTargetDatetimeRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				GetNodeFullByTargetDatetimeRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		Long targetDatetime = RestCommonConverter.convertDTStringToHinemosTime(dtoReq.getTargetDatetime(), MessageConstant.TARGET_DATETIME.getMessage());

		NodeInfo nodeFilterInfo = new NodeInfo();
		nodeFilterInfo.setNodeConfigFilterList(new ArrayList<>());
		for (NodeConfigFilterInfoRequest dtoChildReq :  dtoReq.getNodeConfigFilterList()) {
			NodeConfigFilterInfo infoChildReq = new NodeConfigFilterInfo();
			RestBeanUtil.convertBean(dtoChildReq, infoChildReq);
			nodeFilterInfo.getNodeConfigFilterList().add(infoChildReq);
		}

		NodeInfo infoRes = new RepositoryControllerBean().getNodeFull(facilityId, targetDatetime, nodeFilterInfo);

		NodeInfoResponse dtoRes = toNodeInfoResponse(infoRes);

		RestLanguageConverter.convertMessages(dtoRes);
		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ファシリティIDのファシリティパスの取得を行うAPI
	 */
	@GET
	@Path("/facility_path/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetFacilityPath")
	@RestLog(action = LogAction.Get, target = LogTarget.Facility, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FacilityPathResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFacilityPath(@PathParam(value = "facilityId") String facilityId, @Context Request request,
			@QueryParam(value = "parentFacilityId") String parentFacilityId, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {

		m_log.debug("call getFacilityPath()");

		String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, parentFacilityId);

		FacilityPathResponse dtoRes = new FacilityPathResponse();
		dtoRes.setFacilityPath(facilityPath);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * デバイスサーチによるノードプロパティ取得を行うAPI
	 */
	@POST
	@Path("/snmp")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetNodePropertyBySNMP")
	@RestLog(action = LogAction.Get, target = LogTarget.Snmp, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NodeInfoDeviceSearchResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getNodePropertyBySNMP(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "getNodePropertyBySNMPBody", content = @Content(schema = @Schema(implementation = GetNodesBySNMPRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, SnmpResponseError, InvalidSetting {

		m_log.debug("call GetNodePropertyBySNMP()");

		GetNodesBySNMPRequest dtoReq = RestObjectMapperWrapper.convertJsonToObjectInsensitive(requestBody,
				GetNodesBySNMPRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		NodeInfoDeviceSearch infoRes = new RepositoryControllerBean().getNodePropertyBySNMP(dtoReq.getIpAddress(),
				dtoReq.getPort(), dtoReq.getCommunity(), dtoReq.getVersion().getCode(), dtoReq.getFacilityID(),
				dtoReq.getSecurityLevel(), dtoReq.getUser(), dtoReq.getAuthPass(), dtoReq.getPrivPass(),
				dtoReq.getAuthProtocol(), dtoReq.getPrivProtocol());

		NodeInfoDeviceSearchResponse dtoRes = toNodeInfoDeviceSearchResponse(infoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ノード情報の追加を行うAPI
	 */
	@POST
	@Path("/node")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddNode")
	@RestLog(action = LogAction.Add, target = LogTarget.Node, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.ADD })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NodeInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addNode(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addNodeBody", content = @Content(schema = @Schema(implementation = AddNodeRequest.class))) String requestBody)
			throws FacilityDuplicate, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {

		m_log.debug("call addNode()");

		AddNodeRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddNodeRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		NodeInfo infoReq = new NodeInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setFacilityType(FacilityConstant.TYPE_NODE);

		long startTime = HinemosTime.currentTimeMillis();
		NodeInfo infoRes = new RepositoryControllerBean().addNode(infoReq);
		m_log.info(String.format("addNode: %dms", HinemosTime.currentTimeMillis() - startTime));

		NodeInfoResponse dtoRes = toNodeInfoResponse(infoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ノード情報の更新を行うAPI
	 */
	@PUT
	@Path("/node/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyNode")
	@RestLog(action = LogAction.Modify, target = LogTarget.Node, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NodeInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyNode(@PathParam(value = "facilityId") String facilityId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyNodeBody", content = @Content(schema = @Schema(implementation = ModifyNodeRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting, FacilityNotFound {

		m_log.debug("call modifyNode()");

		ModifyNodeRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyNodeRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		NodeInfo infoReq = new NodeInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setFacilityId(facilityId);
		infoReq.setFacilityType(FacilityConstant.TYPE_NODE);

		NodeInfo infoRes = new RepositoryControllerBean().modifyNode(infoReq);

		NodeInfoResponse dtoRes = toNodeInfoResponse(infoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ノード情報の削除を行うAPI
	 */
	@DELETE
	@Path("/node")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteNode")
	@RestLog(action = LogAction.Delete, target = LogTarget.Node, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NodeInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteNode(@ArrayTypeParam @QueryParam(value = "facilityIds") String facilityIds,
			@Context Request request, @Context UriInfo uriInfo)
			throws UsedFacility, InvalidUserPass, InvalidRole, FacilityNotFound, HinemosUnknown {
		m_log.debug("call deleteNode()");

		String[] facilityIdArray = new String[0];
		if(facilityIds != null && !facilityIds.isEmpty()) {
			facilityIdArray = facilityIds.split(",");
		}
		
		List<NodeInfo> infoResList = new RepositoryControllerBean().deleteNode(facilityIdArray);

		List<NodeInfoResponse> dtoResList = new ArrayList<>();
		for (NodeInfo infoRes : infoResList) {
			NodeInfoResponse dtoRes = toNodeInfoResponse(infoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 指定されたスコープ配下のスコープとノードの一覧の取得を行うAPI
	 */
	@GET
	@Path("/facility")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetFacilityList")
	@RestLog(action = LogAction.Get, target = LogTarget.Facility, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FacilityInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFacilityList(@QueryParam(value = "parentFacilityId") String parentFacilityId,
			@Context Request request, @Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {

		m_log.debug("call getFacilityList()");

		List<FacilityInfo> infoResList = new RepositoryControllerBean().getFacilityList(parentFacilityId);

		List<FacilityInfoResponse> dtoResList = new ArrayList<>();
		for (FacilityInfo infoRes : infoResList) {
			FacilityInfoResponse dtoRes = new FacilityInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		dtoResList.sort(Comparator.comparing(FacilityInfoResponse::getFacilityId));

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * スコープ情報の取得を行うAPI
	 */
	@GET
	@Path("/scope/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetScope")
	@RestLog(action = LogAction.Get, target = LogTarget.Scope, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ScopeInfoResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScope(@PathParam(value = "facilityId") String facilityId, @Context Request request,
			@Context UriInfo uriInfo) throws FacilityNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {

		m_log.debug("call getScope()");

		ScopeInfo infoRes = new RepositoryControllerBean().getScope(facilityId);

		ScopeInfoResponseP1 dtoRes = new ScopeInfoResponseP1();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * スコープ情報の初期値の取得を行うAPI
	 */
	@GET
	@Path("/scope_default")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetScopeDefault")
	@RestLog(action = LogAction.Get, target = LogTarget.Scope, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ScopeInfoResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScopeDefault(@Context Request request, @Context UriInfo uriInfo)
			throws FacilityNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {

		m_log.debug("call getScopeDefault()");

		ScopeInfo infoRes = new RepositoryControllerBean().getScope(null);

		ScopeInfoResponseP1 dtoRes = new ScopeInfoResponseP1();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * スコープ情報の追加を行うAPI
	 */
	@POST
	@Path("/scope")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddScope")
	@RestLog(action = LogAction.Add, target = LogTarget.Scope, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.ADD })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ScopeInfoResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addScope(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addScopeBody", content = @Content(schema = @Schema(implementation = AddScopeRequest.class))) String requestBody)
			throws FacilityDuplicate, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {

		m_log.debug("call addScope()");

		AddScopeRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddScopeRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();
		try {
			if(dtoReq.getParentFacilityId() != null && !( dtoReq.getParentFacilityId().isEmpty())){
				new RepositoryControllerBean().checkIsBuildInScope(dtoReq.getParentFacilityId());
			}
		} catch (FacilityNotFound e) {
			InvalidSetting e1 = new InvalidSetting("Scope does not exist! facilityId = " + dtoReq.getParentFacilityId());
			m_log.info("addScope() : "
				+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
			throw e1;
		} catch (HinemosUnknown e) {
			InvalidSetting e1 = new InvalidSetting(e.getMessage());
			m_log.info("addScope() : "
				+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
			throw e1;
		}

		ScopeInfo infoReq = new ScopeInfo();
		RestBeanUtil.convertBean(dtoReq.getScopeInfo(), infoReq);
		infoReq.setFacilityType(FacilityConstant.TYPE_SCOPE);

		ScopeInfo infoRes = new RepositoryControllerBean().addScope(dtoReq.getParentFacilityId(), infoReq);

		ScopeInfoResponseP1 dtoRes = new ScopeInfoResponseP1();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * スコープ情報の更新を行うAPI
	 */
	@PUT
	@Path("/scope/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyScope")
	@RestLog(action = LogAction.Modify, target = LogTarget.Scope, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ScopeInfoResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyScope(@PathParam(value = "facilityId") String facilityId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyScopeBody", content = @Content(schema = @Schema(implementation = ModifyScopeRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, FacilityNotFound, InvalidSetting {

		m_log.debug("call modifyScope()");

		ModifyScopeRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyScopeRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		ScopeInfo infoReq = new ScopeInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setFacilityId(facilityId);
		infoReq.setFacilityType(FacilityConstant.TYPE_SCOPE);

		ScopeInfo infoRes = new RepositoryControllerBean().modifyScope(infoReq);

		ScopeInfoResponseP1 dtoRes = new ScopeInfoResponseP1();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * スコープ情報の削除を行うAPI
	 */
	@DELETE
	@Path("/scope")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteScope")
	@RestLog(action = LogAction.Delete, target = LogTarget.Scope, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ScopeInfoResponseP1.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteScope(@ArrayTypeParam @QueryParam(value = "facilityIds") String facilityIds,
			@Context Request request, @Context UriInfo uriInfo)
			throws UsedFacility, InvalidUserPass, InvalidRole, FacilityNotFound, HinemosUnknown {
		m_log.debug("call deleteScope()");

		String[] facilityIdArray = new String[0];
		if(facilityIds != null && !facilityIds.isEmpty()) {
			facilityIdArray = facilityIds.split(",");
		}
		
		List<ScopeInfo> infoResList = new RepositoryControllerBean().deleteScope(facilityIdArray);

		List<ScopeInfoResponseP1> dtoResList = new ArrayList<>();
		for (ScopeInfo infoRes : infoResList) {
			ScopeInfoResponseP1 dtoRes = new ScopeInfoResponseP1();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 指定されたスコープ配下の構成情報を除いたノード情報の取得を行うAPI
	 */
	@GET
	@Path("/node_withoutNodeConfigInfo")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetNodeList")
	@RestLog(action = LogAction.Get, target = LogTarget.Node, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetNodeListResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNodeList(@QueryParam(value = "parentFacilityId") String parentFacilityId, @QueryParam(value = "size") String sizeStr,
			@QueryParam(value = "level") Level level, @Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {

		m_log.debug("call getNodeList()");
		Integer size = RestCommonConverter.convertInteger(MessageConstant.SIZE.getMessage(), sizeStr, false, 1, null);

		List<NodeInfo> infoResList;

		if (parentFacilityId != null && level != null) {
			infoResList = new RepositoryControllerBean().getNodeList(parentFacilityId, level.getCode());
		} else {
			infoResList = new RepositoryControllerBean().getNodeList();
		}

		List<NodeInfoResponseP2> dtoResList = new ArrayList<>();
		int recCount = 0;
		for (NodeInfo infoRec : infoResList) {
			NodeInfoResponseP2 dtoRec = new NodeInfoResponseP2();
			RestBeanUtil.convertBeanNoInvalid(infoRec, dtoRec);
			dtoResList.add(dtoRec);
			recCount++;
			if (size != null && recCount >= size) {
				break;
			}
		}

		RestLanguageConverter.convertMessages(dtoResList);
		GetNodeListResponse dtoRes = new GetNodeListResponse();
		dtoRes.setNodeInfoList(dtoResList);
		dtoRes.setTotal(infoResList.size());

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 指定されたノードが割り当てられているスコープの取得を行うAPI
	 */
	@GET
	@Path("/facility_scopePath/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetNodeScopeList")
	@RestLog(action = LogAction.Get, target = LogTarget.Facility, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FacilityInfoResponseP1.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNodeScopeList(@PathParam(value = "facilityId") String facilityId, @Context Request request,
			@Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown, FacilityNotFound {

		m_log.debug("call getNodeScopeList()");

		List<String> infoResList = new RepositoryControllerBean().getNodeScopeList(facilityId);

		List<FacilityInfoResponseP1> dtoResList = new ArrayList<>();
		for (String infoRes : infoResList) {
			FacilityInfoResponseP1 dtoRes = new FacilityInfoResponseP1();
			dtoRes.setFacilityId(infoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 指定されたスコープとその配下のスコープ、ノードの取得を行うAPI
	 */
	@GET
	@Path("/facility_id")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetFacilityIdList")
	@RestLog(action = LogAction.Get, target = LogTarget.Facility, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FacilityInfoResponseP1.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFacilityIdList(@QueryParam(value = "parentFacilityId") String parentFacilityId,
			@QueryParam(value = "level") Level level, @Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {

		m_log.debug("call getFacilityIdList()");
		
		if (level == null) {
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_DISPLAY_REPOSITORY_LEVEL.getMessage());
		}

		List<String> infoResList = new RepositoryControllerBean().getFacilityIdList(parentFacilityId, level.getCode());

		List<FacilityInfoResponseP1> dtoResList = new ArrayList<>();
		for (String infoRes : infoResList) {
			FacilityInfoResponseP1 dtoRes = new FacilityInfoResponseP1();
			dtoRes.setFacilityId(infoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 指定されたスコープ配下のノードのファシリティID取得を行うAPI
	 */
	@GET
	@Path("/facility_nodeFacilityId")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetNodeFacilityIdList")
	@RestLog(action = LogAction.Get, target = LogTarget.Facility, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FacilityInfoResponseP1.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNodeFacilityIdList(@QueryParam(value = "parentFacilityId") String parentFacilityId,
			@QueryParam(value = "ownerRoleId") String ownerRoleId, @QueryParam(value = "level") Level level,
			@Context Request request, @Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {

		m_log.debug("call getNodeFacilityIdList()");

		if (level == null) {
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_DISPLAY_REPOSITORY_LEVEL.getMessage());
		}
		
		// カレントユーザがオーナーロールに所属しているかチェックする
		CommonValidator.validateCurrentUserBelongRole(ownerRoleId);

		List<String> infoResList = new RepositoryControllerBean().getNodeFacilityIdList(parentFacilityId, ownerRoleId,
				level.getCode());

		List<FacilityInfoResponseP1> dtoResList = new ArrayList<>();
		for (String infoRes : infoResList) {
			FacilityInfoResponseP1 dtoRes = new FacilityInfoResponseP1();
			dtoRes.setFacilityId(infoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * スコープへのノード割当を行うAPI
	 */
	@PUT
	@Path("/facilityRelation/{parentFacilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AssignNodeScope")
	@RestLog(action = LogAction.Modify, target = LogTarget.FacilityRelation, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FacilityRelationInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response assignNodeScope(@PathParam(value = "parentFacilityId") String parentFacilityId,
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "assignNodeScopeBody", content = @Content(schema = @Schema(implementation = AssignNodeScopeRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {

		m_log.debug("call assignNodeScope()");

		AssignNodeScopeRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AssignNodeScopeRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		List<String> facilityIdList = dtoReq.getFacilityIdList();
		String[] facilityIds = facilityIdList.toArray(new String[facilityIdList.size()]);

		new RepositoryControllerBean().assignNodeScope(parentFacilityId, facilityIds);
		List<FacilityInfo> infoResList = new RepositoryControllerBean().getFacilityList(parentFacilityId);


		List<FacilityRelationInfoResponse> dtoResList = new ArrayList<>();
		for (FacilityInfo infoRes : infoResList) {
			FacilityRelationInfoResponse dtoRes = new FacilityRelationInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		dtoResList.sort(Comparator.comparing(FacilityRelationInfoResponse::getFacilityId));

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * スコープからのノード割当解除を行うAPI
	 */
	@PUT
	@Path("/facilityRelation_release/{parentFacilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ReleaseNodeScope")
	@RestLog(action = LogAction.Modify, target = LogTarget.FacilityRelation, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FacilityRelationInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response releaseNodeScope(@PathParam(value = "parentFacilityId") String parentFacilityId,
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "releaseNodeScopeBody", content = @Content(schema = @Schema(implementation = ReleaseNodeScopeRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {

		m_log.debug("call releaseNodeScope()");

		ReleaseNodeScopeRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ReleaseNodeScopeRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		List<String> facilityIdList = dtoReq.getFacilityIdList();
		String[] facilityIds = facilityIdList.toArray(new String[facilityIdList.size()]);

		new RepositoryControllerBean().releaseNodeScope(parentFacilityId, facilityIds);
		List<FacilityInfo> infoResList = new RepositoryControllerBean().getFacilityList(parentFacilityId);

		List<FacilityRelationInfoResponse> dtoResList = new ArrayList<>();
		for (FacilityInfo infoRes : infoResList) {
			FacilityRelationInfoResponse dtoRes = new FacilityRelationInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		dtoResList.sort(Comparator.comparing(FacilityRelationInfoResponse::getFacilityId));

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * ファシリティIDがノードであるか否かを返すAPI
	 */
	@POST
	@Path("/facility_isNode/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "IsNode")
	@RestLog(action = LogAction.Get, target = LogTarget.Facility, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = IsNodeResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response isNode(@PathParam(value = "facilityId") String facilityId, @Context Request request,
			@Context UriInfo uriInfo)
			throws FacilityNotFound, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {

		m_log.debug("call isNode()");

		boolean infoRes = new RepositoryControllerBean().isNode(facilityId);

		IsNodeResponse dtoRes = new IsNodeResponse();
		dtoRes.setFacilityId(facilityId);
		dtoRes.setIsNode(infoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ノードへの設定可能なプラットフォーム一覧の取得を行うAPI
	 */
	@GET
	@Path("/platform")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetPlatformList")
	@RestLog(action = LogAction.Get, target = LogTarget.Platform, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RepositoryTableInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPlatformList(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {

		m_log.debug("call getPlatformList()");

		List<RepositoryTableInfo> infoResList = new RepositoryControllerBean().getPlatformList();

		List<RepositoryTableInfoResponse> dtoResList = new ArrayList<>();
		for (RepositoryTableInfo infoRes : infoResList) {
			RepositoryTableInfoResponse dtoRes = new RepositoryTableInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * ノードへの設定可能なサブプラットフォーム一覧の取得を行うAPI
	 */
	@GET
	@Path("/subPlatform")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCollectorSubPlatformTableInfoList")
	@RestLog(action = LogAction.Get, target = LogTarget.SubPlatform, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RepositoryTableInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollectorSubPlatformTableInfoList(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {

		m_log.debug("call getCollectorSubPlatformTableInfoList()");

		List<RepositoryTableInfo> infoResList = new RepositoryControllerBean().getCollectorSubPlatformTableInfoList();

		List<RepositoryTableInfoResponse> dtoResList = new ArrayList<>();
		for (RepositoryTableInfo infoRes : infoResList) {
			RepositoryTableInfoResponse dtoRes = new RepositoryTableInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * ノードの管理対象／対象外の設定を行うAPI
	 */
	@PUT
	@Path("/node_valid/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SetValid")
	@RestLog(action = LogAction.Modify, target = LogTarget.Node, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NodeInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setValid(@PathParam(value = "facilityId") String facilityId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "setValidBody", content = @Content(schema = @Schema(implementation = SetValidRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, FacilityNotFound, InvalidSetting {

		m_log.debug("call setValid()");

		SetValidRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, SetValidRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		NodeInfo nodeInfo = new RepositoryControllerBean().getNodeFull(facilityId);
		nodeInfo.setValid(dtoReq.getFlg());
		NodeInfo infoRes = new RepositoryControllerBean().modifyNode(nodeInfo);

		NodeInfoResponse dtoRes = toNodeInfoResponse(infoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * リポジトリ情報の最終更新日時の取得を行うAPI
	 */
	@GET
	@Path("/lastUpdateTime")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetLastUpdate")
	@RestLog(action = LogAction.Get, target = LogTarget.LastUpdateTime, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Long.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLastUpdate(@Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {

		m_log.debug("call getLastUpdate()");

		Long res = new RepositoryControllerBean().getLastUpdate().getTime();

		return Response.status(Status.OK).entity(res).build();
	}

	/**
	 * エージェントのステータス情報の取得を行うAPI
	 */
	@GET
	@Path("/agentStatus")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetAgentStatusList")
	@RestLog(action = LogAction.Get, target = LogTarget.AgentStatus, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = AgentStatusInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAgentStatusList(@Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {

		m_log.debug("call getAgentStatusList()");

		List<AgentStatusInfo> infoResList = new RepositoryControllerBean().getAgentStatusList();

		List<AgentStatusInfoResponse> dtoResList = new ArrayList<>();
		for (AgentStatusInfo infoRes : infoResList) {
			AgentStatusInfoResponse dtoRes = new AgentStatusInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			try {
				dtoRes.setUpdateStatusCode(AgentUpdateStatusEnum.valueOf(infoRes.getUpdateStatus().name()));
			} catch (IllegalArgumentException e) {
				// 何もしない
			}
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * エージェントへの操作を指示するAPI
	 */
	@POST
	@Path("/agent_operation")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "OperationAgent")
	@RestLog(action = LogAction.Operation, target = LogTarget.Agent, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.EXEC })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = OperationAgentResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response operationAgent(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "operationAgentBody", content = @Content(schema = @Schema(implementation = OperationAgentRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {

		m_log.debug("call operationAgent()");

		OperationAgentRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				OperationAgentRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		List<OperationAgentResponse> dtoResList = new RepositoryControllerBean().restartAgent(dtoReq.getFacilityIds(),
				dtoReq.getAgentCommand().getCode());

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 指定された文字列内に存在するノード変数を置換した文字列を返すAPI
	 */
	@POST
	@Path("/node_nodeVar_replace")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ReplaceNodeVariable")
	@RestLog(action = LogAction.Modify, target = LogTarget.Node, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReplaceNodeVariableResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response replaceNodeVariable(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "replaceNodeVariableBody", content = @Content(schema = @Schema(implementation = ReplaceNodeVariableRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {

		m_log.debug("call replaceNodeVariable()");

		ReplaceNodeVariableRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ReplaceNodeVariableRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		String replaceObject = dtoReq.getReplaceObject();
		String ret = replaceObject;
		String facilityId = dtoReq.getFacilityId();

		Map<String, NodeInfo> nodeInfo = new HashMap<String, NodeInfo>();
		try {
			synchronized (this) {
				nodeInfo.put(facilityId, new RepositoryControllerBean().getNode(facilityId));
			}
		} catch (FacilityNotFound e) {
			// 何もしない
		}

		if (nodeInfo != null && nodeInfo.containsKey(facilityId)) {
			int maxReplaceWord = HinemosPropertyCommon.replace_param_max.getIntegerValue().intValue();
			ArrayList<String> inKeyList = StringBinder.getKeyList(replaceObject, maxReplaceWord);
			Map<String, String> nodeParameter = RepositoryUtil.createNodeParameter(nodeInfo.get(facilityId), inKeyList);
			StringBinder strbinder = new StringBinder(nodeParameter);
			ret = strbinder.bindParam(replaceObject);

			m_log.debug("replaceNodeVariable() after : " + ret);
		}

		ReplaceNodeVariableResponse dtoRes = new ReplaceNodeVariableResponse();
		dtoRes.setReplaceStr(ret);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * SNMPを利用してノード情報を取得するAPI
	 */
	@POST
	@Path("/snmp_multiNode")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SearchNodesBySNMP")
	@RestLog(action = LogAction.Get, target = LogTarget.Snmp, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.ADD })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NodeInfoDeviceSearchResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response searchNodesBySNMP(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "searchNodesBySNMPBody", content = @Content(schema = @Schema(implementation = SearchNodesBySNMPRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, FacilityDuplicate, InvalidSetting {

		m_log.debug("call searchNodesBySNMP()");

		SearchNodesBySNMPRequest dtoReq = RestObjectMapperWrapper.convertJsonToObjectInsensitive(requestBody,
				SearchNodesBySNMPRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		List<NodeInfoDeviceSearch> infoResList = new NodeSearcher().searchNode(dtoReq.getOwnerRoleId(),
				dtoReq.getIpAddressFrom(), dtoReq.getIpAddressTo(), dtoReq.getPort(), dtoReq.getCommunity(),
				dtoReq.getVersion().getCode(), dtoReq.getFacilityID(), dtoReq.getSecurityLevel(), dtoReq.getUser(),
				dtoReq.getAuthPass(), dtoReq.getPrivPass(), dtoReq.getAuthProtocol(), dtoReq.getPrivProtocol());

		List<NodeInfoDeviceSearchResponse> dtoResList = new ArrayList<>();
		for (NodeInfoDeviceSearch infoRes : infoResList) {
			NodeInfoDeviceSearchResponse dtoRes = toNodeInfoDeviceSearchResponse(infoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 構成情報取得設定の追加を行うAPI
	 */
	@POST
	@Path("/nodeConfig")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddNodeConfigSettingInfo")
	@RestLog(action = LogAction.Add, target = LogTarget.NodeConfig, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.ADD })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NodeConfigSettingInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addNodeConfigSettingInfo(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addNodeConfigSettingInfoBody", content = @Content(schema = @Schema(implementation = AddNodeConfigSettingInfoRequest.class))) String requestBody)
			throws NodeConfigSettingDuplicate, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {

		m_log.debug("call addNodeConfigSettingInfo()");

		AddNodeConfigSettingInfoRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddNodeConfigSettingInfoRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		NodeConfigSettingInfo infoReq = new NodeConfigSettingInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		NodeConfigSettingInfo infoRes = new NodeConfigSettingControllerBean().addNodeConfigSettingInfo(infoReq, false);

		NodeConfigSettingInfoResponse dtoRes = new NodeConfigSettingInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
		if (dtoRes.getNodeConfigSettingItemList() != null) {
			dtoRes.getNodeConfigSettingItemList().sort(Comparator.comparing(item -> item.getSettingItemId().name()));
		}

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 構成情報取得設定の更新を行うAPI
	 */
	@PUT
	@Path("/nodeConfig/{settingId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyNodeConfigSettingInfo")
	@RestLog(action = LogAction.Modify, target = LogTarget.NodeConfig, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NodeConfigSettingInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyNodeConfigSettingInfo(@PathParam(value = "settingId") String settingId,
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "modifyNodeConfigSettingInfoBody", content = @Content(schema = @Schema(implementation = ModifyNodeConfigSettingInfoRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting, NodeConfigSettingNotFound {

		m_log.debug("call modifyNodeConfigSettingInfo()");

		ModifyNodeConfigSettingInfoRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyNodeConfigSettingInfoRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		NodeConfigSettingInfo infoReq = new NodeConfigSettingInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setSettingId(settingId);

		NodeConfigSettingInfo infoRes = new NodeConfigSettingControllerBean().modifyNodeConfigSettingInfo(infoReq, false);

		NodeConfigSettingInfoResponse dtoRes = new NodeConfigSettingInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
		if (dtoRes.getNodeConfigSettingItemList() != null) {
			dtoRes.getNodeConfigSettingItemList().sort(Comparator.comparing(item -> item.getSettingItemId().name()));
		}

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 構成情報取得設定の削除を行うAPI
	 */
	@DELETE
	@Path("/nodeConfig")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteNodeConfigSettingInfo")
	@RestLog(action = LogAction.Delete, target = LogTarget.NodeConfig, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NodeConfigSettingInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteNodeConfigSettingInfo(@ArrayTypeParam @QueryParam(value = "settingIds") String settingIds,
			@Context Request request, @Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown, NodeConfigSettingNotFound {
		m_log.debug("call deleteNodeConfigSettingInfo()");

		String[] settingIdArray = new String[0];
		if(settingIds != null && !settingIds.isEmpty()) {
			settingIdArray = settingIds.split(",");
		}
		
		List<NodeConfigSettingInfo> infoResList = new NodeConfigSettingControllerBean().deleteNodeConfigSettingInfo(settingIdArray);

		List<NodeConfigSettingInfoResponse> dtoResList = new ArrayList<>();
		for (NodeConfigSettingInfo infoRes : infoResList) {
			NodeConfigSettingInfoResponse dtoRes = new NodeConfigSettingInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			if (dtoRes.getNodeConfigSettingItemList() != null) {
				dtoRes.getNodeConfigSettingItemList().sort(Comparator.comparing(item -> item.getSettingItemId().name()));
			}
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 構成情報取得設定の有効／無効の設定を行うAPI
	 */
	@PUT
	@Path("/nodeConfig_valid")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SetStatusNodeConfigSetting")
	@RestLog(action = LogAction.Modify, target = LogTarget.NodeConfig, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NodeConfigSettingInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setStatusNodeConfigSetting(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "setStatusNodeConfigSettingBody", content = @Content(schema = @Schema(implementation = SetStatusNodeConfigSettingRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting, NodeConfigSettingNotFound {

		m_log.debug("call setStatusNodeConfigSetting()");

		SetStatusNodeConfigSettingRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				SetStatusNodeConfigSettingRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		List<NodeConfigSettingInfo> infoResList = new NodeConfigSettingControllerBean()
				.setStatusNodeConfigSetting(dtoReq.getSettingId(), dtoReq.isValidFlag());

		List<NodeConfigSettingInfoResponse> dtoResList = new ArrayList<>();
		for (NodeConfigSettingInfo infoRes : infoResList) {
			NodeConfigSettingInfoResponse dtoRes = new NodeConfigSettingInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			if (dtoRes.getNodeConfigSettingItemList() != null) {
				dtoRes.getNodeConfigSettingItemList().sort(Comparator.comparing(item -> item.getSettingItemId().name()));
			}
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 構成情報取得設定情報の取得を行うAPI
	 */
	@GET
	@Path("/nodeConfig/{settingId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetNodeConfigSettingInfo")
	@RestLog(action = LogAction.Get, target = LogTarget.NodeConfig, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NodeConfigSettingInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNodeConfigSettingInfo(@PathParam(value = "settingId") String settingId, @Context Request request,
			@Context UriInfo uriInfo) throws NodeConfigSettingNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {

		m_log.debug("call getNodeConfigSettingInfo()");

		NodeConfigSettingInfo infoRes = new NodeConfigSettingControllerBean().getNodeConfigSettingInfo(settingId);

		NodeConfigSettingInfoResponse dtoRes = new NodeConfigSettingInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
		if (dtoRes.getNodeConfigSettingItemList() != null) {
			dtoRes.getNodeConfigSettingItemList().sort(Comparator.comparing(item -> item.getSettingItemId().name()));
		}

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 構成情報取得設定の一覧の取得を行うAPI
	 */
	@GET
	@Path("/nodeConfig")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetNodeConfigSettingList")
	@RestLog(action = LogAction.Get, target = LogTarget.NodeConfig, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NodeConfigSettingInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNodeConfigSettingList(@Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {

		m_log.debug("call getNodeConfigSettingInfo()");

		List<NodeConfigSettingInfo> infoResList = new NodeConfigSettingControllerBean().getNodeConfigSettingList();

		List<NodeConfigSettingInfoResponse> dtoResList = new ArrayList<>();
		for (NodeConfigSettingInfo infoRes : infoResList) {
			NodeConfigSettingInfoResponse dtoRes = new NodeConfigSettingInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			if (dtoRes.getNodeConfigSettingItemList() != null) {
				dtoRes.getNodeConfigSettingItemList().sort(Comparator.comparing(item -> item.getSettingItemId().name()));
			}
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 構成情報取得の即時実行を行うAPI
	 */
	@POST
	@Path("/nodeConfig_collect/{settingId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "RunCollectNodeConfig")
	@RestLog(action = LogAction.Collect, target = LogTarget.NodeConfig, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.EXEC })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RunCollectNodeConfigResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response runCollectNodeConfig(@PathParam(value = "settingId") String settingId, @Context Request request,
			@Context UriInfo uriInfo)
			throws FacilityNotFound, InvalidUserPass, InvalidRole, NodeConfigSettingNotFound, HinemosUnknown {

		m_log.debug("call runCollectNodeConfig()");

		long startTime = HinemosTime.currentTimeMillis();
		Long loadDistributionTime = new NodeConfigSettingControllerBean().runCollectNodeConfig(settingId);
		m_log.info(String.format("runCollectNodeConfig: %dms", HinemosTime.currentTimeMillis() - startTime));

		RunCollectNodeConfigResponse dtoRes = new RunCollectNodeConfigResponse();
		dtoRes.setSettingId(settingId);
		dtoRes.setLoadDistributionTime(loadDistributionTime);
		dtoRes.setResult(true);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 構成情報検索によるノード一覧取得を行うAPI
	 */
	@POST
	@Path("/node_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SearchNode")
	@RestLog(action = LogAction.Get, target = LogTarget.Node, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NodeInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response searchNode(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "searchNodeBody", content = @Content(schema = @Schema(implementation = GetNodeListRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, InvalidSetting, HinemosDbTimeout, HinemosUnknown {

		m_log.debug("call searchNode()");

		GetNodeListRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, GetNodeListRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		NodeInfo infoReq = new NodeInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);

		List<NodeInfo> infoResList = new RepositoryControllerBean().getNodeList(dtoReq.getParentFacilityId(), 
				infoReq);

		List<NodeInfoResponse> dtoResList = new ArrayList<>();
		for (NodeInfo infoRes : infoResList) {
			NodeInfoResponse dtoRes = toNodeInfoResponse(infoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 指定されたノードにpingを実行し、結果を返すAPI
	 */
	@POST
	@Path("/facility_ping/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "Ping")
	@RestLog(action = LogAction.Ping, target = LogTarget.Facility, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PingResultResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response ping(@PathParam(value = "facilityId") String facilityId, @Context Request request,
			@Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown, NodeMapException {

		m_log.debug("call ping()");

		List<String> facilityList = new RepositoryControllerBean().getExecTargetFacilityIdList(facilityId, null);
		List<String> infoResList = new NodeMapControllerBean().pingToFacilityList(facilityList);

		List<PingResultResponse> dtoResList = new ArrayList<>();
		for (int i = 0; i < infoResList.size(); i++) {
			PingResultResponse dtoRes = new PingResultResponse();
			dtoRes.setFacilityId(facilityList.get(i));
			dtoRes.setResult(infoResList.get(i));
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 構成情報検索で取得したノードを割り当てたスコープ情報の追加を行うAPI
	 */
	@POST
	@Path("/scopeAndFacilityRelation")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddFilterScope")
	@RestLog(action = LogAction.Add, target = LogTarget.ScopeAndFacilityRelation, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = AddFilterScopeResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addFilterScope(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addFilterScopeBody", content = @Content(schema = @Schema(implementation = AddFilterScopeRequest.class))) String requestBody)
			throws InvalidUserPass, FacilityDuplicate, InvalidSetting, InvalidRole, HinemosUnknown {

		m_log.debug("call addFilterScope()");

		AddFilterScopeRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddFilterScopeRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		ScopeInfo property = new ScopeInfo();
		RestBeanUtil.convertBean(dtoReq.getProperty(), property);
		property.setFacilityType(FacilityConstant.TYPE_SCOPE);

		new RepositoryControllerBean().addFilterScope(property, dtoReq.getFacilityIdList());

		AddFilterScopeResponse dtoRes = new AddFilterScopeResponse();

		try {
			ScopeInfoResponseP1 scopeInfoRes = new ScopeInfoResponseP1();
			ScopeInfo scopeInfo = new RepositoryControllerBean().getScope(property.getFacilityId());
			RestBeanUtil.convertBeanNoInvalid(scopeInfo, scopeInfoRes);
			dtoRes.setScopeInfo(scopeInfoRes);
		} catch (Exception e) {
			throw new HinemosUnknown(e.getMessage(), e);
		}

		List<FacilityInfo> facilityInfoList = new RepositoryControllerBean().getFacilityList(property.getFacilityId());
		List<FacilityInfoResponse> facilityInfoResList = new ArrayList<>();
		for (FacilityInfo facilityInfo : facilityInfoList) {
			FacilityInfoResponse res = new FacilityInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(facilityInfo, res);
			facilityInfoResList.add(res);
		}
		facilityInfoResList.sort(Comparator.comparing(FacilityInfoResponse::getFacilityId));
		dtoRes.setFacilityInfos(facilityInfoResList.toArray(new FacilityInfoResponse[facilityInfoResList.size()]));

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ホスト名に該当するノード一覧の取得を行うAPI
	 */
	@GET
	@Path("/node_agentValidFacitlityId")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetAgentValidManagerFacilityIds")
	@RestLog(action = LogAction.Get, target = LogTarget.Node, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetAgentValidManagerFacilityIdsResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAgentValidManagerFacilityIds(@Context Request request, @Context UriInfo uriInfo)
			throws InvalidSetting, InvalidRole, HinemosUnknown {

		m_log.debug("call getAgentValidManagerFacilityIds()");

		GetAgentValidManagerFacilityIdsResponse dtoRes = new GetAgentValidManagerFacilityIdsResponse();

		String hostName = SearchNodeBySNMP.getShortName(HinemosManagerMain._hostname);

		Set<String> facilityIdSet = RepositoryControllerBeanWrapper.bean().getNodeListByHostname(hostName);
		if (facilityIdSet == null) {
			dtoRes.setFacilityIds(Collections.emptyList());
		} else {
			List<String> facilityIds = new ArrayList<>();
			for (String facilityId : facilityIdSet) {
				AgentInfo agent = AgentConnectUtil.getAgentInfo(facilityId);
				if (agent != null) {
					facilityIds.add(facilityId);
				}
			}
			dtoRes.setFacilityIds(facilityIds);
		}

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * クラウドのインストタンスからノード情報を作成し、スコープへの割当を行うAPI
	 */
	@POST
	@Path("/facilityRelation_forCloud")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddNodeAndAssignScopeFromInstance")
	@RestLog(action = LogAction.Add, target = LogTarget.FacilityRelation, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.EXEC })
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FacilityRelationInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response addNodeAndAssignScopeFromInstance(
			@RequestBody(description = "addNodeAndAssignScopeFromInstanceBody", content = @Content(schema = @Schema(implementation = AddNodeAndAssignScopeFromInstanceRequest.class))) String requestBody,			@Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, FacilityNotFound, CloudManagerException, HinemosUnknown, InvalidSetting {
		List<FacilityRelationInfoResponse> dtoResList = new ArrayList<>();

		try (RestSessionScope sessionScope = RestSessionScope.open()) {
			AddNodeAndAssignScopeFromInstanceRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
					AddNodeAndAssignScopeFromInstanceRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();
			
			String cloudScopeId = dtoReq.getCloudScopeId();
			String instanceId = dtoReq.getInstanceId();
			String locationId = dtoReq.getLocationId();
			
			// Validation
			
			CommonValidatorEx.validateId(MessageConstant.XCLOUD_CORE_CLOUDSCOPE_ID.name(), cloudScopeId, 64);
			CommonValidatorEx.validateId(MessageConstant.XCLOUD_CORE_LOCATION_ID.name(), locationId, 64);
	
			CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);
	
			InstanceEntity instance = CloudManager.singleton().getInstances(user, user.getCloudScope().getLocation(locationId))
					.assignNode(instanceId);
			
			String fid = FacilityIdUtil.getResourceId(user.getCloudScope().getPlatformId(), cloudScopeId, instance.getResourceId());
			for (FacilityInfo infoRes : FacilityTreeCache.getParentFacilityInfo(fid)) {
				FacilityRelationInfoResponse dtoRes = new FacilityRelationInfoResponse();
				RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
				dtoResList.add(dtoRes);
			}
	
			RestLanguageConverter.convertMessages(dtoResList);
		}
		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * L2のコネクション状態の取得を行うAPI
	 */
	@GET
	@Path("/snmp_l2Association/{scopeId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetL2ConnectionMap")
	@RestLog(action = LogAction.Get, target = LogTarget.Snmp, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MapAssociationInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getL2ConnectionMap(@PathParam(value = "scopeId") String scopeId, @Context Request request,
			@Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {

		m_log.debug("call getL2ConnectionMap()");

		List<Association> infoResList = new NodeMapControllerBean().getL2ConnectionMap(scopeId);

		List<MapAssociationInfoResponse> dtoResList = new ArrayList<>();
		for (Association infoRes : infoResList) {
			MapAssociationInfoResponse dtoRes = new MapAssociationInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);
		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * L3のコネクション状態の取得を行うAPI
	 */
	@GET
	@Path("/snmp_l3Aassociation/{scopeId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetL3ConnectionMap")
	@RestLog(action = LogAction.Get, target = LogTarget.Snmp, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MapAssociationInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getL3ConnectionMap(@PathParam(value = "scopeId") String scopeId, @Context Request request,
			@Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {

		m_log.debug("call getL3ConnectionMap()");

		List<Association> infoResList = new NodeMapControllerBean().getL3ConnectionMap(scopeId);

		List<MapAssociationInfoResponse> dtoResList = new ArrayList<>();
		for (Association infoRes : infoResList) {
			MapAssociationInfoResponse dtoRes = new MapAssociationInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);
		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * プラットフォーム定義の追加を行うAPI
	 */
	@POST
	@Path("/platformMaster")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCollectPlatformMaster")
	@RestLog(action = LogAction.Add, target = LogTarget.PlatformMaster, type = LogType.UPDATE)
	@RestSystemAdminPrivilege(isNeed=true)
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CollectorPlatformInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addCollectPlatformMaster(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addCollectPlatformMasterBody", content = @Content(schema = @Schema(implementation = AddCollectPlatformMasterRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {

		m_log.info("call addCollectSubPlatformMaster()");

		AddCollectPlatformMasterRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddCollectPlatformMasterRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		CollectorPlatformMstData data = new CollectorPlatformMstData();
		RestBeanUtil.convertBean(dtoReq, data);

		List<CollectorPlatformMstData> infoResList = new PerformanceCollectMasterControllerBean()
				.addCollectPlatformMaster(data);

		CollectorPlatformInfoResponse dtoRes = new CollectorPlatformInfoResponse();
		for (CollectorPlatformMstData infoRes : infoResList) {
			if (infoRes.getPlatformId().equals(dtoReq.getPlatformId())) {
				RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
				break;
			}
		}

		RestLanguageConverter.convertMessages(dtoRes);
		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * サブプラットフォーム定義の追加を行うAPI
	 */
	@POST
	@Path("/subPlatformMasterr")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCollectSubPlatformMaster")
	@RestLog(action = LogAction.Add, target = LogTarget.SubPlatformMaster, type = LogType.UPDATE)
	@RestSystemAdminPrivilege(isNeed=true)
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CollectorSubPlatformInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addCollectSubPlatformMaster(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addCollectSubPlatformMasterBody", content = @Content(schema = @Schema(implementation = AddCollectSubPlatformMasterRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {

		m_log.info("call addCollectSubPlatformMaster()");

		AddCollectSubPlatformMasterRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddCollectSubPlatformMasterRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		CollectorSubPlatformMstData data = new CollectorSubPlatformMstData();
		RestBeanUtil.convertBeanNoInvalid(dtoReq, data);

		List<CollectorSubPlatformMstData> infoResList = new PerformanceCollectMasterControllerBean()
				.addCollectSubPlatformMaster(data);

		CollectorSubPlatformInfoResponse dtoRes = new CollectorSubPlatformInfoResponse();
		for (CollectorSubPlatformMstData infoRes : infoResList) {
			if (infoRes.getSubPlatformId().equals(dtoReq.getSubPlatformId())) {
				RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
				break;
			}
		}

		RestLanguageConverter.convertMessages(dtoRes);
		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * プラットフォーム定義の削除を行うAPI
	 */
	@DELETE
	@Path("/platformMaster")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteCollectPlatformMaster")
	@RestLog(action = LogAction.Delete, target = LogTarget.PlatformMaster, type = LogType.UPDATE)
	@RestSystemAdminPrivilege(isNeed=true)
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CollectorPlatformInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteCollectPlatformMaster(@ArrayTypeParam @QueryParam(value = "platformIds") String platformIds)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, FacilityNotFound {
		m_log.info("call deleteCollectPlatformMaster()");

		List<String> platformIdList = new ArrayList<>();
		if(platformIds != null && !platformIds.isEmpty()) {
			platformIdList = Arrays.asList(platformIds.split(","));
		}

		List<String> infoResList = new PerformanceCollectMasterControllerBean().deleteCollectPlatformMaster(platformIdList);
		List<CollectorPlatformInfoResponse> dtoResList = new ArrayList<>();
		for(String infoRes : infoResList) {
			CollectorPlatformInfoResponse dtoRes = new CollectorPlatformInfoResponse();
			dtoRes.setPlatformId(infoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);
		
		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * サブプラットフォーム定義の削除を行うAPI
	 */
	@DELETE
	@Path("/subPlatformMaster")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteCollectSubPlatformMaster")
	@RestLog(action = LogAction.Delete, target = LogTarget.SubPlatformMaster, type = LogType.UPDATE)
	@RestSystemAdminPrivilege(isNeed=true)
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CollectorSubPlatformInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteCollectSubPlatformMaster(
			@ArrayTypeParam @QueryParam(value = "subPlatformIds") String subPlatformIds)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, FacilityNotFound {

		m_log.info("call deleteCollectSubPlatformMaster()");

		List<String> subPlatformIdList = new ArrayList<>();
		if(subPlatformIds != null && !subPlatformIds.isEmpty()) {
			subPlatformIdList = Arrays.asList(subPlatformIds.split(","));
		}
		
		List<String> infoResList = new PerformanceCollectMasterControllerBean().deleteCollectSubPlatformMaster(subPlatformIdList);
		List<CollectorSubPlatformInfoResponse> dtoResList = new ArrayList<>();
		for(String infoRes : infoResList) {
			CollectorSubPlatformInfoResponse dtoRes = new CollectorSubPlatformInfoResponse();
			dtoRes.setSubPlatformId(infoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);
		
		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * プラットフォーム定義の取得を行うAPI
	 */
	@GET
	@Path("/platformMaster")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCollectPlatformMasterList")
	@RestLog(action = LogAction.Get, target = LogTarget.PlatformMaster, type = LogType.REFERENCE )
	@RestSystemAdminPrivilege(isNeed=true)
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CollectorPlatformInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollectPlatformMasterList() throws HinemosUnknown, InvalidUserPass, InvalidRole {

		m_log.info("call getCollectPlatformMasterList()");

		List<CollectorPlatformMstData> infoResList = new PerformanceCollectMasterControllerBean().getCollectPlatformMaster();

		List<CollectorPlatformInfoResponse> dtoResList = new ArrayList<>();
		for (CollectorPlatformMstData infoRes : infoResList) {
			CollectorPlatformInfoResponse dtoRes = new CollectorPlatformInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);
		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * サブプラットフォーム定義の取得を行うAPI
	 */
	@GET
	@Path("/subPlatformMaster")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCollectSubPlatformMasterList")
	@RestLog(action = LogAction.Get, target = LogTarget.SubPlatformMaster, type = LogType.REFERENCE )
	@RestSystemAdminPrivilege(isNeed=true)
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CollectorSubPlatformInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollectSubPlatformMasterList() throws HinemosUnknown, InvalidUserPass, InvalidRole {

		m_log.info("call getCollectSubPlatformMasterList()");

		List<CollectorSubPlatformMstData> infoResList = new PerformanceCollectMasterControllerBean().getCollectSubPlatformMaster();

		List<CollectorSubPlatformInfoResponse> dtoResList = new ArrayList<>();
		for (CollectorSubPlatformMstData infoRes : infoResList) {
			CollectorSubPlatformInfoResponse dtoRes = new CollectorSubPlatformInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);
		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * ポートに対しマネージャのIPを送信を行うAPI
	 */
	@POST
	@Path("/agent_facilityId_autoConnect_forCloud/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SendManagerDiscoveryInfo")
	@RestLog(action = LogAction.AutoConnect, target = LogTarget.Agent, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.EXEC })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Boolean.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response sendManagerDiscoveryInfo(@PathParam(value = "facilityId") String facilityId,
			@Context Request request, @Context UriInfo uriInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole {

		m_log.info("call sendManagerDiscoveryInfo()");

		Boolean res;
		try {
			res = AgentConnectUtil.sendManagerDiscoveryInfo(facilityId);
		} catch (IOException e) {
			throw new HinemosUnknown(e);
		}

		return Response.status(Status.OK).entity(res).build();
	}

	/**
	 * メンバに@Transientを持つInfoクラスをDTOクラスに変換する
	 * @param info NodeInfo
	 * @return NodeInfoResponseP1
	 */
	private NodeInfoResponseP1 toNodeInfoResponseP1(NodeInfo info) throws HinemosUnknown {
		NodeInfoResponseP1 dto = new NodeInfoResponseP1();
		RestBeanUtil.convertBeanNoInvalid(info, dto);

		dto.setSnmpAuthPassword(info.getSnmpAuthPassword());
		dto.setSnmpPrivPassword(info.getSnmpPrivPassword());
		dto.setWbemUserPassword(info.getWbemUserPassword());
		dto.setIpmiUserPassword(info.getIpmiUserPassword());
		dto.setWinrmUserPassword(info.getWinrmUserPassword());
		dto.setSshUserPassword(info.getSshUserPassword());
		dto.setSshPrivateKeyPassphrase(info.getSshPrivateKeyPassphrase());

		return dto;
	}

	/**
	 * メンバに@Transientを持つInfoクラスをDTOクラスに変換する
	 * @param info NodeInfo
	 * @return NodeInfoResponse
	 */
	private NodeInfoResponse toNodeInfoResponse(NodeInfo info) throws HinemosUnknown {
		NodeInfoResponse dto = new NodeInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(info, dto);

		dto.setSnmpAuthPassword(info.getSnmpAuthPassword());
		dto.setSnmpPrivPassword(info.getSnmpPrivPassword());
		dto.setWbemUserPassword(info.getWbemUserPassword());
		dto.setIpmiUserPassword(info.getIpmiUserPassword());
		dto.setWinrmUserPassword(info.getWinrmUserPassword());
		dto.setSshUserPassword(info.getSshUserPassword());
		dto.setSshPrivateKeyPassphrase(info.getSshPrivateKeyPassphrase());

		return dto;
	}

	/**
	 * メンバに@Transientを持つInfoクラスをDTOクラスに変換する
	 * @param info NodeInfoDeviceSearch
	 * @return NodeInfoDeviceSearchResponse
	 */
	private NodeInfoDeviceSearchResponse toNodeInfoDeviceSearchResponse(NodeInfoDeviceSearch info) throws HinemosUnknown {
		NodeInfoDeviceSearchResponse dto = new NodeInfoDeviceSearchResponse();
		RestBeanUtil.convertBeanNoInvalid(info, dto);

		dto.getNodeInfo().setSnmpAuthPassword(info.getNodeInfo().getSnmpAuthPassword());
		dto.getNodeInfo().setSnmpPrivPassword(info.getNodeInfo().getSnmpPrivPassword());
		dto.getNodeInfo().setWbemUserPassword(info.getNodeInfo().getWbemUserPassword());
		dto.getNodeInfo().setIpmiUserPassword(info.getNodeInfo().getIpmiUserPassword());
		dto.getNodeInfo().setWinrmUserPassword(info.getNodeInfo().getWinrmUserPassword());
		dto.getNodeInfo().setSshUserPassword(info.getNodeInfo().getSshUserPassword());
		dto.getNodeInfo().setSshPrivateKeyPassphrase(info.getNodeInfo().getSshPrivateKeyPassphrase());
		
		// convertMessagesでの翻訳用にitemNameをitemNameTransrateにコピーしておく
		for (DeviceSearchMessageInfoResponse messageDto : dto.getDeviceSearchMessageInfo()) {
			messageDto.setItemNameTransrate(messageDto.getItemName());
		}

		return dto;
	}
}
