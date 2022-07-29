/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.auth.ldap;

import java.nio.file.Path;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.auth.AuthenticationParams;
import com.clustercontrol.accesscontrol.auth.Authenticator;
import com.clustercontrol.accesscontrol.auth.helpers.AuthResultCache;
import com.clustercontrol.accesscontrol.auth.ldap.LdapConnection.LdapSearchResult;
import com.clustercontrol.accesscontrol.model.UserInfo;

/**
 * LDAP認証用の {@link Authenticator} です。
 */
public class LdapAuthenticator implements Authenticator {
	private static final Log log = LogFactory.getLog(LdapAuthenticator.class);
	
	/** [単体テスト用] 外部依存処理の切り出し */
	protected static class External {
		LdapAuthenticatorConfig newConfig() {
			return new LdapAuthenticatorConfig();
		}
		
		AuthResultCache newResultCache() {
			return new AuthResultCache();
		}

		LdapConnection newLdapConnection() {
			return new LdapConnection();
		}
	}

	/** [単体テスト用] 外部依存処理 */
	private External external;

	/** 設定ファイル */
	private LdapAuthenticatorConfig config;
	
	/** 認証成功結果キャッシュ */
	private AuthResultCache resultCache;
	
	public LdapAuthenticator(Path configFilePath) {
		this(new External(), configFilePath);
	}
	
	protected LdapAuthenticator(External external, Path configFilePath) {
		this.external = external;
		this.config = external.newConfig();
		this.resultCache = external.newResultCache();

		// 設定ファイル読み込み
		try {
			config.loadPropertiesFile(configFilePath);
		} catch (Exception e) {
			// 例外は出さず、"設定無効"な状態でインスタンスを成立させる
			log.error("initialize: Error. " + e.getClass().getName() + ", " + e.getMessage());
			log.debug("initialize: Stacktrace is...", e);
			config = external.newConfig();
		}
	}

	@Override
	public String getSimpleName() {
		return "LDAP";
	}

	@Override
	public boolean isExternal() {
		return true;
	}

	@Override
	public boolean execute(AuthenticationParams params, UserInfo userInfo) throws Exception {
		return resultCache.authenticate(params, (p) -> {
			// 設定リロードなどによる置き換え(未実装)を考慮して、ローカルへ持ってきてから参照する
			LdapAuthenticatorConfig c = config;
			// プライマリ
			if (!c.isPrimaryServerAvailable()) {
				throw new LdapAccessException("LDAP server configuration is invalid.");
			}
			try {
				return authenticateByLdapServer(p, c.url_1, c.basedn_1, c.searchuserdn_1,
						c.password_1, c.filter_1, c.connect_timeout_1, c.read_timeout_1);
			} catch (LdapAccessException e) {
				// セカンダリ
				if (!c.isSecondaryServerAvailable()) {
					throw e;
				}
				log.debug("execute: Try the secondary.");
				return authenticateByLdapServer(p, c.url_2, c.basedn_2, c.searchuserdn_2,
						c.password_2, c.filter_2, c.connect_timeout_2, c.read_timeout_2);
			}
		});
	}
	
	private boolean authenticateByLdapServer(AuthenticationParams params, String url, String basedn,
			String searchuserdn, String password, String filter, int connectTimeout, int readTimeout)
			throws LdapAccessException {
		try (LdapConnection conn = external.newLdapConnection()) {
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(readTimeout);
			// まずは検索ユーザで接続
			if (!conn.bind(url, searchuserdn, password)) {
				String msg = "Searcher[" + searchuserdn + "] could not be binded.";
				log.warn("executeAuthentication: " + msg);
				throw new LdapAccessException(msg);
			}
			// ユーザIDを埋め込む
			// - HinemosのユーザID仕様としてはLDAPフィルタ式のメタ文字である「* ( ) \ NUL」は含まれないが、
			//   HTTPリクエストヘッダからそのまま渡ってくるため、エスケープする必要がある。
			String filterReplaced = filter.replace("{0}", LdapConnection.escapeRfc2254(params.getUserId()));
			// ログインユーザのDNを探す
			List<LdapSearchResult> srs = conn.searchSubTree(basedn, filterReplaced);
			// 該当するエントリなし
			if (srs.size() == 0) {
				log.debug("executeAuthentication: User[" + params.getUserId() + "] not found on DS.");
				return false;
			}
			// ログインユーザで接続を試みた結果が認証結果
			LdapSearchResult sr = srs.get(0);
			try (LdapConnection authConn = external.newLdapConnection()) {
				return authConn.bind(url, sr.getName(), params.getPassword());
			}
		}
	}

	@Override
	public void terminate() {
		// NOP
	}

}
