/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.monitor.util;

import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.poller.util.DataTable;
import com.clustercontrol.poller.util.TableEntry;
import com.clustercontrol.poller.util.TableEntry.ErrorType;
import com.clustercontrol.util.HinemosTime;


/**
 * 性能情報を監視間隔ごとに、前回分＋今回分を保持するためのホルダークラス。
 * 
 * このクラスのクラスメソッドは全てスレッドセーフ。
 * 但し、このクラスのインスタンスメソッドはスレッドセーフではない。
 * インスタンスに並行アクセスすることは想定していない。
 *
 */
public final class PerfDataQueue {
	
	// --------------------- クラスメソッド・変数（各ノードに関するインスタンスの制御を行なう） ---------------------
	
	private static final Log log = LogFactory.getLog( PerfDataQueue.class );
	private static final ConcurrentMap<String, PerfDataQueue> allNodePerfQueue = new ConcurrentHashMap<>();
	
	/**
	 * 指定したノードに関する性能情報データのインスタンスを返す
	 * @param facilityId
	 * @return
	 */
	public static PerfDataQueue getInstance(String facilityId) {
		// 指定されたノードに対する、作成済みのインスタンスがあればそれを返す
		final PerfDataQueue cachedPerfHistory = allNodePerfQueue.get(facilityId);
		if (cachedPerfHistory != null) {
			return cachedPerfHistory;
		}
		
		// 指定されたノードに対するインスタンスが存在しない場合、新規に作成する
		final PerfDataQueue newInstance = new PerfDataQueue();
		
		// 別スレッドにより登録済みならそちらを返す。登録されていなければ作成したものを返す
		final PerfDataQueue prevInstance = allNodePerfQueue.putIfAbsent(facilityId, newInstance);
		if (prevInstance == null) {
			return newInstance;
		} else {
			return prevInstance;
		}
	}
	
	/**
	 * 指定したノードに関する性能情報データのインスタンスを削除する
	 * @param facilityId
	 */
	public static void removeInstance(String facilityId) {
		allNodePerfQueue.remove(facilityId);
	}
	
	// --------------------- インスタンスメソッド・変数（各インスタンスが1つのノードに関する性能情報の処理を行なう） ---------------------
	
	/**
	 * コンストラクタは外部から呼ばない。（getInstance がイニシャライザの役割も担っている）
	 */
	private PerfDataQueue() {
	}
	
	private final Map<Integer, Deque<DataTable>> interval2dataTableQueue = new ConcurrentHashMap<>();
	
	/**
	 * 新規に収集したデータを格納し、今回分と前回分を残し、それ以上古いデータを破棄する
	 * 
	 * @param pollingTarget Map<監視間隔, Map<収集プロトコル, Set<収集キー>>> の形をした収集ターゲットの情報
	 * @param polledData pollingTargetを使って、実際に収集されたデータ （収集プロトコルをキーとし、収集済みのDataTableが値となるマップの形式）
	 */
	public void pushNewData(final Map<Integer, Map<String, Set<String>>> pollingTarget, final Map<String, DataTable> polledData) {
		if (log.isDebugEnabled()) {
			log.debug("pushNewData(node=" + getFacilityId() + ") start : pollingTarget = " + pollingTarget);
			log.debug("pushNewData(node=" + getFacilityId() + ") start :    polledData = " + polledData);
		}
		/**
		 * この関数内で行なっている処理のイメージは以下のような感じ。
		 * 
		 * 例えば実際にポーリングしたデータが以下だったとする
		 * [.1.2 = 10], [.1.3 = 20], [.1.4 = 30]
		 * 
		 * 一方、今このタイミングが1分間隔の監視と5分間隔の監視が重なるタイミングだとして、
		 * 
		 * 1分間隔監視＝.1.2 と .1.3 が必要
		 * 5分間隔監視＝ .1.2 と .1.4 が必要
		 * 
		 * となった場合、polledDataから
		 * 
		 * 1分間隔用データ ：  [.1.2 = 10], [.1.3 = 20]
		 * 5分間隔用データ : [.1.2 = 10], [.1.4 = 30]
		 * 
		 * というデータを作る。
		 * 
		 * 下記のように過去のデータ履歴を2回分持つキューがあるので、
		 * 
		 * 1分Queue・・・[現在のデータ], [1分前のデータ]
		 * 5分Queue・・・[現在のデータ], [5分前のデータ]
		 * 10分Queue・・・[現在のデータ], [10分前のデータ]
		 * 30分Queue・・・
		 * ...
		 * 
		 * 上記で作成したデータを、1分のキュー、5分のキューに突っ込む。3つ以上になるようであれば、最も古いデータは削除する。
		 */
		
		// 取得済みのデータから、監視間隔ごとに必要なデータをコピーする処理を行なう
		for (final Map.Entry<Integer, Map<String, Set<String>>> pollingTargetEntry : pollingTarget.entrySet()) {
			final int interval = pollingTargetEntry.getKey();
			
			// この監視間隔において必要なキー情報（キーは収集プロトコル、値は収集キー(OIDなど)のセット）
			final Map<String, Set<String>> partKey = pollingTargetEntry.getValue();
			
			// この監視間隔において必要とされる、実収集データ（以下で構築）
			final DataTable buildData = new DataTable();
			
			// 収集プロトコルごとにDataTableが分かれているため、それぞれごとにコピーを行なう
			for (final Map.Entry<String, Set<String>> partKeyEntry : partKey.entrySet()) {
				final String collectMethod = partKeyEntry.getKey();
				final Set<String> keys = partKeyEntry.getValue();
				
				if (polledData.containsKey(collectMethod)) {
					// 当該監視間隔・収集プロトコルで収集されたデータの中から、現在対象としている監視間隔において必要となる情報のみを抽出する
					final DataTable polledTargetData = polledData.get(collectMethod);
					for (final String key : keys) {
						final String entryKey = entryKey(collectMethod, key);
						final Set<TableEntry> addTarget = polledTargetData.getValueSetStartWith(entryKey);
						if (addTarget != null) {
							buildData.putValue(addTarget);
						} else {
							// FIXME nagatsumas ポーリングデータとして存在しない場合はどうするべきか？
						}
					}
				} else {
					// 特定の収集プロトコルに関して、pollingTargetに存在しているにもかかわらず、収集データのDataTable自体が無い場合、
					// 収集プロトコルが未定義以外の状況は考えづらいため、未定義のエラーを格納する
					for (final String key : keys) {
						final String entryKey = entryKey(collectMethod, key);;
						buildData.putValue(new TableEntry(entryKey, HinemosTime.currentTimeMillis(), ErrorType.ILLEGAL_PROTOCOL, null));
					}
				}
			}
			
			// 出来上がったデータをキュー入れる。データは直近2回分のみ保持するよう、古いデータは破棄する
			if (interval2dataTableQueue.containsKey(interval) == false) {
				interval2dataTableQueue.put(interval, new LinkedBlockingDeque<DataTable>());
			}
			final Deque<DataTable> queue = interval2dataTableQueue.get(interval);
			queue.addLast(buildData);
			while (queue.size() > 2) {
				queue.removeFirst();
			}
			// FIXME nagatsumas pollingTarget.keySet() に存在しているにもかかわらず、そのとき何もデータが無かった場合、空のデータを入れる必要がある（このタイミングで該当する監視間隔の監視が1つも無かったことを意味している）
		}
		// (デバッグ用)現在のキューの状態を表示する
		if (log.isDebugEnabled()) {
			final StringBuilder sb = new StringBuilder();
			String node = getFacilityId();
			for (final Map.Entry<Integer, Deque<DataTable>> interval2queueEntry : interval2dataTableQueue.entrySet()) {
				final int interval = interval2queueEntry.getKey();
				final Deque<DataTable> queue = interval2queueEntry.getValue();
				final Iterator<DataTable> itr = queue.iterator();
				int i = 0;
				while (itr.hasNext()) {
					final DataTable dataTable = itr.next();
					sb.setLength(0);
					sb.append("pushNewData(node=").append(node).append(") end. Queue(interval=").append(interval)
						.append(")[").append(i++).append("] = ").append(dataTable);
					log.debug(sb);
				}
			}
		}
	}
	
	/**
	 * 指定した監視間隔における直近に格納したデータを取得する
	 * @param monitorInterval 取得したいデータの監視間隔
	 * @return 監視プロトコルごとの、直近で格納した実監視データ
	 */
	public DataTable getCurrentData(int monitorInterval) {
		final Deque<DataTable> queue = interval2dataTableQueue.get(monitorInterval);
		if (queue == null) {
			return null;
		}
		if (queue.size() > 0) {
			return queue.getLast();
		} else {
			return null;
		}
	}
	
	/**
	 * 指定した監視間隔における1回前に格納したデータを取得する
	 * @param monitorInterval 取得したいデータの監視間隔
	 * @return 監視プロトコルごとの、1回前に格納した実監視データ
	 */
	public DataTable getPrevData(int monitorInterval) {
		final Deque<DataTable> queue = interval2dataTableQueue.get(monitorInterval);
		if (queue == null) {
			return null;
		}
		switch (queue.size()) {
		case 0:
		case 1:
			return null;
			
		case 2:
			return queue.getFirst();
			
		default:
			throw new IllegalStateException("queue.size() = " + queue.size() + ". queue size must be 0 to 2");
		}
	}
	
	private String getFacilityId() {
		for (Entry<String, PerfDataQueue> perfEntry : allNodePerfQueue.entrySet()) {
			if (perfEntry.getValue() == this) {
				return perfEntry.getKey();
			}
		}
		return null;
	}
	
	private static String entryKey(String collectMethod, String key) {
		return collectMethod + "." + key;
	}

}
