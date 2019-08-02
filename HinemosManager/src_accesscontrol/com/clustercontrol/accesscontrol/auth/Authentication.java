/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.auth;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.HinemosManagerMain;
import com.clustercontrol.accesscontrol.auth.ldap.LdapAuthenticator;
import com.clustercontrol.accesscontrol.bean.UserTypeConstant;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.UserInfo;
import com.clustercontrol.accesscontrol.util.QueryUtil;
import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.Transaction;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Singletons;

/**
 * 設定に応じた {@link Authenticator} インスタンスを管理し、認証処理を呼び出します。
 * <p>
 * シングルトンとしての利用を想定して実装しています。
 * {@link Singletons#get(Class)}からインスタンスを取得して使用してください。
 * <p>
 * ユーザ認証設定のプロパティファイルの更新チェックはこのクラスが担当しますが、
 * 実際にプロパティファイルを読み込むかどうかに関しては{@link Authenticator}に任せます。
 */
public class Authentication {
	private static final Log log = LogFactory.getLog(Authentication.class);

	/** 設定ファイルの名前 */
	private static final String PROPERTY_FILE_NAME = "hinemos_authenticator.properties";

	/** [単体テスト用] 外部依存処理の切り出し */
	protected static class External {
		String getAuthenticatorId() {
			return HinemosPropertyCommon.access_authenticator_id.getStringValue();
		}
		
		UserInfo findUserInfo(String userId) throws UserNotFound {
			return QueryUtil.getUserPK(userId);
		}
		
		boolean existsSystemPrivilege(String userId, SystemPrivilegeInfo systemPrivilegeInfo) throws HinemosUnknown {
			return UserRoleCache.isSystemPrivilege(userId, systemPrivilegeInfo);
		}
		
		LdapAuthenticator newLdapAuthenticator(Path configFilePath) {
			return new LdapAuthenticator(configFilePath);
		}
		
		AuthenticationTx newTransaction() {
			return new AuthenticationTx();
		}
		
		Path getEtcPath() {
			return HinemosManagerMain._etcDir;
		}
	}
	
	/** [単体テスト用] DBアクセスの切り出し */
	protected static class AuthenticationTx extends Transaction {
		public List<UserInfo> getAllUser() {
			return QueryUtil.getAllUser_NONE();
		}
	}

	/** [単体テスト用] 外部依存処理 */
	private External external;
	
	/** ログインユーザ用のAuthenticator */
	private Authenticator atorForLoginUsers;

	/** システムユーザ&内部ユーザ用のAuthenticator */
	private Authenticator atorForBuiltinUsers;

	/** 設定ファイルのパス */
	private Path configFilePath;
	
	/**
	 * シングルトンとしての利用を想定して実装しています。
	 * コンストラクタは使用せずに、{@link Singletons#get(Class)}からインスタンスを取得して使用してください。
	 */
	public Authentication() {
		this(new External(), null, null);
	}

	/** [単体テスト用] 外部依存処理を指定するコンストラクタ */
	protected Authentication(External external, Authenticator forLoginUsers, Authenticator forBuiltinUsers) {
		this.external = external;

		configFilePath = external.getEtcPath().resolve(PROPERTY_FILE_NAME);

		atorForLoginUsers = forLoginUsers != null ? forLoginUsers
				: createLoginUserAuthenticator(external.getAuthenticatorId());
		atorForBuiltinUsers = forBuiltinUsers != null ? forBuiltinUsers
				: new HinemosPasswordAuthenticator();

		log.info("ctor: atorForLoginUsers: " + atorForLoginUsers.getSimpleName());
		log.info("ctor: atorForBuiltinUsers: " + atorForBuiltinUsers.getSimpleName());
	}

	/**
	 * 指定されたIDに対応した{@link Authenticator}のインスタンスを生成して返します。
	 */
	private Authenticator createLoginUserAuthenticator(String id) {
		// Hinemos内部パスワード認証
		if (id == null || id.length() == 0 || id.trim().length() == 0) {
			log.info("createLoginUserAuthenticator: Hinemos internal password authentication");
			return new HinemosPasswordAuthenticator();
		}
		// LDAP認証
		if (id.equalsIgnoreCase("ldap")) {
			log.info("createLoginUserAuthenticator: LDAP authentication");
			return external.newLdapAuthenticator(configFilePath);
		}
		// 規定外のIDの場合はデフォルト(Hinemos認証)
		log.warn("createLoginUserAuthenticator: Invalid authenticator ID [" + id + "],"
				+ " Hinemos internal password authentication will be used.");
		return new HinemosPasswordAuthenticator();
	}
	
	/**
	 * 終了処理を行います。
	 */
	public void terminate() {
		atorForLoginUsers.terminate();
		atorForBuiltinUsers.terminate();
	}

	/**
	 * ログインユーザ用の認証機構を返します。
	 */
	public Authenticator getLoginUserAuthenticator() {
		return atorForLoginUsers;
	}

	/**
	 * ビルトインユーザ(システムユーザ及び内部ユーザ)用の認証機構を返します。
	 */
	public Authenticator getBuiltinUserAuthenticator() {
		return atorForBuiltinUsers;
	}

	/**
	 * ユーザ認証と、システム権限チェックを行います。
	 * 
	 * @param params 認証情報。
	 * @throws InvalidUserPass ユーザIDあるいはパスワードに誤りがあります。
	 * @throws InvalidRole システム権限が不足しています。
	 * @throws HinemosUnknown 何らかの問題が発生しました。
	 */
	public void execute(AuthenticationParams params) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		String userId = params.getUserId();
		
		// 認証失敗理由についてクライアントに必要以上の情報を与えないよう、画一化したメッセージを返す
		String failureMessage = "user(" + userId + ")/password is invalid combination";
		
		// ユーザ情報がDBに存在するかをチェック
		UserInfo userInfo; 
		try {
			userInfo = external.findUserInfo(userId);
		} catch (UserNotFound e) {
			log.info("execute: user(" + userId + ") not found.");
			throw new InvalidUserPass(failureMessage);
		}

		// ユーザの種類に合わせて、Authenticatorを選択
		Authenticator ator;
		if (UserTypeConstant.LOGIN_USER.equals(userInfo.getUserType())) {
			ator = atorForLoginUsers;
		} else {
			ator = atorForBuiltinUsers;
		}

		// 認証
		boolean authOk;
		try {
			authOk = ator.execute(params, userInfo);
		} catch (Exception e) {
			log.warn("execute: " + e.getClass().getName() + ", " + e.getMessage());
			log.debug("execute: Stacktrace is", e);
			if (ator.isExternal()) {
				// 外部認証が"不正な資格情報"以外の理由で失敗した場合は、規定メッセージに統一する
				throw new HinemosUnknown(
						MessageConstant.MESSAGE_FAILED_TO_COMMUNICATE_EXTERNAL_AUTH_SERVER.getMessage(), e);
			} else {
				throw new HinemosUnknown(e.getMessage(), e);
			}
		}

		if (!authOk) {
			// 認証が"不正な資格情報"のため失敗した
			log.info("execute: " + failureMessage);
			throw new InvalidUserPass(failureMessage);
		}
		
		// システム権限チェック
		for (SystemPrivilegeInfo systemPrivilegeInfo : params.getRequiredSystemPrivileges()) {
			if (!external.existsSystemPrivilege(userId, systemPrivilegeInfo)) {
				// 持っていないシステム権限が1つでもあったら InvalidRole
				List<String> labels = new ArrayList<>();
				for (SystemPrivilegeInfo it : params.getRequiredSystemPrivileges()) {
					labels.add(it.getSystemFunction() + "." + it.getSystemPrivilege());
				}
				String message = "need-role " + String.join(",", labels);
				log.info("execute: " + message);
				throw new InvalidRole(message);
			}
		}
	}
	
	/**
	 * 指定されたHinemosユーザのパスワード変更が可能かどうかをチェックします。
	 * 例外が発生せずに制御が戻れば、パスワード変更可能です。
	 * 
	 * @throws HinemosUnknown パスワード変更不能の場合に、メッセージIDを詳細メッセージへ設定して投げます。
	 */
	public void checkInternalPasswordModification(UserInfo userInfo) throws HinemosUnknown {
		if (UserTypeConstant.LOGIN_USER.equals(userInfo.getUserType())) {
			if (atorForLoginUsers.isExternal()) {
				throw new HinemosUnknown(MessageConstant.MESSAGE_PASSWORD_IS_UNCHANGEABLE_WHEN_EXTERNAL_AUTH_USED
						.getMessage(atorForLoginUsers.getSimpleName()));
			}
		}
	}
	
	/**
	 * ログインユーザの認証方式が外部認証である場合に限り、
	 * Hinemosの内部DBに保存されている全ログインユーザのパスワードを空欄にします。
	 * (システムユーザ及び内部ユーザに関しては除外します。)
	 * 
	 * @return パスワードを空欄にしたユーザのIDのリスト。
	 */
	public List<String> eraseInternalPasswords() {
		List<String> erasedUserIds = new ArrayList<>();
		if (atorForLoginUsers.isExternal()) {
			try (AuthenticationTx tx = external.newTransaction()) {
				for (UserInfo user : tx.getAllUser()) {
					if (UserTypeConstant.LOGIN_USER.equals(user.getUserType())
							&& !("".equals(user.getPassword()))) {
						user.setPassword("");
						erasedUserIds.add(user.getUserId());
					}
				}
				tx.commit();
			}
			log.info("eraseInternalPasswords: Erased password of: " + String.join(",", erasedUserIds));
		}
		return erasedUserIds;
	}

}
