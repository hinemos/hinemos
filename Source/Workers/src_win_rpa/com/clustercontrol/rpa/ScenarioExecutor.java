/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.rpa.bean.RoboResultInfo;
import com.clustercontrol.jobmanagement.rpa.bean.RoboRunInfo;
import com.clustercontrol.jobmanagement.rpa.util.CommandProxy;
import com.clustercontrol.rpa.handler.AbstractHandler;
import com.clustercontrol.util.CommandExecutor.CommandResult;
import com.clustercontrol.util.HinemosTime;

/**
 * RPAシナリオ実行を行うクラス
 */
public class ScenarioExecutor {

	/** ロガー */
	private static Log m_log = LogFactory.getLog(ScenarioExecutor.class);
	/** スレッド名 */
	private static final String threadName = "ScenarioExecutor";
	/** スレッドプール */
	private ExecutorService executor;
	/** シナリオ実行完了後の処理を行うハンドラ */
	private AbstractHandler handler;

	/**
	 * コンストラクタ
	 * 
	 * @param handler
	 *            処理完了後に実行するハンドラ
	 */
	public ScenarioExecutor(AbstractHandler handler) {
		this.handler = handler;
		// 処理中断時にshutdownNowを呼ぶためスレッドプールを使用
		executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r, threadName);
				thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

					@Override
					public void uncaughtException(Thread t, Throwable e) {
						m_log.error("uncaughtException() : " + e.getMessage(), e);

					}
				});
				return thread;
			}
		});
	}

	/**
	 * RPA製品を起動しシナリオを実行するクラス
	 */
	private class RpaExecuteTask implements Callable<Boolean> {

		/** RPA実行指示情報 */
		private RoboRunInfo roboRunInfo;

		/**
		 * コンストラクタ
		 * 
		 * @param roboRunInfo
		 *            RPA実行指示情報
		 */
		private RpaExecuteTask(RoboRunInfo roboRunInfo) {
			this.roboRunInfo = roboRunInfo;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public Boolean call() {
			CommandResult ret;
			RoboResultInfo roboResultInfo = new RoboResultInfo(roboRunInfo);
			try {
				// RPAツールのプロセス終了は子プロセスも含めて全てのプロセスを終了するコマンドから行う
				// そのためCommandExecutorではプロセスを終了しないようにする
				ret = CommandProxy.execute(roboRunInfo.getExecCommand(), false);
				if (ret != null) {
					m_log.debug("execute() : exitCode=" + ret.exitCode + ", stdout=" + ret.stdout + ", stderr="
							+ ret.stderr);
					m_log.info("execute() : scenario execution completed, exitCode=" + ret.exitCode);
					roboResultInfo.setDatetime(HinemosTime.currentTimeMillis());
					roboResultInfo.setReturnCode(ret.exitCode);
					handler.handle(roboResultInfo); // シナリオ実行完了後の処理を開始
					return true;
				} else {
					m_log.info("execute() : waiting scenario completion aborted");
					handler.handle(roboResultInfo); // 中断した場合exitCodeはnull
					return false;
				}
			} catch (HinemosUnknown e) {
				// コマンド実行で例外が発生した場合
				m_log.warn("execute() : scenario execution failed");
				roboResultInfo.setDatetime(null);
				roboResultInfo.setError(true);
				roboResultInfo.setErrorMessage(e.getMessage());
				handler.handle(roboResultInfo);
				return false;
			}
		}
	}

	/**
	 * RPAシナリオを実行します。
	 * 
	 * @param roboRunInfo
	 *            RPA実行指示情報
	 * @return true: RPAシナリオの正常終了、false: それ以外
	 */
	public boolean execute(RoboRunInfo roboRunInfo) {
		Future<Boolean> future = executor.submit(new RpaExecuteTask(roboRunInfo));
		try {
			// 処理が終了するまで待機する
			return future.get();
		} catch (InterruptedException e) {
			m_log.warn("thread interrupted");
		} catch (ExecutionException e) {
			m_log.error("execute() : " + e.getMessage(), e);
		}
		return false;

	}

	/**
	 * 処理を中断します。<br>
	 * 
	 * @see AbortFileObserver
	 */
	public void abort() {
		m_log.info("abort()");
		executor.shutdownNow();
	}
}
