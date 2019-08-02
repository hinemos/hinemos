/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.auth.AuthenticationParams;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;

/**
 * HttpBasic認証に関する処理クラス.<br>
 * <br>
 * Rest-API向け(ws向けのHttpBasic認証はHttpAuthenticatorを参照)<br>
 * 
 * @since 6.2.0
 * @version 6.2.0
 * @see com.clustercontrol.ws.util.HttpAuthenticator
 */
public class HttpBasicAuthenticator {

	// ログ出力用.
	/** ロガー */
	private static Log m_log = LogFactory.getLog(HttpBasicAuthenticator.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	/**
	 * 認証権限チェック.
	 * 
	 * @param authzHeader
	 *            HTTPヘッダの認証情報
	 * @param systemPrivilegeList
	 *            必要なオブジェクト権限レベルのリスト
	 * @param isAdmin
	 *            管理者権限が必要か true:管理者権限を持ってないとエラー、false:管理者権限関係なし.
	 */
	public static void authCheck(String authzHeader, ArrayList<SystemPrivilegeInfo> systemPrivilegeList,
			boolean isAdmin) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		String username = null; // "HINEMOS_AGENT";
		String password = null; // "HINEMOS_AGENT";
		String account = null; // username + ":" + password

		if (authzHeader == null || authzHeader.isEmpty()) {
			String message = "need authentication information";
			m_log.info(methodName + DELIMITER + message);
			throw new InvalidUserPass(message);
		}

		account = getAccount(authzHeader);
		int firstColon = account.indexOf(":");
		username = account.substring(0, firstColon);
		password = account.substring(firstColon + 1);
		m_log.trace("username=" + username + ", password=" + password);

		// 認証に失敗したら、Exceptionが発生する。
		try {
			// ログインユーザ名
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, username);

			// ADMINISTRATORS所属有無チェック
			boolean isAdministrator = new AccessControllerBean().isAdministrator();
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, isAdministrator);

			// パスワード、システム権限チェック
			AuthenticationParams authParams = new AuthenticationParams();
			authParams.setUserId(username);
			authParams.setPassword(password);
			authParams.setRequiredSystemPrivileges(systemPrivilegeList);
			authParams.setCacheDisabled(false);
			new AccessControllerBean().authenticate(authParams);

			// isAdmin=trueの場合、ADMINISTRATORSロールに所属していないとエラーとする。
			if (isAdmin && !isAdministrator) {
				String message = "need-role ADMINISTRATORS";
				m_log.info(methodName + DELIMITER + message);
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

	/**
	 * 認証権限チェック、管理者権限は不要な前提.
	 * 
	 * @param authzHeader
	 *            HTTPヘッダの認証情報
	 * @param systemPrivilegeList
	 *            必要なオブジェクト権限レベルのリスト.
	 */
	public static void authCheck(String authzHeader, ArrayList<SystemPrivilegeInfo> systemPrivilegeList)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {
		authCheck(authzHeader, systemPrivilegeList, false);
	}

	/**
	 * username + ":" + password という形でアカウントを取得する。
	 * 
	 * @param accountBase64
	 *            account = "Basic SElORU1PU19BR0VOVDpISU5FTU9TX0FHRU5U" という形式
	 * @return
	 */
	public static String getAccount(String accountBase64) throws InvalidUserPass {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		m_log.trace("accountBase64 : " + accountBase64);
		if (!"Basic".equalsIgnoreCase(accountBase64.split(" ")[0])) {
			m_log.info(methodName + DELIMITER + "Basic auth does not exist : " + accountBase64.split(" ")[0]);
		}
		accountBase64 = accountBase64.split(" ")[1];
		return new String(Base64.decodeBase64(accountBase64));
	}

	/**
	 * 操作ログ用にuser@[ipaddress]の形式でアクセス元情報を取得する
	 * 
	 * @param authzHeader
	 *            HTTPヘッダの認証情報
	 * @param ipAddress
	 *            ()
	 * @return
	 * @throws InvalidUserPass
	 */
	public static String getUserAccountString(String authzHeader, String ipAddress) throws InvalidUserPass {
		m_log.debug("getUserAccountString()");

		String username = "";

		// UserName
		username = getAccount(authzHeader).split(":")[0];

		return username + "@[" + ipAddress.replaceAll("/", "") + "]";
	}
}
