/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.binary.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.binary.BinaryMonitor;
import com.clustercontrol.agent.binary.BinaryMonitorManager;
import com.clustercontrol.agent.binary.readingstatus.MonitorReadingStatus;
import com.clustercontrol.agent.binary.readingstatus.RootReadingStatus;
import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.util.HinemosTime;

/**
 * 定期バイナリ監視毎スレッド定義クラス.<br>
 * <br>
 * 監視設定毎のrunintervalに従って走るスレッド.<br>
 */
public class EveryIntervalMonitorThread extends Thread {

	/** ロガー */
	private static Log log = LogFactory.getLog(EveryIntervalMonitorThread.class);
	/** スレッド名 */
	public static final String threadName = "EveryIntervalMonitorThread";

	/** スレッド繰返し(trueだとrun()の処理内容が繰返される). */
	private boolean loop = true;
	/** スレッドに紐づく監視処理. */
	private MonitorInfoWrapper monInfo;
	/** 更新用のスレッドに紐づく監視処理. */
	private MonitorInfoWrapper refreshMonInfo = new MonitorInfoWrapper(null, null);
	/** スレッド開始時間(性能検証用). */
	private long startmsec = 0L;

	/** 紐付け監視設定のロック用オブジェクト */
	private Object refreshLock = new Object();

	/** コンストラクタ. */
	public EveryIntervalMonitorThread(MonitorInfoWrapper monInfo) {
		this.monInfo = monInfo;
	}

	/**
	 * スレッド実行処理.
	 */
	@Override
	public void run() {
		log.info("run " + threadName);
		log.debug("run " + threadName + " threadID=" + this.getId());
		while (loop) {
			try {
				if (log.isDebugEnabled()) {
					long msec = 0L;
					if (this.startmsec > 0) {
						msec = HinemosTime.getDateInstance().getTime() - this.startmsec;
					}
					log.debug(String.format(
							"run() : " + threadName + " is on top of run(). thread interval time=%dmsec, monitorId=%s",
							msec, this.monInfo.getId()));
					this.startmsec = HinemosTime.getDateInstance().getTime();
				}

				// 更新用監視設定を処理対象として反映(ロック時間短縮).
				synchronized (this.refreshLock) {
					if (this.refreshMonInfo.monitorInfo != null || this.refreshMonInfo.runInstructionInfo != null) {
						this.monInfo = new MonitorInfoWrapper(this.refreshMonInfo.monitorInfo,
								this.refreshMonInfo.runInstructionInfo);
						this.refreshMonInfo = new MonitorInfoWrapper(null, null);
					}
				}

				// 監視対象のファイル構成と読込状態を更新.
				List<MonitorInfoWrapper> monList = new ArrayList<MonitorInfoWrapper>();
				monList.add(monInfo);
				BinaryMonitorManager.refreshReadingStatus(Long.toString(this.getId()), monList,
						monInfo.monitorInfo.getRunInterval(), null);

				// 監視対象ファイル毎に監視用オブジェクトを生成.
				BinaryMonitorManager.refreshMonitorObject(Long.toString(this.getId()));

				boolean continueFlg = false;
				Map<String, BinaryMonitor> cache = BinaryMonitorManager.binMonCacheMap.get(Long.toString(this.getId()));
				if (cache != null && !cache.isEmpty()) {
					// 監視処理前にローテーションチェック.
					BinaryMonitorManager.checkRotatedObject(cache);
					// 監視用オブジェクト毎に監視処理を実行
					for (BinaryMonitor binaryMonitor : cache.values()) {
						// 監視処理実行.
						if (binaryMonitor.run()) {
							// ローテーションで作成されたファイルのみを読込んだ場合は監視実行順がずれるので
							// 一度抜けてrefreshからやり直す.
							continueFlg = true;
							break;
						}
					}
				}
				if (continueFlg) {
					// まだ読込途中なのでユーザー設定の監視間隔分あけずにファイル読込を実行.
					continue;
				}

			} catch (Exception e) {
				log.warn(threadName + " : " + monInfo.getId() + " : " + e.getClass().getCanonicalName() + ", "
						+ e.getMessage(), e);
			} catch (Throwable e) {
				log.error(threadName + " : " + monInfo.getId() + " : " + e.getClass().getCanonicalName() + ", "
						+ e.getMessage(), e);
			}
			try {
				// ユーザー設定の監視間隔.
				if (monInfo.monitorInfo.getRunInterval() != null && monInfo.monitorInfo.getRunInterval() > 0) {
					Thread.sleep(monInfo.monitorInfo.getRunInterval() * 1000);
				} else {
					log.warn(threadName + " for " + monInfo.getId() + " isn't set RunInterval.");
					break;
				}
			} catch (InterruptedException e) {
				log.info(threadName + " for " + monInfo.getId() + " is Interrupted");
				break;
			}
		}
		this.close();
	}

	/** 監視設定更新. */
	public void refreshMonInfo(MonitorInfoWrapper refreshMonInfo) {
		synchronized (this.refreshLock) {
			this.refreshMonInfo = refreshMonInfo;
		}
	}

	/**
	 * 監視設定全削除時のRSディレクトリ削除(terminate前に呼び出す).
	 */
	public void deleteRootRsDir() {
		RootReadingStatus rootRs = BinaryMonitorManager.rootRSMap.get(Long.toString(this.getId()));
		for (MonitorReadingStatus monitorRs : rootRs.getMonitorRSMap().values()) {
			monitorRs.deleteStoreDir();
		}
	}

	/**
	 * スレッド停止.
	 */
	public void terminate() {
		loop = false;
	}

	/**
	 * スレッドクローズ(スレッド紐づきオブジェクト削除・ログ出力).
	 */
	private void close() {
		BinaryMonitorManager.rootRSMap.remove(Long.toString(this.getId()));
		BinaryMonitorManager.binMonCacheMap.remove(Long.toString(this.getId()));
		log.info("terminate() : " + threadName + " is terminated for %s" + this.monInfo.getId());
		log.debug("terminate() : " + threadName + " threadID=" + this.getId());
	}
}
