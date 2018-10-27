/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.performance.factory.OperateCollectCategoryCollectMaster;
import com.clustercontrol.performance.factory.OperateCollectItemCalcMethodMaster;
import com.clustercontrol.performance.factory.OperateCollectItemCodeMaster;
import com.clustercontrol.performance.factory.OperateCollectPollingMaster;
import com.clustercontrol.performance.monitor.entity.CollectorCategoryCollectMstData;
import com.clustercontrol.performance.monitor.entity.CollectorCategoryCollectMstPK;
import com.clustercontrol.performance.monitor.entity.CollectorItemCalcMethodMstData;
import com.clustercontrol.performance.monitor.entity.CollectorItemCalcMethodMstPK;
import com.clustercontrol.performance.monitor.entity.CollectorItemCodeMstData;
import com.clustercontrol.performance.monitor.entity.CollectorPollingMstData;

/**
 * 性能収集時のマスターデータのキャッシュ
 *
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
public class CollectorMasterCache {
	private static Log m_log = LogFactory.getLog( CollectorMasterCache.class );

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(CollectorMasterCache.class.getName());
		
		try {
			_lock.writeLock();
			
			HashMap<CollectorCategoryCollectMstPK, CollectorCategoryCollectMstData> categoryCollectCache = getCategoryCollectCache();
			HashMap<String, CollectorItemCodeMstData> itemCodeCache = getItemCodeCache();
			HashMap<CollectorItemCalcMethodMstPK, CollectorItemCalcMethodMstData> itemCalcMethodCache = getItemCalcMethodCache();
			HashMap<CollectorItemCalcMethodMstPK, ArrayList<String>> pollingTargetCache = getPollingTarget();
			
			if (categoryCollectCache == null || itemCodeCache == null || itemCalcMethodCache == null || pollingTargetCache == null) {	// not null when clustered
				refresh();
			}
		} finally {
			_lock.writeUnlock();
		}
	}
	
	// ポーリングデータのキャッシュ
	@SuppressWarnings("unchecked")
	private static HashMap<CollectorCategoryCollectMstPK, CollectorCategoryCollectMstData> getCategoryCollectCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_PERFORMANCE_CATEGORY_COLLECT);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_PERFORMANCE_CATEGORY_COLLECT + " : " + cache);
		return cache == null ? null : (HashMap<CollectorCategoryCollectMstPK, CollectorCategoryCollectMstData>)cache;
	}
	
	private static void storeCategoryCollectCache(HashMap<CollectorCategoryCollectMstPK, CollectorCategoryCollectMstData> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_PERFORMANCE_CATEGORY_COLLECT + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_PERFORMANCE_CATEGORY_COLLECT, newCache);
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, CollectorItemCodeMstData> getItemCodeCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_PERFORMANCE_ITEM_CODE);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_PERFORMANCE_ITEM_CODE + " : " + cache);
		return cache == null ? null : (HashMap<String, CollectorItemCodeMstData>)cache;
	}
	
	private static void storeItemCodeCache(HashMap<String, CollectorItemCodeMstData> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_PERFORMANCE_ITEM_CODE + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_PERFORMANCE_ITEM_CODE, newCache);
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<CollectorItemCalcMethodMstPK, CollectorItemCalcMethodMstData> getItemCalcMethodCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_PERFORMANCE_ITEM_CALC_METHOD);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_PERFORMANCE_ITEM_CALC_METHOD + " : " + cache);
		return cache == null ? null : (HashMap<CollectorItemCalcMethodMstPK, CollectorItemCalcMethodMstData>)cache;
	}
	
	private static void storeItemCalcMethodCache(HashMap<CollectorItemCalcMethodMstPK, CollectorItemCalcMethodMstData> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_PERFORMANCE_ITEM_CALC_METHOD + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_PERFORMANCE_ITEM_CALC_METHOD, newCache);
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<CollectorItemCalcMethodMstPK, ArrayList<String>> getPollingTarget() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_PERFORMANCE_POLLING_TARGET);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_PERFORMANCE_POLLING_TARGET + " : " + cache);
		return cache == null ? null : (HashMap<CollectorItemCalcMethodMstPK, ArrayList<String>>)cache;
	}
	
	private static void storePollingTarget(HashMap<CollectorItemCalcMethodMstPK, ArrayList<String>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_PERFORMANCE_POLLING_TARGET + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_PERFORMANCE_POLLING_TARGET, newCache);
	}

	/*
	 * 
	 * Utility
	 * 
	 */

	/**
	 * 指定した収集項目IDのマスタ情報を返却する
	 * 
	 * @param itemCode
	 * @return
	 */
	public static CollectorItemCodeMstData getCategoryCodeMst(String itemCode) {
		try {
			_lock.readLock();
			
			HashMap<String, CollectorItemCodeMstData> itemCodeCache = getItemCodeCache();
			return itemCodeCache.get(itemCode);
		} finally {
			_lock.readUnlock();
		}
	}


	public static void refresh() {
		try {
			_lock.writeLock();
			
			createCategoryCollectMstDataMapCache();
			createItemCodeMstDataMapCache();
			createItemCalcMethodMstDataMapCache();
			createPollingTargetMapCache();
		} finally {
			_lock.writeUnlock();
		}
	}

	private static void createCategoryCollectMstDataMapCache() {
		HashMap<CollectorCategoryCollectMstPK, CollectorCategoryCollectMstData> categoryCollectMstDataMapCache 
			= new HashMap<CollectorCategoryCollectMstPK, CollectorCategoryCollectMstData>();

		Collection<CollectorCategoryCollectMstData> collects =
				new OperateCollectCategoryCollectMaster().findAll();
		for(CollectorCategoryCollectMstData data : collects){
			categoryCollectMstDataMapCache.put(data.getPrimaryKey(), data);
		}
		
		storeCategoryCollectCache(categoryCollectMstDataMapCache);
	}

	private static void createItemCodeMstDataMapCache() {
		HashMap<String, CollectorItemCodeMstData> itemCodeMstDataMapCache = new HashMap<String, CollectorItemCodeMstData>();

		Collection<CollectorItemCodeMstData> dataList =
				new OperateCollectItemCodeMaster().findAll();
		for(CollectorItemCodeMstData data : dataList){
			itemCodeMstDataMapCache.put(data.getItemCode(), data);
		}
		
		storeItemCodeCache(itemCodeMstDataMapCache);
	}

	private static void createItemCalcMethodMstDataMapCache() {
		HashMap<CollectorItemCalcMethodMstPK, CollectorItemCalcMethodMstData> itemCalcMethodMstDataMapCache 
			= new HashMap<CollectorItemCalcMethodMstPK, CollectorItemCalcMethodMstData>();
	
		Collection<CollectorItemCalcMethodMstData> dataList =
				new OperateCollectItemCalcMethodMaster().findAll();
		for(CollectorItemCalcMethodMstData data : dataList){
			itemCalcMethodMstDataMapCache.put(data.getPrimaryKey(), data);
		}
		
		storeItemCalcMethodCache(itemCalcMethodMstDataMapCache);
	}

	private static void createPollingTargetMapCache() {
		HashMap<CollectorItemCalcMethodMstPK, ArrayList<String>> pollingTargetMapCache 
			= new HashMap<CollectorItemCalcMethodMstPK, ArrayList<String>>();
	
		Collection<CollectorPollingMstData> pollingMstLocalList =
				new OperateCollectPollingMaster().findAll();
		for(CollectorPollingMstData data : pollingMstLocalList){
			CollectorItemCalcMethodMstPK pk = new CollectorItemCalcMethodMstPK(data.getCollectMethod(), data.getPlatformId(), data.getSubPlatformId(), data.getItemCode());
			ArrayList<String> targetList = pollingTargetMapCache.get(pk);
			if (targetList == null) {
				targetList = new ArrayList<String>();
			}
			targetList.add(data.getPollingTarget());
	
			pollingTargetMapCache.put(pk, targetList);
		}
		
		storePollingTarget(pollingTargetMapCache);
	}

	/**
	 * ポーリング対象の文字列(OIDなど)のリストを取得する
	 * 
	 * @param itemCodeList
	 * @param collectMethod
	 * @param platformId
	 * @param subPlatformId
	 * @return
	 */
	protected static ArrayList<String> getPollingTarget(ArrayList<String> itemCodeList, String collectMethod, String platformId, String subPlatformId) {
		try {
			_lock.readLock();
			
			HashMap<CollectorItemCalcMethodMstPK, ArrayList<String>> pollingTargetCache = getPollingTarget();
			
			ArrayList<String> pollingTargetList = new ArrayList<String>();
			for(String itemCode : itemCodeList) {
				if (m_log.isDebugEnabled()) {
					m_log.debug(String.format("collectMethod = %s, platformId = %s, subPlatformId = %s, itemCode = %s", collectMethod, platformId, subPlatformId, itemCode));
				}
				CollectorItemCalcMethodMstPK pk = new CollectorItemCalcMethodMstPK(collectMethod, platformId, subPlatformId, itemCode);
				ArrayList<String> list = pollingTargetCache.get(pk);
				if (list == null)
					continue;
				
				pollingTargetList.addAll(list);
			}
			
			if (!subPlatformId.isEmpty()) {
				// VM管理やクラウド管理などではsubPlatformIdに「VMWARE」や「AWS」などが存在している収集項目に加え、
				// subPlatformIdが空の場合の（つまり物理と同じ）収集項目が収集できる必要があるため、
				// subPlatformIdが空でない場合にはsubPlatformIdを空にした収集項目も検索する
				subPlatformId = "";
				for(String itemCode : itemCodeList) {
					CollectorItemCalcMethodMstPK pk = new CollectorItemCalcMethodMstPK(collectMethod, platformId, subPlatformId, itemCode);
					ArrayList<String> list = pollingTargetCache.get(pk);
					if (list != null) {
						pollingTargetList.addAll(list);
					}
				}
			}
			
			if (pollingTargetList.isEmpty()) {
				String errorMessag = String.format("getPollingTarget() : polling target not found in cc_collector_polling_mst.  collectMethod = %s, platformId = %s, subPlatformId = %s",
						collectMethod, platformId, subPlatformId
						);
				m_log.error(errorMessag);
				throw new IllegalStateException(errorMessag);
			}
			
			return pollingTargetList;
		} finally {
			_lock.readUnlock();
		}
	}

	/**
	 * 指定した収集項目IDの内訳の収集項目IDのリストを返却する
	 * 
	 * @param itemCode
	 * @param collectMethod
	 * @param platformId
	 * @param subPlatformId
	 * @return
	 */
	protected static ArrayList<String> getBreakdownItemCodeList(String itemCode, String collectMethod, String platformId, String subPlatformId) {
		try {
			_lock.readLock();
			
			HashMap<String, CollectorItemCodeMstData> itemCodeCache = getItemCodeCache();
			HashMap<CollectorItemCalcMethodMstPK, CollectorItemCalcMethodMstData> itemCalcMethodCache = getItemCalcMethodCache();
			
			ArrayList<String> breakdownTmpItemCodeList = new ArrayList<String>();

			for(CollectorItemCodeMstData itemCodeMstData : itemCodeCache.values()){
				if(itemCodeMstData.getParentItemCode() != null &&
						itemCodeMstData.getParentItemCode().equals(itemCode)){
					breakdownTmpItemCodeList.add(itemCodeMstData.getItemCode());
				}
			}
			if(m_log.isDebugEnabled()){
				for(String code : breakdownTmpItemCodeList){
					m_log.debug("getBreakdownItemCodeList() itemCode(tmp) = " + code);
				}
			}

			Set<String> breakdownItemCodeSet = new HashSet<String>();
			while (true) {
				for(String tmpItemCode : breakdownTmpItemCodeList){
					CollectorItemCalcMethodMstData data = itemCalcMethodCache.get(new CollectorItemCalcMethodMstPK(collectMethod, platformId, subPlatformId, tmpItemCode));
					if (data != null) {
						breakdownItemCodeSet.add(tmpItemCode);
					}
				}
				// VM管理やクラウド管理などではsubPlatformIdに「VMWARE」や「AWS」などが存在している収集項目に加え、
				// subPlatformIdが空の場合の（つまり物理と同じ）収集項目が収集できる必要があるため、
				// subPlatformIdが空でない場合にはsubPlatformIdを空にした収集項目も検索する
				if (subPlatformId.isEmpty()) break;
				subPlatformId = "";
			}
			ArrayList<String> breakdownItemCodeList = new ArrayList<String>(breakdownItemCodeSet);
			if(m_log.isDebugEnabled()){
				for(String code : breakdownItemCodeList){
					m_log.debug("getBreakdownItemCodeList() itemCode = " + code);
				}
			}
			return breakdownItemCodeList;
		} finally {
			_lock.readUnlock();
		}
	}

	/**
	 * 指定したカテゴリの収集方法(SNMP/WBEMなど)を取得する
	 * 
	 * @param platformId
	 * @param subPlatformId
	 * @param categoryCode
	 * @return
	 */
	public static String getCollectMethod(String platformId, String subPlatformId, String categoryCode) {
		try {
			_lock.readLock();
			
			HashMap<CollectorCategoryCollectMstPK, CollectorCategoryCollectMstData> categoryCollectCache = getCategoryCollectCache();
			
			CollectorCategoryCollectMstData data = categoryCollectCache.get(new CollectorCategoryCollectMstPK(platformId, subPlatformId, categoryCode));
			if (data != null) {
				return data.getCollectMethod();
			}

			// VM管理やクラウド管理などではsubPlatformIdに「VMWARE」や「AWS」などが存在している収集項目に加え、
			// subPlatformIdが空の場合の（つまり物理と同じ）収集項目が収集できる必要があるため、
			// subPlatformIdが空でない状態で収集カテゴリが取れなかった場合、subPlatformIdを空にして
			// 再度収集カテゴリを見つける
			if (subPlatformId.isEmpty()) return "";

			subPlatformId = "";
			data = categoryCollectCache.get(new CollectorCategoryCollectMstPK(platformId, subPlatformId, categoryCode));
			if (data != null) {
				return data.getCollectMethod();
			}

			return "";
		} finally {
			_lock.readUnlock();
		}
	}

	/**
	 * itemCodeに対応するデバイスタイプを返却
	 * 
	 * @param itemCode
	 * @return
	 */
	public static String getDeviceType(String itemCode) {
		try {
			_lock.readLock();
			
			HashMap<String, CollectorItemCodeMstData> itemCodeCache = getItemCodeCache();
			CollectorItemCodeMstData itemCodeMstData = itemCodeCache.get(itemCode);
			if (itemCodeMstData == null) {
				return null;
			}
			return itemCodeMstData.getDeviceType();
		} finally {
			_lock.readUnlock();
		}
	}
}
