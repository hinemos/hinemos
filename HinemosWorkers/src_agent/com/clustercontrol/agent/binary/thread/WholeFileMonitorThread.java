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
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.binary.BinaryMonitor;
import com.clustercontrol.agent.binary.BinaryMonitorConfig;
import com.clustercontrol.agent.binary.BinaryMonitorManager;
import com.clustercontrol.agent.binary.readingstatus.MonitorReadingStatus;
import com.clustercontrol.agent.binary.readingstatus.RootReadingStatus;
import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.util.HinemosTime;

/**
 * バイナリファイル監視スレッド定義クラス.<br>
 * <br>
 * 規定秒数毎にファイル最終更新日時を比較して書込み完了ファイルから監視スレッドを走らせる.
 */
public class WholeFileMonitorThread extends Thread {
	/** ロガー */
	private static Log log = LogFactory.getLog(WholeFileMonitorThread.class);
	/** スレッド名 */
	public static final String threadName = "WholeFileMonitorThread";

	/** ファイル全体監視間隔デフォルト値(AgentPropertiesの設定優先) */
	private int wholeRunInterval = 10000; // 10sec
	/** スレッド繰返し(trueだとrun()の処理内容が繰返される). */
	private boolean loop = true;
	/** スレッドに紐づく監視設定. */
	private List<MonitorInfoWrapper> monInfoList = new ArrayList<MonitorInfoWrapper>();
	/** 更新用のスレッドに紐づく監視設定. */
	private List<MonitorInfoWrapper> refreshMonInfoList = new ArrayList<MonitorInfoWrapper>();
	/** スレッド開始時間(性能検証用). */
	private long startmsec = 0L;

	/** 紐付け監視設定のロック用オブジェクト */
	private Object refreshLock = new Object();

	/** コンストラクタ. */
	public WholeFileMonitorThread(List<MonitorInfoWrapper> monInfoList) {
		this.monInfoList = monInfoList;
		this.wholeRunInterval = BinaryMonitorConfig.getWholeBinInterval();
	}

	/**
	 * スレッド実行処理.
	 */
	@Override
	public void run() {
		log.info("run " + threadName);
		while (loop) {
			try {
				// 性能測定用にスレッド開始間隔の出力.
				if (log.isDebugEnabled()) {
					long msec = 0L;
					if (this.startmsec > 0) {
						msec = HinemosTime.getDateInstance().getTime() - this.startmsec;
					}
					log.debug(String.format(
							"run() : " + threadName + " is on top of run(). thread interval time=%dmsec.", msec));
					this.startmsec = HinemosTime.getDateInstance().getTime();
				}

				// 監視設定等更新(書込み完了ファイルだけ監視対象としてオブジェクト取得).
				this.refresh();

				// 監視オブジェクト取得.
				Map<String, BinaryMonitor> cache = BinaryMonitorManager.binMonCacheMap.get(Long.toString(this.getId()));

				// 監視処理実行.
				if (cache != null && !cache.isEmpty()) {
					for (Entry<String, BinaryMonitor> entry : cache.entrySet()) {
						BinaryMonitor binaryMonitor = entry.getValue();
						if (binaryMonitor != null) {
							// 監視処理実行.
							EveryWholeFileMonitorThread wholeFileMonThread = new EveryWholeFileMonitorThread(
									binaryMonitor);
							wholeFileMonThread.setName(EveryWholeFileMonitorThread.threadName + "_"
									+ binaryMonitor.getM_wrapper().getId());
							wholeFileMonThread.start();
							log.info("start() : " + EveryWholeFileMonitorThread.threadName + " is started. for "
									+ binaryMonitor.getM_wrapper().getId());
						} else {
							log.warn(EveryWholeFileMonitorThread.threadName + " is skipped to start " + "because "
									+ EveryWholeFileMonitorThread.threadName + " has null Object. key="
									+ entry.getKey());
						}
					}
				} else {
					log.debug(threadName + " has no monInfoList.");
				}

			} catch (Exception e) {
				log.warn(threadName + " : " + e.getClass().getCanonicalName() + ", " + e.getMessage(), e);
			} catch (Throwable e) {
				log.warn(threadName + " : " + e.getClass().getCanonicalName() + ", " + e.getMessage(), e);
			}

			// スレッド停止.
			try {
				Thread.sleep(wholeRunInterval);
			} catch (InterruptedException e) {
				log.info(threadName + " for is Interrupted");
				break;
			}
		}
		this.close();
	}

	/**
	 * 監視処理.
	 */
	private void refresh() {
		// 更新用の監視設定と前回の監視設定を比較して削除された監視設定を抽出.
		List<MonitorInfoWrapper> deleteMonitorList = null;
		// 新しい監視設定を取得している場合は更新.
		synchronized (this.refreshLock) {
			if (!this.refreshMonInfoList.isEmpty()) {
				deleteMonitorList = new ArrayList<MonitorInfoWrapper>();

				// 更新分の監視IDリストを取得.
				List<String> refreshIdList = new ArrayList<String>();
				for (MonitorInfoWrapper newMonitor : this.refreshMonInfoList) {
					refreshIdList.add(newMonitor.getId());
				}

				// 更新分の監視IDリストに前回の監視IDがない場合は削除された監視設定.
				for (MonitorInfoWrapper beforeMonitor : this.monInfoList) {
					if (!refreshIdList.contains(beforeMonitor.getId())) {
						deleteMonitorList.add(beforeMonitor);
					}
				}

				this.monInfoList = new ArrayList<MonitorInfoWrapper>(this.refreshMonInfoList);
				this.refreshMonInfoList.clear();
			}
		}
		// 監視対象のファイル構成と読込状態を更新.
		BinaryMonitorManager.refreshReadingStatus(Long.toString(this.getId()), this.monInfoList, wholeRunInterval,
				deleteMonitorList);
		// 監視対象ファイル毎に監視用オブジェクトを生成.
		BinaryMonitorManager.refreshMonitorObject(Long.toString(this.getId()));
	}

	/**
	 * 更新用の監視設定セット<br>
	 * <br>
	 * ロック時間短縮のため更新用リストを操作.
	 **/
	public void setRefreshMonInfoList(List<MonitorInfoWrapper> refreshMonInfoList) {
		synchronized (this.refreshLock) {
			this.refreshMonInfoList = refreshMonInfoList;
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
		synchronized (this.refreshLock) {
			this.refreshMonInfoList.clear();
			this.monInfoList.clear();
		}

		log.info("terminate() : " + threadName + " is terminated.");
	}

}
