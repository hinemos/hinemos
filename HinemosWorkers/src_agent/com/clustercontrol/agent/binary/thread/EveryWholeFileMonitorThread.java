/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.binary.thread;

import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.binary.BinaryMonitor;
import com.clustercontrol.util.HinemosTime;

/**
 * バイナリファイル全体監視スレッド定義クラス.<br>
 * <br>
 * バイナリファイル全体監視処理の実行.
 */
public class EveryWholeFileMonitorThread extends Thread {

	/** ロガー */
	private static Log log = LogFactory.getLog(EveryWholeFileMonitorThread.class);

	/** スレッド名 */
	protected static final String threadName = "EveryWholeFileMonitorThread";

	/** スレッドに紐づく監視オブジェクト. */
	private BinaryMonitor binaryMonitor;

	/** コンストラクタ. */
	public EveryWholeFileMonitorThread(BinaryMonitor binaryMonitor) {
		this.binaryMonitor = binaryMonitor;
	}

	/**
	 * スレッド実行処理.
	 */
	@Override
	public void run() {
		// 性能測定用にスレッド開始間隔の出力.
		long startmsec = HinemosTime.getDateInstance().getTime();
		String startmsecStr = new Timestamp(startmsec).toString();
		log.info(String.format("run " + threadName + " : monitorId=%s, file=%s, monitorTime=%s",
				binaryMonitor.getM_wrapper().getId(), binaryMonitor.getReadingStatus().getMonFileName(), startmsecStr));

		try {
			// 監視処理実行.
			binaryMonitor.setTmpThreadId(Long.toString(this.getId()));
			binaryMonitor.run();
		} catch (Exception e) {
			log.warn(String.format(
					"interrupt " + threadName + " : monitorId=%s, file=%s, monitorTime=%s"
							+ e.getClass().getCanonicalName() + ", " + e.getMessage(),
					binaryMonitor.getM_wrapper().getId(), binaryMonitor.getReadingStatus().getMonFileName(),
					startmsecStr), e);
		} catch (Throwable e) {
			log.error(String.format(
					"interrupt " + threadName + " : monitorId=%s, file=%s, monitorTime=%s"
							+ e.getClass().getCanonicalName() + ", " + e.getMessage(),
					binaryMonitor.getM_wrapper().getId(), binaryMonitor.getReadingStatus().getMonFileName(),
					startmsecStr), e);
		}
		// 性能測定用にスレッド完了時間出力.
		if (log.isDebugEnabled()) {
			long msec = HinemosTime.getDateInstance().getTime() - startmsec;
			log.debug(String.format("end " + threadName + " : processing time=%dmsec, monitorId=%s, file=%s", msec,
					binaryMonitor.getM_wrapper().getId(), binaryMonitor.getReadingStatus().getMonFileName()));
		}
		log.info(String.format("end " + threadName + " : monitorId=%s, file=%s, monitorTime=%s",
				binaryMonitor.getM_wrapper().getId(), binaryMonitor.getReadingStatus().getMonFileName(), startmsecStr));
	}

}