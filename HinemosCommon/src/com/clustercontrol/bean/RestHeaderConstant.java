/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.bean;

/**
 * REST向けhttpヘッダーに関連する定数クラス<BR>
 * 
 */
public class RestHeaderConstant {
	public static final String CLIENT_DT_FORMAT = "X-HinemosClientDatetimeFormat";
	public static final String CLIENT_LANG_SET = "Accept-Language";
	public static final String CLIENT_VERSION = "X-HinemosClientVersion";
	public static final String HINEMOS_TOKEN = "X-HinemosToken";
	public static final String AUTHORIZATION = "authorization";
	public static final String AUTH_BEARER = "Bearer";
	public static final String AUTH_BASIC = "Basic";
	public static final String AGENT_REQUEST_ID = "X-HinemosAgentRequestId";
	public static final String AGENT_IDENTIFIER = "X-HinemosAgentIdentifier";
	public static final String AGENT_VERSION = "X-HinemosAgentVersion";
	
}
