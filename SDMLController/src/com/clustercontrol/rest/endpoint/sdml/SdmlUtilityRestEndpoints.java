/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.sdml;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.glassfish.grizzly.http.server.Request;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeFunction;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
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
import com.clustercontrol.rest.endpoint.sdml.dto.ImportSdmlControlRequest;
import com.clustercontrol.rest.endpoint.sdml.dto.ImportSdmlControlResponse;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.sdml.v1.utility.ImportSdmlControlV1Controller;

/**
 * SDMLの設定インポートエクスポート向けのエンドポイントクラス<br>
 * 
 * 本クラスのリソースメソッドには@Tag(name = "sdml")を付与すること。<br>
 * 上記を付与することにより、クライアントではcom.clustercontrol.rest.client.SdmlApiクラスからAPIにアクセスされる。
 * (本体側APIのDefaultApiクラスとは別名にする必要がある。)<br>
 * 
 * SDMLのバージョンアップ時にRestKindなどクライアントの本体側のjarに影響を与えないために、クラス名とクラスのPathを共通化する。<br>
 * バージョンごとに用意する必要があるAPIは各メソッドのPathにバージョンを付与すること。
 */
@Path("/utility")
@RestLogFunc(name = LogFuncName.Utility)
public class SdmlUtilityRestEndpoints {
	private static Log logger = LogFactory.getLog(SdmlUtilityRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "utility";

	/**
	 * SDML制御設定のインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/sdmlControlSetting/v1")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportSdmlControlSettingV1")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportSdmlControlResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.controlSetting, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.SdmlSetting, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Tag(name = "sdml")
	public Response importSdmlControlSettingV1(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportSdmlControlSettingV1", content = @Content(schema = @Schema(implementation = ImportSdmlControlRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		logger.info("call importSdmlControlSettingV1()");

		ImportSdmlControlRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportSdmlControlRequest.class);

		ImportSdmlControlResponse resDto = new ImportSdmlControlResponse();

		ImportSdmlControlV1Controller controller = new ImportSdmlControlV1Controller(dtoReq.isRollbackIfAbnormal(),
				dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException(controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

}
