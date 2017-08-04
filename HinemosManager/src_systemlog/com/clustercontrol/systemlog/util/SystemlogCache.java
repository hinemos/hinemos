package com.clustercontrol.systemlog.util;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.util.HinemosTime;

/**
 * システムログ監視のキャッシュ
 *
 * 
 * @version 4.0.2
 * @since 4.0.2
 */
public class SystemlogCache {
	private static Log m_log = LogFactory.getLog( SystemlogCache.class );

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(SystemlogCache.class.getName());
		
		try {
			_lock.writeLock();
			
			ArrayList<MonitorInfo> cache = getCache();
			if (cache == null) {	// not null when clustered
				refresh();
			}
		} finally {
			_lock.writeUnlock();
			m_log.info("Static Initialization [Thread : " + Thread.currentThread() + ", User : " + (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID) + "]");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static ArrayList<MonitorInfo> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_SYSTEMLOG);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_SYSTEMLOG + " : " + cache);
		return cache == null ? null : (ArrayList<MonitorInfo>)cache;
	}
	
	private static void storeCache(ArrayList<MonitorInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_SYSTEMLOG + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_SYSTEMLOG, newCache);
	}
	
	public static void refresh() {
		try {
			_lock.writeLock();
			
			ArrayList<MonitorInfo> systemLogMonitorCache = new ArrayList<MonitorInfo>();
			
			long startTime = HinemosTime.currentTimeMillis();
			try {
				new JpaTransactionManager().getEntityManager().clear();
				systemLogMonitorCache = new SelectMonitor().getMonitorListObjectPrivilegeModeNONE(HinemosModuleConstant.MONITOR_SYSTEMLOG);
			} catch (Exception e) {
				m_log.error("getSystemlogList " + e.getMessage(), e);
			}
			m_log.info("refresh SystemlogCache " + (HinemosTime.currentTimeMillis() - startTime) +
					"ms. size=" + systemLogMonitorCache.size());
			
			storeCache(systemLogMonitorCache);
		} finally {
			_lock.writeUnlock();
		}
	}
	
	/**
	 * システムログ監視一覧リストを返します。
	 * 
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public static ArrayList<MonitorInfo> getSystemlogList() throws MonitorNotFound, InvalidRole, HinemosUnknown{
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		ArrayList<MonitorInfo> cache = getCache();
		return cache;
	}

}
