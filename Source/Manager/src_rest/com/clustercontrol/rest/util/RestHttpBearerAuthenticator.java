/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.auth.AuthenticationParams;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.util.MessageConstant;

/**
 * HttpBearer認証に関する処理クラス.<br>
 * <br>
 * シングルトンインスタンスである<br>
 * Rest-API向け<br>
 * トークンの有効期限は HinemosTimeではなく、システム時刻で管理する。（HimemosTimeのオフセットをクライアントが考慮不要とするため）<br>
 * 
 */
public class RestHttpBearerAuthenticator {

	// ログ出力用.
	/** ロガー */
	private static Log m_log = LogFactory.getLog(RestHttpBearerAuthenticator.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	private static final RestHttpBearerAuthenticator _instance = new RestHttpBearerAuthenticator();

	// トークン管理テーブル
	private final Map<String, Map<String, Object>> tokenMap = new ConcurrentHashMap<String, Map<String, Object>>();
	// トークン管理テーブル向け ユーザIDキー
	private static final String USERID_KEY = "user";
	// トークン管理テーブル 失効日時（失効を迎えるHinemosTime）キー
	private static final String EXPIRETIME_KEY = "expiretime";
	// トークンの発行からの有効期間（分単位）
	private Integer tokenValidTermMinutes = Integer.MAX_VALUE ;

	/**
	 * privateコンストラクタ(Singletonクラス).
	 */
	private RestHttpBearerAuthenticator() {
	}

	/** Singleton */
	public static RestHttpBearerAuthenticator getInstance() {
		return _instance;
	}

	/**
	 * ログイン
	 * 
	 * @param userId
	 *            ユーザ名
	 * @param password
	 *            パスワード
	 * @result アクセストークン
	 */
	public String login(String userId, String password)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {

		// ユーザー認証
		AuthenticationParams authParams = new AuthenticationParams();
		authParams.setUserId(userId);
		authParams.setPassword(password);
		authParams.setCacheDisabled(false);
		new AccessControllerBean().authenticate(authParams);

		// トークン向けにランダムなIDを生成（重複しないようにUUID）
		String resultToken = UUID.randomUUID().toString();

		// 管理テーブルにトークンと付随情報をセット
		Map<String, Object> tokenPropertyMap = new ConcurrentHashMap<String, Object>();
		tokenPropertyMap.put(USERID_KEY, userId);
		tokenPropertyMap.put(EXPIRETIME_KEY,
				Long.valueOf((this.tokenValidTermMinutes * 60L * 1000L) + System.currentTimeMillis()));

		tokenMap.put(resultToken, tokenPropertyMap);
		
		m_log.info(String.format("User %s has logined.", userId));
		
		return resultToken;
	}

	/**
	 * 再ログイン
	 * 
	 * @param oldToken
	 *            現在のトークン
	 * @result 新しいトークン
	 */
	public String relogin(String oldToken ) {

		// トークン向けにランダムなIDを生成（重複しないようにUUID）
		String resultToken = UUID.randomUUID().toString();

		// 管理テーブルに新しいトークンを設定(古いトークンの削除は 定周期に任せる)
		Map<String, Object> oldTokenPropertyMap = tokenMap.get(oldToken);
		Map<String, Object> newTtokenPropertyMap = new ConcurrentHashMap<String, Object>();

		newTtokenPropertyMap.put(USERID_KEY, oldTokenPropertyMap.get(USERID_KEY));
		newTtokenPropertyMap.put(EXPIRETIME_KEY,
				Long.valueOf((this.tokenValidTermMinutes * 60L * 1000L) + System.currentTimeMillis()));

		tokenMap.put(resultToken, newTtokenPropertyMap);

		m_log.info(String.format("User %s has relogined.", newTtokenPropertyMap.get(USERID_KEY)));
		
		return resultToken;
	}

	/**
	 * ログアウト
	 * 
	 * @param token
	 *            アクセストークン
	 * @return 処理の成否
	 */
	public boolean logout(String token) {

		if (!(tokenMap.containsKey(token))) {
			return false;
		}

		Map<String, Object> map = tokenMap.remove(token);
		
		m_log.info(String.format("User %s has logouted.", map.get(USERID_KEY)));
		
		return true;
	}

	/**
	 * 認証権限チェック.
	 * 
	 * @param authzHeader
	 *            HTTPヘッダの認証情報
	 * @param systemPrivilegeList
	 *            必要なオブジェクト権限レベルのリスト
	 * @param isAdmin
	 *            管理者権限が必要か true:管理者権限を持ってないとエラー、false:管理者権限関係なし.
	 */
	public void authCheck(String authzHeader, ArrayList<SystemPrivilegeInfo> systemPrivilegeList,
			boolean isAdmin) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		String userId = null;

		if (authzHeader == null || authzHeader.isEmpty()) {
			String message = MessageConstant.MESSAGE_USER_AUTH_BEARER_IS_INVALID.getMessage();
			m_log.info(methodName + DELIMITER + message);
			throw new InvalidUserPass(message);
		}
		// トークン取得
		String accessToken = null;
		String[] tokenGetwork = authzHeader.split(" ");
		if (authzHeader.startsWith("Bearer") && tokenGetwork.length == 2) {
			accessToken = tokenGetwork[1];
		} else {
			String message = MessageConstant.MESSAGE_USER_AUTH_BEARER_IS_INVALID.getMessage();
			m_log.info(methodName + DELIMITER + message);
			throw new InvalidUserPass(message);
		}

		// トークン有効確認
		Map<String, Object> propertyMap = tokenMap.get(accessToken);
		if (propertyMap == null) {
			String message = MessageConstant.MESSAGE_USER_AUTH_TOKEN_IS_INVALID.getMessage();
			m_log.info(methodName + DELIMITER + message);
			throw new InvalidUserPass(message);
		}
		if ((Long) propertyMap.get(EXPIRETIME_KEY) < System.currentTimeMillis()) {
			String message = MessageConstant.MESSAGE_USER_AUTH_TOKEN_IS_INVALID.getMessage();
			m_log.info(methodName + DELIMITER + message);
			throw new InvalidUserPass(message);
		}
		HinemosSessionContext.instance().setProperty(HinemosSessionContext.AUTH_TOKEN, accessToken);

		userId = (String) propertyMap.get(USERID_KEY);

		if (m_log.isTraceEnabled()) {
			m_log.trace("authCheck : userId=" + userId);
		}
		try {
			// トークンに紐づくログインユーザIDをスレッドローカルに保管
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, userId);

			// ADMINISTRATORS所属有無チェック（スレッドローカルのユーザIDで実施）
			boolean isAdministrator = new AccessControllerBean().isAdministrator();
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, isAdministrator);
			if (isAdmin && !isAdministrator) {
				String message = MessageConstant.MESSAGE_USER_AUTH_NEED_ADMINISTRATORS_ROLE.getMessage();
				m_log.info(methodName + DELIMITER + message);
				throw new InvalidRole(message);
			}

			// システム権限の保持チェック（スレッドローカルのユーザIDで実施）
			AccessControllerBean controller = new AccessControllerBean();
			boolean result = controller.isPermissions(systemPrivilegeList);
			if (!result) {
				List<String> labels = new ArrayList<>();
				for (SystemPrivilegeInfo it : systemPrivilegeList) {
					labels.add(it.getSystemFunction() + "." + it.getSystemPrivilege());
				}
				String messageArg = String.join(",", labels);
				String message = MessageConstant.MESSAGE_USER_AUTH_NOT_ENOUGH_ROLL.getMessage(messageArg);
				m_log.info(methodName + DELIMITER + message);
				throw new InvalidRole(message);
			}

		} catch (InvalidRole e) {
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		}
	}

	/**
	 * ユーザ取得
	 * 
	 * @param アクセストークン
	 * @return userId
	 */
	public String getUserId(String token) {

		Map<String, Object> propertyMap = tokenMap.get(token);
		if (propertyMap == null) {
			return null;
		}
		return (String) propertyMap.get(USERID_KEY);
	}

	/**
	 * 失効日時 取得
	 * 
	 * @param アクセストークン
	 * @return ExpireTime
	 */
	public Long getExpireTime(String token) {

		Map<String, Object> propertyMap = tokenMap.get(token);
		
		if (propertyMap == null) {
			return null;
		}
		return 	(Long) propertyMap.get(EXPIRETIME_KEY);
	}

	/**
	 * 有効期限の切れたトークンのデータを削除
	 * 
	 */
	public void removeExpireToken() {
		if (m_log.isDebugEnabled()) {
			m_log.debug("removeExpireToken : start ");
		}
		for (Entry<String, Map<String, Object>> entry : tokenMap.entrySet()) {
			if ((Long) entry.getValue().get(EXPIRETIME_KEY) < System.currentTimeMillis()) {
				if (m_log.isDebugEnabled()) {
					m_log.debug("removeExpireToken : remove token. id=" + entry.getKey());
				}
				Map<String, Object> map = tokenMap.remove(entry.getKey());
				
				m_log.info(String.format("User %s's token has expired.", map.get(USERID_KEY)));
			}
		}
	}

	/**
	 * パスワードの変更されたユーザのトークンを削除（認証時とパスワードが変わるため再認証が必要となる）
	 * 
	 * @param userId
	 *            変更対象となったユーザのID
	 * @param execToken
	 *            操作を実行したユーザのアクセストークン
	 */
	public void removePasswordChangeUserToken(String userId,String execToken) {
		if (m_log.isDebugEnabled()) {
			m_log.debug("removePasswordChangeUserToken : start . userId=" + userId);
		}
		for (Entry<String, Map<String, Object>> entry : tokenMap.entrySet()) {
			if (userId.equals((String) entry.getValue().get(USERID_KEY))) {
				if (m_log.isDebugEnabled()) {
					m_log.debug("removePasswordChangeUserToken : remove token. userId =" + userId + ", tokenId="
							+ entry.getKey());
				}
				//操作を実行したトークンは無効化の対象外
				if( execToken.equals(entry.getKey()) ){
					continue;
				}
				tokenMap.remove(entry.getKey());
			}
		}
	}

	/**
	 * 
	 * ログインの有効期間を設定する（分単位）
	 * 
	 * @param validTermMinutes
	 *            ログインの有効期間(分単位)
	 * 
	 */
	public void setLoginValidTerm(int validTermMinutes ) {
		this.tokenValidTermMinutes = validTermMinutes;
	}
	
	/**
	 * 
	 * ログインの有効期間を取得する
	 * 
	 * @return  ログインの有効期間(分単位)
	 * 
	 */
	public Integer getLoginValidTerm() {
		return this.tokenValidTermMinutes;
	}
		
}
