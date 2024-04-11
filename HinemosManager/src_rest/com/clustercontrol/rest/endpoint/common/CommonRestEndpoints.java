/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.common;

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
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.StringUtil;
import com.clustercontrol.fault.CommandTemplateDuplicate;
import com.clustercontrol.fault.CommandTemplateNotFound;
import com.clustercontrol.fault.HinemosPropertyDuplicate;
import com.clustercontrol.fault.HinemosPropertyNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MailTemplateDuplicate;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.fault.RestAccessDuplicate;
import com.clustercontrol.fault.RestAccessNotFound;
import com.clustercontrol.fault.RestAccessUsed;
import com.clustercontrol.fault.UsedCommandTemplate;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.maintenance.model.HinemosPropertyInfo;
import com.clustercontrol.maintenance.session.HinemosPropertyControllerBean;
import com.clustercontrol.monitor.bean.EventCustomCommandInfoData;
import com.clustercontrol.monitor.bean.EventDisplaySettingInfo;
import com.clustercontrol.monitor.session.EventCustomCommandBean;
import com.clustercontrol.monitor.session.MonitorControllerBean;
import com.clustercontrol.notify.mail.model.MailTemplateInfo;
import com.clustercontrol.notify.mail.session.MailTemplateControllerBean;
import com.clustercontrol.notify.model.CommandTemplateInfo;
import com.clustercontrol.notify.restaccess.model.RestAccessAuthHttpHeader;
import com.clustercontrol.notify.restaccess.model.RestAccessInfo;
import com.clustercontrol.notify.restaccess.model.RestAccessSendHttpHeader;
import com.clustercontrol.notify.restaccess.session.RestAccessInfoControllerBean;
import com.clustercontrol.notify.session.CommandTemplateControllerBean;
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
import com.clustercontrol.rest.endpoint.common.dto.AddCommandTemplateRequest;
import com.clustercontrol.rest.endpoint.common.dto.AddHinemosPropertyRequest;
import com.clustercontrol.rest.endpoint.common.dto.AddMailTemplateRequest;
import com.clustercontrol.rest.endpoint.common.dto.AddRestAccessInfoRequest;
import com.clustercontrol.rest.endpoint.common.dto.CommandTemplateResponse;
import com.clustercontrol.rest.endpoint.common.dto.EventCustomCommandInfoDataResponse;
import com.clustercontrol.rest.endpoint.common.dto.EventDisplaySettingInfoResponse;
import com.clustercontrol.rest.endpoint.common.dto.HinemosPropertyResponse;
import com.clustercontrol.rest.endpoint.common.dto.HinemosPropertyResponseP1;
import com.clustercontrol.rest.endpoint.common.dto.HinemosTimeResponse;
import com.clustercontrol.rest.endpoint.common.dto.MailTemplateInfoResponse;
import com.clustercontrol.rest.endpoint.common.dto.ModifyCommandTemplateRequest;
import com.clustercontrol.rest.endpoint.common.dto.ModifyHinemosPropertyRequest;
import com.clustercontrol.rest.endpoint.common.dto.ModifyMailTemplateRequest;
import com.clustercontrol.rest.endpoint.common.dto.ModifyRestAccessInfoRequest;
import com.clustercontrol.rest.endpoint.common.dto.RestAccessInfoResponse;
import com.clustercontrol.rest.endpoint.common.dto.enumtype.HinemosPropertyTypeEnum;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

@Path("/common")
@RestLogFunc(name = LogFuncName.Common)
public class CommonRestEndpoints {

	private static Log m_log = LogFactory.getLog(CommonRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "common";

	/**
	 * メールテンプレート情報をマネージャに登録します。<BR>
	 * 
	 * @param AddMailTemplateRequest
	 * @return MailTemplateInfoResponse
	 * @throws HinemosUnknown
	 * @throws MailTemplateDuplicate
	 * @throws InvalidUserPasss
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 * 
	 */
	@POST
	@Path("/mailTemplate")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddMailTemplate")
	@RestLog(action = LogAction.Add, target = LogTarget.MailTemplate, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.ADD })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MailTemplateInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addMailTemplate(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addMailTemplateBody", content = @Content(schema = @Schema(implementation = AddMailTemplateRequest.class))) String requestBody)
			throws HinemosUnknown, MailTemplateDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addMailTemplate()");

		AddMailTemplateRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddMailTemplateRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MailTemplateInfo infoReq = new MailTemplateInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		MailTemplateInfo infoRes = new MailTemplateControllerBean().addMailTemplate(infoReq);

		MailTemplateInfoResponse dtoRes = new MailTemplateInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * マネージャ上のメールテンプレート情報を変更します。<BR>
	 * 
	 * @param ModifyMailTemplateRequest
	 * @return MailTemplateInfoResponse
	 * @throws HinemosUnknown
	 * @throws MailTemplateNotFound
	 * @throws InvalidUserPasss
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 * 
	 */
	@PUT
	@Path("/mailTemplate/{mailTemplateId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyMailTemplate")
	@RestLog(action = LogAction.Modify, target = LogTarget.MailTemplate, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MailTemplateInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyMailTemplate(@PathParam("mailTemplateId") String mailTemplateId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyMailTemplateBody", content = @Content(schema = @Schema(implementation = ModifyMailTemplateRequest.class))) String requestBody)
			throws HinemosUnknown, MailTemplateNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call modifyMailTemplate()");

		ModifyMailTemplateRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyMailTemplateRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MailTemplateInfo infoReq = new MailTemplateInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMailTemplateId(mailTemplateId);
		MailTemplateInfo infoRes = new MailTemplateControllerBean().modifyMailTemplate(infoReq);

		MailTemplateInfoResponse dtoRes = new MailTemplateInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * メールテンプレート情報をマネージャから削除します。<BR>
	 * 
	 * @param mailTemplateIds
	 *            削除対象のメールテンプレートID
	 * @return MailTemplateInfoResponse[]
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MailTemplateNotFound 
	 * 
	 */
	@DELETE
	@Path("/mailTemplate")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteMailTemplate")
	@RestLog(action = LogAction.Delete, target = LogTarget.MailTemplate, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MailTemplateInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteMailTemplate(@ArrayTypeParam @QueryParam(value = "mailTemplateIds") String mailTemplateIds,
			@Context Request request, @Context UriInfo uriInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole, MailTemplateNotFound {
		m_log.info("call deleteMailTemplate()");

		List<String> mailTemplateIdList = new ArrayList<>();
		if(mailTemplateIds != null && !mailTemplateIds.isEmpty()) {
			mailTemplateIdList = Arrays.asList(mailTemplateIds.split(","));
		}
		
		List<MailTemplateInfo> infoResList = new MailTemplateControllerBean().deleteMailTemplate(mailTemplateIdList);
		List<MailTemplateInfoResponse> dtoResList = new ArrayList<>();
		for (MailTemplateInfo info : infoResList) {
			MailTemplateInfoResponse dto = new MailTemplateInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 引数で指定されたメールテンプレート情報を返します。
	 * 
	 * @param mailTemplateId
	 *            取得対象のメールテンプレートID
	 * @return MailTemplateInfoResponse
	 * @throws HinemosUnknown
	 * @throws MailTemplateNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 *
	 */
	@GET
	@Path("/mailTemplate/{mailTemplateId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMailTemplateInfo")
	@RestLog(action = LogAction.Get, target = LogTarget.MailTemplate, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MailTemplateInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMailTemplateInfo(@PathParam(value = "mailTemplateId") String mailTemplateId,
			@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, MailTemplateNotFound, InvalidUserPass, InvalidRole {
		m_log.info("call getMailTemplateInfo()");

		MailTemplateInfo infoRes = new MailTemplateControllerBean().getMailTemplateInfo(mailTemplateId);
		MailTemplateInfoResponse dtoRes = new MailTemplateInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * メールテンプレート情報一覧を取得します。<BR>
	 * 
	 * @param ownerRoleId
	 * @return MailTemplateInfoResponse[]
	 * @throws HinemosUnknown
	 * @throws MailTemplateNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 */
	@GET
	@Path("/mailTemplate")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMailTemplateList")
	@RestLog(action = LogAction.Get, target = LogTarget.MailTemplate, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MailTemplateInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMailTemplateList(@QueryParam(value = "ownerRoleId") String ownerRoleId, @Context Request request,
			@Context UriInfo uriInfo) throws HinemosUnknown, MailTemplateNotFound, InvalidUserPass, InvalidRole {
		m_log.info("call getMailTemplateList()");

		List<MailTemplateInfo> infoResList = new MailTemplateControllerBean()
				.getMailTemplateListByOwnerRole(ownerRoleId);
		List<MailTemplateInfoResponse> dtoResList = new ArrayList<>();
		for (MailTemplateInfo info : infoResList) {
			MailTemplateInfoResponse dto = new MailTemplateInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * Hinemosプロパティを追加します。
	 *
	 * @param AddHinemosPropertyRequest
	 * @return HinemosPropertyResponse
	 * @throws HinemosUnknown
	 * @throws HinemosPropertyDuplicate
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 *
	 */
	@POST
	@Path("/hinemosProperty")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddHinemosProperty")
	@RestLog(action = LogAction.Add, target = LogTarget.HinemosProperty, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Maintenance, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.ADD })
	@RestSystemAdminPrivilege(isNeed=true)
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosPropertyResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addHinemosProperty(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addHinemosPropertyBody", content = @Content(schema = @Schema(implementation = AddHinemosPropertyRequest.class))) String requestBody)
			throws HinemosUnknown, HinemosPropertyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addHinemosProperty()");

		AddHinemosPropertyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddHinemosPropertyRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		HinemosPropertyInfo infoReq = new HinemosPropertyInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		if (HinemosPropertyTypeEnum.STRING.equals(dtoReq.getType())) {
			infoReq.setValueType(HinemosPropertyTypeEnum.STRING.getCode());
			infoReq.setValueString(dtoReq.getValue());
		} else if (HinemosPropertyTypeEnum.NUMERIC.equals(dtoReq.getType())) {
			infoReq.setValueType(HinemosPropertyTypeEnum.NUMERIC.getCode());
			if (dtoReq.getValue() != null && !dtoReq.getValue().isEmpty()) {
				try {
					infoReq.setValueNumeric(Long.valueOf(dtoReq.getValue()));
				} catch (NumberFormatException e) {
					throw new InvalidSetting(MessageConstant.MESSAGE_INPUT_BETWEEN.getMessage(MessageConstant.VALUE.getMessage(), String.valueOf(Long.MIN_VALUE), String.valueOf(Long.MAX_VALUE)));
				}
			}
		} else if (HinemosPropertyTypeEnum.BOOLEAN.equals(dtoReq.getType())) {
			infoReq.setValueType(HinemosPropertyTypeEnum.BOOLEAN.getCode());
			infoReq.setValueBoolean(Boolean.valueOf(dtoReq.getValue()));
		}

		HinemosPropertyInfo infoRes = new HinemosPropertyControllerBean().addHinemosProperty(infoReq);

		HinemosPropertyResponse dtoRes = new HinemosPropertyResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		convertToResponseValue(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();

	}

	/**
	 * Hinemosプロパティを変更します。
	 *
	 * @param ModifyHinemosPropertyRequestt
	 * @return HinemosPropertyResponse
	 * @throws HinemosUnknown1
	 * @throws HinemosPropertyNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@PUT
	@Path("/hinemosProperty/{key}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyHinemosProperty")
	@RestLog(action = LogAction.Modify, target = LogTarget.HinemosProperty, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Maintenance, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@RestSystemAdminPrivilege(isNeed=true)
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosPropertyResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyHinemosProperty(@PathParam("key") String key, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyHinemosPropertyBody", content = @Content(schema = @Schema(implementation = ModifyHinemosPropertyRequest.class))) String requestBody)
			throws HinemosUnknown, HinemosPropertyNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call modifyHinemosProperty()");

		ModifyHinemosPropertyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyHinemosPropertyRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		HinemosPropertyInfo infoReq = new HinemosPropertyControllerBean().getHinemosPropertyInfo(key);
		RestBeanUtil.convertBean(dtoReq, infoReq);
		if (HinemosPropertyTypeEnum.STRING.getCode().equals(infoReq.getValueType())) {
			infoReq.setValueString(dtoReq.getValue());
		} else if (HinemosPropertyTypeEnum.NUMERIC.getCode().equals(infoReq.getValueType())) {
			if (dtoReq.getValue() != null && !dtoReq.getValue().isEmpty()) {
				try {
					infoReq.setValueNumeric(Long.valueOf(dtoReq.getValue()));
				} catch (NumberFormatException e) {
					throw new InvalidSetting(MessageConstant.MESSAGE_INPUT_BETWEEN.getMessage(MessageConstant.VALUE.getMessage(), String.valueOf(Long.MIN_VALUE), String.valueOf(Long.MAX_VALUE)));
				}
			} else {
				infoReq.setValueNumeric(null);
			}
		} else if (HinemosPropertyTypeEnum.BOOLEAN.getCode().equals(infoReq.getValueType())) {
			infoReq.setValueBoolean(Boolean.valueOf(dtoReq.getValue()));
		}
		HinemosPropertyInfo infoRes = new HinemosPropertyControllerBean().modifyHinemosProperty(infoReq);

		HinemosPropertyResponse dtoRes = new HinemosPropertyResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		convertToResponseValue(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * Hinemosプロパティを削除します。
	 *
	 * @param List<String>
	 *            keys
	 * @return HinemosPropertyResponse[]
	 * @throws HinemosUnknown
	 * @throws HinemosPropertyNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 */
	@DELETE
	@Path("/hinemosProperty")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteHinemosProperty")
	@RestLog(action = LogAction.Delete, target = LogTarget.HinemosProperty, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Maintenance, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@RestSystemAdminPrivilege(isNeed=true)
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosPropertyResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteHinemosProperty(@ArrayTypeParam @QueryParam(value = "keys") String keys,
			@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, HinemosPropertyNotFound, InvalidUserPass, InvalidRole {
		m_log.info("call deleteHinemosProperty()");

		List<String> keyList = new ArrayList<>();
		if(keys != null && !keys.isEmpty()) {
			keyList = Arrays.asList(keys.split(","));
		}
		
		List<HinemosPropertyInfo> infoResList = new HinemosPropertyControllerBean().deleteHinemosProperty(keyList);
		List<HinemosPropertyResponse> dtoResList = new ArrayList<>();
		for (HinemosPropertyInfo info : infoResList) {
			HinemosPropertyResponse dto = new HinemosPropertyResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			convertToResponseValue(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * Hinemosプロパティを取得します。
	 *
	 * @param String
	 *            key
	 * @return HinemosPropertyResponse
	 * @throws HinemosUnknown
	 * @throws HinemosPropertyNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 *
	 */
	@GET
	@Path("/hinemosProperty/{key}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetHinemosProperty")
	@RestLog(action = LogAction.Get, target = LogTarget.HinemosProperty, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Maintenance, modeList = { SystemPrivilegeMode.READ })
	@RestSystemAdminPrivilege(isNeed=true)
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosPropertyResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getHinemosProperty(@PathParam(value = "key") String key, @Context Request request,
			@Context UriInfo uriInfo) throws HinemosUnknown, HinemosPropertyNotFound, InvalidUserPass, InvalidRole {
		m_log.info("call getHinemosProperty()");

		HinemosPropertyInfo infoRes = new HinemosPropertyControllerBean().getHinemosPropertyInfo(key);
		HinemosPropertyResponse dtoRes = new HinemosPropertyResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
		convertToResponseValue(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * Hinemosプロパティの一覧を取得します。<BR>
	 *
	 * @return HinemosPropertyResponse[]
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 *
	 */
	@GET
	@Path("/hinemosProperty")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetHinemosPropertyList")
	@RestLog(action = LogAction.Get, target = LogTarget.HinemosProperty, type = LogType.REFERENCE )
	@RestSystemAdminPrivilege(isNeed=true)
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosPropertyResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getHinemosPropertyList(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getHinemosPropertyList()");

		List<HinemosPropertyInfo> infoResList = new HinemosPropertyControllerBean().getHinemosPropertyList();
		List<HinemosPropertyResponse> dtoResList = new ArrayList<>();
		for (HinemosPropertyInfo info : infoResList) {
			HinemosPropertyResponse dto = new HinemosPropertyResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			convertToResponseValue(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 現在のHinemos時刻を返します。<br/>
	 * 
	 * @return HinemosTimeResponse
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * 
	 */
	@GET
	@Path("/hinemosTime")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetHinemosTime")
	@RestLog(action = LogAction.Get, target = LogTarget.HinemosTime, type = LogType.REFERENCE )
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosTimeResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getHinemosTime(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getHinemosTime()");

		HinemosTimeResponse dtoRes = new HinemosTimeResponse();
		dtoRes.setCurrentTimeMillis(HinemosTime.currentTimeMillis());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 承認ジョブにおける承認画面へのリンク先アドレスを取得する。<BR>
	 *
	 * @return HinemosPropertyResponseP1
	 * @throws HinemosUnknown
	 * @throws HinemosPropertyNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/hinemosProperty_forJob/jobApprovalPageLink")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetApprovalPageLink")
	@RestLog(action = LogAction.Get, target = LogTarget.HinemosProperty, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosPropertyResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getApprovalPageLink(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, HinemosPropertyNotFound, InvalidUserPass, InvalidRole {
		m_log.info("call getApprovalPageLink()");

		String valueString = new JobControllerBean().getApprovalPageLink();

		HinemosPropertyResponseP1 dtoRes = new HinemosPropertyResponseP1();
		dtoRes.setValue(valueString);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * スクリプトの最大サイズを取得します。<BR>
	 *
	 * @return HinemosPropertyResponseP1
	 * @throws HinemosUnknown
	 * @throws HinemosPropertyNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/hinemosProperty_forJob/jobScriptMaxsize")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetScriptContentMaxSize")
	@RestLog(action = LogAction.Get, target = LogTarget.HinemosProperty, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosPropertyResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getScriptContentMaxSize(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, HinemosPropertyNotFound, InvalidUserPass, InvalidRole {
		m_log.info("call getScriptContentMaxSize()");

		int maxsize = 0;
		maxsize = HinemosPropertyCommon.job_script_maxsize.getIntegerValue();

		HinemosPropertyResponseP1 dtoRes = new HinemosPropertyResponseP1();
		dtoRes.setValue(String.valueOf(maxsize));

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ファイルサイズの最大サイズを取得します。<BR>
	 *
	 * @return HinemosPropertyResponseP1
	 * @throws HinemosUnknown
	 * @throws HinemosPropertyNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/hinemosProperty_forInfra/infraMaxFileSize")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetInfraMaxFileSize")
	@RestLog(action = LogAction.Get, target = LogTarget.HinemosProperty, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Infra, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosPropertyResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getInfraMaxFileSize(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, HinemosPropertyNotFound, InvalidUserPass, InvalidRole {
		m_log.info("call getInfraMaxFileSize()");

		int infraMaxFileSize = 0;
		infraMaxFileSize = HinemosPropertyCommon.infra_max_file_size.getIntegerValue();

		HinemosPropertyResponseP1 dtoRes = new HinemosPropertyResponseP1();
		dtoRes.setValue(String.valueOf(infraMaxFileSize));

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブマップ用アイコンイメージ（ジョブ用）のデフォルトアイコンIDを取得する。<BR>
	 * 
	 * @return HinemosPropertyResponseP1
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/hinemosProperty_forJobMap/jobmapIconIdDefaultJob")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobmapIconIdJobDefault")
	@RestLog(action = LogAction.Get, target = LogTarget.HinemosProperty, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosPropertyResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getJobmapIconIdJobDefault(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJobmapIconIdJobDefault()");

		String valueString = new JobControllerBean().getJobmapIconIdJobDefault();

		HinemosPropertyResponseP1 dtoRes = new HinemosPropertyResponseP1();
		dtoRes.setValue(valueString);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブマップ用アイコンイメージ（ジョブネット用）のデフォルトアイコンIDを取得する。<BR>
	 * 
	 * @return HinemosPropertyResponseP1
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/hinemosProperty_forJobMap/jobmapIconIdDefaultJobnet")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobmapIconIdJobnetDefault")
	@RestLog(action = LogAction.Get, target = LogTarget.HinemosProperty, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosPropertyResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getJobmapIconIdJobnetDefault(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJobmapIconIdJobnetDefault()");

		String valueString = new JobControllerBean().getJobmapIconIdJobnetDefault();

		HinemosPropertyResponseP1 dtoRes = new HinemosPropertyResponseP1();
		dtoRes.setValue(valueString);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブマップ用アイコンイメージ（承認ジョブ用）のデフォルトアイコンIDを取得する。<BR>
	 * 
	 * @return HinemosPropertyResponseP1
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/hinemosProperty_forJobMap/jobmapIconIdDefaultApprovaljob")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobmapIconIdApprovalDefault")
	@RestLog(action = LogAction.Get, target = LogTarget.HinemosProperty, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosPropertyResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getJobmapIconIdApprovalDefault() throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJobmapIconIdApprovalDefault()");

		String valueString = new JobControllerBean().getJobmapIconIdApprovalDefault();

		HinemosPropertyResponseP1 dtoRes = new HinemosPropertyResponseP1();
		dtoRes.setValue(valueString);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブマップ用アイコンイメージ（監視ジョブ用）のデフォルトアイコンIDを取得する。<BR>
	 * 
	 * @return HinemosPropertyResponseP1
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/hinemosProperty_forJobMap/jobmapIconIdDefaultMonitorjob")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobmapIconIdMonitorDefault")
	@RestLog(action = LogAction.Get, target = LogTarget.HinemosProperty, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosPropertyResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getJobmapIconIdMonitorDefault(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJobmapIconIdMonitorDefault()");

		String valueString = new JobControllerBean().getJobmapIconIdMonitorDefault();

		HinemosPropertyResponseP1 dtoRes = new HinemosPropertyResponseP1();
		dtoRes.setValue(valueString);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブマップ用アイコンイメージ（ファイル転送ジョブ用）のデフォルトアイコンIDを取得する。<BR>
	 * 
	 * @return HinemosPropertyResponseP1
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/hinemosProperty_forJobMap/jobmapIconIdDefaultFilejob")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobmapIconIdFileDefault")
	@RestLog(action = LogAction.Get, target = LogTarget.HinemosProperty, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosPropertyResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getJobmapIconIdFileDefault(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJobmapIconIdFileDefault()");

		String valueString = new JobControllerBean().getJobmapIconIdFileDefault();

		HinemosPropertyResponseP1 dtoRes = new HinemosPropertyResponseP1();
		dtoRes.setValue(valueString);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブマップ用アイコンイメージ（ジョブ連携送信ジョブ用）のデフォルトアイコンIDを取得する。<BR>
	 * 
	 * @return HinemosPropertyResponseP1
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/hinemosProperty_forJobMap/jobmapIconIdDefaultJobLinkSendjob")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobmapIconIdJobLinkSendDefault")
	@RestLog(action = LogAction.Get, target = LogTarget.HinemosProperty, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosPropertyResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getJobmapIconIdJobLinkSendDefault(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJobmapIconIdJobLinkSendDefault()");

		String valueString = new JobControllerBean().getJobmapIconIdJobLinkSendDefault();

		HinemosPropertyResponseP1 dtoRes = new HinemosPropertyResponseP1();
		dtoRes.setValue(valueString);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブマップ用アイコンイメージ（ジョブ連携待機ジョブ用）のデフォルトアイコンIDを取得する。<BR>
	 * 
	 * @return HinemosPropertyResponseP1
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/hinemosProperty_forJobMap/jobmapIconIdDefaultJobLinkRcvjob")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobmapIconIdJobLinkRcvDefault")
	@RestLog(action = LogAction.Get, target = LogTarget.HinemosProperty, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosPropertyResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getJobmapIconIdJobLinkRcvDefault(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJobmapIconIdJobLinkRcvDefault()");

		String valueString = new JobControllerBean().getJobmapIconIdJobLinkRcvDefault();

		HinemosPropertyResponseP1 dtoRes = new HinemosPropertyResponseP1();
		dtoRes.setValue(valueString);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブマップ用アイコンイメージ（ファイルチェックジョブ用）のデフォルトアイコンIDを取得する。<BR>
	 * 
	 * @return HinemosPropertyResponseP1
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/hinemosProperty_forJobMap/jobmapIconIdDefaultFilecheckjob")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobmapIconIdFileCheckDefault")
	@RestLog(action = LogAction.Get, target = LogTarget.HinemosProperty, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosPropertyResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getJobmapIconIdFileCheckDefault(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJobmapIconIdFileCheckDefault()");

		String valueString = new JobControllerBean().getJobmapIconIdFileCheckDefault();

		HinemosPropertyResponseP1 dtoRes = new HinemosPropertyResponseP1();
		dtoRes.setValue(valueString);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブマップ用アイコンイメージ（RPAシナリオジョブ用）のデフォルトアイコンIDを取得する。<BR>
	 * 
	 * @return HinemosPropertyResponseP1
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/hinemosProperty_forJobMap/jobmapIconIdDefaultRpajob")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobmapIconIdRpaDefault")
	@RestLog(action = LogAction.Get, target = LogTarget.HinemosProperty, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosPropertyResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getJobmapIconIdRpaDefault(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJobmapIconIdRpaDefault()");

		String valueString = new JobControllerBean().getJobmapIconIdRpaDefault();

		HinemosPropertyResponseP1 dtoRes = new HinemosPropertyResponseP1();
		dtoRes.setValue(valueString);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 構成情報CSVファイルダウンロードで一度に取得する情報のノード数を取得します。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @return HinemosPropertyResponseP1
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/hinemosProperty_forNodeMap/nodemapDownloadNodeConfigCount")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetDownloadNodeConfigCount")
	@RestLog(action = LogAction.Get, target = LogTarget.HinemosProperty, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosPropertyResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getDownloadNodeConfigCount(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getDownloadNodeConfigCount()");

		int maxsize = 0;
		maxsize = HinemosPropertyCommon.nodemap_download_node_config_count.getIntegerValue();

		HinemosPropertyResponseP1 dtoRes = new HinemosPropertyResponseP1();
		dtoRes.setValue(String.valueOf(maxsize));

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * イベントの画面表示設定を取得します。<BR>
	 * 
	 * @return EventDisplaySettingInfoResponse
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/hinemosProperty_forMonitor/monitorEventUseritemItem_Related")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetEventDisplaySettingInfo")
	@RestLog(action = LogAction.Get, target = LogTarget.HinemosProperty, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorResult, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventDisplaySettingInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getEventDisplaySettingInfo(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getEventDisplaySettingInfo()");

		EventDisplaySettingInfo infoRes = new MonitorControllerBean().getEventDisplaySettingInfo();
		EventDisplaySettingInfoResponse dtoRes = new EventDisplaySettingInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * イベントカスタムコマンドの設定を取得します。<BR>
	 * 
	 * @return CommandTemplateResponse
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/hinemosProperty_forMonitor/monitorEventCustomcmd_Related")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetEventCustomCommandSettingInfo")
	@RestLog(action = LogAction.Get, target = LogTarget.HinemosProperty, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorResult, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventCustomCommandInfoDataResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getEventCustomCommandSettingInfo(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getEventCustomCommandSettingInfo()");

		EventCustomCommandInfoData infoRes = new EventCustomCommandBean().getEventCustomCommandSettingInfo();
		EventCustomCommandInfoDataResponse dtoRes = new EventCustomCommandInfoDataResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * クラウドの設定インポートエクスポート時のアカウント情報保護の要否を取得します。<BR>
	 * 
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/hinemosProperty_forUtility/utilityExpimpXcloudKeyprotectEnable")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetExpimpXcloudKeyprotectEnable")
	@RestLog(action = LogAction.Get, target = LogTarget.HinemosProperty, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HinemosPropertyResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getExpimpXcloudKeyprotectEnable(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getExpimpXcloudKeyprotectEnable()");

		boolean enable = HinemosPropertyCommon.utility_expimp_xcloud_keyprotect_enable.getBooleanValue();

		HinemosPropertyResponseP1 dtoRes = new HinemosPropertyResponseP1();
		dtoRes.setValue(String.valueOf(enable));

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * コマンドテンプレートの設定を取得します。<BR>
	 * 
	 * @return CommandTemplateResponse
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws CommandTemplateNotFound 
	 */
	@GET
	@Path("/commandTemplate/{commandTemplateId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCommandTemplate")
	@RestLog(action = LogAction.Get, target = LogTarget.CommandTemplate, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CommandTemplateResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getCommandTemplate(@PathParam(value = "commandTemplateId") String commandTemplateId, @Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, CommandTemplateNotFound {
		m_log.info("call getCommandTemplate()");

		CommandTemplateInfo infoRes = CommandTemplateControllerBean.bean().getCommandTemplate(commandTemplateId);

		CommandTemplateResponse dtoRes = new CommandTemplateResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * コマンド通知テンプレートの全設定を取得します。<BR>
	 * 
	 * @return CommandTemplateResponse[]
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/commandTemplate")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCommandTemplateList")
	@RestLog(action = LogAction.Get, target = LogTarget.CommandTemplate, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CommandTemplateResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getCommandTemplateList(@QueryParam(value = "ownerRoleId") String ownerRoleId, @Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getCommandTemplateList()");

		List<CommandTemplateInfo> infoResList = CommandTemplateControllerBean.bean().getCommandTemplateList(ownerRoleId);

		List<CommandTemplateResponse> dtoResList = new ArrayList<>();
		for (CommandTemplateInfo infoRes : infoResList) {
			CommandTemplateResponse dtoRes = new CommandTemplateResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * コマンド通知テンプレートの設定を追加します。<BR>
	 * 
	 * @return CommandTemplateResponse
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting 
	 * @throws CommandTemplateDuplicate 
	 * @throws CommandTemplateNotFound 
	 */
	@POST
	@Path("/commandTemplate")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCommandTemplate")
	@RestLog(action = LogAction.Add, target = LogTarget.CommandTemplate, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CommandTemplateResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response addCommandTemplate(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addCommandTemplateBody", content = @Content(schema = @Schema(implementation = AddCommandTemplateRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, CommandTemplateDuplicate {
		m_log.info("call addCommandTemplate()");

		AddCommandTemplateRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddCommandTemplateRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		CommandTemplateInfo infoReq = new CommandTemplateInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);

		CommandTemplateInfo infoRes = CommandTemplateControllerBean.bean().addCommandTemplate(infoReq);

		CommandTemplateResponse dtoRes = new CommandTemplateResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * コマンド通知テンプレートの設定を変更します。<BR>
	 * 
	 * @return CommandTemplateResponse
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting 
	 * @throws CommandTemplateNotFound 
	 */
	@PUT
	@Path("/commandTemplate/{commandTemplateId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCommandTemplate")
	@RestLog(action = LogAction.Modify, target = LogTarget.CommandTemplate, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CommandTemplateResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response modifyCommandTemplate(@PathParam(value = "commandTemplateId") String commandTemplateId, @Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "modifyCommandTemplateBody", content = @Content(schema = @Schema(implementation = ModifyCommandTemplateRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, CommandTemplateNotFound {
		m_log.info("call modifyCommandTemplate()");

		ModifyCommandTemplateRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyCommandTemplateRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		CommandTemplateInfo infoReq = new CommandTemplateInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setCommandTemplateId(commandTemplateId);

		CommandTemplateInfo infoRes = CommandTemplateControllerBean.bean().modifyCommandTemplate(infoReq);

		CommandTemplateResponse dtoRes = new CommandTemplateResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * コマンド通知テンプレートの設定を削除します。<BR>
	 * 
	 * @return CommandTemplateResponse[]
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws CommandTemplateNotFound 
	 * @throws UsedCommandTemplate 
	 */
	@DELETE
	@Path("/commandTemplate")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteCommandTemplate")
	@RestLog(action = LogAction.Delete, target = LogTarget.CommandTemplate, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CommandTemplateResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response deleteCommandTemplate(@ArrayTypeParam @QueryParam(value = "commandTemplateIds") String commandTemplateIds, @Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, CommandTemplateNotFound, UsedCommandTemplate {
		m_log.info("call deleteCommandTemplate()");

		List<String> commandTemplateIdList = new ArrayList<>();
		if(commandTemplateIds != null && !commandTemplateIds.isEmpty()) {
			commandTemplateIdList = Arrays.asList(commandTemplateIds.split(","));
		}
		List<CommandTemplateInfo> infoResList = CommandTemplateControllerBean.bean().deleteCommandTemplate(commandTemplateIdList);
		List<CommandTemplateResponse> dtoResList = new ArrayList<>();
		for (CommandTemplateInfo infoRes : infoResList) {
			CommandTemplateResponse dtoRes = new CommandTemplateResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}


	private void convertToResponseValue(HinemosPropertyInfo infoRes, HinemosPropertyResponse dtoRes) {
		if (HinemosPropertyTypeEnum.STRING.getCode().equals(infoRes.getValueType())) {
			dtoRes.setType(HinemosPropertyTypeEnum.STRING);
			dtoRes.setValue(infoRes.getValueString());
		} else if (HinemosPropertyTypeEnum.NUMERIC.getCode().equals(infoRes.getValueType())) {
			dtoRes.setType(HinemosPropertyTypeEnum.NUMERIC);
			if (infoRes.getValueNumeric() != null) {
				dtoRes.setValue(String.valueOf(infoRes.getValueNumeric()));
			}
		} else if (HinemosPropertyTypeEnum.BOOLEAN.getCode().equals(infoRes.getValueType())) {
			dtoRes.setType(HinemosPropertyTypeEnum.BOOLEAN);
			dtoRes.setValue(String.valueOf(infoRes.getValueBoolean()));
		}
	}

	/**
	 * RESTアクセス情報をマネージャに登録します。<BR>
	 * 
	 * @param AddRestAccessInfoRequest
	 * @return RestAccessInfoResponse
	 * @throws HinemosUnknown
	 * @throws RestAccessDuplicate
	 * @throws InvalidUserPasss
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 * 
	 */
	@POST
	@Path("/restAccess")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddRestAccessInfo")
	@RestLog(action = LogAction.Add, target = LogTarget.RestAccessInfo, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.ADD })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RestAccessInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addRestAccessInfo(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addRestAccessInfoBody", content = @Content(schema = @Schema(implementation = AddRestAccessInfoRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, RestAccessDuplicate {
		m_log.info("call addRestAccessInfo()");

		AddRestAccessInfoRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddRestAccessInfoRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		RestAccessInfo infoReq = new RestAccessInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		//infoの不足設定を補完
		paddingRestAccessInfo(infoReq);
		
		RestAccessInfo infoRes = new RestAccessInfoControllerBean().addRestAccess(infoReq);

		RestAccessInfoResponse dtoRes = new RestAccessInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	/**
	 * マネージャ上のRESTアクセス情報を変更します。<BR>
	 * 
	 * @param ModifyRestAccessInfoRequest
	 * @return RestAccessInfoResponse
	 * @throws HinemosUnknown
	 * @throws RestAccessNotFound 
	 * @throws InvalidUserPasss
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 * 
	 */
	@PUT
	@Path("/restAccess/{restAccessId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyRestAccessInfo")
	@RestLog(action = LogAction.Modify, target = LogTarget.RestAccessInfo, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RestAccessInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyRestAccessInfo(@PathParam("restAccessId") String restAccessId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyRestAccessInfoBody", content = @Content(schema = @Schema(implementation = ModifyRestAccessInfoRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, RestAccessNotFound {
		m_log.info("call modifyRestAccessInfo()");

		ModifyRestAccessInfoRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyRestAccessInfoRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		RestAccessInfo infoReq = new RestAccessInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setRestAccessId(restAccessId);
		//infoの不足設定を補完
		paddingRestAccessInfo(infoReq);

		RestAccessInfo infoRes = new RestAccessInfoControllerBean().modifyRestAccess(infoReq);

		RestAccessInfoResponse dtoRes = new RestAccessInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * RESTアクセス情報をマネージャから削除します。<BR>
	 * 
	 * @param restAccessIds
	 *            削除対象のID
	 * @return RestAccessInfoResponse[]
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws RestAccessNotFound 
	 * 
	 */
	@DELETE
	@Path("/restAccess")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteRestAccessInfo")
	@RestLog(action = LogAction.Delete, target = LogTarget.RestAccessInfo, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RestAccessInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteRestAccessInfo(@ArrayTypeParam @QueryParam(value = "restAccessIds") String restAccessIds,
			@Context Request request, @Context UriInfo uriInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole, RestAccessNotFound, RestAccessUsed {
		m_log.info("call deleteRestAccessInfo()");

		List<String> restAccessIdList  = new ArrayList<>();
		if (!(StringUtil.isNullOrEmpty(restAccessIds))) {
			restAccessIdList = Arrays.asList(restAccessIds.split(","));
		}
		List<RestAccessInfo> infoResList = new RestAccessInfoControllerBean().deleteRestAccess(restAccessIdList);
		List<RestAccessInfoResponse> dtoResList = new ArrayList<>();
		for (RestAccessInfo info : infoResList) {
			RestAccessInfoResponse dto = new RestAccessInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
	/**
	 * 引数で指定されたRESTアクセス情報を返します。
	 * 
	 * @param restAccessId
	 *            取得対象のID
	 * @return RestAccessIdResponse
	 * @throws HinemosUnknown
	 * @throws RestAccessNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 *
	 */
	@GET
	@Path("/restAccess/{restAccessId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRestAccessInfo")
	@RestLog(action = LogAction.Get, target = LogTarget.RestAccessInfo, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RestAccessInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRestAccessInfo(@PathParam(value = "restAccessId") String restAccessId,
			@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, RestAccessNotFound {
		m_log.info("call getRestAccessInfo()");

		RestAccessInfo infoRes = new RestAccessInfoControllerBean().getRestAccessInfo(restAccessId);
		RestAccessInfoResponse dtoRes = new RestAccessInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * RESTアクセス情報一覧を取得します。<BR>
	 * 
	 * @param ownerRoleId
	 * @return RestAccessInfoResponse[]
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 */
	@GET
	@Path("/restAccess")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRestAccessInfoList")
	@RestLog(action = LogAction.Get, target = LogTarget.RestAccessInfo, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RestAccessInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRestAccessInfoList(@QueryParam(value = "ownerRoleId") String ownerRoleId, @Context Request request,
			@Context UriInfo uriInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getRestAccessInfoList()");

		List<RestAccessInfo> infoResList = new RestAccessInfoControllerBean()
				.getRestAccessListByOwnerRole(ownerRoleId);
		List<RestAccessInfoResponse> dtoResList = new ArrayList<>();
		for (RestAccessInfo info : infoResList) {
			RestAccessInfoResponse dto = new RestAccessInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	public static void paddingRestAccessInfo( RestAccessInfo info){
		if(info.getSendHttpHeaders() != null){
			for( RestAccessSendHttpHeader rec : info.getSendHttpHeaders()){
				rec.getId().setRestAccessId(info.getRestAccessId());
			}
		}
		if(info.getAuthHttpHeaders() != null){
			for( RestAccessAuthHttpHeader rec : info.getAuthHttpHeaders()){
				rec.getId().setRestAccessId(info.getRestAccessId());
			}
		}
	}

}
