/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hinemosagent.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Singletons;

/**
 * エージェントアップデートの指示を行ったノードのリストを管理し、
 * ダウンロード枠の制限を行います。
 * <p>
 * {@link Singletons#get(Class)}でインスタンスを取得します。
 *
 * @since 6.2.0
 */
public class AgentUpdateList {
	private static final Log log = LogFactory.getLog(AgentUpdateList.class);

	// 同時更新数の上限
	private final int limit;
	// ノードの情報を管理するmap
	private final Map<String, Entry> map;

	// ノードの情報
	private static class Entry {
		// リストへ追加された日時
		long acquiredTime;
		// ライブラリファイル一覧の取得を行った日時
		long libMapAccessTime;
		// ファイルダウンロード中か否か
		boolean downloading;
		// 最後にダウンロードを行った日時
		long lastDownloadedTime;
	}

	// 外部依存動作をモックへ置換できるように分離
	private External external;
	static class External {
		int getLimit() {
			return HinemosPropertyCommon.repository_agentupdate_limit.getIntegerValue();
		}
		
		long getExpiresTime() {
			return HinemosPropertyCommon.repository_agentupdate_expires.getIntegerValue();
		}
		
		void removeAgentUpdateTopic(String facilityId) {
			AgentConnectUtil.removeAgentUpdateTopic(facilityId);
		}
	}
	
	/**
	 * コンストラクタ。
	 * <p>
	 * 直接インスタンス生成は行わず、{@link Singletons#get(Class)}を使用します。
	 */
	public AgentUpdateList() {
		this(new External());
	}

	AgentUpdateList(External external) {
		this.external = external;
		limit = external.getLimit();
		map = new ConcurrentHashMap<>(limit);
	}

	/**
	 * 指定されたノードが管理下にあるか(更新中か)どうかを返します。
	 * 
	 * @param facilityId ノードのファシリティID。
	 * @return 更新中ならtrue、そうでなければfalse。
	 */
	public boolean isUpdating(String facilityId) {
		return map.containsKey(facilityId);
	}

	/**
	 * 指定されたノードが管理下にあるか(更新中か)どうかを返します。
	 * 
	 * @param facilityId ノードに紐付いたファシリティIDのリスト。
	 * @return 更新中ならtrue、そうでなければfalse。
	 */
	public boolean isUpdating(List<String> facilityIds) {
		for (String facilityId : facilityIds) {
			if (isUpdating(facilityId)) return true;
		}
		return false;
	}
	
	/**
	 * ダウンロード枠の獲得を試みます。
	 * 
	 * @param facilityId ノードのファシリティID。
	 * @return 結果を表す列挙値。
	 */
	public TrialResult tryAcquire(String facilityId) {
		synchronized (map) {
			if (isUpdating(facilityId)) {
				log.debug("tryAcquire: Already updating. facilityId=" + facilityId);
				return TrialResult.ALREADY_EXISTS;
			}

			// 期限切れエントリを削除
			releaseExpired();

			// エントリに空きがあれば獲得する
			if (map.size() < limit) {
				Entry entry = new Entry();
				entry.acquiredTime = HinemosTime.currentTimeMillis();
				entry.libMapAccessTime = 0;
				entry.lastDownloadedTime = 0;
				entry.downloading = false;

				map.put(facilityId, entry);
				log.debug("tryAcquire: Acquired. facilityId=" + facilityId + ", rest=" + (limit - map.size()));
				return TrialResult.ACQUIRED;
			}
			return TrialResult.REJECTED;
		}
	}

	/**
	 * {@link AgentUpdateList#tryAcquire(String)}の戻り値です。
	 */
	public static enum TrialResult {
		/** ダウンロード枠を獲得できた  */
		ACQUIRED,
		/** ダウンロード枠を獲得できなかった */
		REJECTED,
		/** 既にダウンロード枠を獲得済みである */
		ALREADY_EXISTS
	}

	/**
	 * 期限切れとなったノードがあれば、管理から除外します。
	 */
	public void releaseExpired() {
		synchronized (map) {
			long extime = external.getExpiresTime();
			long now = HinemosTime.currentTimeMillis();
			map.entrySet().removeIf(it -> {
				String fid = it.getKey();
				Entry entry = it.getValue();
				if (!entry.downloading) {
					// システムクロックが大きく未来へ進められた(時差が負になる)場合のため、絶対値で判定する
					if (Math.abs(now - entry.acquiredTime) > extime) {
						if (Math.abs(now - entry.libMapAccessTime) > extime) {
							// ver.6.2先行版では前回時刻が"0"のままなので常に成立する
							if (Math.abs(now - entry.lastDownloadedTime) > extime) {
								log.info("releaseExpired: Released. facilityId=" + fid);
								return true;
							}
						}
					}
				}
				return false;
			});
		}
	}
	
	/**
	 * 指定されたノードを管理から除外します。
	 * 
	 * @param facilityId ノードのファシリティID。
	 */
	public void release(String facilityId) {
		Entry removed = map.remove(facilityId);
		external.removeAgentUpdateTopic(facilityId);
		if (removed != null) {
			log.info("release: facilityId=" + facilityId);
		}
	}

	/**
	 * 指定されたノードを管理から除外します。
	 * 
	 * @param facilityIds ノードのファシリティIDのリスト。
	 */
	public void release(List<String> facilityIds) {
		for (String facilityId : facilityIds) {
			release(facilityId);
		}
	}

	/**
	 * 指定されたノードが、ライブラリファイルの一覧へアクセスしたことを記録します。
	 * 
	 * @param facilityId ノードのファシリティID。
	 */
	public void recordLibMapAccessTime(String facilityId) {
		Entry entry = map.get(facilityId);
		if (entry == null) return;
		entry.libMapAccessTime = HinemosTime.currentTimeMillis();
		log.debug("recordLibMapAccessTime: facilityId=" + facilityId);
	}
	
	/**
	 * 指定されたノードが、ライブラリファイルのダウンロードを開始したことを記録します。
	 * 
	 * @param facilityId ノードのファシリティID。
	 */
	public void recordDownloadStart(String facilityId) {
		Entry entry = map.get(facilityId);
		if (entry == null) return;
		entry.downloading = true;
		log.debug("recordDownloadStart: facilityId=" + facilityId);
	}

	/**
	 * 指定されたノードが、ライブラリファイルのダウンロードを終了したことを記録します。
	 * 
	 * @param facilityId ノードのファシリティID。
	 */
	public void recordDownloadEnd(String facilityId) {
		Entry entry = map.get(facilityId);
		if (entry == null) return;
		entry.downloading = false;
		entry.lastDownloadedTime = HinemosTime.currentTimeMillis();
		log.debug("recordDownloadEnd: facilityId=" + facilityId);
	}

	/**
	 * リストの内容をレポートします。
	 * 
	 * @return 整形されたレポート文字列。
	 */
	public String report() {
		StringBuilder rpt = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		for (java.util.Map.Entry<String, Entry> it : map.entrySet()) {
			String facilityId = it.getKey();
			Entry entry = it.getValue();
			rpt.append(facilityId);
			rpt.append(" : ");
			rpt.append(sdf.format(new Date(entry.acquiredTime)));
			rpt.append(", ");
			rpt.append(sdf.format(new Date(entry.libMapAccessTime)));
			rpt.append(", ");
			rpt.append(sdf.format(new Date(entry.lastDownloadedTime)));
			rpt.append(", ");
			rpt.append(entry.downloading ? "yes" : "no");
			rpt.append("\n");
		}
		return rpt.toString();
	}

	/**
	 * 更新中ノードの件数を返します。
	 */
	public int getCount() {
		return map.size();
	}
}
