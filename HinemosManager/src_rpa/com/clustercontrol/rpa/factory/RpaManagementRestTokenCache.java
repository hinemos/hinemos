/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.factory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.classic.HttpClient;

import com.clustercontrol.fault.RpaManagementToolMasterNotFound;
import com.clustercontrol.rpa.model.RpaManagementToolAccount;
import com.clustercontrol.util.HinemosTime;

/**
 * RPA管理ツールのアクセストークンをキャッシュするクラス<br>
 * RPA管理ツールアカウント毎にアクセストークンを保持します。
 */
public class RpaManagementRestTokenCache {

	/** ロガー */
	private static Log m_log = LogFactory.getLog(RpaManagementRestTokenCache.class);
	/** インスタンス */
	private static RpaManagementRestTokenCache instance = new RpaManagementRestTokenCache();
	/** RPA管理ツールアカウント毎にトークン情報を保持するマップ */
	private Map<String, TokenInfo> tokenInfoMap = new ConcurrentHashMap<>();

	/**
	 * コンストラクタ
	 */
	private RpaManagementRestTokenCache() {
	}

	/**
	 * アクセストークンを保持するクラス
	 */
	private static class TokenInfo {
		/** アクセストークン */
		private String token;
		/** トークン生成時刻 */
		private Long creationTime;
	}

	/**
	 * インスタンスを返します。
	 * 
	 * @return インスタンス
	 */
	public static RpaManagementRestTokenCache getInstance() {
		return instance;
	}

	/**
	 * アクセストークンを返します<br>
	 * 既に有効期限内のアクセストークンがある場合はそれを返します。<br>
	 * アクセストークンの有効期限が切れている場合、再度取得します。
	 * 
	 * @param account
	 *            RPA管理ツールアカウント
	 * @param define
	 *            RESTAPI定義
	 * @param client
	 *            HttpClient
	 * @return アクセストークン
	 * @throws IOException
	 */
	public String getToken(RpaManagementToolAccount account, RpaManagementRestDefine define, HttpClient client)
			throws IOException {
		return getToken(account.getRpaScopeId(), account.getUrl(), account.getAccountId(), account.getPassword(),
				account.getTenantName(), define, client, false);
	}

	/**
	 * アクセストークンを返します<br>
	 * 既に有効期限内のアクセストークンがある場合はそれを返します。<br>
	 * アクセストークンの有効期限が切れている場合、再度取得します。
	 * 
	 * @param account
	 *            RPA管理ツールアカウント
	 * @param define
	 *            RESTAPI定義
	 * @param client
	 *            HttpClient
	 * @param force
	 *            強制的にトークンを更新する。
	 * @return アクセストークン
	 * @throws IOException
	 */
	public String getToken(RpaManagementToolAccount account, RpaManagementRestDefine define, HttpClient client, boolean force)
			throws IOException {
		return getToken(account.getRpaScopeId(), account.getUrl(), account.getAccountId(), account.getPassword(),
				account.getTenantName(), define, client, force);
	}

	/**
	 * アクセストークンを返します<br>
	 * 既に有効期限内のアクセストークンがある場合はそれを返します。<br>
	 * アクセストークンの有効期限が切れている場合、再度取得します。
	 * 
	 * @param rpaScopeId
	 *            RPAスコープID
	 * @param baseUrl
	 *            RPA管理ツールAPI URL
	 * @param accountId
	 *            RPA管理ツールアカウントID
	 * @param password
	 *            RPA管理ツールパスワード
	 * @param tenantName
	 *            RPA管理ツールテナントID
	 * @param define
	 *            API定義
	 * @param client
	 *            HttpClient
	 * @param force
	 *            強制的にトークンを更新する。
	 * @return
	 * @throws IOException
	 */
	public String getToken(String rpaScopeId, String baseUrl, String accountId, String password, String tenantName,
			RpaManagementRestDefine define, HttpClient client, boolean force) throws IOException {
		m_log.info("getToken()");
		// トークン未取得の場合は新たにオブジェクトを生成しMapにセットする
		// Map内のキーの存在確認と値のセットはatomicに行う
		tokenInfoMap.putIfAbsent(rpaScopeId, new TokenInfo());
		TokenInfo tokenInfo = tokenInfoMap.get(rpaScopeId);
		// RPAアカウント毎に排他制御する
		// トークンが未取得か有効期限切れの場合はトークンを新たに取得する
		synchronized (tokenInfo) {
			if (force || tokenInfo.token == null || isTokenExpired(tokenInfo, define.getTokenExpiredMillis())) {
				String token = define.getToken(baseUrl, accountId, password, tenantName, client);
				tokenInfo.token = token;
				tokenInfo.creationTime = HinemosTime.currentTimeMillis();
				m_log.info("getToken() : token refreshed, rpaScopeId=" + rpaScopeId);
			}
		}
		return tokenInfo.token;
	}

	/**
	 * Tokenの有効期限が切れているかどうか確認する。
	 * 
	 * @param creationTime
	 *            トークン生成時刻
	 * @return true : 有効期限切れ / false : 有効期限切れでない
	 * @throws RpaManagementToolMasterNotFound
	 */
	private boolean isTokenExpired(TokenInfo tokenInfo, long expiredMillis) {
		boolean expired = true;
		if (expiredMillis == 0) {
			expired = false; // 0の場合は有効期限なし
		} else {
			expired = HinemosTime.currentTimeMillis() > tokenInfo.creationTime + expiredMillis;
		}
		m_log.debug("isTokenExpired() : expired=" + expired);
		return expired;
	}

}
