/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.accesscontrol.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.model.RoleInfo;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.UserInfo;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.HinemosUnknown;

/**
 * ロールIDとユーザIDとの関連をマップで管理するクラス。
 */
public class UserRoleCache {
	private static Log m_log = LogFactory.getLog( UserRoleCache.class );

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(UserRoleCache.class.getName());
		
		try {
			_lock.writeLock();
			
			HashMap<String, ArrayList<String>> roleUserCache = getRoleUserCache();
			HashMap<String, ArrayList<String>> userRoleCache = getUserRoleCache();
			HashMap<String, ArrayList<SystemPrivilegeInfo>> roleSystemPrivilegeCache = getRoleSystemPrivilegeCache();
			
			if (roleUserCache == null || userRoleCache == null || roleSystemPrivilegeCache == null) {	// not null when clustered
				refresh();
			}
		} finally {
			_lock.writeUnlock();
		}
	}
	
	// ロールとそれに所属するユーザを管理
	@SuppressWarnings("unchecked")
	private static HashMap<String, ArrayList<String>> getRoleUserCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_ACCESS_ROLE_USER);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_ACCESS_ROLE_USER + " : " + cache);
		return cache == null ? null : (HashMap<String, ArrayList<String>>)cache;
	}
	
	private static void storeRoleUserCache(HashMap<String, ArrayList<String>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_ACCESS_ROLE_USER + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_ACCESS_ROLE_USER, newCache);
	}
	
	// ユーザとユーザが所属するロールを管理
	@SuppressWarnings("unchecked")
	private static HashMap<String, ArrayList<String>> getUserRoleCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_ACCESS_USER_ROLE);
		return cache == null ? null : (HashMap<String, ArrayList<String>>)cache;
	}
	
	private static void storeUserRoleCache(HashMap<String, ArrayList<String>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		cm.store(AbstractCacheManager.KEY_ACCESS_USER_ROLE, newCache);
	}
	
	// ロールとそれに割り当てられたシステム権限を管理
	@SuppressWarnings("unchecked")
	private static HashMap<String, ArrayList<SystemPrivilegeInfo>> getRoleSystemPrivilegeCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_ACCESS_ROLE_SYSTEMPRIVILEGE);
		return cache == null ? null : (HashMap<String, ArrayList<SystemPrivilegeInfo>>)cache;
	}
	
	private static void storeRoleSystemPrivilegeCache(HashMap<String, ArrayList<SystemPrivilegeInfo>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		cm.store(AbstractCacheManager.KEY_ACCESS_ROLE_SYSTEMPRIVILEGE, newCache);
	}
	
	/**
	 * ロールIDリストを返す。
	 * refresh実行中の場合、更新前・後のいずれの情報が取得できるかは保証されない。
	 * 
	 * @return ロールIDのリスト。エラー時は空のリストを返す。
	 */
	public static List<String> getAllRoleIdList(){
		HashMap<String, ArrayList<String>> cache = getRoleUserCache();
		return new ArrayList<String>(cache.keySet());
	}


	/**
	 * ユーザIDリストを返す。
	 * @return ユーザIDのリスト。エラー時は空のリストを返す。
	 */
	public static List<String> getAllUserIdList(){
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		HashMap<String, ArrayList<String>> cache = getUserRoleCache();
		return new ArrayList<String>(cache.keySet());
	}

	/**
	 * ロールが関連を持つユーザIDのリストを返す。
	 * 
	 * @param roleId ロールID
	 * @return ユーザIDのリスト。エラー時は空のリストを返す。
	 */
	public static List<String> getUserIdList(String roleId){
		m_log.debug("getUserIdList() : roleId " + roleId);
		
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		HashMap<String, ArrayList<String>> cache = getRoleUserCache();
		return cache.get(roleId);
	}

	/**
	 * ユーザが所属するロールIDのリストを返す。
	 * 
	 * @param userId ユーザID
	 * @return ロールIDのリスト。エラー時は空のリストを返す。
	 */
	public static List<String> getRoleIdList(String userId){
		m_log.debug("getRoleIdList() : userId " + userId);
		
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		HashMap<String, ArrayList<String>> cache = getUserRoleCache();
		return cache.get(userId);
	}

	/**
	 * ロールが保持するシステム権限のリストを返す。
	 * 
	 * @param roleId ロールID
	 * @return システム権限のリストを返す。
	 * @throws HinemosUnknown
	 */
	public static List<SystemPrivilegeInfo> getSystemPrivilegeList(String roleId) throws HinemosUnknown{
		m_log.debug("getSystemPrivilegeList() : roleId " + roleId);
		
		// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
		// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
		HashMap<String, ArrayList<SystemPrivilegeInfo>> cache = getRoleSystemPrivilegeCache();
		return cache.get(roleId);
	}

	/**
	 * ユーザが指定された権限を保持しているか確認する。
	 * 
	 * @param userId ユーザID
	 * @param systemPrivilegeInfo チェック対象のシステム権限
	 * @return true:権限あり、false:権限なし
	 * @throws HinemosUnknown
	 */
	public static boolean isSystemPrivilege(String userId, SystemPrivilegeInfo info) throws HinemosUnknown{
		m_log.debug("isSystemPrivilege() : userId " + userId);
		
		// ユーザに対応したロールを取得
		List<String> roleIdList = getRoleIdList(userId);
		
		for (String roleId : roleIdList) {
			List<SystemPrivilegeInfo> systemPrivilegeList = getSystemPrivilegeList(roleId);
			for (SystemPrivilegeInfo cache: systemPrivilegeList) {
				if (cache.getSystemFunction().equals(info.getSystemFunction()) &&
						cache.getSystemPrivilege().equals(info.getSystemPrivilege())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 全件リフレッシュする。
	 */
	public static void refresh(){
		m_log.info("refreshing cache : " + UserRoleCache.class.getSimpleName());
		
		try {
			_lock.writeLock();
			
			long startTime = System.currentTimeMillis();
			new JpaTransactionManager().getEntityManager().clear();
			
			// ロールに所属するユーザを取得
			HashMap<String, ArrayList<String>> roleUserMap = new HashMap<String, ArrayList<String>>();
			List<RoleInfo> roleEntities = QueryUtil.getAllRole_NONE();
			
			for (RoleInfo roleEntity : roleEntities) {
				ArrayList<String> userIdList = new ArrayList<String>();
				if (roleEntity.getUserInfoList() != null) {
					for (UserInfo userEntity : roleEntity.getUserInfoList()) {
						userIdList.add(userEntity.getUserId());
					}
				}
				roleUserMap.put(roleEntity.getRoleId(), userIdList);
			}
			
			// ロールに割り当てられたシステム権限を取得
			HashMap<String, ArrayList<SystemPrivilegeInfo>> roleSystemPrivilegeMap = new HashMap<String, ArrayList<SystemPrivilegeInfo>>();
			
			for (RoleInfo roleEntity : roleEntities) {
				roleSystemPrivilegeMap.put(roleEntity.getRoleId(),
						roleEntity.getSystemPrivilegeList() != null ? 
							new ArrayList<SystemPrivilegeInfo>(roleEntity.getSystemPrivilegeList()):
							new ArrayList<SystemPrivilegeInfo>()
							);
			}
			
			// ユーザが所属するロールを取得
			HashMap<String, ArrayList<String>> userRoleMap = new HashMap<String, ArrayList<String>>();
			List<UserInfo> userEntities = QueryUtil.getAllUser_NONE();

			for (UserInfo userEntity : userEntities) {
				ArrayList<String> roleIdList = new ArrayList<String>();
				if (userEntity.getRoleList() != null) {
					for (RoleInfo roleEntity : userEntity.getRoleList()) {
						roleIdList.add(roleEntity.getRoleId());
					}
				}
				userRoleMap.put(userEntity.getUserId(), roleIdList);
			}
			
			storeRoleUserCache(roleUserMap);
			storeRoleSystemPrivilegeCache(roleSystemPrivilegeMap);
			storeUserRoleCache(userRoleMap);
			
			m_log.info("refresh UserRoleCache " + (System.currentTimeMillis() - startTime) +
					"ms. roleUserMap size=" + roleUserMap.size() +
					" roleSystemPrivilegeMap size=" + roleSystemPrivilegeMap.size() +
					" userRoleMap size=" + userRoleMap.size());
		} finally {
			_lock.writeUnlock();
		}
	}
}
