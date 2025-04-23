/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.monitorsetting;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_404;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeFunction;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.binary.model.BinaryCheckInfo;
import com.clustercontrol.binary.session.BinaryControllerBean;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.custom.bean.CustomConstant.CommandExecType;
import com.clustercontrol.fault.CollectorNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.fault.MonitorIdInvalid;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jmx.bean.JmxUrlFormatInfo;
import com.clustercontrol.jmx.model.JmxMasterInfo;
import com.clustercontrol.jmx.session.JmxMasterControllerBean;
import com.clustercontrol.jmx.util.JmxUrlFormatUtil;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.monitor.bean.MonitorFilterInfo;
import com.clustercontrol.monitor.run.bean.MonitorInfoBean;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.performance.monitor.model.CollectorItemCodeMstEntity;
import com.clustercontrol.rest.annotation.RestLog;
import com.clustercontrol.rest.annotation.RestLog.LogAction;
import com.clustercontrol.rest.annotation.RestLog.LogTarget;
import com.clustercontrol.rest.annotation.RestLog.LogType;
import com.clustercontrol.rest.annotation.RestLogFunc;
import com.clustercontrol.rest.annotation.RestLogFunc.LogFuncName;
import com.clustercontrol.rest.annotation.RestSystemAdminPrivilege;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.ArrayTypeParam;
import com.clustercontrol.rest.annotation.cmdtool.IgnoreCommandline;
import com.clustercontrol.rest.annotation.cmdtool.IgnoreReference;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.*;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.CommandExecTypeEnum;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.sql.bean.JdbcDriverInfo;
import com.clustercontrol.sql.session.MonitorSqlControllerBean;

@Path("/monitorsetting")
@RestLogFunc(name = LogFuncName.Monitor)
public class MonitorsettingRestEndpoints {

	private static Log m_log = LogFactory.getLog(MonitorsettingRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "monitorsetting";

	// 監視種別IDマップ（クラス名、監視種別ID）
	private static final Map<String, String> monitorTypeIdMap = new HashMap<>();

	static {
		// 監視種別ID
		monitorTypeIdMap.put(AddHttpScenarioMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_HTTP_SCENARIO);
		monitorTypeIdMap.put(ModifyHttpScenarioMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_HTTP_SCENARIO);
		monitorTypeIdMap.put(AddHttpNumericMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_HTTP_N);
		monitorTypeIdMap.put(ModifyHttpNumericMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_HTTP_N);
		monitorTypeIdMap.put(AddHttpStringMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_HTTP_S);
		monitorTypeIdMap.put(ModifyHttpStringMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_HTTP_S);
		monitorTypeIdMap.put(AddAgentMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_AGENT);
		monitorTypeIdMap.put(ModifyAgentMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_AGENT);
		monitorTypeIdMap.put(AddJmxMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_JMX);
		monitorTypeIdMap.put(ModifyJmxMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_JMX);
		monitorTypeIdMap.put(AddPingMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_PING);
		monitorTypeIdMap.put(ModifyPingMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_PING);
		monitorTypeIdMap.put(AddSnmptrapMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_SNMPTRAP);
		monitorTypeIdMap.put(ModifySnmptrapMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_SNMPTRAP);
		monitorTypeIdMap.put(AddSnmpNumericMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_SNMP_N);
		monitorTypeIdMap.put(ModifySnmpNumericMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_SNMP_N);
		monitorTypeIdMap.put(AddSnmpStringMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_SNMP_S);
		monitorTypeIdMap.put(ModifySnmpStringMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_SNMP_S);
		monitorTypeIdMap.put(AddSqlNumericMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_SQL_N);
		monitorTypeIdMap.put(ModifySqlNumericMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_SQL_N);
		monitorTypeIdMap.put(AddSqlStringMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_SQL_S);
		monitorTypeIdMap.put(ModifySqlStringMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_SQL_S);
		monitorTypeIdMap.put(AddWineventMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_WINEVENT);
		monitorTypeIdMap.put(ModifyWineventMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_WINEVENT);
		monitorTypeIdMap.put(AddWinserviceMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_WINSERVICE);
		monitorTypeIdMap.put(ModifyWinserviceMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_WINSERVICE);
		monitorTypeIdMap.put(AddCustomtrapNumericMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_CUSTOMTRAP_N);
		monitorTypeIdMap.put(ModifyCustomtrapNumericMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_CUSTOMTRAP_N);
		monitorTypeIdMap.put(AddCustomtrapStringMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_CUSTOMTRAP_S);
		monitorTypeIdMap.put(ModifyCustomtrapStringMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_CUSTOMTRAP_S);
		monitorTypeIdMap.put(AddCustomNumericMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_CUSTOM_N);
		monitorTypeIdMap.put(ModifyCustomNumericMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_CUSTOM_N);
		monitorTypeIdMap.put(AddCustomStringMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_CUSTOM_S);
		monitorTypeIdMap.put(ModifyCustomStringMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_CUSTOM_S);
		monitorTypeIdMap.put(AddCloudserviceMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_CLOUD_SERVICE_CONDITION);
		monitorTypeIdMap.put(ModifyCloudserviceMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_CLOUD_SERVICE_CONDITION);
		monitorTypeIdMap.put(AddCloudserviceBillingMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_CLOUD_SERVICE_BILLING);
		monitorTypeIdMap.put(ModifyCloudserviceBillingMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_CLOUD_SERVICE_BILLING);
		monitorTypeIdMap.put(AddCloudserviceBillingDetailMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_CLOUD_SERVICE_BILLING_DETAIL);
		monitorTypeIdMap.put(ModifyCloudserviceBillingDetailMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_CLOUD_SERVICE_BILLING_DETAIL);
		monitorTypeIdMap.put(AddCloudLogMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_CLOUD_LOG);
		monitorTypeIdMap.put(ModifyCloudLogMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_CLOUD_LOG);
		monitorTypeIdMap.put(AddServiceportMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_PORT);
		monitorTypeIdMap.put(ModifyServiceportMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_PORT);
		monitorTypeIdMap.put(AddSystemlogMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_SYSTEMLOG);
		monitorTypeIdMap.put(ModifySystemlogMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_SYSTEMLOG);
		monitorTypeIdMap.put(AddBinaryfileMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_BINARYFILE_BIN);
		monitorTypeIdMap.put(ModifyBinaryfileMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_BINARYFILE_BIN);
		monitorTypeIdMap.put(AddPacketcaptureMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_PCAP_BIN);
		monitorTypeIdMap.put(ModifyPacketcaptureMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_PCAP_BIN);
		monitorTypeIdMap.put(AddProcessMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_PROCESS);
		monitorTypeIdMap.put(ModifyProcessMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_PROCESS);
		monitorTypeIdMap.put(AddPerformanceMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_PERFORMANCE);
		monitorTypeIdMap.put(ModifyPerformanceMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_PERFORMANCE);
		monitorTypeIdMap.put(AddLogfileMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_LOGFILE);
		monitorTypeIdMap.put(ModifyLogfileMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_LOGFILE);
		monitorTypeIdMap.put(AddLogcountMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_LOGCOUNT);
		monitorTypeIdMap.put(ModifyLogcountMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_LOGCOUNT);
		monitorTypeIdMap.put(AddCorrelationMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_CORRELATION);
		monitorTypeIdMap.put(ModifyCorrelationMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_CORRELATION);
		monitorTypeIdMap.put(AddIntegrationMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_INTEGRATION);
		monitorTypeIdMap.put(ModifyIntegrationMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_INTEGRATION);
		monitorTypeIdMap.put(AddRpaLogfileMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_RPA_LOGFILE);
		monitorTypeIdMap.put(ModifyRpaLogfileMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_RPA_LOGFILE);
		monitorTypeIdMap.put(AddRpaManagementToolMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_RPA_MGMT_TOOL_SERVICE);
		monitorTypeIdMap.put(ModifyRpaManagementToolMonitorRequest.class.getSimpleName() ,HinemosModuleConstant.MONITOR_RPA_MGMT_TOOL_SERVICE);
	}

	/**
	 * 監視設定一覧の取得を行うAPI
	 */
	@GET
	@Path("/monitor")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMonitorList")
	@RestLog(action = LogAction.Get, target = LogTarget.Monitor, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMonitorList(@Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getMonitorList()");

		List<MonitorInfo> infoResList = new MonitorSettingControllerBean().getMonitorListWithoutCheckInfo(null);
		List<MonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			MonitorInfoResponse dto = new MonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 条件に従い監視設定一覧の取得を行うAPI
	 */
	@POST
	@Path("/monitor_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMonitorListByCondition")
	@RestLog(action = LogAction.Get, target = LogTarget.Monitor, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getMonitorListByCondition(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "getMonitorListRequest", content = @Content(schema = @Schema(implementation = GetMonitorListRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidSetting, InvalidRole, HinemosUnknown {
		m_log.info("call getMonitorListByCondition()");

		GetMonitorListRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,GetMonitorListRequest.class);
		
		MonitorFilterInfoRequest dto = dtoReq.getMonitorFilterInfo();
		MonitorFilterInfo infoReq = new MonitorFilterInfo();
		RestBeanUtil.convertBean(dto, infoReq);
		
		List<MonitorInfo> infoResList = new MonitorSettingControllerBean().getMonitorListWithoutCheckInfo(infoReq);
		List<MonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			MonitorInfoResponse dtoRes = new MonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dtoRes);
			updateDto(info, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 監視設定一覧の取得を行うAPI
	 */
	@GET
	@Path("/monitor_withoutCheckInfo")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMonitorBeanList")
	@RestLog(action = LogAction.Get, target = LogTarget.Monitor, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MonitorInfoBeanResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMonitorBeanList(@Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getMonitorBeanList()");

		List<MonitorInfoBean> infoResList = new MonitorSettingControllerBean().getMonitorBeanListWithoutCheckInfo(null);
		List<MonitorInfoBeanResponse> dtoResList = new ArrayList<>();
		for (MonitorInfoBean info : infoResList) {
			MonitorInfoBeanResponse dto = new MonitorInfoBeanResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 条件に従い監視設定一覧の取得を行うAPI
	 */
	@POST
	@Path("/monitor_withoutCheckInfo_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMonitorBeanListByCondition")
	@RestLog(action = LogAction.Get, target = LogTarget.Monitor, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetMonitorBeanListResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getMonitorBeanListByCondition(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "getMonitorBeanListRequest", content = @Content(schema = @Schema(implementation = GetMonitorBeanListRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidSetting, InvalidRole, HinemosUnknown {
		m_log.info("call getMonitorBeanLisByConditiont()");

		GetMonitorBeanListRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,GetMonitorBeanListRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorFilterInfoRequest dto = dtoReq.getMonitorFilterInfo();
		MonitorFilterInfo infoReq = new MonitorFilterInfo();
		RestBeanUtil.convertBean(dto, infoReq);

		Integer size = dtoReq.getSize();

		List<MonitorInfoBean> infoResList = new MonitorSettingControllerBean().getMonitorBeanListWithoutCheckInfo(infoReq);
		List<MonitorInfoBeanResponse> dtoResList = new ArrayList<>();
		int recCount = 0;
		for (MonitorInfoBean info : infoResList) {
			MonitorInfoBeanResponse dtoRec = new MonitorInfoBeanResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dtoRec);
			dtoResList.add(dtoRec);
			recCount++;
			if (size != null && recCount >= size) {
				break;
			}
		}

		RestLanguageConverter.convertMessages(dtoResList);
		GetMonitorBeanListResponse dtoRes = new GetMonitorBeanListResponse();
		dtoRes.setTotal(infoResList.size());
		dtoRes.setMonitorInfoList(dtoResList);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 文字列監視設定一覧の取得を行うAPI
	 */
	@GET
	@Path("/monitor_string")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetStringMonitoInfoList")
	@RestLog(action = LogAction.Get, target = LogTarget.Monitor, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MonitorInfoResponseP1.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStringMonitoInfoList(
			@QueryParam("facilityId") String facilityId,
			@QueryParam("ownerRoleId") String ownerRoleId,
			@Context Request request,
			@Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getStringMonitoInfoList()");

		// カレントユーザがオーナーロールに所属しているかチェックする
		CommonValidator.validateCurrentUserBelongRole(ownerRoleId);
		
		List<MonitorInfo> infoResList = new MonitorSettingControllerBean().getStringMonitoInfoListForAnalytics(facilityId, ownerRoleId);
		List<MonitorInfoResponseP1> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			MonitorInfoResponseP1 dto = new MonitorInfoResponseP1();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * HTTPシナリオ監視設定の追加を行うAPI
	 */
	@POST
	@Path("/httpScenario")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddHttpScenarioMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.HttpScenario, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HttpScenarioMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addHttpScenarioMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addHttpScenarioMonitorBody", content = @Content(schema = @Schema(implementation = AddHttpScenarioMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addHttpScenarioMonitor()");

		AddHttpScenarioMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, AddHttpScenarioMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		HttpScenarioMonitorInfoResponse dtoRes = new HttpScenarioMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * HTTP監視（数値）設定の追加を行うAPI
	 */
	@POST
	@Path("/httpNumeric")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddHttpNumericMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.HttpNumeric, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HttpNumericMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")})
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addHttpNumericMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addHttpNumericMonitorBody", content = @Content(schema = @Schema(implementation = AddHttpNumericMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addHttpNumericMonitor()");

		AddHttpNumericMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddHttpNumericMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		HttpNumericMonitorInfoResponse dtoRes = new HttpNumericMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * HTTP監視（文字列）設定の追加を行うAPI
	 */
	@POST
	@Path("/httpString")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddHttpStringMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.HttpString, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HttpStringMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")})
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addHttpStringMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addHttpStringMonitorBody", content = @Content(schema = @Schema(implementation = AddHttpStringMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addHttpStringMonitor()");

		AddHttpStringMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddHttpStringMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		HttpStringMonitorInfoResponse dtoRes = new HttpStringMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * エージェント監視設定の追加を行うAPI
	 */
	@POST
	@Path("/agent")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddAgentMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.Agent, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = AgentMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")})
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addAgentMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addAgentMonitorBody", content = @Content(schema = @Schema(implementation = AddAgentMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addAgentMonitor()");

		AddAgentMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddAgentMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		AgentMonitorInfoResponse dtoRes = new AgentMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * JMX監視設定の追加を行うAPI
	 */
	@POST
	@Path("/jmx")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddJmxMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.Jmx, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JmxMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addJmxMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addJmxMonitorBody", content = @Content(schema = @Schema(implementation = AddJmxMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addJmxMonitor()");

		AddJmxMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddJmxMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		JmxMonitorInfoResponse dtoRes = new JmxMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * PING監視設定の追加を行うAPI
	 */
	@POST
	@Path("/ping")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddPingMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.Ping, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PingMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addPingMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addPingMonitorBody", content = @Content(schema = @Schema(implementation = AddPingMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addPingMonitor()");

		AddPingMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddPingMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		PingMonitorInfoResponse dtoRes = new PingMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * SNMPトラップ監視設定の追加を行うAPI
	 */
	@POST
	@Path("/snmptrap")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddSnmptrapMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.Snmptrap, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SnmptrapMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addSnmptrapMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addSnmptrapMonitorBody", content = @Content(schema = @Schema(implementation = AddSnmptrapMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addSnmptrapMonitor()");

		AddSnmptrapMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddSnmptrapMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		SnmptrapMonitorInfoResponse dtoRes = new SnmptrapMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * SNMP監視（数値）設定の追加を行うAPI
	 */
	@POST
	@Path("/snmpNumeric")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddSnmpNumericMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.SnmpNumeric, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SnmpNumericMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")})
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addSnmpNumericMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addSnmpNumericMonitorBody", content = @Content(schema = @Schema(implementation = AddSnmpNumericMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addSnmpNumericMonitor()");

		AddSnmpNumericMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddSnmpNumericMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		SnmpNumericMonitorInfoResponse dtoRes = new SnmpNumericMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * SNMP監視（文字列）設定の追加を行うAPI
	 */
	@POST
	@Path("/snmpString")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddSnmpStringMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.SnmpString, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SnmpStringMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addSnmpStringMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addSnmpStringMonitorBody", content = @Content(schema = @Schema(implementation = AddSnmpStringMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addSnmpStringMonitor()");

		AddSnmpStringMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddSnmpStringMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		SnmpStringMonitorInfoResponse dtoRes = new SnmpStringMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * SQL監視（数値）設定の追加を行うAPI
	 */
	@POST
	@Path("/sqlNumeric")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddSqlNumericMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.SqlNumeric, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SqlNumericMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addSqlNumericMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addSqlNumericMonitorBody", content = @Content(schema = @Schema(implementation = AddSqlNumericMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addSqlNumericMonitor()");

		AddSqlNumericMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddSqlNumericMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		SqlNumericMonitorInfoResponse dtoRes = new SqlNumericMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * SQL監視（文字列）設定の追加を行うAPI
	 */
	@POST
	@Path("/sqlString")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddSqlStringMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.SqlString, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SqlStringMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")})
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addSqlStringMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addSqlStringMonitorBody", content = @Content(schema = @Schema(implementation = AddSqlStringMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addSqlStringMonitor()");

		AddSqlStringMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddSqlStringMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		SqlStringMonitorInfoResponse dtoRes = new SqlStringMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * Windowsイベント監視設定の追加を行うAPI
	 */
	@POST
	@Path("/winevent")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddWineventMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.Winevent, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = WineventMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addWineventMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addWineventMonitorBody", content = @Content(schema = @Schema(implementation = AddWineventMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addWineventMonitor()");

		AddWineventMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddWineventMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		WineventMonitorInfoResponse dtoRes = new WineventMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * Windowsサービス監視設定の追加を行うAPI
	 */
	@POST
	@Path("/winservice")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddWinserviceMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.Winservice, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = WinserviceMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addWinserviceMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addWinserviceMonitorBody", content = @Content(schema = @Schema(implementation = AddWinserviceMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addWinserviceMonitor()");

		AddWinserviceMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddWinserviceMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		WinserviceMonitorInfoResponse dtoRes = new WinserviceMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * カスタムトラップ監視（数値）設定の追加を行うAPI
	 */
	@POST
	@Path("/customtrapNumeric")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCustomtrapNumericMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.CustomtrapNumeric, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CustomtrapNumericMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addCustomtrapNumericMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addCustomtrapNumericMonitorBody", content = @Content(schema = @Schema(implementation = AddCustomtrapNumericMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addCustomtrapNumericMonitor()");

		AddCustomtrapNumericMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddCustomtrapNumericMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		CustomtrapNumericMonitorInfoResponse dtoRes = new CustomtrapNumericMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * カスタムトラップ監視（文字列）設定の追加を行うAPI
	 */
	@POST
	@Path("/customtrapString")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCustomtrapStringMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.CustomtrapString, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CustomtrapStringMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addCustomtrapStringMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addCustomtrapStringMonitorBody", content = @Content(schema = @Schema(implementation = AddCustomtrapStringMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addCustomtrapStringMonitor()");

		AddCustomtrapStringMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddCustomtrapStringMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		CustomtrapStringMonitorInfoResponse dtoRes = new CustomtrapStringMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * カスタム監視（数値）設定の追加を行うAPI
	 */
	@POST
	@Path("/customNumeric")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCustomNumericMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.CustomNumeric, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CustomNumericMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addCustomNumericMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addCustomNumericMonitorBody", content = @Content(schema = @Schema(implementation = AddCustomNumericMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addCustomNumericMonitor()");

		AddCustomNumericMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddCustomNumericMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		CustomNumericMonitorInfoResponse dtoRes = new CustomNumericMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * カスタム監視（文字列）設定の追加を行うAPI
	 */
	@POST
	@Path("/customString")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCustomStringMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.CustomString, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CustomStringMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addCustomStringMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addCustomStringMonitorBody", content = @Content(schema = @Schema(implementation = AddCustomStringMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addCustomStringMonitor()");

		AddCustomStringMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddCustomStringMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		CustomStringMonitorInfoResponse dtoRes = new CustomStringMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * クラウドログ監視設定の追加を行うAPI
	 */
	@POST
	@Path("/cloudlog")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCloudLogMonitor")
	@RestLog(action=LogAction.Add, target=LogTarget.CloudLog, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudLogMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response addCloudLogMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addCloudLogMonitorBody", content = @Content(schema = @Schema(implementation = AddCloudLogMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addCloudLogMonitor()");

		AddCloudLogMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddCloudLogMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		CloudLogMonitorInfoResponse dtoRes = new CloudLogMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * クラウドサービス監視設定の追加を行うAPI
	 */
	@POST
	@Path("/cloudservice")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCloudserviceMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.Cloudservice, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudserviceMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response addCloudserviceMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addCloudserviceMonitorBody", content = @Content(schema = @Schema(implementation = AddCloudserviceMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addCloudserviceMonitor()");

		AddCloudserviceMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddCloudserviceMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		CloudserviceMonitorInfoResponse dtoRes = new CloudserviceMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * クラウド課金監視設定の追加を行うAPI
	 */
	@POST
	@Path("/cloudservicebilling")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCloudservicebillingMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.Cloudservicebilling, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudserviceBillingMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response addCloudservicebillingMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addCloudservicebillingMonitorBody", content = @Content(schema = @Schema(implementation = AddCloudserviceBillingMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addCloudservicebillingMonitor()");

		AddCloudserviceBillingMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddCloudserviceBillingMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		CloudserviceBillingMonitorInfoResponse dtoRes = new CloudserviceBillingMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * クラウド課金詳細監視設定の追加を行うAPI
	 */
	@POST
	@Path("/cloudservicebillingdetail")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCloudservicebillingdetailMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.Cloudservicebillingdetail, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudserviceBillingDetailMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response addCloudservicebillingdetailMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addCloudserviceBillingDetailMonitorBody", content = @Content(schema = @Schema(implementation = AddCloudserviceBillingDetailMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addCloudservicebillingdetailMonitor()");

		AddCloudserviceBillingDetailMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddCloudserviceBillingDetailMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		CloudserviceBillingDetailMonitorInfoResponse dtoRes = new CloudserviceBillingDetailMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * サービス・ポート監視設定の追加を行うAPI
	 */
	@POST
	@Path("/serviceport")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddServiceportMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.Serviceport, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ServiceportMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addServiceportMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addServiceportMonitorBody", content = @Content(schema = @Schema(implementation = AddServiceportMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addServiceportMonitor()");

		AddServiceportMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddServiceportMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		ServiceportMonitorInfoResponse dtoRes = new ServiceportMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * システムログ監視設定の追加を行うAPI
	 */
	@POST
	@Path("/systemlog")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddSystemlogMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.Systemlog, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SystemlogMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addSystemlogMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addSystemlogMonitorBody", content = @Content(schema = @Schema(implementation = AddSystemlogMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addSystemlogMonitor()");

		AddSystemlogMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddSystemlogMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		SystemlogMonitorInfoResponse dtoRes = new SystemlogMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * バイナリファイル監視設定の追加を行うAPI
	 */
	@POST
	@Path("/binaryfile")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddBinaryfileMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.Binaryfile, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = BinaryfileMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addBinaryfileMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addBinaryfileMonitorBody", content = @Content(schema = @Schema(implementation = AddBinaryfileMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addBinaryfileMonitor()");

		AddBinaryfileMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddBinaryfileMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		BinaryfileMonitorInfoResponse dtoRes = new BinaryfileMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * パケットキャプチャ監視設定の追加を行うAPI
	 */
	@POST
	@Path("/packetCapture")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddPacketcaptureMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.PacketCapture, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PacketcaptureMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addPacketcaptureMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addPacketcaptureMonitorBody", content = @Content(schema = @Schema(implementation = AddPacketcaptureMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addPacketcaptureMonitor()");

		AddPacketcaptureMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddPacketcaptureMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		PacketcaptureMonitorInfoResponse dtoRes = new PacketcaptureMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * プロセス監視設定の追加を行うAPI
	 */
	@POST
	@Path("/process")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddProcessMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.Process, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProcessMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addProcessMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addProcessMonitorBody", content = @Content(schema = @Schema(implementation = AddProcessMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addProcessMonitor()");

		AddProcessMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddProcessMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		ProcessMonitorInfoResponse dtoRes = new ProcessMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * リソース監視設定の追加を行うAPI
	 */
	@POST
	@Path("/performance")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddPerformanceMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.Performance, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PerformanceMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addPerformanceMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addPerformanceMonitorBody", content = @Content(schema = @Schema(implementation = AddPerformanceMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addPerformanceMonitor()");

		AddPerformanceMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddPerformanceMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		PerformanceMonitorInfoResponse dtoRes = new PerformanceMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ログファイル監視設定の追加を行うAPI
	 */
	@POST
	@Path("/logfile")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddLogfileMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.Logfile, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = LogfileMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addLogfileMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addLogfileMonitorBody", content = @Content(schema = @Schema(implementation = AddLogfileMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addLogfileMonitor()");

		AddLogfileMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddLogfileMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		LogfileMonitorInfoResponse dtoRes = new LogfileMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ログ件数監視設定の追加を行うAPI
	 */
	@POST
	@Path("/logcount")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddLogcountMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.Logcount, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = LogcountMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addLogcountMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addLogcountMonitorBody", content = @Content(schema = @Schema(implementation = AddLogcountMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addLogcountMonitor()");

		AddLogcountMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddLogcountMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		LogcountMonitorInfoResponse dtoRes = new LogcountMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 相関係数監視設定の追加を行うAPI
	 */
	@POST
	@Path("/correlation")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCorrelationMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.Correlation, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CorrelationMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addCorrelationMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addCorrelationMonitorBody", content = @Content(schema = @Schema(implementation = AddCorrelationMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addCorrelationMonitor()");

		AddCorrelationMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddCorrelationMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		CorrelationMonitorInfoResponse dtoRes = new CorrelationMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 収集値統合監視設定の追加を行うAPI
	 */
	@POST
	@Path("/integration")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddIntegrationMonitor")
	@RestLog(action=LogAction.Add, target = LogTarget.Integration, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = IntegrationMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addIntegrationMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addIntegrationMonitorBody", content = @Content(schema = @Schema(implementation = AddIntegrationMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addIntegrationMonitor()");

		AddIntegrationMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddIntegrationMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		IntegrationMonitorInfoResponse dtoRes = new IntegrationMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * RPAログファイル監視設定の追加を行うAPI
	 */
	@POST
	@Path("/rpalogfile")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddRpaLogfileMonitor")
	@RestLog(action=LogAction.Add, target=LogTarget.RpaLogfile, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaLogfileMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response addRpaLogfileMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addRpaLogfileMonitorBody", content = @Content(schema = @Schema(implementation = AddRpaLogfileMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addRpaLogfileMonitor()");

		AddRpaLogfileMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, AddRpaLogfileMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		RpaLogfileMonitorInfoResponse dtoRes = new RpaLogfileMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * RPA管理ツールサービス監視設定の追加を行うAPI
	 */
	@POST
	@Path("/rpaManagementTool")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddRpaManagementToolMonitor")
	@RestLog(action=LogAction.Add, target=LogTarget.RpaToolService, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaManagementToolMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")})
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response addRpaManagementToolMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addRpaManagementToolMonitorBody", content = @Content(schema = @Schema(implementation = AddRpaManagementToolMonitorRequest.class))) String requestBody)
			throws MonitorIdInvalid, MonitorDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call addRpaManagementToolMonitor()");

		AddRpaManagementToolMonitorRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddRpaManagementToolMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().addMonitor(infoReq);

		RpaManagementToolMonitorInfoResponse dtoRes = new RpaManagementToolMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * HTTPシナリオ監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/httpScenario/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyHttpScenarioMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.HttpScenario, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HttpScenarioMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyHttpScenarioMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyHttpScenarioMonitorBody", content = @Content(schema = @Schema(implementation = ModifyHttpScenarioMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyHttpScenarioMonitor()");

		ModifyHttpScenarioMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyHttpScenarioMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		HttpScenarioMonitorInfoResponse dtoRes = new HttpScenarioMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * HTTP監視（数値）設定の更新を行うAPI
	 */
	@PUT
	@Path("/httpNumeric/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyHttpNumericMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.HttpNumeric, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HttpNumericMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyHttpNumericMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyHttpNumericMonitorBody", content = @Content(schema = @Schema(implementation = ModifyHttpNumericMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyHttpNumericMonitor()");

		ModifyHttpNumericMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyHttpNumericMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		HttpNumericMonitorInfoResponse dtoRes = new HttpNumericMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * HTTP監視（文字列）設定の更新を行うAPI
	 */
	@PUT
	@Path("/httpString/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyHttpStringMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.HttpString, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HttpStringMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyHttpStringMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyHttpStringMonitorBody", content = @Content(schema = @Schema(implementation = ModifyHttpStringMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyHttpStringMonitor()");

		ModifyHttpStringMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyHttpStringMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		HttpStringMonitorInfoResponse dtoRes = new HttpStringMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * エージェント監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/agent/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyAgentMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.Agent, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = AgentMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyAgentMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyAgentMonitorBody", content = @Content(schema = @Schema(implementation = ModifyAgentMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyAgentMonitor()");

		ModifyAgentMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyAgentMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		AgentMonitorInfoResponse dtoRes = new AgentMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * JMX監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/jmx/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyJmxMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.Jmx, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JmxMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyJmxMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyJmxMonitorBody", content = @Content(schema = @Schema(implementation = ModifyJmxMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyJmxMonitor()");

		ModifyJmxMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyJmxMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		JmxMonitorInfoResponse dtoRes = new JmxMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * PING監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/ping/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyPingMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.Ping, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PingMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyPingMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyPingMonitorBody", content = @Content(schema = @Schema(implementation = ModifyPingMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyPingMonitor()");

		ModifyPingMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyPingMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		PingMonitorInfoResponse dtoRes = new PingMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * SNMPトラップ監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/snmptrap/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifySnmptrapMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.Snmptrap, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SnmptrapMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifySnmptrapMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifySnmptrapMonitorBody", content = @Content(schema = @Schema(implementation = ModifySnmptrapMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifySnmptrapMonitor()");

		ModifySnmptrapMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifySnmptrapMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		SnmptrapMonitorInfoResponse dtoRes = new SnmptrapMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * SNMP監視（数値）設定の更新を行うAPI
	 */
	@PUT
	@Path("/snmpNumeric/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifySnmpNumericMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.SnmpNumeric, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SnmpNumericMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifySnmpNumericMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifySnmpNumericMonitorBody", content = @Content(schema = @Schema(implementation = ModifySnmpNumericMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifySnmpNumericMonitor()");

		ModifySnmpNumericMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifySnmpNumericMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		SnmpNumericMonitorInfoResponse dtoRes = new SnmpNumericMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * SNMP監視（文字列）設定の更新を行うAPI
	 */
	@PUT
	@Path("/snmpString/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifySnmpStringMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.SnmpString, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SnmpStringMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifySnmpStringMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifySnmpStringMonitorBody", content = @Content(schema = @Schema(implementation = ModifySnmpStringMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifySnmpStringMonitor()");

		ModifySnmpStringMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifySnmpStringMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		SnmpStringMonitorInfoResponse dtoRes = new SnmpStringMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * SQL監視（数値）設定の更新を行うAPI
	 */
	@PUT
	@Path("/sqlNumeric/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifySqlNumericMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.SqlNumeric, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SqlNumericMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifySqlNumericMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifySqlNumericMonitorBody", content = @Content(schema = @Schema(implementation = ModifySqlNumericMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifySqlNumericMonitor()");

		ModifySqlNumericMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifySqlNumericMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		SqlNumericMonitorInfoResponse dtoRes = new SqlNumericMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * SQL監視（文字列）設定の更新を行うAPI
	 */
	@PUT
	@Path("/sqlString/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifySqlStringMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.SqlString, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SqlStringMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifySqlStringMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifySqlStringMonitorBody", content = @Content(schema = @Schema(implementation = ModifySqlStringMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifySqlStringMonitor()");

		ModifySqlStringMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifySqlStringMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		SqlStringMonitorInfoResponse dtoRes = new SqlStringMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * Windowsイベント監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/winevent/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyWineventMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.Winevent, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = WineventMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyWineventMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyWineventMonitorBody", content = @Content(schema = @Schema(implementation = ModifyWineventMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyWineventMonitor()");

		ModifyWineventMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyWineventMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		WineventMonitorInfoResponse dtoRes = new WineventMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * Windowsサービス監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/winservice/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyWinserviceMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.Winservice, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = WinserviceMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyWinserviceMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyWinserviceMonitorBody", content = @Content(schema = @Schema(implementation = ModifyWinserviceMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyWinserviceMonitor()");

		ModifyWinserviceMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyWinserviceMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		WinserviceMonitorInfoResponse dtoRes = new WinserviceMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * カスタムトラップ監視（数値）設定の更新を行うAPI
	 */
	@PUT
	@Path("/customtrapNumeric/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCustomtrapNumericMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.CustomtrapNumeric, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CustomtrapNumericMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyCustomtrapNumericMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyCustomtrapNumericMonitorBody", content = @Content(schema = @Schema(implementation = ModifyCustomtrapNumericMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyCustomtrapNumericMonitor()");

		ModifyCustomtrapNumericMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyCustomtrapNumericMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		CustomtrapNumericMonitorInfoResponse dtoRes = new CustomtrapNumericMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * カスタムトラップ監視（文字列）設定の更新を行うAPI
	 */
	@PUT
	@Path("/customtrapString/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCustomtrapStringMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.CustomtrapString, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CustomtrapStringMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyCustomtrapStringMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyCustomtrapStringMonitorBody", content = @Content(schema = @Schema(implementation = ModifyCustomtrapStringMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyCustomtrapStringMonitor()");

		ModifyCustomtrapStringMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyCustomtrapStringMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		CustomtrapStringMonitorInfoResponse dtoRes = new CustomtrapStringMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * カスタム監視（数値）設定の更新を行うAPI
	 */
	@PUT
	@Path("/customNumeric/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCustomNumericMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.CustomNumeric, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CustomNumericMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyCustomNumericMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyCustomNumericMonitorBody", content = @Content(schema = @Schema(implementation = ModifyCustomNumericMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyCustomNumericMonitor()");

		ModifyCustomNumericMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyCustomNumericMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		CustomNumericMonitorInfoResponse dtoRes = new CustomNumericMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * カスタム監視（文字列）設定の更新を行うAPI
	 */
	@PUT
	@Path("/customString/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCustomStringMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.CustomString, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CustomStringMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyCustomStringMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyCustomStringMonitorBody", content = @Content(schema = @Schema(implementation = ModifyCustomStringMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyCustomStringMonitor()");

		ModifyCustomStringMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyCustomStringMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		CustomStringMonitorInfoResponse dtoRes = new CustomStringMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * クラウドサービス監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/cloudservice/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCloudserviceMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.Cloudservice, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudserviceMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response modifyCloudserviceMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyCloudserviceMonitorBody", content = @Content(schema = @Schema(implementation = ModifyCloudserviceMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyCloudserviceMonitor()");

		ModifyCloudserviceMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyCloudserviceMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		CloudserviceMonitorInfoResponse dtoRes = new CloudserviceMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * クラウド課金監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/cloudservicebilling/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCloudservicebillingMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.Cloudservicebilling, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudserviceBillingMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response modifyCloudservicebillingMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyCloudservicebillingMonitorBody", content = @Content(schema = @Schema(implementation = ModifyCloudserviceBillingMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyCloudservicebillingMonitor()");

		ModifyCloudserviceBillingMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyCloudserviceBillingMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		CloudserviceBillingMonitorInfoResponse dtoRes = new CloudserviceBillingMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * クラウド課金詳細監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/cloudservicebillingdetail/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCloudservicebillingdetailMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.Cloudservicebillingdetail, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudserviceBillingDetailMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response modifyCloudservicebillingdetailMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyCloudservicebillingdetailMonitorBody", content = @Content(schema = @Schema(implementation = ModifyCloudserviceBillingDetailMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyCloudservicebillingdetailMonitor()");

		ModifyCloudserviceBillingDetailMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyCloudserviceBillingDetailMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		CloudserviceBillingDetailMonitorInfoResponse dtoRes = new CloudserviceBillingDetailMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * クラウドログ監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/cloudlog/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCloudLogMonitor")
	@RestLog(action=LogAction.Modify, target=LogTarget.CloudLog, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudLogMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response modifyCloudLogMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyCloudLogMonitorBody", content = @Content(schema = @Schema(implementation = ModifyCloudLogMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyCloudLogMonitor()");

		ModifyCloudLogMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyCloudLogMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		CloudLogMonitorInfoResponse dtoRes = new CloudLogMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * サービス・ポート監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/serviceport/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyServiceportMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.Serviceport, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ServiceportMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyServiceportMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyServiceportMonitorBody", content = @Content(schema = @Schema(implementation = ModifyServiceportMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyServiceportMonitor()");

		ModifyServiceportMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyServiceportMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		ServiceportMonitorInfoResponse dtoRes = new ServiceportMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * システムログ監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/systemlog/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifySystemlogMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.Systemlog, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SystemlogMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifySystemlogMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifySystemlogMonitorBody", content = @Content(schema = @Schema(implementation = ModifySystemlogMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifySystemlogMonitor()");

		ModifySystemlogMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifySystemlogMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		SystemlogMonitorInfoResponse dtoRes = new SystemlogMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * バイナリファイル監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/binaryfile/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyBinaryfileMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.Binaryfile, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = BinaryfileMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyBinaryfileMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyBinaryfileMonitorBody", content = @Content(schema = @Schema(implementation = ModifyBinaryfileMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyBinaryfileMonitor()");

		ModifyBinaryfileMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyBinaryfileMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		BinaryfileMonitorInfoResponse dtoRes = new BinaryfileMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * パケットキャプチャ監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/packetcapture/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyPacketcaptureMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.PacketCapture, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PacketcaptureMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyPacketcaptureMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyPacketcaptureMonitorBody", content = @Content(schema = @Schema(implementation = ModifyPacketcaptureMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyPacketcaptureMonitor()");

		ModifyPacketcaptureMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyPacketcaptureMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		PacketcaptureMonitorInfoResponse dtoRes = new PacketcaptureMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * プロセス監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/process/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyProcessMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.Process, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProcessMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyProcessMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyProcessMonitorBody", content = @Content(schema = @Schema(implementation = ModifyProcessMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyProcessMonitor()");

		ModifyProcessMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyProcessMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		ProcessMonitorInfoResponse dtoRes = new ProcessMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * リソース監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/performance/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyPerformanceMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.Performance, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PerformanceMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyPerformanceMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyPerformanceMonitorBody", content = @Content(schema = @Schema(implementation = ModifyPerformanceMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyPerformanceMonitor()");

		ModifyPerformanceMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyPerformanceMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		PerformanceMonitorInfoResponse dtoRes = new PerformanceMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ログファイル監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/logfile/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyLogfileMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.Logfile, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = LogfileMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyLogfileMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyLogfileMonitorBody", content = @Content(schema = @Schema(implementation = ModifyLogfileMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyLogfileMonitor()");

		ModifyLogfileMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyLogfileMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		LogfileMonitorInfoResponse dtoRes = new LogfileMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ログ件数監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/logcount/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyLogcountMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.Logcount, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = LogcountMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyLogcountMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyLogcountMonitorBody", content = @Content(schema = @Schema(implementation = ModifyLogcountMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyLogcountMonitor()");

		ModifyLogcountMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyLogcountMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		LogcountMonitorInfoResponse dtoRes = new LogcountMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 相関係数監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/correlation/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCorrelationMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.Correlation, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CorrelationMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyCorrelationMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyCorrelationMonitorBody", content = @Content(schema = @Schema(implementation = ModifyCorrelationMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyCorrelationMonitor()");

		ModifyCorrelationMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyCorrelationMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		CorrelationMonitorInfoResponse dtoRes = new CorrelationMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 収集値統合監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/integration/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyIntegrationMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.Integration, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = IntegrationMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyIntegrationMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyIntegrationMonitorBody", content = @Content(schema = @Schema(implementation = ModifyIntegrationMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyIntegrationMonitor()");

		ModifyIntegrationMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyIntegrationMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		IntegrationMonitorInfoResponse dtoRes = new IntegrationMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * RPAログファイル監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/rpalogfile/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyRpaLogfileMonitor")
	@RestLog(action=LogAction.Modify, target=LogTarget.RpaLogfile, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaLogfileMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response modifyRpaLogfileMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyRpaLogfileMonitorBody", content = @Content(schema = @Schema(implementation = ModifyRpaLogfileMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyRpaLogfileMonitor()");

		ModifyRpaLogfileMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyRpaLogfileMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		RpaLogfileMonitorInfoResponse dtoRes = new RpaLogfileMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * RPA管理ツール監視設定の更新を行うAPI
	 */
	@PUT
	@Path("/rpaManagementTool/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyRpaManagementToolMonitor")
	@RestLog(action=LogAction.Modify, target=LogTarget.RpaToolService, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaManagementToolMonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response modifyRpaManagementToolMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyRpaManagementToolMonitorBody", content = @Content(schema = @Schema(implementation = ModifyRpaManagementToolMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call modifyRpaManagementToolMonitor()");

		ModifyRpaManagementToolMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyRpaManagementToolMonitorRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MonitorInfo infoReq = new MonitorInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMonitorId(monitorId);
		updateInfo(dtoReq, infoReq);
		MonitorInfo infoRes = new MonitorSettingControllerBean().modifyMonitor(infoReq);

		RpaManagementToolMonitorInfoResponse dtoRes = new RpaManagementToolMonitorInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	/**
	 * 監視設定の削除を行うAPI
	 */
	@DELETE
	@Path("/monitor")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteMonitor")
	@RestLog(action=LogAction.Delete, target = LogTarget.Monitor, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MonitorInfoBeanResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteMonitor(
			@ArrayTypeParam @QueryParam(value = "monitorIds") String monitorIds,
			@Context Request request,
			@Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		m_log.info("call deleteMonitor()");

		List<String> monitorIdList = new ArrayList<>();
		if(monitorIds != null && !monitorIds.isEmpty()) {
			monitorIdList = Arrays.asList(monitorIds.split(","));
		}
		
		List<MonitorInfo> infoResList = new MonitorSettingControllerBean().deleteMonitor(monitorIdList);
		List<MonitorInfoBeanResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			MonitorInfoBeanResponse dto = new MonitorInfoBeanResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 監視設定の取得を行うAPI
	 */
	@GET
	@Path("/monitor/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMonitor")
	@RestLog(action=LogAction.Get, target = LogTarget.Monitor, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MonitorInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMonitor(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getMonitor()");

		MonitorInfo infoRes = new MonitorSettingControllerBean().getMonitor(monitorId);
		MonitorInfoResponse dtoRes = new MonitorInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
		updateDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 性能グラフ表示への対象である監視設定の取得を行うAPI
	 */
	@GET
	@Path("/monitor_graphInfo_forCollect/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMonitorInfoForGraph")
	@RestLog(action=LogAction.Get, target = LogTarget.Monitor, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.Collect,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MonitorInfoResponseP3.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreReference
	@IgnoreCommandline
	public Response getMonitorInfoForGraph(
			@PathParam("monitorId") String monitorId,
			@Context Request request,
			@Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getMonitorInfoForGraph()");

		MonitorInfo infoRes = new MonitorSettingControllerBean().getMonitor(monitorId);

		MonitorInfoResponseP3 dtoRes = new MonitorInfoResponseP3();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 監視設定の監視有効／無効の切り替えを行うAPI
	 */
	@PUT
	@Path("/monitor_monitorValid")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SetStatusMonitor")
	@RestLog(action=LogAction.Modify, target = LogTarget.Monitor, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MonitorInfoBeanResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setStatusMonitor(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "setStatusMonitorBody", content = @Content(schema = @Schema(implementation = SetStatusMonitorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidSetting, InvalidRole, HinemosUnknown {
		m_log.info("call setStatusMonitor()");

		SetStatusMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, SetStatusMonitorRequest.class);

		List<MonitorInfo> infoResList = new MonitorSettingControllerBean().setStatusMonitor(dtoReq.getMonitorIds(), dtoReq.getValidFlg());
		List<MonitorInfoBeanResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			MonitorInfoBeanResponse dto = new MonitorInfoBeanResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 監視設定の収集有効／無効の切り替えを行うAPI
	 */
	@PUT
	@Path("/monitor_collectorValid")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SetStatusCollector")
	@RestLog(action=LogAction.Modify, target = LogTarget.Monitor, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MonitorInfoBeanResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setStatusCollector(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "setStatusCollectorBody", content = @Content(schema = @Schema(implementation = SetStatusCollectorRequest.class))) String requestBody)
			throws MonitorNotFound, InvalidUserPass, InvalidSetting, InvalidRole, HinemosUnknown {
		m_log.info("call setStatusCollector()");

		SetStatusCollectorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, SetStatusCollectorRequest.class);

		List<MonitorInfo> infoResList = new MonitorSettingControllerBean().setStatusCollector(dtoReq.getMonitorIds(), dtoReq.getValidFlg());
		List<MonitorInfoBeanResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			MonitorInfoBeanResponse dto = new MonitorInfoBeanResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * JDBCドライバ一覧の取得を行うAPI
	 */
	@GET
	@Path("/sql/jdbcDriver")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJdbcDriverList")
	@RestLog(action=LogAction.Get, target = LogTarget.Sql, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JdbcDriverInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJdbcDriverList(@Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getJdbcDriverList()");

		List<JdbcDriverInfo> infoResList = new MonitorSqlControllerBean().getJdbcDriverList();
		List<JdbcDriverInfoResponse> dtoResList = new ArrayList<>();
		for (JdbcDriverInfo info : infoResList) {
			JdbcDriverInfoResponse dto = new JdbcDriverInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * エージェント監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/agent")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetAgentList")
	@RestLog(action=LogAction.Get, target = LogTarget.Agent, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = AgentMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAgentList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getAgentList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_AGENT, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_AGENT);
		}
		List<AgentMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			AgentMonitorInfoResponse dto = new AgentMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * HTTPシナリオ監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/httpScenario")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetHttpScenarioList")
	@RestLog(action=LogAction.Get, target = LogTarget.HttpScenario, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HttpScenarioMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getHttpScenarioList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getHttpScenarioList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_HTTP_SCENARIO, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_HTTP_SCENARIO);
		}
		List<HttpScenarioMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			HttpScenarioMonitorInfoResponse dto = new HttpScenarioMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * HTTP監視（数値）一覧の取得を行うAPI
	 */
	@GET
	@Path("/httpNumeric")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetHttpNumericList")
	@RestLog(action=LogAction.Get, target = LogTarget.HttpNumeric, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HttpNumericMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getHttpNumericList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getHttpNumericList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_HTTP_N, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_HTTP_N);
		}
		List<HttpNumericMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			HttpNumericMonitorInfoResponse dto = new HttpNumericMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * HTTP監視（文字列）一覧の取得を行うAPI
	 */
	@GET
	@Path("/httpString")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetHttpStringList")
	@RestLog(action=LogAction.Get, target = LogTarget.HttpString, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HttpStringMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getHttpStringList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getHttpStringList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_HTTP_S, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_HTTP_S);
		}
		List<HttpStringMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			HttpStringMonitorInfoResponse dto = new HttpStringMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * JMX監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/jmx")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJmxList")
	@RestLog(action=LogAction.Get, target = LogTarget.Jmx, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JmxMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJmxList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getJmxList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_JMX, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_JMX);
		}
		List<JmxMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			JmxMonitorInfoResponse dto = new JmxMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * ログファイル監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/logfile")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetLogfileList")
	@RestLog(action=LogAction.Get, target = LogTarget.Logfile, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = LogfileMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLogfileList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getLogfileList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_LOGFILE, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_LOGFILE);
		}
		List<LogfileMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			LogfileMonitorInfoResponse dto = new LogfileMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * リソース監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/performance")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetPerformanceList")
	@RestLog(action=LogAction.Get, target = LogTarget.Performance, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PerformanceMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPerformanceList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getPerformanceList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_PERFORMANCE, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_PERFORMANCE);
		}
		List<PerformanceMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			PerformanceMonitorInfoResponse dto = new PerformanceMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * PING監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/ping")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetPingList")
	@RestLog(action=LogAction.Get, target = LogTarget.Ping, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PingMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPingList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getPingList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_PING, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_PING);
		}
		List<PingMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			PingMonitorInfoResponse dto = new PingMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * サービス・ポート監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/serviceport")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetPortList")
	@RestLog(action=LogAction.Get, target = LogTarget.Serviceport, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ServiceportMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPortList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getPortList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_PORT, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_PORT);
		}
		List<ServiceportMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			ServiceportMonitorInfoResponse dto = new ServiceportMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * プロセス監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/process")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetProcessList")
	@RestLog(action=LogAction.Get, target = LogTarget.Process, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProcessMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProcessList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getProcessList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_PROCESS, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_PROCESS);
		}
		List<ProcessMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			ProcessMonitorInfoResponse dto = new ProcessMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * SNMPトラップ監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/snmptrap")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetSnmptrapList")
	@RestLog(action=LogAction.Get, target = LogTarget.Snmptrap, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SnmptrapMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSnmptrapList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getSnmptrapList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_SNMPTRAP, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_SNMPTRAP);
		}
		List<SnmptrapMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			SnmptrapMonitorInfoResponse dto = new SnmptrapMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * SNMP監視（数値）一覧の取得を行うAPI
	 */
	@GET
	@Path("/snmpNumeric")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetSnmpNumericList")
	@RestLog(action=LogAction.Get, target = LogTarget.SnmpNumeric, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SnmpNumericMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSnmpNumericList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getSnmpNumericList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_SNMP_N, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_SNMP_N);
		}
		List<SnmpNumericMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			SnmpNumericMonitorInfoResponse dto = new SnmpNumericMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * SNMP監視（文字列）一覧の取得を行うAPI
	 */
	@GET
	@Path("/snmpString")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetSnmpStringList")
	@RestLog(action=LogAction.Get, target = LogTarget.SnmpString, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SnmpStringMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSnmpStringList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getSnmpStringList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_SNMP_S, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_SNMP_S);
		}
		List<SnmpStringMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			SnmpStringMonitorInfoResponse dto = new SnmpStringMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * SQL監視（数値）一覧の取得を行うAPI
	 */
	@GET
	@Path("/sqlNumeric")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetSqlNumericList")
	@RestLog(action=LogAction.Get, target = LogTarget.SqlNumeric, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SqlNumericMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSqlNumericList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getSqlNumericList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_SQL_N, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_SQL_N);
		}
		List<SqlNumericMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			SqlNumericMonitorInfoResponse dto = new SqlNumericMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * SQL監視（文字列）一覧の取得を行うAPI
	 */
	@GET
	@Path("/sqlString")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetSqlStringList")
	@RestLog(action=LogAction.Get, target = LogTarget.SqlString, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SqlStringMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSqlStringList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getSqlStringList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_SQL_S, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_SQL_S);
		}
		List<SqlStringMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			SqlStringMonitorInfoResponse dto = new SqlStringMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * システムログ監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/systemlog")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetSystemlogList")
	@RestLog(action=LogAction.Get, target = LogTarget.Systemlog, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SystemlogMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSystemlogList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getSystemlogList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_SYSTEMLOG, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_SYSTEMLOG);
		}
		List<SystemlogMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			SystemlogMonitorInfoResponse dto = new SystemlogMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * カスタム監視（数値）一覧の取得を行うAPI
	 */
	@GET
	@Path("/customNumeric")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCustomNumericList")
	@RestLog(action=LogAction.Get, target = LogTarget.CustomNumeric, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CustomNumericMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomNumericList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getCustomNumericList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_CUSTOM_N, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_CUSTOM_N);
		}
		List<CustomNumericMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			CustomNumericMonitorInfoResponse dto = new CustomNumericMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * カスタム監視（文字列）一覧の取得を行うAPI
	 */
	@GET
	@Path("/customString")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCustomStringList")
	@RestLog(action=LogAction.Get, target = LogTarget.CustomString, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CustomStringMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomStringList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getCustomStringList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_CUSTOM_S, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_CUSTOM_S);
		}
		List<CustomStringMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			CustomStringMonitorInfoResponse dto = new CustomStringMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * RPAログファイル監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/rpalogfile")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaLogfileList")
	@RestLog(action=LogAction.Get, target=LogTarget.RpaLogfile, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaLogfileMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getRpaLogfileList(@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getRpaLogfileList()");

		List<MonitorInfo> infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_RPA_LOGFILE);
		List<RpaLogfileMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			RpaLogfileMonitorInfoResponse dto = new RpaLogfileMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * RPA管理ツール監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/rpaManagementTool")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaManagementToolList")
	@RestLog(action=LogAction.Get, target=LogTarget.RpaToolService, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaManagementToolMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getRpaManagementToolList(@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getRpaManagementToolList()");

		List<MonitorInfo> infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_RPA_MGMT_TOOL_SERVICE);
		List<RpaManagementToolMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			RpaManagementToolMonitorInfoResponse dto = new RpaManagementToolMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	
	/**
	 * Windowsサービス監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/winservice")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetWinServiceList")
	@RestLog(action=LogAction.Get, target = LogTarget.Winservice, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = WinserviceMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWinServiceList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getWinServiceList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_WINSERVICE, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_WINSERVICE);
		}
		List<WinserviceMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			WinserviceMonitorInfoResponse dto = new WinserviceMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * Windowsイベント監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/winevent")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetWinEventList")
	@RestLog(action=LogAction.Get, target = LogTarget.Winevent, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = WineventMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWinEventList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getWinEventList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_WINEVENT, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_WINEVENT);
		}
		List<WineventMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			WineventMonitorInfoResponse dto = new WineventMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * カスタムトラップ監視（数値）一覧の取得を行うAPI
	 */
	@GET
	@Path("/customtrapNumeric")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCustomtrapNumericList")
	@RestLog(action=LogAction.Get, target = LogTarget.CustomtrapNumeric, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CustomtrapNumericMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomtrapNumericList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getCustomtrapNumericList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N);
		}
		List<CustomtrapNumericMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			CustomtrapNumericMonitorInfoResponse dto = new CustomtrapNumericMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * カスタムトラップ監視（文字列）一覧の取得を行うAPI
	 */
	@GET
	@Path("/customtrapString")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCustomtrapStringList")
	@RestLog(action=LogAction.Get, target = LogTarget.CustomtrapNumeric, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CustomtrapStringMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomtrapStringList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getCustomtrapStringList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S);
		}
		List<CustomtrapStringMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			CustomtrapStringMonitorInfoResponse dto = new CustomtrapStringMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 文字列・トラップ監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/monitor_stringAndTrap")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetStringAndTrapMonitorInfoList")
	@RestLog(action=LogAction.Get, target = LogTarget.Monitor, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MonitorInfoBeanResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStringAndTrapMonitorInfoList(
			@QueryParam("facilityId") String facilityId,
			@QueryParam("ownerRoleId") String ownerRoleId,
			@Context Request request,
			@Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getStringAndTrapMonitorInfoList()");

		// カレントユーザがオーナーロールに所属しているかチェックする
		CommonValidator.validateCurrentUserBelongRole(ownerRoleId);
		
		List<MonitorInfoBeanResponse> dtoResList = new ArrayList<>();
		if (facilityId != null && !facilityId.isEmpty()) {
			
			List<MonitorInfo> infoResList = new MonitorSettingControllerBean().getMonitorListForLogcount(facilityId, ownerRoleId);
			for (MonitorInfo info : infoResList) {
				MonitorInfoBeanResponse dto = new MonitorInfoBeanResponse();
				RestBeanUtil.convertBeanNoInvalid(info, dto);
				dtoResList.add(dto);
			}
	
			RestLanguageConverter.convertMessages(dtoResList);
		}

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * クラウドサービス監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/cloudservice")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCloudServiceList")
	@RestLog(action=LogAction.Get, target = LogTarget.Cloudservice, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudserviceMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getCloudServiceList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getCloudServiceList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_CLOUD_SERVICE_CONDITION, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_CLOUD_SERVICE_CONDITION);
		}
		List<CloudserviceMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			CloudserviceMonitorInfoResponse dto = new CloudserviceMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * クラウド課金監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/cloudservicebilling")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCloudserviceBillingList")
	@RestLog(action=LogAction.Get, target = LogTarget.Cloudservicebilling, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudserviceBillingMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getCloudserviceBillingList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getCloudserviceBillingList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_CLOUD_SERVICE_BILLING, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_CLOUD_SERVICE_BILLING);
		}
		List<CloudserviceBillingMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			CloudserviceBillingMonitorInfoResponse dto = new CloudserviceBillingMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * クラウド課金詳細監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/cloudservicebillingdetail")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCloudserviceBillingDetailList")
	@RestLog(action=LogAction.Get, target = LogTarget.Cloudservicebillingdetail, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudserviceBillingDetailMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getCloudserviceBillingDetailList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getCloudserviceBillingDetailList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_CLOUD_SERVICE_BILLING_DETAIL, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_CLOUD_SERVICE_BILLING_DETAIL);
		}
		List<CloudserviceBillingDetailMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			CloudserviceBillingDetailMonitorInfoResponse dto = new CloudserviceBillingDetailMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * クラウドログ監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/cloudlog")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCloudLogList")
	@RestLog(action=LogAction.Get, target = LogTarget.CloudLog, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudLogMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getCloudLogList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getCloudLogList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_CLOUD_LOG, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_CLOUD_LOG);
		}
		List<CloudLogMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			CloudLogMonitorInfoResponse dto = new CloudLogMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * バイナリファイル監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/binaryfile")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetBinaryFileList")
	@RestLog(action=LogAction.Get, target = LogTarget.Binaryfile, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = BinaryfileMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBinaryFileList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getBinaryFileList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_BINARYFILE_BIN, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_BINARYFILE_BIN);
		}
		List<BinaryfileMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			BinaryfileMonitorInfoResponse dto = new BinaryfileMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * パケットキャプチャ監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/packetcapture")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetPacketCaptureList")
	@RestLog(action=LogAction.Get, target = LogTarget.PacketCapture, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PacketcaptureMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPacketCaptureList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getPacketCaptureList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_PCAP_BIN, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_PCAP_BIN);
		}
		List<PacketcaptureMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			PacketcaptureMonitorInfoResponse dto = new PacketcaptureMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 相関係数監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/correlation")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCorrelationList")
	@RestLog(action=LogAction.Get, target = LogTarget.Correlation, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CorrelationMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCorrelationList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getCorrelationList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_CORRELATION, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_CORRELATION);
		}
		List<CorrelationMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			CorrelationMonitorInfoResponse dto = new CorrelationMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 収集値統合監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/integration")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetIntegrationList")
	@RestLog(action=LogAction.Get, target = LogTarget.Integration, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = IntegrationMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getIntegrationList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getIntegrationList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_INTEGRATION, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_INTEGRATION);
		}
		List<IntegrationMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			IntegrationMonitorInfoResponse dto = new IntegrationMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * ログ件数監視一覧の取得を行うAPI
	 */
	@GET
	@Path("/logcount")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetLogcountList")
	@RestLog(action=LogAction.Get, target = LogTarget.Logcount, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = LogcountMonitorInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLogcountList(@QueryParam("monitorId") String monitorId,
			@Context Request request, @Context UriInfo uriInfo)
			throws MonitorNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getLogcountList()");

		List<MonitorInfo> infoResList = null;
		if(monitorId != null) {
			infoResList = Arrays.asList(new MonitorSettingControllerBean().getMonitor(HinemosModuleConstant.MONITOR_LOGCOUNT, monitorId));
		} else {
			infoResList = new MonitorSettingControllerBean().getMonitorList(HinemosModuleConstant.MONITOR_LOGCOUNT);
		}
		List<LogcountMonitorInfoResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			LogcountMonitorInfoResponse dto = new LogcountMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			updateDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * JMX監視項目マスタの追加を行うAPI
	 */
	@POST
	@Path("/jmxmaster")
	@RestLog(action=LogAction.Add, target = LogTarget.Jmxmaster, type = LogType.UPDATE)
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddJmxMasterList")
	@RestSystemAdminPrivilege(isNeed=true)
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JmxMasterInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addJmxMasterList(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addJmxMasterListBody", content = @Content(schema = @Schema(implementation = AddJmxMasterListRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addJmxMasterList()");
	
		AddJmxMasterListRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,AddJmxMasterListRequest.class);
		List<JmxMasterInfo> infoReqList = new ArrayList<>();
		if (dtoReq.getJmxMasterInfoList() != null) {
			for (JmxMasterInfoRequest dto : dtoReq.getJmxMasterInfoList()) {
				JmxMasterInfo infoReq = new JmxMasterInfo();
				RestBeanUtil.convertBean(dto, infoReq);
				infoReqList.add(infoReq);
			}
		}
		List<JmxMasterInfo> infoResList = new JmxMasterControllerBean().addJmxMasterList(infoReqList);
		List<JmxMasterInfoResponse> dtoResList = new ArrayList<>();
		for (JmxMasterInfo info : infoResList) {
			JmxMasterInfoResponse dto = new JmxMasterInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dto.setNameTransrate(info.getName());
			dto.setMeasureTransrate(info.getMeasure());
			dtoResList.add(dto);
		}
	
		RestLanguageConverter.convertMessages(dtoResList);
	
		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * JMX監視項目マスタの全件削除を行うAPI
	 */
	@DELETE
	@Path("/jmxmaster_all")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteJmxMasterAll")
	@RestLog(action=LogAction.Delete, target = LogTarget.Jmxmaster, type = LogType.UPDATE)
	@RestSystemAdminPrivilege(isNeed=true)
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JmxMasterInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteJmxMasterAll(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call deleteJmxMasterAll()");

		List<JmxMasterInfo> infoResList = new JmxMasterControllerBean().deleteJmxMasterAll();
		List<JmxMasterInfoResponse> dtoResList = new ArrayList<>();
		for (JmxMasterInfo info : infoResList) {
			JmxMasterInfoResponse dto = new JmxMasterInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dto.setNameTransrate(info.getName());
			dto.setMeasureTransrate(info.getMeasure());
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * JMX監視に使用するURLのフォーマットのリスト取得を行うAPI
	 */
	@GET
	@Path("/jmx/jmxFormat")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJmxUrlFormatList")
	@RestLog(action=LogAction.Get, target=LogTarget.Jmx, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JmxUrlFormatInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJmxUrlFormatList(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJmxFormatList()");
		
		List<JmxUrlFormatInfo> infoResList = new JmxUrlFormatUtil().getJmxFormats();
		List<JmxUrlFormatInfoResponse> dtoResList = new ArrayList<>();
		for (JmxUrlFormatInfo info : infoResList) {
			JmxUrlFormatInfoResponse dto = new JmxUrlFormatInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}
		
		RestLanguageConverter.convertMessages(dtoResList);
		
		return Response.status(Status.OK).entity(dtoResList).build();

	}

	/**
	 * JMX監視項目マスタの削除を行うAPI
	 */
	@DELETE
	@Path("/jmxmaster")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteJmxMaster")
	@RestLog(action=LogAction.Delete, target = LogTarget.Jmxmaster, type = LogType.UPDATE)
	@RestSystemAdminPrivilege(isNeed=true)
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JmxMasterInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteJmxMaster(
			@ArrayTypeParam @QueryParam(value = "jmxMasterIds") String jmxMasterIds,
			@Context Request request,
			@Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call deleteJmxMaster()");

		List<String> jmxMasterIdList = new ArrayList<>();
		if(jmxMasterIds != null && !jmxMasterIds.isEmpty()) {
			jmxMasterIdList = Arrays.asList(jmxMasterIds.split(","));
		}
		
		List<JmxMasterInfo> infoResList = new JmxMasterControllerBean().deleteJmxMasterList(jmxMasterIdList);
		List<JmxMasterInfoResponse> dtoResList = new ArrayList<>();
		for (JmxMasterInfo info : infoResList) {
			JmxMasterInfoResponse dto = new JmxMasterInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dto.setNameTransrate(info.getName());
			dto.setMeasureTransrate(info.getMeasure());
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * JMX監視項目マスタの取得を行うAPI
	 */
	@GET
	@Path("/jmxmaster_all")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJmxMasterInfoList")
	@RestLog(action=LogAction.Get, target = LogTarget.Jmxmaster, type = LogType.REFERENCE)
	@RestSystemAdminPrivilege(isNeed = true)
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JmxMasterInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJmxMasterInfoList(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJmxMasterInfoList()");

		List<JmxMasterInfo> infoResList = new JmxMasterControllerBean().getJmxMasterList();
		List<JmxMasterInfoResponse> dtoResList = new ArrayList<>();
		for (JmxMasterInfo info : infoResList) {
			JmxMasterInfoResponse dto = new JmxMasterInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dto.setNameTransrate(info.getName());
			dto.setMeasureTransrate(info.getMeasure());
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * JMX監視への設定対象である監視項目一覧の取得を行うAPI
	 */
	@GET
	@Path("/jmxmonitoritem_all")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJmxMonitorItemList")
	@RestLog(action = LogAction.Get, target = LogTarget.Jmxmaster, type = LogType.REFERENCE)
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JmxMasterInfoResponseP1.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJmxMonitorItemList(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJmxMonitorItemList()");

		List<JmxMasterInfo> infoResList = new JmxMasterControllerBean().getJmxMasterList();
		List<JmxMasterInfoResponseP1> dtoResList = new ArrayList<>();
		for (JmxMasterInfo info : infoResList) {
			JmxMasterInfoResponseP1 dto = new JmxMasterInfoResponseP1();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 監視ジョブへの設定対象である監視設定一覧の取得を行うAPI
	 */
	@GET
	@Path("/monitor_withoutCheckInfo_forJob")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMonitorListForJobMonitor")
	@RestLog(action=LogAction.Get, target = LogTarget.Monitor, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorSetting,modeList={SystemPrivilegeMode.READ})
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MonitorInfoBeanResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getMonitorListForJobMonitor(
			@QueryParam("ownerRoleId") String ownerRoleId,
			@Context Request request,
			@Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getMonitorListForJobMonitor()");

		List<MonitorInfo> infoResList = new JobControllerBean().getMonitorListForJobMonitor(ownerRoleId);
		List<MonitorInfoBeanResponse> dtoResList = new ArrayList<>();
		for (MonitorInfo info : infoResList) {
			MonitorInfoBeanResponse dto = new MonitorInfoBeanResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * バイナリ監視詳細設定の取得を行うAPI
	 */
	@GET
	@Path("/binaryCheckInfo")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetBinaryPresetList")
	@RestLog(action=LogAction.Get, target = LogTarget.BinaryCheckInfo, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.Hub,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = BinaryCheckInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBinaryPresetList(@Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		m_log.info("call getBinaryPresetList()");

		List<BinaryCheckInfo> infoResList = new BinaryControllerBean().getPresetList();
		List<BinaryCheckInfoResponse> dtoResList = new ArrayList<>();
		for (BinaryCheckInfo info : infoResList) {
			BinaryCheckInfoResponse dto = new BinaryCheckInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 監視情報の更新
	 * 
	 * @param srcInfo
	 * @param destInfo
	 */
	private void updateDto(MonitorInfo srcInfo, AbstractMonitorResponse destInfo) {

		// カスタム監視情報の更新
		if (srcInfo != null 
				&& srcInfo.getCustomCheckInfo() != null
				&& srcInfo.getCustomCheckInfo().getCommandExecType() != null
				&& (destInfo instanceof CustomNumericMonitorInfoResponse
					|| destInfo instanceof CustomStringMonitorInfoResponse
					|| destInfo instanceof MonitorInfoResponse
					|| destInfo instanceof MonitorInfoResponseP1)) {
			CustomCheckInfoResponse checkInfo = null;
			if (destInfo instanceof CustomNumericMonitorInfoResponse) {
				checkInfo = ((CustomNumericMonitorInfoResponse)destInfo).getCustomCheckInfo();
			} else if (destInfo instanceof CustomStringMonitorInfoResponse) {
				checkInfo = ((CustomStringMonitorInfoResponse)destInfo).getCustomCheckInfo();
			} else if (destInfo instanceof MonitorInfoResponse) {
				checkInfo = ((MonitorInfoResponse)destInfo).getCustomCheckInfo();
			} else if (destInfo instanceof MonitorInfoResponseP1) {
				checkInfo = ((MonitorInfoResponseP1)destInfo).getCustomCheckInfo();
			}
			switch (srcInfo.getCustomCheckInfo().getCommandExecType()) {
			case INDIVIDUAL:
				checkInfo.setCommandExecTypeCode(CommandExecTypeEnum.INDIVIDUAL);
				break;
			case SELECTED:
				checkInfo.setCommandExecTypeCode(CommandExecTypeEnum.SELECTED);
				break;
			}
		}
	}

	/**
	 * 監視情報の更新
	 * 
	 * @param srcInfo
	 * @param destInfo
	 * @throws InvalidSetting 
	 */
	public static void updateInfo(AbstractMonitorRequest srcInfo, MonitorInfo destInfo) throws InvalidSetting {

		// 監視種別
		if (srcInfo instanceof AbstractAddTruthMonitorRequest || srcInfo instanceof AbstractModifyTruthMonitorRequest) {
			destInfo.setMonitorType(MonitorTypeConstant.TYPE_TRUTH);

		} else if (srcInfo instanceof AbstractAddNumericMonitorRequest || srcInfo instanceof AbstractModifyNumericMonitorRequest) {
			destInfo.setMonitorType(MonitorTypeConstant.TYPE_NUMERIC);

		} else if (srcInfo instanceof AbstractAddStringMonitorRequest || srcInfo instanceof AbstractModifyStringMonitorRequest) {
			destInfo.setMonitorType(MonitorTypeConstant.TYPE_STRING);

		} else if (srcInfo instanceof AddSnmptrapMonitorRequest || srcInfo instanceof ModifySnmptrapMonitorRequest) {
			destInfo.setMonitorType(MonitorTypeConstant.TYPE_TRAP);

		} else if (srcInfo instanceof AddHttpScenarioMonitorRequest || srcInfo instanceof ModifyHttpScenarioMonitorRequest) {
			destInfo.setMonitorType(MonitorTypeConstant.TYPE_SCENARIO);

		} else if (srcInfo instanceof AddBinaryfileMonitorRequest
				|| srcInfo instanceof ModifyBinaryfileMonitorRequest
				|| srcInfo instanceof AddPacketcaptureMonitorRequest
				|| srcInfo instanceof ModifyPacketcaptureMonitorRequest) {
			destInfo.setMonitorType(MonitorTypeConstant.TYPE_BINARY);
		}

		// 監視種別ID
		destInfo.setMonitorTypeId(monitorTypeIdMap.get(srcInfo.getClass().getSimpleName()));

		// 失敗時の重要度
		destInfo.setFailurePriority(PriorityConstant.TYPE_UNKNOWN);

		// カスタム監視情報
		if (srcInfo instanceof AddCustomNumericMonitorRequest
				|| srcInfo instanceof AddCustomStringMonitorRequest
				|| srcInfo instanceof ModifyCustomNumericMonitorRequest
				|| srcInfo instanceof ModifyCustomStringMonitorRequest) {
			CustomCheckInfoRequest checkInfo = null;
			if (srcInfo instanceof AddCustomNumericMonitorRequest) {
				checkInfo = ((AddCustomNumericMonitorRequest)srcInfo).getCustomCheckInfo();
			} else if (srcInfo instanceof AddCustomStringMonitorRequest) {
				checkInfo = ((AddCustomStringMonitorRequest)srcInfo).getCustomCheckInfo();
			} else if (srcInfo instanceof ModifyCustomNumericMonitorRequest) {
				checkInfo = ((ModifyCustomNumericMonitorRequest)srcInfo).getCustomCheckInfo();
			} else if (srcInfo instanceof ModifyCustomStringMonitorRequest) {
				checkInfo = ((ModifyCustomStringMonitorRequest)srcInfo).getCustomCheckInfo();
			}
			if (checkInfo != null && checkInfo.getCommandExecTypeCode() != null) {
				switch (checkInfo.getCommandExecTypeCode()) {
				case INDIVIDUAL:
					destInfo.getCustomCheckInfo().setCommandExecType(CommandExecType.INDIVIDUAL);
					break;
				case SELECTED:
					destInfo.getCustomCheckInfo().setCommandExecType(CommandExecType.SELECTED);
					break;
				}
			}
		}

		// Windowsイベント監視情報
		if (srcInfo instanceof AddWineventMonitorRequest
				|| srcInfo instanceof ModifyWineventMonitorRequest) {
			if (destInfo.getWinEventCheckInfo() != null) {
				destInfo.getWinEventCheckInfo().setMonitorId(destInfo.getMonitorId());
				destInfo.getWinEventCheckInfo().reflect();
			}
		}

		// JMX監視情報
		// 収集値表示名と収集値単位に、監視項目を基に取得した値を上書きする
		if (srcInfo instanceof AddJmxMonitorRequest || srcInfo instanceof ModifyJmxMonitorRequest) {
			JmxCheckInfoRequest checkInfo = null;
			if (srcInfo instanceof AddJmxMonitorRequest) {
				checkInfo = ((AddJmxMonitorRequest) srcInfo).getJmxCheckInfo();
			} else if (srcInfo instanceof ModifyJmxMonitorRequest) {
				checkInfo = ((ModifyJmxMonitorRequest) srcInfo).getJmxCheckInfo();
			}
			if (checkInfo != null && checkInfo.getMasterId() != null && !checkInfo.getMasterId().isEmpty()) {
				try {
					JmxMasterInfo jmxMasterInfo = com.clustercontrol.jmx.util.QueryUtil
							.getJmxMasterInfoPK(checkInfo.getMasterId());
					destInfo.setItemName(jmxMasterInfo.getName());
					destInfo.setMeasure(jmxMasterInfo.getMeasure());
				} catch (MonitorNotFound e) {
					InvalidSetting invalidSetting = new InvalidSetting("Item code is invalid. monitorId = " + destInfo.getMonitorId() +", Item code = "+ checkInfo.getMasterId() );
					m_log.info("updateInfo() : "
							+ invalidSetting.getClass().getSimpleName() + ", " + invalidSetting.getMessage());
					throw invalidSetting;
				}
			}
		}

		// リソース監視情報
		// 収集値表示名と収集値単位に、監視項目を基に取得した値を上書きする
		if (srcInfo instanceof AddPerformanceMonitorRequest || srcInfo instanceof ModifyPerformanceMonitorRequest) {
			PerfCheckInfoRequest checkInfo = null;
			if (srcInfo instanceof AddPerformanceMonitorRequest) {
				checkInfo = ((AddPerformanceMonitorRequest) srcInfo).getPerfCheckInfo();
			} else if (srcInfo instanceof ModifyPerformanceMonitorRequest) {
				checkInfo = ((ModifyPerformanceMonitorRequest) srcInfo).getPerfCheckInfo();
			}
			if (checkInfo != null && checkInfo.getItemCode() != null && !checkInfo.getItemCode().isEmpty()) {
				try {
					CollectorItemCodeMstEntity collectorItemCodeMstEntity = com.clustercontrol.performance.monitor.util.QueryUtil
							.getCollectorItemCodeMstPK(checkInfo.getItemCode());
					destInfo.setItemName(collectorItemCodeMstEntity.getItemName());
					destInfo.setMeasure(collectorItemCodeMstEntity.getMeasure());
				} catch (CollectorNotFound e) {
					InvalidSetting invalidSetting = new InvalidSetting("Item code is invalid. monitorId = " + destInfo.getMonitorId() +", Item code = "+ checkInfo.getItemCode() );
					m_log.info("updateInfo() : "
							+ invalidSetting.getClass().getSimpleName() + ", " + invalidSetting.getMessage());
					throw invalidSetting;
				}
			}
		}
	}
	
	@GET
	@Path("/monitor_string_tag/{monitorId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMonitorStringTagList")
	@RestLog(action=LogAction.Get, target = LogTarget.Monitor, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorSetting, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetMonitorStringTagListResponse.class, type=SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMonitorStringTagList(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("monitorId") String monitorId,
			@QueryParam("ownerRoleId") String ownerRoleId) throws InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.info("call getMonitorStringTagList()");
		
		// カレントユーザがオーナーロールに所属しているかチェックする
		CommonValidator.validateCurrentUserBelongRole(ownerRoleId);
		
		List<GetMonitorStringTagListResponse> dtoResList = new ArrayList<>();
		
		
		List<String> tagList = new MonitorSettingControllerBean().getMonitorStringTagList(monitorId, ownerRoleId);
		for (String tag : tagList) {
			GetMonitorStringTagListResponse dtoRes = new GetMonitorStringTagListResponse();
			dtoRes.setKey(tag);
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);
		

		return Response.status(Status.OK).entity(dtoResList).build();
	}
}
