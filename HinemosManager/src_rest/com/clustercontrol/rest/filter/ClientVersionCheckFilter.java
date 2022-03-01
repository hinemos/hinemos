/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.filter;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.util.MessageConstant;

/**
 * Hinemosクライアントのバージョンチェックを行う HTTPヘッダで X-HinemosClientVersion が指定された場合、当該ヘッダの値が
 * メジャーバージョンと一致するか確認する。一致しない場合、エラーとする。 (メジャーバージョン違いの Hinemosクライアントからの接続を防止するため)
 * 
 * HTTPヘッダで X-HinemosClientVersion が未指定の場合、チェックはしない。
 * 
 * X-HinemosClientVersion は ClientSettingAcquisitionFilter で
 * HinemosSessionContext に格納されるため、そちらの値を使う。
 * 
 */
@Priority(FilterPriorities.INDIVIDUAL)
public class ClientVersionCheckFilter implements ContainerRequestFilter {

	private static final Log log = LogFactory.getLog(ClientVersionCheckFilter.class);

	private static final String VERSION = "7.0";

	@Context
	private ResourceInfo resourceInfo;

	@Override
	public void filter(ContainerRequestContext reqContext) {
		
		String clientVersion = (String) HinemosSessionContext.instance()
				.getProperty(HinemosSessionContext.REST_HINEMOS_CLIENT_VERSION);
		if (log.isDebugEnabled()) {
			log.debug("filter() : clientVersion =" + clientVersion);
		}
		
		// バージョン情報がない場合何もしない
		if (clientVersion == null) {
			return;
		}

		if (!VERSION.equals(clientVersion)) {
			String message = MessageConstant.MESSAGE_HINEMOS_CLIENT_MAJOR_VERSION_MISMATCH.getMessage(VERSION,
					clientVersion);
			Response response = Response.status(Response.Status.BAD_REQUEST)
					.entity(new ExceptionBody(Status.BAD_REQUEST.getStatusCode(), new InvalidSetting(message)))
					.build();
			reqContext.abortWith(response);
		}
	}

}
