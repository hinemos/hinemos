/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.util;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.grizzly.http.server.Request;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.rest.RestConstant;
import com.clustercontrol.rest.annotation.RestLog;
import com.clustercontrol.rest.annotation.RestLog.LogTarget;
import com.clustercontrol.rest.annotation.RestLog.LogType;
import com.clustercontrol.rest.annotation.RestLogFunc;
import com.clustercontrol.rest.annotation.RestLogFunc.LogFuncName;
import com.clustercontrol.rest.exception.HinemosRestExceptionMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * HinemosのRestApiへのアクセスを経由したユーザー操作のログ出力を行うクラス<BR>
 *
 */
public class RestLogger {

	private static final String EXCEPTION_NAME_PREFIX = "com.clustercontrol.fault.";
	private static Log log = LogFactory.getLog(RestLogger.class);
	private static Log m_opelog = LogFactory.getLog("HinemosOperation");
	
	// 以下リストに含まれる文字列に部分一致するJSONプロパティ値がマスクされる（大文字小文字を区別する）
	private static final List<String> MASK_CHECK_LIST = Arrays.asList("password", "Password", "secretKey",
			"sshPrivateKeyPassphrase", "AccessKey", "accessKey", "jsonCredentialInfo", "authPass", "privPass",
			"infraManagementParamInfoEntities" // passwordFlgがtrueの時valueのマスクが必要だが、個別対応が難しいので丸ごとマスクする
			);
	// 除外リストに完全一致するJSONプロパティはマスクされない
	private static final List<String> MASK_CHECK_EXCLUSION_LIST = Arrays.asList();
	private static final String MASK_STRING = "*****";

	/**
	 *
	 * RestApiによる操作ログを出力します。<BR>
	 * 
	 * @param execClass
	 *            呼び出し元クラス（アノテーションの取得用）
	 * @param execMethod
	 *            呼び出し元メソッド（アノテーションの取得用）
	 * @param Request
	 *            リクエスト情報
	 * @param RequestBody
	 *            リクエストボディ
	 * @param Response
	 *            レスポンスのステータス（Exception発生時はそのクラス名）
	 * @since
	 */
	static public void putOperationLog(Class<?> execClass, Method execMethod, Request request, UriInfo uri,
			String requestBody, String responseStatus) {
		try {
			String requestPath = "";
			String requestMethod = "";
			String requestUserId = "";
			String requestFromIp = "";
			String requestToken = "";
			// リクエストからURL等を取得
			if (request != null) {
				requestMethod = request.getMethod().getMethodString();
				requestFromIp = request.getRemoteAddr();
				try {
					requestPath = request.getDecodedRequestURI();
				} catch (Exception e) {
					log.error("putOperationLog : Failed to get url info . class=" + execClass.getSimpleName()
							+ ", methodName=" + execMethod.getName(), e);
				}
			}

			// スレッドローカル変数からユーザーとトークンを取得（認証時にfilterにて設定されている前提）
			requestUserId = HinemosSessionContext.getLoginUserId();
			requestToken = HinemosSessionContext.getAuthToken();

			// クエリパラメータ取得
			String queryParams = null;
			if (uri != null) {
				StringBuilder queryParamsString = new StringBuilder();
				MultivaluedMap<String, String> queryParamMap = uri.getQueryParameters();
				queryParamMap.entrySet().forEach(h -> queryParamsString.append(h.getKey() + "=" + h.getValue() + " "));
				queryParams = queryParamsString.toString().trim();
			}
			putOperationLog(execClass, execMethod, requestPath, requestMethod, responseStatus, requestUserId,
					requestToken, requestFromIp, queryParams, requestBody);
		} catch (Exception e) {
			log.error("putOperationLog : Failed to put Operation Log . class=" + execClass.getSimpleName()
					+ ", methodName=" + execMethod.getName(), e);
		}
	}

	/**
	 *
	 * RestApiによる操作ログを出力します。<BR>
	 * 
	 * @param execClass
	 *            呼び出し元クラス（アノテーションの取得用）
	 * @param execMethod
	 *            呼び出し元メソッド（アノテーションの取得用）
	 * @param requestPath
	 *            APIへのURL
	 * @param requestMethod
	 *            APIへのhttpメソッド
	 * @param responseStatus
	 *            APIが返却した状態
	 * @param requestUserName
	 *            APIへのユーザー名（認証されたユーザー名）
	 * @param requestToken
	 *            APIへの識別情報（アクセストークン）
	 * @param requestFromIp
	 *            APIへのアクセス元IPアドレス
	 * @param requestBody
	 *            APIへのリクエストの詳細（httpRequestContents）
	 * @since
	 */
	static public void putOperationLog(Class<?> execClass, Method execMethod, String requestPath, String requestMethod,
			String responseStatus, String requestUserId, String requestToken, String requestFromIp,
			String queryParameter, String requestBody) {

		// 機能名 操作名 操作対象 ログタイプ の取得（呼び出し元のアノテーションから取得）
		String logFuncName = null;
		String logProcessName = null;
		LogType logType = null;
		try {
			RestLog logProcess = (RestLog) execMethod.getAnnotation(RestLog.class);
			if (logProcess.target() != LogTarget.Null) {
				logProcessName = logProcess.action().toString() + " " + logProcess.target().toString();
			} else {
				logProcessName = logProcess.action().toString();
			}

			if (logProcess.funcName() != LogFuncName.Default) {
				logFuncName = "[" +logProcess.funcName().toString() + "]";
			} else {
				RestLogFunc classLogFunc = (RestLogFunc) execClass.getAnnotation(RestLogFunc.class);
				logFuncName = "[" + classLogFunc.name().toString() + "]";
			}

			logType = logProcess.type();
		} catch (Exception e) {
			log.error("putOperationLog : Failed to get the name from the class . class=" + execClass.getSimpleName()
					+ ", methodName=" + execMethod.getName() + " ,message=" + e.getMessage(), e);
		}

		// ステータスが200(OK)でなければ 操作概要に failed を付与
		if (!(responseStatus.startsWith(RestConstant.STATUS_CODE_200))) {
			logProcessName = logProcessName + " failed";
		}

		// ステータスが独自定義Exceptionクラス名(リソースメソッド内にて異常発生時)なら 対応するステータスコードを取得
		String editStatus = responseStatus;
		if (responseStatus.startsWith(EXCEPTION_NAME_PREFIX)) {
			Integer code = null;
			try {
				Class<?> myClass = Class.forName(responseStatus);
				Throwable myClassInstance = (Throwable) myClass.newInstance();
				Status exceptionStatus = HinemosRestExceptionMapper.getResponseStatus(myClassInstance);
				code = exceptionStatus.getStatusCode();
			} catch (Exception e) {
				log.warn("putOperationLog : Failed to get responce code from exception . class="
						+ execClass.getSimpleName() + ", methodName=" + execMethod.getName() + " ,message="
						+ e.getMessage(), e);
			}
			if (code == null) {
				code = 500;
			}
			editStatus = code + "(" + responseStatus.replace(EXCEPTION_NAME_PREFIX, "") + ")";
		}

		// ログの出力内容を確定
		String logPutLine = logFuncName + " " + logProcessName + ", Method= " + execMethod.getName() + ", RequestPath="
				+ requestPath + ", RequestMethod=" + requestMethod + ", ResponseStatus=" + editStatus + ", User="
				+ requestUserId + ", Token=" + tokenHashing(requestToken) + ", FromIp=" + requestFromIp + ", QueryParameter={"
				+ queryParameter + "}, RequestBody=" + maskRequestBody(requestBody);

		// 指定サイズを超える場合カットする
		logPutLine = cutOperationLog(logPutLine);
		
		// ログのTypeと処理の状態に応じたログレベルで出力
		if (logType == null || logType.equals(LogType.REFERENCE)) {
			m_opelog.debug(logPutLine);
		} else {
			if (responseStatus.startsWith(RestConstant.STATUS_CODE_200)) {
				m_opelog.info(logPutLine);
			} else {
				m_opelog.warn(logPutLine);
			}
		}
	}

	private static String maskRequestBody(String requestBody) {
		String maskedRequestBody = null;
		
		if (requestBody != null && needsMask(requestBody)) {
			log.debug("requestBody=" + requestBody);
			try {
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> map = mapper.readValue(requestBody, new TypeReference<Map<String, Object>>() {
				});
				
				mask(map);

				maskedRequestBody = mapper.writeValueAsString(map);
			} catch (Exception e) {
				// エラー時はリクエストボディは出力しない
				log.debug("maskRequestBody failed. " + e.getMessage());
			}
		} else {
			maskedRequestBody = requestBody;
		}

		return maskedRequestBody;
	}

	private static boolean needsMask(String input) {
		for (String s : MASK_CHECK_LIST) {
			if (input.contains(s)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private static void mask(Map<String, Object> map) {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			// ネストされた要素は再帰的に処理
			if(entry.getValue() instanceof Map) {
				mask((Map<String, Object>)entry.getValue());
			} else if (entry.getValue() instanceof List) {
				// 要素がListだった場合は順に再帰的に処理
				for (Object obj : (List<Object>) entry.getValue()) {
					if (obj instanceof Map) {
						mask((Map<String, Object>) obj);
					}
				}
			}
			
			if(MASK_CHECK_EXCLUSION_LIST.contains(entry.getKey())) {
				continue;
			}
			if (needsMask(entry.getKey())) {
				entry.setValue(MASK_STRING);
			}
		}
	}
	
	private static String tokenHashing(String token) {
		String tokenHashing = null;
		
		if(token != null && !token.isEmpty()) {
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				tokenHashing = Hex.encodeHexString(md.digest(token.getBytes()));
			} catch (Exception e) {
				// エラー時はなにもしない
				log.debug("tokenHashing failed. " + e.getMessage());
			}
		}
		
		return tokenHashing;
	}
	
	private static String cutOperationLog(String org) {
		int limit = HinemosPropertyCommon.rest_operation_log_length.getIntegerValue();
		if(org.length() > limit) {
			log.debug("Original Operation Log Length is " + org.length());
			return org.substring(0, limit);
		}
		return org;
	}
}
