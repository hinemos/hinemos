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
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.model.MonitorStatusEntity;
import com.clustercontrol.notify.model.MonitorStatusEntityPK;
import com.clustercontrol.util.HinemosTime;

/**
 * MonitorStatusEntityをキャッシュするクラス
 */
public class MonitorStatusCache {
	private static final Log log = LogFactory.getLog(MonitorStatusCache.class);

	private static final List<String> excludePluginIds = new ArrayList<>();
	
	static {
		// キャッシュ対象外
		for (String str : HinemosPropertyCommon.monitor_status_cache_exclude_pluginid.getStringValue().split("[\\s]*,[\\s]*")) {
			if (!str.isEmpty()) {
				excludePluginIds.add(str);
			}
		}

		Set<MonitorStatusEntityPK> keySet = cacheKeys();
		
		if (keySet == null || keySet.size() == 0) {
			init();
		}
	}
	
	private static ILock getLock(MonitorStatusEntityPK pk) {
		ILockManager lockManager = LockManagerFactory.instance().create();
		ILock lock = lockManager.create(getLockKey(pk));
		return lock;
	}
	
	private static boolean removeLock(MonitorStatusEntityPK pk) {
		ILockManager lockManager = LockManagerFactory.instance().create();
		return lockManager.delete(getLockKey(pk));
	}
	
	private static String getLockKey(MonitorStatusEntityPK pk) {
		return String.format("%s [%s, %s, %s, %s]", MonitorStatusCache.class.getName(), 
				pk.getFacilityId(), pk.getPluginId(), pk.getMonitorId(), pk.getSubKey());
	}
	
	private static MonitorStatusEntity getCache(MonitorStatusEntityPK pk) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(pk);
		if (log.isDebugEnabled()) log.debug("get cache " + AbstractCacheManager.KEY_NOTIFY_MONITOR_STATUS + " : " + cache);
		return cache == null ? null : (MonitorStatusEntity)cache;
	}
	
	private static void storeCache(MonitorStatusEntity newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (log.isDebugEnabled()) log.debug("store cache " + AbstractCacheManager.KEY_NOTIFY_MONITOR_STATUS + " : " + newCache);
		cm.store(newCache.getId(), newCache);
	}
	
	public static void removeCache(MonitorStatusEntityPK pk) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (log.isDebugEnabled()) log.debug("remove cache " + AbstractCacheManager.KEY_NOTIFY_MONITOR_STATUS + " : " + pk);
		cm.remove(pk);
	}
	
	private static Set<MonitorStatusEntityPK> cacheKeys() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Set<MonitorStatusEntityPK> cacheKeys = cm.getKeySet(MonitorStatusEntityPK.class);
		return cacheKeys != null ? Collections.unmodifiableSet(cacheKeys): null;
	}

	private static void init() {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.getEntityManager().clear();
			// 対象外を除いてキャッシュに保持する。
			List<MonitorStatusEntity> entities = QueryUtil.getMonitorStatusWithoutPluginIds(excludePluginIds);
			long start = System.currentTimeMillis();
			for (MonitorStatusEntity entity : entities) {
				ILock lock = getLock(entity.getId());
				try {
					lock.writeLock();
					
					storeCache(entity);
				} finally {
					lock.writeUnlock();
				}
			}
			log.info("init MonitorStatusCache " + (System.currentTimeMillis() - start) + "ms. size=" + cacheKeys().size());
		} finally {
			if(jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * キャッシュ情報削除（ジョブ履歴情報削除で使用）
	 * 
	 * @param pluginId プラグインID
	 * @param monitorMap IDのマップ（Key、Valueで同じ値）
	 */
	public static void remove(String pluginId, Set<String> monitorIdSet) {
		if (!excludePluginIds.contains(pluginId)) {
			Set<MonitorStatusEntityPK> keySet = cacheKeys();
			for (MonitorStatusEntityPK key : keySet) {
				if (key.getPluginId().equals(pluginId) && monitorIdSet.contains(key.getMonitorId())) {
					if (log.isTraceEnabled()) {
						log.trace("remove() : " + key.toString());
					}
					ILock lock = getLock(key);
					try {
						lock.writeLock();
						removeCache(key);
					} finally {
						lock.writeUnlock();
						removeLock(key);
					}
				}
			}
		} else {
			JpaTransactionManager jtm = new JpaTransactionManager();
			try {
				jtm.begin();
				for (String monitorId : monitorIdSet) {
					QueryUtil.deleteMonitorStatusByPluginIdAndMonitorId(pluginId, monitorId);
				}
				jtm.commit();
			} catch (Exception e) {
				log.warn("remove() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				jtm.rollback();
			} finally {
				jtm.close();
			}
		}
	}

	/**
	 * キャッシュ情報削除
	 * 
	 * @param pluginId プラグインID
	 * @param monitorId ID
	 */
	public static void remove(String pluginId, String monitorId) {
		log.debug("remove() : start. pluginId=" + pluginId + ", monitorId=" + monitorId);

		if (!excludePluginIds.contains(pluginId)) {
			Set<MonitorStatusEntityPK> keySet = cacheKeys();
			for (MonitorStatusEntityPK key : keySet) {
				if (key.getPluginId().equals(pluginId) && key.getMonitorId().equals(monitorId)) {
					if (log.isTraceEnabled()) {
						log.trace("remove() : " + key.toString());
					}
					ILock lock = getLock(key);
					try {
						lock.writeLock();
						removeCache(key);
					} finally {
						lock.writeUnlock();
						removeLock(key);
					}
				}
			}
		} else {
			JpaTransactionManager jtm = new JpaTransactionManager();
			try {
				jtm.begin();
				QueryUtil.deleteMonitorStatusByPluginIdAndMonitorId(pluginId, monitorId);
				jtm.commit();
			} catch (Exception e) {
				log.warn("remove() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				jtm.rollback();
			} finally {
				jtm.close();
			}
		}
	}

	/**
	 * キャッシュ情報削除
	 * 
	 * @param facilityId ファシリティID
	 */
	public static void remove(String facilityId){
		if (log.isDebugEnabled()) {
			log.debug("remove() : start. facilityId=" + facilityId);
		}
		Set<MonitorStatusEntityPK> keySet = cacheKeys();
		for (MonitorStatusEntityPK key : keySet) {
			if (key.getFacilityId().equals(facilityId)) {
				if (log.isTraceEnabled()) {
					log.trace("remove() : " + key.toString());
				}
				ILock lock = getLock(key);
				try {
					lock.writeLock();
					removeCache(key);
				} finally {
					lock.writeUnlock();
					removeLock(key);
				}
			}
		}
	}

	/**
	 * キャッシュ情報全件削除
	 */
	public static void removeAll() {
		log.debug("removeAll() : start.");
		
		Set<MonitorStatusEntityPK> keySet = cacheKeys();
		for (MonitorStatusEntityPK key : keySet) {
			if (log.isTraceEnabled()) {
				log.trace("removeAll() : " + key.toString());
			}
			ILock lock = getLock(key);

			try {
				lock.writeLock();
				removeCache(key);
			} finally {
				lock.writeUnlock();
				removeLock(key);
			}
		}
	}

	// persist が多重で動作しないように修正
	private static boolean persistSkip = false;
	
	/**
	 * キャッシュ情報をDBへ反映
	 */
	@SuppressWarnings("deprecation")
	public static void persist() {
		if (persistSkip) {
			log.warn("persist skip");
			return;
		}
		try {
			persistSkip = true;
			long start = HinemosTime.currentTimeMillis();
			List<MonitorStatusEntity> entityList = new ArrayList<MonitorStatusEntity>();
			Set<MonitorStatusEntityPK> keySet = cacheKeys();
			for (MonitorStatusEntityPK key : keySet) {
				ILock lock = getLock(key);
				try {
					lock.readLock();
					
					MonitorStatusEntity cache = getCache(key);
					if (cache != null && !excludePluginIds.contains(cache.getId().getPluginId())) {
						/* MonitorStatusEntityを直接persistすると、eclipselinkのクラスにおいて、MonitorStatusEntity同士の参照が生まれ、
						 * GCで回収されないため、deep copyをpersistする
						 */
						entityList.add(cache.clone());
					}
				} finally {
					lock.readUnlock();
				}
			}
			JpaTransactionManager jtm = new JpaTransactionManager();
			try {
				HinemosEntityManager em = jtm.getEntityManager();
				jtm.begin();
	
				//削除
				QueryUtil.deleteMonitorStatusWithoutPluginIds(excludePluginIds);
				
				em.flush();
	
				//作成
				for (MonitorStatusEntity entity : entityList) {
					/* MonitorStatusEntityを直接persistすると、eclipselinkのクラスにおいて、MonitorStatusEntity同士の参照が生まれ、
					 * GCで回収されないため、deep copyをpersistする
					 */
					em.persist(entity);
				}
				jtm.commit();
			} catch (Exception e) {
				log.error(e);
				jtm.rollback();
			} finally {
				jtm.close();
			}
			log.info(String.format("persist: %dms, size=%d", HinemosTime.currentTimeMillis() - start, entityList.size()));
		} finally {
			persistSkip = false;
		}
	}

	/**
	 * 重要度カウントの結果を返す
	 * 
	 * @param pk キー情報
	 * @return 重要度カウンタ
	 * @throws NotifyNotFound
	 */
	public static Long getCounter(MonitorStatusEntityPK pk) throws NotifyNotFound {
		Long count = null;

		if (!excludePluginIds.contains(pk.getPluginId())) {
			// キャッシュ保持対象
			ILock lock = getLock(pk);
			try {
				lock.readLock();
				MonitorStatusEntity monitorStatus = getCache(pk);
				if (monitorStatus == null) {
					// 履歴情報が存在しない
					throw new NotifyNotFound("getCounter() MonitorStatusEntity is not found. pk=" + pk);
				}
				count = monitorStatus.getCounter();
				if (count == null) {
					log.info("getCounter() counter is null. pk=" + pk);
				}
				return count;
			} catch (NotifyNotFound e) {
				//findbugs対応 ここくる場合は lockの nullはあり得ないのでnullチェックを除去
				lock.readUnlock();
				lock = null;
				removeLock(pk);
				throw e;
			} finally {
				if (lock != null) {
					lock.readUnlock();
				}
			}
		} else {
			// キャッシュ保持対象外
			// DBから取得する
			MonitorStatusEntity entity = QueryUtil.getMonitorStatusPK(pk, ObjectPrivilegeMode.NONE);
			return entity.getCounter();
		}
	}

	/**
	 * 直前の監視結果と現在の監視結果の重要度を比較し、変更がある場合は、DBで保持している同一重要度カウンタをリセット。
	 * 戻り値として、trueを返す。
	 * 重要度変化がない場合は、同一重要度カウンタをカウントアップし、DB情報を更新。
	 * 戻り値として、falseを返す。
	 *
	 * @param output 監視結果
	 * @return 重要度変化の有無（有:true / 無:false）
	 */
	public static boolean update(OutputBasicInfo output) {
		boolean rtnFlag = false;

		if(output.getSubKey() == null){
			log.info("SubKey is null. PluginId = " + output.getPluginId() +
					", MonitorId = " + output.getMonitorId() +
					", FacilityId = " + output.getFacilityId());
			output.setSubKey("");
		}

		log.debug("update() facilityId = " + output.getFacilityId() +
				", pluginId = " + output.getPluginId() +
				", monitorId = " + output.getMonitorId() +
				", subkey = " + output.getSubKey() +
				", generateDate = " + output.getGenerationDate() +
				", currentPriority = " + output.getPriority());

		MonitorStatusEntityPK pk = new MonitorStatusEntityPK(
				output.getFacilityId(), output.getPluginId(), output.getMonitorId(), output.getSubKey());

		if (!excludePluginIds.contains(output.getPluginId())) {
			// キャッシュ保持対象
			ILock lock = getLock(pk);
			try {
				lock.writeLock();
				MonitorStatusEntity newEntity = createIncrementalData(getCache(pk), output);
				if (newEntity != null) {
					if (newEntity.getCounter() == 1L) {
						rtnFlag = true;
					} else {
						rtnFlag = false;
					}
					// キャッシュに登録
					storeCache(newEntity);
				} else {
					int maxInitialCount = HinemosPropertyCommon.notify_initial_count_max.getIntegerValue();
					if (maxInitialCount == 0) {
						rtnFlag = true;
					} else {
						rtnFlag = false;
					}
				}
			} finally {
				lock.writeUnlock();
			}
		} else {
			// キャッシュ保持対象外
			try (JpaTransactionManager jtm = new JpaTransactionManager()) {
				HinemosEntityManager em = jtm.getEntityManager();
				MonitorStatusEntity entity = null;
				boolean addFlag = false;
				try {
					// DBから取得する
					entity = QueryUtil.getMonitorStatusPK(pk, ObjectPrivilegeMode.NONE);
				} catch (NotifyNotFound e) {
					// 何もしない
					addFlag = true;
				}
				MonitorStatusEntity newEntity = createIncrementalData(entity, output);
				if (newEntity != null) {
					if (newEntity.getCounter() == 1L) {
						rtnFlag = true;
					} else {
						rtnFlag = false;
					}
					// DBに登録(更新はentityを直接修正しているため不要)
					if (addFlag) {
						em.persist(newEntity);
						em.flush();
					}
				} else {
					int maxInitialCount = HinemosPropertyCommon.notify_initial_count_max.getIntegerValue();
					if (maxInitialCount == 0) {
						rtnFlag = true;
					} else {
						rtnFlag = false;
					}
				}
			}
		}
		return rtnFlag;
	}

	private static MonitorStatusEntity createIncrementalData(MonitorStatusEntity entity, OutputBasicInfo output) {

		MonitorStatusEntityPK pk = new MonitorStatusEntityPK(
				output.getFacilityId(), output.getPluginId(), output.getMonitorId(), output.getSubKey());

		if (entity == null) {
			log.debug("create new entity. " + pk);

			// 同一重要度カウンタは1で生成
			// インスタンス生成
			MonitorStatusEntity newEntity = new MonitorStatusEntity(pk);
			newEntity.setPriority(output.getPriority());
			newEntity.setLastUpdate(output.getGenerationDate());
			newEntity.setCounter(1l);
			return newEntity;
		} else if (output.getPriority() != entity.getPriority()) {
			// 重要度が変化している
			if(log.isDebugEnabled()){
				log.debug("prioityChangeFlag = true. " + pk + " ," +
						entity.getPriority() + " to " +
						output.getPriority());
			}
			// 重要度を更新
			// 同一重要度カウンタを1にリセット
			entity.setPriority(output.getPriority());
			entity.setCounter(1l);
			return entity;
		} else {
			// 同一重要度カウンタの最大値を越えた場合は、以降は更新しない（DBへのupdateを減らすための方策）
			if(entity.getCounter() <= HinemosPropertyCommon.notify_initial_count_max.getIntegerValue()){
				entity.setCounter(entity.getCounter() + 1);
				entity.setLastUpdate(output.getGenerationDate());
				return entity;
			} else {
				return null;
			}
		}
	}
	
	public static boolean isCache(String pluginId) {
		if (!excludePluginIds.contains(pluginId)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * ファシリティID、プラグインID、モニターIDから、現在キャッシュに登録されている監視ステータス情報を返します。
	 * 
	 * @param facilityId ファシリティID
	 * @param pluginId プラグインID
	 * @param monitorId モニターID
	 * @return 監視ステータス情報リスト
	 */
	public static List<MonitorStatusEntity> getCacheByFacilityIdAndPluginIdAndMonitorId(String facilityId, String pluginId, String monitorId) {

		List<MonitorStatusEntity> monitorStatusList = new ArrayList<>();

		Set<MonitorStatusEntityPK> keySet = cacheKeys();
		for (MonitorStatusEntityPK key : keySet) {
			if (key.getFacilityId().equals(facilityId)
					&& key.getPluginId().equals(pluginId)
					&& key.getMonitorId().equals(monitorId)) {
				ILock lock = getLock(key);

				try {
					lock.readLock();
					
					MonitorStatusEntity cache = getCache(key);
					if (cache != null) {
						monitorStatusList.add(cache);
					}
				} finally {
					lock.readUnlock();
				}
			}
		}

		return monitorStatusList;
	}
}