/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.glassfish.jersey.message.internal.OutboundJaxrsResponse;

import com.clustercontrol.rest.annotation.RestLogPut;

import org.glassfish.grizzly.http.server.Request;

import java.lang.reflect.Method;

import javax.ws.rs.core.UriInfo;

/**
 *
 * HinemosのRestApiの各種処理に対し、アスペクト（横断的な処理）を定義するクラス<BR>
 * META-INF\aop.xml とセットになっているので注意すること。<BR>
 *
 */
@Aspect
public class RestAspectDefine {

	private static Log log = LogFactory.getLog(RestAspectDefine.class);

	/**
	 *
	 * リソースメソッドの実行時にログを出力する<BR>
	 *
	 * クラス名（Endpointsクラスのみ）とアノテーション（RestLogが設定されている）を条件として
	 * RESTAPI向けリソースメソッド実行時にのみ 本処理が適用されるように限定している。
	 */
	@Around("execution(* com.clustercontrol.rest.endpoint..*Endpoints.*(..)) && @annotation(com.clustercontrol.rest.annotation.RestLog)")
	public Object putResourceMethodExecuteLog(ProceedingJoinPoint point) throws Throwable {
		long methodStartTime = System.currentTimeMillis();

		// リソースメソッド（呼び出し元）情報取得
		MethodSignature methodSignature = (MethodSignature) point.getSignature();
		String className = methodSignature.getDeclaringType().getName();
		String methodName = methodSignature.getName();
		Method execMethod = methodSignature.getMethod();

		// 操作ログ自動出力フラグを取得
		RestLogPut logPut = (RestLogPut) execMethod.getAnnotation(RestLogPut.class);
		boolean isLogPut = true;
		if (logPut != null && logPut.auto() == false) {
			isLogPut = false;
			if (log.isDebugEnabled()) {
				log.debug("@Around putResourceMethodExecuteLog : " + className + "#" + methodName + " isLogPut="
						+ isLogPut);
			}
		}

		// リソースメソッドに対するパラメータの情報を必要なら取得(Request UriInfo RequestBody)
		String[] params = methodSignature.getParameterNames();
		Object[] args = point.getArgs();
		String urlString = "";
		String methodString = "";
		Request request = null;
		UriInfo uri = null;
		String reqBody = null;
		if (isLogPut) {
			try {
				for (int argIndex = 0; argIndex < args.length; argIndex++) {
					if (args[argIndex] instanceof Request) {
						request = (Request) args[argIndex];
						methodString = request.getMethod().getMethodString();
						urlString = request.getDecodedRequestURI();
					}
					if (args[argIndex] instanceof UriInfo) {
						uri = (UriInfo) args[argIndex];
					}
					if (args[argIndex] instanceof String) {
						if (reqBody == null && params[argIndex].equals("requestBody")) {
							reqBody = (String) args[argIndex];
						}
					}
				}
			} catch (Exception e) {
				log.error(
						"@Around putResourceMethodExecuteLog : Exception occurred while getting Method argument information : Method="
								+ className + "#" + methodName + ", Excepion=" + e.getMessage(),
						e);
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("@Around putResourceMethodExecuteLog :" + className + "#" + methodName + " start!");
		}

		// 織り込み先(リソースメソッドの本体処理)のメソッドを呼ぶ
		Object result = null;
		try {
			result = point.proceed();
		} catch (Throwable e) {
			if (log.isDebugEnabled()) {
				log.debug("@Around putResourceMethodExecuteLog : Exception occurred while Method process : Method="
						+ className + "#" + methodName + ", Excepion=" + e.getMessage(), e);
			}
			// リソースメソッド実行中に例外が発生した場合でも操作ログは出力しておく（例外はそのまま送る）
			if (isLogPut) {
				RestLogger.putOperationLog(methodSignature.getDeclaringType(), execMethod, request, uri, reqBody,
						e.getClass().getName());
			}
			throw e;
		}

		long execTime = System.currentTimeMillis() - methodStartTime;
		if (log.isDebugEnabled()) {
			log.debug("@Around putResourceMethodExecuteLog : " + className + "#" + methodName + " execTime= " + execTime
					+ " mllis");
		}

		// リソースメソッドの処理結果に応じた操作ログを出力
		if (isLogPut) {
			if (result != null && result instanceof OutboundJaxrsResponse) {
				if (log.isDebugEnabled()) {
					log.debug("@Around putResourceMethodExecuteLog : " + className + "#" + methodName + " result type="
							+ result.getClass().getSimpleName() + " ,exec time=" + execTime + " ,http url=" + urlString
							+ " ,http method=" + methodString + " ,http status="
							+ ((OutboundJaxrsResponse) result).getStatus());
				}
				RestLogger.putOperationLog(methodSignature.getDeclaringType(), execMethod, request, uri, reqBody,
						Integer.toString(((OutboundJaxrsResponse) result).getStatus()));
			} else {
				// ここには来ない想定。もし来ていたらリソースメソッド以外のメソッドが処理の対象になってしまっている
				log.error("@Around putResourceMethodExecuteLog : " + className + "#" + methodName
						+ " . result is Unexpected ");
			}
		}

		// 織り込み先(ジョインポイント)のメソッドの戻り値を返却
		return result;
	}

}
