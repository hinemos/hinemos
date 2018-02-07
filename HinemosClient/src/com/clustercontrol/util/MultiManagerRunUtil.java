/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.SingletonUtil;


/**
 * マルチマネージャの実行管理を行うクラス<BR>
 * マルチマネージャモードでAPIを呼び出す場合は本クラスを継承すること。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class MultiManagerRunUtil {
	private static Log m_log = LogFactory.getLog(MultiManagerRunUtil.class);

	/** スレッドプール数 */
	private static int permits = 8; //TODO ユーザーが変更できるようにする
	/** 取得情報のリスト内の位置 */
	public static int POS_INFO = 0;
	/** エラー情報のリスト内の位置 */
	public static int POS_ERROR = 1;
	private String workerName = "MultiManagerRunUtil";

	private static MultiManagerRunUtil getInstance() {
		return SingletonUtil.getSessionInstance(MultiManagerRunUtil.class);
	}

	private ExecutorService _executorService = Executors.newFixedThreadPool(
			permits,
			new ThreadFactory() {
				private volatile int _count = 0;

				@Override
				public Thread newThread(Runnable r) {
					String threadName = workerName + "-" + _count++;
					m_log.debug("newThread=" + threadName);
					return new Thread(r, threadName);
				}
			}
	);

	public static ExecutorService getExecutorService() {
		MultiManagerRunUtil util = getInstance();
		return util._executorService;
	}
}
