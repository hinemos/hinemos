/*

Copyright (C) since 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.accesscontrol.factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.ObjectPrivilegeFilterInfo;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.accesscontrol.bean.RoleTreeItem;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.RoleInfo;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.UserInfo;
import com.clustercontrol.accesscontrol.util.QueryUtil;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.PrivilegeNotFound;
import com.clustercontrol.fault.RoleNotFound;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.util.MessageConstant;

/**
 * ロール情報を検索するファクトリクラス<BR>
 *
 * @version 1.0.0
 * @since 3.2.0
 */
public class RoleSelector {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(RoleSelector.class);

	/**
	 * ロール一覧情報を取得する。<BR>
	 * 
	 * @return ロール情報のリスト
	 * @throws UserNotFound
	 * @throws HinemosUnknown
	 */
	public static ArrayList<RoleInfo> getRoleInfoList() {
		m_log.debug("getting all role...");

		// 全ユーザを取得
		List<RoleInfo> roles = QueryUtil.getAllShowRole();

		m_log.debug("successful in getting all role.");
		if(roles == null || roles.isEmpty()){
			return new ArrayList<>();
		} else {
			return new ArrayList<>(roles);
		}
	}


	/**
	 * 指定のロール情報を取得する。<BR>
	 * 
	 * @return ユーザ情報
	 * @throws HinemosUnknown
	 * @throws RoleNotFound 
	 */
	public static RoleInfo getRoleInfo(String roleId) throws HinemosUnknown, RoleNotFound {
		if(roleId == null || roleId.isEmpty())
			return null;
		
		try {
			// ユーザを取得
			return QueryUtil.getRolePK(roleId);
		} catch (RoleNotFound e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("getRoleInfo() failure to get user. : userId = " + roleId, e);
			throw new HinemosUnknown(e.getMessage() + " : failure to get role.", e);
		}
	}

	/**
	 * ロールツリー情報を取得します。
	 * <P>
	 * <ol>
	 * <li>ロールツリー情報のルート(最上位)のインスタンスを作成します。</li>
	 * <li>ロール情報を取得し、</li>
	 * <li>取得したロール情報の数、ユーザ情報を取得し、ロールツリー情報を作成します。</li>
	 * </ol>
	 * 
	 * @param locale ロケール情報
	 * @param userId ログインユーザのユーザID
	 * @return ロールツリー情報{@link com.clustercontrol.accesscontrol.bean.RoleTreeItem}の階層オブジェクト
	 * @throws UserNotFound
	 * @throws HinemosUnknown 
	 * 
	 */
	public RoleTreeItem getRoleTree(Locale locale, String userId) throws UserNotFound, HinemosUnknown {

		// 最上位インスタンスを作成
		RoleTreeItem top = new RoleTreeItem();

		// ルートインスタンスを作成
		RoleInfo rootRoleItem = new RoleInfo();
		rootRoleItem.setRoleId(RoleSettingTreeConstant.ROOT_ID);
		rootRoleItem.setRoleName(MessageConstant.ROLE.getMessage());
		RoleTreeItem root = new RoleTreeItem(top, rootRoleItem);

		// ロール一覧を取得
		List<RoleInfo> list = QueryUtil.getAllShowRole();
		if (list != null) {
			m_log.debug("roleList size = " + list.size());
		} else {
			throw new HinemosUnknown("roleList is null");
		}

		for (RoleInfo roleInfo : list) {
			if (roleInfo.getUserInfoList() != null) {
				m_log.debug("userList size = " + roleInfo.getUserInfoList().size());
			}

			// ロール情報のRoleTreeItemを作成
			RoleTreeItem role = new RoleTreeItem(root, roleInfo);

			for (UserInfo userInfo : roleInfo.getUserInfoList()) {
				// ユーザ情報のRoleTreeItemを作成
				new RoleTreeItem(role, userInfo);
			}
		}
		removeParent(top);
		return top;
	}


	/**
	 * ユーザが所属するロールID情報を取得する。<BR>
	 * 
	 * @param ユーザID（指定されていない場合はADMINISTRATORS）
	 * @return ユーザID一覧
	 * @throws HinemosUnknown
	 */
	public static ArrayList<String> getOwnerRoleIdList(String userId) throws HinemosUnknown {

		ArrayList<String> list = new ArrayList<String>();
		List<RoleInfo> roleList = null;
		if (userId == null || userId.isEmpty()) {
			// 全ユーザを取得
			roleList = QueryUtil.getAllShowRole();
		} else {
			try {
				// ユーザを取得
				UserInfo userInfo = QueryUtil.getUserPK(userId);
				if (userInfo != null){
					roleList = userInfo.getRoleList();
				}
			} catch (UserNotFound e) {
				// 何もしない
			} catch (Exception e) {
				m_log.warn("getUserRoleInfo() failure to get user. : userId = " + userId, e);
				throw new HinemosUnknown(e.getMessage() + " : failure to get user.", e);
			}
		}
		if(roleList != null && roleList.size() > 0){
			Iterator<RoleInfo> itr = roleList.iterator();
			while(itr.hasNext()){
				RoleInfo roleInfo = itr.next();
				if (!roleInfo.getRoleId().equals(RoleIdConstant.INTERNAL)) {
					list.add(roleInfo.getRoleId());
				}
			}
		}
		return list;
	}

	/**
	 * webサービスでは双方向の参照を保持することができないので、
	 * 親方向への参照を消す。
	 * クライアント側で参照を付与する。
	 * @param roleSettinsgTreeItem
	 */
	private void removeParent(RoleTreeItem roleSettinsgTreeItem) {
		roleSettinsgTreeItem.setParent(null);
		for (RoleTreeItem child : roleSettinsgTreeItem.getChildren()) {
			removeParent(child);
		}
	}

	/**
	 * システム権限一覧情報を取得する。<BR>
	 * 
	 * @return システム権限情報のリスト
	 * @throws HinemosUnknown
	 */
	public static ArrayList<SystemPrivilegeInfo> getSystemPrivilegeInfoList() {

		ArrayList<SystemPrivilegeInfo> list = new ArrayList<SystemPrivilegeInfo>();
		List<SystemPrivilegeInfo> systemPrivileges = null;

		m_log.debug("getting all system privilege...");

		// 全ユーザを取得
		systemPrivileges = QueryUtil.getAllSystemPrivilege();

		if(systemPrivileges != null && systemPrivileges.size() > 0){
			list.addAll(systemPrivileges);
		}

		m_log.debug("successful in getting all system privilege.");
		return list;
	}

	/**
	 * 指定されたユーザIDを条件としてシステム権限一覧情報を取得する。<BR>
	 * 
	 * @param userId ユーザID
	 * @return システム権限情報のリスト
	 * @throws HinemosUnknown
	 */
	public static ArrayList<SystemPrivilegeInfo> getSystemPrivilegeInfoListByUserId(String userId) {
		// 全ユーザを取得
		return QueryUtil.getSystemPrivilegeByUserId(userId);
	}

	/**
	 * 指定された編集種別を条件としてシステム権限一覧情報を取得する。<BR>
	 * 
	 * @param editType 編集種別
	 * @return システム権限情報のリスト
	 * @throws HinemosUnknown
	 */
	public static ArrayList<SystemPrivilegeInfo> getSystemPrivilegeInfoListByEditType(String editType) {
		// 全ユーザを取得
		List<SystemPrivilegeInfo> list = QueryUtil.getSystemPrivilegeByEditType(editType);

		if(list == null || list.isEmpty()){
			return new ArrayList<>();
		} else {
			return new ArrayList<>(list);
		}

	}

	/**
	 * 指定のオブジェクト権限情報を取得する。<BR>
	 * 
	 * @param objectType
	 * @param objectId
	 * @param roleId
	 * @param objectPrivilege
	 * @return オブジェクト権限情報
	 * @throws HinemosUnknown
	 */
	public static ObjectPrivilegeInfo getObjectPrivilegeInfo(
			String objectType,
			String objectId,
			String roleId,
			String objectPrivilege) throws HinemosUnknown {

		ObjectPrivilegeInfo info = null;

		if(objectType != null && objectType.compareTo("") != 0
				&& objectId != null && objectId.compareTo("") != 0
				&& roleId != null && roleId.compareTo("") != 0
				&& objectPrivilege != null && objectPrivilege.compareTo("") != 0){
			try {
				// オブジェクト権限を取得
				info = QueryUtil.getObjectPrivilegePK(objectType, objectId, roleId, objectPrivilege);
			} catch (PrivilegeNotFound e) {
				// 何もしない
			} catch (Exception e) {
				m_log.warn("getRoleInfo() failure to get user. : userId = " + roleId, e);
				throw new HinemosUnknown(e.getMessage() + " : failure to get role.", e);
			}
		}
		return info;
	}

	/**
	 * オブジェクト権限一覧情報を取得する。<BR>
	 * 
	 * @return オブジェクト権限情報のリスト
	 * @throws HinemosUnknown
	 */
	public static ArrayList<ObjectPrivilegeInfo> getObjectPrivilegeInfoList(ObjectPrivilegeFilterInfo filter) {

		ArrayList<ObjectPrivilegeInfo> list = new ArrayList<ObjectPrivilegeInfo>();
		List<ObjectPrivilegeInfo> objectPrivileges = null;

		if (filter == null) {
			// 全ユーザを取得
			objectPrivileges = QueryUtil.getAllObjectPrivilege();
		} else {
			// 全ユーザを取得
			objectPrivileges = QueryUtil.getAllObjectPrivilegeByFilter(
					filter.getObjectType(),
					filter.getObjectId(),
					filter.getRoleId(),
					filter.getObjectPrivilege());
		}

		if(objectPrivileges != null && objectPrivileges.size() > 0){
			list.addAll(objectPrivileges);
		}
		return list;
	}

}
