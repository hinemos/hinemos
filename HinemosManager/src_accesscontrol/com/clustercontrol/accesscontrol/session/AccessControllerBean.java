/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.auth.Authentication;
import com.clustercontrol.accesscontrol.auth.AuthenticationParams;
import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.ManagerInfo;
import com.clustercontrol.accesscontrol.bean.ObjectPrivilegeFilterInfo;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.bean.RoleTreeItem;
import com.clustercontrol.accesscontrol.bean.RoleTypeConstant;
import com.clustercontrol.accesscontrol.factory.LoginUserModifier;
import com.clustercontrol.accesscontrol.factory.LoginUserSelector;
import com.clustercontrol.accesscontrol.factory.RoleModifier;
import com.clustercontrol.accesscontrol.factory.RoleSelector;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.RoleInfo;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.UserInfo;
import com.clustercontrol.accesscontrol.util.ObjectPrivilegeUtil;
import com.clustercontrol.accesscontrol.util.ObjectPrivilegeValidator;
import com.clustercontrol.accesscontrol.util.OptionManager;
import com.clustercontrol.accesscontrol.util.QueryUtil;
import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.accesscontrol.util.UserRoleCacheRefreshCallback;
import com.clustercontrol.accesscontrol.util.UserValidator;
import com.clustercontrol.accesscontrol.util.VersionUtil;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
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
import com.clustercontrol.filtersetting.session.FilterSettingControllerBean;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilitySortOrderConstant;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.repository.factory.FacilityModifier;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.FacilityIdCacheInitCallback;
import com.clustercontrol.repository.util.FacilityTreeCacheRefreshCallback;
import com.clustercontrol.repository.util.RepositoryChangedNotificationCallback;
import com.clustercontrol.repository.util.RepositoryValidator;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Singletons;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.bean.AvailableRole;
import com.clustercontrol.xcloud.model.CloudScopeEntity;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.TypedQuery;

/**
 * アカウント機能を実現するSession Bean<BR>
 *
 */
public class AccessControllerBean {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( AccessControllerBean.class );

	/**
	 * ログインチェックの為、本メソッドを使用します。
	 *
	 */
	public ManagerInfo checkLogin() {
		// Hinemosプロパティで設定されたタイムゾーンオフセットを取得し、返却する
		return new ManagerInfo(HinemosTime.getTimeZoneOffset(), OptionManager.getOptions());
	}

	/**
	 * ユーザ検索条件に基づき、ユーザ一覧情報を取得する。<BR>
	 *
	 *
	 * @return ユーザ情報のリスト
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserSelector#getUserList(Property)
	 */
	public ArrayList<UserInfo> getUserInfoList() throws HinemosUnknown {

		JpaTransactionManager jtm = null;
		ArrayList<UserInfo> userInfoList = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			userInfoList = LoginUserSelector.getUserInfoList();
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getUserInfoList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return userInfoList;
	}

	/**
	 * 自身のユーザ情報を取得する。<BR>
	 *
	 *
	 * @return 自身のユーザ情報
	 * @throws HinemosUnknown
	 */
	public UserInfo getOwnUserInfo() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		UserInfo userInfo = null;
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		m_log.debug("getOwnUserInfo() loginUser = " + loginUser);

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			userInfo = LoginUserSelector.getUserInfo(loginUser);
			jtm.commit();
		} catch (UserNotFound e) {
			// 何もしない
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getOwnUserInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return userInfo;
	}


	/**
	 * ユーザ情報を取得する。<BR>
	 *
	 *
	 * @param userId ユーザID
	 * @param mode 取得モード
	 * @param locale ロケール情報
	 * @return ユーザ情報
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.bean.PropertyConstant
	 * @see com.clustercontrol.accesscontrol.factory.UserProperty#getProperty(String, int, Locale)
	 */
	public UserInfo getUserInfo(String userId) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		UserInfo userInfo = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			userInfo = LoginUserSelector.getUserInfo(userId);
			jtm.commit();
		} catch (UserNotFound e) {
			// 何もしない
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			if (jtm != null)
				jtm.rollback();
			m_log.warn("getUserInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return userInfo;
	}

	/**
	 * ユーザ認証を行います。
	 *
	 * @param params
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void authenticate(AuthenticationParams params) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			Singletons.get(Authentication.class).execute(params);
			jtm.commit();
		} catch (InvalidUserPass | InvalidRole | HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			if (jtm != null)
				jtm.rollback();
			m_log.warn("authenticate() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}
	
	/**
	 * ユーザを追加する。<BR>
	 *
	 *
	 * @param info ユーザ情報
	 * @throws HinemosUnknown
	 * @throws UserDuplicate
	 * @throws InvalidSetting
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserModifier#addUser(Property, String, String)
	 */
	public UserInfo addUserInfo(UserInfo info) throws HinemosUnknown, UserDuplicate, InvalidSetting {
		m_log.info("user=" + HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

		UserInfo ret = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			// 入力チェック
			UserValidator.validateUserInfo(info);

			/** メイン処理 */
			LoginUserModifier.modifyUserInfo(info, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID), true, false);
			
			jtm.addCallback(new UserRoleCacheRefreshCallback());
			jtm.commit();
			
			ret = LoginUserSelector.getUserInfo(info.getUserId());
		} catch (UserDuplicate | HinemosUnknown | InvalidSetting e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("addUserInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}
	
	
	/**
	 * ユーザ情報を変更する。<BR>
	 * パスワードは含めない。
	 *
	 *
	 * @param info ユーザ情報
	 * @throws HinemosUnknown
	 * @throws UserNotFound
	 * @throws UnEditableUser
	 * @throws InvalidSetting
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserModifier#modifyUser(Property, String)
	 */
	public void modifyUserInfo(UserInfo info) throws HinemosUnknown, UserNotFound, UnEditableUser, InvalidSetting {
		modifyUserInfo(info, false);
	}

	/**
	 * ユーザ情報を変更する。<BR>
	 * withHashedPasswordをtrueにするとgetUserInfoで取得したユーザ情報をパスワード込みで更新できる。
	 *
	 *
	 * @param info ユーザ情報
	 * @param withHashedPassword ハッシュ済みパスワードを含んでいるか。true:パスワードも更新/false:パスワードは更新しない
	 * @throws HinemosUnknown
	 * @throws UserNotFound
	 * @throws UnEditableUser
	 * @throws InvalidSetting
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserModifier#modifyUser(Property, String)
	 */
	public UserInfo modifyUserInfo(UserInfo info, boolean withHashedPassword) throws HinemosUnknown, UserNotFound, UnEditableUser, InvalidSetting {
		m_log.info("user=" + HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));
		UserInfo ret = null;
		JpaTransactionManager jtm = null;

		/** メイン処理 */
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			//入力チェック
			UserValidator.validateUserInfo(info);

			/** メイン処理 */
			LoginUserModifier.modifyUserInfo(info, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID), false, withHashedPassword);

			jtm.commit();
			
			ret = LoginUserSelector.getUserInfo(info.getUserId());
		} catch (UserNotFound | UnEditableUser | InvalidSetting | HinemosUnknown e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("modifyUserInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}

	/**
	 * ユーザ情報を削除する。<BR>
	 *
	 *
	 * @param userIdList ユーザIDリスト
	 * @throws HinemosUnknown
	 * @throws UserNotFound
	 * @throws UsedUser
	 * @throws UnEditableUser
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserModifier#deleteUser(String, String)
	 */
	public List<UserInfo> deleteUserInfo(List<String> userIdList) throws HinemosUnknown, UserNotFound, UsedUser, UnEditableUser {
		List<UserInfo> ret = new ArrayList<>();
		JpaTransactionManager jtm = null;

		/** メイン処理 */
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for(String userId : userIdList) {
				UserInfo info = LoginUserSelector.getUserInfo(userId);
				ret.add(info);
				LoginUserModifier.deleteUserInfo(userId, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));
			}

			// ユーザに紐づくフィルタ設定も削除する
			new FilterSettingControllerBean().deleteAllFilterSettingsOf(userIdList);

			jtm.addCallback(new UserRoleCacheRefreshCallback());

			jtm.commit();
		} catch (UserNotFound | UnEditableUser | UsedUser | HinemosUnknown e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteUserInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}

	/**
	 * 自分自身のパスワードを変更する。<BR>
	 *
	 *
	 * @param userId ユーザID
	 * @param password パスワード
	 * @throws HinemosUnknown
	 * @throws UserNotFound
	 *
	 */
	public UserInfo changeOwnPassword(String password) throws HinemosUnknown, UserNotFound {
		UserInfo ret = null;
		m_log.debug("changeOwnPassword() password = " + password);

		JpaTransactionManager jtm = null;
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		m_log.debug("changeOwnPassword() loginUser = " + loginUser);

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			LoginUserModifier.modifyUserPassword(loginUser, password, loginUser);

			jtm.commit();
			
			ret = LoginUserSelector.getUserInfo(loginUser);
		} catch (UserNotFound | HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("changeOwnPassword() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}

	/**
	 * パスワードを変更する。<BR>
	 *
	 *
	 * @param userId ユーザID
	 * @param password パスワード
	 * @throws HinemosUnknown
	 * @throws UserNotFound
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserModifier#modifyUserPassword(String, String)
	 */
	public UserInfo changePassword(String userId, String password) throws HinemosUnknown, UserNotFound {
		UserInfo ret = null;
		JpaTransactionManager jtm = null;

		/** メイン処理 */
		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			LoginUserModifier.modifyUserPassword(userId, password, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.commit();
			
			ret = LoginUserSelector.getUserInfo(userId);
		} catch (UserNotFound | HinemosUnknown e){
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (Exception e){
			m_log.warn("changePassword() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}

	/**
	 * ログインしているユーザがADMINISTRATORSロールかHINEMOS_MODULEロールに所属しているかを確認する。<BR>
	 *
	 *
	 * @return ADMINISTRATORSロールに所属していればtrue, そうでなければfalse
	 * @throws HinemosUnknown
	 */
	public boolean isAdministrator() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		boolean rtn = false;

		String loginUserId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		/** メイン処理 */
		try{
			jtm = new JpaTransactionManager();
			jtm.begin();
			// ADMINISTRATORSロールチェック
			List<String> userIdList = UserRoleCache.getUserIdList(RoleIdConstant.ADMINISTRATORS);
			if (userIdList != null && userIdList.contains(loginUserId)) {
				rtn = true;
			}
			if (!rtn) {
				// HINEMOS_MODULEロールチェック
				userIdList = UserRoleCache.getUserIdList(RoleIdConstant.HINEMOS_MODULE);
				if (userIdList != null && userIdList.contains(loginUserId)) {
					rtn = true;
				}
			}
			jtm.commit();
		} catch (Exception e){
			m_log.warn("isAdministrator() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		m_log.debug("isAdministrator() : loginUserId = " + loginUserId + ", " + rtn);
		return rtn;
	}

	/**
	 * ログインしているユーザが指定したユーザ権限を持っているかどうかを確認する。<BR>
	 *
	 *
	 * @param systemPrivilege システム権限情報
	 * @return ユーザ権限を保持していればtrue, そうでなければfalse
	 * @throws HinemosUnknown
	 */
	public boolean isPermission(SystemPrivilegeInfo systemPrivilege) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		boolean rtn = false;

		String loginUserId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		/** メイン処理 */
		try{
			jtm = new JpaTransactionManager();
			jtm.begin();
			if (systemPrivilege != null
				&& UserRoleCache.isSystemPrivilege(loginUserId, systemPrivilege)) {
				rtn = true;
			}
			jtm.commit();
		} catch (Exception e){
			m_log.warn("isPermission() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return rtn;
	}

	/**
	 * ログインしているユーザが指定したユーザ権限を持っているかどうかを確認する。(ログイン認証向け)<BR>
	 *
	 *
	 * @param systemPrivilegeList 必要な権限一覧
	 * @return すべてのユーザ権限を保持していればtrue, そうでなければfalse
	 * @throws HinemosUnknown
	 */
	public boolean isPermissions(List<SystemPrivilegeInfo> systemPrivilegeList) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		//一覧が空なら無条件にtrue
		if (systemPrivilegeList == null || systemPrivilegeList.isEmpty() ){
			return true;
		}

		boolean rtn = true;
		String loginUserId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		/** メイン処理 */
		try{
			jtm = new JpaTransactionManager();
			jtm.begin();
			for ( SystemPrivilegeInfo target :systemPrivilegeList ){
				if( !(UserRoleCache.isSystemPrivilege(loginUserId, target))  ) {
					rtn = false;
					break;
				}
			}
			jtm.commit();
		} catch (Exception e){
			m_log.warn("isPermissions() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return rtn;
	}


	/**
	 * ログインユーザのユーザ名を取得する。<BR>
	 *
	 *
	 * @return ユーザ名
	 * @throws HinemosUnknown
	 * @throws UserNotFound
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserSelector#getUserName(String)
	 */
	public String getUserName() throws HinemosUnknown, UserNotFound {
		JpaTransactionManager jtm = null;
		String userName = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			String loginUserId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			if (loginUserId == null || loginUserId.compareTo("") == 0) {
				throw new HinemosUnknown("userID is null");
			} else {
				userName = LoginUserSelector.getUserInfo(loginUserId).getUserName();
			}
			jtm.commit();
		} catch (UserNotFound | HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("getUserName() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return userName;
	}

	/**
	 * バージョン番号を取得する。<BR>
	 *
	 *
	 */
	public String getVersion() {
		return VersionUtil.getVersion();
	}

	/**
	 * ログインユーザ所属するロール一覧情報を取得する。<BR>
	 *
	 *
	 * @return ロール情報のリスト
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserSelector#getRoleList(Property)
	 */
	public ArrayList<String> getOwnerRoleIdList() throws HinemosUnknown {
		Boolean isAdministrator = (Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR);
		JpaTransactionManager jtm = null;
		ArrayList<String> roleIdList = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			String loginUser = null;
			if (isAdministrator == null || !isAdministrator) {
				loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			}
			roleIdList = RoleSelector.getOwnerRoleIdList(loginUser);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getOwnerRoleIdList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return roleIdList;
	}

	/**
	 * ロール一覧情報を取得する。<BR>
	 *
	 *
	 * @return ロール情報のリスト
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.accesscontrol.factory.LoginUserSelector#getRoleList(Property)
	 */
	public ArrayList<RoleInfo> getRoleInfoList() throws HinemosUnknown {

		JpaTransactionManager jtm = null;
		ArrayList<RoleInfo> roleInfoList = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			roleInfoList = RoleSelector.getRoleInfoList();
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getRoleInfoList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return roleInfoList;
	}


	/**
	 * ロール情報を取得する。<BR>
	 *
	 *
	 * @param roleId ロールID
	 * @return ユーザ情報
	 * @throws HinemosUnknown
	 * @throws RoleNotFound 
	 *
	 */
	public RoleInfo getRoleInfo(String roleId) throws HinemosUnknown, RoleNotFound {
		JpaTransactionManager jtm = null;
		RoleInfo roleInfo = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			roleInfo = RoleSelector.getRoleInfo(roleId);
			jtm.commit();
		} catch (RoleNotFound e) {
			jtm.rollback();
			throw e;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			if (jtm != null)
				jtm.rollback();
			m_log.warn("getUserInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return roleInfo;
	}

	/**
	 * ロールを新規に作成する。
	 * ロール作成時にロールスコープも新規に追加します。<BR>
	 *
	 * @param roleInfo
	 * @throws RoleDuplicate
	 * @throws FacilityDuplicate
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public RoleInfo addRoleInfo(RoleInfo roleInfo)
			throws RoleDuplicate, FacilityDuplicate, InvalidSetting, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		RoleInfo ret = null;
		/** メイン処理 */
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// ロール入力チェック
			RoleValidator.validateRoleInfo(roleInfo);

			// ロール新規作成
			RoleModifier.modifyRoleInfo(roleInfo, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID), true);

			// ロールスコープの作成
			ScopeInfo scopeInfo = new ScopeInfo();
			scopeInfo.setFacilityId(roleInfo.getRoleId());
			scopeInfo.setFacilityName(roleInfo.getRoleName());
			scopeInfo.setFacilityType(FacilityConstant.TYPE_SCOPE);
			scopeInfo.setDescription(roleInfo.getRoleName());
			scopeInfo.setValid(Boolean.TRUE);
			scopeInfo.setOwnerRoleId(roleInfo.getRoleId());
			String parentFacilityId = FacilityTreeAttributeConstant.OWNER_SCOPE;

			//ファシリティIDとの重複チェック
			jtm.checkEntityExists(FacilityInfo.class, roleInfo.getRoleId());
			
			// ロールスコープ入力チェック			
			RepositoryValidator.validateScopeInfo(parentFacilityId, scopeInfo, false);

			// ロールスコープ新規作成
			FacilityModifier.addScope(
					parentFacilityId,
					scopeInfo,
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					FacilitySortOrderConstant.DEFAULT_SORT_ORDER_ROLE_SCOPE);
			
			jtm.addCallback(new FacilityIdCacheInitCallback());
			jtm.addCallback(new UserRoleCacheRefreshCallback());
			jtm.addCallback(new FacilityTreeCacheRefreshCallback());
			jtm.addCallback(new RepositoryChangedNotificationCallback());
			
			jtm.commit();
			
			ret = RoleSelector.getRoleInfo(roleInfo.getRoleId());
		} catch (RoleDuplicate | HinemosUnknown | InvalidSetting |InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (EntityExistsException e) {
			if (jtm != null)
				jtm.rollback();
			throw new FacilityDuplicate(e.getMessage(), e);
		} catch (FacilityNotFound e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addScope() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return ret;
	}

	/**
	 * ロール情報を変更する。<BR>
	 * ロール変更時にロールスコープも変更します。<BR>
	 *
	 *
	 * @param info ロール情報
	 * @throws HinemosUnknown
	 * @throws RoleNotFound
	 * @throws UnEditableRole
	 * @throws FacilityNotFound
	 * @throws InvalidSetting
	 */
	public RoleInfo modifyRoleInfo(RoleInfo roleInfo) throws InvalidSetting, InvalidRole, RoleNotFound, UnEditableRole, FacilityNotFound, HinemosUnknown {
		JpaTransactionManager jtm = null;
		RoleInfo ret = null;
		
		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			// ロール入力チェック
			RoleValidator.validateRoleInfo(roleInfo);

			// ロール更新
			RoleModifier.modifyRoleInfo(roleInfo, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID), false);

			// ロールスコープの作成
			ScopeInfo scopeInfo = new ScopeInfo();
			scopeInfo.setFacilityId(roleInfo.getRoleId());
			scopeInfo.setFacilityName(roleInfo.getRoleName());
			scopeInfo.setFacilityType(FacilityConstant.TYPE_SCOPE);
			scopeInfo.setDescription(roleInfo.getRoleName());
			scopeInfo.setValid(Boolean.TRUE);
			scopeInfo.setOwnerRoleId(roleInfo.getRoleId());

			// ロールスコープ入力チェック
			RepositoryValidator.validateScopeInfo(null, scopeInfo, false);

			// ロールスコープ更新
			FacilityModifier.modifyOwnerRoleScope(scopeInfo, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));
			
			jtm.addCallback(new FacilityIdCacheInitCallback());
			jtm.addCallback(new UserRoleCacheRefreshCallback());
			jtm.addCallback(new FacilityTreeCacheRefreshCallback());
			jtm.addCallback(new RepositoryChangedNotificationCallback());
			
			jtm.commit();
			
			ret = RoleSelector.getRoleInfo(roleInfo.getRoleId());
		} catch (InvalidSetting | RoleNotFound | UnEditableRole | FacilityNotFound | InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("modifyScope() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}

	/**
	 * ロール情報を削除する。<BR>
	 * ロール削除時にロールスコープも削除します。<BR>
	 *
	 *
	 * @param roleIdList ロールIDリスト
	 * @throws UsedFacility
	 * @throws RoleNotFound
	 * @throws UnEditableRole
	 * @throws UsedRole
	 * @throws UsedOwnerRole
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 */
	public List<RoleInfo> deleteRoleInfo(List<String> roleIdList) throws UsedFacility, RoleNotFound, UnEditableRole, UsedRole, UsedOwnerRole, FacilityNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<RoleInfo> retList = new ArrayList<>(); 
		
		/** メイン処理 */
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for(String roleId : roleIdList) {
				retList.add(RoleSelector.getRoleInfo(roleId));
			}
			
			for(String roleId : roleIdList) {
				// 組み込みスコープか確認
				checkIsBuildInRole(roleId);

				// ロールスコープが他機能で使用されているか確認
				new RepositoryControllerBean().checkIsUseFacilityWithChildren(roleId);
				// ロールスコープ削除
				FacilityModifier.deleteOwnerRoleScope(roleId, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

				// ロールがオーナーロールとして使用されているか確認
				RoleValidator.validateDeleteRole(roleId);
				// オブジェクト権限で使用されているか確認
				RoleValidator.checkHavingObjectPrivilege(roleId);
				// ロール削除
				RoleModifier.deleteRoleInfo(roleId, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));
			}
			
			jtm.addCallback(new FacilityIdCacheInitCallback());
			jtm.addCallback(new UserRoleCacheRefreshCallback());
			jtm.addCallback(new FacilityTreeCacheRefreshCallback());
			jtm.addCallback(new RepositoryChangedNotificationCallback());
			
			jtm.commit();
		} catch (UsedFacility | FacilityNotFound | RoleNotFound | UnEditableRole | UsedRole | InvalidRole | UsedOwnerRole | HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteScope() : "
					+ e.getClass().getSimpleName() +", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return retList;
	}

	/**
	 * ロールツリー情報を取得する。<BR>
	 *
	 * @param locale ロケール情報
	 * @return ロールツリー情報{@link com.clustercontrol.accesscontrol.bean.RoleTreeItem}の階層オブジェクト
	 * @throws HinemosUnknown
	 * @throws UserNotFound
	 */
	public RoleTreeItem getRoleTree(Locale locale) throws HinemosUnknown, UserNotFound, InvalidRole {
		m_log.debug("getRoleTree() : locale=" + locale);

		JpaTransactionManager jtm = null;
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		RoleTreeItem item = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			//ロールツリーを取得
			item = new RoleSelector().getRoleTree(locale, loginUser);
			jtm.commit();
		} catch (UserNotFound e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getRoleTree() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return item;
	}

	/**
	 * システム権限一覧情報を取得する。<BR>
	 *
	 * @return システム権限情報のリスト
	 * @throws HinemosUnknown
	 */
	public ArrayList<SystemPrivilegeInfo> getSystemPrivilegeInfoList() throws HinemosUnknown {

		JpaTransactionManager jtm = null;
		ArrayList<SystemPrivilegeInfo> systemPrivilegeInfoList = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			systemPrivilegeInfoList = RoleSelector.getSystemPrivilegeInfoList();
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getSystemPrivilegeInfoList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return systemPrivilegeInfoList;
	}

	/**
	 * 指定されたロールIDを条件としてシステム権限一覧情報を取得する。<BR>
	 *
	 * @param ロールID
	 * @return システム権限情報のリスト
	 * @throws HinemosUnknown
	 */
	public ArrayList<SystemPrivilegeInfo> getSystemPrivilegeInfoListByRoleId(String roleId) throws HinemosUnknown {

		JpaTransactionManager jtm = null;
		ArrayList<SystemPrivilegeInfo> systemPrivilegeInfoList = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			List<SystemPrivilegeInfo> list = UserRoleCache.getSystemPrivilegeList(roleId);
			if (list != null) {
				systemPrivilegeInfoList = new ArrayList<SystemPrivilegeInfo>(list);
			} else {
				m_log.info("getSystemPrivilegeInfoListByRoleId : roleId=" + roleId + " have no SystemPrivileges.");
				systemPrivilegeInfoList = new ArrayList<SystemPrivilegeInfo>();
			}
			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getSystemPrivilegeInfoList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return systemPrivilegeInfoList;
	}

	/**
	 * 指定されたユーザIDを条件としてシステム権限一覧情報を取得する。<BR>
	 *
	 * @param ユーザID
	 * @return システム権限情報のリスト
	 * @throws HinemosUnknown
	 */
	public ArrayList<SystemPrivilegeInfo> getSystemPrivilegeInfoListByUserId(String userId) throws HinemosUnknown {

		JpaTransactionManager jtm = null;
		ArrayList<SystemPrivilegeInfo> systemPrivilegeInfoList = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			systemPrivilegeInfoList = RoleSelector.getSystemPrivilegeInfoListByUserId(userId);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getSystemPrivilegeInfoList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return systemPrivilegeInfoList;
	}

	/**
	 * 指定された編集種別を条件としてシステム権限一覧情報を取得する。<BR>
	 *
	 * @param 編集種別
	 * @return システム権限情報のリスト
	 * @throws HinemosUnknown
	 */
	public ArrayList<SystemPrivilegeInfo> getSystemPrivilegeInfoListByEditType(String editType) throws HinemosUnknown {

		JpaTransactionManager jtm = null;
		ArrayList<SystemPrivilegeInfo> systemPrivilegeInfoList = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			systemPrivilegeInfoList = RoleSelector.getSystemPrivilegeInfoListByEditType(editType);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getSystemPrivilegeInfoListByEditType() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return systemPrivilegeInfoList;
	}

	/**
	 * ロールへのユーザの割り当てを行います<BR>
	 *
	 * @param roleId　ユーザを割り当てるノード
	 * @param userIds 割り当てさせるユーザ(群)
	 * @throws UnEditableRole
	 * @throws HinemosUnknown
	 */
	public RoleInfo assignUserRole(String roleId, String[] userIds)
			throws UnEditableRole, HinemosUnknown {
		RoleInfo ret = null;
		JpaTransactionManager jtm = null;

		try{
			// トランザクション開始
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			RoleModifier.assignUserToRole(roleId, userIds);
			
			jtm.addCallback(new UserRoleCacheRefreshCallback());
			jtm.addCallback(new FacilityTreeCacheRefreshCallback());
			jtm.addCallback(new RepositoryChangedNotificationCallback());
			
			jtm.commit();
			
			ret = RoleSelector.getRoleInfo(roleId);
		} catch (UnEditableRole | HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (UserNotFound e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("assignUserRole() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}

	/**
	 * ロールへのシステム権限の割り当てを行います<BR>
	 *
	 * @param roleId　システム権限を割り当てるノード
	 * @param systemPrivileges 割り当てさせるシステム権限(群)
	 * @throws UnEditableRole
	 * @throws HinemosUnknown
	 * @throws InvalidSetting 
	 */
	public List<SystemPrivilegeInfo> replaceSystemPrivilegeRole(String roleId, List<SystemPrivilegeInfo> systemPrivileges)
			throws UnEditableRole, HinemosUnknown, InvalidSetting {
		List<SystemPrivilegeInfo> ret = new ArrayList<>();
		JpaTransactionManager jtm = null;

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
			throw new InvalidSetting("replaceSystemPrivilegeRole() : Repository - Read not exists");
		}

		try{
			// トランザクション開始
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			RoleModifier.replaceSystemPrivilegeToRole(roleId, systemPrivileges);
			
			jtm.addCallback(new UserRoleCacheRefreshCallback());
			
			jtm.commit();
			
			for(SystemPrivilegeInfo info : systemPrivileges) {
				ret.add(QueryUtil.getSystemPrivilegePK(info.getId()));
				
			}
		} catch (UnEditableRole | HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (RoleNotFound e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("assignUserRole() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}

	/**
	 * オブジェクト権限検索条件に基づき、オブジェクト権限一覧情報を取得する。<BR>
	 *
	 *
	 * @param filter オブジェクト権限検索条件
	 * @return オブジェクト権限のリスト
	 * @throws HinemosUnknown
	 *
	 */
	public ArrayList<ObjectPrivilegeInfo> getObjectPrivilegeInfoList(ObjectPrivilegeFilterInfo filter) throws HinemosUnknown {

		JpaTransactionManager jtm = null;
		ArrayList<ObjectPrivilegeInfo> objectPrivilegeInfoList = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			objectPrivilegeInfoList = new ArrayList<>();
			for (ObjectPrivilegeInfo objPriv : RoleSelector.getObjectPrivilegeInfoList(filter)) {
				if (canUserRead(objPriv)) {
					objectPrivilegeInfoList.add(objPriv);
				} else {
					m_log.debug("getObjectPrivilegeInfoList: Discard, " + objPriv.getId());
				}
			}

			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getObjectPrivilegeInfoList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return objectPrivilegeInfoList;
	}

	/**
	 * オブジェクト権限情報を取得する。<BR>
	 *
	 *
	 * @param objectType
	 * @param objectId
	 * @param roleId
	 * @param objectPrivilege
	 * @return ユーザ情報
	 * @throws HinemosUnknown
	 *
	 */
	public ObjectPrivilegeInfo getObjectPrivilegeInfo(
			String objectType, String objectId, String roleId, String objectPrivilege) throws HinemosUnknown {

		JpaTransactionManager jtm = null;
		ObjectPrivilegeInfo objectPrivilegeInfo = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			objectPrivilegeInfo = RoleSelector.getObjectPrivilegeInfo(objectType, objectId, roleId, objectPrivilege);
			if (!canUserRead(objectPrivilegeInfo)) {
				m_log.debug("getObjectPrivilegeInfo: Discard, " + objectPrivilegeInfo.getId());
				objectPrivilegeInfo = null;
			}

			jtm.commit();
		} catch (HinemosUnknown e) {
			if (jtm != null)
				jtm.rollback();
			throw e;
		} catch (Exception e) {
			if (jtm != null)
				jtm.rollback();
			m_log.warn("getObjectPrivilegeInfo() : "
				+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return objectPrivilegeInfo;
	}

	/**
	 * クライアントユーザが指定されたオブジェクト権限を参照可能な場合はtrueを返す。
	 */
	private boolean canUserRead(ObjectPrivilegeInfo objPriv) {
		// 管理者なら常に参照可能
		if (HinemosSessionContext.isAdministrator()) {
			return true;
		}
		try {
			// 例外が発生するかどうかのみ必要、戻り値は捨てる。
			ObjectPrivilegeUtil.getObjectPrivilegeObject(objPriv.getObjectType(), objPriv.getObjectId(),
					ObjectPrivilegeMode.READ);
			return true;
		} catch (JobMasterNotFound e) {
			// 呼び出し階層を追ったところ、投げているメソッドが存在しないように見える。
			// もし発生したらログへ記録し、処理は止めずに不許可扱いとする。
			m_log.info("canUserRead: " ,e);
			return false;
		} catch (ObjectPrivilege_InvalidRole e) {
			// 不許可
			return false;
		}
	}

	/**
	 * オブジェクト種別、オブジェクトIDに紐づくオブジェクト権限情報を差し替える。<BR>
	 *
	 * @param objectType オブジェクト種別
	 * @param objectId オブジェクトID
	 * @param info オブジェクト権限情報リスト
	 * @throws PrivilegeDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 *
	 */
	public List<ObjectPrivilegeInfo> replaceObjectPrivilegeInfo(String objectType, String objectId, List<ObjectPrivilegeInfo> list)
			throws PrivilegeDuplicate, UsedObjectPrivilege, HinemosUnknown, InvalidSetting, InvalidRole, JobMasterNotFound {
		m_log.info("user=" + HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));
		List<ObjectPrivilegeInfo> ret = new ArrayList<>();
		JpaTransactionManager jtm = null;
		/** メイン処理 */
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			//入力チェック
			ObjectPrivilegeValidator.validateObjectPrivilegeInfo(objectType, objectId, list);

			// JobMstEntityに対応したオブジェクト権限の編集は
			// 該当データがDBに登録されていない場合は処理終了
			if (HinemosModuleConstant.JOB.equals(objectType)) {
				com.clustercontrol.jobmanagement.util.QueryUtil.getJobMstPK_NONE(new JobMstEntityPK(objectId, objectId));
			}

			// オブジェクトWRITE権限チェック
			ObjectPrivilegeUtil.getObjectPrivilegeObject(objectType, objectId, ObjectPrivilegeMode.MODIFY);

			/** メイン処理 */
			RoleModifier.replaceObjectPrivilegeInfo(objectType, objectId, list,
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));
			
			// スコープのオブジェクト権限処理の場合はファシリティツリー情報、最終更新時刻を更新する
			if (HinemosModuleConstant.PLATFORM_REPOSITORY.equals(objectType)) {
				jtm.addCallback(new FacilityTreeCacheRefreshCallback());
				jtm.addCallback(new RepositoryChangedNotificationCallback());
			}
			
			jtm.commit();

			for(ObjectPrivilegeInfo info : list) {
				ret.add(getObjectPrivilegeInfo(objectType, objectId, info.getRoleId(), info.getObjectPrivilege()));
			}
		} catch (InvalidSetting | UsedObjectPrivilege | PrivilegeDuplicate | HinemosUnknown | JobMasterNotFound e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("replaceObjectPrivilegeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}

	/**
	 * 利用可能なロールID,ロール名のリストを取得する(クラウドVM管理機能より利用される)
	 * @return 利用可能なロールID,ロール名のリスト
	 * @throws InvalidRole
	 */
	public List<AvailableRole> getAvailableRoles() throws InvalidRole {
		TypedQuery<RoleInfo> query = null;
		List<String> excludes = Arrays.asList(RoleIdConstant.INTERNAL, RoleIdConstant.HINEMOS_MODULE);
		Boolean isAdministrator = (Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR);
		if (isAdministrator == null || !isAdministrator) {
			HinemosEntityManager em = Session.current().getEntityManager();
			query = em.createNamedQuery(CloudScopeEntity.findAvailableRoles, RoleInfo.class);
			query.setParameter("userId", Session.current().getHinemosCredential().getUserId());
			query.setParameter("excludes", excludes);
		} else {
			HinemosEntityManager em = Session.current().getEntityManager();
			query = em.createNamedQuery(CloudScopeEntity.findAvailableRolesAsAdmin, RoleInfo.class);
			query.setParameter("excludes", excludes);
		}
		
		List<AvailableRole> availableRoles = new ArrayList<>();
		List<RoleInfo> roles = query.getResultList();
		for (RoleInfo r: roles) {
			availableRoles.add(new AvailableRole(r.getRoleId(), r.getRoleName()));
		}
		return availableRoles;
	}
	
	/**
	 * 引数で与えられたロールIDが組み込みスコープである場合には
	 * HinemosUnknownを送出します。
	 *
	 * @version 6.0.0
	 *
	 * @param roleId チェックを行う対象のファシリティID
	 * @throws RoleNotFound
	 * @throws UnEditableRole
	 */
	private void checkIsBuildInRole(String roleId) throws RoleNotFound, UnEditableRole{
		// 該当するロールを検索して取得
		RoleInfo role = QueryUtil.getRolePK(roleId);
		// システムロール、内部モジュールロールは削除不可
		if (role != null && !role.getRoleType().equals(RoleTypeConstant.USER_ROLE)) {
			throw new UnEditableRole();
		}
	}
}

