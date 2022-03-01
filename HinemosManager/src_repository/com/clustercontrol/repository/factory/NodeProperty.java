/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.factory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.repository.bean.NodeConfigFilterComparisonMethod;
import com.clustercontrol.repository.bean.NodeConfigFilterDataType;
import com.clustercontrol.repository.bean.NodeConfigFilterInfo;
import com.clustercontrol.repository.bean.NodeConfigFilterItem;
import com.clustercontrol.repository.bean.NodeConfigFilterItemInfo;
import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import com.clustercontrol.repository.model.NodeCpuHistoryDetail;
import com.clustercontrol.repository.model.NodeCpuInfo;
import com.clustercontrol.repository.model.NodeCustomHistoryDetail;
import com.clustercontrol.repository.model.NodeCustomInfo;
import com.clustercontrol.repository.model.NodeDiskHistoryDetail;
import com.clustercontrol.repository.model.NodeDiskInfo;
import com.clustercontrol.repository.model.NodeFilesystemHistoryDetail;
import com.clustercontrol.repository.model.NodeFilesystemInfo;
import com.clustercontrol.repository.model.NodeHostnameHistoryDetail;
import com.clustercontrol.repository.model.NodeHostnameInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeLicenseHistoryDetail;
import com.clustercontrol.repository.model.NodeLicenseInfo;
import com.clustercontrol.repository.model.NodeMemoryHistoryDetail;
import com.clustercontrol.repository.model.NodeMemoryInfo;
import com.clustercontrol.repository.model.NodeProductHistoryDetail;
import com.clustercontrol.repository.model.NodeProductInfo;
import com.clustercontrol.repository.model.NodeNetstatHistoryDetail;
import com.clustercontrol.repository.model.NodeNetstatInfo;
import com.clustercontrol.repository.model.NodeNetworkInterfaceHistoryDetail;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.model.NodeOsHistoryDetail;
import com.clustercontrol.repository.model.NodeOsInfo;
import com.clustercontrol.repository.model.NodePackageHistoryDetail;
import com.clustercontrol.repository.model.NodePackageInfo;
import com.clustercontrol.repository.model.NodeProcessInfo;
import com.clustercontrol.repository.model.NodeVariableHistoryDetail;
import com.clustercontrol.repository.model.NodeVariableInfo;
import com.clustercontrol.repository.util.NodeConfigFilterUtil;
import com.clustercontrol.repository.util.QueryUtil;

/**
 * ノード用プロパティを作成するクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class NodeProperty {

	private static Log m_log = LogFactory.getLog(NodeProperty.class);

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(NodeProperty.class.getName());
		
		try {
			_lock.writeLock();
			
			Map<String, NodeInfo> cache = getCache();
			if (cache == null) {	// not null when clustered
				init();
			}
		} catch (Throwable t) {
			m_log.error("NodeProperty initialisation error. " + t.getMessage(), t);
		} finally {
			_lock.writeUnlock();
		}
	}
	
	/** ----- 初期値キャッシュ ----- */
	
	@SuppressWarnings("unchecked")
	private static ConcurrentHashMap<String, NodeInfo> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_REPOSITORY_NODE);
		if (m_log.isTraceEnabled()) m_log.trace("get cache " + AbstractCacheManager.KEY_REPOSITORY_NODE + " : " + cache);
		return cache == null ? null : (ConcurrentHashMap<String, NodeInfo>)cache;
	}
	
	private static void storeCache(ConcurrentHashMap<String, NodeInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_REPOSITORY_NODE + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_REPOSITORY_NODE, newCache);
	}

	public static void removeNode (String facilityId) {
		m_log.info("remove NodeCache : " + facilityId);
		
		try {
			_lock.writeLock();
			
			ConcurrentHashMap<String, NodeInfo> cache = getCache();
			cache.remove(facilityId);
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	public static void updateNode (String facilityId) {
		m_log.info("update NodeCache : " + facilityId);
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			_lock.writeLock();
			
			ConcurrentHashMap<String, NodeInfo> cache = getCache();
			try {
				em.clear();
				NodeInfo facilityEntity = QueryUtil.getNodePK(facilityId);
				facilityEntity.getNodeDeviceInfo().addAll(QueryUtil.getNodeGeneralDeviceInfoByFacilityId(facilityId));
				facilityEntity.getNodeCpuInfo().addAll(QueryUtil.getNodeCpuInfoByFacilityId(facilityId));
				facilityEntity.getNodeMemoryInfo().addAll(QueryUtil.getNodeMemoryInfoByFacilityId(facilityId));
				facilityEntity.getNodeNetworkInterfaceInfo().addAll(QueryUtil.getNodeNetworkInterfaceInfoByFacilityId(facilityId));
				facilityEntity.getNodeDiskInfo().addAll(QueryUtil.getNodeDiskInfoByFacilityId(facilityId));
				facilityEntity.getNodeFilesystemInfo().addAll(QueryUtil.getNodeFilesystemInfoByFacilityId(facilityId));
				facilityEntity.getNodeHostnameInfo().addAll(QueryUtil.getNodeHostnameInfoByFacilityId(facilityId));
				facilityEntity.getNodeNoteInfo().addAll(QueryUtil.getNodeNoteInfoByFacilityId(facilityId));
				facilityEntity.getNodeVariableInfo().addAll(QueryUtil.getNodeVariableInfoByFacilityId(facilityId));
				cache.put(facilityId, facilityEntity);
			} catch (Exception e) {
				m_log.warn("update NodeCache failed : " + e.getMessage());
				//例外発生時は古い値がキャッシュに残らないように削除する
				cache.remove(facilityId);
			}
			storeCache(cache);
		} finally {
			_lock.writeUnlock();
		}
	}

	public static void init() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			_lock.writeLock();
			
			long startTime = System.currentTimeMillis();
			em.clear();
			
			ConcurrentHashMap<String, NodeInfo> cache = new ConcurrentHashMap<String, NodeInfo>();
			
			for (NodeInfo node : QueryUtil.getAllNode_NONE()) {
				node.getNodeDeviceInfo().addAll(QueryUtil.getNodeGeneralDeviceInfoByFacilityId(node.getFacilityId()));
				node.getNodeCpuInfo().addAll(QueryUtil.getNodeCpuInfoByFacilityId(node.getFacilityId()));
				node.getNodeMemoryInfo().addAll(QueryUtil.getNodeMemoryInfoByFacilityId(node.getFacilityId()));
				node.getNodeNetworkInterfaceInfo().addAll(QueryUtil.getNodeNetworkInterfaceInfoByFacilityId(node.getFacilityId()));
				node.getNodeDiskInfo().addAll(QueryUtil.getNodeDiskInfoByFacilityId(node.getFacilityId()));
				node.getNodeFilesystemInfo().addAll(QueryUtil.getNodeFilesystemInfoByFacilityId(node.getFacilityId()));
				node.getNodeHostnameInfo().addAll(QueryUtil.getNodeHostnameInfoByFacilityId(node.getFacilityId()));
				node.getNodeNoteInfo().addAll(QueryUtil.getNodeNoteInfoByFacilityId(node.getFacilityId()));
				node.getNodeVariableInfo().addAll(QueryUtil.getNodeVariableInfoByFacilityId(node.getFacilityId()));
				cache.put(node.getFacilityId(), node);
			}
			
			storeCache(cache);
			
			m_log.info("init cache " + (System.currentTimeMillis() - startTime) + "ms. size=" + cache.size());
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * 与えられたファシリティIDに基づき、該当するノード情報の存在有無を返す。
	 *
	 * @param facilityId ファシリティID
	 * @return true:存在する, false:存在しない
	 */
	public static boolean isContains(String facilityId) {
		m_log.debug("isContains() : facilityId = " + facilityId);

		boolean rtn = false;

		if (facilityId == null || facilityId.compareTo("") == 0) {
			return rtn;
		}

		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (ConcurrentHashMapの特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		ConcurrentHashMap<String, NodeInfo> cache = getCache();

		NodeInfo tmpNodeInfo = cache.get(facilityId);
		if (tmpNodeInfo != null) {
			if (!facilityId.equals(tmpNodeInfo.getFacilityId())) {
				// 試験中に怪しい挙動があったので、一応ログを仕込んでおく。
				m_log.error("cache is broken." + facilityId + "," + tmpNodeInfo.getFacilityId());
			}
			rtn = true;		// 存在する
		} else {
			try {
				QueryUtil.getNodePK(facilityId);
				rtn = true;	// 存在する
			} catch (FacilityNotFound e) {
				rtn = false;	// 存在しない
			}
		}
		return rtn;
	}

	/**
	 * 与えられたファシリティIDに基づき、該当するノード情報を返す。<BR>
	 * 以下の詳細情報を含む
	 * ・OS情報
	 * ・汎用デバイス情報
	 * ・CPU情報
	 * ・メモリ情報
	 * ・NIC情報
	 * ・ディスク情報
	 * ・ファイルシステム情報
	 * ・ホスト名情報
	 * ・備考情報
	 * ・ノード変数情報
	 *
	 * @param facilityId ファシリティID
	 * @return ノード情報
	 * @throws FacilityNotFound
	 */
	public static NodeInfo getProperty(String facilityId) throws FacilityNotFound {
		m_log.debug("getProperty() : start facilityId = " + facilityId);
		Long starttime = 0L;
		if (m_log.isDebugEnabled()) {
			starttime = new Date().getTime();
		}

		if (facilityId == null || facilityId.compareTo("") == 0) {
			return new NodeInfo();
		}

		{
			// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
			// (ConcurrentHashMapの特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
			ConcurrentHashMap<String, NodeInfo> cache = getCache();
			m_log.debug("getProperty() : getCache() success. facilityId=" + facilityId);
			NodeInfo nodeInfo = cache.get(facilityId);
			m_log.debug("getProperty() : cache get success. facilityId=" + facilityId);
			if (nodeInfo != null) {
				if (!facilityId.equals(nodeInfo.getFacilityId())) {
					// 試験中に怪しい挙動があったので、一応ログを仕込んでおく。
					m_log.error("getProperty() : cache is broken." + facilityId + "," + nodeInfo.getFacilityId());
				}
				return nodeInfo;
			}
		}
		NodeInfo nodeInfo = QueryUtil.getNodePK(facilityId);
		nodeInfo.setNodeDeviceInfo(QueryUtil.getNodeGeneralDeviceInfoByFacilityId(facilityId));
		nodeInfo.setNodeCpuInfo(QueryUtil.getNodeCpuInfoByFacilityId(facilityId));
		nodeInfo.setNodeMemoryInfo(QueryUtil.getNodeMemoryInfoByFacilityId(facilityId));
		nodeInfo.setNodeNetworkInterfaceInfo(QueryUtil.getNodeNetworkInterfaceInfoByFacilityId(facilityId));
		nodeInfo.setNodeDiskInfo(QueryUtil.getNodeDiskInfoByFacilityId(facilityId));
		nodeInfo.setNodeFilesystemInfo(QueryUtil.getNodeFilesystemInfoByFacilityId(facilityId));
		nodeInfo.setNodeHostnameInfo(QueryUtil.getNodeHostnameInfoByFacilityId(facilityId));
		nodeInfo.setNodeNoteInfo(QueryUtil.getNodeNoteInfoByFacilityId(facilityId));
		nodeInfo.setNodeVariableInfo(QueryUtil.getNodeVariableInfoByFacilityId(facilityId));
		m_log.debug("getProperty() : QueryUtil success. facilityId=" + facilityId);

		// ログ出力
		if (m_log.isTraceEnabled()) {
			m_log.trace("getProperty() return value : " + nodeInfo.toString());
		}
		if (m_log.isDebugEnabled()) {
			Long endtime = new Date().getTime();
			m_log.debug("getProperty() total time=" + (endtime - starttime) + "ms");
		}
		return nodeInfo;
	}

	/**
	 * 与えられたファシリティIDに基づき、該当するノード情報を返す。(詳細情報を含む)<BR>
	 *
	 * @param facilityId ファシリティID
	 * @return ノード情報
	 * @throws FacilityNotFound
	 */
	public static NodeInfo getPropertyFull(String facilityId) throws FacilityNotFound {
		m_log.debug("getPropertyFull() : start. facilityId = " + facilityId);

		NodeInfo nodeInfo = null;

		if (facilityId == null || facilityId.compareTo("") == 0) {
			return new NodeInfo();
		} else {
			NodeInfo tmpNodeInfo = getProperty(facilityId);
			nodeInfo = tmpNodeInfo.clone();
			if (nodeInfo != null) {
				nodeInfo.getNodeNetstatInfo().addAll(QueryUtil.getNodeNetstatInfoByFacilityId(facilityId));
				nodeInfo.getNodeProcessInfo().addAll(QueryUtil.getNodeProcessInfoByFacilityId(facilityId));
				nodeInfo.getNodePackageInfo().addAll(QueryUtil.getNodePackageInfoByFacilityId(facilityId));
				nodeInfo.getNodeProductInfo().addAll(QueryUtil.getNodeProductInfoByFacilityId(facilityId));
				nodeInfo.getNodeLicenseInfo().addAll(QueryUtil.getNodeLicenseInfoByFacilityId(facilityId));
				nodeInfo.getNodeCustomInfo().addAll(QueryUtil.getNodeCustomByFacilityId(facilityId));
				m_log.debug("getPropertyFull() : optional data get success. facilityId=" + facilityId);
			}
		}

		// ログ出力
		if (m_log.isTraceEnabled()) {
			m_log.trace("getPropertyFull() return value : " + nodeInfo.toString());
		}
		return nodeInfo;
	}

	/**
	 * 与えられたファシリティIDに基づき、指定された条件の最新のノード情報を返す。(詳細情報を含む)<BR>
	 *
	 * @param facilityId ファシリティID
	 * @param nodeFilterInfo 構成情報検索条件
	 * @return ノード情報
	 * @throws FacilityNotFound
	 */
	public static NodeInfo getPropertyFull(String facilityId, NodeInfo nodeFilterInfo) throws FacilityNotFound {
		m_log.debug("getPropertyFull() : facilityId = " + facilityId );

		if (facilityId == null || facilityId.compareTo("") == 0) {
			return new NodeInfo();
		}

		NodeInfo tmpNodeInfo = getProperty(facilityId);
		NodeInfo nodeInfo = tmpNodeInfo.clone();
		
		/** ネットワーク接続情報 */
		nodeInfo.setNodeNetstatInfo(QueryUtil.getNodeNetstatInfoByFacilityId(facilityId));
		/** プロセス情報 */
		nodeInfo.getNodeProcessInfo().addAll(QueryUtil.getNodeProcessInfoByFacilityId(facilityId));
		/** パッケージ情報 */
		nodeInfo.setNodePackageInfo(QueryUtil.getNodePackageInfoByFacilityId(facilityId));
		/** 個別導入製品情報 */
		nodeInfo.setNodeProductInfo(QueryUtil.getNodeProductInfoByFacilityId(facilityId));
		/** ライセンス情報 */
		nodeInfo.setNodeLicenseInfo(QueryUtil.getNodeLicenseInfoByFacilityId(facilityId));
		/** ユーザ任意情報 */
		nodeInfo.setNodeCustomInfo(QueryUtil.getNodeCustomByFacilityId(facilityId));
		
		// 構成情報検索条件によるノード情報検索
		nodeInfo = searchByNodeConfigFilterInfo(nodeInfo, nodeFilterInfo);

		return nodeInfo;
	}

	
	
	/**
	 * 与えられたファシリティIDに基づき、該当するノード情報を返す。(詳細情報指定)<BR>
	 *
	 * @param facilityId ファシリティID
	 * @param nodeConfigSettingItemList 構成情報取得対象一覧
	 * @return ノード情報
	 * @throws FacilityNotFound
	 */
	public static NodeInfo getPropertyFull(String facilityId, List<NodeConfigSettingItem> nodeConfigSettingItemList) throws FacilityNotFound {
		String strItem = "";
		if (m_log.isDebugEnabled()) {
			strItem = "";
			if (nodeConfigSettingItemList != null) {
				strItem = Arrays.toString(nodeConfigSettingItemList.toArray());
			}
		}
		m_log.debug("getPropertyFull() : start. facilityId = " + facilityId + ", nodeConfigSettingItem=" + strItem);

		NodeInfo nodeInfo = null;

		if (facilityId == null || facilityId.compareTo("") == 0) {
			return new NodeInfo();
		} else {
			NodeInfo tmpNodeInfo = getProperty(facilityId);
			nodeInfo = tmpNodeInfo.clone();
			if (nodeInfo != null && nodeConfigSettingItemList != null) {
				if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.NETSTAT)) {
					nodeInfo.getNodeNetstatInfo().addAll(QueryUtil.getNodeNetstatInfoByFacilityId(facilityId));
				}
				if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.PROCESS)) {
					nodeInfo.getNodeProcessInfo().addAll(QueryUtil.getNodeProcessInfoByFacilityId(facilityId));
				}
				if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.PACKAGE)) {
					nodeInfo.getNodePackageInfo().addAll(QueryUtil.getNodePackageInfoByFacilityId(facilityId));
				}
				if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.PRODUCT)) {
					nodeInfo.getNodeProductInfo().addAll(QueryUtil.getNodeProductInfoByFacilityId(facilityId));
				}
				if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.LICENSE)) {
					nodeInfo.getNodeLicenseInfo().addAll(QueryUtil.getNodeLicenseInfoByFacilityId(facilityId));
				}
				if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.CUSTOM)) {
					nodeInfo.getNodeCustomInfo().addAll(QueryUtil.getNodeCustomByFacilityId(facilityId));
				}
				m_log.debug("getPropertyFull() : optional data get success. facilityId=" + facilityId + ", nodeConfigSettingItem=" + strItem);
			}
		}

		// ログ出力
		if (m_log.isTraceEnabled()) {
			m_log.trace("getPropertyFull() return value : " + nodeInfo.toString() + ", nodeConfigSettingItem=" + strItem);
		}
		return nodeInfo;
	}

	/**
	 * 与えられたファシリティIDに基づき、指定された時点のノード情報を返す。(詳細情報を含む)<BR>
	 *
	 * @param facilityId ファシリティID
	 * @param targetDatetime 日時
	 * @param nodeFilterInfo 構成情報検索条件
	 * @return ノード情報
	 * @throws FacilityNotFound
	 */
	public static NodeInfo getPropertyFull(String facilityId, Long targetDatetime, NodeInfo nodeFilterInfo) throws FacilityNotFound {
		m_log.debug("getPropertyFull() : facilityId = " + facilityId + ", targetDatetime" + targetDatetime);

		if (facilityId == null || facilityId.compareTo("") == 0) {
			return new NodeInfo();
		}

		NodeInfo tmpNodeInfo = getProperty(facilityId);
		NodeInfo nodeInfo = tmpNodeInfo.clone();
		/** OS情報 */
		List<NodeOsHistoryDetail> osHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeOsHistoryDetail.class, facilityId, targetDatetime);
		// getProperty()で取得した情報を削除
		nodeInfo.setNodeOsInfo(new NodeOsInfo());
		if (osHistoryList.size() > 0) {
			NodeOsInfo info = new NodeOsInfo(facilityId);
			info.setOsName(osHistoryList.get(0).getOsName());
			info.setOsRelease(osHistoryList.get(0).getOsRelease());
			info.setOsVersion(osHistoryList.get(0).getOsVersion());
			info.setCharacterSet(osHistoryList.get(0).getCharacterSet());
			info.setStartupDateTime(osHistoryList.get(0).getStartupDateTime());
			info.setRegDate(null);
			info.setRegUser(null);
			info.setUpdateDate(osHistoryList.get(0).getRegDate());
			info.setUpdateUser(osHistoryList.get(0).getRegUser());
			nodeInfo.setNodeOsInfo(info);
		} else {
			// エラー
			m_log.warn("NodeOsInfo is not found. : " + facilityId + "=" + facilityId);
		}

		/** CPU情報 */
		List<NodeCpuHistoryDetail> cpuHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeCpuHistoryDetail.class, facilityId, targetDatetime);
		// getProperty()で取得した情報を削除
		nodeInfo.getNodeCpuInfo().clear();
		for (NodeCpuHistoryDetail history : cpuHistoryList) {
			NodeCpuInfo info = new NodeCpuInfo(facilityId, history.getDeviceIndex(), history.getDeviceType(), history.getDeviceName());
			info.setDeviceDisplayName(history.getDeviceDisplayName());
			info.setDeviceSize(history.getDeviceSize());
			info.setDeviceSizeUnit(history.getDeviceSizeUnit());
			info.setDeviceDescription(history.getDeviceDescription());
			info.setCoreCount(history.getCoreCount());
			info.setThreadCount(history.getThreadCount());
			info.setClockCount(history.getClockCount());
			info.setRegDate(null);
			info.setRegUser(null);
			info.setUpdateDate(history.getRegDate());
			info.setUpdateUser(history.getRegUser());
			nodeInfo.getNodeCpuInfo().add(info);
		}

		/** メモリ情報 */
		List<NodeMemoryHistoryDetail> memoryHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeMemoryHistoryDetail.class, facilityId, targetDatetime);
		// getProperty()で取得した情報を削除
		nodeInfo.getNodeMemoryInfo().clear();
		for (NodeMemoryHistoryDetail history : memoryHistoryList) {
			NodeMemoryInfo info = new NodeMemoryInfo(facilityId, history.getDeviceIndex(), history.getDeviceType(), history.getDeviceName());
			info.setDeviceDisplayName(history.getDeviceDisplayName());
			info.setDeviceSize(history.getDeviceSize());
			info.setDeviceSizeUnit(history.getDeviceSizeUnit());
			info.setDeviceDescription(history.getDeviceDescription());
			info.setRegDate(null);
			info.setRegUser(null);
			info.setUpdateDate(history.getRegDate());
			info.setUpdateUser(history.getRegUser());
			nodeInfo.getNodeMemoryInfo().add(info);
		}

		/** NIC情報 */
		List<NodeNetworkInterfaceHistoryDetail> nicHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeNetworkInterfaceHistoryDetail.class, facilityId, targetDatetime);
		// getProperty()で取得した情報を削除
		nodeInfo.getNodeNetworkInterfaceInfo().clear();
		for (NodeNetworkInterfaceHistoryDetail history : nicHistoryList) {
			NodeNetworkInterfaceInfo info = new NodeNetworkInterfaceInfo(facilityId, history.getDeviceIndex(), history.getDeviceType(), history.getDeviceName());
			info.setDeviceDisplayName(history.getDeviceDisplayName());
			info.setDeviceSize(history.getDeviceSize());
			info.setDeviceSizeUnit(history.getDeviceSizeUnit());
			info.setDeviceDescription(history.getDeviceDescription());
			info.setNicIpAddress(history.getNicIpAddress());
			info.setNicMacAddress(history.getNicMacAddress());
			info.setRegDate(null);
			info.setRegUser(null);
			info.setUpdateDate(history.getRegDate());
			info.setUpdateUser(history.getRegUser());
			nodeInfo.getNodeNetworkInterfaceInfo().add(info);
		}

		/** ディスク情報 */
		List<NodeDiskHistoryDetail> diskHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeDiskHistoryDetail.class, facilityId, targetDatetime);
		// getProperty()で取得した情報を削除
		nodeInfo.getNodeDiskInfo().clear();
		for (NodeDiskHistoryDetail history : diskHistoryList) {
			NodeDiskInfo info = new NodeDiskInfo(facilityId, history.getDeviceIndex(), history.getDeviceType(), history.getDeviceName());
			info.setDeviceDisplayName(history.getDeviceDisplayName());
			info.setDeviceSize(history.getDeviceSize());
			info.setDeviceSizeUnit(history.getDeviceSizeUnit());
			info.setDeviceDescription(history.getDeviceDescription());
			info.setDiskRpm(history.getDiskRpm());
			info.setRegDate(null);
			info.setRegUser(null);
			info.setUpdateDate(history.getRegDate());
			info.setUpdateUser(history.getRegUser());
			nodeInfo.getNodeDiskInfo().add(info);
		}

		/** ファイルシステム情報 */
		List<NodeFilesystemHistoryDetail> filesystemHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeFilesystemHistoryDetail.class, facilityId, targetDatetime);
		// getProperty()で取得した情報を削除
		nodeInfo.getNodeFilesystemInfo().clear();
		for (NodeFilesystemHistoryDetail history : filesystemHistoryList) {
			NodeFilesystemInfo info = new NodeFilesystemInfo(facilityId, history.getDeviceIndex(), history.getDeviceType(), history.getDeviceName());
			info.setDeviceDisplayName(history.getDeviceDisplayName());
			info.setDeviceSize(history.getDeviceSize());
			info.setDeviceSizeUnit(history.getDeviceSizeUnit());
			info.setDeviceDescription(history.getDeviceDescription());
			info.setFilesystemType(history.getFilesystemType());
			info.setRegDate(null);
			info.setRegUser(null);
			info.setUpdateDate(history.getRegDate());
			info.setUpdateUser(history.getRegUser());
			nodeInfo.getNodeFilesystemInfo().add(info);
		}

		/** ホスト名情報 */
		List<NodeHostnameHistoryDetail> hostnameHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeHostnameHistoryDetail.class, facilityId, targetDatetime);
		// getProperty()で取得した情報を削除
		nodeInfo.getNodeHostnameInfo().clear();
		for (NodeHostnameHistoryDetail history : hostnameHistoryList) {
			NodeHostnameInfo info = new NodeHostnameInfo(facilityId, history.getHostname());
			info.setRegDate(history.getRegDate());
			info.setRegUser(history.getRegUser());
			nodeInfo.getNodeHostnameInfo().add(info);
		}

		/** ノード変数情報 */
		List<NodeVariableHistoryDetail> variableHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeVariableHistoryDetail.class, facilityId, targetDatetime);
		// getProperty()で取得した情報を削除
		nodeInfo.getNodeVariableInfo().clear();
		for (NodeVariableHistoryDetail history : variableHistoryList) {
			NodeVariableInfo info = new NodeVariableInfo(facilityId, history.getNodeVariableName());
			info.setNodeVariableValue(history.getNodeVariableValue());
			info.setRegDate(null);
			info.setRegUser(null);
			info.setUpdateDate(history.getRegDate());
			info.setUpdateUser(history.getRegUser());
			nodeInfo.getNodeVariableInfo().add(info);
		}

		/** ネットワーク接続情報 */
		List<NodeNetstatHistoryDetail> netstatHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeNetstatHistoryDetail.class, facilityId, targetDatetime);
		for (NodeNetstatHistoryDetail history : netstatHistoryList) {
			NodeNetstatInfo info = new NodeNetstatInfo(
					facilityId, history.getProtocol(), history.getLocalIpAddress(), history.getLocalPort(), history.getForeignIpAddress(), history.getForeignPort(),
					history.getProcessName(), history.getPid());
			info.setStatus(history.getStatus());
			info.setRegDate(null);
			info.setRegUser(null);
			info.setUpdateDate(history.getRegDate());
			info.setUpdateUser(history.getRegUser());
			nodeInfo.getNodeNetstatInfo().add(info);
		}

		/** プロセス情報 */
		nodeInfo.getNodeProcessInfo().addAll(QueryUtil.getNodeProcessInfoByFacilityId(facilityId));

		/** パッケージ情報 */
		List<NodePackageHistoryDetail> packageHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodePackageHistoryDetail.class, facilityId, targetDatetime);
		for (NodePackageHistoryDetail history : packageHistoryList) {
			NodePackageInfo info = new NodePackageInfo(facilityId, history.getPackageId());
			info.setPackageName(history.getPackageName());
			info.setVersion(history.getVersion());
			info.setRelease(history.getRelease());
			info.setInstallDate(history.getInstallDate());
			info.setVendor(history.getVendor());
			info.setArchitecture(history.getArchitecture());
			info.setRegDate(null);
			info.setRegUser(null);
			info.setUpdateDate(history.getRegDate());
			info.setUpdateUser(history.getRegUser());

			nodeInfo.getNodePackageInfo().add(info);
		}

		/** 個別導入製品情報 */
		List<NodeProductHistoryDetail> productHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeProductHistoryDetail.class, facilityId, targetDatetime);
		for (NodeProductHistoryDetail history : productHistoryList) {
			NodeProductInfo info = new NodeProductInfo(facilityId, history.getProductName());
			info.setVersion(history.getVersion());
			info.setPath(history.getPath());
			info.setRegDate(null);
			info.setRegUser(null);
			info.setUpdateDate(history.getRegDate());
			info.setUpdateUser(history.getRegUser());
			nodeInfo.getNodeProductInfo().add(info);
		}

		/** ライセンス情報 */
		List<NodeLicenseHistoryDetail> licenseHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeLicenseHistoryDetail.class, facilityId, targetDatetime);
		for (NodeLicenseHistoryDetail history : licenseHistoryList) {
			NodeLicenseInfo info = new NodeLicenseInfo(facilityId, history.getProductName());
			info.setVendor(history.getVendor());
			info.setVendorContact(history.getVendorContact());
			info.setSerialNumber(history.getSerialNumber());
			info.setCount(history.getCount());
			info.setExpirationDate(history.getExpirationDate());
			info.setRegDate(null);
			info.setRegUser(null);
			info.setUpdateDate(history.getRegDate());
			info.setUpdateUser(history.getRegUser());
			nodeInfo.getNodeLicenseInfo().add(info);
		}

		/** ユーザ任意情報 */
		List<NodeCustomHistoryDetail> customHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeCustomHistoryDetail.class, facilityId, targetDatetime);
		for (NodeCustomHistoryDetail history : customHistoryList) {
			NodeCustomInfo info = new NodeCustomInfo(facilityId, history.getSettingId(), history.getSettingCustomId());
			info.setDisplayName(history.getDisplayName());
			info.setCommand(history.getCommand());
			info.setValue(history.getValue());
			info.setRegDate(null);
			info.setRegUser(null);
			info.setUpdateDate(history.getRegDate());
			info.setUpdateUser(history.getRegUser());
			nodeInfo.getNodeCustomInfo().add(info);
		}

		// 構成情報検索条件によるノード情報検索
		nodeInfo = searchByNodeConfigFilterInfo(nodeInfo, nodeFilterInfo);

		return nodeInfo;
	}

	/**
	 * 与えられたファシリティIDに基づき、指定された時点のノード情報を返す。(詳細情報指定)<BR>
	 *
	 * @param facilityId ファシリティID
	 * @param targetDatetime 日時
	 * @param nodeConfigSettingItemList 構成情報取得対象一覧
	 * @return ノード情報
	 * @throws FacilityNotFound
	 */
	public static NodeInfo getPropertyFull(String facilityId, Long targetDatetime, List<NodeConfigSettingItem> nodeConfigSettingItemList)
			throws FacilityNotFound {
		String strItem = "";
		if (m_log.isDebugEnabled()) {
			strItem = "";
			if (nodeConfigSettingItemList != null) {
				strItem = Arrays.toString(nodeConfigSettingItemList.toArray());
			}
		}
		m_log.debug("getPropertyFull() : facilityId = " + facilityId + ", targetDatetime=" 
				+ targetDatetime + ", nodeConfigSettingItem=" + strItem);

		if (facilityId == null || facilityId.compareTo("") == 0) {
			return new NodeInfo();
		}
		NodeInfo tmpNodeInfo = getProperty(facilityId);
		NodeInfo nodeInfo = tmpNodeInfo.clone();
		if (nodeInfo != null && nodeConfigSettingItemList != null) {
			/** OS情報 */
			if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.OS)) {
				List<NodeOsHistoryDetail> osHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeOsHistoryDetail.class, facilityId, targetDatetime);
				if (osHistoryList.size() > 0) {
					NodeOsInfo info = new NodeOsInfo(facilityId);
					info.setOsName(osHistoryList.get(0).getOsName());
					info.setOsRelease(osHistoryList.get(0).getOsRelease());
					info.setOsVersion(osHistoryList.get(0).getOsVersion());
					info.setCharacterSet(osHistoryList.get(0).getCharacterSet());
					info.setStartupDateTime(osHistoryList.get(0).getStartupDateTime());
					info.setRegDate(null);
					info.setRegUser(null);
					info.setUpdateDate(osHistoryList.get(0).getRegDate());
					info.setUpdateUser(osHistoryList.get(0).getRegUser());
					nodeInfo.setNodeOsInfo(info);
				}
			}
			/** CPU情報 */
			if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.HW_CPU)) {
				List<NodeCpuHistoryDetail> cpuHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeCpuHistoryDetail.class, facilityId, targetDatetime);
				// getProperty()で取得した情報を削除
				nodeInfo.getNodeCpuInfo().clear();
				for (NodeCpuHistoryDetail history : cpuHistoryList) {
					NodeCpuInfo info = new NodeCpuInfo(facilityId, history.getDeviceIndex(), history.getDeviceType(), history.getDeviceName());
					info.setDeviceDisplayName(history.getDeviceDisplayName());
					info.setDeviceSize(history.getDeviceSize());
					info.setDeviceSizeUnit(history.getDeviceSizeUnit());
					info.setDeviceDescription(history.getDeviceDescription());
					info.setCoreCount(history.getCoreCount());
					info.setThreadCount(history.getThreadCount());
					info.setClockCount(history.getClockCount());
					info.setRegDate(null);
					info.setRegUser(null);
					info.setUpdateDate(history.getRegDate());
					info.setUpdateUser(history.getRegUser());
					nodeInfo.getNodeCpuInfo().add(info);
				}
			}
			/** メモリ情報 */
			if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.HW_MEMORY)) {
				List<NodeMemoryHistoryDetail> memoryHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeMemoryHistoryDetail.class, facilityId, targetDatetime);
				// getProperty()で取得した情報を削除
				nodeInfo.getNodeMemoryInfo().clear();
				for (NodeMemoryHistoryDetail history : memoryHistoryList) {
					NodeMemoryInfo info = new NodeMemoryInfo(facilityId, history.getDeviceIndex(), history.getDeviceType(), history.getDeviceName());
					info.setDeviceDisplayName(history.getDeviceDisplayName());
					info.setDeviceSize(history.getDeviceSize());
					info.setDeviceSizeUnit(history.getDeviceSizeUnit());
					info.setDeviceDescription(history.getDeviceDescription());
					info.setRegDate(null);
					info.setRegUser(null);
					info.setUpdateDate(history.getRegDate());
					info.setUpdateUser(history.getRegUser());
					nodeInfo.getNodeMemoryInfo().add(info);
				}
			}
			/** NIC情報 */
			if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.HW_NIC)) {
				List<NodeNetworkInterfaceHistoryDetail> nicHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeNetworkInterfaceHistoryDetail.class, facilityId, targetDatetime);
				// getProperty()で取得した情報を削除
				nodeInfo.getNodeNetworkInterfaceInfo().clear();
				for (NodeNetworkInterfaceHistoryDetail history : nicHistoryList) {
					NodeNetworkInterfaceInfo info = new NodeNetworkInterfaceInfo(facilityId, history.getDeviceIndex(), history.getDeviceType(), history.getDeviceName());
					info.setDeviceDisplayName(history.getDeviceDisplayName());
					info.setDeviceSize(history.getDeviceSize());
					info.setDeviceSizeUnit(history.getDeviceSizeUnit());
					info.setDeviceDescription(history.getDeviceDescription());
					info.setNicIpAddress(history.getNicIpAddress());
					info.setNicMacAddress(history.getNicMacAddress());
					info.setRegDate(null);
					info.setRegUser(null);
					info.setUpdateDate(history.getRegDate());
					info.setUpdateUser(history.getRegUser());
					nodeInfo.getNodeNetworkInterfaceInfo().add(info);
				}
			}
			/** ディスク情報 */
			if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.HW_DISK)) {
				List<NodeDiskHistoryDetail> diskHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeDiskHistoryDetail.class, facilityId, targetDatetime);
				// getProperty()で取得した情報を削除
				nodeInfo.getNodeDiskInfo().clear();
				for (NodeDiskHistoryDetail history : diskHistoryList) {
					NodeDiskInfo info = new NodeDiskInfo(facilityId, history.getDeviceIndex(), history.getDeviceType(), history.getDeviceName());
					info.setDeviceDisplayName(history.getDeviceDisplayName());
					info.setDeviceSize(history.getDeviceSize());
					info.setDeviceSizeUnit(history.getDeviceSizeUnit());
					info.setDeviceDescription(history.getDeviceDescription());
					info.setDiskRpm(history.getDiskRpm());
					info.setRegDate(null);
					info.setRegUser(null);
					info.setUpdateDate(history.getRegDate());
					info.setUpdateUser(history.getRegUser());
					nodeInfo.getNodeDiskInfo().add(info);
				}
			}
			/** ファイルシステム情報 */
			if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.HW_FILESYSTEM)) {
				List<NodeFilesystemHistoryDetail> filesystemHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeFilesystemHistoryDetail.class, facilityId, targetDatetime);
				// getProperty()で取得した情報を削除
				nodeInfo.getNodeFilesystemInfo().clear();
				for (NodeFilesystemHistoryDetail history : filesystemHistoryList) {
					NodeFilesystemInfo info = new NodeFilesystemInfo(facilityId, history.getDeviceIndex(), history.getDeviceType(), history.getDeviceName());
					info.setDeviceDisplayName(history.getDeviceDisplayName());
					info.setDeviceSize(history.getDeviceSize());
					info.setDeviceSizeUnit(history.getDeviceSizeUnit());
					info.setDeviceDescription(history.getDeviceDescription());
					info.setFilesystemType(history.getFilesystemType());
					info.setRegDate(null);
					info.setRegUser(null);
					info.setUpdateDate(history.getRegDate());
					info.setUpdateUser(history.getRegUser());
					nodeInfo.getNodeFilesystemInfo().add(info);
				}
			}
			/** ホスト名情報 */
			if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.HOSTNAME)) {
				List<NodeHostnameHistoryDetail> hostnameHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeHostnameHistoryDetail.class, facilityId, targetDatetime);
				// getProperty()で取得した情報を削除
				nodeInfo.getNodeHostnameInfo().clear();
				for (NodeHostnameHistoryDetail history : hostnameHistoryList) {
					NodeHostnameInfo info = new NodeHostnameInfo(facilityId, history.getHostname());
					info.setRegDate(history.getRegDate());
					info.setRegUser(history.getRegUser());
					nodeInfo.getNodeHostnameInfo().add(info);
				}
			}
			/** ノード変数情報 */
			if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.NODE_VARIABLE)) {
				List<NodeVariableHistoryDetail> variableHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeVariableHistoryDetail.class, facilityId, targetDatetime);
				// getProperty()で取得した情報を削除
				nodeInfo.getNodeVariableInfo().clear();
				for (NodeVariableHistoryDetail history : variableHistoryList) {
					NodeVariableInfo info = new NodeVariableInfo(facilityId, history.getNodeVariableName());
					info.setNodeVariableValue(history.getNodeVariableValue());
					info.setRegDate(null);
					info.setRegUser(null);
					info.setUpdateDate(history.getRegDate());
					info.setUpdateUser(history.getRegUser());
					nodeInfo.getNodeVariableInfo().add(info);
				}
			}
			/** ネットワーク接続情報 */
			if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.NETSTAT)) {
				List<NodeNetstatHistoryDetail> netstatHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeNetstatHistoryDetail.class, facilityId, targetDatetime);
				for (NodeNetstatHistoryDetail history : netstatHistoryList) {
					NodeNetstatInfo info = new NodeNetstatInfo(
							facilityId, history.getProtocol(), history.getLocalIpAddress(), history.getLocalPort(), history.getForeignIpAddress(), history.getForeignPort(),
							history.getProcessName(), history.getPid());
					info.setStatus(history.getStatus());
					info.setRegDate(null);
					info.setRegUser(null);
					info.setUpdateDate(history.getRegDate());
					info.setUpdateUser(history.getRegUser());
					nodeInfo.getNodeNetstatInfo().add(info);
				}
			}
			/** プロセス情報 */
			if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.PROCESS)) {
				nodeInfo.getNodeProcessInfo().addAll(QueryUtil.getNodeProcessInfoByFacilityId(facilityId));
			}
			/** パッケージ情報 */
			if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.PACKAGE)) {
				List<NodePackageHistoryDetail> packageHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodePackageHistoryDetail.class, facilityId, targetDatetime);
				for (NodePackageHistoryDetail history : packageHistoryList) {
					NodePackageInfo info = new NodePackageInfo(facilityId, history.getPackageId());
					info.setPackageName(history.getPackageName());
					info.setVersion(history.getVersion());
					info.setRelease(history.getRelease());
					info.setInstallDate(history.getInstallDate());
					info.setVendor(history.getVendor());
					info.setArchitecture(history.getArchitecture());
					info.setRegDate(null);
					info.setRegUser(null);
					info.setUpdateDate(history.getRegDate());
					info.setUpdateUser(history.getRegUser());
					nodeInfo.getNodePackageInfo().add(info);
				}
			}
			/** 個別導入製品情報 */
			if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.PRODUCT)) {
				List<NodeProductHistoryDetail> productHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeProductHistoryDetail.class, facilityId, targetDatetime);
				for (NodeProductHistoryDetail history : productHistoryList) {
					NodeProductInfo info = new NodeProductInfo(facilityId, history.getProductName());
					info.setVersion(history.getVersion());
					info.setPath(history.getPath());
					info.setRegDate(null);
					info.setRegUser(null);
					info.setUpdateDate(history.getRegDate());
					info.setUpdateUser(history.getRegUser());
					nodeInfo.getNodeProductInfo().add(info);
				}
			}
			/** ライセンス情報 */
			if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.LICENSE)) {
				List<NodeLicenseHistoryDetail> licenseHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeLicenseHistoryDetail.class, facilityId, targetDatetime);
				for (NodeLicenseHistoryDetail history : licenseHistoryList) {
					NodeLicenseInfo info = new NodeLicenseInfo(facilityId, history.getProductName());
					info.setVendor(history.getVendor());
					info.setVendorContact(history.getVendorContact());
					info.setSerialNumber(history.getSerialNumber());
					info.setCount(history.getCount());
					info.setExpirationDate(history.getExpirationDate());
					info.setRegDate(null);
					info.setRegUser(null);
					info.setUpdateDate(history.getRegDate());
					info.setUpdateUser(history.getRegUser());
					nodeInfo.getNodeLicenseInfo().add(info);
				}
			}
			/** ユーザ任意情報 */
			if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.CUSTOM)) {
				List<NodeCustomHistoryDetail> customHistoryList = QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodeCustomHistoryDetail.class, facilityId, targetDatetime);
				for (NodeCustomHistoryDetail history : customHistoryList) {
					NodeCustomInfo info = new NodeCustomInfo(facilityId, history.getSettingId(), history.getSettingCustomId());
					info.setDisplayName(history.getDisplayName());
					info.setCommand(history.getCommand());
					info.setValue(history.getValue());
					info.setRegDate(null);
					info.setRegUser(null);
					info.setUpdateDate(history.getRegDate());
					info.setUpdateUser(history.getRegUser());
					nodeInfo.getNodeCustomInfo().add(info);
				}
			}
		}
		return nodeInfo;
	}

	/**
	 * ノード情報をListで返す<BR>
	 *
	 * @return ノードリスト
	 */
	public static List<NodeInfo> getAllListOrderByFacilityId() {
		m_log.debug("getAllListOrderByFacilityId()");
		List<NodeInfo> list = new ArrayList<>();
		{
			// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
			// (ConcurrentHashMapの特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
			List<String> sortedKeys = new ArrayList<>(getCache().keySet());
			Collections.sort(sortedKeys);

			for (String key : sortedKeys) {
				list.add(getCache().get(key));
			}
		}
		return list;
	}

	/**
	 * ノード情報をListで返す<BR>
	 *
	 * @return ノードリスト
	 */
	public static List<NodeInfo> getAllList() {
		m_log.debug("getAllList()");
		List<NodeInfo> list = null;
		{
			// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
			// (ConcurrentHashMapの特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
			list = new ArrayList<>(getCache().values());
		}
		return list;
	}
	
	/**
	 * 構成情報検索条件と一致するノード情報を検索する<BR>
	 *
	 * @param nodeInfo ノード情報
	 * @param nodeFilterInfo 構成情報検索条件
	 * @return ノード情報（検索結果を設定）
	 */
	private static NodeInfo searchByNodeConfigFilterInfo(NodeInfo nodeInfo, NodeInfo nodeFilterInfo) {

		// 検索条件が指定されている場合、強調表示設定をする
		HashMap<NodeConfigSettingItem, List<NodeConfigFilterInfo>> nodeConfigFilterMap = new HashMap<>();
		if (nodeFilterInfo != null && nodeFilterInfo.getNodeConfigFilterList() != null) {
			for (NodeConfigFilterInfo filterInfo : nodeFilterInfo.getNodeConfigFilterList()) {
				if (!filterInfo.getExists()) {
					continue;
				}
				if (!nodeConfigFilterMap.containsKey(filterInfo.getNodeConfigSettingItem())) {
					nodeConfigFilterMap.put(filterInfo.getNodeConfigSettingItem(), new ArrayList<>());
				}
				nodeConfigFilterMap.get(filterInfo.getNodeConfigSettingItem()).add(filterInfo);
			}
		}

		// 構成情報検索条件
		List<NodeConfigFilterInfo> filterList = null;

		/** OS情報 */
		filterList = nodeConfigFilterMap.get(NodeConfigSettingItem.OS);
		if (filterList != null) {
			NodeOsInfo osInfo = nodeInfo.getNodeOsInfo();
			for (NodeConfigFilterInfo filterInfo : filterList) {
				boolean isSearch = false;
				for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
					if (itemInfo.getItem() == NodeConfigFilterItem.OS_NAME) {
						isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(), itemInfo.getItemValue(),
								osInfo.getOsName());
					} else if (itemInfo.getItem() == NodeConfigFilterItem.OS_RELEASE) {
						isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(), itemInfo.getItemValue(),
								osInfo.getOsRelease());
					} else if (itemInfo.getItem() == NodeConfigFilterItem.OS_VERSION) {
						isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(), itemInfo.getItemValue(),
								osInfo.getOsVersion());
					} else if (itemInfo.getItem() == NodeConfigFilterItem.OS_CHARACTER_SET) {
						isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(), itemInfo.getItemValue(),
								osInfo.getCharacterSet());
					} else if (itemInfo.getItem() == NodeConfigFilterItem.OS_STARTUP_DATE_TIME) {
						isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(), itemInfo.getItemValue(),
								osInfo.getStartupDateTime());
					} else {
						// 通らない
						isSearch = Boolean.FALSE;
					}
					if (!isSearch) {
						break;
					}
				}
				if (isSearch) {
					osInfo.setSearchTarget(true);
					break;
				}
			}
			nodeInfo.setNodeOsInfo(osInfo);
		}

		/** CPU情報 */
		filterList = nodeConfigFilterMap.get(NodeConfigSettingItem.HW_CPU);
		if (filterList != null) {
			List<NodeCpuInfo> cpuInfoList = nodeInfo.getNodeCpuInfo();
			for (NodeCpuInfo info : cpuInfoList) {
				for (NodeConfigFilterInfo filterInfo : filterList) {
					boolean isSearch = false;
					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.CPU_DEVICE_NAME) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceName());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.CPU_DEVICE_DISPLAY_NAME) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceDisplayName());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.CPU_DEVICE_SIZE) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceSize());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.CPU_DEVICE_SIZE_UNIT) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceSizeUnit());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.CPU_DEVICE_DESCRIPTION) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceDescription());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.CPU_CORE_COUNT) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getCoreCount());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.CPU_THREAD_COUNT) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getThreadCount());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.CPU_CLOCK_COUNT) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getClockCount());
						} else {
							// 通らない
							isSearch = Boolean.FALSE;
						}
						if (!isSearch) {
							break;
						}
					}
					if (isSearch) {
						info.setSearchTarget(true);
						break;
					}
				}
			}
			nodeInfo.setNodeCpuInfo(cpuInfoList);
		}

		/** メモリ情報 */
		filterList = nodeConfigFilterMap.get(NodeConfigSettingItem.HW_MEMORY);
		if (filterList != null) {
			List<NodeMemoryInfo> memoryInfoList = nodeInfo.getNodeMemoryInfo();
			for (NodeMemoryInfo info : memoryInfoList) {
				for (NodeConfigFilterInfo filterInfo : filterList) {
					boolean isSearch = false;
					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.MEMORY_DEVICE_NAME) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceName());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.MEMORY_DEVICE_DISPLAY_NAME) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceDisplayName());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.MEMORY_DEVICE_SIZE) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceSize());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.MEMORY_DEVICE_SIZE_UNIT) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceSizeUnit());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.MEMORY_DEVICE_DESCRIPTION) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceDescription());
						} else {
							// 通らない
							isSearch = Boolean.FALSE;
						}
						if (!isSearch) {
							break;
						}
					}
					if (isSearch) {
						info.setSearchTarget(true);
						break;
					}
				}
			}
			nodeInfo.setNodeMemoryInfo(memoryInfoList);
		}

		/** NIC情報 */
		filterList = nodeConfigFilterMap.get(NodeConfigSettingItem.HW_NIC);
		if (filterList != null) {
			List<NodeNetworkInterfaceInfo> nicInfoList = nodeInfo.getNodeNetworkInterfaceInfo();
			for (NodeNetworkInterfaceInfo info : nicInfoList) {
				for (NodeConfigFilterInfo filterInfo : filterList) {
					boolean isSearch = false;
					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.NIC_DEVICE_NAME) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceName());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NIC_DEVICE_DISPLAY_NAME) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceDisplayName());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NIC_DEVICE_SIZE) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceSize());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NIC_DEVICE_SIZE_UNIT) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceSizeUnit());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NIC_DEVICE_DESCRIPTION) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceDescription());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NIC_IP_ADDRESS) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getNicIpAddress());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NIC_MAC_ADDRESS) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getNicMacAddress());
						} else {
							// 通らない
							isSearch = Boolean.FALSE;
						}
						if (!isSearch) {
							break;
						}
					}
					if (isSearch) {
						info.setSearchTarget(true);
						break;
					}
				}
			}
			nodeInfo.setNodeNetworkInterfaceInfo(nicInfoList);
		}

		/** ディスク情報 */
		filterList = nodeConfigFilterMap.get(NodeConfigSettingItem.HW_DISK);
		if (filterList != null) {
			List<NodeDiskInfo> diskInfoList = nodeInfo.getNodeDiskInfo();
			for (NodeDiskInfo info : diskInfoList) {
				for (NodeConfigFilterInfo filterInfo : filterList) {
					boolean isSearch = false;
					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.DISK_DEVICE_NAME) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceName());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.DISK_DEVICE_DISPLAY_NAME) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceDisplayName());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.DISK_DEVICE_SIZE) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceSize());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.DISK_DEVICE_SIZE_UNIT) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceSizeUnit());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.DISK_DEVICE_DESCRIPTION) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceDescription());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.DISK_RPM) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDiskRpm());
						} else {
							// 通らない
							isSearch = Boolean.FALSE;
						}
						if (!isSearch) {
							break;
						}
					}
					if (isSearch) {
						info.setSearchTarget(true);
						break;
					}
				}
			}
			nodeInfo.setNodeDiskInfo(diskInfoList);
		}

		/** ファイルシステム情報 */
		filterList = nodeConfigFilterMap.get(NodeConfigSettingItem.HW_FILESYSTEM);
		if (filterList != null) {
			List<NodeFilesystemInfo> filesystemList = nodeInfo.getNodeFilesystemInfo();
			for (NodeFilesystemInfo info : filesystemList) {
				for (NodeConfigFilterInfo filterInfo : filterList) {
					boolean isSearch = false;
					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.FILESYSTEM_DEVICE_NAME) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceName());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.FILESYSTEM_DEVICE_DISPLAY_NAME) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceDisplayName());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.FILESYSTEM_DEVICE_SIZE) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceSize());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.FILESYSTEM_DEVICE_SIZE_UNIT) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceSizeUnit());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.FILESYSTEM_DEVICE_DESCRIPTION) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDeviceDescription());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.FILESYSTEM_TYPE) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getFilesystemType());
						} else {
							// 通らない
							isSearch = Boolean.FALSE;
						}
						if (!isSearch) {
							break;
						}
					}
					if (isSearch) {
						info.setSearchTarget(true);
						break;
					}
				}
			}
			nodeInfo.setNodeFilesystemInfo(filesystemList);
		}

		/** ホスト名情報 */
		filterList = nodeConfigFilterMap.get(NodeConfigSettingItem.HOSTNAME);
		if (filterList != null) {
			List<NodeHostnameInfo> hostnameList = nodeInfo.getNodeHostnameInfo();
			for (NodeHostnameInfo info : hostnameList) {
				for (NodeConfigFilterInfo filterInfo : filterList) {
					boolean isSearch = false;
					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.HOSTNAME) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getHostname());
						} else {
							// 通らない
							isSearch = Boolean.FALSE;
						}
						if (!isSearch) {
							break;
						}
					}
					if (isSearch) {
						info.setSearchTarget(true);
						break;
					}
				}
			}
			nodeInfo.setNodeHostnameInfo(hostnameList);
		}

		/** ノード変数情報 */
		filterList = nodeConfigFilterMap.get(NodeConfigSettingItem.NODE_VARIABLE);
		if (filterList != null) {
			List<NodeVariableInfo> variableInfoList = nodeInfo.getNodeVariableInfo();
			for (NodeVariableInfo info : variableInfoList) {
				for (NodeConfigFilterInfo filterInfo : filterList) {
					boolean isSearch = false;
					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.NODE_VARIABLE_NAME) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getNodeVariableName());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NODE_VARIABLE_VALUE) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getNodeVariableValue());
						} else {
							// 通らない
							isSearch = Boolean.FALSE;
						}
						if (!isSearch) {
							break;
						}
					}
					if (isSearch) {
						info.setSearchTarget(true);
						break;
					}
				}
			}
			nodeInfo.setNodeVariableInfo(variableInfoList);
		}

		/** ネットワーク接続情報 */
		filterList = nodeConfigFilterMap.get(NodeConfigSettingItem.NETSTAT);
		if (filterList != null) {
			List<NodeNetstatInfo> netstatInfoList = nodeInfo.getNodeNetstatInfo();
			for (NodeNetstatInfo info : netstatInfoList) {
				for (NodeConfigFilterInfo filterInfo : filterList) {
					boolean isSearch = false;
					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.NETSTAT_PROTOCOL) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getProtocol());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NETSTAT_LOCAL_IP_ADDRESS) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getLocalIpAddress());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NETSTAT_LOCAL_PORT) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getLocalPort());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NETSTAT_FOREIGN_IP_ADDRESS) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getForeignIpAddress());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NETSTAT_FOREIGN_PORT) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getForeignPort());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NETSTAT_PROCESS_NAME) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getProcessName());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NETSTAT_PID) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getPid());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NETSTAT_STATUS) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getStatus());
						} else {
							// 通らない
							isSearch = Boolean.FALSE;
						}
						if (!isSearch) {
							break;
						}
					}
					if (isSearch) {
						info.setSearchTarget(true);
						break;
					}
				}
			}
			nodeInfo.setNodeNetstatInfo(netstatInfoList);
		}

		/** プロセス情報 */
		filterList = nodeConfigFilterMap.get(NodeConfigSettingItem.PROCESS);
		if (filterList != null) {
			List<NodeProcessInfo> processInfoList = nodeInfo.getNodeProcessInfo();
			for (NodeProcessInfo info : processInfoList) {
				for (NodeConfigFilterInfo filterInfo : filterList) {
					boolean isSearch = false;
					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.PROCESS_NAME) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getProcessName());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.PROCESS_PID) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getPid());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.PROCESS_PATH) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getPath());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.PROCESS_EXEC_USER) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getExecUser());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.PROCESS_STARTUP_DATE_TIME) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getStartupDateTime());
						} else {
							// 通らない
							isSearch = Boolean.FALSE;
						}
						if (!isSearch) {
							break;
						}
					}
					if (isSearch) {
						info.setSearchTarget(true);
						break;
					}
				}
			}
			nodeInfo.setNodeProcessInfo(processInfoList);
		}

		/** パッケージ情報 */
		filterList = nodeConfigFilterMap.get(NodeConfigSettingItem.PACKAGE);
		if (filterList != null) {
			List<NodePackageInfo> packageInfoList = nodeInfo.getNodePackageInfo();
			for (NodePackageInfo info : packageInfoList) {
				for (NodeConfigFilterInfo filterInfo : filterList) {
					boolean isSearch = false;
					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.PACKAGE_NAME) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getPackageName());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.PACKAGE_VERSION) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getVersion());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.PACKAGE_RELEASE) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getRelease());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.PACKAGE_INSTALL_DATE) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getInstallDate());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.PACKAGE_VENDOR) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getVendor());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.PACKAGE_ARCHITECTURE) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getArchitecture());
						} else {
							// 通らない
							isSearch = Boolean.FALSE;
						}
						if (!isSearch) {
							break;
						}
					}
					if (isSearch) {
						info.setSearchTarget(true);
						break;
					}
				}
			}
			nodeInfo.setNodePackageInfo(packageInfoList);
		}

		/** 個別導入製品情報 */
		filterList = nodeConfigFilterMap.get(NodeConfigSettingItem.PRODUCT);
		if (filterList != null) {
			List<NodeProductInfo> productInfoList = nodeInfo.getNodeProductInfo();
			for (NodeProductInfo info : productInfoList) {
				for (NodeConfigFilterInfo filterInfo : filterList) {
					boolean isSearch = false;
					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.PRODUCT_NAME) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getProductName());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.PRODUCT_VERSION) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getVersion());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.PRODUCT_PATH) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getPath());
						} else {
							// 通らない
							isSearch = Boolean.FALSE;
						}
						if (!isSearch) {
							break;
						}
					}
					if (isSearch) {
						info.setSearchTarget(true);
						break;
					}
				}
			}
			nodeInfo.setNodeProductInfo(productInfoList);
		}

		/** ライセンス情報 */
		filterList = nodeConfigFilterMap.get(NodeConfigSettingItem.LICENSE);
		if (filterList != null) {
			List<NodeLicenseInfo> licenseInfoList = nodeInfo.getNodeLicenseInfo();
			for (NodeLicenseInfo info : licenseInfoList) {
				for (NodeConfigFilterInfo filterInfo : filterList) {
					boolean isSearch = false;
					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.LICENSE_PRODUCT_NAME) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getProductName());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.LICENSE_VENDOR) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getVendor());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.LICENSE_VENDOR_CONTACT) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getVendorContact());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.LICENSE_SERIAL_NUMBER) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getSerialNumber());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.LICENSE_COUNT) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getCount());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.LICENSE_EXPIRATION_DATE) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getExpirationDate());
						} else {
							// 通らない
							isSearch = Boolean.FALSE;
						}
						if (!isSearch) {
							break;
						}
					}
					if (isSearch) {
						info.setSearchTarget(true);
						break;
					}
				}
			}
			nodeInfo.setNodeLicenseInfo(licenseInfoList);
		}

		/** ユーザ任意情報 */
		filterList = nodeConfigFilterMap.get(NodeConfigSettingItem.CUSTOM);
		if (filterList != null) {
			List<NodeCustomInfo> customInfoList = nodeInfo.getNodeCustomInfo();
			for (NodeCustomInfo info : customInfoList) {
				for (NodeConfigFilterInfo filterInfo : filterList) {
					boolean isSearch = false;
					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.CUSTOM_DISPLAY_NAME) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getDisplayName());
						} else if (itemInfo.getItem() == NodeConfigFilterItem.CUSTOM_VALUE) {
							isSearch = isSearchTarget(itemInfo.getItem(), itemInfo.getMethodType(),
									itemInfo.getItemValue(), info.getValue());
						} else {
							// 通らない
							isSearch = Boolean.FALSE;
						}
						if (!isSearch) {
							break;
						}
					}
					if (isSearch) {
						info.setSearchTarget(true);
						break;
					}
				}
			}
			nodeInfo.setNodeCustomInfo(customInfoList);
		}

		return nodeInfo;
	}

	/**
	 * 検索条件をもとにした判定
	 * 
	 * @param item 対象項目
	 * @param compareMethod 比較演算子
	 * @param compareValue 比較値
	 * @param value 値
	 * @return true:該当する, false:該当しない
	 */
	private static Boolean isSearchTarget(NodeConfigFilterItem item, NodeConfigFilterComparisonMethod compareMethod, Object compareValue, Object value) {
		Boolean rtn = Boolean.FALSE;

		if (compareValue == null) {
			if (value == null) {
				rtn = Boolean.TRUE;
			}
			return rtn;
		}
		if (item.dataType() == NodeConfigFilterDataType.STRING
				|| item.dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL
				|| item.dataType() == NodeConfigFilterDataType.STRING_VERSION) {
			if (!(compareValue instanceof String)) {
				return rtn;
			}
			String compareValueStr = (String)compareValue;
			String valueStr = (String)value;

			if (item.dataType() == NodeConfigFilterDataType.STRING) {
				Integer compareResult = valueStr.compareTo(compareValueStr);
				rtn =((compareMethod == NodeConfigFilterComparisonMethod.EQ && compareResult == 0)
					|| (compareMethod == NodeConfigFilterComparisonMethod.GE && compareResult >= 0)
					|| (compareMethod == NodeConfigFilterComparisonMethod.GT && compareResult > 0)
					|| (compareMethod == NodeConfigFilterComparisonMethod.LE && compareResult <= 0)
					|| (compareMethod == NodeConfigFilterComparisonMethod.LT && compareResult < 0)
					|| (compareMethod == NodeConfigFilterComparisonMethod.NE && compareResult != 0));

			} else if (item.dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) {
				rtn =((compareMethod == NodeConfigFilterComparisonMethod.EQ && valueStr.startsWith(compareValueStr))
						|| (compareMethod == NodeConfigFilterComparisonMethod.NE && !valueStr.startsWith(compareValueStr)));

			} else if (item.dataType() == NodeConfigFilterDataType.STRING_VERSION) {
				Integer compareResult = NodeConfigFilterUtil.compareVersion(valueStr, compareValueStr);
				rtn =((compareMethod == NodeConfigFilterComparisonMethod.EQ && compareResult == 0)
					|| (compareMethod == NodeConfigFilterComparisonMethod.GE && compareResult >= 0)
					|| (compareMethod == NodeConfigFilterComparisonMethod.GT && compareResult > 0)
					|| (compareMethod == NodeConfigFilterComparisonMethod.LE && compareResult <= 0)
					|| (compareMethod == NodeConfigFilterComparisonMethod.LT && compareResult < 0)
					|| (compareMethod == NodeConfigFilterComparisonMethod.NE && compareResult != 0));

			}
		} else if (item.dataType() == NodeConfigFilterDataType.INTEGER
				|| item.dataType() == NodeConfigFilterDataType.INTEGER_ONLYEQUAL) {
			if (!(compareValue instanceof Integer)) {
				return rtn;
			}
			Integer compareValueInt = (Integer)compareValue;
			Integer valueInt = (Integer)value;
			if (item.dataType() == NodeConfigFilterDataType.INTEGER) {
				Integer compareResult = valueInt.compareTo(compareValueInt);
				rtn =((compareMethod == NodeConfigFilterComparisonMethod.EQ && compareResult == 0)
						|| (compareMethod == NodeConfigFilterComparisonMethod.GE && compareResult >= 0)
						|| (compareMethod == NodeConfigFilterComparisonMethod.GT && compareResult > 0)
						|| (compareMethod == NodeConfigFilterComparisonMethod.LE && compareResult <= 0)
						|| (compareMethod == NodeConfigFilterComparisonMethod.LT && compareResult < 0)
						|| (compareMethod == NodeConfigFilterComparisonMethod.NE && compareResult != 0));

			} else if (item.dataType() == NodeConfigFilterDataType.INTEGER_ONLYEQUAL) {
				Integer compareResult = valueInt.compareTo(compareValueInt);
				rtn =((compareMethod == NodeConfigFilterComparisonMethod.EQ && compareResult == 0)
						|| (compareMethod == NodeConfigFilterComparisonMethod.NE && compareResult != 0));

			}
		} else if (item.dataType() == NodeConfigFilterDataType.DATETIME) {
			if (!(compareValue instanceof Long)) {
				return rtn;
			}
			Long compareValueDate = (Long)compareValue;
			Long valueDate = (Long)value;
			Integer compareResult = valueDate.compareTo(compareValueDate);
			rtn =((compareMethod == NodeConfigFilterComparisonMethod.EQ && compareResult == 0)
					|| (compareMethod == NodeConfigFilterComparisonMethod.GE && compareResult >= 0)
					|| (compareMethod == NodeConfigFilterComparisonMethod.GT && compareResult > 0)
					|| (compareMethod == NodeConfigFilterComparisonMethod.LE && compareResult <= 0)
					|| (compareMethod == NodeConfigFilterComparisonMethod.LT && compareResult < 0)
					|| (compareMethod == NodeConfigFilterComparisonMethod.NE && compareResult != 0));
		}
		return rtn;
	}
}
