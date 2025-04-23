/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.util;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.JobOperationInfo;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.bean.RunInfo;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.factory.JobSessionNodeImpl;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.rpa.bean.LoginParameter;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.platform.rpa.LoginExecutor;
import com.clustercontrol.rpa.util.LoginResultEnum;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * RPAシナリオジョブ（直接実行）でマネージャからログインを行うクラス
 */
public class RpaJobLoginWorker {
	/** ロガー */
	private static Log m_log = LogFactory.getLog(RpaJobLoginWorker.class);
	/** スレッドプール */
	private static MonitoredThreadPoolExecutor service;
	/** ワーカースレッド名 */
	private static String workerName = "RpaJobLoginWorker";
	/** スレッドプールサイズ */
	private static int maxThreadPoolSize = HinemosPropertyCommon.job_rpa_login_thread_pool_size.getIntegerValue();
	/** 処理を中断する際に使用するFutureオブジェクトのキャッシュ */
	private static Map<String, Future<Boolean>> futureCache = new ConcurrentHashMap<>();

	static {
		service = new MonitoredThreadPoolExecutor(maxThreadPoolSize, maxThreadPoolSize, 0L, TimeUnit.MICROSECONDS,
				new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
					private volatile int _count = 0;

					@Override
					public Thread newThread(Runnable r) {
						Thread thread = new Thread(r, workerName + "-" + _count++);
						thread.setUncaughtExceptionHandler(
								(t, e) -> m_log.error("uncaughtException() : " + e.getMessage(), e));
						return thread;
					}
				}, new ThreadPoolExecutor.AbortPolicy());
	}

	/**
	 * ログイン処理を開始します。<br>
	 * 
	 * @param runInstructionInfo
	 *            ジョブ実行指示情報
	 * @param parameter
	 *            ログインに必要なパラメータ
	 * @param retry
	 *            ログインのリトライン回数
	 * @throws RpaJobTooManyLogin
	 */
	public static void run(RunInstructionInfo runInstructionInfo, LoginParameter parameter, int retry) {
		m_log.info("run() : execute login task");
		try {
			// スレッドプールのサイズ以上にログインを行うと待たされるため、メッセージを出力する。
			if (service.getQueueRunningThreadMap().size() >= maxThreadPoolSize) {
				m_log.warn("run() : too many concurrent login, current=" + service.getQueueRunningThreadMap().size()
						+ ", max=" + maxThreadPoolSize);
				// マネージャからの同時ログイン数が上限を超えている旨のメッセージを出力
				JobSessionNodeEntity sessionNode = QueryUtil.getJobSessionNodePK(runInstructionInfo.getSessionId(),
						runInstructionInfo.getJobunitId(), runInstructionInfo.getJobId(),
						runInstructionInfo.getFacilityId());
				new JobSessionNodeImpl().setMessage(sessionNode,
						MessageConstant.MESSAGE_JOB_RPA_TOO_MANY_LOGIN.getMessage(String
								.valueOf(maxThreadPoolSize)));
			}
			Future<Boolean> future = service.submit(new JobLoginTask(runInstructionInfo, parameter, retry));
			// 処理をキャンセルできるようMapに保持しておく
			futureCache.put(getKey(runInstructionInfo), future);
		} catch (Throwable e) {
			m_log.warn("run() Error : " + e.getMessage());
		}
	}

	/**
	 * RPAシナリオジョブでログインを実行するクラス
	 */
	private static class JobLoginTask implements Callable<Boolean> {
		/** ロガー */
		private Log m_log = LogFactory.getLog(JobLoginTask.class);
		/** 実行指示情報 */
		private RunInstructionInfo runInstructionInfo;
		/** ログインに使用するパラメータ */
		private LoginParameter parameter;
		/** ログイン失敗時のリトライ回数 */
		private int retry;

		/**
		 * コンストラクタ
		 * 
		 * @param runInstructionInfo
		 *            ジョブ実行指示情報
		 * @param parameter
		 *            ログインパラメータ
		 * @param retry
		 *            ログイン失敗時のリトライ回数
		 */
		private JobLoginTask(RunInstructionInfo runInstructionInfo, LoginParameter parameter, int retry) {
			this.runInstructionInfo = runInstructionInfo;
			this.parameter = parameter;
			this.retry = retry;
			m_log.debug("run() sessionId=" + runInstructionInfo.getSessionId() + ", jobunitId="
					+ runInstructionInfo.getJobunitId() + ", jobId=" + runInstructionInfo.getJobId() + ", facilityId="
					+ runInstructionInfo.getFacilityId() + ", userId=" + parameter.getUserId() + ", retry=" + retry);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public Boolean call() {
			m_log.info("call() SessionID=" + this.runInstructionInfo.getSessionId()
				+ ", JobunitID=" + this.runInstructionInfo.getJobunitId()
				+ ", JobID=" + this.runInstructionInfo.getJobId()
				+ ", FacilityID=" + this.runInstructionInfo.getFacilityId()
				+ ", CommandType=" + this.runInstructionInfo.getCommandType()
				+ ", Command=" + this.runInstructionInfo.getCommand());

			LoginExecutor executor = new LoginExecutor(parameter);
			LoginResultEnum result = LoginResultEnum.UNKNOWN;
			final int retryInterval = HinemosPropertyCommon.job_rpa_login_retry_interval.getIntegerValue();
			int retryCount = 0;
			// ログイン実行
			m_log.debug("call() : login, maxRetry=" + retry + ", sleep " + retryInterval + "ms");
			do {
				m_log.debug("call() : retry=" + retryCount);
				if (retryCount > 0) {
					try {
						m_log.debug("call() : login retring, retry=" + retryCount + ", maxRetry=" + retry + ", sleep " + retryInterval + "ms");
						// ログイン失敗のメッセージを出力
						new JobControllerBean().setMessage(runInstructionInfo.getSessionId(),
								runInstructionInfo.getJobunitId(), runInstructionInfo.getJobId(),
								runInstructionInfo.getFacilityId(), MessageConstant.MESSAGE_JOB_RPA_LOGIN_FAILED
										.getMessage(String.valueOf(retryCount), String.valueOf(this.retry)));
						Thread.sleep(retryInterval);
					} catch (InterruptedException e) {
						m_log.warn("call() : thread interrupted");
						break;
					}
				}
				// ログインが成功した場合、ログアウトまで待機
				result = executor.login();
				m_log.debug("call() : login result=" + result);
				if (result == LoginResultEnum.SUCCESS
						|| result == LoginResultEnum.CANCELL
						) {
					// 成功、キャンセルはリトライしない
					break;
				}
				// ログインが失敗した場合、指定された回数リトライする
			} while (retryCount++ < retry);

			boolean methodResult = result == LoginResultEnum.SUCCESS;
			m_log.info("call() : end, result=" + methodResult);

			// 処理完了時にキャッシュから削除する
			futureCache.remove(getKey(runInstructionInfo));

			return methodResult;
		}
	}

	/**
	 * 指示情報を元にキー文字列を返します。
	 * 
	 * @param runInstructionInfo
	 *            指示情報
	 * @return キー文字列
	 */
	private static String getKey(RunInfo runInfo) {
		return runInfo.getSessionId() + "#" + runInfo.getJobunitId() + "#" + runInfo.getJobId() + "#"
				+ runInfo.getFacilityId();
	}

	/**
	 * ログイン処理が継続中の場合は停止します。
	 * 
	 * @param runInfo
	 */
	public static void cancel(RunInfo runInfo) {
		m_log.debug("cancel() : runInfo=" + runInfo);

		String key = getKey(runInfo);
		m_log.debug("cancel() : key=" + key);

		Future<Boolean> future = futureCache.get(key);
		if (future != null) {
			future.cancel(true);
		}
		futureCache.remove(key);
	}
	
	/**
	 * ログイン処理が継続中なら一定時間待機し、それでも変わらない場合は強制停止します。
	 * 
	 * @param runInfo
	 */
	public static void waitStopAndCancel(RunInfo runInfo) {
		m_log.debug("checkRunAndCancel() : runInfo=" + runInfo);
		/// ログアウトの確認間隔 
		int logoutInterval = HinemosPropertyCommon.job_rpa_login_thread_logout_check_interval.getIntegerValue();
		//* ログアウトの確認タイムアウト
		int logoutTimeout = HinemosPropertyCommon.job_rpa_login_thread_logout_check_timeout.getIntegerValue();

		// futureCache に 該当ジョブの情報がある場合、ログイン処理が継続中とみなす。
		String key = getKey(runInfo);
		Future<Boolean> future = futureCache.get(key);
		long limitMills = HinemosTime.currentTimeMillis() + logoutTimeout;
		while (limitMills > HinemosTime.currentTimeMillis()) {
			if (future == null) {
				break;
			}
			try {
				Thread.sleep(logoutInterval);
			} catch (InterruptedException e) {
				break;
			}
			future = futureCache.get(key);
		}
		//  ログイン処理が継続中のままならキャンセルする
		if (future != null) {
			m_log.info("checkRunAndCancel() : Cancel the thread. key=" + key);
			future.cancel(true);
			futureCache.remove(key);
		}
	}
	
}
