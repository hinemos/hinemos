/*
 * Copyright (c) 2025 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.util;

import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.GetScriptResponse;
import org.openapitools.client.model.SetJobStartRequest;
import org.openapitools.client.model.SetJobStartResponse;

import com.clustercontrol.agent.AgentRestClientWrapper;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.SessionIdLocked;
import com.clustercontrol.rest.Pair;

public class AgentRestClientEx {
	private static final Log logger = LogFactory.getLog(AgentRestClientEx.class);

	// コマンドジョブの実行開始リクエストやスクリプト取得時のリトライインターバル
	public final static String PROPNAME_RESTAPI_RETRY_INTERVAL = "job.reconnection.interval";

	// リトライインターバルのデフォルト値を設定
	private static long retryInterval = 10000L;

	static {
		String retryInterval_string = AgentProperties.getProperty(PROPNAME_RESTAPI_RETRY_INTERVAL);
		try {
			long interval = Long.parseLong(retryInterval_string);

			if (interval > 0) {
				retryInterval = interval;
			} else {
				// 負の値が設定されていた場合ログを出力し、デフォルト値を利用する
				logger.warn("Set " + PROPNAME_RESTAPI_RETRY_INTERVAL + " to a positive number. "
						+ PROPNAME_RESTAPI_RETRY_INTERVAL + " = " + retryInterval_string);
			}
		} catch (NumberFormatException e) {
			// 数値以外を設定されていた場合、ログを出力し、デフォルト値を利用する
			logger.warn("Set " + PROPNAME_RESTAPI_RETRY_INTERVAL + " to a positive number. "
					+ PROPNAME_RESTAPI_RETRY_INTERVAL + " = " + retryInterval_string);
		}
		logger.info(PROPNAME_RESTAPI_RETRY_INTERVAL + " = " + retryInterval);
	}
	
	/**
	 * REST APIでリトライ不可な例外を通知するためにスローされる
	 */
	public static class RestApiCallException extends Exception {
		private static final long serialVersionUID = 4085088731926701167L;

		public RestApiCallException(String message, Exception cause) {
			super(message, cause);
		}
	}

	/**
	 * リトライの制限時間を超過した場合にスローされる
	 */
	public static class RetryTimeoutException extends Exception {
		private static final long serialVersionUID = 8930268004417516123L;

		public RetryTimeoutException() {
		}

		public RetryTimeoutException(String message) {
			super(message);
		}

		public RetryTimeoutException(String message, Exception cause) {
			super(message, cause);
		}
	}
	
	/**
	 * REST APIの再実行を指示する場合にスローする
	 */
	private static class NeedRetryException extends Exception {
		private static final long serialVersionUID = 2993057358572960953L;

		public NeedRetryException(String message, Exception cause) {
			super(message, cause);
		}
	}

	/**
	 * executeWithRetry内で実行させるRestApi処理のインターフェース
	 * 
	 */
	private interface RestApiExecutor<T> {
		/**
		 * executeWithRetry 内で実行される。
		 * 再実行必要な場合は、NeedRestApiRetryExceptionをスローする
		 * 
		 * @return
		 * @throws Exception
		 */
		T execute() throws NeedRetryException, Exception;
	}
	
	/**
	 * AgentRestClientWrapper.setJobStartを内部で呼び出し、必要に応じて再実行を行う
	 * 
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @param facilityId
	 * @param setJobStartRequest
	 * @param agentRequestId
	 * @param timeout タイムアウト時間
	 * @return AgentRestClientWrapper.setJobStartの返り値
	 * @throws RetryTimeoutException リトライ制限時間超過
	 * @throws RestApiCallException RestApiから再実施しない対象の例外が出力された場合
	 */
	public static boolean setJobStart(
			final String sessionId,
			final String jobunitId,
			final String jobId,
			final String facilityId,
			final SetJobStartRequest setJobStartRequest,
			final String agentRequestId,
			long timeout) throws RestApiCallException, RetryTimeoutException {
		return executeWithRetry(new RestApiExecutor<Boolean>() {
				@Override
				public Boolean execute() throws NeedRetryException, Exception {
					try {
						SetJobStartResponse res = AgentRestClientWrapper.setJobStart(
								sessionId,
								jobunitId,
								jobId, 
								facilityId,
								setJobStartRequest,
								agentRequestId);
						
						if (res == null || !res.getJobRunnable()) {
							logger.warn(String.format("The start request was rejected by the manager. sessionID=%s, jobunitID=%s, jobID=%s", sessionId, jobunitId, jobId));
							return false;
						} else {
							return true;
						}
					} catch(SessionIdLocked e) {
						throw new NeedRetryException(
							String.format("Rejected because sessionID is locked. sessionID=%s, jobunitID=%s, jobID=%s", sessionId, jobunitId, jobId), e);
					}
				}
			}, "setJobStart", argsOf(
								"sessionId", sessionId, "jobunitId", jobunitId, "jobId", jobId, "facilityId", facilityId,
								"setJobStartRequest", setJobStartRequest.toString(), "agentRequestId", agentRequestId),
							timeout);
		}
	
	/**
	 * AgentRestClientWrapper.getScriptを内部で呼び出し、必要に応じて再実行を行う
	 * 
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @param timeout タイムアウト時間
	 * @return AgentRestClientWrapper.getScriptの返り値
	 * @throws RetryTimeoutException リトライ制限時間超過
	 * @throws RestApiCallException RestApiから再実施しない対象の例外が出力された場合
	 */
	public static GetScriptResponse getScript(
			final String sessionId,
			final String jobunitId,
			final String jobId,
			long timeout) throws RestApiCallException, RetryTimeoutException {
		return executeWithRetry(new RestApiExecutor<GetScriptResponse>() {
				@Override
				public GetScriptResponse execute() throws NeedRetryException, Exception {
					return AgentRestClientWrapper.getScript(sessionId, jobunitId, jobId);
				}
			}, "getScript", argsOf("sessionId", sessionId, "jobunitId", jobunitId, "jobId", jobId), timeout);
	}
	
	/**
	 * RestApiをラッパー経由で呼び出す。その際、接続エラーや再実行の指示が確認された場合、再実行も行う
	 * 
	 * @param executor 実行するRestApi処理
	 * @param apiName よびだす対象のAPI名
	 * @param timeout リトライ実施のタイムアウト時間
	 * @return API実行結果
	 * @throws RetryTimeoutException リトライ制限時間超過
	 * @throws RestApiCallException RestApiから再実施しない対象の例外が出力された場合
	 */
	private static <T> T executeWithRetry(RestApiExecutor<T> executor, String apiName, Pair[] args, long timeout)throws RetryTimeoutException, RestApiCallException {
		long startTime = System.currentTimeMillis();
		long count = 0;
		
		while (true) {
			try {
				return executor.execute();
			} catch(NeedRetryException e) { // リトライが必要な場合
				logger.warn(String.format("%s(%s) needs a retry. detail = %s", apiName, argsString(args), e.getMessage()));
				if (logger.isDebugEnabled()) {
					logger.debug(e.getMessage(), e);
				}
			} catch(RestConnectFailed | RejectedExecutionException e) { // 接続失敗時
				logger.warn(String.format("Connection Failed in %s(%s). error = %s", apiName, argsString(args), e.getMessage()));
				if (logger.isDebugEnabled()) {
					logger.debug(e.getMessage(), e);
				}
			} catch(Exception e) { // その他の例外発生時
				String message = String.format("Faled to call %s(%s). error = %s", apiName, argsString(args), e.getMessage());
				throw new RestApiCallException(message, e);
			}

			// タイムアウトチェック
			long current = System.currentTimeMillis();
			if (current - startTime > timeout) {
				String message = String.format("Aborted the retry of %s(%s) because it exceeded the time limit."
						+ " count = %d, startTime = %d, currentTime = %d, timeout = %d)",
						apiName, argsString(args), count, startTime, current, timeout);
				throw new RetryTimeoutException(message);
			} else {
				++count;

				logger.warn(String.format("%s will be executed in %d [ms]. count = %d", apiName, retryInterval, count));

				try {
					Thread.sleep(retryInterval);
				} catch(InterruptedException e) {
					String message = String.format("An unexpected iterruption occurred during the retry of %s. error = %s", apiName, e.getMessage());
					throw new RestApiCallException(message, e);
				}
			}
		}
	}
	
	/**
	 * API呼び出し時に指定する引数のkeyとvalueを指定する
	 * 
	 * @param argName 最初のkey
	 * @param arg 最初のvalue
	 * @param others 2つめ以降のkeyとvalueのペアで偶数でない場合keyとvalueが適切に指定されていないと判断する
	 * @return 引数のペアの配列
	 */
	private static Pair[] argsOf(String argName, String arg, String... others) {
		if (others.length % 2 != 0) {
			throw new IllegalArgumentException("argName and arg must be in paires.");
		}
		Pair[] args = new Pair[1 + others.length / 2];
		args[0] = new Pair(argName, arg);
		for (int i = 0; i < others.length; i += 2) {
			args[1 + i / 2] = new Pair(others[i], others[i + 1]);
		}
		return args;
	}

	/**
	 * 引数のPair配列をkey = value形式で表示する
	 * 
	 * @param args 引数のペア
	 * @return 連結された文字列
	 */
	private static String argsString(Pair[] args) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < args.length; ++i) {
			Pair a = args[i];
			builder.append(a.getName()).append(" = ").append(a.getValue());
			if (i < args.length - 1) {
				builder.append(", ");
			}
		}
		return builder.toString();
	}
}
