/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.agent;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_409;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeFunction;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.IconFileDuplicate;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.jobmanagement.bean.RpaJobScreenshot;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.logfile.bean.LogfileResultDTO;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.IgnoreCommandline;
import com.clustercontrol.rest.endpoint.agent.dto.AgtLogfileResultDTORequest;
import com.clustercontrol.rest.endpoint.agent.dto.ForwardLogfileResultRequest;
import com.clustercontrol.rest.endpoint.agent.dto.ForwardRpaScreenshotRequest;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.session.RestControllerBean;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestByteArrayConverter;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.rpa.monitor.session.MonitorRpaLogfileControllerBean;
import com.clustercontrol.xcloud.bean.Request;

@Path("/agentRpa")
public class AgentRpaRestEndpoints {

	private static Log m_log = LogFactory.getLog(AgentRpaRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "agentRpa";

	/**
	 * RPAシナリオジョブで取得したスクリーンショットを登録します。
	 */
	@POST
	@Path("rpaScreenshot")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ForwardRpaScreenshot")
	@APIResponses(value = { @APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	// @Produces(MediaType.APPLICATION_JSON) // レスポンスボディは空なので不要
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	public Response forwardRpaScreenshot(
			@RequestBody(description = "forwardRpaScreenshotBody", content = @Content(schema = @Schema(type = SchemaType.OBJECT, implementation = ForwardRpaScreenshotRequest.class))) @FormDataParam("file") InputStream inputStream,
			@FormDataParam("rpaScreenshot") String requestBody, @Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, IconFileDuplicate {
		m_log.debug("forwardRpaScreenshot: Start.");

		ForwardRpaScreenshotRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ForwardRpaScreenshotRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		RpaJobScreenshot infoReq = new RpaJobScreenshot();
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// 画像データをinputStreamからbyte[]へ変換
		infoReq.setFiledata(RestByteArrayConverter.convertInputStreamToByteArray(inputStream));

		new JobControllerBean().addJobRpaScreenshot(infoReq);
		return Response.status(Status.OK).build();
	}
	
	/**
	 * RPAログファイル監視でマッチしたものに対して通知をマネージャに依頼する。
	 * エージェントでは同一のロジックで監視するため、DTOはログファイル監視と同様
	 */
	@POST
	@Path("/rpalogfileResult")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ForwardRpaLogfileResult")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	// @Produces(MediaType.APPLICATION_JSON) // レスポンスボディは空なので不要
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	public Response forwardRpaLogfileResult(@RequestBody(description = "forwardRpaLogfileResultBody", content = @Content(schema = @Schema(implementation = ForwardLogfileResultRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown {
		m_log.debug("forwardLogfileResult: Start.");

		// ---- 重複チェック
		boolean first = new RestControllerBean().registerRestAgentRequest("", "forwardRpaLogfileResult");
		if (!first) {
			return Response.status(Status.OK).build();
		}

		ForwardLogfileResultRequest dto = RestObjectMapperWrapper.convertJsonToObject(requestBody, ForwardLogfileResultRequest.class);
		AgentInfo agentInfo = new AgentInfo();

		RestBeanUtil.convertBean(dto.getAgentInfo(), agentInfo);
		List<LogfileResultDTO> resultList = new ArrayList<LogfileResultDTO>();
		for (AgtLogfileResultDTORequest dtoResult : dto.getResultList()) {
			LogfileResultDTO infoResult = new LogfileResultDTO();
			RestBeanUtil.convertBean(dtoResult, infoResult);
			resultList.add(infoResult);
		}

		for (String facilityId : AgentConnectUtil.getFacilityIds(agentInfo)) {
			new MonitorRpaLogfileControllerBean().run(facilityId, resultList);
		}

		return Response.status(Status.OK).build();
	}
}
