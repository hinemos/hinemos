/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.jobmanagement.rpa.bean.RoboInfo;

/**
 * /* RpaExecutorのキャッシュ<br>
 * 終了遅延等で処理を中断する際に使用します。
 * 
 * @see AbortFileObserver
 */
public class ExecutorCache {
	/** ロガー */
	private static Log m_log = LogFactory.getLog(ExecutorCache.class);
	/** RpaExecutorのキャッシュ */
	private final static Map<String, ScenarioExecutor> cache = new ConcurrentHashMap<>();

	/**
	 * コンストラクタ
	 */
	private ExecutorCache() {

	}

	/**
	 * キャッシュから取得します。
	 * 
	 * @param roboInfo
	 *            RPAシナリオ実行情報
	 * @return RpaExecutor
	 */
	public static ScenarioExecutor get(RoboInfo roboInfo) {
		String key = getKey(roboInfo);
		m_log.info("get() :  key=" + key);
		return cache.get(getKey(roboInfo));
	}

	/**
	 * キャッシュに追加します。
	 * 
	 * @param roboInfo
	 *            RPAシナリオ実行情報
	 * @param executor
	 *            RpaExecutor
	 */
	public static void put(RoboInfo roboInfo, ScenarioExecutor executor) {
		String key = getKey(roboInfo);
		m_log.info("put() :  key=" + key);
		cache.put(key, executor);
	}

	/**
	 * キャッシュから削除します。
	 * 
	 * @param roboInfo
	 *            RPAシナリオ実行情報
	 */
	public static void remove(RoboInfo roboInfo) {
		String key = getKey(roboInfo);
		m_log.info("remove() :  key=" + key);
		cache.remove(getKey(roboInfo));
	}

	/**
	 * キャッシュのキーを返します。
	 * 
	 * @param roboInfo
	 *            RPAシナリオ実行情報
	 * @return キャッシュのキー文字列
	 */
	private static String getKey(RoboInfo roboInfo) {
		return roboInfo.getDatetime() + "," + roboInfo.getSessionId() + "," + roboInfo.getJobunitId() + ","
				+ roboInfo.getJobId() + "," + roboInfo.getFacilityId();
	}
}
