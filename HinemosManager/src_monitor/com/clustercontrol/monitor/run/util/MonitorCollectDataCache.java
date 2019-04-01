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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.collect.model.CollectKeyInfoPK;
import com.clustercontrol.collect.util.QueryUtil;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.CollectKeyNotFound;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.util.HinemosTime;

/**
 * 監視で使用する収集データ用のキャッシュ
 *
 * @version 6.1.0
 */
public class MonitorCollectDataCache {

	private static Log m_log = LogFactory.getLog( MonitorCollectDataCache.class );

	private static final ILock _lock;

	// 保持期間情報(monitorId, range)
	private static ConcurrentHashMap<String, Integer> monitorRangeMap = new ConcurrentHashMap<>();

	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(MonitorCollectDataCache.class.getName());
		
		try {
			_lock.writeLock();
			
			ConcurrentHashMap<MonitorCollectDataPK, MonitorCollectDataInfo> cache = getCache();
			if (cache == null) {	// not null when clustered
				storeCache(new ConcurrentHashMap<MonitorCollectDataPK, MonitorCollectDataInfo>());
			}
		} finally {
			_lock.writeUnlock();
		}
	}


	/**
	 * 収集データ情報
	 */
	public static class MonitorCollectDataInfo {
		// キー情報
		private MonitorCollectDataPK id;
		// 収集データ
		private List<MonitorCollectData> dataList = new ArrayList<>();
		// 回帰方程式の種別
		private String predictionMethod;
		// 回帰方程式の係数
		private Double[] coefficients;
		// 必要保持期間
		private Integer range;

		public MonitorCollectDataInfo(MonitorCollectDataPK pk) {
			this.setId(pk);
			setRange(getRangeFromMap(pk.monitorId));
		}

		public MonitorCollectDataPK getId() {
			return this.id;
		}
		public void setId(MonitorCollectDataPK id) {
			this.id = id;
		}

		public List<MonitorCollectData> getDataList() {
			return this.dataList;
		}

		public String getPredictionMethod() {
			return this.predictionMethod;
		}
		public void setPredictionMethod(String predictionMethod) {
			this.predictionMethod = predictionMethod;
		}

		public Double[] getCoefficients() {
			return this.coefficients;
		}
		public void setCoefficients(Double[] coefficients) {
			this.coefficients = coefficients;
		}
		
		public Integer getRange() {
			return this.range;
		}
		public void setRange(Integer range) {
			this.range = range;
		}

	}

	/**
	 * 収集データBean
	 * 収集日時、値は、計算時に解析時にデータ型を統一する必要があるため、
	 * Double型にして保持している。
	 */
	public static class MonitorCollectData {
		// 収集日時
		private Double time;
		// 値
		private Double value;

		public MonitorCollectData(Long time, Float value) {
			this.setTime(time.doubleValue());
			if (value == null) {
				this.setValue(null);
			} else {
				this.setValue(value.doubleValue());
			}
		}

		public MonitorCollectData(Double time, Double value) {
			this.setTime(time);
			this.setValue(value);
		}

		public Double getTime() {
			return this.time;
		}
		public void setTime(Double time) {
			this.time = time;
		}

		public Double getValue() {
			return this.value;
		}
		public void setValue(Double value) {
			this.value = value;
		}
	}

	/**
	 * 収集データキー情報
	 */
	public static class MonitorCollectDataPK {
		// 監視項目ID
		private String monitorId;
		// ファシリティID
		private String facilityId;
		// DisplayName
		private String displayName;
		// Item Name
		private String itemName;

		public MonitorCollectDataPK(String monitorId, String facilityId, String displayName, String itemName) {
			if (displayName == null) {
				displayName = "";
			}
			if (itemName == null) {
				itemName = "";
			}
			this.setMonitorId(monitorId);
			this.setFacilityId(facilityId);
			this.setDisplayName(displayName);
			this.setItemName(itemName);
		}

		public String getMonitorId() {
			return this.monitorId;
		}
		public void setMonitorId(String monitorId) {
			this.monitorId = monitorId;
		}

		public String getFacilityId() {
			return this.facilityId;
		}
		public void setFacilityId(String facilityId) {
			this.facilityId = facilityId;
		}

		public String getDisplayName() {
			return this.displayName;
		}
		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getItemName() {
			return this.itemName;
		}
		public void setItemName(String itemName) {
			this.itemName = itemName;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof MonitorCollectDataPK)) {
				return false;
			}
			MonitorCollectDataPK castOther = (MonitorCollectDataPK)other;
			return
				this.monitorId.equals(castOther.monitorId)
				&& this.facilityId.equals(castOther.facilityId)
				&& this.displayName.equals(castOther.displayName)
				&& this.itemName.equals(castOther.itemName);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int hash = 17;
			hash = hash * prime + this.monitorId.hashCode();
			hash = hash * prime + this.facilityId.hashCode();
			hash = hash * prime + this.displayName.hashCode();
			hash = hash * prime + this.itemName.hashCode();
			return hash;
		}

		@Override
		public String toString() {
			String[] names = {
					"monitorId",
					"facilityId",
					"displayName",
					"itemName",
			};
			String[] values = {
					this.monitorId,
					this.facilityId,
					this.displayName,
					this.itemName
			};
			return Arrays.toString(names) + " = " + Arrays.toString(values);
		}
	}

	@SuppressWarnings("unchecked")
	private static ConcurrentHashMap<MonitorCollectDataPK, MonitorCollectDataInfo> getCache() {
		ConcurrentHashMap<MonitorCollectDataPK, MonitorCollectDataInfo> rtn = null;
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_MONITOR_COLLECT_DATA);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_MONITOR_COLLECT_DATA + " : " + cache);
		if (cache != null) {
			rtn = (ConcurrentHashMap<MonitorCollectDataPK, MonitorCollectDataInfo>)cache;
		}
		return rtn;
	}
	
	private static void storeCache(ConcurrentHashMap<MonitorCollectDataPK, MonitorCollectDataInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_MONITOR_COLLECT_DATA + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_MONITOR_COLLECT_DATA, newCache);
	}

	/**
	 * 収集値を取得する
	 *    指定された日時の情報は、DBより取得せずに渡した値を使用する
	 *    
	 * @param monitorId		監視設定ID
	 * @param facilityId	ファシリティID
	 * @param displayName	DisplayName
	 * @param itemName		ItemName
	 * @param targetDate	処理時点の日時
	 * @return
	 */
	private static List<MonitorCollectData> getCollectDataList(
			String monitorId,
			String facilityId, 
			String displayName,
			String itemName,
			Long targetDate,
			Integer range) {

		List<MonitorCollectData> monitorCollectDataList = new ArrayList<>();
		CollectKeyInfo collectKeyInfo = null;

		// パラメータが設定されていない場合は処理終了
		if (monitorId == null
				|| monitorId.isEmpty()
				|| facilityId == null
				|| facilityId.isEmpty()) {
			return monitorCollectDataList;
		}
		if (displayName == null) {
			displayName = "";
		}
		if (itemName == null) {
			itemName = "";
		}

		// 日時が設定されていない場合は現在日時を設定する。
		if (targetDate == null) {
			targetDate = HinemosTime.currentTimeMillis();
		}

		// キー取得
		try {
			collectKeyInfo = QueryUtil.getCollectKeyPK(
					new CollectKeyInfoPK(itemName, displayName, monitorId, facilityId));
		} catch (CollectKeyNotFound e) {
			m_log.debug("collectKeyInfo is not found. " + e.getClass().getName() 
					+ ", monitorId=" + monitorId 
					+ ", facilityId=" + facilityId
					+ ", displayName=" + displayName
					+ ", itemName=" + itemName);
			return monitorCollectDataList;
		}

		// 取得対象日時の算出
		Long startDate = targetDate - range.longValue() * 60L * 1000L;

		// データ取得
		if (collectKeyInfo != null) {
			try {
				List<CollectData> dbCollectDataList 
					= QueryUtil.getCollectDataListOrderByTimeDesc(collectKeyInfo.getCollectorid(), startDate, targetDate);
				if (dbCollectDataList != null) {
					for (CollectData dbCollectData : dbCollectDataList) {
						monitorCollectDataList.add(new MonitorCollectData(dbCollectData.getTime(), dbCollectData.getValue()));
					}
				}
			} catch (Exception e) {
				m_log.debug("collectData is not found. " + e.getClass().getName() 
						+ ", monitorId=" + monitorId 
						+ ", facilityId=" + facilityId
						+ ", displayName=" + displayName
						+ ", itemName=" + itemName
						+ ", targetDate=" + targetDate);
				return monitorCollectDataList;
			}
		}
		return monitorCollectDataList;
	}

	/**
	 * キャッシュの更新を行う。
	 * データが存在する場合は処理を終了する。
	 * 
	 * @param monitorId 監視項目ID
	 * @param facilityId ファシリティID
	 * @param displayName DisplayName
	 * @param itemName ItemName
	 */
	public static void update(String monitorId, String facilityId, String displayName, String itemName, Long targetDate) {

		if (displayName == null) {
			displayName = "";
		}
		if (itemName == null) {
			itemName = "";
		}
		// キー
		MonitorCollectDataPK monitorCollectDataPk = new MonitorCollectDataPK(monitorId, facilityId, displayName, itemName);
		try {
			_lock.readLock();
			ConcurrentHashMap<MonitorCollectDataPK, MonitorCollectDataInfo> cache = getCache();
			// キャッシュ上にデータが存在する場合は処理終了
			if (cache.containsKey(monitorCollectDataPk)) {
				return;
			}
		} catch (Exception e) {
			m_log.warn("update() read : "
					+ ", monitorId=" + monitorId 
					+ ", facilityId=" + facilityId
					+ ", displayName=" + displayName
					+ ", itemName=" + itemName
					+ ", targetDate=" + targetDate
					+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.readUnlock();
			m_log.debug("update() read : "
					+ ", monitorId=" + monitorId 
					+ ", facilityId=" + facilityId
					+ ", displayName=" + displayName
					+ ", itemName=" + itemName
					+ ", targetDate=" + targetDate
					+ ", cachesize=" + getCache().size());
		}
		try {
			_lock.writeLock();
			ConcurrentHashMap<MonitorCollectDataPK, MonitorCollectDataInfo> cache = getCache();
			// キャッシュ上にデータが存在しない場合にキャッシュ更新処理を行う。
			if (!cache.containsKey(monitorCollectDataPk)) {
				// DBからデータを取得する。
				MonitorCollectDataInfo monitorCollectDataInfo = new MonitorCollectDataInfo(monitorCollectDataPk);
				monitorCollectDataInfo.getDataList().addAll(
						getCollectDataList(monitorId, facilityId, displayName, itemName, targetDate, getRangeFromMap(monitorId)));
				cache.put(monitorCollectDataPk, monitorCollectDataInfo);
			}
			storeCache(cache);
		} catch (Exception e) {
			m_log.warn("update() write : "
					+ ", monitorId=" + monitorId 
					+ ", facilityId=" + facilityId
					+ ", displayName=" + displayName
					+ ", itemName=" + itemName
					+ ", targetDate=" + targetDate
					+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.writeUnlock();
			m_log.debug("update() write : "
					+ ", monitorId=" + monitorId 
					+ ", facilityId=" + facilityId
					+ ", displayName=" + displayName
					+ ", itemName=" + itemName
					+ ", targetDate=" + targetDate
					+ ", cachesize=" + getCache().size());
		}
	}

	/**
	 * キャッシュのデータの追加を行う。
	 * データが存在しない場合は処理を終了する。
	 * 
	 * @param monitorId 監視項目ID
	 * @param facilityId ファシリティID
	 * @param displayName DisplayName
	 * @param itemName ItemName
	 * @param collectTime 収集日時
	 * @param collectValue 収集値
	 */
	public static void add(
			String monitorId, String facilityId, String displayName, String itemName, Long collectTime, Float collectValue) {
		// 現在日時
		Long nowDate = HinemosTime.currentTimeMillis();

		if (displayName == null) {
			displayName = "";
		}
		if (itemName == null) {
			itemName = "";
		}
		// キー
		MonitorCollectDataPK monitorCollectDataPk = new MonitorCollectDataPK(monitorId, facilityId, displayName, itemName);
		try {
			_lock.writeLock();

			ConcurrentHashMap<MonitorCollectDataPK, MonitorCollectDataInfo> cache = getCache();
			if (!cache.containsKey(monitorCollectDataPk)) {
				// update()メソッドにより既にデータが存在するはずなので、ない場合はここで終了。
				return;
			}
			MonitorCollectDataInfo monitorCollectDataInfo = cache.get(monitorCollectDataPk);
			// 収集期間以前のデータを削除する
			for(ListIterator<MonitorCollectData> iter 
					= monitorCollectDataInfo.getDataList().listIterator(
							monitorCollectDataInfo.getDataList().size());iter.hasPrevious();){
				MonitorCollectData monitorCollectData = (MonitorCollectData)iter.previous();
				if (monitorCollectData.getTime() >= nowDate - getRangeFromMap(monitorId) * 60D * 1000D) {
					break;
				}
				iter.remove();
			}
			// 最新データがキャッシュ上に存在するかどうか確認する
			boolean isExists = false;
			for (MonitorCollectData monitorCollectData : monitorCollectDataInfo.getDataList()) {
				if (monitorCollectData.getTime() < collectTime.longValue()) {
					break;
				} else if (monitorCollectData.getTime().longValue() == collectTime.longValue()) {
					isExists = true;
					break;
				}
			}
			if (!isExists) {
				// 最新のデータを保持していない場合に追加する
				monitorCollectDataInfo.getDataList().add(0,
						new MonitorCollectData(collectTime, collectValue));
			}
			cache.put(monitorCollectDataPk, monitorCollectDataInfo);
			storeCache(cache);
		} catch (Exception e) {
			m_log.warn("add() : "
					+ ", monitorId=" + monitorId 
					+ ", facilityId=" + facilityId
					+ ", displayName=" + displayName
					+ ", itemName=" + itemName
					+ ", collectTime=" + collectTime
					+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.writeUnlock();
			m_log.debug("MonitorCollectDataCache add() : "
					+ ", monitorId=" + monitorId 
					+ ", facilityId=" + facilityId
					+ ", displayName=" + displayName
					+ ", itemName=" + itemName
					+ ", collectTime=" + collectTime
					+ ", cachesize=" + getCache().size());
		}
	}

	/**
	 * キャッシュへの回帰分析情報の更新を行う。
	 * 
	 * @param monitorId			監視項目ID
	 * @param facilityId		ファシリティID
	 * @param displayName		DisplayName
	 * @param itemName			ItemName
	 * @param predictionMethod	回帰分析種別
	 * @param coefficients		係数
	 */
	public static void setPredictionInfo (String monitorId, String facilityId, String displayName, String itemName,
			String predictionMethod, Double[] coefficients) {
		if (displayName == null) {
			displayName = "";
		}
		if (itemName == null) {
			itemName = "";
		}
		// キー
		MonitorCollectDataPK monitorCollectDataPk = new MonitorCollectDataPK(monitorId, facilityId, displayName, itemName);
		try {
			_lock.writeLock();

			ConcurrentHashMap<MonitorCollectDataPK, MonitorCollectDataInfo> cache = getCache(); 
			MonitorCollectDataInfo monitorCollectDataInfo = null;
			if (cache.containsKey(monitorCollectDataPk)) {
				monitorCollectDataInfo = cache.get(monitorCollectDataPk);
			} else {
				monitorCollectDataInfo = new MonitorCollectDataInfo(monitorCollectDataPk);
			}
			monitorCollectDataInfo.setPredictionMethod(predictionMethod);
			Double[] newCoefficients = null;
			if (coefficients != null) {
				newCoefficients = new Double[coefficients.length];
				for (int i = 0; i < coefficients.length; i++) {
					if (coefficients[i] == null) {
						newCoefficients[i] = null;
					} else {
						newCoefficients[i] = coefficients[i];
					}
				}
			}
			monitorCollectDataInfo.setCoefficients(newCoefficients);
			// キャッシュ更新
			cache.put(monitorCollectDataPk, monitorCollectDataInfo);
			storeCache(cache);
		} catch (Exception e) {
			m_log.warn("update() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.writeUnlock();
			m_log.debug("MonitorCollectDataCache update cachesize : " + getCache().size());
		}
	}

	/**
	 * キャッシュされている収集データを取得する。
	 * @param monitorId 監視項目ID
	 * @param facilityId ファシリティID
	 * @return 収集データ
	 */
	public static MonitorCollectDataInfo getMonitorCollectDataInfo(String monitorId, String facilityId, String displayName, String itemName) {
		if (displayName == null) {
			displayName = "";
		}
		if (itemName == null) {
			itemName = "";
		}
		// キー
		MonitorCollectDataPK monitorCollectDataPk = new MonitorCollectDataPK(monitorId, facilityId, displayName, itemName);
		MonitorCollectDataInfo monitorCollectDataInfo = null;
		try {
			_lock.readLock();
			ConcurrentHashMap<MonitorCollectDataPK, MonitorCollectDataInfo> cache = getCache();
			if (cache.containsKey(monitorCollectDataPk)) {
				monitorCollectDataInfo = cache.get(monitorCollectDataPk);
			}
		} catch (Exception e) {
			m_log.warn("update() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_lock.readUnlock();
			m_log.debug("MonitorCollectDataCache update cachesize : " + getCache().size());
		}
		return monitorCollectDataInfo;
	}

	/**
	 * 収集値を取得する
	 *    指定された日時の情報は、DBより取得せずに渡した値を使用する
	 *    
	 * @param monitorId 監視設定ID
	 * @param facilityId　ファシリティID
	 * @param displayName DisplayName
	 * @param itemName ItemName
	 * @param targetDate 処理時点の日時
	 * @param targetAnalysysRange 収集期間
	 * @return　収集データ
	 */
	public static List<MonitorCollectData> getMonitorCollectDataList(
			String monitorId, 
			String facilityId, 
			String displayName,
			String itemName,
			Long targetDate,
			Double targetAnalysysRange) {
		if (displayName == null) {
			displayName = "";
		}
		if (itemName == null) {
			itemName = "";
		}
		List<MonitorCollectData> rtn = new ArrayList<>();
		
		// キャッシュを取得する
		MonitorCollectDataInfo monitorCollectDataInfo = 
				getMonitorCollectDataInfo(monitorId, facilityId, displayName, itemName);
		// キャッシュのリストより必要な情報を抽出する
		if (monitorCollectDataInfo != null && monitorCollectDataInfo.getDataList() != null) {
			for (MonitorCollectData monitorCollectData : monitorCollectDataInfo.getDataList()) {
				if (monitorCollectData.getTime() <
						targetDate.doubleValue() - targetAnalysysRange * 60D * 1000D) {
					break;
				}
				rtn.add(new MonitorCollectData(monitorCollectData.getTime(), 
						monitorCollectData.getValue()));
			}
		}
		return rtn;
	}

	/**
	 * 回帰方程式の係数を取得する(将来予測監視)
	 *    
	 * @param monitorInfo	監視設定
	 * @param facilityId	ファシリティID
	 * @param displayName	DisplayName
	 * @param itemName		ItemName
	 * @return 回帰方程式の係数
	 */
	public static Double[] getCoefficients(
			String monitorId, 
			String facilityId, 
			String displayName,
			String itemName) {
		Double[] rtn = {0D, 0D, 0D, 0D, 0D};

		if (displayName == null) {
			displayName = "";
		}
		if (itemName == null) {
			itemName = "";
		}
		// キャッシュを取得する
		MonitorCollectDataInfo monitorCollectDataInfo = getMonitorCollectDataInfo(monitorId, facilityId, displayName, itemName);
		if (monitorCollectDataInfo == null
				|| monitorCollectDataInfo.getCoefficients() == null) {
			return rtn;
		}
		rtn = new Double[monitorCollectDataInfo.getCoefficients().length];
		for (int i = 0; i < monitorCollectDataInfo.getCoefficients().length; i++) {
			rtn[i] = monitorCollectDataInfo.getCoefficients()[i];
		}
		return rtn;
	}

	/**
	 * キャッシュより不要なデータを削除する。
	 * メンテナンス機能より呼び出されることを想定。
	 */
	public static void removeUnnecessaryData() {
		m_log.info("removeUnnecessaryData() cache is removed. start");
		List<String> delMonitorIdList = new ArrayList<>();
		int delCount = 0;
		
		try {
			_lock.writeLock();

			ConcurrentHashMap<MonitorCollectDataPK, MonitorCollectDataInfo> cache = getCache();
			for (Iterator<MonitorCollectDataPK> iter = cache.keySet().iterator(); iter.hasNext();) {
				MonitorCollectDataPK pk = iter.next();
				if (delMonitorIdList.contains(pk.getMonitorId())) {
					iter.remove();
					m_log.debug("removeUnnecessaryData() : delete"
							+ " monitorId=" + pk.getMonitorId()
							+ ", displayName=" + pk.getDisplayName()
							+ ", itemName=" + pk.getItemName()
							+ ", facilityId=" + pk.getFacilityId());
					delCount++;
					continue;
				}
				MonitorInfo monitorInfo = null;
				try {
					monitorInfo = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_NONE(pk.getMonitorId());
				} catch (MonitorNotFound e) {
					// 監視設定が存在しない場合
					delMonitorIdList.add(pk.getMonitorId());
					iter.remove();
					m_log.debug("removeUnnecessaryData() : delete"
							+ " monitorId=" + pk.getMonitorId()
							+ ", displayName=" + pk.getDisplayName()
							+ ", itemName=" + pk.getItemName()
							+ ", facilityId=" + pk.getFacilityId());
					delCount++;
					continue;
				}
				if (!monitorInfo.getFacilityId().equals(pk.getFacilityId())
						&& !(FacilitySelector
							.getFacilityIdList(monitorInfo.getFacilityId(), RoleIdConstant.ADMINISTRATORS, 0, false, true)
							.contains(pk.getFacilityId()))) {
					// ファシリティIDがない場合
					iter.remove();
					m_log.debug("removeUnnecessaryData() : delete"
							+ " monitorId=" + pk.getMonitorId()
							+ ", displayName=" + pk.getDisplayName()
							+ ", itemName=" + pk.getItemName()
							+ ", facilityId=" + pk.getFacilityId());
					delCount++;
					continue;
				}
			}
			storeCache(cache);
			m_log.info("removeUnnecessaryData() : delete count=" + delCount);
		} finally {
			_lock.writeUnlock();
		}
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
			ConcurrentHashMap<MonitorCollectDataPK, MonitorCollectDataInfo> cache = getCache();
			for (Iterator<MonitorCollectDataPK> iter = cache.keySet().iterator(); iter.hasNext();) {
				MonitorCollectDataPK pk = iter.next();
				if (Objects.equals(pk.getMonitorId(), monitorId)) {
					iter.remove();
				}
			}
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * キャッシュの当該項目を更新する。
	 * ログ件数監視集計処理で呼ぶ。
	 * 
	 * @param monitorId		監視設定ID
	 */
	public static void refresh(String monitorId) {
		if (monitorId == null || monitorId.isEmpty()) {
			return;
		}
		List<MonitorCollectDataPK> monitorCollectDataPkList = new ArrayList<>();
		m_log.info("refresh() cache is refresh. , monitorId=" + monitorId);

		// 削除処理
		try {
			_lock.writeLock();

			ConcurrentHashMap<MonitorCollectDataPK, MonitorCollectDataInfo> cache = getCache();
			for (Iterator<MonitorCollectDataPK> iter = cache.keySet().iterator(); iter.hasNext();) {
				MonitorCollectDataPK pk = iter.next();
				if (Objects.equals(pk.getMonitorId(), monitorId)) {
					monitorCollectDataPkList.add(new MonitorCollectDataPK(
							pk.getMonitorId(), pk.getFacilityId(), pk.getDisplayName(), pk.getItemName()));
					iter.remove();
				}
			}
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}

		// 更新処理
		Long targetDate = HinemosTime.currentTimeMillis();
		for (MonitorCollectDataPK pk : monitorCollectDataPkList) {
			update(pk.getMonitorId(), pk.getFacilityId(), pk.getDisplayName(), pk.getItemName(), targetDate);
		}
	}

	public static void refreshRange(String monitorId, String monitorTypeId, int range) {
		if (monitorId == null || monitorId.isEmpty()
				|| monitorTypeId == null || monitorTypeId.isEmpty()) {
			return;
		}
		m_log.info("refreshRange() cache is refresh. , monitorId=" + monitorId);

		// 更新処理
		try {
			_lock.writeLock();

			// 収集期間情報の更新
			calcRange(monitorId, monitorTypeId, range);

			// 収集データの更新
			ConcurrentHashMap<MonitorCollectDataPK, MonitorCollectDataInfo> cache = getCache();
			for (Iterator<MonitorCollectDataPK> iter = cache.keySet().iterator(); iter.hasNext();) {
				MonitorCollectDataPK pk = iter.next();
				if (Objects.equals(pk.getMonitorId(), monitorId)) {
					MonitorCollectDataInfo collectData = cache.get(pk);
					if (collectData.getRange() < range) {
						// より広くキャッシュに持つ必要がある場合はキャッシュをリフレッシュする
						m_log.info("refreshRange() range become bigger. before="+collectData.getRange()+",after="+range);
						collectData.setRange(range);
						collectData.getDataList().clear();
						collectData.getDataList().addAll(
								getCollectDataList(monitorId, pk.facilityId, pk.displayName, pk.itemName, HinemosTime.currentTimeMillis(), range));
					} else {
						// キャッシュの保持幅を狭めてよいかチェックする
						int requireRange = getRangeFromMap(monitorId);
						if (requireRange < collectData.getRange()) {
							m_log.info("refreshRange() range become smaller. before="+collectData.getRange()+",after="+requireRange);
							collectData.setRange(requireRange);
						}
					}
				}
			}
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	private static void calcRange(String monitorId) {
		calcRange(monitorId, null, 0);
	}

	/**
	 * 
	 * @param monitorId
	 * @param monitorTypeId
	 */
	private static void calcRange(String monitorId, String monitorTypeId, int paramRange) {
		if (monitorId.isEmpty()) {
			m_log.warn("calcRange(): monitorId is null or empty");
			return;
		}

		int range = 0;
		try {
			if (monitorTypeId == null 
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CORRELATION)) {
				// リフレッシュ、もしくは相関係数監視の場合
				// 相関係数監視以外の監視の情報取得
				MonitorInfo monitorInfo = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_NONE(monitorId);
				range = monitorInfo.getPredictionAnalysysRange();
				if (range < monitorInfo.getChangeAnalysysRange()) {
					range = monitorInfo.getChangeAnalysysRange();
				}
			} else {
				// リフレッシュではなく、かつ相関係数監視以外の監視の場合
				// 引数（相関係数監視以外の監視の収集期間）を使用
				range = paramRange;
			}

			if (monitorTypeId == null
					|| !monitorTypeId.equals(HinemosModuleConstant.MONITOR_CORRELATION)) {
				// リフレッシュ、もしくは相関係数以外の監視の場合
				// 相関係数監視の情報取得
				List<MonitorInfo> list = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoByMonitorTypeId_NONE(HinemosModuleConstant.MONITOR_CORRELATION);
				for (MonitorInfo info : list) {
					if (info.getCorrelationCheckInfo().getTargetMonitorId().equals(monitorId) ||
							info.getCorrelationCheckInfo().getReferMonitorId().equals(monitorId)) {
						if (info.getCorrelationCheckInfo().getAnalysysRange() > range) {
							range = info.getCorrelationCheckInfo().getAnalysysRange();
						}
					}
				}
			} else {
				// リフレッシュではなく、かつ相関係数監視の場合
				// 引数（相関係数監視の収集期間）を使用
				if (paramRange > range) {
					range = paramRange;
				}
			}

			// 収集期間の設定
			monitorRangeMap.put(monitorId, range);
		} catch (MonitorNotFound e) {
			m_log.warn("calcRange() : " + e.getMessage());
		}
	}

	private static int getRangeFromMap(String monitorId) {
		int range = 0;
		if (monitorId.isEmpty()) {
			m_log.warn("getRange(): monitorId is null or empty");
		} else if (monitorRangeMap.containsKey(monitorId)) {
			range = monitorRangeMap.get(monitorId);
		} else {
			// DBから取得
			calcRange(monitorId);
			if (monitorRangeMap.containsKey(monitorId)) {
				range = monitorRangeMap.get(monitorId);
			} else {
				m_log.warn("getRange(): monitorId is not registered in the map. : monitorId=" + monitorId);
			}
		}
		return range;
	}
}
