/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.calendar;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_404;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_409;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.clustercontrol.calendar.model.CalendarDetailInfo;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.calendar.model.CalendarPatternInfo;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.calendar.util.TimeStringConverter;
import com.clustercontrol.fault.CalendarDuplicate;
import com.clustercontrol.fault.CalendarNotFound;
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
import com.clustercontrol.rest.endpoint.calendar.dto.enumtype.OperationStatusEnum;
import com.clustercontrol.rest.endpoint.calendar.dto.AddCalendarPatternRequest;
import com.clustercontrol.rest.endpoint.calendar.dto.AddCalendarRequest;
import com.clustercontrol.rest.endpoint.calendar.dto.CalendarDetailInfoRequest;
import com.clustercontrol.rest.endpoint.calendar.dto.CalendarDetailInfoResponse;
import com.clustercontrol.rest.endpoint.calendar.dto.CalendarDetailInfoResponseP1;
import com.clustercontrol.rest.endpoint.calendar.dto.CalendarInfoResponse;
import com.clustercontrol.rest.endpoint.calendar.dto.CalendarMonthResponse;
import com.clustercontrol.rest.endpoint.calendar.dto.CalendarPatternInfoResponse;
import com.clustercontrol.rest.endpoint.calendar.dto.ModifyCalendarPatternRequest;
import com.clustercontrol.rest.endpoint.calendar.dto.ModifyCalendarRequest;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.util.MessageConstant;

@Path("/calendar")
@RestLogFunc(name = LogFuncName.Calendar)
public class CalendarRestEndpoints {

	private static Log m_log = LogFactory.getLog(CalendarRestEndpoints.class);
	private static final String ENDPOINT_OPERATION_ID_PREFIX = "calendar";

	/**
	 * オーナーロールIDを条件としてカレンダ一覧を取得します。<BR>
	 * 返り値のCalendarInfoResponseのメンバ変数List<CalendarDetailInfoResponse>は空。<BR>
	 *
	 * CalendarRead権限が必要
	 *
	 * @param ownerRoleId
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@GET
	@Path("/calendar")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCalendarList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CalendarInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Calendar, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Calendar, modeList = { SystemPrivilegeMode.READ })
	public Response getCalendarList(@QueryParam("ownerRoleId") String ownerRoleId, @Context Request request,
			@Context UriInfo uriInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call getCalendarList()");

		List<CalendarInfo> infoResList = new CalendarControllerBean().getCalendarList(ownerRoleId);
		List<CalendarInfoResponse> dtoResList = new ArrayList<>();
		for (CalendarInfo info : infoResList) {
			CalendarInfoResponse dto = new CalendarInfoResponse();
			RestBeanUtil.convertBean(info, dto);

			// 独自変換処理
			convertDetailResponse(info.getCalendarDetailList(), dto.getCalendarDetailList());
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 引数で指定したカレンダーIDに対応するカレンダ情報を取得します。<BR>
	 *
	 * CalendarRead権限が必要
	 *
	 * @param calendarId
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws CalendarNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@GET
	@Path("/calendar/{calendarId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCalendar")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CalendarInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Calendar, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Calendar, modeList = { SystemPrivilegeMode.READ })
	public Response getCalendar(@PathParam(value = "calendarId") String calendarId, @Context Request request,
			@Context UriInfo uriInfo)
			throws CalendarNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call getCalendar()");

		CalendarInfo infoRes = new CalendarControllerBean().getCalendar(calendarId);
		CalendarInfoResponse dtoRes = new CalendarInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		// 個別変換
		convertDetailResponse(infoRes.getCalendarDetailList(), dtoRes.getCalendarDetailList());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * カレンダ（基本）情報を登録します。<BR>
	 *
	 * CalendarAdd権限が必要
	 *
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws CalendarDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@POST
	@Path("/calendar")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCalendar")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CalendarInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.Calendar, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Calendar, modeList = { SystemPrivilegeMode.ADD })
	public Response addCalendar(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addCalendarBody", content = @Content(schema = @Schema(implementation = AddCalendarRequest.class))) String requestBody)
			throws CalendarDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addCalendar()");

		AddCalendarRequest dtoReq = null;
		// JSONからDTOへ変換
		dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddCalendarRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);

		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		CalendarInfo infoReq = new CalendarInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		// 独自変換処理
		convertDetailRequest(dtoReq.getCalendarDetailList(), infoReq.getCalendarDetailList());

		// ControllerBean呼び出し
		CalendarInfo infoRes = new CalendarControllerBean().addCalendar(infoReq);
		CalendarInfoResponse dtoRes = new CalendarInfoResponse();

		// ControllerBeanからのINFOをDTOへ変換
		RestBeanUtil.convertBean(infoRes, dtoRes);
		// 独自変換処理
		convertDetailResponse(infoRes.getCalendarDetailList(), dtoRes.getCalendarDetailList());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * カレンダ（基本）情報を変更します。<BR>
	 *
	 * CalendarModify権限が必要
	 *
	 * @param calendarId
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws CalendarNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@PUT
	@Path("/calendar/{calendarId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCalendar")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CalendarInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Calendar, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Calendar, modeList = { SystemPrivilegeMode.MODIFY })
	public Response modifyCalendar(@PathParam("calendarId") String calendarId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyCalendarBody", content = @Content(schema = @Schema(implementation = ModifyCalendarRequest.class))) String requestBody)
			throws CalendarNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call modifyCalendar()");

		ModifyCalendarRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyCalendarRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		CalendarInfo infoReq = new CalendarInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setCalendarId(calendarId);
		// 独自変換処理
		convertDetailRequest(dtoReq.getCalendarDetailList(), infoReq.getCalendarDetailList());

		CalendarInfo infoRes = new CalendarControllerBean().modifyCalendar(infoReq);
		CalendarInfoResponse dtoRes = new CalendarInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		// 独自変換処理
		convertDetailResponse(infoRes.getCalendarDetailList(), dtoRes.getCalendarDetailList());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * カレンダ(基本）情報を 削除します。<BR>
	 *
	 * CalendarModify権限が必要
	 *
	 * @param calendaIds
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws CalendarNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@DELETE
	@Path("/calendar")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteCalendar")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CalendarInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.Calendar, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Calendar, modeList = { SystemPrivilegeMode.MODIFY })
	public Response deleteCalendar(@ArrayTypeParam @QueryParam(value = "calendarIds") String calendaIds,
			@Context Request request, @Context UriInfo uriInfo)
			throws CalendarNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call deleteCalendar()");

		List<String> calendaIdList = new ArrayList<>();
		if(calendaIds != null && !calendaIds.isEmpty()) {
			calendaIdList = Arrays.asList(calendaIds.split(","));
		}
		
		List<CalendarInfo> infoResList = new CalendarControllerBean().deleteCalendar(calendaIdList);
		List<CalendarInfoResponse> dtoResList = new ArrayList<>();
		for (CalendarInfo infoRes : infoResList) {
			CalendarInfoResponse dtoRes = new CalendarInfoResponse();
			RestBeanUtil.convertBean(infoRes, dtoRes);
			// 独自変換処理
			convertDetailResponse(infoRes.getCalendarDetailList(), dtoRes.getCalendarDetailList());
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 引数で指定された、年月の1日から順番に稼働/非稼働を詰めたArrayListを返す。
	 * 　稼働：ALL_OPERATION
	 * 　稼働/非稼働：PARTIAL_OPERATION
	 * 　非稼働：NOT_OPERATION
	 *
	 * CalendarRead権限が必要
	 *
	 * @param calendarId
	 * @param request
	 * @param uriInfo
	 * @param year
	 * @param month
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws CalendarNotFound
	 */
	@GET
	@Path("/calendar/{calendarId}/calendarDetail_monthOperationState")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCalendarMonth")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CalendarMonthResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.CalendarDetail, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Calendar, modeList = { SystemPrivilegeMode.READ })
	public Response getCalendarMonth(@PathParam(value = "calendarId") String calendarId, @Context Request request,
			@Context UriInfo uriInfo, @QueryParam(value = "year") String year,
			@QueryParam(value = "month") String month)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, CalendarNotFound {

		Map<Integer, Integer> infoResMap = new CalendarControllerBean().getCalendarMonth(calendarId,
				Integer.parseInt(year), Integer.parseInt(month));
		List<CalendarMonthResponse> dtoResList = getCalendarMonthConverter(infoResMap);
		
		RestLanguageConverter.convertMessages(dtoResList);
		
		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * スケジュールバーを表示するための情報取得
	 * 第ｘ何曜日など、その他にて選択される祝日など年月日がDTOとして保持されないものは、
	 * 年月日に変換して保持し、カレンダ実行予定ビューを表示する際に使用する。
	 * 
	 * CalendarRead権限が必要
	 * 
	 * @param calendarId
	 * @param year
	 * @param month
	 * @param day
	 * @param uriInfo
	 * @param request
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws CalendarNotFound
	 * @throws InvalidSetting
	 */
	@GET
	@Path("/calendar/{calendarId}/calendarDetail_week")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCalendarWeek")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CalendarDetailInfoResponseP1.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.CalendarDetail, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Calendar, modeList = { SystemPrivilegeMode.READ })
	public Response getCalendarWeek(@PathParam(value = "calendarId") String calendarId,
			@QueryParam(value = "year") String year, @QueryParam(value = "month") String month,
			@QueryParam(value = "day") String day, @Context UriInfo uriInfo, @Context Request request)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, CalendarNotFound, InvalidSetting {

		List<CalendarDetailInfo> infoResList = new CalendarControllerBean().getCalendarWeek(calendarId,
				Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
		List<CalendarDetailInfoResponseP1> dtoResList = new ArrayList<>();

		for (CalendarDetailInfo infoRes : infoResList) {
			CalendarDetailInfoResponseP1 dtoRes = new CalendarDetailInfoResponseP1();
			RestBeanUtil.convertBean(infoRes, dtoRes);
			// 独自変換実装
			calendarDetailConvertBeanRequest(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * カレンダ[カレンダパターン]情報一覧を取得します。<BR>
	 *
	 * CalendarRead権限が必要
	 *
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws CalendarNotFound
	 * @throws InvalidSetting
	 */
	@GET
	@Path("/pattern")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCalendarPatternList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CalendarPatternInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Pattern, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Calendar, modeList = { SystemPrivilegeMode.READ })
	public Response getCalendarPatternList(@QueryParam(value = "ownerRoleId") String ownerRoleId)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, CalendarNotFound, InvalidSetting {
		m_log.info("call getCalendarPatternList()");

		List<CalendarPatternInfo> infoResList = new CalendarControllerBean().getCalendarPatternList(ownerRoleId);
		List<CalendarPatternInfoResponse> dtoResList = new ArrayList<>();

		for (CalendarPatternInfo infoRes : infoResList) {
			CalendarPatternInfoResponse dtoRes = new CalendarPatternInfoResponse();
			RestBeanUtil.convertBean(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 引数で指定したIDに対応するカレンダ[カレンダパターン]情報を取得します。<BR>
	 * 
	 * CalendarRead権限が必要
	 * 
	 * @param calendarPatternId
	 * @return
	 * @throws CalendarNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@GET
	@Path("/pattern/{calendarPatternId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCalendarPattern")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CalendarPatternInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Pattern, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Calendar, modeList = { SystemPrivilegeMode.READ })
	public Response getCalendarPattern(@PathParam(value = "calendarPatternId") String calendarPatternId)
			throws CalendarNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call getCalendarPattern()");

		CalendarPatternInfo infoRes = new CalendarControllerBean().getCalendarPattern(calendarPatternId);
		CalendarPatternInfoResponse dtoRes = new CalendarPatternInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * カレンダ[カレンダパターン]情報を登録します。<BR>
	 *
	 * CalendarAdd権限が必要
	 *
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws CalendarDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@POST
	@Path("/pattern")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCalendarPattern")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CalendarPatternInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.Pattern, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Calendar, modeList = { SystemPrivilegeMode.ADD })
	public Response addCalendarPattern(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addCalendarPatternBody", content = @Content(schema = @Schema(implementation = AddCalendarPatternRequest.class))) String requestBody)
			throws CalendarDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addCalendarPattern()");

		AddCalendarPatternRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddCalendarPatternRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		CalendarPatternInfo infoReq = new CalendarPatternInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		new CalendarControllerBean().addCalendarPattern(infoReq);
		CalendarPatternInfo infoRes = null;
		try{
			infoRes = new CalendarControllerBean().getCalendarPattern(infoReq.getCalPatternId());
		}catch( CalendarNotFound e){
			// 追加が正常終了してるので ここに来ることは通常あり得ない。
			m_log.error("addCalendarPattern () : CalendarNotFound .id =" + infoReq.getCalPatternId() + " message="
					+ e.getMessage(), e);
		}
		CalendarPatternInfoResponse dtoRes = new CalendarPatternInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * カレンダ[カレンダパターン]情報を変更します。<BR>
	 *
	 * CalendarModify権限が必要
	 *
	 * @param request
	 * @param uriInfo
	 * @param calendarPatternId
	 * @param requestBody
	 * @return
	 * @throws CalendarNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@PUT
	@Path("/pattern/{calendarPatternId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCalendarPattern")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CalendarPatternInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Pattern, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Calendar, modeList = { SystemPrivilegeMode.MODIFY })
	public Response modifyCalendarPattern(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("calendarPatternId") String calendarPatternId,
			@RequestBody(description = "modifyCalendarPatternBody", content = @Content(schema = @Schema(implementation = ModifyCalendarPatternRequest.class))) String requestBody)
			throws CalendarNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call modifyCalendarPattern()");

		ModifyCalendarPatternRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyCalendarPatternRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		CalendarPatternInfo infoReq = new CalendarPatternInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setCalPatternId(calendarPatternId);

		new CalendarControllerBean().modifyCalendarPattern(infoReq);
		CalendarPatternInfo infoRes = null;
		try{
			infoRes = new CalendarControllerBean().getCalendarPattern(infoReq.getCalPatternId());
		}catch( CalendarNotFound e){
			// 変更が正常終了してるので ここに来ることは通常あり得ない。
			m_log.error("modifyCalendarPattern () : CalendarNotFound .id =" + infoReq.getCalPatternId() + " message="
					+ e.getMessage(), e);
		}
		
		CalendarPatternInfoResponse dtoRes = new CalendarPatternInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * カレンダ[カレンダパターン]情報を 削除します。<BR>
	 *
	 * CalendarModify権限が必要
	 *
	 * @param calendarPatternIds
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws CalendarNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@DELETE
	@Path("/pattern")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteCalendarPattern")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CalendarPatternInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.Pattern, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Calendar, modeList = { SystemPrivilegeMode.MODIFY })
	public Response deleteCalendarPattern(
			@ArrayTypeParam @QueryParam(value = "calendarPatternIds") String calendarPatternIds,
			@Context Request request, @Context UriInfo uriInfo)
			throws CalendarNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call deleteCalendarPattern()");

		List<String> calendarPatternIdList = new ArrayList<>();
		if(calendarPatternIds != null && !calendarPatternIds.isEmpty()) {
			calendarPatternIdList = Arrays.asList(calendarPatternIds.split(","));
		}
		
		List<CalendarPatternInfo> infoResList = new CalendarControllerBean()
				.deleteCalendarPattern(calendarPatternIdList);

		List<CalendarPatternInfoResponse> dtoResList = new ArrayList<CalendarPatternInfoResponse>();
		for (CalendarPatternInfo infoRes : infoResList) {
			CalendarPatternInfoResponse dtoRes = new CalendarPatternInfoResponse();
			RestBeanUtil.convertBean(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 時刻情報を Long から String に変換します。 対象：カレンダ詳細 ( 開始時刻、終了時刻 )
	 * 
	 * @param longTime
	 * @return
	 */
	private String convertTimeToString(Long longTime) {

		String strTime = null;
		strTime = TimeStringConverter.formatTime(new Date(longTime));
		return strTime;
	}

	/**
	 * 時刻情報を String から Long に変換します。 対象：カレンダ詳細 ( 開始時刻、終了時刻)
	 * 
	 * @param strTime
	 * @return
	 */
	private Long convertStringToTime(String strTime, String itemName) throws InvalidSetting {
		Long longTime;
		Date dateTime = null;
		try {
			dateTime = TimeStringConverter.parseTime(strTime);
		} catch (Exception e) {
			// TODO REST対応 InvalidSetting の メッセージ編集（項目名表示 多言語対応）
			throw new InvalidSetting(MessageConstant.MESSAGE_DATEFOMAT_ILLEGAL_VALUE.getMessage( itemName, strTime, e.getMessage()));
		}
		longTime = dateTime.getTime();
		return longTime;
	}

	/**
	 * CalendarDetailInfoのstartTime, endTimeの個別変換(INFO -> DTO)
	 * 
	 * @param detailInfoList(コピー元)
	 * @param detailDtoList(コピー先)
	 */
	private void convertDetailResponse(List<CalendarDetailInfo> detailInfoList,
			List<CalendarDetailInfoResponse> detailDtoList) {
		for(CalendarDetailInfo info : detailInfoList) {
			for(CalendarDetailInfoResponse dto : detailDtoList) {
				// orderNoが等しいものが変換対象
				if(info.getOrderNo().equals(dto.getOrderNo())) {
					dto.setStartTime(convertTimeToString(info.getTimeFrom()));
					dto.setEndTime(convertTimeToString(info.getTimeTo()));
					break;
				}
			}
		}
	}

	/**
	 * CalendarDetailInfoのstartTime, endTimeの個別変換(DTO -> INFO)
	 * 
	 * @param detailInfoList(コピー元)
	 * @param detailDtoList(コピー先)
	 * @throws InvalidSetting
	 */
	private void convertDetailRequest(List<CalendarDetailInfoRequest> detailDtoList,
			List<CalendarDetailInfo> detailInfoList) throws InvalidSetting {
		for (CalendarDetailInfoRequest dto : detailDtoList) {
			for(CalendarDetailInfo info : detailInfoList) {
				// orderNoが等しいものが変換対象
				if(dto.getOrderNo().equals(info.getOrderNo())) {
					info.setTimeFrom(convertStringToTime(dto.getStartTime(), "startTime"));
					info.setTimeTo(convertStringToTime(dto.getEndTime(), "endTime"));
					break;
				}
			}
		}
	}

	/**
	 * CalendarEndpoints固有で必要な変換処理を行います
	 * 
	 * @param detailInfo
	 * @param detailDto
	 * @return
	 */
	private CalendarDetailInfoResponseP1 calendarDetailConvertBeanRequest(CalendarDetailInfo detailInfo,
			CalendarDetailInfoResponseP1 detailDto) {
		detailDto.setStartTime(convertTimeToString(detailInfo.getTimeFrom()));
		detailDto.setEndTime(convertTimeToString(detailInfo.getTimeTo()));
		return detailDto;
	}

	private List<CalendarMonthResponse> getCalendarMonthConverter(Map<Integer, Integer> map) {
		List<CalendarMonthResponse> ret = new ArrayList<>();
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			CalendarMonthResponse res = new CalendarMonthResponse();
			res.setDay(entry.getKey());
			switch (entry.getValue()) {
			case 0:
				res.setOperationStatus(OperationStatusEnum.ALL_OPERATION);
				break;
			case 1:
				res.setOperationStatus(OperationStatusEnum.PARTIAL_OPERATION);
				break;
			case 2:
				res.setOperationStatus(OperationStatusEnum.NOT_OPERATION);
				break;
			default:
				// findbugs対応 設定なし
				break;
			}
			ret.add(res);
		}
		return ret;
	}
}