/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.monitor.run.bean.MonitorNumericType;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.model.MonitorJudgementInfo;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.monitor.run.model.MonitorTruthValueInfo;

/**
 * 監視の判定情報キャッシュ
 *
 * @version 6.1.0
 */
public class MonitorJudgementInfoCache {

	private static Log m_log = LogFactory.getLog( MonitorJudgementInfoCache.class );

	private static final ILock _lock;

	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(MonitorJudgementInfoCache.class.getName());
		
		try {
			_lock.writeLock();
			
			ConcurrentHashMap<MonitorJudgementKey, TreeMap<Integer, MonitorJudgementInfo>> cache = getCache();
			if (cache == null) {	// not null when clustered
				storeCache(new ConcurrentHashMap<MonitorJudgementKey, TreeMap<Integer, MonitorJudgementInfo>>());
			}
		} finally {
			_lock.writeUnlock();
		}
	}


	/**
	 * キャッシュキー情報
	 */
	public static class MonitorJudgementKey {
		// 監視項目ID
		private String monitorId;
		// 監視種別
		private Integer monitorType;
		// 数値監視の分類（空文字:通常の監視、CHANGE:変化量監視）
		private String monitorNumericType = "";

		public MonitorJudgementKey(String monitorId, Integer monitorType) {
			this.monitorId = monitorId;
			this.monitorType = monitorType;
		}

		public MonitorJudgementKey(
				String monitorId, Integer monitorType, String monitorNumericType) {
			this.monitorId = monitorId;
			this.monitorType = monitorType;
			this.monitorNumericType = monitorNumericType;
		}

		public String getMonitorId() {
			return monitorId;
		}
		public void setMonitorId(String monitorId) {
			this.monitorId = monitorId;
		}

		public Integer getMonitorType() {
			return monitorType;
		}
		public void setMonitorType(Integer monitorType) {
			this.monitorType = monitorType;
		}

		public String getMonitorNumericType() {
			return monitorNumericType;
		}
		public void setMonitorNumericType(String monitorNumericType) {
			this.monitorNumericType = monitorNumericType;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((monitorId == null) ? 0 : monitorId.hashCode());
			result = prime * result + ((monitorNumericType == null) ? 0 : monitorNumericType.hashCode());
			result = prime * result + ((monitorType == null) ? 0 : monitorType.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MonitorJudgementKey other = (MonitorJudgementKey) obj;
			if (monitorId == null) {
				if (other.monitorId != null)
					return false;
			} else if (!monitorId.equals(other.monitorId))
				return false;
			if (monitorNumericType == null) {
				if (other.monitorNumericType != null)
					return false;
			} else if (!monitorNumericType.equals(other.monitorNumericType))
				return false;
			if (monitorType == null) {
				if (other.monitorType != null)
					return false;
			} else if (!monitorType.equals(other.monitorType))
				return false;
			return true;
		}

	}

	@SuppressWarnings("unchecked")
	private static ConcurrentHashMap<MonitorJudgementKey, TreeMap<Integer, MonitorJudgementInfo>> getCache() {
		ConcurrentHashMap<MonitorJudgementKey, TreeMap<Integer, MonitorJudgementInfo>> rtn = null;
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_MONITOR_JUDGEMENT_INFO);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_MONITOR_JUDGEMENT_INFO + " : " + cache);
		if (cache != null) {
			rtn = (ConcurrentHashMap<MonitorJudgementKey, TreeMap<Integer, MonitorJudgementInfo>>)cache;
		}
		return rtn;
	}
	
	private static void storeCache(ConcurrentHashMap<MonitorJudgementKey, TreeMap<Integer, MonitorJudgementInfo>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_MONITOR_JUDGEMENT_INFO + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_MONITOR_JUDGEMENT_INFO, newCache);
	}

	/**
	 * 文字列監視の判定情報を作成する
	 *
	 * @param monitorStringValueList 文字列監視情報
	 * @return 文字列監視の判定情報
	 */
	private static TreeMap<Integer, MonitorJudgementInfo> createStringJudgementInfoList(
			List<MonitorStringValueInfo> monitorStringValueList) {

		TreeMap<Integer, MonitorJudgementInfo> judgementMap = new TreeMap<Integer, MonitorJudgementInfo>();

		// 判定情報作成
		if (monitorStringValueList != null) {
			Iterator<MonitorStringValueInfo> itr = monitorStringValueList.iterator();
			judgementMap = new TreeMap<Integer, MonitorJudgementInfo>();
			int idx = 1;
			while(itr.hasNext()){
				MonitorStringValueInfo entity = itr.next();
				MonitorJudgementInfo monitorJudgementInfo = new MonitorJudgementInfo();
				monitorJudgementInfo.setMonitorId(entity.getId().getMonitorId());
				monitorJudgementInfo.setPriority(entity.getPriority());
				monitorJudgementInfo.setMessage(entity.getMessage());
				monitorJudgementInfo.setCaseSensitivityFlg(entity.getCaseSensitivityFlg());
				monitorJudgementInfo.setDescription(entity.getDescription());
				monitorJudgementInfo.setPattern(entity.getPattern());
				monitorJudgementInfo.setProcessType(entity.getProcessType());
				monitorJudgementInfo.setValidFlg(entity.getValidFlg());
				judgementMap.put(idx, monitorJudgementInfo);
				idx++;
			}
		}
		return judgementMap;
	}

	/**
	 * 真偽値監視の判定情報を作成する
	 *    
	 * @param monitorTruthValueList	 真偽値監視情報
	 * @return 真偽値監視の判定情報
	 */
	private static TreeMap<Integer, MonitorJudgementInfo> createTruthJudgementInfoList(
			List<MonitorTruthValueInfo> monitorTruthValueList) {

		TreeMap<Integer, MonitorJudgementInfo> judgementMap = new TreeMap<Integer, MonitorJudgementInfo>();

		// 判定情報作成
		if (monitorTruthValueList != null) {
			Iterator<MonitorTruthValueInfo> itr = monitorTruthValueList.iterator();
			judgementMap = new TreeMap<Integer, MonitorJudgementInfo>();
			MonitorTruthValueInfo entity = null;
			while(itr.hasNext()){
				entity = itr.next();
				MonitorJudgementInfo monitorJudgementInfo = new MonitorJudgementInfo();
				monitorJudgementInfo.setMonitorId(entity.getId().getMonitorId());
				monitorJudgementInfo.setPriority(entity.getId().getPriority());
				monitorJudgementInfo.setTruthValue(entity.getId().getTruthValue());
				monitorJudgementInfo.setMessage(entity.getMessage());
				judgementMap.put(entity.getId().getTruthValue(), monitorJudgementInfo);
			}
		}
		return judgementMap;
	}

	/**
	 * 数値監視の判定情報を取得する
	 *    
	 * @param monitorNumericValueList	数値かんし情報
	 * @return 数値監視の判定情報
	 */
	private static TreeMap<Integer, MonitorJudgementInfo> createNumericJudgementInfoList(
			List<MonitorNumericValueInfo> monitorNumericValueList) {

		TreeMap<Integer, MonitorJudgementInfo> judgementMap = new TreeMap<Integer, MonitorJudgementInfo>();

		// 判定情報作成
		if (monitorNumericValueList != null) {
			Iterator<MonitorNumericValueInfo> itr = monitorNumericValueList.iterator();
			judgementMap = new TreeMap<Integer, MonitorJudgementInfo>();
			MonitorNumericValueInfo entity = null;
			while(itr.hasNext()){
				entity = itr.next();
				MonitorJudgementInfo monitorJudgementInfo = new MonitorJudgementInfo();
				monitorJudgementInfo.setMonitorId(entity.getId().getMonitorId());
				monitorJudgementInfo.setPriority(entity.getId().getPriority());
				monitorJudgementInfo.setMessage(entity.getMessage());
				monitorJudgementInfo.setThresholdLowerLimit(entity.getThresholdLowerLimit());
				monitorJudgementInfo.setThresholdUpperLimit(entity.getThresholdUpperLimit());
				judgementMap.put(entity.getId().getPriority(), monitorJudgementInfo);
			}
		}
		return judgementMap;
	}

	/**
	 * 文字列監視の判定情報キャッシュの更新を行う。
	 * 
	 * @param monitorId 監視項目ID
	 * @param monitorStringValueList 文字列監視の判定情報
	 * 
	 */
	public static void updateString(String monitorId, List<MonitorStringValueInfo> monitorStringValueList) {

		if (monitorId == null || monitorId.isEmpty()) {
			return;
		}
		try {
			_lock.writeLock();
			ConcurrentHashMap<MonitorJudgementKey, TreeMap<Integer, MonitorJudgementInfo>> cache = getCache();
			TreeMap<Integer, MonitorJudgementInfo> judgementInfoList = createStringJudgementInfoList(monitorStringValueList);
			cache.put(new MonitorJudgementKey(monitorId, MonitorTypeConstant.TYPE_STRING), 
					judgementInfoList);
			storeCache(cache);
		} catch (Exception e) {
			m_log.warn("updateString() write : "
					+ " monitorId=" + monitorId 
					+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.writeUnlock();
			m_log.debug("updateString() write : "
					+ " monitorId=" + monitorId 
					+ ", cachesize=" + getCache().size());
		}
	}

	/**
	 * 真偽値監視の判定情報キャッシュの更新を行う。
	 * 
	 * @param monitorId 監視項目ID
	 * @param monitorTruthValueList 真偽値監視の判定情報
	 * 
	 */
	public static void updateTruth(String monitorId, List<MonitorTruthValueInfo> monitorTruthValueList) {

		if (monitorId == null || monitorId.isEmpty()) {
			return;
		}
		try {
			_lock.writeLock();
			ConcurrentHashMap<MonitorJudgementKey, TreeMap<Integer, MonitorJudgementInfo>> cache = getCache();
			TreeMap<Integer, MonitorJudgementInfo> judgementInfoList = createTruthJudgementInfoList(monitorTruthValueList);
			cache.put(new MonitorJudgementKey(monitorId, MonitorTypeConstant.TYPE_TRUTH), 
					judgementInfoList);
			storeCache(cache);
		} catch (Exception e) {
			m_log.warn("updateTruth() write : "
					+ " monitorId=" + monitorId 
					+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.writeUnlock();
			m_log.debug("updateTruth() write : "
					+ " monitorId=" + monitorId 
					+ ", cachesize=" + getCache().size());
		}
	}

	/**
	 * 数値監視の判定情報キャッシュの更新を行う。
	 * 
	 * @param monitorId 監視項目ID
	 * @param monitorNumericValueList 数値監視の判定情報
	 * 
	 */
	public static void updateNumeric(String monitorId, List<MonitorNumericValueInfo> monitorNumericValueList) {

		if (monitorId == null 
				|| monitorId.isEmpty()) {
			return;
		}
		List<MonitorNumericValueInfo> monitorNumericValueListBasic = new ArrayList<>();
		List<MonitorNumericValueInfo> monitorNumericValueListChange = new ArrayList<>();
		try {
			_lock.writeLock();
			ConcurrentHashMap<MonitorJudgementKey, TreeMap<Integer, MonitorJudgementInfo>> cache = getCache();
			if (monitorNumericValueList != null) {
				for (MonitorNumericValueInfo monitorNumericValueInfo : monitorNumericValueList) {
					if (MonitorNumericType.TYPE_BASIC.getType().equals(monitorNumericValueInfo.getId().getMonitorNumericType())) {
						monitorNumericValueListBasic.add(monitorNumericValueInfo);
					} else if (MonitorNumericType.TYPE_CHANGE.getType().equals(monitorNumericValueInfo.getId().getMonitorNumericType())) {
						monitorNumericValueListChange.add(monitorNumericValueInfo);
					}
				}
			}
			// 通常監視
			TreeMap<Integer, MonitorJudgementInfo> judgementInfoList = createNumericJudgementInfoList(monitorNumericValueListBasic);
			cache.put(new MonitorJudgementKey(monitorId, MonitorTypeConstant.TYPE_NUMERIC, MonitorNumericType.TYPE_BASIC.getType()), 
					judgementInfoList);
			// 変化量監視
			judgementInfoList = createNumericJudgementInfoList(monitorNumericValueListChange);
			cache.put(new MonitorJudgementKey(monitorId, MonitorTypeConstant.TYPE_NUMERIC, MonitorNumericType.TYPE_CHANGE.getType()), 
					judgementInfoList);
			storeCache(cache);
		} catch (Exception e) {
			m_log.warn("updateNumeric() write : "
					+ " monitorId=" + monitorId 
					+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.writeUnlock();
			m_log.debug("updateNumeric() write : "
					+ " monitorId=" + monitorId 
					+ ", cachesize=" + getCache().size());
		}
	}

	/**
	 * DBから判定情報を取得し、キャッシュを更新する
	 * 
	 * @param monitorId 監視項目ID
	 * @param monitorType 監視種別
	 */
	private static void update(String monitorId, Integer monitorType) {

		if (monitorId == null
				|| monitorId.isEmpty()
				|| monitorType == null
				|| (!monitorType.equals(MonitorTypeConstant.TYPE_STRING)
				&& !monitorType.equals(MonitorTypeConstant.TYPE_TRUTH)
				&& !monitorType.equals(MonitorTypeConstant.TYPE_NUMERIC))){
			return;
		}
		// キー情報
		MonitorJudgementKey monitorJudgementKey = null;
		MonitorJudgementKey monitorJudgementKeyChange = null;	// 数値監視（変化量）用
		if (monitorType.equals(MonitorTypeConstant.TYPE_STRING) 
				|| monitorType.equals(MonitorTypeConstant.TYPE_TRUTH)) {
			// 文字列監視、真偽値監視
			monitorJudgementKey = new MonitorJudgementKey(monitorId, monitorType);
		} else {
			// 数値監視
			monitorJudgementKey = new MonitorJudgementKey(monitorId, monitorType, MonitorNumericType.TYPE_BASIC.getType());
			monitorJudgementKeyChange = new MonitorJudgementKey(monitorId, monitorType, MonitorNumericType.TYPE_CHANGE.getType());
		}
		try {
			_lock.writeLock();
			ConcurrentHashMap<MonitorJudgementKey, TreeMap<Integer, MonitorJudgementInfo>> cache = getCache();
			if (cache.containsKey(monitorJudgementKey)) {
				// 既にキャッシュ上に存在する場合は処理終了
				return;
			}
			// 判定情報
			TreeMap<Integer, MonitorJudgementInfo> judgementInfoList = null;
			TreeMap<Integer, MonitorJudgementInfo> judgementInfoListChange = null;	// 数値監視（変化量）用
			if (monitorType.equals(MonitorTypeConstant.TYPE_STRING)) {
				// 文字列監視
				List<MonitorStringValueInfo> monitorStringValueList
					= QueryUtil.getMonitorStringValueInfoFindByMonitorId(monitorId, ObjectPrivilegeMode.NONE);
				judgementInfoList = createStringJudgementInfoList(monitorStringValueList);
				cache.put(monitorJudgementKey, judgementInfoList);
			} else if (monitorType.equals(MonitorTypeConstant.TYPE_TRUTH)) {
				// 真偽値監視
				List<MonitorTruthValueInfo> monitorTruthValueList
					= QueryUtil.getMonitorTruthValueInfoFindByMonitorId(monitorId, ObjectPrivilegeMode.NONE);
				judgementInfoList = createTruthJudgementInfoList(monitorTruthValueList);
				cache.put(monitorJudgementKey, judgementInfoList);
			} else if (monitorType.equals(MonitorTypeConstant.TYPE_NUMERIC)) {
				// 数値監視
				// 通常監視
				List<MonitorNumericValueInfo> monitorNumericValueList
					= QueryUtil.getMonitorNumericValueInfoByIdNumericType(monitorId, MonitorNumericType.TYPE_BASIC.getType(), ObjectPrivilegeMode.NONE);
				judgementInfoList = createNumericJudgementInfoList(monitorNumericValueList);
				cache.put(monitorJudgementKey, judgementInfoList);
				// 変化量監視
				monitorNumericValueList
					= QueryUtil.getMonitorNumericValueInfoByIdNumericType(monitorId, MonitorNumericType.TYPE_CHANGE.getType(), ObjectPrivilegeMode.NONE);
				judgementInfoListChange = createNumericJudgementInfoList(monitorNumericValueList);
				cache.put(monitorJudgementKeyChange, judgementInfoListChange);
			}
			storeCache(cache);
		} catch (Exception e) {
			m_log.warn("update() : "
					+ " monitorId=" + monitorId 
					+ ", monitorType=" + monitorType
					+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.writeUnlock();
			m_log.debug("update() : "
					+ " monitorId=" + monitorId 
					+ ", monitorType=" + monitorType);
		}
	}

	/**
	 * キャッシュされている判定情報を取得する。
	 * @param monitorId 監視項目ID
	 * @param monitorType 監視種別
	 * @return 判定情報マップ
	 */
	public static TreeMap<Integer, MonitorJudgementInfo> getMonitorJudgementMap(String monitorId, Integer monitorType) {
		return getMonitorJudgementMap(monitorId, monitorType, "");
	}

	/**
	 * キャッシュされている判定情報を取得する。
	 * @param monitorId 監視項目ID
	 * @param monitorType 監視種別
	 * @param monitorNumericType 数値監視種別
	 * @return 判定情報マップ
	 */
	public static TreeMap<Integer, MonitorJudgementInfo> getMonitorJudgementMap(String monitorId, Integer monitorType, String monitorNumericType) {
		if (monitorId == null
				|| monitorId.isEmpty()
				|| monitorType == null
				|| (!monitorType.equals(MonitorTypeConstant.TYPE_STRING)
					&& !monitorType.equals(MonitorTypeConstant.TYPE_TRUTH)
					&& !monitorType.equals(MonitorTypeConstant.TYPE_NUMERIC))){
			return null;
		}

		// 更新処理
		update(monitorId,monitorType);

		// 取得処理
		TreeMap<Integer, MonitorJudgementInfo> monitorJudgementMap = new TreeMap<>();
		// キー
		MonitorJudgementKey monitorJudgementKey = null;
		if (monitorType.equals(MonitorTypeConstant.TYPE_STRING) 
				|| monitorType.equals(MonitorTypeConstant.TYPE_TRUTH)) {
			// 文字列監視、真偽値監視
			monitorJudgementKey = new MonitorJudgementKey(monitorId, monitorType);
		} else {
			// 数値監視
			monitorJudgementKey = new MonitorJudgementKey(monitorId, monitorType, monitorNumericType);
		}
		try {
			_lock.readLock();
			ConcurrentHashMap<MonitorJudgementKey, TreeMap<Integer, MonitorJudgementInfo>> cache = getCache();
			if (cache.containsKey(monitorJudgementKey)) {
				monitorJudgementMap = cache.get(monitorJudgementKey);
			}
		} catch (Exception e) {
			m_log.warn("getMonitorJudgementList() : "
					+ " monitorId=" + monitorId 
					+ ", monitorType=" + monitorType
					+ ", monitorNumericType=" + monitorNumericType
					+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.readUnlock();
			m_log.debug("getMonitorJudgementList cachesize : " + getCache().size());
		}
		return monitorJudgementMap;
	}

	/**
	 * キャッシュから当該項目を削除する。
	 * 監視設定の削除時に呼ぶ。
	 * 
	 * @param monitorId		監視設定ID
	 */
	public static void remove(String monitorId) {
		if (monitorId == null || monitorId.isEmpty()) {
			return;
		}
		m_log.info("remove() cache is removed. , monitorId=" + monitorId);
		try {
			_lock.writeLock();
			ConcurrentHashMap<MonitorJudgementKey, TreeMap<Integer, MonitorJudgementInfo>> cache = getCache();
			for (Iterator<MonitorJudgementKey> iter = cache.keySet().iterator(); iter.hasNext();) {
				MonitorJudgementKey pk = iter.next();
				if (Objects.equals(pk.getMonitorId(), monitorId)) {
					iter.remove();
				}
			}
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}
}
