/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.nodemap.bean.ReservedFacilityIdConstant;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityTreeItem;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.FacilityRelationEntity;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * ロールとそのロールが操作可能なファシリティツリーを管理するクラス。
 */
public class FacilityTreeCache {
	private static Log m_log = LogFactory.getLog( FacilityTreeCache.class );

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(FacilityTreeCache.class.getName());
		
		try {
			_lock.writeLock();
			
			HashMap<String, FacilityInfo> facilityCache = getFacilityCache();
			FacilityTreeItem facilityTreeRootCache = getFacilityTreeRootCache();
			HashMap<String, ArrayList<FacilityTreeItem>> facilityTreeItemCache = getFacilityTreeItemCache();
			
			if (facilityCache == null || facilityTreeRootCache == null || facilityTreeItemCache == null) {	// not null when clustered
				refresh();
			}
		} finally {
			_lock.writeUnlock();
		}
	}

	// リポジトリ情報（key: facilityId）
	@SuppressWarnings("unchecked")
	private static HashMap<String, FacilityInfo> getFacilityCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_REPOSITORY_FACILITY);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_REPOSITORY_FACILITY + " : " + cache);
		return cache == null ? null : (HashMap<String, FacilityInfo>)cache;
	}
	
	private static void storeFacilityCache(HashMap<String, FacilityInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_REPOSITORY_FACILITY + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_REPOSITORY_FACILITY, newCache);
	}
	
	// ファシリティツリー
	private static FacilityTreeItem getFacilityTreeRootCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_REPOSITORY_TREE_ROOT);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_REPOSITORY_TREE_ROOT + " : " + cache);
		return cache == null ? null : (FacilityTreeItem)cache;
	}
	
	private static void storeFacilityTreeRootCache(FacilityTreeItem newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_REPOSITORY_TREE_ROOT + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_REPOSITORY_TREE_ROOT, newCache);
	}
	
	// ファシリティツリーマップ(key: facilityId, value: facilityIdに該当するfacilityTreeItem配列)
	@SuppressWarnings("unchecked")
	private static HashMap<String, ArrayList<FacilityTreeItem>> getFacilityTreeItemCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_REPOSITORY_TREE_ITEM);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_REPOSITORY_TREE_ITEM + " : " + cache);
		return cache == null ? null : (HashMap<String, ArrayList<FacilityTreeItem>>)cache;
	}
	
	private static void storeFacilityTreeItemCache(HashMap<String, ArrayList<FacilityTreeItem>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_REPOSITORY_TREE_ITEM + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_REPOSITORY_TREE_ITEM, newCache);
	}
	
	/**
	 * ロールがファシリティの参照権限があるかどうかを返す。
	 * 
	 * @param facilityId チェック対象のファシリティID
	 * @param roleId チェック対象のロールID
	 * @param isNode ノードチェックが必要な場合はtrue
	 * @throws FacilityNotFound ファシリティ未存在エラー
	 * @throws InvalidRole オブジェクト権限エラー
	 * @throws InvalidSetting ノードでない場合のエラー
	 */
	public static void validateFacilityId(String facilityId, String roleId, boolean isNode) throws FacilityNotFound, InvalidRole, InvalidSetting {
		m_log.debug("validateFacilityId() : facilityId = " + facilityId
					+ ", roleId = " + roleId);

		// 存在確認
		FacilityInfo facilityInfo = getFacilityInfo(facilityId);
		if (facilityInfo == null) {
			throw new FacilityNotFound("FacilityId is not exist in repository. : facilityId = " + facilityId);
		}

		if (isNode && facilityInfo.getFacilityType() != FacilityConstant.TYPE_NODE) {
			throw new InvalidSetting("Src FacilityId is not node. : facilityId = " + facilityId);
		}

		if (!isFacilityReadable(facilityId, roleId)) {
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage() + ", facilityId = " + facilityId);
		}
	}

	private static boolean isFacilityReadable(String facilityId, String roleId) {
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		HashMap<String, ArrayList<FacilityTreeItem>> facilityTreeItemCache = getFacilityTreeItemCache();
		
		List<FacilityTreeItem> treeItemList = facilityTreeItemCache.get(facilityId);
		if (treeItemList != null && !treeItemList.isEmpty()) {
			for (FacilityTreeItem item : treeItemList) {
				m_log.debug("item=" + item.getData().getFacilityId());
				if (item.getAuthorizedRoleIdSet().contains(roleId)) {
					return true;
				}
			}
		}
	
		return false;
	}

	/**
	 * ユーザが参照可能なノードを返す。
	 * 
	 * @param userId ユーザID
	 * @return ユーザが参照可能なノード一覧を返す。
	 */
	public static List<FacilityInfo> getNodeFacilityInfoListByUserId(String userId){
		m_log.debug("getNodeListByUserId() : userId " + userId);
		return getNodeFacilityList(getFacilityTreeByUserId(userId));
	}

	/**
	 * ロールが参照可能なノードを返す。
	 * 
	 * @param roleId ロールID
	 * @return ロールが参照可能なノード一覧を返す。
	 */
	public static List<FacilityInfo> getNodeFacilityInfoListByRoleId(String roleId){
		m_log.debug("getNodeListByRoleId() : roleId " + roleId);
		FacilityTreeItem facilityTreeItem = getFacilityTreeByRoleId(roleId);
		return getNodeFacilityList(facilityTreeItem);
	}


	/**
	 * 参照可能なノード一覧を返す。
	 * 
	 * @param facilityTreePrivilege ファシリティのオブジェクト権限
	 * @return 操作可能なノード一覧
	 */
	private static List<FacilityInfo> getNodeFacilityList(FacilityTreeItem facilityTreeItem){
		m_log.debug("getNodeList() ");
		
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		HashMap<String, FacilityInfo> facilityCache = getFacilityCache();
		
		List<FacilityInfo> facilityInfoList = new ArrayList<FacilityInfo>();
		Set<String> facilityIdSet = new HashSet<String>();
		FacilityTreeItem rootItem = null;
		rootItem = facilityTreeItem.clone();

		if (rootItem.getChildrenArray() != null) {
			for (FacilityTreeItem childItem : rootItem.getChildren()) {
				getNodeFacilityListRecursive(childItem, facilityIdSet);
			}
		}

		// リストにファシリティ情報を格納する
		for (String facilityId : facilityIdSet) {
			FacilityInfo facilityInfo = facilityCache.get(facilityId);
			facilityInfo.setNotReferFlg(false);
			facilityInfoList.add(facilityInfo);
		}
		return facilityInfoList;
	}

	/**
	 * 参照可能なノード一覧を返す。
	 * 
	 */
	private static void getNodeFacilityListRecursive(FacilityTreeItem facilityTreeItem, Set<String> facilityIdSet){
		// ノードの場合、格納して処理終了
		if (facilityTreeItem.getData().getFacilityType() == FacilityConstant.TYPE_NODE) {
			String facilityId = facilityTreeItem.getData().getFacilityId();
			if (!facilityIdSet.contains(facilityId)) {
				facilityIdSet.add(facilityId);
			}
			return;
		}
		// 再帰的にノードを格納する
		if (facilityTreeItem.getChildrenArray() != null) {
			for (FacilityTreeItem childItem : facilityTreeItem.getChildrenArray()) {
				getNodeFacilityListRecursive(childItem, facilityIdSet);
			}
		}
	}

	/**
	 * ファシリティツリーを返す。
	 * 
	 * @return ファシリティツリーを返す。
	 */
	public static FacilityTreeItem getAllFacilityTree(){
		m_log.debug("getAllFacilityTree()");
		
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		FacilityTreeItem facilityTreeRootCache = getFacilityTreeRootCache();
		return facilityTreeRootCache;
	}

	/**
	 * ユーザが操作可能なファシリティツリーを返す。
	 * 
	 * @param userId ユーザID
	 * @return ユーザが操作可能なファシリティツリーを返す。
	 */
	public static FacilityTreeItem getFacilityTreeByUserId(String userId){
		m_log.debug("getFacilityTreeByUserId() : userId " + userId);
		return getFacilityTree(UserRoleCache.getRoleIdList(userId));
	}


	/**
	 * ロールが操作可能なファシリティツリーを返す。
	 * 
	 * @param roleId ロールID
	 * @return ロールが操作可能なファシリティツリーを返す。
	 */
	public static FacilityTreeItem getFacilityTreeByRoleId(String roleId){
		m_log.debug("getFacilityTreeByRoleId() : roleId " + roleId);
		return getFacilityTree(roleId);
	}

	private static FacilityTreeItem getFacilityTree(List<String> roleIdList) {
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		FacilityTreeItem facilityTreeRootCache = getFacilityTreeRootCache();
		
		FacilityTreeItem rootItem = null;
		rootItem = facilityTreeRootCache.clone();
		getFacilityTreeRecursive(rootItem, roleIdList);

		return rootItem;
	}

	private static FacilityTreeItem getFacilityTree(String roleId){
		ArrayList<String> roleIdList = new ArrayList<String>();
		roleIdList.add(roleId);

		return getFacilityTree(roleIdList);
	}

	/**
	 * 操作可能なファシリティツリーを返す。
	 * 
	 */
	private static void getFacilityTreeRecursive(FacilityTreeItem facilityTreeItem, List<String> roleIdList){
		Iterator<FacilityTreeItem>iter = facilityTreeItem.getChildren().iterator();
		while (iter.hasNext()) {
			FacilityTreeItem childItem = iter.next();
			HashSet<String> roleIdSet = childItem.getAuthorizedRoleIdSet();
			if (roleIdSet == null || !hasAnyCommonRoleId(roleIdSet, roleIdList)) {
				childItem.getData().setNotReferFlg(true);
			} else {
				childItem.getData().setNotReferFlg(false);
			}

			getFacilityTreeRecursive(childItem, roleIdList);
			
			// 配下のスコープを確認して参照できるスコープがなかったら削除する
			if (childItem.getData().isNotReferFlg() && childItem.getChildren().size() == 0) {
				iter.remove();
			}
		}
	}

	private static boolean hasAnyCommonRoleId(HashSet<String> roleIdSet,
			List<String> roleIdList) {
		for (String roleId : roleIdList) {
			if (roleIdSet.contains(roleId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * ファシリティIDを引数にFacilityInfoを返す
	 */
	public static FacilityInfo getFacilityInfo(String facilityId) {
		m_log.debug("getFacilityInfo() : facilityId " + facilityId);

		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		HashMap<String, FacilityInfo> facilityCache = getFacilityCache();
		
		return facilityCache.get(facilityId);
	}

	/**
	 * 自身が子である親のFacilityInfoのリストを返す
	 * 
	 * @param facilityId
	 * @return list
	 */
	public static List<FacilityInfo> getParentFacilityInfo(String facilityId) {
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		HashMap<String, ArrayList<FacilityTreeItem>> facilityTreeItemCache = getFacilityTreeItemCache();
		
		List<FacilityInfo> list = new ArrayList<FacilityInfo>();
		List<FacilityTreeItem> treeItems = facilityTreeItemCache.get(facilityId);
		if (treeItems == null) {
			return list;
		}
		for (FacilityTreeItem treeItem : treeItems) {
			FacilityTreeItem parentTreeItem = treeItem.getParent();
			if (parentTreeItem != null) {
				list.add(parentTreeItem.getData());
			}
		}
		return list;
	}

	/**
	 * キャッシュ中のファシリティツリー情報から自身が親である直下の子のリストを返す
	 * @param facilityId
	 * @return list
	 */
	public static List<FacilityInfo> getChildFacilityInfoList(String facilityId) {
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		HashMap<String, FacilityInfo> facilityCache = getFacilityCache();
		
		List<FacilityInfo>childFacilityInfoList = new ArrayList<FacilityInfo>();
		Set<String> childFacilityIdSet = getChildFacilityIdSet(facilityId);
		for (String childFacilityId : childFacilityIdSet) {
			childFacilityInfoList.add(facilityCache.get(childFacilityId));
		}
		return childFacilityInfoList;
	}

	public static Set<String> getChildFacilityIdSet(String facilityId) {
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		HashMap<String, ArrayList<FacilityTreeItem>> facilityTreeItemCache = getFacilityTreeItemCache();
		
		HashSet<String> childFacilityIdSet = new HashSet<String>();
		List<FacilityTreeItem> treeItems = facilityTreeItemCache.get(facilityId);
		if (treeItems == null) {
			return childFacilityIdSet;
		}

		for (FacilityTreeItem treeItem : treeItems) {
			for (FacilityTreeItem childTreeItem : treeItem.getChildren()) {
				FacilityInfo childFacilityInfo = childTreeItem.getData();
				childFacilityIdSet.add(childFacilityInfo.getFacilityId());
			}
		}
		return childFacilityIdSet;
	}

	/** ファシリティ関連情報をリフレッシュする */
	public static synchronized void refresh() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			if (!jtm.isNestedEm()) {
				m_log.warn("refresh() : transactioin has not been begined.");
				return;
			}

			try {
				_lock.writeLock();
				
				/*
				 * FacilityInfoMap のリフレッシュ
				 */
				long startTime = HinemosTime.currentTimeMillis();
				em.clear();
				
				// FacilityInfoはFacilityTreeItem再構築時に参照されるため、先に反映させる
				HashMap<String, FacilityInfo>facilityInfoMap = createFacilityInfoMap();
				if (facilityInfoMap == null) {
					return;
				}
				long infoMapRefreshTime = HinemosTime.currentTimeMillis() - startTime;
				m_log.info("refresh() : FacilityInfoMap(Cache) " + infoMapRefreshTime + "ms. size=" + facilityInfoMap.size());

				/*
				 * FacilityTreeItem のリフレッシュ
				 */
				startTime = HinemosTime.currentTimeMillis();
				FacilityTreeItem facilityTreeItem = createFacilityTreeItem(facilityInfoMap);
				if (facilityTreeItem == null) {
					return;
				}
				long treeItemRefreshTime = HinemosTime.currentTimeMillis() - startTime;
				m_log.info("refresh() : FacilityTreeItem(Cache) " + treeItemRefreshTime + "ms");

				//FacilityTreeItemMapのリフレッシュ
				startTime = HinemosTime.currentTimeMillis();
				HashMap<String, ArrayList<FacilityTreeItem>> facilityTreeItemMap = createFacilityTreeItemMap(
						facilityInfoMap, facilityTreeItem);
				long treeItemMapRefreshTime = HinemosTime.currentTimeMillis() - startTime;
				m_log.info("refresh() : FacilityTreeItemMap(Cache) " + treeItemMapRefreshTime + "ms");

				storeFacilityCache(facilityInfoMap);
				storeFacilityTreeRootCache(facilityTreeItem);
				storeFacilityTreeItemCache(facilityTreeItemMap);
			} finally {
				_lock.writeUnlock();
			}
		}
	}

	private static HashMap<String, ArrayList<FacilityTreeItem>> createFacilityTreeItemMap(
			HashMap<String, FacilityInfo> facilityInfoMap,
			FacilityTreeItem facilityTreeItem) {
		HashMap<String, ArrayList<FacilityTreeItem>> facilityTreeItemMap = new HashMap<String, ArrayList<FacilityTreeItem>>();

		for (FacilityTreeItem childTreeItem : facilityTreeItem.getChildren()) {
			createFacilityTreeItemMapRecursive(childTreeItem, facilityTreeItemMap);
		}

		return facilityTreeItemMap;
	}

	private static void createFacilityTreeItemMapRecursive(
			FacilityTreeItem treeItem,
			HashMap<String, ArrayList<FacilityTreeItem>> facilityTreeItemMap) {

		String facilityId = treeItem.getData().getFacilityId();
		ArrayList<FacilityTreeItem> facilityTreeItemList = facilityTreeItemMap.get(facilityId);
		if (facilityTreeItemList == null) {
			facilityTreeItemList = new ArrayList<FacilityTreeItem>();
		}

		facilityTreeItemList.add(treeItem);
		facilityTreeItemMap.put(facilityId, facilityTreeItemList);

		for (FacilityTreeItem childTreeItem : treeItem.getChildren()) {
			createFacilityTreeItemMapRecursive(childTreeItem, facilityTreeItemMap);
		}
	}

	/** キャッシュの情報を出力する **/
	public static void printCache() {
		try {
			_lock.readLock();
			
			Map<String, FacilityInfo> facilityCache = getFacilityCache();
			FacilityTreeItem facilityTreeRootCache = getFacilityTreeRootCache();
			
			/*
			 * m_facilityInfoMap を出力
			 */
			m_log.info("printCache() : FacilityInfo start");
			for(FacilityInfo info: facilityCache.values()) {
				m_log.info("facility id = " + info.getFacilityId() +
						", facility name = " + info.getFacilityName());

			}
			m_log.info("printCache() : FacilityInfo end");

			/*
			 * m_facilityTreeItem を出力
			 */
			m_log.info("printCache() : FacilityTreeItem start");
			String brank = "  ";
			FacilityTreeItem treeItem = facilityTreeRootCache.clone();
			if (treeItem != null) {
				m_log.info("facility id = " + treeItem.getData().getFacilityId());
				for (FacilityTreeItem tree : treeItem.getChildrenArray()) {
					m_log.info(brank + "facility id = " + tree.getData().getFacilityId());
					printFacilityTreeItemRecursive(tree, RepositoryControllerBean.ALL, true, brank);
				}
			}
			m_log.info("printCache() : FacilityTreeItem end");
		} finally {
			_lock.readUnlock();
		}
	}

	/**
	 * スコープ配下にあるファシリティの一覧を出力する。<BR>
	 * 
	 * @param parentFacilityTreeItem スコープのファシリティインスタンス
	 * @param level 取得する階層数
	 * @param facilityList 格納先となるファシリティの配列
	 * @param scopeFlag スコープ自身を含めるか（含める:true 含めない:false)
	 * @param brank 表示用の空欄
	 */
	private static void printFacilityTreeItemRecursive(FacilityTreeItem parentFacilityTreeItem,
			int level, boolean scopeFlag, String brank) {
		/** ローカル変数 */
		boolean recursive = false;
		int nextLevel = 0;

		// 表示用の空欄
		brank = brank + "  ";

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
						m_log.info(brank + "facility id = " + childFacilityTreeItem.getData().getFacilityId());
					}
				} else {
					m_log.info(brank + "facility id = " + childFacilityTreeItem.getData().getFacilityId());
				}
				if (recursive) {
					printFacilityTreeItemRecursive(childFacilityTreeItem, nextLevel, scopeFlag, brank);
				}
			}
		}
	}


	/**
	 * ファシリティ情報一覧をキャッシュに設定する。<BR>
	 * 
	 * @return ConcurrentHashMap<String, FacilityInfo>
	 */
	private static HashMap<String, FacilityInfo> createFacilityInfoMap() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			// トランザクションが開始されていない場合には処理を終了する
			if (!jtm.isNestedEm()) {
				m_log.warn("refresh() : transactioin has not been begined.");
				return null;
			}

			HashMap<String, FacilityInfo> facilityInfoMap = new HashMap<String, FacilityInfo>();

			// ファシリティ情報を全件取得する
			List<FacilityInfo> facilityEntities = QueryUtil.getAllFacility_NONE();
			for (FacilityInfo facilityEntity : facilityEntities) {
				// ファシリティの格納
				FacilityInfo facilityInfo = new FacilityInfo();
				facilityInfo.setFacilityId(facilityEntity.getFacilityId());
				facilityInfo.setFacilityName(facilityEntity.getFacilityName());
				facilityInfo.setFacilityType(facilityEntity.getFacilityType());
				facilityInfo.setDisplaySortOrder(facilityEntity.getDisplaySortOrder());
				facilityInfo.setIconImage(facilityEntity.getIconImage());
				facilityInfo.setBuiltInFlg(facilityEntity instanceof ScopeInfo ? FacilitySelector.isBuildinScope((ScopeInfo)facilityEntity): false);
				facilityInfo.setValid(FacilityUtil.isValid(facilityEntity));
				facilityInfo.setOwnerRoleId(facilityEntity.getOwnerRoleId());
				facilityInfo.setDescription(facilityEntity.getDescription());

				facilityInfoMap.put(facilityEntity.getFacilityId(), facilityInfo);
			}

			return facilityInfoMap;
		}
	}

	/**
	 * ファシリティの木構造を取得し、キャッシュに設定する。<BR>
	 * 
	 * @return FacilityTreeItem
	 */
	private static FacilityTreeItem createFacilityTreeItem(Map<String, FacilityInfo> facilityInfoMap) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			// トランザクションが開始されていない場合には処理を終了する
			if (!jtm.isNestedEm()) {
				m_log.warn("refresh() : transactioin has not been begined.");
				return null;
			}

			m_log.debug("getting tree data of facilities...");

			//Objectにアクセスできるロールの情報を取得
			HashMap<String, ArrayList<String>> objectRoleMap = getObjectRoleMap();

			// 木構造最上位インスタンスの生成
			FacilityInfo rootFacilityInfo = new FacilityInfo();
			rootFacilityInfo.setFacilityId(ReservedFacilityIdConstant.ROOT_SCOPE);
			rootFacilityInfo.setFacilityName(MessageConstant.ROOT.getMessage());
			rootFacilityInfo.setFacilityType(FacilityConstant.TYPE_COMPOSITE);
			FacilityTreeItem rootTreeItem = new FacilityTreeItem(null, rootFacilityInfo);

			// 親子であるFacilityのIDのマップを生成
			List<FacilityRelationEntity> facilityRelationList = QueryUtil.getAllFacilityRelations_NONE();
			Map<String, ArrayList<String>> facilityRelationMap = new HashMap<String, ArrayList<String>>();
			for (FacilityRelationEntity facilityRelationEntity : facilityRelationList) {
				String parentFacilityId = facilityRelationEntity.getParentFacilityId();
				String childFacilityId = facilityRelationEntity.getChildFacilityId();
				ArrayList<String> childFacilityIdList = facilityRelationMap.get(parentFacilityId);
				if (childFacilityIdList == null) {
					childFacilityIdList = new ArrayList<String>();
				}
				childFacilityIdList.add(childFacilityId);
				facilityRelationMap.put(parentFacilityId, childFacilityIdList);
			}

			try {
				for (FacilityInfo facilityEntity : FacilitySelector.getRootScopeList()) {
					createFacilityTreeItemRecursive(rootTreeItem,
							facilityEntity.getFacilityId(), facilityInfoMap,
							facilityRelationMap, objectRoleMap);
				}
				FacilityTreeItem.completeParent(rootTreeItem); // createFacilityTreeItemRecursiveでは親が設定されないので。
			} catch (FacilityNotFound e) {
			} catch (Exception e) {
				m_log.warn("createFacilityTreeItem() failure to get a tree data of facilities. : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}

			m_log.debug("successful in getting tree data of facilities.");
			return rootTreeItem;
		}
	}

	private static HashMap<String, ArrayList<String>> getObjectRoleMap() {
		List<ObjectPrivilegeInfo> objectPrivilegeEntities = com.clustercontrol.accesscontrol.util.QueryUtil
				.getAllObjectPrivilegeByFilter(
						HinemosModuleConstant.PLATFORM_REPOSITORY,
						null,
						null,
						PrivilegeConstant.ObjectPrivilegeMode.READ.toString());

		HashMap<String, ArrayList<String>> objectRoleMap = new HashMap<String, ArrayList<String>>();
		for (ObjectPrivilegeInfo objectPrivilegeEntity : objectPrivilegeEntities) {
			String objectId = objectPrivilegeEntity.getId().getObjectId();
			ArrayList<String> roleIdList = objectRoleMap.get(objectId);
			if (roleIdList == null) {
				roleIdList = new ArrayList<String>();
			}
			roleIdList.add(objectPrivilegeEntity.getId().getRoleId());
			objectRoleMap.put(objectId, roleIdList);
		}
		return objectRoleMap;
	}

	/**
	 * ファシリティの木構造を再帰的に取得する。<BR>
	 * 
	 * @param parentTreeItem 親となるファシリティの木構造
	 * @param facilityId
	 * @param facilityInfoMap
	 * @param facilityRelationMap
	 * @param objectRoleMap
	 * @param roleId ロールID
	 */
	private static void createFacilityTreeItemRecursive(
			FacilityTreeItem parentTreeItem,
			String facilityId,
			Map<String, FacilityInfo> facilityInfoMap,
			Map<String, ArrayList<String>> facilityRelationMap,
			HashMap<String, ArrayList<String>> objectRoleMap) {

		// ファシリティの格納
		FacilityInfo facilityInfo = facilityInfoMap.get(facilityId);
		if (facilityInfo == null) {
			// ここは通らないはず。
			m_log.error("createFacilityTreeItemRecursive : facilityInfo is null. " + facilityId);
			throw new NullPointerException();
		}
		FacilityTreeItem treeItem = new FacilityTreeItem(parentTreeItem, facilityInfo);
		treeItem.setAuthorizedRoleIdSet(getAuthorizedRoleIdSet(facilityInfo,
				parentTreeItem, objectRoleMap));

		// 再帰的にファシリティを格納する
		if (FacilityUtil.isScope_FacilityInfo(facilityInfo)) {
			List<String> childFacilityIdList = facilityRelationMap.get(facilityId);
			if (childFacilityIdList != null) {
				for (String childFacilityId : childFacilityIdList) {
					createFacilityTreeItemRecursive(treeItem, childFacilityId,
							facilityInfoMap, facilityRelationMap, objectRoleMap);
				}
			}
		}
	}

	private static HashSet<String> getAuthorizedRoleIdSet(
			FacilityInfo facilityInfo, FacilityTreeItem parentTreeItem, HashMap<String, ArrayList<String>>objectRoleMap) {

		HashSet<String> roleIdSet = new HashSet<String>();
		// 特権ロール
		roleIdSet.add(RoleIdConstant.ADMINISTRATORS);
		roleIdSet.add(RoleIdConstant.HINEMOS_MODULE);

		// オーナーロール
		// スコープの場合のみ。ノードはスコープの参照権限に従うため、ノードのオーナーロールは含めない
		if(facilityInfo.getFacilityType() == FacilityConstant.TYPE_SCOPE) {
			roleIdSet.add(facilityInfo.getOwnerRoleId());
		}

		// オブジェクト権限が設定されている場合
		ArrayList<String>roleIdList = objectRoleMap.get(facilityInfo.getFacilityId());
		if (roleIdList != null) {
			roleIdSet.addAll(roleIdList);
		}

		//親スコープに参照権限
		if (parentTreeItem != null
				&& parentTreeItem.getData().getFacilityType() == FacilityConstant.TYPE_SCOPE
				&& parentTreeItem.getAuthorizedRoleIdSet() != null) {
			roleIdSet.addAll(parentTreeItem.getAuthorizedRoleIdSet());
		}

		return roleIdSet;
	}
}