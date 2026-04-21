/*
 * Copyright (c) 2025 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.jobmanagement.bean.JobKick;
import com.clustercontrol.jobmanagement.factory.SelectJobKick;

/**
 * 実行契機用キャッシュクラス<BR>
 * <p>現時点ではファイルチェック実行契機の情報のみを保持している。<BR>
 */
public class JobKickCache {

	/**
	 * ログ出力のインスタンス
	 */
	private static Log m_log = LogFactory.getLog(JobKickCache.class);

	/**
	 * 実行契機用キャッシュ。
	 * 初回アクセス時にrefresh()によりインスタンスが生成される。
	 * キー：実行契機ID
	 * 値  ：実行契機Bean
	 */
	private volatile static ConcurrentMap<String, JobKick> cache = null;

	/**
	 * ロック
	 */
	private static final ILock _lock;

	/**
	 * 読込みロック数カウンタ（デバッグ用）
	 */
	private static int readLockCounter = 0;

	/**
	 * 書込みロック数カウンタ（デバッグ用）
	 */
	private static int writeLockCounter = 0;


	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(JobKickCache.class.getName());
	}

	/**
	 * コンストラクタ
	 * インスタンス化させないため、private。
	 */
	private JobKickCache() {
	}

	/**
	 * 読込みロック
	 * キャッシュの構造上これは呼び出さない。
	 */
	public static void readLock() {
		readLockCounter++;
		if (m_log.isTraceEnabled()) {
			m_log.trace("readLock() readLockCounter=" + readLockCounter + ", writeLockCounter=" + writeLockCounter);
		}
		_lock.readLock();
	}

	/**
	 * 読込みアンロック
	 * キャッシュの構造上これは呼び出さない。
	 */
	public static void readUnlock() {
		readLockCounter--;
		if (m_log.isTraceEnabled()) {
			m_log.trace("readUnlock() readLockCounter=" + readLockCounter + ", writeLockCounter=" + writeLockCounter);
		}
		_lock.readUnlock();
	}

	/**
	 * 書込みロック
	 */
	public static void writeLock() {
		writeLockCounter++;
		if (m_log.isTraceEnabled()) {
			m_log.trace("writeLock() readLockCounter=" + readLockCounter + ", writeLockCounter=" + writeLockCounter);
		}
		_lock.writeLock();
	}

	/**
	 * 書込みアンロック
	 */
	public static void writeUnlock() {
		writeLockCounter--;
		if (m_log.isTraceEnabled()) {
			m_log.trace("writeUnlock() readLockCounter=" + readLockCounter + ", writeLockCounter=" + writeLockCounter);
		}
		_lock.writeUnlock();
	}

	/**
	 * 実行契機のリストを返す。
	 * 順序はID順。
	 * 呼び出し元ではwriteLockを取得すること
	 * 
	 * @return 実行契機のリスト、キャッシュが空の場合は空のリスト
	 */
	public static List<JobKick> getAsList() {
		m_log.info("getAsList() start.");

		refresh();


		List<JobKick> list = new ArrayList<>(cache.values());
		// ID順でソート
		list.sort((a, b) -> a.getId().compareTo(b.getId()));

		if (m_log.isTraceEnabled()) {
			m_log.trace("getAsList() end. list=" + list);
		}
		return list;
	}

	/**
	 * 該当の実行契機を追加、または更新する。
	 * 呼び出し元ではwriteLockを取得すること
	 * 
	 * @param jobKick 追加、または更新する実行契機
	 * @return 更新時は更新前の実行契機。追加の場合、引数がnullの場合はnull。
	 */
	public static JobKick put(JobKick jobKick) {
		m_log.info("put() start.");
		if (m_log.isDebugEnabled()) {
			m_log.debug("put() start. jobKick=" + jobKick);
		}
		if (jobKick == null) {
			return null;
		}

		refresh();

		JobKick oldJobKick = null;
		oldJobKick = cache.put(jobKick.getId(), jobKick);

		if (m_log.isTraceEnabled()) {
			m_log.trace("put() keys=" + String.join(",", cache.keySet()));
			m_log.trace("put() put new jobKick=" + jobKick + ", old jobKick=" + oldJobKick);
		}
		return oldJobKick;
	}

	/**
	 * 引数のIDのリストの実行契機を削除する。
	 * 呼び出し元ではwriteLockを取得すること
	 * 
	 * @param idList 削除する実行契機のIDのリスト
	 */
	public static void removeByList(List<String> idList) {
		m_log.info("removeByList() start. idList=" + idList);

		if (idList == null) {
			return;
		}

		refresh();
		for (String id : idList) {
			if (id == null || id.equals("")) {
				continue;
			}
			JobKick oldJobKick = cache.remove(id);
	
			if (m_log.isTraceEnabled()) {
				m_log.trace("removeByList() keys=" + String.join(",", cache.keySet()));
				m_log.trace("removeByList() removed jobKick=" + oldJobKick);
			}
		}
	}

	/**
	 * DBからジョブ実行契機のリストを取得し、実行契機キャッシュを更新する。<BR>
	 * <p>refresh()メソッドはJobKickCache内でのみ呼び出すようにしている。<BR>
	 */
	private static void refresh() {
		if (cache != null) {
			return;
		}
		m_log.info("refresh()");

		List<JobKick> list = null;
		SelectJobKick select = new SelectJobKick();
		try {
			list = select.getJobKickListForJobKickCache();

			ConcurrentMap<String, JobKick> newCache = new ConcurrentHashMap<>();
			list.forEach((v) -> {
				newCache.put(v.getId(), v);
			});
			cache = newCache;
		} catch (JobMasterNotFound | HinemosUnknown | InvalidRole e) {
			m_log.warn("refresh() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}

		if (m_log.isTraceEnabled()) {
			m_log.trace("refresh() end. list=" + list);
		}
	}
}
