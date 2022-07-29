/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.auth.ldap;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.auth.helpers.ConfigHelper;

/**
 * LDAP認証の設定情報を保持します。
 */
public class LdapAuthenticatorConfig {
	private static final Log log = LogFactory.getLog(LdapAuthenticatorConfig.class);

	private static final String KEY_PREFIX = "access.authenticator.ldap.";

	public String url_1 = "";
	public String basedn_1 = "";
	public String searchuserdn_1 = "";
	public String password_1 = "";
	public String filter_1 = "";
	public int connect_timeout_1 = 0;
	public int read_timeout_1 = 60000;

	public String url_2 = "";
	public String basedn_2 = "";
	public String searchuserdn_2 = "";
	public String password_2 = "";
	public String filter_2 = "";
	public int connect_timeout_2 = 0;
	public int read_timeout_2 = 60000;
	
	private boolean primaryServerAvailable;
	private boolean secondaryServerAvailable;

	public LdapAuthenticatorConfig() {
		primaryServerAvailable = false;
		secondaryServerAvailable = false;
	}

	/**
	 * プロパティファイルから設定値を読み取ります。
	 * 読み取り後、設定値検証 {@link #verify()} を実施します。
	 * 
	 * @throws ConfigHelper.LoadFailedException 読み取り失敗。
	 */
	public void loadPropertiesFile(Path path) {
		ConfigHelper.loadProperties(this, path, LdapAuthenticatorConfig::mapKeyToField, "password_.*");
		verify();
	}
	
	/**
	 * プロパティキー名をフィールド名へマッピングします。
	 */
	protected static String mapKeyToField(String key) {
		// 共通のプレフィックスになっていない場合はスキップ
		if (!key.startsWith(KEY_PREFIX)) return null;
		// 共通のプレフィックスを取り除く
		// '.' を '_' へ変更する
		return key.replace(KEY_PREFIX, "").replace('.', '_');
	}

	/**
	 * 設定値を検証します。
	 * <p>
	 * publicフィールドの設定値を変更した場合は、
	 * このメソッドの呼び出すことで、{@link #isPrimaryServerAvailable()} 及び
	 * {@link #isSecondaryServerAvailable()} が正しい情報を返すようになります。 
	 */
	public void verify() {
		primaryServerAvailable = verifyPerServer("Primary LDAP server configuration",
				url_1, basedn_1, searchuserdn_1, password_1, filter_1, connect_timeout_1, read_timeout_1);

		secondaryServerAvailable = verifyPerServer("Secondary LDAP server configuration",
				url_2, basedn_2, searchuserdn_2, password_2, filter_2, connect_timeout_2, read_timeout_2);
	}
	
	protected static boolean verifyPerServer(String desc, String url, String basedn, String searchuserdn,
			String password, String filter, int connect_timeout, int read_timeout) {
		List<String> ngList = new ArrayList<>();

		if (isBlank(url)) {
			ngList.add("URL is blank");
		}
		if (isBlank(basedn)) {
			ngList.add("BaseDN is blank");
		}
		if (isBlank(filter)) {
			ngList.add("filter is blank");
		}

		if (ngList.size() == 0) {
			log.info("verify: " + desc + " is available.");
			return true;
		} else {
			log.info("verify: " + desc + " is unavailable. [" + String.join(", ", ngList) + "]");
			return false;
		}
	}

	protected static boolean isBlank(String s) {
		return (s == null)
				|| (s.length() == 0)
				|| (s.trim().length() == 0);
	}

	/**
	 * プライマリサーバ接続情報が有効である場合は true を返します。
	 */
	public boolean isPrimaryServerAvailable() {
		return primaryServerAvailable;
	}
	
	/**
	 * セカンダリサーバ接続情報が有効である場合は true を返します。
	 */
	public boolean isSecondaryServerAvailable() {
		return secondaryServerAvailable;
	}

}
