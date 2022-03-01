/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.exception;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosDuplicate;
import com.clustercontrol.fault.HinemosInvalid;
import com.clustercontrol.fault.HinemosNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.HinemosUsed;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NodeMapElementNoPrivilege;
import com.clustercontrol.fault.NodeMapException;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.UnEditableRole;
import com.clustercontrol.fault.UnEditableUser;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.common.ErrorCode;

@Provider
public class HinemosRestExceptionMapper implements ExceptionMapper<Throwable> {

	private static Log m_log = LogFactory.getLog(HinemosRestExceptionMapper.class);

	private static final Map<String, Status> exceptionStatusMap = new HashMap<>();

	static {
		//各Exceptionに対応するResponseStatusの個別定義Map(exceptionStatusMap)の設定。
		// 個別対応が必要な場合(親クラスから判断できない)にのみ設定する。
		// HinemosDuplicate HinemosUsed HinemosInvalid HinemosNotFoundの継承クラスについては原則設定不要です。

		//400(BAD_REQUEST)
		exceptionStatusMap.put(UnEditableRole.class.getSimpleName(), Status.BAD_REQUEST);
		exceptionStatusMap.put(UnEditableUser.class.getSimpleName(), Status.BAD_REQUEST);
		exceptionStatusMap.put(NodeMapException.class.getSimpleName(), Status.BAD_REQUEST);

		//401(UNAUTHORIZED)
		//  HinemosInvalidの継承だが、個別定義
		exceptionStatusMap.put(InvalidUserPass.class.getSimpleName(), Status.UNAUTHORIZED);

		//403（FORBIDDEN）
		//  HinemosInvalidの継承だが、個別定義
		exceptionStatusMap.put(InvalidRole.class.getSimpleName(), Status.FORBIDDEN);
		exceptionStatusMap.put(ObjectPrivilege_InvalidRole.class.getSimpleName(), Status.FORBIDDEN);
		exceptionStatusMap.put(NodeMapElementNoPrivilege.class.getSimpleName(), Status.FORBIDDEN);

		//404(NOT_FOUND)
		//500(INTERNAL_SERVER_ERROR)
		exceptionStatusMap.put(HinemosUnknown.class.getSimpleName(), Status.INTERNAL_SERVER_ERROR);
	}

	@Override
	public Response toResponse(Throwable exception) {
		
		Status exceptionStatus = getResponseStatus(exception);
		
		return Response.status(exceptionStatus)
				.type(MediaType.APPLICATION_JSON)
				.entity(new ExceptionBody(exceptionStatus.getStatusCode(), replaceWithHinemosException(exception))).build();
	}

	public static Status getResponseStatus(Throwable exception) {
		String name = exception.getClass().getSimpleName();
		//定義Mapを検索
		Status exceptionStatus = exceptionStatusMap.get(name);
		//Mapになければ親クラスで検索
		if( exceptionStatus == null ){
			if( exception instanceof HinemosDuplicate ){
				exceptionStatus = Status.CONFLICT;
			}
			if( exception instanceof HinemosUsed ){
				exceptionStatus = Status.BAD_REQUEST;
			}
			if( exception instanceof HinemosInvalid ){
				exceptionStatus = Status.BAD_REQUEST;
			}
			if( exception instanceof HinemosNotFound ){
				exceptionStatus = Status.NOT_FOUND;
			}
			if( exception instanceof CloudManagerException ){
				exceptionStatus = getCloudResponseStatus(exception);
			}
			if( exception instanceof WebApplicationException){
				WebApplicationException e = (WebApplicationException)exception;
				exceptionStatus = Status.fromStatusCode(e.getResponse().getStatus());
			}
		}
		//該当がなければ内部エラー扱い
		if( exceptionStatus == null ){
			exceptionStatus = Status.INTERNAL_SERVER_ERROR;
		}
		
		return exceptionStatus;
	}
	
	//クラウドVM管理はErrorCodeからステータスコードを決定する
	public static Status getCloudResponseStatus(Throwable exception) {
		// findbugs対応 現実装では呼び出し側でチェック済みだが 再チェック
		if( !(exception instanceof CloudManagerException)){
			return Status.INTERNAL_SERVER_ERROR;
		}
		CloudManagerException ex = (CloudManagerException) exception;
		String errorCode = ex.getErrorCode();
		m_log.debug("getCloudResponseStatus() : ErrorCode=" + errorCode);
		try {
			switch (ErrorCode.valueOf(errorCode)) {
				case AGENT_NOT_FOUND:
				case AUTOASSIGNENODEPATTERN_NOT_FOUND:
				case AUTO_CONTROL_NOT_FOUND_INSTANCE:
				case AUTO_CONTROL_NOT_FOUND_SCRIPT:
				case AUTO_CONTROL_NOT_FOUND_STORAGE:
				case BILLINGALARM_NOT_FOUND:
				case BILLINGALARM_NOT_FOUND_BY_HINEMOSUSER:
				case CLOUDENTITY_NOT_FOUND_PLATFORMENTITY:
				case CLOUDINSTANCEBACKUP_NOT_FOUND:
				case CLOUDINSTANCE_NOT_ACQUIRE_LOCK:
				case CLOUDINSTANCE_NOT_FOUND:
				case CLOUDINSTANCE_NOT_FOUND_BY_FACILITY:
				case CLOUDINSTANCE_NOT_FOUND_PLATFORMINSTANANCE:
				case CLOUDREGION_INVALID_CLOUDREGION_NOT_FOUND:
				case CLOUDSCOPE_INVALID_CLOUDSCOPE_NOT_FOUND:
				case CLOUDSTORAGEBACKUP_NOT_FOUND:
				case CLOUDSTORAGE_NOT_FOUND:
				case CLOUD_PLATFORM_NOT_FOUND:
				case ENDPOINT_INVALID_ENDPOINT_FOUND:
				case FACILITY_NOT_FOUND:
				case LOCATION_NOT_FOUND:
				case LOGINUSER_NOT_FOUND:
				case MONITOR_CLOUDSERVICE_FOUND_NO_SERVICEID:
					return Status.NOT_FOUND;
	
				case AUTO_ASSIGN_NODE_UNMATCHED_CIDR_FORMAT:
				case AUTO_ASSIGN_NODE_UNMATCHED_REGEX:
				case BILLINGALARM_INVALID_OWNERROLEID:
				case LOGINUSER_NUM_NOT_MATCH:
				case LOGINUSER_USER_NOT_INCLUDE:
				case MONITOR_CLOUDSERVICE_ONLY_NODE:
				case VALIDATION_ERROR:
					return Status.BAD_REQUEST;
	
				case CLOUDINSTANCE_ALREADY_EXIST:
				case CLOUDSCOPE_ALREADY_EXIST:
				case CLOUDSTORAGE_ALREADY_EXIST:
				case HINEMOS_SCOPE_DUPLICATED:
				case ALREADY_ASSIGN_NODE:
				case BILLINGALARM_ALREADY_EXIST:
				case LOGINUSER_ALREADY_EXIST:
					return Status.CONFLICT;
	
				case NEED_ADMINISTRATORS_ROLE:
				case NEED_ADMINISTRATORS_ROLE_OR_ACCOUNT_USER:
				case NEED_ADMINISTRATORS_ROLE_OR_ACCOUT_USER_OR_SELF:
				case OBJECTPRIVILEGE_NOT_FOUND_TARGET_OBJECT:
				case OBJECTPRIVILEGE_UNAUTHORIZED_TO_TARGET_OBJECT:
					return Status.FORBIDDEN;
	
				case AUTOUPDATE_NOT_DELETE_FACILITY:
				case CLOUDINSTANCE_FIAL_TO_POWEROFF_INSTANCE_BY_FACILITY:
				case CLOUDINSTANCE_FIAL_TO_POWERON_INSTANCE_BY_FACILITY:
				case CLOUDINSTANCE_FIAL_TO_REBOOT_INSTANCE_BY_FACILITY:
				case CLOUDINSTANCE_FIAL_TO_SUSPEND_INSTANCE_BY_FACILITY:
				case COMMUNITY_EDITION_FUNC_NOT_AVAILABLE:
				case UNEXPECTED:
				case UNSUPPORTED_OPERATION:
				case HINEMOS_MANAGER_ERROR:
					return Status.INTERNAL_SERVER_ERROR;
			}
		} catch (IllegalArgumentException e) {
			return Status.INTERNAL_SERVER_ERROR;
		}
		return Status.INTERNAL_SERVER_ERROR;
	}
	
	// JAX-RS例外をHinemosUnknownに置き換える
	private static Throwable replaceWithHinemosException(Throwable e) {
		if( e instanceof WebApplicationException ){
			e = new HinemosUnknown(e);
		}
		return e;
	}
}
