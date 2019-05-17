/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.factory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.ObjectPrivilegeUtil;
import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.Ipv6Util;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.commons.util.ObjectValidator;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.nodemap.bean.ReservedFacilityIdConstant;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.plugin.impl.OsScopeInitializerPlugin;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.bean.FacilityTreeItem;
import com.clustercontrol.repository.bean.NodeConfigFilterInfo;
import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import com.clustercontrol.repository.bean.NodeConstant;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.NodeCpuInfo;
import com.clustercontrol.repository.model.NodeCustomInfo;
import com.clustercontrol.repository.model.NodeDiskInfo;
import com.clustercontrol.repository.model.NodeFilesystemInfo;
import com.clustercontrol.repository.model.NodeHostnameInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeLicenseInfo;
import com.clustercontrol.repository.model.NodeMemoryInfo;
import com.clustercontrol.repository.model.NodeProductInfo;
import com.clustercontrol.repository.model.NodeNetstatInfo;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.model.NodeOsInfo;
import com.clustercontrol.repository.model.NodePackageInfo;
import com.clustercontrol.repository.model.NodeProcessInfo;
import com.clustercontrol.repository.model.NodeVariableInfo;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.repository.util.FacilityUtil;
import com.clustercontrol.repository.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.xcloud.common.CloudConstants;

public class FacilitySelector {

	private static Log m_log = LogFactory.getLog(FacilitySelector.class);

	public static final String SEPARATOR = ">";

	private static ILock _hostnameIpaddrFacilityIdCacheLock;
	private static ILock _nodenameFacilityIdCacheLock;
	private static ILock _ipaddrFacilityIdCacheLock;
	private static ILock _hostnameFacilityIdCacheLock;
	private static ILock _scopeNodeFacilityIdCacheLock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_hostnameIpaddrFacilityIdCacheLock = lockManager.create(FacilitySelector.class.getName() + "-" + AbstractCacheManager.KEY_REPOSITORY_HOSTNAME_IPADDR_FACILITYID);
		_nodenameFacilityIdCacheLock = lockManager.create(FacilitySelector.class.getName() + "-" + AbstractCacheManager.KEY_REPOSITORY_NODENAME_FACILITYID);
		_ipaddrFacilityIdCacheLock = lockManager.create(FacilitySelector.class.getName() + "-" + AbstractCacheManager.KEY_REPOSITORY_IPADDR_FACILITYID);
		_hostnameFacilityIdCacheLock = lockManager.create(FacilitySelector.class.getName() + "-" + AbstractCacheManager.KEY_REPOSITORY_HOSTNAME_FACILITYID);
		_scopeNodeFacilityIdCacheLock = lockManager.create(FacilitySelector.class.getName() + "-" + AbstractCacheManager.KEY_REPOSITORY_SCOPE_NODE_FACILITYID);
		
		try {
			_hostnameIpaddrFacilityIdCacheLock.writeLock();
			refreshHostnameIpaddrFacilityIdCache();
		} finally {
			_hostnameIpaddrFacilityIdCacheLock.writeUnlock();
		}
		
		try {
			_nodenameFacilityIdCacheLock.writeLock();
			refreshNodenameFacilityIdCache();
		} finally {
			_nodenameFacilityIdCacheLock.writeUnlock();
		}
		
		try {
			_ipaddrFacilityIdCacheLock.writeLock();
			refreshIpaddrFacilityIdCache();
		} finally {
			_ipaddrFacilityIdCacheLock.writeUnlock();
		}
		
		try {
			_hostnameFacilityIdCacheLock.writeLock();
			refreshHostnameFacilityIdCache();
		} finally {
			_hostnameFacilityIdCacheLock.writeUnlock();
		}
		
		try {
			_scopeNodeFacilityIdCacheLock.writeLock();
			refreshScopeNodeFacilityIdCache();
		} finally {
			_scopeNodeFacilityIdCacheLock.writeUnlock();
		}
		
		m_log.info("Static Initialization [Thread : " + Thread.currentThread() + ", User : " + (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID) + "]");
	}
	
	/** ----- ファシリティの木情報のキャッシュ ----- */
	
	// getFacilityIdList(hostname, ipAddress)のためのキャッシュ
	@SuppressWarnings("unchecked")
	private static HashMap<String, ArrayList<String>> getHostnameIpaddrFacilityIdCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_REPOSITORY_HOSTNAME_IPADDR_FACILITYID);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_REPOSITORY_HOSTNAME_IPADDR_FACILITYID + " : " + cache);
		return cache == null ? null : (HashMap<String, ArrayList<String>>)cache;
	}
	
	private static void storeHostnameIpaddrFacilityIdCache(HashMap<String, ArrayList<String>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_REPOSITORY_HOSTNAME_IPADDR_FACILITYID + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_REPOSITORY_HOSTNAME_IPADDR_FACILITYID, newCache);
	}
	
	// <小文字ノード名, ファシリティIDのセット>（監視対象外となっているものは含まない。）
	@SuppressWarnings("unchecked")
	private static HashMap<String, HashSet<String>> getNodenameFacilityIdCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_REPOSITORY_NODENAME_FACILITYID);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_REPOSITORY_NODENAME_FACILITYID + " : " + cache);
		return cache == null ? null : (HashMap<String, HashSet<String>>)cache;
	}
	
	private static void storeNodenameFacilityIdCache(HashMap<String, HashSet<String>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_REPOSITORY_NODENAME_FACILITYID + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_REPOSITORY_NODENAME_FACILITYID, newCache);
	}
	
	// <IPアドレス, ファシリティIDのセット>（監視対象外となっているものは含まない。）
	@SuppressWarnings("unchecked")
	private static HashMap<InetAddress, HashSet<String>> getIpaddrFacilityIdCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_REPOSITORY_IPADDR_FACILITYID);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_REPOSITORY_IPADDR_FACILITYID + " : " + cache);
		return cache == null ? null : (HashMap<InetAddress, HashSet<String>>)cache;
	}
	
	private static void storeIpaddrFacilityIdCache(HashMap<InetAddress, HashSet<String>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_REPOSITORY_IPADDR_FACILITYID + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_REPOSITORY_IPADDR_FACILITYID, newCache);
	}

	// <ホスト名, ファシリティIDのセット>（監視対象外となっているものは含まない。）
	@SuppressWarnings("unchecked")
	private static HashMap<String, HashSet<String>> getHostnameFacilityIdCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_REPOSITORY_HOSTNAME_FACILITYID);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_REPOSITORY_HOSTNAME_FACILITYID + " : " + cache);
		return cache == null ? null : (HashMap<String, HashSet<String>>)cache;
	}
	
	private static void storeHostnameFacilityIdCache(HashMap<String, HashSet<String>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_REPOSITORY_HOSTNAME_FACILITYID + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_REPOSITORY_HOSTNAME_FACILITYID, newCache);
	}

	// containsFacilityIdメソッドのためのキャッシュ
	@SuppressWarnings("unchecked")
	private static HashMap<String, Boolean> getScopeNodeFacilityIdCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_REPOSITORY_SCOPE_NODE_FACILITYID);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_REPOSITORY_SCOPE_NODE_FACILITYID + " : " + cache);
		return cache == null ? null : (HashMap<String, Boolean>)cache;
	}
	
	private static void storeScopeNodeFacilityIdCache(HashMap<String, Boolean> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_REPOSITORY_SCOPE_NODE_FACILITYID + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_REPOSITORY_SCOPE_NODE_FACILITYID, newCache);
	}

	/**
	 * ファシリティの木情報のキャッシュを初期化する
	 */
	public static void initCacheFacilityTree() {
		m_log.info("initializing a facility's tree to cache.");
		
		refreshHostnameIpaddrFacilityIdCache();
		refreshNodenameFacilityIdCache();
		refreshIpaddrFacilityIdCache();
		refreshHostnameFacilityIdCache();
		refreshScopeNodeFacilityIdCache();
	}

	/**
	 * ノード情報の配列を取得する。<BR>
	 * <PRE>
	 * {
	 *    {facilityId1, facilityName1, description1},
	 *    {facilityId2, facilityName2, description2},
	 *    ...
	 * }
	 * </PRE>
	 * 
	 * @return ノード情報の配列
	 */
	public static ArrayList<NodeInfo> getNodeList() {

		/** ローカル変数 */
		ArrayList<NodeInfo> nodeList = null;

		/** メイン処理 */
		m_log.debug("getting a list of all nodes...");

		nodeList = new ArrayList<NodeInfo>();
		List<NodeInfo> nodes = QueryUtil.getAllNode();
		m_log.debug("getNodeList() : QueryUtil success");
		for (NodeInfo facility : nodes) {
			nodeList.add(copyToNodeInfo(facility));
		}

		m_log.debug("successful in getting a list of all nodes.");
		return nodeList;
	}

	/**
	 * 検索条件にマッチしたノード情報の配列を取得する。<BR>
	 * <PRE>
	 * {
	 *    {facilityId1, facilityName1, description1},
	 *    {facilityId2, facilityName2, description2},
	 *    ...
	 * }
	 * </PRE>
	 * 
	 * @param property 検索条件
	 * @return ノード情報配列
	 */
	public static ArrayList<NodeInfo> getFilterNodeList(NodeInfo property) {

		/** ローカル変数 */
		ArrayList<NodeInfo> nodeList = null;
		String facilityId = null;
		String facilityName = null;
		String description = null;
		String ipAddressV4 = null;
		String ipAddressV6 = null;
		String osName = null;
		String osRelease = null;
		String administrator = null;
		String contact = null;

		// 「含まない」検索を行うかの判断に使う値
		String notInclude = "NOT:";

		boolean facilityIdFlg = false;
		boolean facilityNameFlg = false;
		boolean descriptionFlg = false;
		boolean ipAddressV4Flg = false;
		boolean ipAddressV6Flg = false;
		boolean osNameFlg = false;
		boolean osReleaseFlg = false;
		boolean administratorFlg = false;
		boolean contactFlg = false;

		Collection<NodeInfo> nodeAll = null;

		/** メイン処理 */
		m_log.debug("getting a list of nodes by using filter...");

		// 検索条件の入力値の取得
		facilityId = property.getFacilityId();

		facilityName = property.getFacilityName();

		description = property.getDescription();

		ipAddressV4 = property.getIpAddressV4();

		ipAddressV6 = property.getIpAddressV6();

		if (property.getNodeOsInfo() != null) {
			osName = property.getNodeOsInfo().getOsName();
			osRelease = property.getNodeOsInfo().getOsRelease();
		}

		administrator = property.getAdministrator();

		contact = property.getContact();

		// 検索条件の有効確認
		if (! ObjectValidator.isEmptyString(facilityId)) {
			facilityIdFlg = true;
		}
		if (! ObjectValidator.isEmptyString(facilityName)) {
			facilityNameFlg = true;
		}
		if (! ObjectValidator.isEmptyString(description)) {
			descriptionFlg = true;
		}
		if (! ObjectValidator.isEmptyString(ipAddressV4)) {
			ipAddressV4Flg = true;
		}
		if (! ObjectValidator.isEmptyString(ipAddressV6)) {
			ipAddressV6Flg = true;
		}
		if (! ObjectValidator.isEmptyString(osName)) {
			osNameFlg = true;
		}
		if (! ObjectValidator.isEmptyString(osRelease)) {
			osReleaseFlg = true;
		}
		if (! ObjectValidator.isEmptyString(administrator)) {
			administratorFlg = true;
		}
		if (! ObjectValidator.isEmptyString(contact)) {
			contactFlg = true;
		}

		nodeAll = QueryUtil.getAllNode();

		nodeList = new ArrayList<NodeInfo>();
		if (nodeAll != null) {
			// 文字列が部分一致した場合、マッチしたノードとする
			for (NodeInfo node : nodeAll) {

				if(!facilityId.startsWith(notInclude)) {
					if (facilityIdFlg && node.getFacilityId().indexOf(facilityId) == -1) {
						continue;
					}
				} else {
					if (facilityIdFlg && node.getFacilityId().indexOf(facilityId.substring(notInclude.length())) != -1) {
						continue;
					}
				}
				if(!facilityName.startsWith(notInclude)) {
					if (facilityNameFlg && node.getFacilityName().indexOf(facilityName) == -1) {
						continue;
					}
				} else {
					if (facilityNameFlg && node.getFacilityName().indexOf(facilityName.substring(notInclude.length())) != -1) {
						continue;
					}
				}
				if (!description.startsWith(notInclude)) {
					if (descriptionFlg && node.getDescription().indexOf(description) == -1) {
						continue;
					}
				} else {
					if (descriptionFlg && node.getDescription().indexOf(description.substring(notInclude.length())) != -1) {
						continue;
					}
				}
				if (ipAddressV4Flg && node.getIpAddressV4().indexOf(ipAddressV4) == -1) {
					continue;
				}
				if (ipAddressV6Flg && node.getIpAddressV6().indexOf(ipAddressV6) == -1) {
					continue;
				}
				if (!osName.startsWith(notInclude)) {
					if (osNameFlg && node.getNodeOsInfo().getOsName().indexOf(osName) == -1) {
						continue;
					}
				} else {
					if (osNameFlg && node.getNodeOsInfo().getOsName().indexOf(osName.substring(notInclude.length())) != -1) {
						continue;
					}
				}
				if (!osRelease.startsWith(notInclude)) {
					if (osReleaseFlg && node.getNodeOsInfo().getOsRelease().indexOf(osRelease) == -1) {
						continue;
					}
				} else {
					if (osReleaseFlg && node.getNodeOsInfo().getOsRelease().indexOf(osRelease.substring(notInclude.length())) != -1) {
						continue;
					}
				}
				if (!administrator.startsWith(notInclude)) {
					if (administratorFlg && node.getAdministrator().indexOf(administrator) == -1) {
						continue;
					}
				} else {
					if (administratorFlg && node.getAdministrator().indexOf(administrator.substring(notInclude.length())) != -1) {
						continue;
					}
				}
				if (!contact.startsWith(notInclude)) {
					if (contactFlg && node.getContact().indexOf(contact) == -1) {
						continue;
					}
				} else {
					if (contactFlg && node.getContact().indexOf(contact.substring(notInclude.length())) != -1) {
						continue;
					}
				}

				nodeList.add(copyToNodeInfo(node));
			}
		}

		m_log.debug("successful in getting a list of nodes by using filter.");
		return nodeList;
	}

	/**
	 * 構成情報履歴検索処理
	 * 
	 * @param property 検索条件
	 * @param allNodeFacilityIdList ノード全件のファシリティID一覧
	 * @return ノードのファシリティID一覧
	 * @throws HinemosDbTimeout
	 */
	public static List<String> getFilterNodeIdListByNodeConfig(NodeInfo property, List<String> allNodeFacilityIdList)
		throws HinemosDbTimeout {
		HashSet<String> rtnSet = new HashSet<>();
		boolean isFirst = true;

		if (property.getNodeConfigFilterList() == null
				|| property.getNodeConfigFilterList().size() == 0) {
			// 検索条件未存在
			return new ArrayList<>();
		}
		
		for (NodeConfigFilterInfo filterInfo : property.getNodeConfigFilterList()) {
			if (filterInfo.getItemList() == null
					|| filterInfo.getItemList().size() == 0) {
				if (property.getNodeConfigFilterIsAnd()) {
					// データ未存在のため0件で処理終了
					return new ArrayList<>();
				} else {
					continue;
				}
			}
			List<String> list = QueryUtil.getNodeFacilityIdByNodeConfig(
					filterInfo, property.getNodeConfigTargetDatetime(), allNodeFacilityIdList);
			if (list == null || list.size() == 0) {
				if (property.getNodeConfigFilterIsAnd()) {
					// AND比較では結果がない場合は0件
					return new ArrayList<>();
				} else {
					continue;
				}
			}
			if (isFirst || !property.getNodeConfigFilterIsAnd()) {
				rtnSet.addAll(list);
				if (isFirst) {
					isFirst = false;
				}
			} else {
				// AND比較
				rtnSet.retainAll(list);
			}
		}
		return new ArrayList<>(rtnSet);
	}

	/**
	 * スコープ配下にあるノード情報の一覧を取得する。<BR>
	 * 
	 * @param parentFacilityId スコープのファシリティID
	 * @param ownerRoleId スコープのファシリティID
	 * @param level 取得する階層数
	 * @return ノード情報の一覧
	 */
	public static ArrayList<NodeInfo> getNodeList(String parentFacilityId, String ownerRoleId, int level) {
		/** ローカル変数 */
		ArrayList<NodeInfo> nodes = null;
		ArrayList<FacilityInfo> facilities = null;
		NodeInfo node = null;

		/** メイン処理 */
		facilities = getFacilityList(parentFacilityId, ownerRoleId, level, false);

		nodes = new ArrayList<NodeInfo>();
		if (facilities != null) {
			for (FacilityInfo facility : facilities) {
				// ノードの場合、配列に追加
				if (facility.getFacilityType() == FacilityConstant.TYPE_NODE) {
					try {
						node = QueryUtil.getNodePK(facility.getFacilityId(), ObjectPrivilegeMode.NONE);
					} catch (FacilityNotFound e) {
						m_log.warn("NodeEntity is not found. : facilityId = " + facility.getFacilityId());
					} catch (InvalidRole e) {
						// NONEのためここは通らない。
						m_log.warn("NodeEntity is invalid role. : facilityId = " + facility.getFacilityId());
					}
					nodes.add(copyToNodeInfo(node));
				}
			}
		}

		return nodes;
	}

	/**
	 * スコープ配下にあるファシリティIDの一覧を取得してソートする。<BR>
	 * parentFacilityIdがノードの場合は、そのノードのファシリティIDを返す。
	 * 
	 * @param parentFacilityId スコープのファシリティID
	 * @param ownerRoleId オーナーロールID
	 * @param level 取得する階層数
	 * @param sort ファシリティIDをソートする場合はtrue, ソートしない場合はfalse
	 * @return ファシリティIDの配列
	 * @throws FacilityNotFound
	 */
	public static ArrayList<String> getFacilityIdList(String parentFacilityId, String ownerRoleId, int level, boolean sort,
			boolean scopeFlag) {
		/** ローカル変数 */
		ArrayList<String> facilityIds = null;
		ArrayList<FacilityInfo> facilities = null;

		/** メイン処理 */
		facilities = getFacilityList(parentFacilityId, ownerRoleId, level, scopeFlag);

		facilityIds = new ArrayList<String>();
		if (facilities != null) {
			for (FacilityInfo facility : facilities) {
				facilityIds.add(facility.getFacilityId());
			}
		}

		if (sort) {
			Collections.sort(facilityIds);
		}

		return facilityIds;
	}

	/**
	 * スコープ配下にあるファシリティIDの一覧を有効/無効を指定して取得してソートする。<BR>
	 * parentFacilityIdが有効ノードの場合は、ノードのファシリティIDを返す。
	 * parentFacilityIdが無効ノードの場合は、空のArrayListを返す。
	 * 
	 * @param parentFacilityId スコープのファシリティID
	 * @param ownerRoleId オーナーロールID
	 * @param level 取得する階層数
	 * @param sort ファシリティIDをソートする場合はtrue, ソートしない場合はfalse
	 * @param validFlg 有効/無効の指定
	 * @return ファシリティIDの配列
	 * @throws FacilityNotFound
	 */
	public static ArrayList<String> getNodeFacilityIdList(String parentFacilityId, String ownerRoleId, int level, boolean sort, Boolean validFlg) {
		/** ローカル変数 */
		ArrayList<String> facilityIdList = null;
		ArrayList<FacilityInfo> facilityList = null;

		/** メイン処理 */
		facilityList = getFacilityList(parentFacilityId, ownerRoleId, level, false);

		facilityIdList = new ArrayList<String>();
		if (facilityList != null) {
			for (FacilityInfo facility : facilityList) {
				if (facility.getFacilityType() == FacilityConstant.TYPE_NODE) {
					if (validFlg == null
							|| validFlg.equals(facility.getValid())) {
						facilityIdList.add(facility.getFacilityId());
					}
				}
			}
		}

		if (sort) {
			Collections.sort(facilityIdList);
		}

		return facilityIdList;
	}

	/**
	 * 全てのノードのファシリティIDの一覧を取得する。<BR>
	 * 
	 * @param sort ファシリティIDをソートする場合はtrue, ソートしない場合はfalse
	 * @return ファシリティIDの配列
	 */
	public static ArrayList<String> getNodeFacilityIdList(boolean sort) {
		/** メイン処理 */
		ArrayList<String> facilityIdList = new ArrayList<String>();
		for (NodeInfo facility : QueryUtil.getAllNode()) {
			facilityIdList.add(facility.getFacilityId());
		}

		if (sort) {
			Collections.sort(facilityIdList);
		}
		return facilityIdList;
	}

	/**
	 * IPアドレスから該当するノードのファシリティID一覧を取得する。
	 * @param ipaddr IPアドレス(Inet4Address or Inet6Address)
	 * @return ファシリティIDのリスト
	 * @throws HinemosUnknown
	 */
	public static List<String> getFacilityIdByIpAddress(InetAddress ipaddr) throws HinemosUnknown {

		List<String> ret = new ArrayList<String>();

		if (ipaddr == null) {
			return ret;
		}

		try {
			String ipaddrStr = ipaddr.getHostAddress();
			if (m_log.isDebugEnabled()) {
				m_log.debug("finding node by ipaddress. (ipaddr = " + ipaddr + ")");
			}

			List<NodeInfo> facilities = null;
			if (ipaddr instanceof Inet4Address) {
				facilities = QueryUtil.getNodeByIpv4(ipaddrStr);
			}

			if (ipaddr instanceof Inet6Address) {
				facilities = QueryUtil.getNodeByIpv6(ipaddrStr);
			}

			if (facilities != null) {
				for (FacilityInfo facility : facilities) {
					ret.add(facility.getFacilityId());
				}
			}
		} catch (Exception e) {
			m_log.warn("unexpected internal error. (ipaddr = " + ipaddr + ") : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown("unexpected internal error. (ipaddr = " + ipaddr + ")", e);
		}

		return ret;
	}

	private static void refreshHostnameIpaddrFacilityIdCache() {
		try {
			_hostnameIpaddrFacilityIdCacheLock.writeLock();
			
			storeHostnameIpaddrFacilityIdCache(new HashMap<String, ArrayList<String>>());
		} finally {
			_hostnameIpaddrFacilityIdCacheLock.writeUnlock();
		}
	}
	
	/**
	 * ホスト名とIPv4アドレスを指定して、該当するノードのファシリティIDの一覧を取得する。<BR>
	 * 
	 * @param hostname ホスト名
	 * @param ipAddressV4 IPv4アドレス
	 * @return ファシリティIDの配列
	 */
	public static ArrayList<String> getFacilityIdList(String hostname, String ipAddress) {

		m_log.debug("getFacilityIdList() start : hostname = " + hostname +
				", ipAddress = " + ipAddress);

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			/** ローカル変数 */
			String key = hostname + "-," + ipAddress;
			
			try {
				_hostnameIpaddrFacilityIdCacheLock.readLock();
				
				HashMap<String, ArrayList<String>> cache = getHostnameIpaddrFacilityIdCache();
				ArrayList<String> facilityIds = cache.get(key);
				if (facilityIds != null) {
					return facilityIds;
				}
			} finally {
				_hostnameIpaddrFacilityIdCacheLock.readUnlock();
			}
			
			ArrayList<String> facilityIds = new ArrayList<String>();

			if (ipAddress == null || "".equals(ipAddress) ||
					hostname == null || "".equals(hostname)) {
				return facilityIds;
			}
			/** メイン処理 */
			try {
				_hostnameIpaddrFacilityIdCacheLock.writeLock();
				
				em.clear();
				// hostname変数のNodeプロパティのnodename(必須項目)をLowerCaseで検索
				List<NodeInfo> nodes = QueryUtil.getNodeByNodename(hostname);
				if (nodes != null){
					for (NodeInfo node : nodes){
						m_log.debug("getFacilityIdList() List " +
								" FacilityId = " + node.getFacilityId() +
								" NodeName = " + node.getNodeName() +
								" IpAddressV4 = " + node.getIpAddressV4() +
								" IpAddressV6 = " + node.getIpAddressV6());

						// IPv6とマッチ
						if(node.getIpAddressVersion() == 6) {
							if(Ipv6Util.expand(ipAddress).equals(
									Ipv6Util.expand(node.getIpAddressV6()))){
								m_log.debug("getFacilityIdList() hit facilityId = " + node.getFacilityId());
								facilityIds.add(node.getFacilityId());
							}
						} else {
							if(ipAddress.equals(node.getIpAddressV4())){
								m_log.debug("getFacilityIdList() hit facilityId = " + node.getFacilityId());
								facilityIds.add(node.getFacilityId());
							}
						}
					}
				}

				// Debugが有効の場合のみ取得したIPアドレスを表示させる
				if(m_log.isDebugEnabled()){
					for (Iterator<String> iter = facilityIds.iterator(); iter.hasNext();) {
						String facilityId = iter.next();
						m_log.debug("getFacilityIdList() hostname = " + hostname
								+ ", ipAddress = " + ipAddress + " has " + facilityId);
					}
				}
				
				HashMap<String, ArrayList<String>> cache = getHostnameIpaddrFacilityIdCache();
				cache.put(key, facilityIds);
				storeHostnameIpaddrFacilityIdCache(cache);
			} finally {
				_hostnameIpaddrFacilityIdCacheLock.writeUnlock();
			}
			return facilityIds;
		}
	}


	/**
	 * 検索条件にマッチするノードのファシリティIDの一覧を取得する。<BR>
	 * 
	 * @param condition 検索条件
	 * @return ファシリティIDの配列
	 * @throws HinemosUnknown
	 */
	public static ArrayList<String> getNodeFacilityIdListByCondition(HashMap<String, ?> condition) throws HinemosUnknown {
		/** ローカル変数 */
		ArrayList<NodeInfo> nodes = null;
		ArrayList<String> facilityIds = null;

		/** メイン処理 */
		nodes = getNodeByCondition(condition);
		facilityIds = new ArrayList<String>();
		if (nodes != null) {
			for (NodeInfo node : nodes) {
				facilityIds.add(node.getFacilityId());
			}
		}

		return facilityIds;
	}

	/**
	 * 検索条件にマッチするノードのファシリティインスタンスの一覧を取得する。<BR>
	 * 
	 * @param condition 検索条件
	 * @return ファシリティインスタンスの配列
	 * @throws HinemosUnknown
	 */
	private static ArrayList<NodeInfo> getNodeByCondition(HashMap<String, ?> condition) throws HinemosUnknown {

		m_log.debug("getNodeByCondition() : ");

		/** ローカル変数 */
		ArrayList<NodeInfo> nodes = null;
		String value = null;
		String valueTarget = null;
		List<NodeInfo> nodeAll = null;

		/** メイン処理 */
		m_log.debug("getting nodes by using filter...");

		// ノードの一覧を取得する
		nodeAll = QueryUtil.getAllNode();

		// 該当するノードの一覧を生成する
		nodes = new ArrayList<NodeInfo>();
		if (nodeAll != null) {
			for (NodeInfo node : nodeAll) {
				if (condition != null) {
					boolean matchFlg = true;

					for (Map.Entry<String, ?> entry : condition.entrySet()) {
						String attribute = entry.getKey();
						if (ObjectValidator.isEmptyString(attribute)) {
							continue;
						}
						value = (String)entry.getValue();
						if (value == null) {
							continue;
						}

						valueTarget = null;

						// ファシリティ関連
						if (attribute.compareTo(NodeConstant.FACILITY_ID) == 0) {
							valueTarget = node.getFacilityId();
						} else if(attribute.compareTo(NodeConstant.FACILITY_NAME) == 0) {
							valueTarget = node.getFacilityName();
						} else if(attribute.compareTo(NodeConstant.DESCRIPTION) == 0) {
							valueTarget = node.getDescription();
						} else if(attribute.compareTo(NodeConstant.DISPLAY_SORT_ORDER) == 0){
							valueTarget = node.getDisplaySortOrder().toString();
						} else if(attribute.compareTo(NodeConstant.VALID) == 0){
							// 旧格納データベースであるLDAPの物理格納形式に変換（VMオプション対応）
							if (node.getValid()) {
								valueTarget = Boolean.TRUE.toString().toUpperCase();
							} else {
								valueTarget = Boolean.FALSE.toString().toUpperCase();
							}
						} else if (attribute.compareTo(NodeConstant.ICONIMAGE) == 0) {
							valueTarget = node.getIconImage();
						} else if(attribute.compareTo(NodeConstant.CREATOR_NAME) == 0) {
							valueTarget = node.getCreateUserId();
						} else if(attribute.compareTo(NodeConstant.CREATE_TIME) == 0) {
							valueTarget = node.getCreateDatetime().toString();
						} else if(attribute.compareTo(NodeConstant.MODIFIER_NAME) == 0) {
							valueTarget = node.getModifyUserId();
						} else if(attribute.compareTo(NodeConstant.MODIFY_TIME) == 0) {
							valueTarget = node.getModifyDatetime().toString();
						}

						// HW
						else if(attribute.compareTo(NodeConstant.PLATFORM_FAMILY_NAME) == 0){
							valueTarget = node.getPlatformFamily();
						} else if(attribute.compareTo(NodeConstant.SUB_PLATFORM_FAMILY_NAME) == 0){
							valueTarget = node.getSubPlatformFamily();
						} else if(attribute.compareTo(NodeConstant.HARDWARE_TYPE) == 0){
							valueTarget = node.getHardwareType();
						}

						// IPアドレス関連
						else if(attribute.compareTo(NodeConstant.IP_ADDRESS_VERSION) == 0){
							valueTarget = node.getIpAddressVersion().toString();
						} else if(attribute.compareTo(NodeConstant.IP_ADDRESS_V4) == 0){
							valueTarget = node.getIpAddressV4();
						} else if(attribute.compareTo(NodeConstant.IP_ADDRESS_V6) == 0){
							valueTarget = node.getIpAddressV6();
						}

						// OS関連
						else if(attribute.compareTo(NodeConstant.NODE_NAME) == 0){
							valueTarget = node.getNodeName();
						} else if(attribute.compareTo(NodeConstant.OS_NAME) == 0){
							valueTarget = node.getNodeOsInfo().getOsName();
						} else if(attribute.compareTo(NodeConstant.OS_RELEASE) == 0){
							valueTarget = node.getNodeOsInfo().getOsRelease();
						} else if(attribute.compareTo(NodeConstant.OS_VERSION) == 0){
							valueTarget = node.getNodeOsInfo().getOsVersion();
						} else if(attribute.compareTo(NodeConstant.CHARACTER_SET) == 0){
							valueTarget = node.getNodeOsInfo().getCharacterSet();
						}

						// Hinemosエージェント関連
						else if(attribute.compareTo(NodeConstant.AGENT_AWAKE_PORT) == 0){
							valueTarget = node.getAgentAwakePort().toString();
						}

						// JOB
						else if(attribute.compareTo(NodeConstant.JOB_PRIORITY) == 0) {
							valueTarget = node.getJobPriority().toString();
						} else if (attribute.compareTo(NodeConstant.JOB_MULTIPLICITY) == 0) {
							valueTarget = node.getJobMultiplicity().toString();
						}

						// SNMP関連
						else if(attribute.compareTo(NodeConstant.SNMP_PORT) == 0){
							valueTarget = node.getSnmpPort().toString();
						} else if(attribute.compareTo(NodeConstant.SNMP_COMMUNITY) == 0){
							valueTarget = node.getSnmpCommunity();
						} else if(attribute.compareTo(NodeConstant.SNMP_VERSION) == 0){
							valueTarget = node.getSnmpVersion().toString();
						} else if(attribute.compareTo(NodeConstant.SNMPTIMEOUT) == 0){
							valueTarget = node.getSnmpTimeout().toString();
						} else if(attribute.compareTo(NodeConstant.SNMPRETRIES) == 0){
							valueTarget = node.getSnmpRetryCount().toString();
						}

						// WBEM関連
						else if(attribute.compareTo(NodeConstant.WBEM_USER) == 0){
							valueTarget = node.getWbemUser();
						} else if(attribute.compareTo(NodeConstant.WBEM_USER_PASSWORD) == 0){
							valueTarget = node.getWbemUserPassword();
						} else if(attribute.compareTo(NodeConstant.WBEM_PORT) == 0){
							valueTarget = node.getWbemPort().toString();
						} else if(attribute.compareTo(NodeConstant.WBEM_PROTOCOL) == 0){
							valueTarget = node.getWbemProtocol();
						} else if(attribute.compareTo(NodeConstant.WBEM_TIMEOUT) == 0){
							valueTarget = node.getWbemTimeout().toString();
						} else if(attribute.compareTo(NodeConstant.WBEM_RETRIES) == 0){
							valueTarget = node.getWbemRetryCount().toString();
						}

						// IPMI関連
						else if(attribute.compareTo(NodeConstant.IPMI_IP_ADDRESS) == 0){
							valueTarget = node.getIpmiIpAddress();
						} else if(attribute.compareTo(NodeConstant.IPMI_PORT) == 0){
							valueTarget = node.getIpmiPort().toString();
						} else if(attribute.compareTo(NodeConstant.IPMI_USER) == 0){
							valueTarget = node.getIpmiUser();
						} else if(attribute.compareTo(NodeConstant.IPMI_USER_PASSWORD) == 0){
							valueTarget = node.getIpmiUserPassword();
						} else if(attribute.compareTo(NodeConstant.IPMI_TIMEOUT) == 0){
							valueTarget = node.getIpmiTimeout().toString();
						} else if(attribute.compareTo(NodeConstant.IPMI_RETRIES) == 0){
							valueTarget = node.getIpmiRetries().toString();
						} else if(attribute.compareTo(NodeConstant.IPMI_PROTOCOL) == 0){
							valueTarget = node.getIpmiProtocol();
						} else if(attribute.compareTo(NodeConstant.IPMI_LEVEL) == 0){
							valueTarget = node.getIpmiLevel();
						}

						// SSH関連
						else if(attribute.compareTo(NodeConstant.SSH_USER) == 0) {
							valueTarget = node.getSshUser();
						} else if (attribute.compareTo(NodeConstant.SSH_USER_PASSWORD) == 0) {
							valueTarget = node.getSshUserPassword();
						} else if (attribute.compareTo(NodeConstant.SSH_PRIVATE_KEY_FILEPATH) == 0) {
							valueTarget = node.getSshPrivateKeyFilepath();
						} else if (attribute.compareTo(NodeConstant.SSH_PRIVATE_KEY_PASSPHRASE) == 0) {
							valueTarget = node.getSshPrivateKeyPassphrase();
						} else if (attribute.compareTo(NodeConstant.SSH_PORT) == 0) {
							valueTarget = node.getSshPort().toString();
						} else if (attribute.compareTo(NodeConstant.SSH_TIMEOUT) == 0) {
							valueTarget = node.getSshTimeout().toString();
						}
						
						// デバイス関連
						// 単一項目ではないため対象外とする

						// クラウド管理
						else if(attribute.compareTo(NodeConstant.CLOUDSERVICE) == 0){
							valueTarget = node.getCloudService();
						} else if(attribute.compareTo(NodeConstant.CLOUDSCOPE) == 0){
							valueTarget = node.getCloudScope();
						} else if(attribute.compareTo(NodeConstant.CLOUDRESOURCETYPE) == 0){
							valueTarget = node.getCloudResourceType();
						} else if(attribute.compareTo(NodeConstant.CLOUDRESOURCENAME) == 0){
							valueTarget = node.getCloudResourceName();
						} else if(attribute.compareTo(NodeConstant.CLOUDRESOURCEID) == 0){
							valueTarget = node.getCloudResourceId();
						} else if(attribute.compareTo(NodeConstant.CLOUDLOCATION) == 0){
							valueTarget = node.getCloudLocation();
						}

						// 管理関連
						// 単一項目ではないため対象外とする

						// 管理関連
						else if(attribute.compareTo(NodeConstant.ADMINISTRATOR) == 0){
							valueTarget = node.getAdministrator();
						} else if(attribute.compareTo(NodeConstant.CONTACT) == 0){
							valueTarget = node.getContact();
						}

						else {
							m_log.info("a filter's attribute is invalid. (attribute = " + attribute + ")");
							throw new HinemosUnknown("a filter's attribute is invalid. (attribute = " + attribute + ")");
						}

						// 文字列としての値の比較
						if (valueTarget == null || value.compareTo(valueTarget) != 0) {
							m_log.debug("a node's attribute is not equal. (attribute = " + attribute + ", value = " + valueTarget + ", compareValue = " + value + ")");
							matchFlg = false;
							break;
						}
					}

					if (matchFlg) {
						m_log.debug("a node is matched. (facilityId = " + node.getFacilityId() + ")");
						nodes.add(node);
					}
				}
			}
		}

		m_log.debug("successful in getting nodes by using filter.");
		return nodes;
	}

	/**
	 * 特定のスコープ配下の直下に属するファシリティの一覧を取得する。<BR>
	 * 
	 * @param parentFacilityId スコープのファシリティID
	 * @return ファシリティインスタンスの配列
	 */
	public static ArrayList<FacilityInfo> getFacilityListAssignedScope(String parentFacilityId) {
		/** ローカル変数 */
		ArrayList<FacilityInfo> facilityList = null;
		FacilityInfo scope = null;

		/** メイン処理 */
		m_log.debug("getting a list of facilities under a scope...");

		try {
			facilityList = new ArrayList<FacilityInfo>();

			if (ObjectValidator.isEmptyString(parentFacilityId)) {
				// コンポジットアイテムが選択された場合
				for (ScopeInfo rootScope : getRootScopeList()) {
					try {
						facilityList.add(copyToFacilityInfo(rootScope));
					} catch (Exception e) {
						m_log.warn("facilityToArrayList : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					}
				}
			} else {
				// スコープが選択された場合
				scope = FacilityTreeCache.getFacilityInfo(parentFacilityId);
				if(scope.getFacilityType() == FacilityConstant.TYPE_SCOPE) {
					List<FacilityInfo> childFacilityInfos = FacilityTreeCache.getChildFacilityInfoList(scope.getFacilityId());
					if (childFacilityInfos != null) {
						facilityList.addAll(childFacilityInfos);
					}
				}
			}
		} catch (FacilityNotFound e) {
			// 何もしない
		}

		m_log.debug("successful in getting a list of facilities under a scope...");
		return facilityList;
	}

	/**
	 * ノードが属するスコープのパス名の一覧を取得する。<BR>
	 * 
	 * @param facilityId ノードのファシリティID
	 * @return スコープのパス名の配列
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 */
	public static ArrayList<String> getNodeScopeList(String facilityId) throws FacilityNotFound, InvalidRole {

		/** ローカル変数 */
		ArrayList<String> scopePathList = null;
		FacilityInfo facility = null;

		/** メイン処理 */
		m_log.debug("getting scope paths of a node...");

		facility = QueryUtil.getFacilityPK(facilityId);

		scopePathList = new ArrayList<String>();
		if (facility != null) {
			List<FacilityInfo> parentFacilityInfos = FacilityTreeCache.getParentFacilityInfo(facilityId);
			if (parentFacilityInfos != null) {
				for (FacilityInfo parent : parentFacilityInfos) {
					scopePathList.add(getNodeScopePathRecursive(parent, null));
				}
			}
		}

		m_log.debug("successful in getting scope paths of a node.");
		return scopePathList;
	}

	/**
	 * ファシリティのパス名を取得する。<BR>
	 * 
	 * @param parentFacilityId 起点となるファシリティのファシリティID
	 * @param facilityId パスを取得するファシリティID
	 * @return ファシリティのパス名
	 */
	public static String getNodeScopePath(String parentFacilityId, String facilityId) {

		/** ローカル変数 */
		FacilityInfo info = null;
		FacilityInfo parentInfo = null;
		String path = null;

		/** メイン処理 */
		m_log.debug("getting a scope path of a facility's relation..." + "facilityId : " + facilityId + " , parentFacilityId : " + parentFacilityId);

		path = "";

		if (! ObjectValidator.isEmptyString(facilityId)) {
			info = FacilityTreeCache.getFacilityInfo(facilityId);
			if (info != null) {
				if (! ObjectValidator.isEmptyString(parentFacilityId)) {
					parentInfo = FacilityTreeCache.getFacilityInfo(parentFacilityId);
				}
				path = getNodeScopePathRecursive(info, parentInfo);
				if (ObjectValidator.isEmptyString(path)) {
					path = SEPARATOR;
				}
			}
		}

		m_log.debug("successful in getting a scope path of a facility's relation.");
		return path;
	}

	/**
	 * ファシリティ関連インスタンスに属するファシリティの相対パス名を取得する。<BR>
	 * スコープの場合は「facilityName1>facilityName2>facilityName3」、ノードの場合は「facilityName」となる。<BR>
	 * 
	 * @param info パスを取得するファシリティインスタンス
	 * @param parentInfo 相対パスを取得する場合は起点となるファシリティインスタンス, 絶対パスを取得する場合はnull
	 * @return ファシリティのパス名
	 */
	private static String getNodeScopePathRecursive(FacilityInfo info, FacilityInfo parentInfo) {
		/** ローカル変数 */
		String path = null;

		/** メイン処理 */
		if (FacilityUtil.isNode_FacilityInfo(info)) {
			// ノードの場合、ファシリティ名を返す
			return info.getFacilityName();
		}

		if (parentInfo != null && info.getFacilityId().compareTo(parentInfo.getFacilityId()) == 0) {
			// 起点となるファシリティと一致していた場合、区切り文字だけ返す
			return "";
		}

		List<FacilityInfo> parentFacilityInfos = FacilityTreeCache.getParentFacilityInfo(info.getFacilityId());
		if (parentFacilityInfos == null || parentFacilityInfos.size() == 0) {
			// ルートスコープの場合
			if (parentInfo == null) {
				return "";
			}
		} else {
			// 再帰的にスコープのパス名を生成する
			for (FacilityInfo parent : parentFacilityInfos) {
				if (parentInfo == null) {
					// 絶対パス名を取得する場合
					path = getNodeScopePathRecursive(parent, null) + info.getFacilityName() + SEPARATOR;
				} else if (info.getFacilityId().compareTo(parentInfo.getFacilityId()) != 0) {
					// 相対パス名を取得する場合
					path = getNodeScopePathRecursive(parent, parentInfo) + info.getFacilityName() + SEPARATOR;
				}

				// スコープ-スコープ間の関連は単一であるため、ループを抜ける
				break;
			}
		}

		return path;
	}

	/**
	 * ノードが属するスコープのファシリティIDの一覧を取得する。<BR>
	 * 
	 * @param facilityIds ノードのファシリティIDリスト
	 * @return スコープのファシリティIDリスト
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 */
	public static ArrayList<String> getNodeScopeIdList(String facilityId) throws FacilityNotFound, InvalidRole {

		/** ローカル変数 */
		HashSet<String> scopeIdSet = new HashSet<>();

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			FacilityInfo facilityInfo = QueryUtil.getFacilityPK(facilityId);
			if (facilityInfo != null) {
				List<FacilityInfo> parentFacilityInfoList = FacilityTreeCache.getParentFacilityInfo(facilityId);
				if (parentFacilityInfoList != null) {
					for (FacilityInfo parent : parentFacilityInfoList) {
						getParentFacilityIdRecursive(parent, scopeIdSet);
					}
				}
			}
		}
		// 自身も設定する
		scopeIdSet.add(facilityId);
		return new ArrayList<>(scopeIdSet);
	}

	/**
	 * ノードが所属するスコープのファシリティIDの一覧を取得する。<BR>
	 * 
	 * @param info 親スコープを取得するファシリティ情報
	 * @param scopeIdSet スコープIDを反映するHashSet
	 */
	private static void getParentFacilityIdRecursive(FacilityInfo info, HashSet<String> scopeIdSet) {

		if (info == null) {
			return;
		}
		if (FacilityUtil.isNode_FacilityInfo(info)) {
			// ノードの場合は、空文字を返す。
			return;
		}

		scopeIdSet.add(info.getFacilityId());

		// さらに親を取得する。
		List<FacilityInfo> parentFacilityInfoList = FacilityTreeCache.getParentFacilityInfo(info.getFacilityId());
		if (parentFacilityInfoList != null) {
			for (FacilityInfo facilityInfo : parentFacilityInfoList) {
				getParentFacilityIdRecursive(facilityInfo, scopeIdSet);
			}
		}
	}

	/**
	 * 指定したFacilityId配下のTreeItemを取得します
	 * 
	 * @param facilityId
	 * @param locale
	 * @param scopeOnly
	 * @param validFlg
	 * @return
	 */
	public static FacilityTreeItem getFacilityTree(String facilityId, Locale locale, boolean scopeOnly, Boolean validFlg, String ownerRoleId) {
		FacilityTreeItem top = null;
		FacilityTreeItem originalFacilityTree = getFacilityTree(locale, scopeOnly, validFlg, ownerRoleId);

		// ツリーのコピーを作成する
		FacilityTreeItem facilityTree = null;
		facilityTree = originalFacilityTree.clone();

		// 指定のファシリティID以下のツリーを取得する
		FacilityTreeItem subFacilityTree = selectFacilityTreeItem(facilityTree, facilityId);

		if(subFacilityTree == null){
			return null;
		}

		//FacilityTreeの最上位インスタンスを作成
		FacilityInfo info = new FacilityInfo();
		info.setFacilityId(ReservedFacilityIdConstant.ROOT_SCOPE);
		info.setFacilityName(MessageConstant.ROOT.getMessage());
		info.setFacilityType(FacilityConstant.TYPE_COMPOSITE);
		top = new FacilityTreeItem(null, info);

		// 取得したファシリティツリーをコンポジットアイテムに繋ぐ
		subFacilityTree.setParent(top);
		top.addChildren(subFacilityTree);

		return top;
	}


	/**
	 * ファシリティツリーの中で指定のファシリティIDを持つファシリティを再帰的に探します
	 * 
	 * @param facilityTree 対象のファシリティツリー
	 * @param facilityId パスを取得したいファシリティのファシリティID
	 * @return ファシリティ情報
	 */
	private static FacilityTreeItem selectFacilityTreeItem(FacilityTreeItem facilityTree, String facilityId){
		if(facilityTree.getData().getFacilityId().equals(facilityId)){
			return facilityTree;
		} else {
			for(int i=0; i<facilityTree.getChildrenArray().length; i++){
				FacilityTreeItem target = facilityTree.getChildrenArray()[i];
				FacilityTreeItem temp = selectFacilityTreeItem(target, facilityId);  // 再帰的
				if(temp != null){
					return temp;
				}
			}
		}
		return null;
	}

	/**
	 * ノードの木構造を取得する。<BR>
	 * 
	 * @param locale ロケール情報
	 * @param ownerRoleId オーナーロールID
	 * @return ファシリティの木構造
	 */
	public static FacilityTreeItem getNodeFacilityTree(Locale locale, String ownerRoleId) {
		/** ローカル変数 */
		FacilityTreeItem rootTree = null;
		FacilityInfo rootInfo = null;

		/** メイン処理 */
		m_log.debug("getting tree data of node facilities...");

		// 木構造最上位インスタンスの生成
		rootInfo = new FacilityInfo();
		rootInfo.setFacilityId(ReservedFacilityIdConstant.ROOT_SCOPE);
		rootInfo.setFacilityName(MessageConstant.ROOT.getMessage());
		rootInfo.setFacilityType(FacilityConstant.TYPE_COMPOSITE);
		rootInfo.setNotReferFlg(Boolean.TRUE);
		rootTree = new FacilityTreeItem(null, rootInfo);

		try {
			// オブジェクト権限で参照可能なノード一覧を取得する
			List<FacilityInfo> nodeInfoList = null;
			if (ownerRoleId != null && !ownerRoleId.isEmpty()) {
				// オーナーロールでオブジェクト権限チェック
				nodeInfoList = FacilityTreeCache.getNodeFacilityInfoListByRoleId(ownerRoleId);
			} else {
				// ログインユーザでオブジェクト権限チェック
				String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
				nodeInfoList = FacilityTreeCache.getNodeFacilityInfoListByUserId(userId);
			}

			for (FacilityInfo nodeInfo : nodeInfoList) {
				new FacilityTreeItem(rootTree, nodeInfo);
			}
		} catch (Exception e) {
			m_log.warn("getFacilityTree() failure to get a tree data of facilities. : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}

		m_log.debug("successful in getting tree data of facilities.");
		return rootTree;
	}

	/**
	 * ファシリティの木構造を取得する。<BR>
	 * 
	 * @param locale ロケール情報
	 * @param scopeOnly スコープのみの場合はtrue, 全てのファシリティの場合はfalse
	 * @param validFlg 有効/無効（nullの場合は全てのノード）
	 * @param ownerRoleId オーナーロールID（設定した場合はオーナーロールIDでのオブジェクト権限チェックを行う。）
	 * @return ファシリティの木構造
	 */
	public static FacilityTreeItem getFacilityTree(Locale locale, boolean scopeOnly, Boolean validFlg, String ownerRoleId) {
		/** ローカル変数 */
		FacilityTreeItem rootTree = null;

		/** メイン処理 */
		m_log.debug("getting tree data of facilities...");

		// ユーザもしくはロールが参照可能なファシリティツリーを返す
		if (ownerRoleId == null || ownerRoleId.isEmpty()) {
			// オーナーロールが設定されていない場合はユーザ
			String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			rootTree = FacilityTreeCache.getFacilityTreeByUserId(userId);
		} else {
			// オーナーロール
			rootTree = FacilityTreeCache.getFacilityTreeByRoleId(ownerRoleId);
		}

		try {
			FacilityTreeItem[] facilityTreeItemChildren = rootTree.getChildrenArray();
			if (facilityTreeItemChildren != null) {
				for (FacilityTreeItem facilityTreeItem : facilityTreeItemChildren) {
					getFacilityTreeRecursive(facilityTreeItem, scopeOnly, validFlg);
				}
			}
		} catch (Exception e) {
			m_log.warn("getFacilityTree() failure to get a tree data of facilities. : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}

		m_log.debug("successful in getting tree data of facilities.");
		return rootTree;
	}

	/**
	 * ファシリティの木構造を再帰的に取得する。<BR>
	 * 
	 * @param parentTree 親となるファシリティの木構造
	 * @param scopeOnly スコープのみの場合はtrue, 全てのファシリティの場合はfalse
	 * @param validFlg 有効/無効（nullの場合は全てのノード）
	 * @throws HinemosUnknown 
	 */
	private static void getFacilityTreeRecursive(FacilityTreeItem tree, boolean scopeOnly, Boolean validFlg) throws HinemosUnknown {
		if (tree == null)
			throw new HinemosUnknown("tree is null");
		m_log.debug("getFacilityTreeRecursive()"
				+ " tree = " + (tree.getData().getFacilityId())
				+ " loginuser = " + (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

		/** メイン処理 */
		// 木構造への格納必要性の確認
		if (tree.getData().getFacilityType() == FacilityConstant.TYPE_NODE) {
			// スコープのみの場合、ノードは格納しない
			if (scopeOnly) {
				return;
			}
			// 有効/無効の確認（nullならば必ず格納）
			if (validFlg != null
					&& !validFlg.equals(tree.getData().getValid())) {
				return;
			}
		}
		FacilityTreeItem[] itemChildren = tree.getChildrenArray();
		if (itemChildren != null) {
			for (FacilityTreeItem item : itemChildren) {
				getFacilityTreeRecursive(item, scopeOnly, validFlg);
			}
		}
	}

	/**
	 * ルートとなるスコープの一覧を取得する。<BR>
	 * 
	 * @return ファシリティインスタンスの配列
	 * @throws FacilityNotFound
	 */
	public static ArrayList<ScopeInfo> getRootScopeList() throws FacilityNotFound {
		m_log.debug("getRootScopeList() start");

		/** メイン処理 */
		return new ArrayList<>(QueryUtil.getRootScopeFacility_NONE());
	}

	/**
	 * スコープ配下にあるファシリティの一覧を取得する。<BR>
	 * 引数がノードの場合は、そのノードのファシリティIDを返す。
	 * 引数が登録ノード全ての場合は空リストを返す。
	 * 
	 * @param parentFacilityId スコープのファシリティID
	 * @param ownerRoleId オーナーロールID
	 * @param level 取得する階層数
	 * @param scopeFlag スコープ自身を含めるか（含める:true 含めない:false)
	 * @return ファシリティの配列
	 */
	private static ArrayList<FacilityInfo> getFacilityList(String parentFacilityId,
			String ownerRoleId, int level, boolean scopeFlag) {

		/** ローカル変数 */
		HashMap<String, FacilityInfo> facilityMap = new HashMap<String, FacilityInfo>();

		/** メイン処理 */
		m_log.debug("getting facilities under a scope. (scopeFacilityId = " + parentFacilityId + ")");

		if (ObjectValidator.isEmptyString(parentFacilityId)) {
			return new ArrayList<FacilityInfo>();
		}

		FacilityTreeItem treeItem = null;
		if (ownerRoleId != null && !ownerRoleId.isEmpty()) {
			// オーナーロールIDをもとにオブジェクト権限チェック
			treeItem = FacilityTreeCache.getFacilityTreeByRoleId(ownerRoleId);
		} else {
			// ユーザをもとにオブジェクト権限チェック
			String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			treeItem = FacilityTreeCache.getFacilityTreeByUserId(userId);
		}
		FacilityTreeItem parentFacilityTreeItem = getTopFacilityRecursive(treeItem, parentFacilityId);
		if (parentFacilityTreeItem == null) {
			m_log.info("getFacilityList() : Entity is not found. : facilityId = " + parentFacilityId);
		} else {
			if (parentFacilityTreeItem.getData().getFacilityType() == FacilityConstant.TYPE_NODE) {
				facilityMap.put(parentFacilityTreeItem.getData().getFacilityId(), parentFacilityTreeItem.getData());
				return new ArrayList<FacilityInfo>(facilityMap.values());
			} else if (scopeFlag) {
				facilityMap.put(parentFacilityTreeItem.getData().getFacilityId(), parentFacilityTreeItem.getData());
			}
			getFacilityListRecursive(parentFacilityTreeItem, level, facilityMap, scopeFlag);
		}
		m_log.debug("successful in getting facilities under a scope. (scopeFacilityId = " + parentFacilityId + ")");
		return new ArrayList<FacilityInfo>(facilityMap.values());
	}

	/**
	 * 最上位のファシリティを検索する。<BR>
	 * 
	 * @param parentFacilityTreeItem スコープのファシリティインスタンス
	 * @param facilityId 検索するファシリティのファシリティID
	 */
	private static FacilityTreeItem getTopFacilityRecursive(FacilityTreeItem parentFacilityTreeItem, String facilityId) {
		FacilityTreeItem[] facilityTreeItems = parentFacilityTreeItem.getChildrenArray();
		FacilityTreeItem treeItem = null;
		if (parentFacilityTreeItem.getData().getFacilityId().equals(facilityId)) {
			return parentFacilityTreeItem;
		}
		if (facilityTreeItems != null) {
			for (FacilityTreeItem childItem : facilityTreeItems) {
				treeItem = getTopFacilityRecursive(childItem, facilityId);
				if (treeItem != null) {
					return treeItem;
				}
			}
		}
		return treeItem;
	}

	/**
	 * スコープ配下にあるファシリティの一覧を取得する。<BR>
	 * 
	 * @param parentFacilityTreeItem スコープのファシリティインスタンス
	 * @param level 取得する階層数
	 * @param facilityMap 格納先となるファシリティの配列
	 * @param scopeFlag スコープ自身を含めるか（含める:true 含めない:false)
	 */
	private static void getFacilityListRecursive(FacilityTreeItem parentFacilityTreeItem,
			int level, HashMap<String, FacilityInfo> facilityMap, boolean scopeFlag) {
		/** ローカル変数 */
		boolean recursive = false;
		int nextLevel = 0;

		/** メイン処理 */
		// 階層数による再帰的処理の必要性の確認
		if (level == RepositoryControllerBean.ALL) {
			recursive = true;
			nextLevel = RepositoryControllerBean.ALL;
		} else if (level > 1) {
			recursive = true;
			nextLevel = level - 1;
		}

		// 再帰的にファシリティを配列に追加する
		FacilityTreeItem[] childFacilityTreeItems = parentFacilityTreeItem.getChildrenArray();
		if (childFacilityTreeItems != null) {
			for (FacilityTreeItem childFacilityTreeItem : childFacilityTreeItems) {
				if (childFacilityTreeItem.getData().getFacilityType() == FacilityConstant.TYPE_SCOPE) {
					if (scopeFlag) {
						facilityMap.put(childFacilityTreeItem.getData().getFacilityId(), childFacilityTreeItem.getData());
						m_log.debug("add scope = " + childFacilityTreeItem.getData().getFacilityId());
					}
				} else {
					facilityMap.put(childFacilityTreeItem.getData().getFacilityId(), childFacilityTreeItem.getData());
					m_log.debug("add node = " + childFacilityTreeItem.getData().getFacilityId());
				}
				if (recursive) {
					getFacilityListRecursive(childFacilityTreeItem, nextLevel, facilityMap, scopeFlag);
				}
			}
		}
	}

	/**
	 * ファシリティインスタンスが組み込みスコープであるかどうかを確認する。<BR>
	 * 
	 * @param facility ファシリティインスタンス
	 * @return 組み込みスコープの場合はtrue, それ以外はfalse
	 */
	public static boolean isBuildinScope(FacilityInfo facility) {
		/** メイン処理 */
		if (facility == null) {
			return false;
		}

		Set<String> buildInScopeFacilityIdSet = new HashSet<String>();
		buildInScopeFacilityIdSet.add(FacilityTreeAttributeConstant.INTERNAL_SCOPE);
		buildInScopeFacilityIdSet.add(FacilityTreeAttributeConstant.REGISTERED_SCOPE);
		buildInScopeFacilityIdSet.add(FacilityTreeAttributeConstant.UNREGISTERED_SCOPE);
		buildInScopeFacilityIdSet.add(FacilityTreeAttributeConstant.OWNER_SCOPE);
		buildInScopeFacilityIdSet.add(FacilityTreeAttributeConstant.OS_PARENT_SCOPE);
		buildInScopeFacilityIdSet.add(FacilityTreeAttributeConstant.NODE_CONFIGURATION_SCOPE);
		
		// クラウドのルートスコープは、初期は存在しないが、作成されるとビルトインになる。
		buildInScopeFacilityIdSet.add(CloudConstants.privateRootId);
		buildInScopeFacilityIdSet.add(CloudConstants.publicRootId);
		
		buildInScopeFacilityIdSet.addAll(OsScopeInitializerPlugin.getOsScopeIdSet());
		if (buildInScopeFacilityIdSet.contains(facility.getFacilityId())) {
			return true;
		} else {
			// ロールスコープは組み込みスコープとする
			List<String> roleIdList = UserRoleCache.getAllRoleIdList();
			if (roleIdList.contains(facility.getFacilityId())) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * ファシリティがノードかどうかを確認する。<BR>
	 * 
	 * @param facilityId ファシリティID
	 * @return ノードの場合はtrue, それ以外の場合はfalse
	 * @throws FacilityNotFound
	 */
	public static boolean isNode(String facilityId) throws FacilityNotFound {

		/** メイン処理 */
		m_log.debug("checking whether a facility is node...");

		FacilityInfo facility = QueryUtil.getFacilityPK_NONE(facilityId);

		m_log.debug("successful in checking whether a facility is node or not.");
		return FacilityUtil.isNode(facility);
	}

	/**
	 * 指定のノード名で登録されているノードのファシリティIDを返す。
	 * 管理対象フラグ「無効」となっているものは含まない。
	 * よって、指定のノード名で登録されているノードがリポジトリに存在しているが、
	 * 全て管理対象フラグ「無効」の場合は、空のセットが返る。
	 * 
	 * @param nodename ノード名
	 * @return ファシリティIDのセット。存在しない場合は"UNREGISTEREFD"だけを含めたセット。
	 */
	public static Set<String> getNodeListByNodename(String nodename){
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		HashMap<String, HashSet<String>> cache = getNodenameFacilityIdCache();
		return cache.get(nodename.toLowerCase());
	}

	/**
	 * getNodeListByNodenameで利用するキャッシュを現在のDBに基づき再構成する。<br/>
	 */
	private static void refreshNodenameFacilityIdCache() {
		long start = HinemosTime.currentTimeMillis();
		
		HashMap<String, HashSet<String>> nodenameFacilityIdMap = new HashMap<String, HashSet<String>>();
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			_nodenameFacilityIdCacheLock.writeLock();
			
			em.clear();
			List<NodeInfo> allNodes = QueryUtil.getAllNode_NONE();

			for(NodeInfo node : allNodes){
				String facilityId = node.getFacilityId();

				// ノード名からファシリティIDのセットを引けるようにする
				// ノード名は小文字に変換して処理する
				String checkNodename = node.getNodeName().toLowerCase();
				HashSet<String> nodenameFacilityIdSet = nodenameFacilityIdMap.get(checkNodename);
				if(nodenameFacilityIdSet == null){
					nodenameFacilityIdSet = new HashSet<String>();
					nodenameFacilityIdMap.put(checkNodename, nodenameFacilityIdSet);
				}

				// 管理対象フラグ「有効」のノードのみセットに追加する。管理対象フラグ「無効」のノードはセットに追加しない。
				// よって、最終的にノード名でnodenameFacilityIdSetから取得したファシリティのセットは存在するが、
				// その中にエンティティが含まれない場合は、該当ノード名のノードは存在するが、全て無効だったことなる。
				if(node.getValid()){
					nodenameFacilityIdSet.add(facilityId);
				}
			}
			
			storeNodenameFacilityIdCache(nodenameFacilityIdMap);
		} finally {
			_nodenameFacilityIdCacheLock.writeUnlock();
			
			m_log.info("refresh nodenameFacilityIdMap(Cache). " +
					(HinemosTime.currentTimeMillis() - start) + "ms. key size=" + nodenameFacilityIdMap.size());
		}
	}

	
	/**
	 * 指定のIPアドレスで登録されているノードのファシリティIDのセットを返す。
	 * 管理対象フラグ「無効」となっているノードのファシリティIDは含まない。
	 * よって、指定のIPアドレスで登録されているノードがリポジトリに存在しているが、
	 * 全て管理対象フラグ「無効」の場合は、空のセットが返る。
	 * 
	 * @param ipAddress IPアドレス
	 * @return ファシリティIDのセット。存在しない場合はnullを返す。
	 * 	 */
	public static Set<String> getNodeListByIpAddress(InetAddress ipAddress){
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		HashMap<InetAddress, HashSet<String>> cache = getIpaddrFacilityIdCache();
		return cache.get(ipAddress);
	}

	/**
	 * getNodeListByIpAddressのためのキャッシュを現在のDBから再構成する。<br/>
	 */
	private static void refreshIpaddrFacilityIdCache() {
		long start = HinemosTime.currentTimeMillis();
		
		HashMap<InetAddress, HashSet<String>> inetAddressFacilityIdMap = new HashMap<InetAddress, HashSet<String>>();
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			_ipaddrFacilityIdCacheLock.writeLock();
			
			em.clear();
			List<NodeInfo> allNodes = QueryUtil.getAllNode();

			for(NodeInfo node : allNodes){
				String facilityId = node.getFacilityId();

				// 「IPアドレスのバージョン」により指定されたIPアドレスを設定する。
				Integer ipVersion = node.getIpAddressVersion();
				String ipAddressString = null;
				if(ipVersion != null && ipVersion.intValue() == 6){
					ipAddressString = node.getIpAddressV6();
				} else {
					ipAddressString = node.getIpAddressV4();
				}
				InetAddress checkIpAddress = InetAddress.getByName(ipAddressString);

				// 監視対象のノードは、IPアドレスからファシリティIDのセットを引けるようにする
				HashSet<String> inetAddressFacilityIdSet = inetAddressFacilityIdMap.get(checkIpAddress);
				if(inetAddressFacilityIdSet == null){
					inetAddressFacilityIdSet = new HashSet<String>();
					inetAddressFacilityIdMap.put(checkIpAddress, inetAddressFacilityIdSet);
				}

				// 管理対象フラグ「有効」のノードのみセットに追加する。管理対象フラグ「無効」のノードはセットに追加しない。
				// よって、最終的にIPアドレスでinetAddressFacilityIdMapから取得したファシリティのセットは存在するが、
				// その中にエンティティが含まれない場合は、該当IPアドレスのノードは存在するが、全て無効だったことなる。
				if(node.getValid()){
					inetAddressFacilityIdSet.add(facilityId);
				}
			}
			
			storeIpaddrFacilityIdCache(inetAddressFacilityIdMap);
		} catch (Exception e) {
			m_log.warn("refreshIpaddrFacilityIdCache() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			_ipaddrFacilityIdCacheLock.writeUnlock();
			
			m_log.info("refresh inetAddressFacilityIdMap(Cache). " +
					(HinemosTime.currentTimeMillis() - start) + "ms. key size=" + inetAddressFacilityIdMap.size());
		}
	}
	
	/**
	 * 指定のホスト名（複数登録可能）で登録されているノードのファシリティIDを返す。
	 * 管理対象フラグ「無効」となっているものは含まない。
	 * よって、指定のノード名で登録されているノードがリポジトリに存在しているが、
	 * 全て管理対象フラグ「無効」の場合は、空のセットが返る。
	 * 
	 * @param hostname ホスト名
	 * @return ファシリティIDのセット。存在しない場合は"UNREGISTEREFD"だけを含めたセット。
	 */
	public static Set<String> getNodeListByHostname(String hostname){
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		HashMap<String, HashSet<String>> cache = getHostnameFacilityIdCache();
		return cache.get(hostname);
	}
	
	/**
	 * getNodeListByHostnameのためのキャッシュを現在のDBから再構成する。<br/>
	 */
	private static void refreshHostnameFacilityIdCache() {
		long start = HinemosTime.currentTimeMillis();
		
		HashMap<String, HashSet<String>> hostnameFacilityIdMap = new HashMap<String, HashSet<String>>();
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			_hostnameFacilityIdCacheLock.writeLock();
			
			em.clear();
			List<NodeHostnameInfo> allNodes = QueryUtil.getAllNodeHostname();

			for(NodeHostnameInfo node : allNodes){
				String facilityId = node.getId().getFacilityId();

				// ホスト名は大文字小文字を区別する
				String checkHostname = node.getId().getHostname();
				HashSet<String> hostnameFacilityIdSet = hostnameFacilityIdMap.get(checkHostname);
				if(hostnameFacilityIdSet == null){
					hostnameFacilityIdSet = new HashSet<String>();
					hostnameFacilityIdMap.put(checkHostname, hostnameFacilityIdSet);
				}

				// 管理対象フラグ「有効」のノードのみセットに追加する。管理対象フラグ「無効」のノードはセットに追加しない。
				// よって、最終的にホスト名でhostnameFacilityIdSetから取得したファシリティのセットは存在するが、
				// その中にエンティティが含まれない場合は、該当ホスト名のノードは存在するが、全て無効だったことなる。
				FacilityInfo entity = null;
				try {
					entity = QueryUtil.getFacilityPK(facilityId);
				} catch (FacilityNotFound e) {
					m_log.debug(e.getMessage(), e);
				} catch (InvalidRole e) {
					m_log.debug(e.getMessage(), e);
				}
				if(entity != null && entity.getValid()){
					hostnameFacilityIdSet.add(facilityId);
				}
			}
			
			storeHostnameFacilityIdCache(hostnameFacilityIdMap);
		} finally {
			_hostnameFacilityIdCacheLock.writeUnlock();
			
			m_log.info("refresh hostnameFacilityIdMap(Cache). " +
					(HinemosTime.currentTimeMillis() - start) + "ms. key size=" + hostnameFacilityIdMap.size());
		}
	}

	private static void refreshScopeNodeFacilityIdCache() {
		try {
			_scopeNodeFacilityIdCacheLock.writeLock();
			
			storeScopeNodeFacilityIdCache(new HashMap<String, Boolean>());
		} finally {
			_scopeNodeFacilityIdCacheLock.writeUnlock();
		}
	}
	
	/**
	 * 指定のノードが、指定スコープ配下の有効なノードに含まれるかチェックする
	 * @param scopeFacilityId 確認対象のスコープ（このスコープ配下をチェックする）
	 * @param nodeFacilityId 確認対象ノードのファシリティID
	 * @param ownerRoleId オーナーロールID
	 * @return 含まれる場合はtrueを返す
	 */
	public static boolean containsFaciliyId(String scopeFacilityId, String nodeFacilityId, String ownerRoleId){
		Boolean ret = null;
		String key = scopeFacilityId + "," + nodeFacilityId;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			try {
				_scopeNodeFacilityIdCacheLock.readLock();
				
				HashMap<String, Boolean> cache = getScopeNodeFacilityIdCache();
				ret = cache.get(key);
				if (ret != null) {
					return ret;
				}
			} finally {
				_scopeNodeFacilityIdCacheLock.readUnlock();
			}
			
			try {
				_scopeNodeFacilityIdCacheLock.writeLock();
				
				HashMap<String, Boolean> cache = getScopeNodeFacilityIdCache();
				em.clear();
				ret = getNodeFacilityIdList(scopeFacilityId, ownerRoleId, RepositoryControllerBean.ALL, false, true).contains(nodeFacilityId);
				m_log.debug("containsFacilityId key=" + key + ", ret=" + ret);
				cache.put(key, ret);
				
				storeScopeNodeFacilityIdCache(cache);
				
				return ret;
			} finally {
				_scopeNodeFacilityIdCacheLock.writeUnlock();
			}
		}
	}


	/**
	 * 構成情報ヘッダーファイルを返します。
	 *
	 * @param conditionStr 検索対象
	 * @param filename ファイル名
	 * @param username ユーザ名
	 * @param locale ロケール
	 * @return 帳票出力用構成情報一覧
	 * @throws HinemosUnknown
	 * @throws IOException
	 */
	public DataHandler getNodeConfigInfoFileHeader(	String conditionStr, String filename, String username, Locale locale)
			throws HinemosUnknown, IOException {

		String exportDirectory = HinemosPropertyDefault.node_config_export_dir.getStringValue();
		String filepath = exportDirectory + "/" + filename;
		File file = new File(filepath);
		boolean UTF8_BOM = HinemosPropertyCommon.node_config_report_bom.getBooleanValue();
		if (UTF8_BOM) {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write( 0xef );
			fos.write( 0xbb );
			fos.write( 0xbf );
			fos.close();
		}
		FileWriter filewriter = new FileWriter(file, true);

		try {

			String SEPARATOR = HinemosPropertyCommon.node_config_report_separator.getStringValue();
			String DATE_FORMAT = HinemosPropertyCommon.node_config_report_format.getStringValue();

			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			sdf.setTimeZone(HinemosTime.getTimeZone());
			filewriter.write(Messages.getString(MessageConstant.REPORT_TITLE_NODE_CONFIG.name(), locale) + "\n");
			filewriter.write(Messages.getString(MessageConstant.REPORT_OUTPUT_DATE.name(), locale) + SEPARATOR +
					sdf.format(HinemosTime.getDateInstance()) + "\n");
			filewriter.write(Messages.getString(MessageConstant.SEARCH_TARGET.name(), locale) + SEPARATOR +
					conditionStr + "\n");
			filewriter.write(Messages.getString(MessageConstant.REPORT_OUTPUT_USER.name(), locale) + SEPARATOR + username + "\n");

			// 検索条件
			filewriter.write("" + "\n");
				
			// 出力処理
			filewriter.write(
					Messages.getString(MessageConstant.MANAGER_NAME.name(), locale) + SEPARATOR + 				// マネージャ名
					Messages.getString(MessageConstant.FACILITY_ID.name(), locale) + SEPARATOR + 				// ファシリティID
					Messages.getString(MessageConstant.TYPE.name(), locale) + SEPARATOR + 						// 種別
					Messages.getString(MessageConstant.NAME.name(), locale) + SEPARATOR + 						// 名前
					Messages.getString(MessageConstant.SUB_NAME.name(), locale) + SEPARATOR +					// 名前（サブ）
					Messages.getString(MessageConstant.DEVICE_INDEX.name(), locale) + SEPARATOR +				// インデックス
					Messages.getString(MessageConstant.DEVICE_DISPLAY_NAME.name(), locale) + SEPARATOR +		// 表示名
					Messages.getString(MessageConstant.DEVICE_SIZE.name(), locale) + SEPARATOR +			// サイズ
					Messages.getString(MessageConstant.DEVICE_SIZE_UNIT.name(), locale) + SEPARATOR +		// サイズ単位
					Messages.getString(MessageConstant.DESCRIPTION.name(), locale) + SEPARATOR +			// 説明
					Messages.getString(MessageConstant.VERSION.name(), locale) + SEPARATOR +				// バージョン
					Messages.getString(MessageConstant.RELEASE.name(), locale) + SEPARATOR +				// リリース
					Messages.getString(MessageConstant.STARTUP_DATE_TIME.name(), locale) + SEPARATOR +		// 起動日時
					Messages.getString(MessageConstant.INSTALL_DATE.name(), locale) + SEPARATOR +			// インストール日時
					Messages.getString(MessageConstant.VALUE.name(), locale) + SEPARATOR +					// 値
					Messages.getString(MessageConstant.CPU_CORE_COUNT.name(), locale) + SEPARATOR +		// コア数
					Messages.getString(MessageConstant.CPU_THREAD_COUNT.name(), locale) + SEPARATOR +		// スレッド数
					Messages.getString(MessageConstant.CPU_CLOCK_COUNT.name(), locale) + SEPARATOR +		// クロック数
					Messages.getString(MessageConstant.NIC_IP_ADDRESS.name(), locale) + SEPARATOR +		// IPアドレス
					Messages.getString(MessageConstant.NIC_MAC_ADDRESS.name(), locale) + SEPARATOR +		// MACアドレス
					Messages.getString(MessageConstant.DISK_RPM.name(), locale) + SEPARATOR +				// ディスク回転数
					Messages.getString(MessageConstant.FILE_SYSTEM_TYPE.name(), locale) + SEPARATOR +		// ファイルシステム種別
					Messages.getString(MessageConstant.CHARACTER_SET.name(), locale) + SEPARATOR +			// 文字セット
					Messages.getString(MessageConstant.NODE_NETSTAT_PROTOCOL.name(), locale) + SEPARATOR +	// プロトコル
					Messages.getString(MessageConstant.NODE_NETSTAT_LOCAL_IP_ADDRESS.name(), locale) + SEPARATOR +	// ローカルIPアドレス
					Messages.getString(MessageConstant.NODE_NETSTAT_LOCAL_PORT.name(), locale) + SEPARATOR +	// ローカルポート
					Messages.getString(MessageConstant.NODE_NETSTAT_FOREIGN_IP_ADDRESS.name(), locale) + SEPARATOR +	// 外部IPアドレス
					Messages.getString(MessageConstant.NODE_NETSTAT_FOREIGN_PORT.name(), locale) + SEPARATOR +	// 外部ポート
					Messages.getString(MessageConstant.NODE_NETSTAT_PROCESS_NAME.name(), locale) + SEPARATOR +	// プロセス名
					Messages.getString(MessageConstant.NODE_NETSTAT_PID.name(), locale) + SEPARATOR +	// PID
					Messages.getString(MessageConstant.NODE_NETSTAT_STATUS.name(), locale) + SEPARATOR +	// ステータス
					Messages.getString(MessageConstant.NODE_PROCESS_PATH.name(), locale) + SEPARATOR +	// フルパス
					Messages.getString(MessageConstant.NODE_PROCESS_EXEC_USER.name(), locale) + SEPARATOR +	// 実行ユーザ
					Messages.getString(MessageConstant.NODE_PACKAGE_VENDOR.name(), locale) + SEPARATOR +	// ベンダー
					Messages.getString(MessageConstant.NODE_PACKAGE_ARCHITECTURE.name(), locale) + SEPARATOR +		// アーキテクチャ
					Messages.getString(MessageConstant.COMMAND.name(), locale) + SEPARATOR +		// コマンド
					Messages.getString(MessageConstant.NODE_LICENSE_VENDOR_CONTACT.name(), locale) + SEPARATOR +	// ベンダー連絡先
					Messages.getString(MessageConstant.NODE_LICENSE_SERIAL_NUMBER.name(), locale) + SEPARATOR +	// シリアルナンバー
					Messages.getString(MessageConstant.NODE_LICENSE_COUNT.name(), locale) + SEPARATOR +	// 数量
					Messages.getString(MessageConstant.NODE_LICENSE_EXPIRATION_DATE.name(), locale) + SEPARATOR +	// 有効期限
					Messages.getString(MessageConstant.REG_DATE.name(), locale) + SEPARATOR +		// 作成日時
					Messages.getString(MessageConstant.REG_USER.name(), locale) + SEPARATOR +		// 作成ユーザ
					Messages.getString(MessageConstant.UPDATE_DATE.name(), locale) + SEPARATOR +		// 更新日時
					Messages.getString(MessageConstant.UPDATE_USER.name(), locale) +		// 更新ユーザ
					"\n");

		} finally {
			filewriter.close();
		}

		// リストをファイルに書き出し。
		FileDataSource source = new FileDataSource(file);
		DataHandler handler = new DataHandler(source);

		return handler;
	}

	/**
	 * 引数で指定された条件に一致する構成情報ファイルを返します。
	 *
	 * @param facilityIdlist ファシリティID一覧
	 * @param targetDatetime 対象日時
	 * @param filename ファイル名
	 * @param locale ロケール
	 * @param managerName マネージャ名
	 * @param nodeConfigSettingItemList 構成情報ダウンロード対象一覧
	 * @return 帳票出力用構成情報一覧
	 * @throws HinemosUnknown
	 * @throws IOException
	 */
	public DataHandler getNodeConfigInfoFile(
			List<String> facilityIdList, Long targetDatetime, String filename, Locale locale, 
			String managerName, List<NodeConfigSettingItem> nodeConfigSettingItemList)
			throws HinemosUnknown, IOException {

		String exportDirectory = HinemosPropertyDefault.node_config_export_dir.getStringValue();
		String filepath = exportDirectory + "/" + filename;
		File file = new File(filepath);
		FileWriter filewriter = new FileWriter(file, true);

		try {
			// 対象ファシリティのファシリティIDを取得
			Collections.sort(facilityIdList);
			for (int i = 0; i < facilityIdList.size(); i++) {
				String facilityId = facilityIdList.get(i);
				try {
					QueryUtil.getFacilityPK(facilityId);
				} catch (FacilityNotFound e) {
					// ログ出す
					m_log.warn("getNodeConfigInfoFile(): Facility ID not found."+e);
					continue;
				} catch (InvalidRole e){
					m_log.warn("getNodeConfigInfoFile(): No Object privileage for facility id: "+facilityId+"."+e);
					continue;
				}

				// 対象構成情報はより全件取得する。
				NodeInfo nodeInfo = null;
				try {
					if (targetDatetime == null 	|| targetDatetime == 0L) {
						nodeInfo = NodeProperty.getPropertyFull(facilityId, nodeConfigSettingItemList);
					} else {
						nodeInfo = NodeProperty.getPropertyFull(facilityId, targetDatetime, nodeConfigSettingItemList);
					}
				} catch (FacilityNotFound e) {
					// 対象のノード情報が未存在
					m_log.warn("getNodeConfigInfoFile() : node info is not found. facilityId=" + facilityId);
					continue;
				}
				if (nodeInfo == null) {
					m_log.warn("getNodeConfigInfoFile() : node info is null. facilityId=" + facilityId);
					continue;
				}

				// 帳票出力用に変換
				nodeConfigToFile(nodeInfo, filewriter, locale, managerName, nodeConfigSettingItemList);
			}

		} finally {
			filewriter.close();
		}

		// リストをファイルに書き出し。
		FileDataSource source = new FileDataSource(file);
		DataHandler handler = new DataHandler(source);

		return handler;
	}

	/**
	 * 構成情報ファイルのうち、指定されたファイル名と前方一致するファイルを削除します。
	 * 
	 * @param filename ファイル名
	 */
	public void deleteNodeConfigInfoFile(String filename) {
		String exportDirectory = HinemosPropertyDefault.node_config_export_dir.getStringValue();

		File dir = new File(exportDirectory);
		File[] list = dir.listFiles();
		if (list == null) {
			m_log.warn("deleteNodeConfigInfoFile() : Fail to delete. directory=" + exportDirectory + ", filename=" + filename);
			return;
		}
		for (File file : list) {
			if (file.isDirectory() || !file.getName().startsWith(filename)) {
				continue;
			}
			if (!file.delete()) {
				m_log.warn("deleteNodeConfigInfoFile() : Fail to delete. filename=" + file.getAbsolutePath());
			}
		}
	}

	/**
	 * DBより取得した構成情報をFileWriterに出力します。
	 *
	 * @param managerName マネージャ名
	 * @param nodeInfo 構成情報
	 * @param filewriter FileWriter
	 * @param locale ロケール
	 * @param managerName マネージャ名
	 * @param nodeConfigSettingItemList 構成情報ダウンロード対象一覧
	 * @throws IOException
	 */
	private void nodeConfigToFile(NodeInfo nodeInfo, FileWriter filewriter, Locale locale, String managerName, 
			List<NodeConfigSettingItem> nodeConfigSettingItemList) throws IOException {

		if (nodeInfo == null) {
			return;
		}
		// OS情報
		if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.OS) && nodeInfo.getNodeOsInfo() != null) {
			NodeOsInfo nodeDetailInfo = nodeInfo.getNodeOsInfo();
			NodeConfigFileInfo info = new NodeConfigFileInfo(managerName, nodeInfo.getFacilityId(), NodeConfigSettingItem.OS);
			info.name = nodeDetailInfo.getOsName();
			info.version = nodeDetailInfo.getOsVersion();
			info.release = nodeDetailInfo.getOsRelease();
			info.startupDateTime = nodeDetailInfo.getStartupDateTime();
			info.characterSet = nodeDetailInfo.getCharacterSet();
			info.regDate = nodeDetailInfo.getRegDate();
			info.regUser = nodeDetailInfo.getRegUser();
			info.updateDate = nodeDetailInfo.getUpdateDate();
			info.updateUser = nodeDetailInfo.getUpdateUser();
			writeFileWriter(filewriter, info);
		}
		// CPU情報
		if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.HW_CPU) && nodeInfo.getNodeCpuInfo() != null) {
			for (NodeCpuInfo nodeDetailInfo : nodeInfo.getNodeCpuInfo()) {
				NodeConfigFileInfo info = new NodeConfigFileInfo(managerName, nodeInfo.getFacilityId(), NodeConfigSettingItem.HW_CPU);
				info.name = nodeDetailInfo.getDeviceName();
				info.index = nodeDetailInfo.getDeviceIndex();
				info.displayName = nodeDetailInfo.getDeviceDisplayName();
				info.size = nodeDetailInfo.getDeviceSize();
				info.sizeUnit = nodeDetailInfo.getDeviceSizeUnit();
				info.description = nodeDetailInfo.getDeviceDescription();
				info.coreCount = nodeDetailInfo.getCoreCount();
				info.threadCount = nodeDetailInfo.getThreadCount();
				info.clockCount = nodeDetailInfo.getClockCount();
				info.regDate = nodeDetailInfo.getRegDate();
				info.regUser = nodeDetailInfo.getRegUser();
				info.updateDate = nodeDetailInfo.getUpdateDate();
				info.updateUser = nodeDetailInfo.getUpdateUser();
				writeFileWriter(filewriter, info);
			}
		}
		// メモリ情報
		if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.HW_MEMORY) && nodeInfo.getNodeMemoryInfo() != null) {
			for (NodeMemoryInfo nodeDetailInfo : nodeInfo.getNodeMemoryInfo()) {
				NodeConfigFileInfo info = new NodeConfigFileInfo(managerName, nodeInfo.getFacilityId(), NodeConfigSettingItem.HW_MEMORY);
				info.name = nodeDetailInfo.getDeviceName();
				info.index = nodeDetailInfo.getDeviceIndex();
				info.displayName = nodeDetailInfo.getDeviceDisplayName();
				info.size = nodeDetailInfo.getDeviceSize();
				info.sizeUnit = nodeDetailInfo.getDeviceSizeUnit();
				info.description = nodeDetailInfo.getDeviceDescription();
				info.regDate = nodeDetailInfo.getRegDate();
				info.regUser = nodeDetailInfo.getRegUser();
				info.updateDate = nodeDetailInfo.getUpdateDate();
				info.updateUser = nodeDetailInfo.getUpdateUser();
				writeFileWriter(filewriter, info);
			}
		}
		// NIC情報
		if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.HW_NIC) && nodeInfo.getNodeNetworkInterfaceInfo() != null) {
			for (NodeNetworkInterfaceInfo nodeDetailInfo : nodeInfo.getNodeNetworkInterfaceInfo()) {
				NodeConfigFileInfo info = new NodeConfigFileInfo(managerName, nodeInfo.getFacilityId(), NodeConfigSettingItem.HW_NIC);
				info.name = nodeDetailInfo.getDeviceName();
				info.index = nodeDetailInfo.getDeviceIndex();
				info.displayName = nodeDetailInfo.getDeviceDisplayName();
				info.size = nodeDetailInfo.getDeviceSize();
				info.sizeUnit = nodeDetailInfo.getDeviceSizeUnit();
				info.description = nodeDetailInfo.getDeviceDescription();
				info.ipAddress = nodeDetailInfo.getNicIpAddress();
				info.macAddress = nodeDetailInfo.getNicMacAddress();
				info.regDate = nodeDetailInfo.getRegDate();
				info.regUser = nodeDetailInfo.getRegUser();
				info.updateDate = nodeDetailInfo.getUpdateDate();
				info.updateUser = nodeDetailInfo.getUpdateUser();
				writeFileWriter(filewriter, info);
			}
		}
		// ディスク情報
		if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.HW_DISK) && nodeInfo.getNodeDiskInfo() != null) {
			for (NodeDiskInfo nodeDetailInfo : nodeInfo.getNodeDiskInfo()) {
				NodeConfigFileInfo info = new NodeConfigFileInfo(managerName, nodeInfo.getFacilityId(), NodeConfigSettingItem.HW_DISK);
				info.name = nodeDetailInfo.getDeviceName();
				info.index = nodeDetailInfo.getDeviceIndex();
				info.displayName = nodeDetailInfo.getDeviceDisplayName();
				info.size = nodeDetailInfo.getDeviceSize();
				info.sizeUnit = nodeDetailInfo.getDeviceSizeUnit();
				info.description = nodeDetailInfo.getDeviceDescription();
				info.diskRpm = nodeDetailInfo.getDiskRpm();
				info.regDate = nodeDetailInfo.getRegDate();
				info.regUser = nodeDetailInfo.getRegUser();
				info.updateDate = nodeDetailInfo.getUpdateDate();
				info.updateUser = nodeDetailInfo.getUpdateUser();
				writeFileWriter(filewriter, info);
			}
		}
		// ファイルシステム情報
		if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.HW_FILESYSTEM) && nodeInfo.getNodeFilesystemInfo() != null) {
			for (NodeFilesystemInfo nodeDetailInfo : nodeInfo.getNodeFilesystemInfo()) {
				NodeConfigFileInfo info = new NodeConfigFileInfo(managerName, nodeInfo.getFacilityId(), NodeConfigSettingItem.HW_FILESYSTEM);
				info.name = nodeDetailInfo.getDeviceName();
				info.index = nodeDetailInfo.getDeviceIndex();
				info.displayName = nodeDetailInfo.getDeviceDisplayName();
				info.size = nodeDetailInfo.getDeviceSize();
				info.sizeUnit = nodeDetailInfo.getDeviceSizeUnit();
				info.description = nodeDetailInfo.getDeviceDescription();
				info.fileSystemType = nodeDetailInfo.getFilesystemType();
				info.regDate = nodeDetailInfo.getRegDate();
				info.regUser = nodeDetailInfo.getRegUser();
				info.updateDate = nodeDetailInfo.getUpdateDate();
				info.updateUser = nodeDetailInfo.getUpdateUser();
				writeFileWriter(filewriter, info);
			}
		}
		// ホスト名情報
		if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.HOSTNAME) && nodeInfo.getNodeHostnameInfo() != null) {
			for (NodeHostnameInfo nodeDetailInfo : nodeInfo.getNodeHostnameInfo()) {
				NodeConfigFileInfo info = new NodeConfigFileInfo(managerName, nodeInfo.getFacilityId(), NodeConfigSettingItem.HOSTNAME);
				info.name = nodeDetailInfo.getHostname();
				info.regDate = nodeDetailInfo.getRegDate();
				info.regUser = nodeDetailInfo.getRegUser();
				writeFileWriter(filewriter, info);
			}
		}
		// ネットワーク接続情報
		if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.NETSTAT) && nodeInfo.getNodeNetstatInfo() != null) {
			for (NodeNetstatInfo nodeDetailInfo : nodeInfo.getNodeNetstatInfo()) {
				NodeConfigFileInfo info = new NodeConfigFileInfo(managerName, nodeInfo.getFacilityId(), NodeConfigSettingItem.NETSTAT);
				info.protocol = nodeDetailInfo.getProtocol();
				info.localIpAddress = nodeDetailInfo.getLocalIpAddress();
				info.localPort = nodeDetailInfo.getLocalPort();
				info.foreignIpAddress = nodeDetailInfo.getForeignIpAddress();
				info.foreignPort = nodeDetailInfo.getForeignPort();
				info.processName = nodeDetailInfo.getProcessName();
				info.pid = nodeDetailInfo.getPid();
				info.status = nodeDetailInfo.getStatus();
				info.regDate = nodeDetailInfo.getRegDate();
				info.regUser = nodeDetailInfo.getRegUser();
				info.updateDate = nodeDetailInfo.getUpdateDate();
				info.updateUser = nodeDetailInfo.getUpdateUser();
				writeFileWriter(filewriter, info);
			}
		}
		// プロセス情報
		if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.PROCESS) && nodeInfo.getNodeProcessInfo() != null) {
			for (NodeProcessInfo nodeDetailInfo : nodeInfo.getNodeProcessInfo()) {
				NodeConfigFileInfo info = new NodeConfigFileInfo(managerName, nodeInfo.getFacilityId(), NodeConfigSettingItem.PROCESS);
				info.name = nodeDetailInfo.getProcessName();
				info.startupDateTime = nodeDetailInfo.getStartupDateTime();
				info.pid = nodeDetailInfo.getPid();
				info.path = nodeDetailInfo.getPath();
				info.execUser = nodeDetailInfo.getExecUser();
				info.regDate = nodeDetailInfo.getRegDate();
				info.regUser = nodeDetailInfo.getRegUser();
				writeFileWriter(filewriter, info);
			}
		}
		// パッケージ情報
		if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.PACKAGE) && nodeInfo.getNodePackageInfo() != null) {
			for (NodePackageInfo nodeDetailInfo : nodeInfo.getNodePackageInfo()) {
				NodeConfigFileInfo info = new NodeConfigFileInfo(managerName, nodeInfo.getFacilityId(), NodeConfigSettingItem.PACKAGE);
				info.name = nodeDetailInfo.getPackageName();
				info.displayName = nodeDetailInfo.getPackageId();
				info.version = nodeDetailInfo.getVersion();
				info.release = nodeDetailInfo.getRelease();
				info.installDate = nodeDetailInfo.getInstallDate();
				info.vendor = nodeDetailInfo.getVendor();
				info.architecture = nodeDetailInfo.getArchitecture();
				info.regDate = nodeDetailInfo.getRegDate();
				info.regUser = nodeDetailInfo.getRegUser();
				info.updateDate = nodeDetailInfo.getUpdateDate();
				info.updateUser = nodeDetailInfo.getUpdateUser();
				writeFileWriter(filewriter, info);
			}
		}
		// ノード変数情報
		if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.NODE_VARIABLE) && nodeInfo.getNodeVariableInfo() != null) {
			for (NodeVariableInfo nodeDetailInfo : nodeInfo.getNodeVariableInfo()) {
				NodeConfigFileInfo info = new NodeConfigFileInfo(managerName, nodeInfo.getFacilityId(), NodeConfigSettingItem.NODE_VARIABLE);
				info.name = nodeDetailInfo.getNodeVariableName();
				info.value = nodeDetailInfo.getNodeVariableValue();
				info.regDate = nodeDetailInfo.getRegDate();
				info.regUser = nodeDetailInfo.getRegUser();
				info.updateDate = nodeDetailInfo.getUpdateDate();
				info.updateUser = nodeDetailInfo.getUpdateUser();
				writeFileWriter(filewriter, info);
			}
		}
		// 個別導入製品情報
		if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.PRODUCT) && nodeInfo.getNodeProductInfo() != null) {
			for (NodeProductInfo nodeDetailInfo : nodeInfo.getNodeProductInfo()) {
				NodeConfigFileInfo info = new NodeConfigFileInfo(managerName, nodeInfo.getFacilityId(), NodeConfigSettingItem.PRODUCT);
				info.name = nodeDetailInfo.getProductName();
				info.version = nodeDetailInfo.getVersion();
				info.path = nodeDetailInfo.getPath();
				info.regDate = nodeDetailInfo.getRegDate();
				info.regUser = nodeDetailInfo.getRegUser();
				info.updateDate = nodeDetailInfo.getUpdateDate();
				info.updateUser = nodeDetailInfo.getUpdateUser();
				writeFileWriter(filewriter, info);
			}
		}
		// ライセンス情報
		if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.LICENSE) && nodeInfo.getNodeLicenseInfo() != null) {
			for (NodeLicenseInfo nodeDetailInfo : nodeInfo.getNodeLicenseInfo()) {
				NodeConfigFileInfo info = new NodeConfigFileInfo(managerName, nodeInfo.getFacilityId(), NodeConfigSettingItem.LICENSE);
				info.name = nodeDetailInfo.getProductName();
				info.vendor = nodeDetailInfo.getVendor();
				info.vendorContact = nodeDetailInfo.getVendorContact();
				info.serialNumber = nodeDetailInfo.getSerialNumber();
				info.count = nodeDetailInfo.getCount();
				info.expirationDate = nodeDetailInfo.getExpirationDate();
				info.regDate = nodeDetailInfo.getRegDate();
				info.regUser = nodeDetailInfo.getRegUser();
				info.updateDate = nodeDetailInfo.getUpdateDate();
				info.updateUser = nodeDetailInfo.getUpdateUser();
				writeFileWriter(filewriter, info);
			}
		}
		// ユーザ任意情報
		if (nodeConfigSettingItemList.contains(NodeConfigSettingItem.CUSTOM) && nodeInfo.getNodeCustomInfo() != null) {
			for (NodeCustomInfo nodeDetailInfo : nodeInfo.getNodeCustomInfo()) {
				NodeConfigFileInfo info = new NodeConfigFileInfo(managerName, nodeInfo.getFacilityId(), NodeConfigSettingItem.CUSTOM);
				info.name = nodeDetailInfo.getSettingId();
				info.subName = nodeDetailInfo.getSettingCustomId();
				info.displayName = nodeDetailInfo.getDisplayName();
				info.value = nodeDetailInfo.getValue();
				info.command = nodeDetailInfo.getCommand();
				info.regDate = nodeDetailInfo.getRegDate();
				info.regUser = nodeDetailInfo.getRegUser();
				info.updateDate = nodeDetailInfo.getUpdateDate();
				info.updateUser = nodeDetailInfo.getUpdateUser();
				writeFileWriter(filewriter, info);
			}
		}
	}

	/**
	 * 構成情報ファイル書き込み処理
	 * 
	 * @param filewriter FileWriter
	 * @param info 構成情報のファイル情報
	 * @throws IOException
	 */
	private void writeFileWriter(FileWriter filewriter, NodeConfigFileInfo info) throws IOException{

		String nodeConfigSeparator = HinemosPropertyCommon.node_config_report_separator.getStringValue();

		if (info == null) {
			return;
		}
		filewriter.write(
				getDoubleQuote(info.managerName) + nodeConfigSeparator +
				getDoubleQuote(info.facilityId) + nodeConfigSeparator +
				getDoubleQuote(info.type.typeName()) + nodeConfigSeparator +
				getDoubleQuote(info.name) + nodeConfigSeparator +
				getDoubleQuote(info.subName) + nodeConfigSeparator +
				getDoubleQuote(info.index) + nodeConfigSeparator +
				getDoubleQuote(info.displayName) + nodeConfigSeparator +
				getDoubleQuote(info.size) + nodeConfigSeparator +
				getDoubleQuote(info.sizeUnit) + nodeConfigSeparator +
				getDoubleQuote(info.description) + nodeConfigSeparator +
				getDoubleQuote(info.version) + nodeConfigSeparator +
				getDoubleQuote(info.release) + nodeConfigSeparator +
				getDoubleQuote(l2s(info.startupDateTime)) + nodeConfigSeparator +
				getDoubleQuote(l2s(info.installDate)) + nodeConfigSeparator +
				getDoubleQuote(info.value) + nodeConfigSeparator +
				getDoubleQuote(info.coreCount) + nodeConfigSeparator +
				getDoubleQuote(info.threadCount) + nodeConfigSeparator +
				getDoubleQuote(info.clockCount) + nodeConfigSeparator +
				getDoubleQuote(info.ipAddress) + nodeConfigSeparator +
				getDoubleQuote(info.macAddress) + nodeConfigSeparator +
				getDoubleQuote(info.diskRpm) + nodeConfigSeparator +
				getDoubleQuote(info.fileSystemType) + nodeConfigSeparator +
				getDoubleQuote(info.characterSet) + nodeConfigSeparator +
				getDoubleQuote(info.protocol) + nodeConfigSeparator +
				getDoubleQuote(info.localIpAddress) + nodeConfigSeparator +
				getDoubleQuote(info.localPort) + nodeConfigSeparator +
				getDoubleQuote(info.foreignIpAddress) + nodeConfigSeparator +
				getDoubleQuote(info.foreignPort) + nodeConfigSeparator +
				getDoubleQuote(info.processName) + nodeConfigSeparator +
				getDoubleQuote(info.pid) + nodeConfigSeparator +
				getDoubleQuote(info.status) + nodeConfigSeparator +
				getDoubleQuote(info.path) + nodeConfigSeparator +
				getDoubleQuote(info.execUser) + nodeConfigSeparator +
				getDoubleQuote(info.vendor) + nodeConfigSeparator +
				getDoubleQuote(info.architecture) + nodeConfigSeparator +
				getDoubleQuote(info.command) + nodeConfigSeparator +
				getDoubleQuote(info.vendorContact) + nodeConfigSeparator +
				getDoubleQuote(info.serialNumber) + nodeConfigSeparator +
				getDoubleQuote(info.count) + nodeConfigSeparator +
				getDoubleQuote(l2s(info.expirationDate)) + nodeConfigSeparator +
				getDoubleQuote(l2s(info.regDate)) + nodeConfigSeparator +
				getDoubleQuote(info.regUser) + nodeConfigSeparator +
				getDoubleQuote(l2s(info.updateDate)) + nodeConfigSeparator +
				getDoubleQuote(info.updateUser) +
				"\n");
	}

	/**
	 * 構成情報のファイル情報
	 */
	private static class NodeConfigFileInfo {
		String managerName = "";			// マネージャ名
		String facilityId = "";				// ファシリティID
		NodeConfigSettingItem type = null;	// 種別
		String name = "";					// 名前
		String subName = "";				// 名前（サブ）
		Integer index = null;				// インデックス
		String displayName = "";			// 表示名
		Integer size = null;				// サイズ
		String sizeUnit = "";				// サイズ単位
		String description = "";			// 説明
		String version = "";				// バージョン
		String release = "";				// リリース
		Long startupDateTime = null;		// 起動日時
		Long installDate = null;			// インストール日時
		String value = "";					// 値
		Integer coreCount = null;			// コア数
		Integer threadCount = null;			// スレッド数
		Integer clockCount = null;			// クロック数
		String ipAddress = "";				// IPアドレス
		String macAddress = "";				// MACアドレス
		Integer diskRpm = null;				// ディスク回転数
		String fileSystemType = "";			// ファイルシステム種別
		String characterSet = "";			// 文字セット
		String protocol = "";				// プロトコル
		String localIpAddress = "";			// ローカルIPアドレス
		String localPort = "";				// ローカルポート
		String foreignIpAddress = "";		// 外部IPアドレス
		String foreignPort = "";			// 外部ポート
		String processName = "";			// プロセス名
		Integer pid = null;					// PID
		String status = "";					// ステータス
		String path = "";					// フルパス
		String execUser = "";				// 実行ユーザ
		String vendor = "";					// ベンダー
		String architecture = "";			// アーキテクチャ
		String command = "";				// コマンド
		String vendorContact = "";			// ベンダー連絡先
		String serialNumber = "";			// シリアルナンバー
		Integer count = null;				// 数量
		Long expirationDate = null;			// 有効期限
		Long regDate = null;				// 作成日時
		String regUser = "";				// 作成ユーザ
		Long updateDate = null;				// 更新日時
		String updateUser = "";				// 更新ユーザ

		NodeConfigFileInfo (String managerName, String facilityId, NodeConfigSettingItem type){
			this.managerName = managerName;
			this.facilityId = facilityId;
			this.type = type;
		}
	}

	/**
	 * Long型のエポックミリ秒を日付型に整形する。
	 * @param t
	 * @return
	 */
	private String l2s(Long l) {
		if (l == null || l == 0L) {
			return "";
		}
		// 日付フォーマットおよびタイムゾーンの設定
		String DATE_FORMAT = HinemosPropertyCommon.node_config_report_format.getStringValue();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		sdf.setTimeZone(HinemosTime.getTimeZone());
		return sdf.format(new Date(l));
	}

	/**
	 * メッセージやオリジナルメッセージに改行が含まれている場合や、
	 * 「"」が含まれている場合はMS Excelで読もうとするとおかしくなる。
	 * 改行等が含まれる可能性のある箇所は下記を利用すること。
	 */
	private String getDoubleQuote(String in) {
		if (in == null) {
			return "\"\"";
		}
		return "\"" + in.replace("\"", "\"\"") + "\"";
	}

	/**
	 * メッセージやオリジナルメッセージに改行が含まれている場合や、
	 * 「"」が含まれている場合はMS Excelで読もうとするとおかしくなる。
	 * 改行等が含まれる可能性のある箇所は下記を利用すること。
	 */
	private String getDoubleQuote(Integer in) {
		if (in == null) {
			return "\"\"";
		}
		return "\"" + in.toString().replace("\"", "\"\"") + "\"";
	}

	/**
	 * ノードインスタンスからノード情報を生成する。<BR>
	 * ただし、ノード情報は一部の情報のみ。
	 * 
	 * @param node ノードインスタンス
	 * @return ノード情報
	 */
	private static NodeInfo copyToNodeInfo(NodeInfo node) {
		NodeInfo nodeInfo = new NodeInfo();
		nodeInfo.setFacilityId(node.getFacilityId());
		nodeInfo.setFacilityName(node.getFacilityName());
		nodeInfo.setIpAddressVersion(node.getIpAddressVersion());
		nodeInfo.setIpAddressV4(node.getIpAddressV4());
		nodeInfo.setIpAddressV6(node.getIpAddressV6());
		nodeInfo.setPlatformFamily(node.getPlatformFamily());
		nodeInfo.setDescription(node.getDescription());
		nodeInfo.setOwnerRoleId(node.getOwnerRoleId());
		long createDateTime = node.getCreateDatetime().longValue();
		nodeInfo.setCreateDatetime(createDateTime);
		long modifyDateTime = node.getModifyDatetime().longValue();
		nodeInfo.setModifyDatetime(modifyDateTime);
		return nodeInfo;
	}

	/**
	 * ファシリティインスタンスからファシリティ情報を生成する。<BR>
	 * ただし、ファシリティ情報は以下の形式で格納されている。<BR>
	 * <PRE>
	 * {
	 *    {facilityId1, facilityName1, description1, displaySortOrder1},
	 *    {facilityId2, facilityName2, description2, displaySortOrder2},
	 *    ...
	 * }
	 * </PRE>
	 * 
	 * @param scope ファシリティインスタンス
	 * @return ファシリティ情報
	 */
	private static FacilityInfo copyToFacilityInfo(FacilityInfo facility) {
		FacilityInfo facilityInfo = new FacilityInfo();
		if (facility instanceof NodeInfo) {
			facilityInfo.setFacilityType(FacilityConstant.TYPE_NODE);
		} else {
			facilityInfo.setFacilityType(FacilityConstant.TYPE_SCOPE);
		}
		facilityInfo.setFacilityId(facility.getFacilityId());
		facilityInfo.setFacilityName(facility.getFacilityName());
		facilityInfo.setDescription(facility.getDescription());
		facilityInfo.setDisplaySortOrder(facility.getDisplaySortOrder());
		facilityInfo.setIconImage(facility.getIconImage());
		facilityInfo.setOwnerRoleId(facility.getOwnerRoleId());
		long createDateTime = facility.getCreateDatetime().longValue();
		facilityInfo.setCreateDatetime(createDateTime);
		long modifyDateTime = facility.getModifyDatetime().longValue();
		facilityInfo.setModifyDatetime(modifyDateTime);
		return facilityInfo;
	}
}
