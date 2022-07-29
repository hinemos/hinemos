/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_404;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
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
import javax.ws.rs.core.StreamingOutput;
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
import com.clustercontrol.collect.session.CollectControllerBean;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.EventLogNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.filtersetting.bean.EventFilterBaseInfo;
import com.clustercontrol.filtersetting.bean.StatusFilterBaseInfo;
import com.clustercontrol.monitor.bean.EventCustomCommandResultRoot;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.monitor.bean.EventSelectionInfo;
import com.clustercontrol.monitor.bean.ScopeDataInfo;
import com.clustercontrol.monitor.bean.StatusDataInfo;
import com.clustercontrol.monitor.bean.ViewListInfo;
import com.clustercontrol.monitor.session.EventCustomCommandBean;
import com.clustercontrol.monitor.session.MonitorControllerBean;
import com.clustercontrol.rest.annotation.RestLog;
import com.clustercontrol.rest.annotation.RestLog.LogAction;
import com.clustercontrol.rest.annotation.RestLog.LogTarget;
import com.clustercontrol.rest.annotation.RestLog.LogType;
import com.clustercontrol.rest.annotation.RestLogFunc;
import com.clustercontrol.rest.annotation.RestLogFunc.LogFuncName;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.ArrayTypeParam;
import com.clustercontrol.rest.annotation.cmdtool.IgnoreCommandline;
import com.clustercontrol.rest.endpoint.monitorresult.dto.DeleteStatusRequest;
import com.clustercontrol.rest.endpoint.monitorresult.dto.DownloadEventFileRequest;
import com.clustercontrol.rest.endpoint.monitorresult.dto.EventCustomCommandResultRootResponse;
import com.clustercontrol.rest.endpoint.monitorresult.dto.EventLogInfoRequest;
import com.clustercontrol.rest.endpoint.monitorresult.dto.EventLogInfoResponse;
import com.clustercontrol.rest.endpoint.monitorresult.dto.EventLogOperationHistoryEntityResponse;
import com.clustercontrol.rest.endpoint.monitorresult.dto.EventSelectionRequest;
import com.clustercontrol.rest.endpoint.monitorresult.dto.ExecEventCustomCommandRequest;
import com.clustercontrol.rest.endpoint.monitorresult.dto.ExecEventCustomCommandResponse;
import com.clustercontrol.rest.endpoint.monitorresult.dto.GetEventCustomCommandResultResponse;
import com.clustercontrol.rest.endpoint.monitorresult.dto.GetEventDataMapResponse;
import com.clustercontrol.rest.endpoint.monitorresult.dto.GetEventInfoRequest;
import com.clustercontrol.rest.endpoint.monitorresult.dto.GetEventInfoResponse;
import com.clustercontrol.rest.endpoint.monitorresult.dto.GetEventListRequest;
import com.clustercontrol.rest.endpoint.monitorresult.dto.GetEventListResponse;
import com.clustercontrol.rest.endpoint.monitorresult.dto.GetStatusListRequest;
import com.clustercontrol.rest.endpoint.monitorresult.dto.GetStatusListResponse;
import com.clustercontrol.rest.endpoint.monitorresult.dto.ModifyBatchConfirmRequest;
import com.clustercontrol.rest.endpoint.monitorresult.dto.ModifyCollectGraphFlgRequest;
import com.clustercontrol.rest.endpoint.monitorresult.dto.ModifyCommnetRequest;
import com.clustercontrol.rest.endpoint.monitorresult.dto.ModifyConfirmRequest;
import com.clustercontrol.rest.endpoint.monitorresult.dto.ModifyEventInfoRequest;
import com.clustercontrol.rest.endpoint.monitorresult.dto.ScopeDataInfoResponse;
import com.clustercontrol.rest.endpoint.monitorresult.dto.StatusDataInfoRequestP1;
import com.clustercontrol.rest.endpoint.monitorresult.dto.StatusInfoResponse;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonConverter;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestDownloadFile;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.rest.util.RestTempFileUtil;

@Path("/monitorresult")
@RestLogFunc(name = LogFuncName.Monitor)
public class MonitorResultRestEndpoints {

	private static Log m_log = LogFactory.getLog( MonitorResultRestEndpoints.class );

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "monitorresult";

	
	/**
	 * 引数で指定された条件に一致するイベント一覧情報を取得します。(クライアントview用)
	 * <p>
	 * MonitorResultRead権限が必要
	 */
	@POST
	@Path("/event_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetEventList")
	@RestLog(action = LogAction.Get, target = LogTarget.Event, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorResult, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetEventListResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getEventList(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "getEventListBody", content = @Content(schema = @Schema(
					implementation = GetEventListRequest.class))) String requestBody)
			throws InvalidRole, HinemosUnknown, InvalidSetting {

		m_log.info("call getEventList");

		// size
		int size = 0;

		//Request
		EventFilterBaseInfo filterInfo = EventFilterBaseInfo.ofClientViewDefault();

		if (requestBody != null && requestBody.length() > 0) {
			GetEventListRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(
					requestBody, GetEventListRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();
			RestBeanUtil.convertBean(dtoReq.getFilter(), filterInfo);
			if (dtoReq.getSize() != null) {
				size = dtoReq.getSize();
			}
		}

		//
		ViewListInfo viewListInfo = new MonitorControllerBean().getEventList(filterInfo, size);

		//Response
		GetEventListResponse dtoRes = new GetEventListResponse();
		RestBeanUtil.convertBeanNoInvalid(viewListInfo, dtoRes);

		List<EventLogInfoResponse> eventList = new ArrayList<EventLogInfoResponse>();
		for (EventDataInfo tmp : viewListInfo.getEventList()) {
			EventLogInfoResponse eventLogInfoResponse = new EventLogInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(tmp, eventLogInfoResponse);
			eventList.add(eventLogInfoResponse);
		}
		dtoRes.setEventList(eventList);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * スコープ情報一覧を取得します。<BR><BR>
	 * 引数で指定されたファシリティの配下全てのファシリティのスコープ情報一覧を返します。<BR>
	 * 各スコープ情報は、ScopeDataInfoのインスタンスとして保持されます。<BR>
	 * 
	 * MonitorResultRead権限が必要
	 * 
	 * @param facilityId 取得対象の親ファシリティID
	 * @param statusFlag
	 * @param eventFlag
	 * @param orderFlg
	 * @return スコープ情報一覧（ScopeDataInfoが格納されたArrayList）
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws MonitorNotFound
	 * 
	 */
	@GET
	@Path("/scope")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetScopeList")
	@RestLog(action = LogAction.Get, target = LogTarget.Scope, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorResult,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ScopeDataInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScopeList(
			@QueryParam(value = "facilityId")	String facilityId, @Context Request request, @Context UriInfo uriInfo,
			@QueryParam(value = "statusFlag")	boolean statusFlag, 
			@QueryParam(value = "eventFlag")	boolean eventFlag,
			@QueryParam(value = "orderFlg")		boolean orderFlg) 
			throws InvalidRole, HinemosUnknown, MonitorNotFound{
		
		m_log.info("call getScopeList");
		
		List<ScopeDataInfo> scopeDataInfoList = new MonitorControllerBean().getScopeList(facilityId, statusFlag, eventFlag, orderFlg);
		//Response
		List<ScopeDataInfoResponse> dtoResList = new ArrayList<>();
		
		if(scopeDataInfoList != null) {
			for(ScopeDataInfo tmp:scopeDataInfoList){
				ScopeDataInfoResponse dtoRes = new ScopeDataInfoResponse();
				RestBeanUtil.convertBeanNoInvalid(tmp, dtoRes);
				dtoResList.add(dtoRes);
			}
		}
		
		RestLanguageConverter.convertMessages(dtoResList);
		return Response.status(Status.OK).entity(dtoResList).build();
		
	}

	/**
	 * 引数で指定された条件に一致するステータス情報一覧を取得します。<BR>
	 * 各ステータス情報は、StatusDataInfoのインスタンスとして保持されます。<BR>
	 * 
	 * MonitorResultRead権限が必要
	 */
	@POST
	@Path("/status_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetStatusList")
	@RestLog(action = LogAction.Get, target = LogTarget.Status, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorResult, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetStatusListResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getStatusList(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "getStatusListBody", content = @Content(schema = @Schema(
					implementation = GetStatusListRequest.class))) String requestBody)
			throws InvalidRole, HinemosUnknown, InvalidSetting {

		m_log.info("call getStatusList");

		// size
		Integer size = null;

		//Request
		StatusFilterBaseInfo filterInfo = StatusFilterBaseInfo.ofClientViewDefault();

		if (requestBody != null && requestBody.length() > 0) {
			GetStatusListRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(
					requestBody, GetStatusListRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();
			size = dtoReq.getSize();
			RestBeanUtil.convertBean(dtoReq.getFilter(), filterInfo);
		}

		//
		List<StatusDataInfo> statusDataInfoList = new MonitorControllerBean().getStatusList(filterInfo);

		//Response
		List<StatusInfoResponse> dtoResList = new ArrayList<StatusInfoResponse>();
		int recCount = 0;
		for (StatusDataInfo tmp : statusDataInfoList) {
			StatusInfoResponse dtoResRec = new StatusInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(tmp, dtoResRec);
			dtoResList.add(dtoResRec);
			recCount++;
			if (size != null && recCount >= size) {
				break;
			}
		}
		RestLanguageConverter.convertMessages(dtoResList);
		GetStatusListResponse dtoRes = new GetStatusListResponse();
		dtoRes.setTotal(statusDataInfoList.size());
		dtoRes.setStatusList(dtoResList);
		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 引数で指定されたステータス情報を削除します。<BR>
	 * 
	 * 引数のlistは、StatusDataInfoが格納されたListとして渡されます。<BR>
	 * 
	 * @param list 削除対象のステータス情報一覧（StatusDataInfoが格納されたList）
	 * @return 削除に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * @since 2.0.0
	 * 
	 * @see com.clustercontrol.monitor.bean.StatusInfoData
	 * @see com.clustercontrol.monitor.factory.DeleteStatus#delete(List)
	 */
	@POST
	@Path("/status_delete")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteStatus")
	@RestLog(action = LogAction.Delete, target = LogTarget.Status, type = LogType.UPDATE )
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorResult,modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
	@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StatusInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
	@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
	@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
	@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
	@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
	@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") }) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteStatus(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "deleteStatusBody", 
			content = @Content(schema = @Schema(implementation = DeleteStatusRequest.class))) String requestBody)
					throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound, InvalidSetting {
		
		m_log.info("call deleteStatus");
		
		//Request
		DeleteStatusRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				DeleteStatusRequest.class);
		
		ArrayList<StatusDataInfo> inStatusDataInfoList = new ArrayList<>();
		
		for(StatusDataInfoRequestP1 tmp : dtoReq.getStatusDataInfoRequestlist() ){
			StatusDataInfo inStatusDataInfo = new StatusDataInfo();
			RestBeanUtil.convertBeanNoInvalid(tmp, inStatusDataInfo);
			inStatusDataInfoList.add(inStatusDataInfo);
		}
		inStatusDataInfoList = new MonitorControllerBean().deleteStatus(inStatusDataInfoList);
		
		//Response
		List<StatusInfoResponse> dtoResList = new ArrayList<>(); 
		StatusInfoResponse dtoRes = new StatusInfoResponse();
		
		for(StatusDataInfo tmp : inStatusDataInfoList){
			RestBeanUtil.convertBeanNoInvalid(tmp, dtoRes);
			dtoResList.add(dtoRes);
		}
		
		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 引数で指定された条件に一致する帳票出力用イベント情報一覧を返します。<BR><BR>
	 * 
	 * MonitorResultRead権限が必要
	 * 	 *
	 * @param facilityId 取得対象の親ファシリティID
	 * @param filter 検索条件
	 * @param filename 出力ファイル名
	 * @param language ロケール
	 * @return 帳票のデータハンドラ
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting 
	 * 
	 * @since 2.1.0
	 * 
	 * @see com.clustercontrol.monitor.factory.SelectEvent#getEventListForReport(String, EventFilterInfo)
	 */
	@POST
	@Path("/event_download")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DownloadEventFile")
	@RestLog(action = LogAction.Download, target = LogTarget.Event, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorResult, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	@Consumes(MediaType.APPLICATION_JSON)
	public Response downloadEventFile(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "downloadEventFileBody", 
			content = @Content(schema = @Schema(implementation = DownloadEventFileRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting{
		
		m_log.info("call downloadEventFile");
		
		//Request
		DownloadEventFileRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, DownloadEventFileRequest.class);

		EventFilterBaseInfo filter = EventFilterBaseInfo.ofDownloadDefault();
		RestBeanUtil.convertBeanNoInvalid(dtoReq.getFilter(), filter);
		
		List<EventSelectionInfo> selectedEvents = new ArrayList<>();
		if (dtoReq.getSelectedEvents() != null) {
			for (EventSelectionRequest dto : dtoReq.getSelectedEvents()) {
				EventSelectionInfo info = new EventSelectionInfo();
				RestBeanUtil.convertBeanNoInvalid(dto, info);
				selectedEvents.add(info);
			}
		}

		// 最優先のLocaleを取得
		List<Locale> localeList = HinemosSessionContext.getLocaleList();
		List<Locale> availableLocaleList = Arrays.asList(NumberFormat.getAvailableLocales());
		Locale targetLocale = null;
		for (Locale locale : localeList) {
			if (availableLocaleList.contains(locale)) {
				m_log.debug(locale.toString() + " is contained in availableLocaleList");
				targetLocale = locale;
			}
		}

		RestDownloadFile restDownloadFile = new MonitorControllerBean()
				.downloadEventFile(filter, selectedEvents, dtoReq.getFilename(), targetLocale);
		StreamingOutput stream = RestTempFileUtil.getTempFileStream(restDownloadFile.getTempFile());

		return Response.ok(stream).header("Content-Disposition", "filename=\"" + restDownloadFile.getFileName() + "\"").build();
	}

	/**
	 * イベント詳細情報を取得します。<BR><BR>
	 * 
	 * @param monitorId 取得対象の監視項目ID
	 * @param monitorDetailId 取得対象の監視詳細
	 * @param pluginId 取得対象のプラグインID
	 * @param facilityId 取得対象のファシリティID
	 * @param outputDate 取得対象の受信日時
	 * @return イベント詳細情報
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 */
	@POST
	@Path("/event_detail_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetEventInfo")
	@RestLog(action = LogAction.Get, target = LogTarget.Event, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorResult, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetEventInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getEventInfo(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "getEventInfoBody", 
			content = @Content(schema = @Schema(implementation = GetEventInfoRequest.class))) String requestBody)
					throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting, MonitorNotFound {
		
		//Request
		GetEventInfoRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				GetEventInfoRequest.class);
		
		EventDataInfo eventDataInfo =new MonitorControllerBean().getEventInfo(dtoReq.getMonitorId(), 
				dtoReq.getMonitorDetailId(), dtoReq.getPluginId(), dtoReq.getFacilityId(), RestCommonConverter.convertDTStringToHinemosTime(dtoReq.getOutputDate(), ""));
		
		//Response
		GetEventInfoResponse dtoRes = new GetEventInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(eventDataInfo, dtoRes);
		
		List<EventLogOperationHistoryEntityResponse> eventLogOperationHistoryEntityList =new ArrayList<>();
		for(com.clustercontrol.notify.monitor.model.EventLogOperationHistoryEntity tmp:eventDataInfo.getEventLogHitory()){
			EventLogOperationHistoryEntityResponse eventLogOperationHistoryEntity = new EventLogOperationHistoryEntityResponse();
			RestBeanUtil.convertBeanNoInvalid(tmp, eventLogOperationHistoryEntity);
			eventLogOperationHistoryEntityList.add(eventLogOperationHistoryEntity);
		}
		dtoRes.setEventLogHitory(eventLogOperationHistoryEntityList);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 引数で指定されたイベント情報のコメントを更新します。<BR><BR>
	 * コメント追記ユーザとして、コメントユーザを設定します。
	 * 
	 * MonitorResultWrite権限が必要
	 * 
	 * @param monitorId 更新対象の監視項目ID
	 * @param monitorDetailId 更新対象の監視詳細
	 * @param pluginId 更新対象のプラグインID
	 * @param facilityId 更新対象のファシリティID
	 * @param outputDate 更新対象の受信日時
	 * @param comment コメント
	 * @param commentDate コメント変更日時
	 * @param commentUser コメント変更ユーザ
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * @throws InvalidRole 
	 * @throws EventLogNotFound 
	 * @throws MonitorNotFound 
	 * @see com.clustercontrol.bean.ConfirmConstant
	 * @see com.clustercontrol.monitor.factory.ModifyEventComment#modifyComment(String, String, String, Long, String, Long, String)
	 */
	@PUT
	@Path("/event_comment")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyComment")
	@RestLog(action = LogAction.Modify, target = LogTarget.Event, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorResult, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventLogInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyComment(
			@RequestBody(description = "modifyCommentBody", 
			content = @Content(schema = @Schema(implementation = ModifyCommnetRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown, EventLogNotFound, InvalidRole, MonitorNotFound {
		
		m_log.info("call modifyComment");
		
		//Request
		ModifyCommnetRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyCommnetRequest.class);
		
		new MonitorControllerBean().modifyComment(dtoReq.getMonitorId(), dtoReq.getMonitorDetailId(), dtoReq.getPluginId(), 
				dtoReq.getFacilityId(), RestCommonConverter.convertDTStringToHinemosTime(dtoReq.getOutputDate(), ""), 
				dtoReq.getComment(), RestCommonConverter.convertDTStringToHinemosTime(dtoReq.getCommentDate(), ""), 
				dtoReq.getCommentUser());
		
		//更新したEventDataInfoを再度取得
		EventDataInfo eventDataInfo = null;
		eventDataInfo = new MonitorControllerBean().getEventInfo(dtoReq.getMonitorId(), dtoReq.getMonitorDetailId(), 
				dtoReq.getPluginId(), dtoReq.getFacilityId(), RestCommonConverter.convertDTStringToHinemosTime(dtoReq.getOutputDate(), ""));
		
		//Response
		EventLogInfoResponse dtoRes = new EventLogInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(eventDataInfo, dtoRes);
		RestLanguageConverter.convertMessages(dtoRes);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * 引数で指定されたイベント情報一覧の確認を更新します。<BR><BR>
	 * 確認ユーザとして、操作を実施したユーザを設定します。<BR>
	 * 複数のイベント情報を更新します。
	 * 
	 * MonitorResultWrite権限が必要
	 * 
	 * @param list 更新対象のイベント情報一覧（EventDataInfoが格納されたArrayList）
	 * @param confirmType 確認タイプ（未確認／確認中／確認済）（更新値）
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 * @throws InvalidSetting 
	 * 
	 * @see com.clustercontrol.bean.ConfirmConstant
	 * @see com.clustercontrol.monitor.factory.ModifyEventConfirm#modifyConfirm(List, int)
	 */
	@PUT
	@Path("/event_confirm")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyConfirm")
	@RestLog(action = LogAction.Modify, target = LogTarget.Event, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorResult, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventLogInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyConfirm(	@RequestBody(description = "modifyConfirmBody", 
			content = @Content(schema = @Schema(implementation = ModifyConfirmRequest.class))) String requestBody)
					throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound, InvalidSetting {
		
		m_log.info("call modifyConfirm");
		
		//Request
		ModifyConfirmRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyConfirmRequest.class);
		ArrayList<EventDataInfo> inEventDataInfoList = new ArrayList<>();
		EventDataInfo inEventDataInfo = null;
		
		for(EventLogInfoRequest tmp:dtoReq.getList()){
			inEventDataInfo = new EventDataInfo();
			RestBeanUtil.convertBeanNoInvalid(tmp, inEventDataInfo);
			inEventDataInfoList.add(inEventDataInfo);
		}
		
		new MonitorControllerBean().modifyConfirm(inEventDataInfoList, dtoReq.getConfirmType());
		
		//Response
		List<EventLogInfoResponse> dtoResList = new ArrayList<EventLogInfoResponse>();
		
		//更新したEventDataInfoを再度取得
		for(EventDataInfo tmp : inEventDataInfoList){
			EventDataInfo eventDataInfo = new MonitorControllerBean().getEventInfo(tmp.getMonitorId(), tmp.getMonitorDetailId(), 
					tmp.getPluginId(), tmp.getFacilityId(), tmp.getOutputDate());
			EventLogInfoResponse dtoRes = new EventLogInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(eventDataInfo, dtoRes);
			dtoResList.add(dtoRes);
		}
		
		RestLanguageConverter.convertMessages(dtoResList);
		
		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * 引数で指定された条件に一致するイベント情報の確認を一括更新します。<BR><BR>
	 * 確認ユーザとして、操作を実施したユーザを設定します。<BR>
	 * 
	 * MonitorResultWrite権限が必要
	 * 
	 * @param confirmType 確認タイプ（未／済）（更新値）
	 * @param facilityId 更新対象の親ファシリティID
	 * @param property 更新条件
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting 
	 * 
	 * @see com.clustercontrol.bean.ConfirmConstant
	 * @see com.clustercontrol.monitor.factory.ModifyEventConfirm#modifyBatchConfirm(int, String, EventBatchConfirmInfo)
	 */
	@PUT
	@Path("/event_multiConfirm")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyBatchConfirm")
	@RestLog(action = LogAction.Modify, target = LogTarget.Event, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorResult, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventLogInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyBatchConfirm(@RequestBody(description = "modifyBatchConfirmBody",
			content = @Content(schema = @Schema(implementation = ModifyBatchConfirmRequest.class))) String requestBody)
					throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		
		m_log.info("call modifyBatchConfirm");
		
		//Request
		ModifyBatchConfirmRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyBatchConfirmRequest.class);
		EventFilterBaseInfo filter = new EventFilterBaseInfo();
		RestBeanUtil.convertBeanNoInvalid(dtoReq.getFilter(), filter);
		
		int rtn = new MonitorControllerBean().modifyBatchConfirm(dtoReq.getConfirmType(), filter);
		
		//更新したイベント情報を取得
		ViewListInfo viewListInfo = new MonitorControllerBean().getEventList(filter, rtn);

		//Response
		List<EventLogInfoResponse> dtoResList = new ArrayList<EventLogInfoResponse>();
		
		for(EventDataInfo tmp:viewListInfo.getEventList()){
			EventLogInfoResponse dtoRes =new EventLogInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(tmp, dtoRes);
			dtoResList.add(dtoRes);
		}
		
		RestLanguageConverter.convertMessages(dtoResList);
		
		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 引数で指定されたイベント情報一覧の性能グラフ用フラグを更新します。<BR><BR>
	 * 複数のイベント情報を更新します。
	 * 
	 * MonitorResultWrite権限が必要
	 * 
	 * @param list 更新対象のイベント情報一覧（EventDataInfoが格納されたArrayList）
	 * @param collectGraphFlg 性能グラフ用フラグ（ON:true、OFF:false）（更新値）
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 * @throws InvalidSetting 
	 */
	@PUT
	@Path("/event_collectGraphFlg")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCollectGraphFlg")
	@RestLog(action = LogAction.Modify, target = LogTarget.Event, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorResult, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventLogInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyCollectGraphFlg(@RequestBody(description = "modifyConfirmBody",
			content = @Content(schema = @Schema(implementation = ModifyCollectGraphFlgRequest.class))) String requestBody) 
					throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound, InvalidSetting {
		
		m_log.info("call modifyCollectGraphFlg");
		
		//Request
		ModifyCollectGraphFlgRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyCollectGraphFlgRequest.class);
		ArrayList<EventDataInfo> inEventDataInfoList = new ArrayList<>();
		EventDataInfo inEventDataInfo = null;
		
		for(EventLogInfoRequest tmp : dtoReq.getList()){
			inEventDataInfo = new EventDataInfo();
			RestBeanUtil.convertBeanNoInvalid(tmp, inEventDataInfo);
			inEventDataInfoList.add(inEventDataInfo);
		}
		new MonitorControllerBean().modifyCollectGraphFlg(inEventDataInfoList, dtoReq.getCollectGraphFlg());
		
		//Response
		List<EventLogInfoResponse> dtoResList = new ArrayList<EventLogInfoResponse>();
		EventLogInfoResponse dtoRes = new EventLogInfoResponse();
		
		//更新したEventDataInfoを再度取得
		for(EventDataInfo tmp : inEventDataInfoList){
			EventDataInfo eventDataInfo = null;
			eventDataInfo = new MonitorControllerBean().getEventInfo(tmp.getMonitorId(), tmp.getMonitorDetailId(), 
					tmp.getPluginId(), tmp.getFacilityId(), tmp.getOutputDate());
			dtoRes = new EventLogInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(eventDataInfo, dtoRes);
			dtoResList.add(dtoRes);
		}
		
		RestLanguageConverter.convertMessages(dtoRes);
		
		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * 引数で指定されたイベント情報を更新します。<BR><BR> 
	 * 
	 * @param info 更新対象のイベント情報
	 * @throws HinemosUnknown 
	 * @throws Exception 
	 * @throws EventLogNotFound
	 * @throws InvalidRole
	 * @throws InvalidSetting 
	 * @throws MonitorNotFound 
	 */
	@PUT
	@Path("/event")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyEventInfo")
	@RestLog(action = LogAction.Modify, target = LogTarget.Event, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorResult, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventLogInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyEventInfo(@RequestBody(description = "modifyEventInfoBody",
			content = @Content(schema = @Schema(implementation = ModifyEventInfoRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown, MonitorNotFound, InvalidRole {
		
		m_log.info("call modifyEventInfo");
		
		//Request
		ModifyEventInfoRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyEventInfoRequest.class);
		
		EventDataInfo inEventDataInfo = new EventDataInfo();
		RestBeanUtil.convertBeanNoInvalid(dtoReq.getInfo(), inEventDataInfo);
		
		new MonitorControllerBean().modifyEventInfo(inEventDataInfo);
		
		//更新したEventDataInfoを再度取得
		EventDataInfo eventDataInfo = null;
		eventDataInfo = new MonitorControllerBean().getEventInfo(dtoReq.getInfo().getMonitorId(), dtoReq.getInfo().getMonitorDetailId(), 
				dtoReq.getInfo().getPluginId(), dtoReq.getInfo().getFacilityId(), 
				RestCommonConverter.convertDTStringToHinemosTime(dtoReq.getInfo().getOutputDate(), ""));
		
		//Response
		EventLogInfoResponse dtoRes = new EventLogInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(eventDataInfo, dtoRes);
		RestLanguageConverter.convertMessages(dtoRes);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * 引数で指定されたイベントNoのコマンドを指定されたイベント情報に対して実行します。<BR><BR>
	 * 
	 * MonitorResultExec権限が必要
	 * 
	 * @param commandNo 実行するコマンド番号
	 * @param eventList コマンド実行対象のイベント
	 * @return 実行結果を確認するためのコマンド結果ID
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting 
	 */
	@POST
	@Path("/eventCustomCommand_exec")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ExecEventCustomCommand")
	@RestLog(action = LogAction.Exec, target = LogTarget.EventCustomCommand, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorResult, modeList = { SystemPrivilegeMode.EXEC })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExecEventCustomCommandResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response execEventCustomCommand(@RequestBody(description = "execEventCustomCommandBody",
			content = @Content(schema = @Schema(implementation = ExecEventCustomCommandRequest.class))) String requestBody) 
					throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		
		m_log.info("call execEventCustomCommand");
		
		//Request
		ExecEventCustomCommandRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ExecEventCustomCommandRequest.class);
		
		List<EventDataInfo> inEventDataInfoList = new ArrayList<>();
		
		for(EventLogInfoRequest tmp : dtoReq.getEventList()){
			EventDataInfo inEventDataInfo = new EventDataInfo();
			RestBeanUtil.convertBeanNoInvalid(tmp, inEventDataInfo);
			inEventDataInfoList.add(inEventDataInfo);
		}
		
		String retVal = new EventCustomCommandBean().execEventCustomCommand(dtoReq.getCommandNo(), inEventDataInfoList);
		
		//Response
		ExecEventCustomCommandResponse dtoRes = new ExecEventCustomCommandResponse();
		dtoRes.setCommandResultID(retVal);
		
		RestLanguageConverter.convertMessages(dtoRes);
		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * イベントカスタムコマンドの実行結果を取得します。<BR><BR>
	 * 
	 * MonitorResultExec権限が必要
	 * 
	 * @param commandResultID イベントカスタムコマンド実行時に出力されたコマンド結果ID
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/eventCustomCommand/{uuid}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetEventCustomCommandResult")
	@RestLog(action = LogAction.Get, target = LogTarget.EventCustomCommand, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.MonitorResult,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetEventCustomCommandResultResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEventCustomCommandResult(
			@PathParam("uuid")String commandResultID) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		
		m_log.info("call getEventCustomCommandResult");
		
		EventCustomCommandResultRoot eventCustomCommandResultRoot = null;
		
		eventCustomCommandResultRoot = new EventCustomCommandBean().getEventCustomCommandResult(commandResultID);
		
		//Response
		GetEventCustomCommandResultResponse dtoRes = null;
		if( eventCustomCommandResultRoot != null ){
			dtoRes = new GetEventCustomCommandResultResponse();
			EventCustomCommandResultRootResponse eventCustomCommandResultRootDTO = new EventCustomCommandResultRootResponse();
			RestBeanUtil.convertBeanNoInvalid(eventCustomCommandResultRoot, eventCustomCommandResultRootDTO);
			dtoRes.setEventCustomCommandResultRoot(eventCustomCommandResultRootDTO);
		}
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * イベント履歴のフラグを取得する。
	 * @throws HinemosUnknown 
	 */
	@GET
	@Path("/event_collectValid_mapKeyFacility")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetEventDataMap")
	@RestLog(action = LogAction.Get, target = LogTarget.Event, type = LogType.REFERENCE)
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetEventDataMapResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline //Map型をResponseDtoに含む場合、コマンドラインツール共通ロジックでは問題があったので個別化のため対象から除外
	public Response getEventDataMap (
			@ArrayTypeParam @QueryParam(value = "facilityIdList")String facilityIdList) 
					throws HinemosUnknown {
		
		m_log.info("call getEventDataMap");
		
		//Response
		GetEventDataMapResponse dtoRes = new GetEventDataMapResponse();
		Map<String, ArrayList<EventLogInfoResponse>> dtoResMap = new HashMap<>();
		
		ArrayList<String> tmpFacilityIdList = new ArrayList<>();
		if(facilityIdList != null && !facilityIdList.isEmpty()) {
			tmpFacilityIdList = new ArrayList<String>(Arrays.asList(facilityIdList.split(",")));
		}
		
		Map<String, ArrayList<EventDataInfo>> map1 = new CollectControllerBean().getEventDataMap(tmpFacilityIdList);
		
		for (Entry<String, ArrayList<EventDataInfo>> e : map1.entrySet()) {
			ArrayList<EventLogInfoResponse> dtoList = new ArrayList<EventLogInfoResponse>();
			for (EventDataInfo info : e.getValue()) {
				EventLogInfoResponse dto = new EventLogInfoResponse();
				RestBeanUtil.convertBeanNoInvalid(info, dto);
				dtoList.add(dto);
			}
			dtoResMap.put(e.getKey(), dtoList);
		}
		dtoRes.setMap(dtoResMap);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}
}
