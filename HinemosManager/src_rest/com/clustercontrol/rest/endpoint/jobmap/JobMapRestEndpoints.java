/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.jobmap;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_404;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_409;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import java.io.File;
import java.io.InputStream;
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
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.IconFileDuplicate;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.jobmanagement.bean.JobmapIconImage;
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
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobTypeEnum;
import com.clustercontrol.rest.endpoint.jobmap.dto.AddJobmapIconImageRequest;
import com.clustercontrol.rest.endpoint.jobmap.dto.CheckPublishResponse;
import com.clustercontrol.rest.endpoint.jobmap.dto.JobmapIconIdDefaultListResponse;
import com.clustercontrol.rest.endpoint.jobmap.dto.JobmapIconImageInfoResponse;
import com.clustercontrol.rest.endpoint.jobmap.dto.ModifyJobmapIconImageRequest;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestByteArrayConverter;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.rest.util.RestTempFileType;
import com.clustercontrol.rest.util.RestTempFileUtil;
import com.clustercontrol.util.KeyCheck;

@Path("/jobmap")
@RestLogFunc(name = LogFuncName.JobMap)
public class JobMapRestEndpoints {
	private static Log m_log = LogFactory.getLog(JobMapRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "jobmap";
	
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
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.CheckPublish, type = LogType.REFERENCE)
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


	/**
	 * ジョブマップのアイコン画像を登録します。
	 * 
	 * @param inputStream
	 * @param requestBody
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 * @throws IconFileDuplicate
	 */
	@POST
	@Path("/iconImage")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddJobmapIconImage")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobmapIconImageInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@RestLog(action = LogAction.Add, target = LogTarget.IconImage, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.ADD })
	public Response addJobmapIconImage(
			@RequestBody(description = "addJobmapIconImageBody", content = @Content(schema = @Schema(type = SchemaType.OBJECT, implementation = AddJobmapIconImageRequest.class))) @FormDataParam("file") InputStream inputStream,
			@FormDataParam("JobmapIconImage") String requestBody, @Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, IconFileDuplicate {
		m_log.info("call addJobmapIconImage()");

		AddJobmapIconImageRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddJobmapIconImageRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobmapIconImage infoReq = new JobmapIconImage();
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// 画像データをinputStreamからbyte[]へ変換
		infoReq.setFiledata(RestByteArrayConverter.convertInputStreamToByteArray(inputStream));

		JobmapIconImage infoRes = new JobControllerBean().addJobmapIconImage(infoReq);
		JobmapIconImageInfoResponse dtoRes = new JobmapIconImageInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブマップのアイコン画像を更新します。
	 * 
	 * @param iconId 更新対象のアイコンID
	 * @param inputStream
	 * @param requestBody
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 * @throws IconFileNotFound
	 */
	@PUT
	@Path("/iconImage/{iconId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyJobmapIconImage")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobmapIconImageInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@RestLog(action = LogAction.Modify, target = LogTarget.IconImage, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	public Response modifyJobmapIconImage(@PathParam(value = "iconId") String iconId,
			@RequestBody(description = "modifyJobmapIconImageBody", content = @Content(schema = @Schema(type = SchemaType.OBJECT, implementation = ModifyJobmapIconImageRequest.class))) @FormDataParam("file") InputStream inputStream,
			@FormDataParam("JobmapIconImage") String requestBody, @Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, IconFileNotFound {
		m_log.info("call modifyJobmapIconImage()");

		ModifyJobmapIconImageRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyJobmapIconImageRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobmapIconImage infoReq = new JobmapIconImage();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setIconId(iconId);

		// 画像データをinputStreamからbyte[]へ変換
		infoReq.setFiledata(RestByteArrayConverter.convertInputStreamToByteArray(inputStream));

		JobmapIconImage infoRes = new JobControllerBean().modifyJobmapIconImage(infoReq);
		JobmapIconImageInfoResponse dtoRes = new JobmapIconImageInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブマップのアイコン画像を削除します。
	 * 
	 * @param iconId 削除対象のアイコンID
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 * @throws IconFileNotFound
	 */
	@DELETE
	@Path("/iconImage")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteJobmapIconImage")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobmapIconImageInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.IconImage, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	public Response deleteJobmapIconImage(@QueryParam(value = "iconIds") @ArrayTypeParam String iconIds,
			@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, IconFileNotFound {
		m_log.info("call deleteJobmapIconImage()");

		List<String> iconIdList = new ArrayList<>();
		if(iconIds != null && !iconIds.isEmpty()) {
			iconIdList = Arrays.asList(iconIds.split(","));
		}
		
		List<JobmapIconImage> infoResList = new JobControllerBean().deleteJobmapIconImage(iconIdList);
		List<JobmapIconImageInfoResponse> dtoResList = new ArrayList<>();
		for (JobmapIconImage infoRes : infoResList) {
			JobmapIconImageInfoResponse dtoRes = new JobmapIconImageInfoResponse();
			RestBeanUtil.convertBean(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * ジョブマップのアイコン画像のDTOを取得します。<BR>
	 * 画像データはDTOに含まれません。
	 * 
	 * @param iconId 取得対象のアイコンID
	 * @param request
	 * @param uriInfo
	 * @return アイコン画像のDTO
	 * @throws IconFileNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/iconImage/{iconId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobmapIconImage")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobmapIconImageInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.IconImage, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getJobmapIconImage(@PathParam(value = "iconId") String iconId, @Context Request request,
			@Context UriInfo uriInfo) throws IconFileNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJobmapIconImage()");

		JobmapIconImage infoRes = new JobControllerBean().getJobmapIconImage(iconId);
		JobmapIconImageInfoResponse dtoRes = new JobmapIconImageInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブマップのアイコン画像の画像ファイルをダウンロードします。
	 * 
	 * @param iconId ダウンロード対象のアイコンID
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws IconFileNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/iconImage_file/{iconId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DownloadJobmapIconImageFile")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	@RestLog(action = LogAction.Get, target = LogTarget.IconImage, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	public Response downloadJobmapIconImageFile(@PathParam(value = "iconId") String iconId, @Context Request request,
			@Context UriInfo uriInfo) throws IconFileNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call downloadJobmapIconImageFile()");

		JobmapIconImage infoRes = new JobControllerBean().getJobmapIconImage(iconId);

		File file = RestByteArrayConverter.convertByteArrayToFile(infoRes.getFiledata(), RestTempFileType.JOBMAP_ICONIMAGE);
		StreamingOutput stream = RestTempFileUtil.getTempFileStream(file);

		return Response.ok(stream).header("Content-Disposition", "filename=\"" + file.getName() + "\"").build();
	}
	
	/**
	 * ジョブマップのアイコン画像のDTOを全て取得します。<BR>
	 * 画像データはDTOに含まれません。
	 * 
	 * @param request
	 * @param uriInfo
	 * @return アイコン画像のDTOのリスト
	 * @throws IconFileNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/iconImage")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobmapIconImageList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobmapIconImageInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.IconImage, type = LogType.REFERENCE)
	public Response getJobmapIconImageList(@Context Request request, @Context UriInfo uriInfo) throws IconFileNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("getJobmapIconImageList :");
		
		List<JobmapIconImage> imageList = new JobControllerBean().getJobmapIconImageList();
		List<JobmapIconImageInfoResponse> listRes = new ArrayList<JobmapIconImageInfoResponse>();
		
		for (JobmapIconImage image : imageList) {
			JobmapIconImageInfoResponse dtoRes = new JobmapIconImageInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(image, dtoRes);
			listRes.add(dtoRes);
		}
		
		RestLanguageConverter.convertMessages(listRes);
		
		return Response.status(Status.OK).entity(listRes).build();
	}
	
	
	/**
	 * ジョブマップのデフォルトのアイコン画像のIDを取得します。
	 * 
	 * @param request
	 * @param uriInfo
	 * @return デフォルトのアイコン画像のIDのリスト
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/jobmapIconIdDefault")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobmapIconIdDefaultList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobmapIconIdDefaultListResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.JobMapIconIdDefault, type = LogType.REFERENCE)
	public Response getJobmapIconIdDefaultList(@Context Request request, @Context UriInfo uriInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobmapIconIdDefaultList :");
		
		List<JobmapIconIdDefaultListResponse> dtoResList = new ArrayList<JobmapIconIdDefaultListResponse>();
		JobControllerBean bean = new JobControllerBean();
		dtoResList.add(new JobmapIconIdDefaultListResponse(JobTypeEnum.JOB, bean.getJobmapIconIdJobDefault()));
		dtoResList.add(new JobmapIconIdDefaultListResponse(JobTypeEnum.JOBNET, bean.getJobmapIconIdJobnetDefault()));
		dtoResList.add(new JobmapIconIdDefaultListResponse(JobTypeEnum.APPROVALJOB, bean.getJobmapIconIdApprovalDefault()));
		dtoResList.add(new JobmapIconIdDefaultListResponse(JobTypeEnum.MONITORJOB, bean.getJobmapIconIdMonitorDefault()));
		dtoResList.add(new JobmapIconIdDefaultListResponse(JobTypeEnum.FILEJOB, bean.getJobmapIconIdFileDefault()));
		dtoResList.add(new JobmapIconIdDefaultListResponse(JobTypeEnum.FILECHECKJOB, bean.getJobmapIconIdFileCheckDefault()));
		dtoResList.add(new JobmapIconIdDefaultListResponse(JobTypeEnum.RESOURCEJOB, bean.getJobmapIconIdResourceDefault()));
		dtoResList.add(new JobmapIconIdDefaultListResponse(JobTypeEnum.JOBLINKSENDJOB, bean.getJobmapIconIdJobLinkSendDefault()));
		dtoResList.add(new JobmapIconIdDefaultListResponse(JobTypeEnum.JOBLINKRCVJOB, bean.getJobmapIconIdJobLinkRcvDefault()));
		dtoResList.add(new JobmapIconIdDefaultListResponse(JobTypeEnum.RPAJOB, bean.getJobmapIconIdRpaDefault()));

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
}
