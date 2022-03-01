/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.factory;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityExistsException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.auth.Authentication;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.bean.UserTypeConstant;
import com.clustercontrol.accesscontrol.model.RoleInfo;
import com.clustercontrol.accesscontrol.model.UserInfo;
import com.clustercontrol.accesscontrol.util.PasswordHashUtil;
import com.clustercontrol.accesscontrol.util.QueryUtil;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.UnEditableUser;
import com.clustercontrol.fault.UsedUser;
import com.clustercontrol.fault.UserDuplicate;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Singletons;

/**
 * ユーザ情報を更新するファクトリクラス<BR>
 *
 * @version 1.0.0
 * @since 3.2.0
 */
public class LoginUserModifier {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(LoginUserModifier.class);

	/**
	 * ログインユーザを新規登録・変更する。<BR>
	 * 
	 * @param userInfo 新規登録・変更するユーザ情報
	 * @param modifyUserId 作業ユーザID
	 * @param isNew true:新規登録／false:更新
	 * @param withHashedPassword ハッシュ済みパスワードを含んでいるか。true:パスワードも更新/false:パスワードは更新しない
	 * @throws UserDuplicate
	 * @throws UserNotFound
	 * @throws UnEditableUser
	 * @throws HinemosUnknown
	 */
	public static void modifyUserInfo(UserInfo userInfo, String modifyUserId, boolean isNew, boolean withHashedPassword) 
			throws UserDuplicate, UserNotFound, UnEditableUser, HinemosUnknown {

		if(userInfo == null || modifyUserId == null || modifyUserId.compareTo("") == 0){
			return;
		}
		m_log.debug("modifyUserInfo() start (roleId = " + userInfo.getUserId() 
			+ ", modifyUserId = " + modifyUserId + ", isNew = " + isNew + ")");

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {	
			long currentTimeMillis = HinemosTime.currentTimeMillis();
			UserInfo userInfoEntity = null;
			if (isNew) {
				// 新規登録
				// 重複チェック
				jtm.checkEntityExists(UserInfo.class, userInfo.getUserId());
				// 情報設定
				userInfo.setCreateUserId(modifyUserId);
				userInfo.setCreateDate(currentTimeMillis);

				// ALL_USERSロールを設定
				RoleInfo roleInfo = QueryUtil.getRolePK(RoleIdConstant.ALL_USERS);
				List<UserInfo> userList = roleInfo.getUserInfoList();
				userList.add(userInfo);
				roleInfo.setUserInfoList(userList);
				userInfo.setRoleList(new ArrayList<RoleInfo>());
				userInfo.getRoleList().add(roleInfo);
				userInfo.setUserType(UserTypeConstant.LOGIN_USER);	// ユーザ種別の格納
				userInfo.setModifyUserId(modifyUserId);
				userInfo.setModifyDate(HinemosTime.currentTimeMillis());
				jtm.getEntityManager().persist(userInfo);

				// デバッグログ
				if (userInfo.getRoleList() != null) {
					for (RoleInfo role : userInfo.getRoleList()) {
						m_log.info("userInfo.getRoleList(): userid=" + userInfo.getUserId() + ", roleid=" + role.getRoleId());
					}
				}
			} else {
				// 更新
				// インスタンスの取得
				userInfoEntity = QueryUtil.getUserPK(userInfo.getUserId());
				UserInfo modifyUserInfoEntity = QueryUtil.getUserPK(modifyUserId);
				// 内部モジュールユーザは変更不可(システムユーザはシステムユーザのみ変更可能）
				if (userInfoEntity.getUserType().equals(UserTypeConstant.SYSTEM_USER) && !modifyUserInfoEntity.getUserType().equals(UserTypeConstant.SYSTEM_USER)) {
					throw new UnEditableUser();
				} else if (userInfoEntity.getUserType().equals(UserTypeConstant.INTERNAL_USER)) {
					throw new UnEditableUser();
				}
				
				// 情報設定
				userInfoEntity.setUserName(userInfo.getUserName());
				userInfoEntity.setDescription(userInfo.getDescription());
				userInfoEntity.setMailAddress(userInfo.getMailAddress());
				userInfoEntity.setModifyUserId(modifyUserId);
				userInfoEntity.setModifyDate(HinemosTime.currentTimeMillis());
				
				if (withHashedPassword) {
					if (userInfo.getPassword().isEmpty()) {
						m_log.warn("modifyUserInfo () : hashedPassword is empty");
					} else {
						// パスワード変更可能か？
						Singletons.get(Authentication.class).checkInternalPasswordModification(userInfoEntity);

						userInfoEntity.setPassword(userInfo.getPassword());
					}
				}

				// デバッグログ
				if (userInfoEntity.getRoleList() != null) {
					for (RoleInfo role : userInfoEntity.getRoleList()) {
						m_log.info("userInfo.getRoleList(): userid=" + userInfo.getUserId() + ", roleid=" + role.getRoleId());
					}
				}
			}
			m_log.info("successful in modifing a user. (userId = " + userInfo.getUserId() + ")");
		} catch (UserNotFound | UnEditableUser e) {
			throw e;
		} catch (EntityExistsException e) {
			m_log.info("modifyUserInfo() failure to add a user. a user'id is duplicated. (userId = " + userInfo.getUserId() + ")");
			throw new UserDuplicate(e.getMessage(), e);
		} catch (HinemosUnknown e) {
			m_log.warn("modifyUserInfo() failure to modify a user. (userId = " + userInfo.getUserId() + ") "
					+ e.getMessage());
			throw e;
		} catch (Exception e) {
			m_log.warn("modifyUserInfo() failure to modify a user. (userId = " + userInfo.getUserId() + ")", e);
			throw new HinemosUnknown("failure to modify a user. (userId = " + userInfo.getUserId() + ")", e);
		}
	}

	/**
	 * ログインユーザを削除する。<BR>
	 * 
	 * @param userId 削除対象のユーザID
	 * @param modifyUserId 作業ユーザID
	 * @throws UserNotFound
	 * @throws UnEditableUser
	 * @throws UsedUser
	 * @throws HinemosUnknown
	 */
	public static void deleteUserInfo(String userId, String modifyUserId) throws UserNotFound, UsedUser, UnEditableUser, HinemosUnknown {

		if(userId == null || userId.compareTo("") == 0 
				|| modifyUserId == null || modifyUserId.compareTo("") == 0){
			return;
		}

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
	
			// 作業ユーザと削除対象のユーザが一致している場合、削除不可とする
			if (userId.compareTo(modifyUserId) == 0) {
				throw new UsedUser("a user will be deleted is equal to current login user.");
			}

			// 該当するユーザを検索して取得
			UserInfo user = QueryUtil.getUserPK(userId);
			// システムユーザ、内部モジュールユーザは削除不可
			if (user != null && !user.getUserType().equals(UserTypeConstant.LOGIN_USER)) {
				throw new UnEditableUser();
			}
			// リレーションを削除する
			user.unchainRoleInfoList();
			// ユーザを削除する
			em.remove(user);

		} catch (UserNotFound | UnEditableUser e) {
			throw e;
		} catch (UsedUser e) {
			m_log.info("deleteUserInfo() failure to delete a user. (userId = " + userId + ") : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteUserInfo() failure to delete a user. (userId = " + userId + ")", e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		m_log.info("successful in deleting a user. (userId = " + userId + ")");
	}

	/**
	 * ログインユーザに設定されたパスワードを変更する。<BR>
	 * 
	 * @param userId ユーザID
	 * @param password 新しいパスワード文字列(平文)
	 * @param modifyUserId 作業ユーザID
	 * @throws UserNotFound
	 * @throws HinemosUnknown
	 */
	public static void modifyUserPassword(String userId, String password, String modifyUserId) throws UserNotFound, HinemosUnknown {

		if(userId == null || userId.compareTo("") == 0 
				|| password == null || password.compareTo("") == 0
				|| modifyUserId == null || modifyUserId.compareTo("") == 0){
			return;
		}

		// 該当するユーザを検索して取得
		UserInfo user;
		try {
			user = QueryUtil.getUserPK(userId);

			// パスワード変更可能か？
			Singletons.get(Authentication.class).checkInternalPasswordModification(user);

			// パスワードのハッシュ値取得、およびお、Base64でエンコード
			MessageDigest md = MessageDigest.getInstance(PasswordHashUtil.getPasswordHash());
			String hashedPassword = Base64.encodeBase64String(md.digest(password.getBytes()));
			
			// パスワードを反映する
			user.setPassword(hashedPassword);
			user.setModifyUserId(modifyUserId);
			user.setModifyDate(HinemosTime.currentTimeMillis());

		} catch (UserNotFound e) {
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("modifyUserPassword() failure to modify user's password. (userId = " + userId + ")", e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		m_log.info("successful in modifing a user's password. (userId = " + userId + ")");
	}
	
}
