/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.binary.thread;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

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
 * バイナリ監視スレッド定義クラス.<br>
 * <br>
 * ファイル増分監視の内、時間区切り以外とパケットキャプチャ自動生成ファイルの監視.
 * 
 */
public class IncrementMonitorThread extends Thread {

	/** ロガー */
	private static Log log = LogFactory.getLog(IncrementMonitorThread.class);

	/** スレッド名 */
	public static final String threadName = "IncrementMonitorThread";

	/** ファイル増分監視間隔デフォルト値(AgentPropertiesの設定優先) */
	private int runInterval = 10000; // 10sec

	/** スレッド繰返し(trueだとrun()の処理内容が繰返される). */
	private boolean loop = true;

	/** スレッド開始時間(性能検証用). */
	private long startmsec = 0L;

	/** 紐づき監視設定全量(キー:監視ID, 値:監視設定). */
	private Map<String, MonitorInfoWrapper> monitorMap = new ConcurrentHashMap<String, MonitorInfoWrapper>();

	/** 更新用監視設定全量(キー:監視ID, 値:監視設定). */
	private Map<String, MonitorInfoWrapper> refreshMap = new ConcurrentHashMap<String, MonitorInfoWrapper>();

	/** 削除監視設定(キー:監視ID, 値:監視設定). */
	private Map<String, MonitorInfoWrapper> deleteMap = new ConcurrentHashMap<String, MonitorInfoWrapper>();

	/**
	 * コンストラクタ.<br>
	 * <br>
	 * 監視設定リスト指定.
	 */
	public IncrementMonitorThread(Map<String, MonitorInfoWrapper> monitorMap) {
		this.monitorMap = monitorMap;
		// 監視スレッド間隔の設定.
		this.runInterval = BinaryMonitorConfig.getIncrementBinInterval();
	}

	/**
	 * スレッド実行処理.
	 */
	@Override
	public void run() {
		log.info("run " + threadName);
		while (loop) {
			try {
				if (log.isDebugEnabled()) {
					long msec = 0L;
					if (this.startmsec > 0) {
						msec = HinemosTime.getDateInstance().getTime() - this.startmsec;
					}
					log.debug(String.format(
							"run() : " + threadName + " is on top of run(). thread interval time=%dmsec", msec));
					this.startmsec = HinemosTime.getDateInstance().getTime();
				}

				// 登録されている最新の監視設定取得(ファイル増分監視(時間区切り以外)).
				this.refreshMonitorMap();

				// 監視対象のファイル構成と読込状態を更新.
				BinaryMonitorManager.refreshReadingStatus(Long.toString(this.getId()),
						new ArrayList<MonitorInfoWrapper>(this.monitorMap.values()), runInterval,
						this.deleteMap.values());

				// 監視対象ファイル毎に監視用オブジェクトを生成.
				BinaryMonitorManager.refreshMonitorObject(Long.toString(this.getId()));

				Map<String, BinaryMonitor> cache = BinaryMonitorManager.binMonCacheMap.get(Long.toString(this.getId()));
				if (cache != null && !cache.isEmpty()) {
					// 監視処理前にローテーションチェック.
					BinaryMonitorManager.checkRotatedObject(cache);
					// 生成した監視用オブジェクト毎に監視処理を実行.
					for (BinaryMonitor binaryMonitor : cache.values()) {
						// 監視処理実行.
						if (binaryMonitor.run()) {
							// ローテーションで作成されたファイルのみを読込んだ場合は監視実行順がずれるので
							// 一度forループを抜けてrefreshからやり直す.
							break;
						}
					}
				}
			} catch (Exception e) {
				log.warn(threadName + " : " + e.getClass().getCanonicalName() + ", " + e.getMessage(), e);
			} catch (Throwable e) {
				log.warn(threadName + ": " + e.getClass().getCanonicalName() + ", " + e.getMessage(), e);
			}

			try {
				Thread.sleep(runInterval);
			} catch (InterruptedException e) {
				log.info(threadName + " is Interrupted");
				break;
			}
		}
		this.close();
	}

	/**
	 * 監視設定更新.
	 */
	public void refreshMonitorMap() {
		// 削除された監視設定を一度クリア.
		this.deleteMap.clear();
		// 更新用の監視設定なければ終了.
		if (this.refreshMap.isEmpty()) {
			return;
		}

		// 更新用の監視設定と前回の監視設定を比較して削除された監視設定を抽出.
		Set<String> newMonitorIdList = this.refreshMap.keySet();
		for (MonitorInfoWrapper oldMonitorInfo : this.monitorMap.values()) {
			String oldId = oldMonitorInfo.getId();
			if (!newMonitorIdList.contains(oldId)) {
				this.deleteMap.put(oldId, oldMonitorInfo);
			}
		}

		// 監視設定を更新.
		this.monitorMap = new TreeMap<String, MonitorInfoWrapper>(this.refreshMap);
		this.refreshMap.clear();

	}

	/**
	 * 監視設定追加(渡される設定に過不足ない前提).
	 */
	public void addMonitorMap(Map<String, MonitorInfoWrapper> newMonMap) {
		this.refreshMap.putAll(newMonMap);
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
		this.monitorMap.clear();
		this.refreshMap.clear();

		log.info("terminate() : BinaryThread is terminated.");
	}

}
