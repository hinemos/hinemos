/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.sun.net.httpserver.HttpExchange;

// openJDK
// import java.util.prefs.Base64;

public class HttpAuthenticator{
	private static Log m_log = LogFactory.getLog( HttpAuthenticator.class );

	public static void authCheck(WebServiceContext wsContext, ArrayList<SystemPrivilegeInfo> systemPrivilegeList, boolean isAdmin)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {

		String username = null; // "HINEMOS_AGENT";
		String password = null; // "HINEMOS_AGENT";
		String account = null; // username + ":" + password

		account = getAccount(wsContext);
		int firstColon = account.indexOf(":");
		username = account.substring(0, firstColon);
		password = account.substring(firstColon+1);
		m_log.trace("username=" + username + ", password=" + password);

		// 認証に失敗したら、Exceptionが発生する。
		try{
			// ログインユーザ名
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, username);

			// ADMINISTRATORS所属有無チェック
			boolean isAdministrator = new AccessControllerBean().isAdministrator();
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, isAdministrator);

			// パスワード、システム権限チェック
			new AccessControllerBean().getUserInfoByPassword(username, password, systemPrivilegeList);

			// isAdmin=trueの場合、ADMINISTRATORSロールに所属していないとエラーとする。
			if (isAdmin && !isAdministrator) {
				String message = "need-role ADMINISTRATORS";
				m_log.info(message);
				throw new InvalidRole(message);
			}

		} catch (InvalidUserPass e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		}
	}

	public static void authCheck(WebServiceContext wsContext, ArrayList<SystemPrivilegeInfo> systemPrivilegeList)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {
		authCheck(wsContext, systemPrivilegeList, false);
	}

	/**
	 * username + ":" + password
	 * という形でアカウントを取得する。
	 * @param wsContext
	 * @return
	 */
	public static String getAccount(WebServiceContext wsContext) throws InvalidUserPass {
		MessageContext messageContext = wsContext.getMessageContext();
		@SuppressWarnings("unchecked")
		Map<String, ?> map = (Map<String, ?>)messageContext.get(MessageContext.HTTP_REQUEST_HEADERS);

		@SuppressWarnings("unchecked")
		LinkedList<String> auth = (LinkedList<String>)map.get("Authorization");
		if (auth == null || auth.size() == 0) {
			String message = "Authorization does not exist";
			m_log.info(message);
			throw new InvalidUserPass(message);
		}

		/*
		 * account = "Basic SElORU1PU19BR0VOVDpISU5FTU9TX0FHRU5U"
		 * という形式。
		 */
		String accountBase64 = (auth.get(0));
		m_log.trace("accountBase64 : " + accountBase64);
		if (!"Basic".equalsIgnoreCase(accountBase64.split(" ")[0])) {
			m_log.info("Basic auth does not exist : " + accountBase64.split(" ")[0]);
		}
		accountBase64 = accountBase64.split(" ")[1];
		return new String(Base64.decodeBase64(accountBase64));
	}

	/**
	 * 操作ログ用にuser@[ipaddress]の形式でアクセス元情報を取得する
	 * 
	 * @param wsContext
	 * @return
	 * @throws InvalidUserPass
	 */
	public static String getUserAccountString(WebServiceContext wsContext) throws InvalidUserPass {
		m_log.debug("getUserAccountString()");

		String username = "";
		String ipAddress = "";

		// UserName
		username = getAccount(wsContext).split(":")[0];

		// Source IPAddress(HTTP Only)
		MessageContext messageContext = wsContext.getMessageContext();
		HttpExchange exchange = (HttpExchange)messageContext.get("com.sun.xml.internal.ws.http.exchange");
		if(exchange != null && exchange.getRemoteAddress() != null && exchange.getRemoteAddress().getAddress() != null){
			ipAddress = exchange.getRemoteAddress().getAddress().toString();
		}

		return username + "@[" + ipAddress.replaceAll("/", "") + "]";
	}
}
