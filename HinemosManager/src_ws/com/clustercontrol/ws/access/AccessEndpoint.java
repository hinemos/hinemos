/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.access;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.ManagerInfo;
import com.clustercontrol.accesscontrol.bean.ObjectPrivilegeFilterInfo;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.bean.RoleTreeItem;
import com.clustercontrol.accesscontrol.model.RoleInfo;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.UserInfo;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.accesscontrol.util.VersionUtil;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.PrivilegeDuplicate;
import com.clustercontrol.fault.RoleDuplicate;
import com.clustercontrol.fault.RoleNotFound;
import com.clustercontrol.fault.UnEditableRole;
import com.clustercontrol.fault.UnEditableUser;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.fault.UsedObjectPrivilege;
import com.clustercontrol.fault.UsedOwnerRole;
import com.clustercontrol.fault.UsedRole;
import com.clustercontrol.fault.UsedUser;
import com.clustercontrol.fault.UserDuplicate;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * アクセス用のWebAPIエンドポイント
 */
@javax.jws.WebService(targetNamespace = "http://access.ws.clustercontrol.com")
public class AccessEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( AccessEndpoint.class );
	private static Log m_opelog = LogFactory.getLog("HinemosOperation");

	/**
	 * echo(WebサービスAPI疎通用)
	 *
	 * 権限必要なし（ユーザ名チェックのみ実施）
	 *
	 * @param str
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public String echo(String str) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return str + ", " + str;
	}

	/**
	 * ログインチェックの為、本メソッドを使用します。
	 *
	 * 権限必要なし（ユーザ名チェックのみ実施）
	 * ログインに成功した場合はHinemosプロパティから取得したタイムゾーンオフセットを返却します。
	 *
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ManagerInfo checkLogin() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("checkLogin");

		try {
			ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
			HttpAuthenticator.authCheck(wsctx, systemPrivilegeList, false, true);  // 認証キャッシュ無効
		} catch (InvalidUserPass e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Login Failed, Method=checkLogin, User="
					+ HttpAuthenticator.getUserAccountString(wsctx));
			throw e;
		}

		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Login, Method=checkLogin, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		return new AccessControllerBean().checkLogin();
	}

	/**
	 * ユーザ検索条件に基づき、ユーザ一覧情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 *
	 * @return ユーザ情報のリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserSelector#getUserList(Property)
	 */
	public ArrayList<UserInfo> getUserInfoList() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getUserInfoList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.ACCESSCONTROL, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Get, Method=getUserInfoListAll, User="
				+ HttpAuthenticator.getUserAccountString(wsctx) );
		return new AccessControllerBean().getUserInfoList();
	}

	/**
	 * 自身のユーザ情報を取得する。<BR>
	 *
	 * @return 自身のユーザ情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 */
	public UserInfo getOwnUserInfo() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getOwnUserInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Get, Method=getOwnUserInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new AccessControllerBean().getOwnUserInfo();
	}

	/**
	 * ユーザ情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 *
	 * @param userId ユーザID
	 * @param mode 取得モード
	 * @param locale ロケール情報
	 * @return ユーザ情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.bean.PropertyConstant
	 * @see com.clustercontrol.accesscontrol.factory.UserProperty#getProperty(String, int, Locale)
	 */
	public UserInfo getUserInfo(String userId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getUserInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.ACCESSCONTROL, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", UserID=");
		msg.append(userId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Get, Method=getUserInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new AccessControllerBean().getUserInfo(userId);
	}

	/**
	 * ユーザを追加する。<BR>
	 *
	 * AccessControlAdd権限が必要
	 *
	 * @param info ユーザ情報
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 * @throws UserDuplicate
	 * @throws InvalidSetting
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserModifier#addUser(Property, String, String)
	 */
	public void addUserInfo(UserInfo info) throws InvalidUserPass, InvalidRole, HinemosUnknown, UserDuplicate, InvalidSetting {
		m_log.debug("addUserInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.ACCESSCONTROL, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", UserID=");
			msg.append(info.getUserId());
		}

		try {
			new AccessControllerBean().addUserInfo(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Add User Failed, Method=addUser, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Add User, Method=addUser, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * ユーザ情報を変更する。<BR>
	 *
	 * AccessControlWrite権限が必要
	 *
	 * @param info ユーザ情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws UserNotFound
	 * @throws UnEditableUser
	 * @throws InvalidSetting
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserModifier#modifyUser(Property, String)
	 */
	public void modifyUserInfo(UserInfo info) throws InvalidUserPass, InvalidRole, HinemosUnknown, UserNotFound, UnEditableUser, InvalidSetting {
		m_log.debug("modifyUserInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.ACCESSCONTROL, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", UserID=");
			msg.append(info.getUserId());
		}

		try {
			new AccessControllerBean().modifyUserInfo(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Change User Failed, Method=modifyUser, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Change User, Method=modifyUser, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	
	public void modifyUserInfoWithHashedPassword(UserInfo info) throws Exception {
		m_log.debug("modifyUserInfoWithHashedPassword");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.ACCESSCONTROL, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", UserID=");
			msg.append(info.getUserId());
		}

		try {
			new AccessControllerBean().modifyUserInfo(info, true);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Change User Failed, Method=modifyUserInfoWithHashedPassword, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Change User, Method=modifyUserInfoWithHashedPassword, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * ユーザ情報を削除する。<BR>
	 *
	 * AccessControlWrite権限が必要
	 *
	 * @param userIdList ユーザIDリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws UserNotFound
	 * @throws UsedUser
	 * @throws UnEditableUser
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserModifier#deleteUser(String, String)
	 */
	public void deleteUserInfo(List<String> userIdList) throws InvalidUserPass, InvalidRole, HinemosUnknown, UserNotFound, UsedUser, UnEditableUser {
		m_log.debug("deleteUserInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.ACCESSCONTROL, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", UserID=");
		msg.append(userIdList);

		try {
			new AccessControllerBean().deleteUserInfo(userIdList);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Delete User Failed, Method=deleteUser, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Delete User, Method=deleteUser, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

	}

	/**
	 * 自分自身のパスワードを変更する。<BR>
	 *
	 * @param userId ユーザID
	 * @param password パスワード
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws UserNotFound
	 *
	 */
	public void changeOwnPassword(String password) throws InvalidUserPass, InvalidRole, HinemosUnknown, UserNotFound {
		m_log.debug("changeOwnPassword");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		try {
			new AccessControllerBean().changeOwnPassword(password);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Change Password Failed, Method=changeOwnPassword, User="
					+ HttpAuthenticator.getUserAccountString(wsctx));
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Change Password, Method=changeOwnPassword, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
	}

	/**
	 * パスワードを変更する。<BR>
	 *
	 * AccessControlWrite権限が必要
	 *
	 * @param userId ユーザID
	 * @param password パスワード
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws UserNotFound
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserModifier#modifyUserPassword(String, String)
	 */
	public void changePassword(String userId, String password) throws InvalidUserPass, InvalidRole, HinemosUnknown, UserNotFound {
		m_log.debug("changePassword");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.ACCESSCONTROL, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", UserID=");
		msg.append(userId);

		try {
			new AccessControllerBean().changePassword(userId, password);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Change Password Failed, Method=changePassword, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Change Password, Method=changePassword, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * ログインしているユーザが指定したユーザ権限を持っているかどうかを確認する。<BR>
	 *
	 * @param userRole ユーザ権限名
	 * @return ユーザ権限を保持していればtrue, そうでなければfalse
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public boolean isPermission(SystemPrivilegeInfo systemPrivilegeInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("isPermission");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SystemPrivilege=");
		msg.append(systemPrivilegeInfo.getSystemFunction() + systemPrivilegeInfo.getSystemPrivilege());
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Check Permission, Method=isPermission, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new AccessControllerBean().isPermission(systemPrivilegeInfo);
	}

	/**
	 * ログインユーザのユーザ名を取得する。<BR>
	 *
	 * @return ユーザ名
	 * @throws HinemosUnknown
	 * @throws UserNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserSelector#getUserName(String)
	 */
	public String getUserName() throws HinemosUnknown, UserNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("getUserName");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Get, Method=getUserName, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new AccessControllerBean().getUserName();
	}

	/**
	 * バージョン番号を取得する（権限必要なし）。
	 *
	 * @param userId ユーザID
	 * @return バージョン番号
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public String getVersion() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getVersion");

		// 操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Get, Method=getVersion, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return VersionUtil.getVersion();
	}

	/**
	 * ロール検索条件に基づき、ロール一覧情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 *
	 * @return ロール情報のリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserSelector#getRoleList(Property)
	 */
	public ArrayList<RoleInfo> getRoleInfoList() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getRoleInfoList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.ACCESSCONTROL, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Get, Method=getRoleInfoListAll, User="
				+ HttpAuthenticator.getUserAccountString(wsctx) );
		return new AccessControllerBean().getRoleInfoList();
	}

	/**
	 * ロール情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 *
	 * @param roleId ロールID
	 * @return ロール情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws RoleNotFound 
	 *
	 * @see com.clustercontrol.bean.PropertyConstant
	 * @see com.clustercontrol.accesscontrol.factory.RoleProperty#getProperty(String, int, Locale)
	 */
	public RoleInfo getRoleInfo(String roleId) throws InvalidUserPass, InvalidRole, HinemosUnknown, RoleNotFound {
		m_log.debug("getRoleInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.ACCESSCONTROL, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", RoleID=");
		msg.append(roleId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Get, Method=getRoleInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new AccessControllerBean().getRoleInfo(roleId);
	}

	/**
	 * ロールを追加する。<BR>
	 *
	 * AccessControlAdd権限が必要
	 *
	 * @param info ロール情報
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 * @throws RoleDuplicate
	 * @throws FacilityDuplicate
	 * @throws UnEditableRole
	 * @throws InvalidSetting
	 *
	 * @see com.clustercontrol.accesscontrol.factory.RoleModifier#addRole(Property, String, String)
	 */
	public void addRoleInfo(RoleInfo info) throws InvalidUserPass, InvalidRole, HinemosUnknown, RoleDuplicate, FacilityDuplicate, InvalidSetting, UnEditableRole {
		m_log.debug("addRoleInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.ACCESSCONTROL, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", RoleID=");
			msg.append(info.getRoleId());
		} else {
			throw new HinemosUnknown("RoleInfo is null.");
		}

		try {
			new AccessControllerBean().addRoleInfo(info);
		} catch (Exception e){
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Add Role Failed, Method=addRole, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Add Role, Method=addRole, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * ロール情報を変更する。<BR>
	 *
	 * AccessControlWrite権限が必要
	 *
	 * @param info ロール情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws RoleNotFound
	 * @throws UnEditableRole
	 * @throws FacilityNotFound
	 * @throws InvalidSetting
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserModifier#modifyRole(Property, String)
	 */
	public void modifyRoleInfo(RoleInfo info) throws InvalidUserPass, InvalidRole, HinemosUnknown, RoleNotFound, UnEditableRole, FacilityNotFound, InvalidSetting {
		m_log.debug("modifyRoleInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.ACCESSCONTROL, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", RoleID=");
			msg.append(info.getRoleId());
		}

		try {
			new AccessControllerBean().modifyRoleInfo(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Change Role Failed, Method=modifyRole, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Change Role, Method=modifyRole, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * ロール情報を削除する。<BR>
	 *
	 * AccessControlWrite権限が必要
	 *
	 * @param roleIdList ロールIDリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws RoleNotFound
	 * @throws UnEditableRole
	 * @throws UsedRole
	 * @throws FacilityNotFound
	 * @throws UsedFacility
	 * @throws UsedOwnerRole
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserModifier#deleteRole(String, String)
	 */
	public void deleteRoleInfo(List<String> roleIdList) throws InvalidUserPass, InvalidRole, HinemosUnknown, RoleNotFound, UnEditableRole, UsedRole, FacilityNotFound, UsedFacility, UsedOwnerRole {
		m_log.debug("deleteRoleInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.ACCESSCONTROL, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", RoleID=");
		msg.append(roleIdList);

		try {
			new AccessControllerBean().deleteRoleInfo(roleIdList);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Delete Role Failed, Method=deleteRole, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Delete Role, Method=deleteRole, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

	}


	/**
	 * ロールツリー情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 *
	 * @throws UserNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public RoleTreeItem getRoleTree() throws HinemosUnknown, UserNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("getJobTree()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.ACCESSCONTROL, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobTree, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new AccessControllerBean().getRoleTree(Locale.getDefault());
	}

	/**
	 * 自身の所属するロールID情報を取得する。<BR>
	 *
	 * 権限必要なし（複数機能で使用するため）
	 *
	 * @return 自身の所属するロールID情報リスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 */
	public ArrayList<String> getOwnerRoleIdList() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getOwnerRoleIdList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Get, Method=getOwnerRoleIdList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new AccessControllerBean().getOwnerRoleIdList();
	}

	/**
	 * システム権限一覧情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 *
	 * @return システム権限情報のリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserSelector#getRoleList(Property)
	 */
	public ArrayList<SystemPrivilegeInfo> getSystemPrivilegeInfoList() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getSystemPrivilegeInfoList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.ACCESSCONTROL, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Get, Method=getSystemPrivilegeInfoList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx) );
		return new AccessControllerBean().getSystemPrivilegeInfoList();
	}

	/**
	 * 指定されたロールIDを条件としてシステム権限一覧情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 *
	 * @param ロールID
	 * @return システム権限情報のリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserSelector#getRoleList(Property)
	 */
	public ArrayList<SystemPrivilegeInfo> getSystemPrivilegeInfoListByRoleId(String roleId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getSystemPrivilegeInfoList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.ACCESSCONTROL, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", RoleID=");
		msg.append(roleId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Get, Method=getSystemPrivilegeInfoListByRoleId, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		return new AccessControllerBean().getSystemPrivilegeInfoListByRoleId(roleId);
	}

	/**
	 * 指定されたユーザIDを条件としてシステム権限一覧情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 *
	 * @param ユーザID
	 * @return システム権限情報のリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserSelector#getRoleList(Property)
	 */
	public ArrayList<SystemPrivilegeInfo> getSystemPrivilegeInfoListByUserId(String userId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getSystemPrivilegeInfoList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.ACCESSCONTROL, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", UserID=");
		msg.append(userId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Get, Method=getSystemPrivilegeInfoListByUserId, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		return new AccessControllerBean().getSystemPrivilegeInfoListByUserId(userId);
	}

	/**
	 * 指定された編集種別を条件としてシステム権限一覧情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 *
	 * @param 編集種別
	 * @return システム権限情報のリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<SystemPrivilegeInfo> getSystemPrivilegeInfoListByEditType(String editType) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getSystemPrivilegeInfoListByEditType : editType=" + editType);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.ACCESSCONTROL, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", EditType=");
		msg.append(editType);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Get, Method=getSystemPrivilegeInfoListByEditType, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		return new AccessControllerBean().getSystemPrivilegeInfoListByEditType(editType);
	}

	/**
	 * ロールへのユーザの割り当てを行います。<BR>
	 *
	 * roleIdで指定されるロールにuserIdsで指定されるユーザ群を
	 * 割り当てます。
	 *
	 * AccessControlWrite権限が必要
	 *
	 * @param roleId　ユーザを割り当てるロール
	 * @param userIds 割り当てさせるユーザ(群)
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws UnEditableRole
	 */
	public void assignUserRole(String roleId, String[] userIds) throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting, UnEditableRole {
		m_log.debug("assignUserRole : roleId=" + roleId + ", userIds=" + Arrays.toString(userIds));
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.ACCESSCONTROL, SystemPrivilegeMode.MODIFY));

		if(!RoleIdConstant.ADMINISTRATORS.equals(roleId)) {
			HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		}
		// ADMINSTRATORSロールのユーザ操作は、ADMINISTRATORSロールに所属しているユーザのみ可能
		else {
			HttpAuthenticator.authCheck(wsctx, systemPrivilegeList, true);
		}

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", RoleID=");
		msg.append(roleId);
		if (userIds != null) {
			msg.append(", UserID=");
			msg.append(Arrays.toString(userIds));
		}

		try {
			new AccessControllerBean().assignUserRole(roleId, userIds);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Assign User Failed, Method=assignUserRole, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Assign User, Method=assignUserRole, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * ロールIDに紐づくシステム権限情報を差し替える。<BR>
	 *
	 * AccessControlWrite権限が必要
	 *
	 * @param roleId　システム権限を割り当てるロール
	 * @param systemPrivileges 割り当てさせるシステム権限(群)
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws UnEditableRole
	 */
	public void replaceSystemPrivilegeRole(String roleId, List<SystemPrivilegeInfo> systemPrivileges) throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting, UnEditableRole {
		m_log.debug("replaceSystemPrivilegeRole");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.ACCESSCONTROL, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// システム権限の中身チェック リポジトリの参照がない場合はInvalidSettingを返す
		boolean isRepositoryRefer = false;
		for (SystemPrivilegeInfo systemPrivilege : systemPrivileges) {
			if(systemPrivilege.getSystemFunction().equals(FunctionConstant.REPOSITORY)
					&& systemPrivilege.getSystemPrivilege().equals(SystemPrivilegeMode.READ.name())) {
				isRepositoryRefer = true;
				break;
			}
		}
		if(!isRepositoryRefer) {
			m_log.warn("replaceSystemPrivilegeRole() : Repository - Read not exists");
			throw new InvalidSetting();
		}

		try {
			new AccessControllerBean().replaceSystemPrivilegeRole(roleId, systemPrivileges);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Assign User Failed, Method=assignSystemPrivilegeRole, User="
					+ HttpAuthenticator.getUserAccountString(wsctx));
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Assign User, Method=assignSystemPrivilegeRole, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
	}

	/**
	 * オブジェクト権限検索条件に基づき、オブジェクト権限一覧情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 *
	 * @param filter オブジェクト権限検索条件
	 * @return オブジェクト権限のリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 */
	public ArrayList<ObjectPrivilegeInfo> getObjectPrivilegeInfoList(ObjectPrivilegeFilterInfo filter) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getObjectPrivilegeInfoList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		// オブジェクト種別によりシステム権限を設定
		if (filter == null) {
			m_log.info("getObjectPrivilegeInfoList() : filter is null");
			// 全オブジェクト権限を返すため、全機能のシステム権限READを確認する
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CALENDAR, SystemPrivilegeMode.READ));
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.READ));
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MAINTENANCE, SystemPrivilegeMode.READ));
		} else if (HinemosModuleConstant.PLATFORM_CALENDAR.equals(filter.getObjectType())
				|| HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN.equals(filter.getObjectType())) {
			// カレンダ、カレンダパターン
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CALENDAR, SystemPrivilegeMode.READ));
		} else if (HinemosModuleConstant.JOB.equals(filter.getObjectType())
				|| HinemosModuleConstant.JOB_KICK.equals(filter.getObjectType())
				|| HinemosModuleConstant.JOBMAP_IMAGE_FILE.equals(filter.getObjectType())
				|| HinemosModuleConstant.JOB_QUEUE.equals(filter.getObjectType())) {
			// ジョブ、ジョブファイルチェック、ジョブスケジュール
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		} else if (HinemosModuleConstant.MONITOR.equals(filter.getObjectType())
				|| HinemosModuleConstant.HUB_LOGFORMAT.equals(filter.getObjectType())) {
			// 監視設定、ログフォーマット
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		} else if (HinemosModuleConstant.PLATFORM_NOTIFY.equals(filter.getObjectType())
				|| HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE.equals(filter.getObjectType())) {
			// 通知、メールテンプレート
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.READ));
		} else if (HinemosModuleConstant.PLATFORM_REPOSITORY.equals(filter.getObjectType())) {
			// スコープ
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		} else if (HinemosModuleConstant.SYSYTEM_MAINTENANCE.equals(filter.getObjectType())) {
			// メンテナンス
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MAINTENANCE, SystemPrivilegeMode.READ));
		}

		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		if(filter != null){
			StringBuffer msg = new StringBuffer();
			msg.append(", ObjectType=");
			msg.append(filter.getObjectType());
			msg.append(", ObjectID=");
			msg.append(filter.getObjectId());
			msg.append(", RoleID=");
			msg.append(filter.getRoleId());
			msg.append(", ObjectPrivilege=");
			msg.append(filter.getObjectPrivilege());

			m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Get, Method=getObjectPrivilegeInfoList, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
		} else {
			m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Get, Method=getObjectPrivilegeInfoList, User="
					+ HttpAuthenticator.getUserAccountString(wsctx) );
		}
		return new AccessControllerBean().getObjectPrivilegeInfoList(filter);
	}

	/**
	 * オブジェクト権限情報を取得する。<BR>
	 *
	 * AccessControlRead権限が必要
	 *
	 * @param objectType オブジェクトタイプ
	 * @param objectId オブジェクトID
	 * @param roleId ロールID
	 * @param objectPrivilege オブジェクト権限
	 * @return オブジェクト権限情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.bean.PropertyConstant
	 * @see com.clustercontrol.accesscontrol.factory.RoleProperty#getProperty(String, int, Locale)
	 */
	public ObjectPrivilegeInfo getObjectPrivilegeInfo(String objectType, String objectId, String roleId, String objectPrivilege)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getObjectPrivilegeInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		// オブジェクト種別によりシステム権限を設定
		if (HinemosModuleConstant.PLATFORM_CALENDAR.equals(objectType)
				|| HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN.equals(objectType)) {
			// カレンダ、カレンダパターン
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CALENDAR, SystemPrivilegeMode.READ));
		} else if (HinemosModuleConstant.JOB.equals(objectType)
				|| HinemosModuleConstant.JOB_KICK.equals(objectType)
				|| HinemosModuleConstant.JOBMAP_IMAGE_FILE.equals(objectType)
				|| HinemosModuleConstant.JOB_QUEUE.equals(objectType)) {
			// ジョブ、ジョブファイルチェック、ジョブスケジュール
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		} else if (HinemosModuleConstant.MONITOR.equals(objectType)
				|| HinemosModuleConstant.HUB_LOGFORMAT.equals(objectType)) {
			// 監視設定、ログフォーマット
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		} else if (HinemosModuleConstant.PLATFORM_NOTIFY.equals(objectType)
				|| HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE.equals(objectType)) {
			// 通知、メールテンプレート
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.READ));
		} else if (HinemosModuleConstant.PLATFORM_REPOSITORY.equals(objectType)) {
			// スコープ
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		} else if (HinemosModuleConstant.SYSYTEM_MAINTENANCE.equals(objectType)) {
			// メンテナンス
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MAINTENANCE, SystemPrivilegeMode.READ));
		}
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ObjectType=");
		msg.append(objectType);
		msg.append(", ObjectID=");
		msg.append(objectId);
		msg.append(", RoleID=");
		msg.append(roleId);
		msg.append(", ObjectPrivilege=");
		msg.append(objectPrivilege);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Get, Method=getObjectPrivilegeInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new AccessControllerBean().getObjectPrivilegeInfo(objectType, objectId, roleId, objectPrivilege);
	}

	/**
	 * オブジェクト種別、オブジェクトIDに紐づくオブジェクト権限情報を差し替える。<BR>
	 *
	 * AccessControlWrite権限が必要
	 *
	 * @param objectType オブジェクト種別
	 * @param objectId オブジェクトID
	 * @param list オブジェクト権限情報リスト
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 * @throws UsedObjectPrivilege
	 * @throws PrivilegeDuplicate
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserModifier#replaceObjectPrivilegeInfo(String, String)
	 */
	public void replaceObjectPrivilegeInfo(String objectType, String objectId, List<ObjectPrivilegeInfo> list)
			throws InvalidUserPass, InvalidRole, InvalidSetting, PrivilegeDuplicate, UsedObjectPrivilege, JobMasterNotFound, HinemosUnknown {
		m_log.debug("replaceObjectPrivilegeInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		// オブジェクト種別によりシステム権限を設定
		if (HinemosModuleConstant.PLATFORM_CALENDAR.equals(objectType)
				|| HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN.equals(objectType)) {
			// カレンダ、カレンダパターン
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CALENDAR, SystemPrivilegeMode.MODIFY));
		} else if (HinemosModuleConstant.JOB.equals(objectType)
				|| HinemosModuleConstant.JOB_KICK.equals(objectType)
				|| HinemosModuleConstant.JOBMAP_IMAGE_FILE.equals(objectType)
				|| HinemosModuleConstant.JOB_QUEUE.equals(objectType)) {
			// ジョブ、ジョブ実行契機
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		} else if (HinemosModuleConstant.MONITOR.equals(objectType)
				|| HinemosModuleConstant.HUB_LOGFORMAT.equals(objectType)) {
			// 監視設定、ログフォーマット
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.MODIFY));
		} else if (HinemosModuleConstant.PLATFORM_NOTIFY.equals(objectType)
				|| HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE.equals(objectType)) {
			// 通知、メールテンプレート
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.MODIFY));
		} else if (HinemosModuleConstant.PLATFORM_REPOSITORY.equals(objectType)) {
			// スコープ
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.MODIFY));
		} else if (HinemosModuleConstant.SYSYTEM_MAINTENANCE.equals(objectType)) {
			// メンテナンス
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MAINTENANCE, SystemPrivilegeMode.MODIFY));
		} else if (HinemosModuleConstant.INFRA.equals(objectType)
				|| HinemosModuleConstant.INFRA_FILE.equals(objectType)) {
			// 環境構築
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.MODIFY));
		} else if (HinemosModuleConstant.HUB_TRANSFER.equals(objectType)) {
			// 収集蓄積
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HUB, SystemPrivilegeMode.MODIFY));
		} else {
			m_log.info("replaceObjectPrivilegeInfo " + objectType);
		}
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		try {
			new AccessControllerBean().replaceObjectPrivilegeInfo(objectType, objectId, list);
		} catch (Exception e){
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Replace ObjectPrivilege, Method=replaceObjectPrivilegeInfo, User="
					+ HttpAuthenticator.getUserAccountString(wsctx));
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_ACCESS + " Replace ObjectPrivilege, Method=replaceObjectPrivilegeInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

	}
}
