/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.util.HinemosTime;

/**
 * 通知グループIDと通知IDとの関連をマップで管理するクラス。
 * ジョブセッションで利用されている通知グループはキャッシュされない。
 */
public class NotifyRelationCache {
	private static Log m_log = LogFactory.getLog( NotifyRelationCache.class );

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(NotifyRelationCache.class.getName());
		
		try {
			_lock.writeLock();
			
			HashMap<String, List<NotifyRelationInfo>> cache = getCache();
			if (cache == null) {	// not null when clustered
				refresh();
			}
		} finally {
			_lock.writeUnlock();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, List<NotifyRelationInfo>> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_NOTIFY_RELATION);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_NOTIFY_RELATION + " : " + cache);
		return cache == null ? null : (HashMap<String, List<NotifyRelationInfo>>)cache;
	}
	
	private static void storeCache(HashMap<String, List<NotifyRelationInfo>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_NOTIFY_RELATION + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_NOTIFY_RELATION, newCache);
	}

	/**
	 * 通知グループIDが関連を持つ通知IDのリストを返す。
	 * 
	 * @param notifyGroupId 通知グループID
	 * @return 通知IDのリスト。エラー時は空のリストを返す。
	 */
	public static List<String> getNotifyIdList(String notifyGroupId){
		List<String> list = new ArrayList<String>();
		for (NotifyRelationInfo info : getNotifyList(notifyGroupId)) {
			list.add(info.getNotifyId());
		}
		Collections.sort(list);
		return list;
	}

	/**
	 * 通知グループIDが関連を持つ通知情報のリストを返す。
	 * 
	 * @param notifyGroupId 通知グループID
	 * @return 通知情報のリスト。エラー時は空のリストを返す。
	 */
	public static List<NotifyRelationInfo> getNotifyList(String notifyGroupId){
		try {
			{
				HashMap<String, List<NotifyRelationInfo>> cache = getCache();
				
				// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
				// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
				List<NotifyRelationInfo> notifyList = cache.get(notifyGroupId);
				if(notifyList != null){
					return notifyList;
				}
				
				// 通知が設定されていない監視の場合は、空のリストを返す。
				if(onCache(notifyGroupId)) {
					return new ArrayList<NotifyRelationInfo>();
				}
			}
			
			m_log.debug("getNotifyIdList() : Job Master or Job Session. " + notifyGroupId);
			List<NotifyRelationInfo> nriList
			= QueryUtil.getNotifyRelationInfoByNotifyGroupId(notifyGroupId);
			return nriList;
		} catch (Exception e) {
			m_log.warn("getNotifyList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			return new ArrayList<NotifyRelationInfo>(); // 空のリストを返す
		}
	}

	/**
	 * キャッシュに乗せる場合はtrueが返る。
	 * @param notifyGroupId
	 * @return
	 */
	private static boolean onCache(String notifyGroupId) {
		return (notifyGroupId.startsWith(HinemosModuleConstant.JOB_SESSION + "-") == false &&
				notifyGroupId.startsWith(HinemosModuleConstant.JOB_MST + "-") == false);
	}

	public static void refresh(){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			if (!jtm.isNestedEm()) {
				m_log.warn("refresh() : transactioin has not been begined.");
				return;
			}

			try {
				_lock.writeLock();
				
				long start = HinemosTime.currentTimeMillis();
				em.clear();
				HashMap<String, List<NotifyRelationInfo>> notifyMap = new HashMap<String, List<NotifyRelationInfo>>();
				List<NotifyRelationInfo> nriList = null;
				try {
					nriList = QueryUtil.getAllNotifyRelationInfoWithoutJob();
				} catch (Exception e) {
					m_log.warn("refresh() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					return;
				}
				for (NotifyRelationInfo nri : nriList) {
					String notifyGroupId = nri.getId().getNotifyGroupId();
					// ジョブセッションで利用されている通知グループの場合は、キャッシュしない。
					if(onCache(notifyGroupId)){
						List<NotifyRelationInfo> notifyList = notifyMap.get(notifyGroupId);
						if (notifyList == null) {
							notifyList = new ArrayList<NotifyRelationInfo>();
							notifyList.add(nri);
							notifyMap.put(notifyGroupId, notifyList);
						} else {
							notifyList.add(nri);
						}
					}
				}
				for (List<NotifyRelationInfo> notifyList : notifyMap.values()) {
					if (notifyList == null) {
						continue;
					}
					Collections.sort(notifyList);
				}
				storeCache(notifyMap);
				m_log.info("refresh NotifyRelationCache. " + (HinemosTime.currentTimeMillis() - start)
						+ "ms. size=" + notifyMap.size());
			} finally {
				_lock.writeUnlock();
			}
		}
	}
}
