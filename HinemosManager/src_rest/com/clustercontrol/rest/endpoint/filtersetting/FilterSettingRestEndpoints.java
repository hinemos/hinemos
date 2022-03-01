/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.filtersetting;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_404;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_409;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeFunction;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.FilterSettingDuplicate;
import com.clustercontrol.fault.FilterSettingNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.filtersetting.bean.FilterSettingConstant;
import com.clustercontrol.filtersetting.bean.FilterSettingInfo;
import com.clustercontrol.filtersetting.bean.FilterSettingSearchPattern;
import com.clustercontrol.filtersetting.bean.FilterSettingSummaryInfo;
import com.clustercontrol.filtersetting.session.FilterSettingControllerBean;
import com.clustercontrol.rest.annotation.RestLog;
import com.clustercontrol.rest.annotation.RestLog.LogAction;
import com.clustercontrol.rest.annotation.RestLog.LogTarget;
import com.clustercontrol.rest.annotation.RestLog.LogType;
import com.clustercontrol.rest.annotation.RestLogFunc;
import com.clustercontrol.rest.annotation.RestLogFunc.LogFuncName;
import com.clustercontrol.rest.endpoint.filtersetting.dto.AddEventFilterSettingRequest;
import com.clustercontrol.rest.endpoint.filtersetting.dto.AddJobHistoryFilterSettingRequest;
import com.clustercontrol.rest.endpoint.filtersetting.dto.AddStatusFilterSettingRequest;
import com.clustercontrol.rest.endpoint.filtersetting.dto.EventFilterSettingResponse;
import com.clustercontrol.rest.endpoint.filtersetting.dto.FilterSettingSummariesResponse;
import com.clustercontrol.rest.endpoint.filtersetting.dto.FilterSettingSummaryResponse;
import com.clustercontrol.rest.endpoint.filtersetting.dto.JobHistoryFilterSettingResponse;
import com.clustercontrol.rest.endpoint.filtersetting.dto.ModifyEventFilterSettingRequest;
import com.clustercontrol.rest.endpoint.filtersetting.dto.ModifyJobHistoryFilterSettingRequest;
import com.clustercontrol.rest.endpoint.filtersetting.dto.ModifyStatusFilterSettingRequest;
import com.clustercontrol.rest.endpoint.filtersetting.dto.StatusFilterSettingResponse;
import com.clustercontrol.rest.annotation.RestSystemAdminPrivilege;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.util.MessageConstant;;

@Path("/filtersetting")
@RestLogFunc(name = LogFuncName.FilterSetting)
public class FilterSettingRestEndpoints {

	private static Log m_log = LogFactory.getLog(FilterSettingRestEndpoints.class);
	private static final String ENDPOINT_OPERATION_ID_PREFIX = "filtersetting";

	private final FilterSettingControllerBean controller;

	public FilterSettingRestEndpoints() {
		this(new FilterSettingControllerBean());
	}

	/** テスト用 */
	public FilterSettingRestEndpoints(FilterSettingControllerBean controller) {
		this.controller = controller;
	}

	@GET
	@Path("/{category:(event|status|job_history)}/common")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCommonFilterSettingSummaries")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FilterSettingSummariesResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.FilterSetting_Common, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.FilterSetting, modeList = { SystemPrivilegeMode.READ })
	public Response getCommonFilterSettingSummaries(
			@PathParam("category") String prmCategory,
			@QueryParam("pattern") String prmPattern)
			throws HinemosUnknown, InvalidSetting {
		m_log.debug("call getCommonFilterSettingSummaries()");

		// Request変換
		FilterCategoryEnum category = validateCategoryPathParam(prmCategory);
		FilterSettingSearchPattern pattern = validateSearchPattern(prmPattern);

		// メイン処理
		List<FilterSettingSummaryInfo> result = controller.searchCommonFilterSettings(category, pattern);

		// Response変換
		FilterSettingSummariesResponse res = convertQueryResultResponse(result);
		RestLanguageConverter.convertMessages(res);

		return Response.status(Status.OK).entity(res).build();
	}

	@GET
	@Path("/{category:(event|status|job_history)}/user")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetUserFilterSettingSummaries")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FilterSettingSummariesResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.FilterSetting_User, type = LogType.REFERENCE)
	// @RestSystemPrivilege システム権限不要
	public Response getUserFilterSettingSummaries(
			@PathParam("category") String prmCategory,
			@QueryParam("pattern") String prmPattern,
			@QueryParam("userId") String prmUserId)
			throws HinemosUnknown, InvalidSetting, InvalidRole {
		m_log.debug("call getUserFilterSettingSummaries()");

		// Request変換
		FilterCategoryEnum category = validateCategoryPathParam(prmCategory);
		FilterSettingSearchPattern pattern = validateSearchPattern(prmPattern);
		String userId = validateOwnerUserId(prmUserId);

		// メイン処理
		List<FilterSettingSummaryInfo> result = controller.searchUserFilterSettings(category, userId, pattern);

		// Response変換
		FilterSettingSummariesResponse res = convertQueryResultResponse(result);
		RestLanguageConverter.convertMessages(res);

		return Response.status(Status.OK).entity(res).build();
	}

	@GET
	@Path("/{category:(event|status|job_history)}/allusers")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetAllUserFilterSettingSummaries")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FilterSettingSummariesResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.FilterSetting_User, type = LogType.REFERENCE)
	// @RestSystemPrivilege システム権限は不要
	@RestSystemAdminPrivilege(isNeed = true)
	public Response getAllUserFilterSettingSummaries(
			@PathParam("category") String prmCategory,
			@QueryParam("pattern") String prmPattern)
			throws HinemosUnknown, InvalidSetting {
		m_log.debug("call getAllUserFilterSettingSummaries()");

		// Request変換
		FilterCategoryEnum category = validateCategoryPathParam(prmCategory);
		FilterSettingSearchPattern pattern = validateSearchPattern(prmPattern);

		// メイン処理
		List<FilterSettingSummaryInfo> result = controller.searchAllUserFilterSettings(category, pattern);

		// Response変換
		FilterSettingSummariesResponse res = convertQueryResultResponse(result);
		RestLanguageConverter.convertMessages(res);

		return Response.status(Status.OK).entity(res).build();
	}

	@GET
	@Path("/event/common/{filterId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCommonEventFilterSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.FilterSetting_Common, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.FilterSetting, modeList = { SystemPrivilegeMode.READ })
	public Response getCommonEventFilterSetting(
			@PathParam("filterId") String prmFilterId)
			throws HinemosUnknown, FilterSettingNotFound, InvalidSetting, InvalidRole {
		m_log.debug("call getCommonEventFilterSetting()");

		// Request変換
		String filterId = validateFilterId(prmFilterId);

		// メイン処理
		FilterSettingInfo result = controller.getCommonFilterSetting(FilterCategoryEnum.EVENT, filterId);

		// Response変換
		EventFilterSettingResponse rsp = new EventFilterSettingResponse();
		RestBeanUtil.convertBeanNoInvalid(result, rsp);

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}

	@GET
	@Path("/status/common/{filterId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCommonStatusFilterSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StatusFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.FilterSetting_Common, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.FilterSetting, modeList = { SystemPrivilegeMode.READ })
	public Response getCommonStatusFilterSetting(
			@PathParam("filterId") String prmFilterId)
			throws HinemosUnknown, FilterSettingNotFound, InvalidSetting, InvalidRole {
		m_log.debug("call getCommonStatusFilterSetting()");

		// Request変換
		String filterId = validateFilterId(prmFilterId);

		// メイン処理
		FilterSettingInfo result = controller.getCommonFilterSetting(FilterCategoryEnum.STATUS, filterId);

		// Response変換
		StatusFilterSettingResponse rsp = new StatusFilterSettingResponse();
		RestBeanUtil.convertBeanNoInvalid(result, rsp);

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}
	
	@GET
	@Path("/job_history/common/{filterId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCommonJobHistoryFilterSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobHistoryFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.FilterSetting_Common, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.FilterSetting, modeList = { SystemPrivilegeMode.READ })
	public Response getCommonJobHistoryFilterSetting(
			@PathParam("filterId") String prmFilterId)
			throws HinemosUnknown, FilterSettingNotFound, InvalidSetting, InvalidRole {
		m_log.debug("call getCommonJobHistoryFilterSetting()");

		// Request変換
		String filterId = validateFilterId(prmFilterId);

		// メイン処理
		FilterSettingInfo result = controller.getCommonFilterSetting(FilterCategoryEnum.JOB_HISTORY, filterId);

		// Response変換
		JobHistoryFilterSettingResponse rsp = new JobHistoryFilterSettingResponse();
		RestBeanUtil.convertBeanNoInvalid(result, rsp);

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}
	
	@GET
	@Path("/event/user/{filterId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetUserEventFilterSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.FilterSetting_User, type = LogType.REFERENCE)
	// @RestSystemPrivilege システム権限は不要
	public Response getUserEventFilterSetting(
			@PathParam("filterId") String prmFilterId,
			@QueryParam("userId") String prmUserId)  // 管理者のみ指定可能
			throws HinemosUnknown, FilterSettingNotFound, InvalidSetting, InvalidRole {
		m_log.debug("call getUserEventFilterSetting()");

		// Request変換
		String filterId = validateFilterId(prmFilterId);
		String userId = validateOwnerUserId(prmUserId);

		// メイン処理
		FilterSettingInfo result = controller.getUserFilterSetting(FilterCategoryEnum.EVENT, userId, filterId);

		// Response変換
		EventFilterSettingResponse rsp = new EventFilterSettingResponse();
		RestBeanUtil.convertBeanNoInvalid(result, rsp);

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}

	@GET
	@Path("/status/user/{filterId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetUserStatusFilterSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StatusFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.FilterSetting_User, type = LogType.REFERENCE)
	// @RestSystemPrivilege システム権限は不要
	public Response getUserStatusFilterSetting(
			@PathParam("filterId") String prmFilterId,
			@QueryParam("userId") String prmUserId)  // 管理者のみ指定可能
			throws HinemosUnknown, FilterSettingNotFound, InvalidSetting, InvalidRole {
		m_log.debug("call getUserStatusFilterSetting()");

		// Request変換
		String filterId = validateFilterId(prmFilterId);
		String userId = validateOwnerUserId(prmUserId);

		// メイン処理
		FilterSettingInfo result = controller.getUserFilterSetting(FilterCategoryEnum.STATUS, userId, filterId);

		// Response変換
		StatusFilterSettingResponse rsp = new StatusFilterSettingResponse();
		RestBeanUtil.convertBeanNoInvalid(result, rsp);

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}
	
	@GET
	@Path("/job_history/user/{filterId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetUserJobHistoryFilterSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobHistoryFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.FilterSetting_User, type = LogType.REFERENCE)
	// @RestSystemPrivilege システム権限は不要
	public Response getUserJobHistoryFilterSetting(
			@PathParam("filterId") String prmFilterId,
			@QueryParam("userId") String prmUserId)  // 管理者のみ指定可能
			throws HinemosUnknown, FilterSettingNotFound, InvalidSetting, InvalidRole {
		m_log.debug("call getUserJobHistoryFilterSetting()");

		// Request変換
		String filterId = validateFilterId(prmFilterId);
		String userId = validateOwnerUserId(prmUserId);

		// メイン処理
		FilterSettingInfo result = controller.getUserFilterSetting(FilterCategoryEnum.JOB_HISTORY, userId, filterId);

		// Response変換
		JobHistoryFilterSettingResponse rsp = new JobHistoryFilterSettingResponse();
		RestBeanUtil.convertBeanNoInvalid(result, rsp);

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}
	
	@POST
	@Path("/event/common")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCommonEventFilterSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.FilterSetting_Common, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.FilterSetting, modeList = { SystemPrivilegeMode.ADD })
	public Response addCommonEventFilterSetting(
			@RequestBody(
					description = "addCommonEventFilterSettingBody",
					content = @Content(schema = @Schema(
							implementation = AddEventFilterSettingRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidSetting, FilterSettingDuplicate {
		m_log.debug("call addCommonEventFilterSetting()");

		// Request変換
		AddEventFilterSettingRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddEventFilterSettingRequest.class);
		RestCommonValitater.checkRequestDto(req);
		req.correlationCheck();
		req.validate(true);

		FilterSettingInfo info = new FilterSettingInfo();
		RestBeanUtil.convertBean(req, info);
		info.setCommon(true);
		info.setFilterCategory(FilterCategoryEnum.EVENT);
		info.setOwnerUserId(null);

		// メイン処理
		FilterSettingInfo result = controller.addFilterSetting(info);

		// Response変換
		EventFilterSettingResponse rsp = new EventFilterSettingResponse();
		RestBeanUtil.convertBeanNoInvalid(result, rsp);

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}

	
	@POST
	@Path("/status/common")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCommonStatusFilterSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StatusFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.FilterSetting_Common, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.FilterSetting, modeList = { SystemPrivilegeMode.ADD })
	public Response addCommonStatusFilterSetting(
			@RequestBody(
					description = "addCommonStatusFilterSettingBody",
					content = @Content(schema = @Schema(
							implementation = AddStatusFilterSettingRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidSetting, FilterSettingDuplicate {
		m_log.debug("call addCommonStatusFilterSetting()");

		// Request変換
		AddStatusFilterSettingRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddStatusFilterSettingRequest.class);
		RestCommonValitater.checkRequestDto(req);
		req.correlationCheck();
		req.validate(true);

		FilterSettingInfo info = new FilterSettingInfo();
		RestBeanUtil.convertBean(req, info);
		info.setCommon(true);
		info.setFilterCategory(FilterCategoryEnum.STATUS);
		info.setOwnerUserId(null);

		// メイン処理
		FilterSettingInfo result = controller.addFilterSetting(info);

		// Response変換
		StatusFilterSettingResponse rsp = new StatusFilterSettingResponse();
		RestBeanUtil.convertBeanNoInvalid(result, rsp);

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}
	
	@POST
	@Path("/job_history/common")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCommonJobHistoryFilterSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobHistoryFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.FilterSetting_Common, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.FilterSetting, modeList = { SystemPrivilegeMode.ADD })
	public Response addCommonJobHistoryFilterSetting(
			@RequestBody(
					description = "addCommonJobHistoryFilterSettingBody",
					content = @Content(schema = @Schema(
							implementation = AddJobHistoryFilterSettingRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidSetting, FilterSettingDuplicate {
		m_log.debug("call addCommonJobHistoryFilterSetting()");

		// Request変換
		AddJobHistoryFilterSettingRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddJobHistoryFilterSettingRequest.class);
		RestCommonValitater.checkRequestDto(req);
		req.correlationCheck();
		req.validate(true);

		FilterSettingInfo info = new FilterSettingInfo();
		RestBeanUtil.convertBean(req, info);
		info.setCommon(true);
		info.setFilterCategory(FilterCategoryEnum.JOB_HISTORY);
		info.setOwnerUserId(null);

		// メイン処理
		FilterSettingInfo result = controller.addFilterSetting(info);

		// Response変換
		JobHistoryFilterSettingResponse rsp = new JobHistoryFilterSettingResponse();
		RestBeanUtil.convertBeanNoInvalid(result, rsp);

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}
	
	@POST
	@Path("/event/user")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddUserEventFilterSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.FilterSetting_User, type = LogType.UPDATE)
	// @RestSystemPrivilege システム権限は不要
	public Response addUserEventFilterSetting(
			@RequestBody(
					description = "addUserEventFilterSettingBody",
					content = @Content(schema = @Schema(
							implementation = AddEventFilterSettingRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidSetting, FilterSettingDuplicate, InvalidRole {
		m_log.debug("call addUserEventFilterSetting()");

		// Request変換
		AddEventFilterSettingRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddEventFilterSettingRequest.class);
		RestCommonValitater.checkRequestDto(req);
		req.correlationCheck();
		req.validate(false);

		String userId = validateOwnerUserId(req.getOwnerUserId());

		FilterSettingInfo info = new FilterSettingInfo();
		RestBeanUtil.convertBean(req, info);
		info.setCommon(false);
		info.setFilterCategory(FilterCategoryEnum.EVENT);
		info.setOwnerUserId(userId);

		// メイン処理
		FilterSettingInfo result = controller.addFilterSetting(info);

		// Response変換
		EventFilterSettingResponse rsp = new EventFilterSettingResponse();
		RestBeanUtil.convertBeanNoInvalid(result, rsp);

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}

	@POST
	@Path("/status/user")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddUserStatusFilterSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StatusFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.FilterSetting_User, type = LogType.UPDATE)
	// @RestSystemPrivilege システム権限は不要
	public Response addUserStatusFilterSetting(
			@RequestBody(
					description = "addUserStatusFilterSettingBody",
					content = @Content(schema = @Schema(
							implementation = AddStatusFilterSettingRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidSetting, FilterSettingDuplicate, InvalidRole {
		m_log.debug("call addUserStatusFilterSetting()");

		// Request変換
		AddStatusFilterSettingRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddStatusFilterSettingRequest.class);
		RestCommonValitater.checkRequestDto(req);
		req.correlationCheck();
		req.validate(false);

		String userId = validateOwnerUserId(req.getOwnerUserId());

		FilterSettingInfo info = new FilterSettingInfo();
		RestBeanUtil.convertBean(req, info);
		info.setCommon(false);
		info.setFilterCategory(FilterCategoryEnum.STATUS);
		info.setOwnerUserId(userId);

		// メイン処理
		FilterSettingInfo result = controller.addFilterSetting(info);

		// Response変換
		StatusFilterSettingResponse rsp = new StatusFilterSettingResponse();
		RestBeanUtil.convertBeanNoInvalid(result, rsp);

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}
	
	@POST
	@Path("job_history/user")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddUserJobHistoryFilterSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobHistoryFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.FilterSetting_User, type = LogType.UPDATE)
	// @RestSystemPrivilege システム権限は不要
	public Response addUserJobHistoryFilterSetting(
			@RequestBody(
					description = "addUserJobHistoryFilterSettingBody",
					content = @Content(schema = @Schema(
							implementation = AddJobHistoryFilterSettingRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidSetting, FilterSettingDuplicate, InvalidRole {
		m_log.debug("call addUserJobHistoryFilterSetting()");

		// Request変換
		AddJobHistoryFilterSettingRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddJobHistoryFilterSettingRequest.class);
		RestCommonValitater.checkRequestDto(req);
		req.correlationCheck();
		req.validate(false);

		String userId = validateOwnerUserId(req.getOwnerUserId());

		FilterSettingInfo info = new FilterSettingInfo();
		RestBeanUtil.convertBean(req, info);
		info.setCommon(false);
		info.setFilterCategory(FilterCategoryEnum.JOB_HISTORY);
		info.setOwnerUserId(userId);

		// メイン処理
		FilterSettingInfo result = controller.addFilterSetting(info);

		// Response変換
		JobHistoryFilterSettingResponse rsp = new JobHistoryFilterSettingResponse();
		RestBeanUtil.convertBeanNoInvalid(result, rsp);

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}
	
	@PUT
	@Path("/event/common/{filterId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCommonEventFilterSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.FilterSetting_Common, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.FilterSetting, modeList = { SystemPrivilegeMode.MODIFY })
	public Response modifyCommonEventFilterSetting(
			@PathParam("filterId") String prmFilterId,
			@RequestBody(
					description = "modifyCommonEventFilterSettingBody",
					content = @Content(schema = @Schema(
							implementation = ModifyEventFilterSettingRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidSetting, FilterSettingNotFound, InvalidRole {
		m_log.debug("call modifyCommonEventFilterSetting()");

		// Request変換
		String filterId = validateFilterId(prmFilterId);

		ModifyEventFilterSettingRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyEventFilterSettingRequest.class);
		RestCommonValitater.checkRequestDto(req);
		req.correlationCheck();
		req.validate(true);

		FilterSettingInfo info = new FilterSettingInfo();
		RestBeanUtil.convertBean(req, info);
		info.setFilterId(filterId);
		info.setCommon(true);
		info.setFilterCategory(FilterCategoryEnum.EVENT);
		info.setOwnerUserId(null);

		// メイン処理
		FilterSettingInfo result = controller.modifyFilterSetting(info);

		// Response変換
		EventFilterSettingResponse rsp = new EventFilterSettingResponse();
		RestBeanUtil.convertBeanNoInvalid(result, rsp);

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}

	@PUT
	@Path("/status/common/{filterId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCommonStatusFilterSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StatusFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.FilterSetting_Common, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.FilterSetting, modeList = { SystemPrivilegeMode.MODIFY })
	public Response modifyCommonStatusFilterSetting(
			@PathParam("filterId") String prmFilterId,
			@RequestBody(
					description = "modifyCommonStatusFilterSettingBody",
					content = @Content(schema = @Schema(
							implementation = ModifyStatusFilterSettingRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidSetting, FilterSettingNotFound, InvalidRole {
		m_log.debug("call modifyCommonStatusFilterSetting()");

		// Request変換
		String filterId = validateFilterId(prmFilterId);

		ModifyStatusFilterSettingRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyStatusFilterSettingRequest.class);
		RestCommonValitater.checkRequestDto(req);
		req.correlationCheck();
		req.validate(true);

		FilterSettingInfo info = new FilterSettingInfo();
		RestBeanUtil.convertBean(req, info);
		info.setFilterId(filterId);
		info.setCommon(true);
		info.setFilterCategory(FilterCategoryEnum.STATUS);
		info.setOwnerUserId(null);

		// メイン処理
		FilterSettingInfo result = controller.modifyFilterSetting(info);

		// Response変換
		StatusFilterSettingResponse rsp = new StatusFilterSettingResponse();
		RestBeanUtil.convertBeanNoInvalid(result, rsp);

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}
	
	@PUT
	@Path("/job_history/common/{filterId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCommonJobHistoryFilterSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobHistoryFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.FilterSetting_Common, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.FilterSetting, modeList = { SystemPrivilegeMode.MODIFY })
	public Response modifyCommonJobHistoryFilterSetting(
			@PathParam("filterId") String prmFilterId,
			@RequestBody(
					description = "modifyCommonJobHistoryFilterSettingBody",
					content = @Content(schema = @Schema(
							implementation = ModifyJobHistoryFilterSettingRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidSetting, FilterSettingNotFound, InvalidRole {
		m_log.debug("call modifyCommonJobHistoryFilterSetting()");

		// Request変換
		String filterId = validateFilterId(prmFilterId);

		ModifyJobHistoryFilterSettingRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyJobHistoryFilterSettingRequest.class);
		RestCommonValitater.checkRequestDto(req);
		req.correlationCheck();
		req.validate(true);

		FilterSettingInfo info = new FilterSettingInfo();
		RestBeanUtil.convertBean(req, info);
		info.setFilterId(filterId);
		info.setCommon(true);
		info.setFilterCategory(FilterCategoryEnum.JOB_HISTORY);
		info.setOwnerUserId(null);

		// メイン処理
		FilterSettingInfo result = controller.modifyFilterSetting(info);

		// Response変換
		JobHistoryFilterSettingResponse rsp = new JobHistoryFilterSettingResponse();
		RestBeanUtil.convertBeanNoInvalid(result, rsp);

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}
	
	@PUT
	@Path("/event/user/{filterId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyUserEventFilterSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.FilterSetting_User, type = LogType.UPDATE)
	// @RestSystemPrivilege システム権限は不要
	public Response modifyUserEventFilterSetting(
			@PathParam("filterId") String prmFilterId,
			@RequestBody(
					description = "modifyUserEventFilterSettingBody",
					content = @Content(schema = @Schema(
							implementation = ModifyEventFilterSettingRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidSetting, FilterSettingNotFound, InvalidRole {
		m_log.debug("call modifyUserEventFilterSetting()");

		// Request変換
		String filterId = validateFilterId(prmFilterId);

		ModifyEventFilterSettingRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyEventFilterSettingRequest.class);
		RestCommonValitater.checkRequestDto(req);
		req.correlationCheck();
		req.validate(false);

		String userId = validateOwnerUserId(req.getOwnerUserId());

		FilterSettingInfo info = new FilterSettingInfo();
		RestBeanUtil.convertBean(req, info);
		info.setFilterId(filterId);
		info.setCommon(false);
		info.setFilterCategory(FilterCategoryEnum.EVENT);
		info.setOwnerUserId(userId);

		// メイン処理
		FilterSettingInfo result = controller.modifyFilterSetting(info);

		// Response変換
		EventFilterSettingResponse rsp = new EventFilterSettingResponse();
		RestBeanUtil.convertBeanNoInvalid(result, rsp);

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}

	@PUT
	@Path("/status/user/{filterId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyUserStatusFilterSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StatusFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.FilterSetting_User, type = LogType.UPDATE)
	// @RestSystemPrivilege システム権限は不要
	public Response modifyUserStatusFilterSetting(
			@PathParam("filterId") String prmFilterId,
			@RequestBody(
					description = "modifyUserStatusFilterSettingBody",
					content = @Content(schema = @Schema(
							implementation = ModifyStatusFilterSettingRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidSetting, FilterSettingNotFound, InvalidRole {
		m_log.debug("call modifyUserStatusFilterSetting()");

		// Request変換
		String filterId = validateFilterId(prmFilterId);

		ModifyStatusFilterSettingRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyStatusFilterSettingRequest.class);
		RestCommonValitater.checkRequestDto(req);
		req.correlationCheck();
		req.validate(false);

		String userId = validateOwnerUserId(req.getOwnerUserId());

		FilterSettingInfo info = new FilterSettingInfo();
		RestBeanUtil.convertBean(req, info);
		info.setFilterId(filterId);
		info.setCommon(false);
		info.setFilterCategory(FilterCategoryEnum.STATUS);
		info.setOwnerUserId(userId);

		// メイン処理
		FilterSettingInfo result = controller.modifyFilterSetting(info);

		// Response変換
		StatusFilterSettingResponse rsp = new StatusFilterSettingResponse();
		RestBeanUtil.convertBeanNoInvalid(result, rsp);

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}
	
	@PUT
	@Path("/job_history/user/{filterId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyUserJobHistoryFilterSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobHistoryFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.FilterSetting_User, type = LogType.UPDATE)
	// @RestSystemPrivilege システム権限は不要
	public Response modifyUserJobHistoryFilterSetting(
			@PathParam("filterId") String prmFilterId,
			@RequestBody(
					description = "modifyUserJobHistoryFilterSettingBody",
					content = @Content(schema = @Schema(
							implementation = ModifyJobHistoryFilterSettingRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidSetting, FilterSettingNotFound, InvalidRole {
		m_log.debug("call modifyUserJobHistoryFilterSetting()");

		// Request変換
		String filterId = validateFilterId(prmFilterId);

		ModifyJobHistoryFilterSettingRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyJobHistoryFilterSettingRequest.class);
		RestCommonValitater.checkRequestDto(req);
		req.correlationCheck();
		req.validate(false);

		String userId = validateOwnerUserId(req.getOwnerUserId());

		FilterSettingInfo info = new FilterSettingInfo();
		RestBeanUtil.convertBean(req, info);
		info.setFilterId(filterId);
		info.setCommon(false);
		info.setFilterCategory(FilterCategoryEnum.JOB_HISTORY);
		info.setOwnerUserId(userId);

		// メイン処理
		FilterSettingInfo result = controller.modifyFilterSetting(info);

		// Response変換
		JobHistoryFilterSettingResponse rsp = new JobHistoryFilterSettingResponse();
		RestBeanUtil.convertBeanNoInvalid(result, rsp);

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}
	
	@DELETE
	@Path("/event/common")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteCommonEventFilterSettings")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventFilterSettingResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.FilterSetting_Common, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.FilterSetting, modeList = { SystemPrivilegeMode.MODIFY })
	public Response deleteCommonEventFilterSettings(
			@QueryParam("filterIds") String prmFilterIds)
			throws HinemosUnknown, InvalidSetting, InvalidRole {
		m_log.debug("call deleteCommonEventFilterSettings()");

		// Request変換
		List<String> filterIdList = new ArrayList<>();
		if(prmFilterIds != null && !prmFilterIds.isEmpty()) {
			for (String filterId : prmFilterIds.split(",")) {
				filterIdList.add(validateFilterId(filterId.trim()));
			}
		}

		// メイン処理
		List<FilterSettingInfo> result = controller.deleteCommonFilterSettings(FilterCategoryEnum.EVENT, filterIdList);

		// Response変換
		List<EventFilterSettingResponse> rsp = new ArrayList<>();
		for (FilterSettingInfo src : result) {
			EventFilterSettingResponse dst = new EventFilterSettingResponse();
			RestBeanUtil.convertBeanNoInvalid(src, dst);
			rsp.add(dst);
		}

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}

	@DELETE
	@Path("/status/common")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteCommonStatusFilterSettings")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StatusFilterSettingResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.FilterSetting_Common, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.FilterSetting, modeList = { SystemPrivilegeMode.MODIFY })
	public Response deleteCommonStatusFilterSettings(
			@QueryParam("filterIds") String prmFilterIds)
			throws HinemosUnknown, InvalidSetting, InvalidRole {
		m_log.debug("call deleteCommonStatusFilterSettings()");

		// Request変換
		List<String> filterIdList = new ArrayList<>();
		if(prmFilterIds != null && !prmFilterIds.isEmpty()) {
			for (String filterId : prmFilterIds.split(",")) {
				filterIdList.add(validateFilterId(filterId.trim()));
			}
		}

		// メイン処理
		List<FilterSettingInfo> result = controller.deleteCommonFilterSettings(FilterCategoryEnum.STATUS, filterIdList);

		// Response変換
		List<StatusFilterSettingResponse> rsp = new ArrayList<>();
		for (FilterSettingInfo src : result) {
			StatusFilterSettingResponse dst = new StatusFilterSettingResponse();
			RestBeanUtil.convertBeanNoInvalid(src, dst);
			rsp.add(dst);
		}

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}
	
	@DELETE
	@Path("/job_history/common")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteCommonJobHistoryFilterSettings")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobHistoryFilterSettingResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.FilterSetting_Common, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.FilterSetting, modeList = { SystemPrivilegeMode.MODIFY })
	public Response deleteCommonJobHistoryFilterSettings(
			@QueryParam("filterIds") String prmFilterIds)
			throws HinemosUnknown, InvalidSetting, InvalidRole {
		m_log.debug("call deleteCommonJobHistoryFilterSettings()");

		// Request変換
		List<String> filterIdList = new ArrayList<>();
		if(prmFilterIds != null && !prmFilterIds.isEmpty()) {
			for (String filterId : prmFilterIds.split(",")) {
				filterIdList.add(validateFilterId(filterId.trim()));
			}
		}

		// メイン処理
		List<FilterSettingInfo> result = controller.deleteCommonFilterSettings(FilterCategoryEnum.JOB_HISTORY, filterIdList);

		// Response変換
		List<JobHistoryFilterSettingResponse> rsp = new ArrayList<>();
		for (FilterSettingInfo src : result) {
			JobHistoryFilterSettingResponse dst = new JobHistoryFilterSettingResponse();
			RestBeanUtil.convertBeanNoInvalid(src, dst);
			rsp.add(dst);
		}

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}
	
	@DELETE
	@Path("/event/user")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteUserEventFilterSettings")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventFilterSettingResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.FilterSetting_User, type = LogType.UPDATE)
	// @RestSystemPrivilege システム権限は不要
	public Response deleteUserEventFilterSettings(
			@QueryParam("filterIds") String prmFilterIds,
			@QueryParam("userId") String prmUserId)
			throws HinemosUnknown, InvalidSetting, InvalidRole {
		m_log.debug("call deleteUserEventFilterSettings()");

		// Request変換
		String userId = validateOwnerUserId(prmUserId);

		List<String> filterIdList = new ArrayList<>();
		if(prmFilterIds != null && !prmFilterIds.isEmpty()) {
			for (String filterId : prmFilterIds.split(",")) {
				filterIdList.add(validateFilterId(filterId.trim()));
			}
		}

		// メイン処理
		List<FilterSettingInfo> result = controller.deleteUserFilterSettings(FilterCategoryEnum.EVENT, userId, filterIdList);

		// Response変換
		List<EventFilterSettingResponse> rsp = new ArrayList<>();
		for (FilterSettingInfo src : result) {
			EventFilterSettingResponse dst = new EventFilterSettingResponse();
			RestBeanUtil.convertBeanNoInvalid(src, dst);
			rsp.add(dst);
		}

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}

	@DELETE
	@Path("/status/user")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteUserStatusFilterSettings")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StatusFilterSettingResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.FilterSetting_User, type = LogType.UPDATE)
	// @RestSystemPrivilege システム権限は不要
	public Response deleteUserStatusFilterSettings(
			@QueryParam("filterIds") String prmFilterIds,
			@QueryParam("userId") String prmUserId)
			throws HinemosUnknown, InvalidSetting, InvalidRole {
		m_log.debug("call deleteUserStatusFilterSettings()");

		// Request変換
		String userId = validateOwnerUserId(prmUserId);

		List<String> filterIdList = new ArrayList<>();
		if(prmFilterIds != null && !prmFilterIds.isEmpty()) {
			for (String filterId : prmFilterIds.split(",")) {
				filterIdList.add(validateFilterId(filterId.trim()));
			}
		}

		// メイン処理
		List<FilterSettingInfo> result = controller.deleteUserFilterSettings(FilterCategoryEnum.STATUS, userId, filterIdList);

		// Response変換
		List<StatusFilterSettingResponse> rsp = new ArrayList<>();
		for (FilterSettingInfo src : result) {
			StatusFilterSettingResponse dst = new StatusFilterSettingResponse();
			RestBeanUtil.convertBeanNoInvalid(src, dst);
			rsp.add(dst);
		}

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}
	
	@DELETE
	@Path("/job_history/user")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteUserJobHistoryFilterSettings")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobHistoryFilterSettingResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.FilterSetting_User, type = LogType.UPDATE)
	// @RestSystemPrivilege システム権限は不要
	public Response deleteUserJobHistoryFilterSettings(
			@QueryParam("filterIds") String prmFilterIds,
			@QueryParam("userId") String prmUserId)
			throws HinemosUnknown, InvalidSetting, InvalidRole {
		m_log.debug("call deleteUserJobHistoryFilterSettings()");

		// Request変換
		String userId = validateOwnerUserId(prmUserId);

		List<String> filterIdList = new ArrayList<>();
		if(prmFilterIds != null && !prmFilterIds.isEmpty()) {
			for (String filterId : prmFilterIds.split(",")) {
				filterIdList.add(validateFilterId(filterId.trim()));
			}
		}

		// メイン処理
		List<FilterSettingInfo> result = controller.deleteUserFilterSettings(FilterCategoryEnum.JOB_HISTORY, userId, filterIdList);

		// Response変換
		List<JobHistoryFilterSettingResponse> rsp = new ArrayList<>();
		for (FilterSettingInfo src : result) {
			JobHistoryFilterSettingResponse dst = new JobHistoryFilterSettingResponse();
			RestBeanUtil.convertBeanNoInvalid(src, dst);
			rsp.add(dst);
		}

		RestLanguageConverter.convertMessages(rsp);
		return Response.status(Status.OK).entity(rsp).build();
	}
	
	/**
	 * {@link FilterSettingSummaryInfo} をレスポンスへ変換します。
	 */
	protected static FilterSettingSummariesResponse convertQueryResultResponse(List<FilterSettingSummaryInfo> result) throws HinemosUnknown {
		FilterSettingSummariesResponse res = new FilterSettingSummariesResponse();
		res.setSummaries(new ArrayList<>());
		for (FilterSettingSummaryInfo src : result) {
			FilterSettingSummaryResponse dst = new FilterSettingSummaryResponse();
			RestBeanUtil.convertBeanNoInvalid(src, dst);
			res.getSummaries().add(dst);
		}
		return res;
	}

	/**
	 * PathParamのフィルタカテゴリ文字列を列挙値へ変換して返します。
	 */
	protected static FilterCategoryEnum validateCategoryPathParam(String pathParam) throws InvalidSetting {
		try {
			return FilterCategoryEnum.fromPath(pathParam);
		} catch (Exception e) {
			throw new InvalidSetting(MessageConstant.MESSAGE_INPUT_NON_EXISTENT_MEMBER.getMessage(
					MessageConstant.FILTER_CATEGORY.getMessage(),
					Stream.of(FilterCategoryEnum.values()).map(FilterCategoryEnum::getPath).collect(Collectors.joining(","))));
		}
	}

	/**
	 * PathParamのフィルタID文字列を検証して返します。
	 */
	protected static String validateFilterId(String filterId) throws InvalidSetting {
		CommonValidator.validateId(MessageConstant.FILTER_ID.getMessage(), filterId, FilterSettingConstant.ID_LEN_MAX);
		return filterId;
	}

	/**
	 * オーナーユーザIDを検証して返します。<br/>
	 * ログインユーザIDと異なるユーザIDが指定されている場合は、ログインユーザに管理者権限があることを確認します。<br/>
	 *
	 * @param userId 検証対象のユーザId。
	 * @return 引数の値がnullまたは空白の場合はログインユーザIDを返します。それ以外の場合は引数の値を返します。
	 */
	protected static String validateOwnerUserId(String userId) throws InvalidSetting, InvalidRole {
		if (userId == null || userId.trim().length() == 0) {
			return HinemosSessionContext.getLoginUserId();
		} else {
			CommonValidator.validateId(MessageConstant.USER_ID.getMessage(), userId, DataRangeConstant.USER_ID_MAXLEN);
			if (!userId.equals(HinemosSessionContext.getLoginUserId())) {
				if (!HinemosSessionContext.isAdministrator()) {
					throw new InvalidRole(MessageConstant.MESSAGE_USER_AUTH_NEED_ADMINISTRATORS_ROLE.getMessage());
				}
			}
			return userId;
		}
	}

	/**
	 * フィルタ設定検索パターンを検証して返します。
	 */
	protected static FilterSettingSearchPattern validateSearchPattern(String pattern) throws InvalidSetting {
		CommonValidator.validateString(
				MessageConstant.FILTER_SETTING_SEARCH_PATTERN.getMessage(),
				pattern,
				false,
				0,
				FilterSettingConstant.SEARCH_PATTERN_LEN_MAX);
		return new FilterSettingSearchPattern(pattern);
	}

}
