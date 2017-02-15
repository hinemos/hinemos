package com.clustercontrol.process.factory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.process.entity.MonitorProcessPollingMstData;
import com.clustercontrol.process.entity.MonitorProcessPollingMstPK;
import com.clustercontrol.process.model.MonitorProcessPollingMstEntity;
import com.clustercontrol.process.util.QueryUtil;

/**
 * プロセス監視のキャッシュ
 *
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
public class ProcessMasterCache {
	private static Log m_log = LogFactory.getLog( ProcessMasterCache.class );

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(ProcessMasterCache.class.getName());
		
		try {
			_lock.writeLock();
			
			HashMap<MonitorProcessPollingMstPK, MonitorProcessPollingMstData> cache = getCache();
			if (cache == null) {	// not null when clustered
				refresh();
			}
		} finally {
			_lock.writeUnlock();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<MonitorProcessPollingMstPK, MonitorProcessPollingMstData> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_PROCESS_POLLING);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_PROCESS_POLLING + " : " + cache);
		return cache == null ? null : (HashMap<MonitorProcessPollingMstPK, MonitorProcessPollingMstData>)cache;
	}
	
	private static void storeCache(HashMap<MonitorProcessPollingMstPK, MonitorProcessPollingMstData> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_PROCESS_POLLING + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_PROCESS_POLLING, newCache);
	}

	public static void refresh() {
		JpaTransactionManager jtm = new JpaTransactionManager();
		if (! jtm.isNestedEm()) {
			m_log.warn("refresh() : transactioin has not been begined.");
			jtm.close();
			return;
		}
		
		try {
			_lock.writeLock();
			
			HashMap<MonitorProcessPollingMstPK, MonitorProcessPollingMstData> cache 
				= new HashMap<MonitorProcessPollingMstPK, MonitorProcessPollingMstData>();
			
			List<MonitorProcessPollingMstEntity> c = QueryUtil.getAllMonitorProcessPollingMst();

			for (MonitorProcessPollingMstEntity entity : c) {
				MonitorProcessPollingMstData data =
						new MonitorProcessPollingMstData(
								entity.getId().getCollectMethod(),
								entity.getId().getPlatformId(),
								entity.getId().getSubPlatformId(),
								entity.getId().getVariableId(),
								entity.getEntryKey(),
								entity.getPollingTarget());
				MonitorProcessPollingMstPK dataPk = new MonitorProcessPollingMstPK(
						entity.getId().getCollectMethod(),
						entity.getId().getPlatformId(),
						entity.getId().getSubPlatformId(),
						entity.getId().getVariableId());
				cache.put(dataPk, data);
			}
			
			storeCache(cache);
		} catch (Exception e) {
			m_log.warn("ProcessMasterCache() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.writeUnlock();
		}
	}
	
	public static MonitorProcessPollingMstData getMonitorProcessPollingMst (MonitorProcessPollingMstPK pk) {
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		HashMap<MonitorProcessPollingMstPK, MonitorProcessPollingMstData> cache = getCache();
		return cache.get(pk);
	}
}
