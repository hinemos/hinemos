/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.nodemap;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_404;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_409;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeFunction;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.bean.ActivationKeyConstant;
import com.clustercontrol.fault.BgFileNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NodeMapException;
import com.clustercontrol.nodemap.bean.NodeMapModel;
import com.clustercontrol.nodemap.model.MapBgImageEntity;
import com.clustercontrol.nodemap.model.MapIconImageEntity;
import com.clustercontrol.nodemap.session.NodeMapControllerBean;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rest.annotation.RestLog;
import com.clustercontrol.rest.annotation.RestLog.LogAction;
import com.clustercontrol.rest.annotation.RestLog.LogTarget;
import com.clustercontrol.rest.annotation.RestLog.LogType;
import com.clustercontrol.rest.annotation.RestLogFunc;
import com.clustercontrol.rest.annotation.RestLogFunc.LogFuncName;
import com.clustercontrol.rest.endpoint.jobmap.dto.CheckPublishResponse;
import com.clustercontrol.rest.endpoint.nodemap.dto.AddBgImageRequest;
import com.clustercontrol.rest.endpoint.nodemap.dto.AddIconImageRequest;
import com.clustercontrol.rest.endpoint.nodemap.dto.DownloadNodeConfigFileRequest;
import com.clustercontrol.rest.endpoint.nodemap.dto.ExistBgImageResponse;
import com.clustercontrol.rest.endpoint.nodemap.dto.ExistIconImageResponse;
import com.clustercontrol.rest.endpoint.nodemap.dto.MapBgImageInfoResponse;
import com.clustercontrol.rest.endpoint.nodemap.dto.MapIconImageInfoResponse;
import com.clustercontrol.rest.endpoint.nodemap.dto.NodeMapModelResponse;
import com.clustercontrol.rest.endpoint.nodemap.dto.RegisterNodeMapModelRequest;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.IgnoreCommandline;
import com.clustercontrol.rest.annotation.cmdtool.IgnoreReference;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonConverter;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.rest.util.RestTempFileUtil;
import com.clustercontrol.util.KeyCheck;
import com.clustercontrol.util.MessageConstant;

@Path("/nodemap")
@RestLogFunc(name = LogFuncName.NodeMap)
public class NodeMapRestEndpoints {
	private static Log m_log = LogFactory.getLog(NodeMapRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "nodemap";

	/**
	 * ノードマップを登録します。
	 * @param requestBody
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 */
	@POST
	@Path("/nodemap")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "RegisterNodeMapModel")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NodeMapModelResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Add, target = LogTarget.Nodemap, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerNodeMapModel(
			@RequestBody(description = "RegisterNodeMapModelBody", content = @Content(schema = @Schema(implementation = RegisterNodeMapModelRequest.class))) String requestBody,
			@Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting, NodeMapException {
		m_log.info("call registerNodeMapModel()");

		RegisterNodeMapModelRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				RegisterNodeMapModelRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		NodeMapModel infoReq = new NodeMapModel();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		NodeMapModel infoRes = new NodeMapControllerBean().registerNodeMapModel(infoReq);
		NodeMapModelResponse dtoRes = new NodeMapModelResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ノードマップを取得します。
	 * 
	 * @param facilityId
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws NodeMapException
	 */
	@GET
	@Path("/nodemap/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetNodeMapModel")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NodeMapModelResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action=LogAction.Get, target = LogTarget.Nodemap, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNodeMapModel(@PathParam(value = "facilityId") String facilityId, @Context Request request,
			@Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown, NodeMapException {
		m_log.info("call getNodeMapModel()");

		NodeMapModel infoRes = new NodeMapControllerBean().getNodeMapModel(facilityId);
		NodeMapModelResponse dtoRes = new NodeMapModelResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 構成情報をダウンロードします。
	 * 
	 * @param request
	 * @param requestBody
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@POST
	@Path("/repository/node_download")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DownloadNodeConfigFile")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action=LogAction.Download, target = LogTarget.Node, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	@Consumes(MediaType.APPLICATION_JSON)
	public Response downloadNodeConfigFile(@Context Request request,
			@RequestBody(description = "DownloadNodeConfigFileBody", content = @Content(schema = @Schema(implementation = DownloadNodeConfigFileRequest.class))) String requestBody,
			@Context UriInfo uriInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call downloadNodeConfigFile()");

		DownloadNodeConfigFileRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				DownloadNodeConfigFileRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();
		
		Long targetDatetime = 0L;
		if (!dtoReq.getTargetDatetime().isEmpty()) {
			targetDatetime = RestCommonConverter.convertDTStringToHinemosTime(dtoReq.getTargetDatetime(), MessageConstant.TARGET_DATETIME.getMessage());
		}
		File file = new RepositoryControllerBean().downloadNodeConfigFile(dtoReq.getFacilityIdList(),
				targetDatetime, dtoReq.getConditionStr(), new Locale(dtoReq.getLanguage()),
				dtoReq.getManagerName(), dtoReq.getItemList(), dtoReq.getNeedHeaderInfo());
		StreamingOutput stream = RestTempFileUtil.getTempFileStream(file);
		return Response.ok(stream).header("Content-Disposition", "filename=\"" + file.getName() + "\"").build();
	}

	/**
	 * ノードマップの背景画像をダウンロードします。
	 * 
	 * @param filename
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws NodeMapException
	 * @throws BgFileNotFound
	 */
	@GET
	@Path("/backgroundImage_download/{filename}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DownloadBgImage")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action=LogAction.Download, target = LogTarget.BackgroundImage, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	public Response downloadBgImage(@PathParam(value = "filename") String filename, @Context Request request,
			@Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting, NodeMapException, BgFileNotFound {
		m_log.info("call downloadBgImage()");

		File file = new NodeMapControllerBean().getBgImage(filename);
		StreamingOutput stream = RestTempFileUtil.getTempFileStream(file);
		return Response.ok(stream).header("Content-Disposition", "filename=\"" + filename + "\"").build();
	}

	/**
	 * ノードマップの背景画像を登録します。
	 * 
	 * @param inputStream
	 * @param requestBody
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws NodeMapException
	 */
	@POST
	@Path("/backgroundImage")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddBgImage")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MapBgImageInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Add, target = LogTarget.BackgroundImage, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response addBgImage(
			@RequestBody(description = "addBgImageBody", content = @Content(schema = @Schema(type = SchemaType.OBJECT, implementation = AddBgImageRequest.class))) @FormDataParam("file") InputStream inputStream,
			@FormDataParam("mapBgImage") String requestBody, @Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting, NodeMapException {
		m_log.info("call addBgImage()");

		AddBgImageRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddBgImageRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		// 画像データをinputStreamからbyte[]へ変換しinfoReqにsetする
		MapBgImageEntity infoRes = new NodeMapControllerBean().setBgImage(dtoReq.getFilename(),
				convertInputStreamToByteArray(inputStream));
		MapBgImageInfoResponse dtoRes = new MapBgImageInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ノードマップの背景画像のファイル名を全て取得します。
	 * 
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/backgroundImage_filename")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetBgImageFilename")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MapBgImageInfoResponse.class, type=SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action=LogAction.Get, target = LogTarget.BackgroundImage, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBgImageFilename(@Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getBgImageFilename()");

		Collection<String> filenames = new NodeMapControllerBean().getBgImagePK();
		List<MapBgImageInfoResponse> dtoResList = new ArrayList<>();
		for (String filename : filenames) {
			MapBgImageInfoResponse dtoRes = new MapBgImageInfoResponse();
			dtoRes.setFilename(filename);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * ノードマップの背景画像が存在するかどうか調べます。
	 * 
	 * @param filename
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/backgroundImage_exist/{filename}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ExistBgImage")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExistBgImageResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action=LogAction.Get, target = LogTarget.BackgroundImage, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@Produces(MediaType.APPLICATION_JSON)
	public Response existBgImage(@PathParam(value = "filename") String filename, @Context Request request,
			@Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call existBgImage()");

		boolean exist = new NodeMapControllerBean().isBgImage(filename);
		ExistBgImageResponse dtoRes = new ExistBgImageResponse();
		dtoRes.setExist(exist);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ノードマップのアイコン画像をダウンロードします。
	 * 
	 * @param filename
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws NodeMapException
	 * @throws IconFileNotFound
	 */
	@GET
	@Path("/iconImage_download/{filename}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DownloadIconImage")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Download, target = LogTarget.IconImage, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	public Response downloadIconImage(@PathParam(value = "filename") String filename, @Context Request request,
			@Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting, NodeMapException, IconFileNotFound {
		m_log.info("call downloadIconImage()");

		File file = new NodeMapControllerBean().getIconImage(filename);
		StreamingOutput stream = RestTempFileUtil.getTempFileStream(file);
		return Response.ok(stream).header("Content-Disposition", "filename=\"" + filename + "\"").build();
	}

	/**
	 * ノードマップのアイコン画像を登録します。
	 * 
	 * @param inputStream
	 * @param requestBody
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws NodeMapException
	 */
	@POST
	@Path("/iconImage")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddIconImage")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MapIconImageInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Add, target = LogTarget.IconImage, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response addIconImage(
			@RequestBody(description = "addIconImageBody", content = @Content(schema = @Schema(type = SchemaType.OBJECT, implementation = AddIconImageRequest.class))) @FormDataParam("file") InputStream inputStream,
			@FormDataParam("mapIconImage") String requestBody, @Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting, NodeMapException {
		m_log.info("call addIconImage()");

		AddIconImageRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddIconImageRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		// 画像データをinputStreamからbyte[]へ変換しinfoReqにsetする
		MapIconImageEntity infoRes = new NodeMapControllerBean().setIconImage(dtoReq.getFilename(),
				convertInputStreamToByteArray(inputStream));
		MapIconImageInfoResponse dtoRes = new MapIconImageInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ノードマップのアイコン画像のファイル名を全て取得します。
	 * 
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/iconImage_filename")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetIconImageFilename")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MapIconImageInfoResponse.class, type=SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action=LogAction.Get, target = LogTarget.IconImage, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getIconImageFilename(@Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getIconImageFilename()");

		Collection<String> filenames = new NodeMapControllerBean().getIconImagePK();
		List<MapIconImageInfoResponse> dtoResList = new ArrayList<>();
		for (String filename : filenames) {
			MapIconImageInfoResponse dtoRes = new MapIconImageInfoResponse();
			dtoRes.setFilename(filename);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * ノードマップのアイコン画像が存在するかどうか調べます。
	 * 
	 * @param filename
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/iconImage_exist/{filename}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ExistIconImage")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExistIconImageResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action=LogAction.Get, target = LogTarget.IconImage, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.READ })
	@Produces(MediaType.APPLICATION_JSON)
	public Response existIconImage(@PathParam(value = "filename") String filename, @Context Request request,
			@Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call existIconImage()");

		boolean exist = new NodeMapControllerBean().isIconImage(filename);
		ExistIconImageResponse dtoRes = new ExistIconImageResponse();
		dtoRes.setExist(exist);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	/**
	 * クライアントがマネージャのサブスクリプション有無の判定に使用します。
	 * 
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/checkPublish")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "CheckPublish")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CheckPublishResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action=LogAction.Get, target = LogTarget.CheckPublish, type = LogType.REFERENCE)
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response checkPublish(@Context Request request, @Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call checkPublish()");
		
		boolean publish = KeyCheck.checkKey(ActivationKeyConstant.TYPE_ENTERPRISE);
		
		CheckPublishResponse dtoRes = new CheckPublishResponse();
		dtoRes.setPublish(publish);
		
		RestLanguageConverter.convertMessages(dtoRes);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}

	private byte[] convertInputStreamToByteArray(InputStream is) throws HinemosUnknown, InvalidSetting {
		if (is == null) {
			m_log.warn("inputstream is null.");
			throw new InvalidSetting();
		}
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		try {
			while ((len = is.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}
		} catch (IOException e) {
			m_log.warn("failed convert inputstream to bytearray.");
			throw new HinemosUnknown(e);
		}
		return os.toByteArray();

	}
}
