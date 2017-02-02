/**********************************************************************
 * Copyright (C) 2014 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 *
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

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
