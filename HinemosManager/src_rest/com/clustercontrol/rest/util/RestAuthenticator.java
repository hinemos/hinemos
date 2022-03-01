/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.commons.util.HttpBasicAuthenticator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.rest.annotation.RestSystemAdminPrivilege;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.util.MessageConstant;

/**
 *
 * HinemosのRestApiリソースメソッド呼び出しに対し、ログイン認証とシステム権限認証を行うクラス<BR>
 *
 */
public class RestAuthenticator {

	private static Log log = LogFactory.getLog(RestAuthenticator.class);

	/**
	 * Basic認証（httpFilterからの呼び出し向け）.<br>
	 * <br>
	 * リクエストヘッダの認証情報を用いて、該当操作の権限を持つユーザーか判定する.<br>
	 * 
	 * @param execMethod
	 *            実行メソッド
	 * @param authorizations
	 *            認証ヘッダ
	 * @return 認証チェックエラー時のResponse、認証チェックOKであればnullを返却
	 */
	public static Response authBasic4HttpFilter(Method execMethod, List<String> authorizations) {
		return authCheck(execMethod, authorizations, true, true, false);
	}

	/**
	 * Basic認証（リソースメソッドからの呼び出し向け）.<br>
	 * <br>
	 * リクエストヘッダの認証情報を用いて、該当操作の権限を持つユーザーか判定する.<br>
	 * 
	 * @param execMethod
	 *            実行メソッド
	 * @param authorization
	 *            認証ヘッダ
	 * @return 認証チェックエラー時のResponse、認証チェックOKであればnullを返却
	 */
	public static Response authBasic4ResourceMethod(Method execMethod, String authorization) {
		ArrayList<String> authorizations = new ArrayList<String>();
		if (authorization != null) {
			authorizations.add(authorization);
		}
		return authCheck(execMethod, authorizations, false, true, false);
	}

	/**
	 * Bearer認証（httpFilterからの呼び出し向け）.<br>
	 * <br>
	 * リクエストヘッダの認証情報を用いて、該当操作の権限を持つユーザーか判定する.<br>
	 * 
	 * @param execMethod
	 *            実行メソッド
	 * @param authorizations
	 *            認証ヘッダ
	 * @return 認証チェックエラー時のResponse、認証チェックOKであればnullを返却
	 */
	public static Response authBearer4HttpFilter(Method execMethod, List<String> authorizations) {
		return authCheck(execMethod, authorizations, true, false, true);
	}

	/**
	 * Bearer認証(リソースメソッドからの呼び出し向け) .<br>
	 * <br>
	 * リクエストヘッダの認証情報を用いて、該当操作の権限を持つユーザーか判定する.<br>
	 * 
	 * @param execMethod
	 *            実行メソッド
	 * @param authorization
	 *            認証ヘッダ
	 * @return 認証チェックエラー時のResponse、認証チェックOKであればnullを返却
	 */
	public static Response authBearer4ResourceMethod(Method execMethod, String authorization) {
		ArrayList<String> authorizations = new ArrayList<String>();
		if (authorization != null) {
			authorizations.add(authorization);
		}
		return authCheck(execMethod, authorizations, false, false, true);
	}

	/**
	 * 認証チェック.<br>
	 * <br>
	 * リクエストヘッダの認証情報と指定された権限情報を用いて、該当操作の権限を持つユーザーか判定する.<br>
	 * 
	 * @param authorizations
	 *            認証ヘッダ
	 * @param isCallHttpFilter
	 *            httpFilterから呼出（アノテーションの取得手順が変わるため指定）
	 * @param useBasicAuth
	 *            Basic認証を行うかどうかのフラグ
	 * @param useBearerAuth
	 *            Bearer認証を行うかどうかのフラグ（Basic認証のほうが優先される）
	 * @return 認証チェックエラー時のResponse、認証チェックOKであればnullを返却
	 */

	private static Response authCheck(Method execMethod, List<String> authorizations, boolean isCallHttpFilter,
			boolean useBasicAuth, boolean useBearerAuth) {

		if (log.isDebugEnabled()) {
			log.debug("authCheck() : method=" + execMethod.getDeclaringClass().getName() + "#" + execMethod.getName());
		}

		if (authorizations == null || authorizations.size() == 0) {
			// 認証情報なし
			String message = MessageConstant.MESSAGE_USER_AUTH_HEADER_IS_NOTHING.getMessage();
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(new ExceptionBody(Status.UNAUTHORIZED.getStatusCode(), new InvalidUserPass(message)))
					.build();
		}

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = getSystemPrivilegeList(execMethod, isCallHttpFilter);

		boolean isNeedAdmin = getNeedAdmin(execMethod, isCallHttpFilter);

		if (log.isDebugEnabled()) {
			log.debug("authCheck() : systemPrivilegeList.size is " + systemPrivilegeList.size());
		}

		// 認証ヘッダからBasic認証とBearer認証の情報を取得
		String basicAuthHeader = null;
		String bearerAuthHeader = null;
		for (String authorizationEntity : authorizations) {
			if (log.isDebugEnabled()) {
				log.debug("authCheck() : line=" + authorizationEntity);
			}
			if (authorizationEntity == null) {
				continue;
			}
			String[] credentialArray = authorizationEntity.split(",");
			for (String credentialString : credentialArray) {
				String trimString = credentialString.trim();
				if (trimString.startsWith("Basic")) {
					basicAuthHeader = trimString;
					if (log.isDebugEnabled()) {
						log.debug("authCheck() : basicAuthHeader=" + basicAuthHeader);
					}
				}
				if (trimString.startsWith("Bearer")) {
					bearerAuthHeader = trimString;
					if (log.isDebugEnabled()) {
						log.debug("authCheck() : bearerAuthHeader=" + bearerAuthHeader);
					}
				}
			}
		}

		// 判定
		if (useBasicAuth && basicAuthHeader != null) {
			// Basic認証が設定されている場合
			try {
				HttpBasicAuthenticator.authCheck(basicAuthHeader, systemPrivilegeList, isNeedAdmin);
			} catch (InvalidUserPass e) {
				// ユーザー名/パスワード不正.
				return Response.status(Response.Status.UNAUTHORIZED)
						.entity(new ExceptionBody(Status.UNAUTHORIZED.getStatusCode(), e)).build();
			} catch (InvalidRole e) {
				// アクセス権限なしのユーザー.
				return Response.status(Response.Status.FORBIDDEN)
						.entity(new ExceptionBody(Status.FORBIDDEN.getStatusCode(), e)).build();
			} catch (HinemosUnknown e) {
				// DB接続不可など.
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(new ExceptionBody(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e)).build();
			}
		} else if (useBearerAuth && bearerAuthHeader != null) {
			// Bearer認証が設定されている場合
			try {
				RestHttpBearerAuthenticator.getInstance().authCheck(bearerAuthHeader, systemPrivilegeList, isNeedAdmin);
			} catch (InvalidUserPass e) {
				// ユーザー名/パスワード不正.
				return Response.status(Response.Status.UNAUTHORIZED)
						.entity(new ExceptionBody(Status.UNAUTHORIZED.getStatusCode(), e)).build();
			} catch (InvalidRole e) {
				// アクセス権限なしのユーザー.
				return Response.status(Response.Status.FORBIDDEN)
						.entity(new ExceptionBody(Status.FORBIDDEN.getStatusCode(), e)).build();
			} catch (HinemosUnknown e) {
				// DB接続不可など.
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(new ExceptionBody(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e)).build();
			}
		} else {
			// 対応している認証情報なし
			String message = MessageConstant.MESSAGE_USER_AUTH_HEADER_IS_NOTHING.getMessage();
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(new ExceptionBody(Status.UNAUTHORIZED.getStatusCode(), new InvalidUserPass(message)))
					.build();
		}

		return null;
	}

	/**
	 * システム権限情報リストの取得<br>
	 * <br>
	 * 実行メソッドが保持するシステム権限のリストを取得する。<br>
	 * 
	 * @param execMethod
	 *            実行メソッド
	 * @param isCallHttpFilter
	 *            httpFilterから呼出（アノテーションの取得手順が変わるため指定）
	 * @return システム権限情報のリスト
	 */
	private static ArrayList<SystemPrivilegeInfo> getSystemPrivilegeList(Method execMethod, boolean isCallHttpFilter) {

		// 必要なシステム権限の取得（呼び出し元のアノテーションから取得）
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		RestSystemPrivilege[] RepeatSettingList = null;
		if (isCallHttpFilter) {
			// HttpFilter経由でMethodを取得している場合を想定し getDeclaredAnnotation にて取得
			RepeatSettingList = execMethod.getDeclaredAnnotationsByType(RestSystemPrivilege.class);
		} else {
			RepeatSettingList = execMethod.getAnnotationsByType(RestSystemPrivilege.class);
		}
		if (RepeatSettingList == null) {
			return systemPrivilegeList;
		}
		try {
			for (RestSystemPrivilege settingTarget : RepeatSettingList) {
				String authFuncName = settingTarget.function().toString();
				SystemPrivilegeMode[] authModeArray = settingTarget.modeList();
				for (SystemPrivilegeMode modeTarget : authModeArray) {
					systemPrivilegeList.add(new SystemPrivilegeInfo(authFuncName, modeTarget));
					if (log.isDebugEnabled()) {
						log.debug("getSystemPrivilegeList() : adding SystemPrivilegeInfo is " + authFuncName + "#"
								+ modeTarget);
					}
				}
			}
		} catch (Exception e) {
			log.error("getSystemPrivilegeList() : Failed to get @RestSystemPrivilege from  Method . method="
					+ execMethod.getDeclaringClass() + "#" + execMethod.getName() + ", message=" + e.getMessage(), e);
		}
		return systemPrivilegeList;
	}

	/**
	 * admin権限要否の取得<br>
	 * <br>
	 * アノテーションによって指定された実行メソッドのadmin権限要否を取得する。<br>
	 * 
	 * @param execMethod
	 *            実行メソッド
	 * @param isCallHttpFilter
	 *            httpFilterから呼出（アノテーションの取得手順が変わるため指定）
	 * @return admin権限の要否
	 */
	private static boolean getNeedAdmin(Method execMethod, boolean isCallHttpFilter) {

		boolean isNeedAdmin = false;
		try {
			RestSystemAdminPrivilege target = null;
			if (isCallHttpFilter) {
				// HttpFilter経由でMethodを取得している場合を想定し getDeclaredAnnotation にて取得
				target = (RestSystemAdminPrivilege) execMethod.getDeclaredAnnotation(RestSystemAdminPrivilege.class);
			} else {
				target = (RestSystemAdminPrivilege) execMethod.getAnnotation(RestSystemAdminPrivilege.class);
			}
			if (target == null) {
				return false;
			}
			isNeedAdmin = target.isNeed();
			if (log.isDebugEnabled()) {
				log.debug("getNeedAdmin() : isAdminNeed = " + isNeedAdmin);
			}
		} catch (Exception e) {
			log.error("getNeedAdmin() : Failed to get @RestSystemAdminPrivilege from  Method . method="
					+ execMethod.getDeclaringClass().getName() + "#" + execMethod.getName() + ", message="
					+ e.getMessage(), e);
		}
		return isNeedAdmin;
	}

}
