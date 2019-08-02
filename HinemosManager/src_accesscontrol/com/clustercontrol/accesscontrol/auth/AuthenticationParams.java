/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.auth;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;

/**
 * ユーザ認証に必要な情報のコンテナです。
 */
public class AuthenticationParams {
	private String userId;
	private String password;
	private List<SystemPrivilegeInfo> requiredSystemPrivileges;
	private boolean cacheDisabled;

	/** ユーザID */
	public String getUserId() {
		if (userId == null) return "";
		return userId;
	}

	/** ユーザID */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/** パスワード */
	public String getPassword() {
		if (password == null) return "";
		return password;
	}

	/** パスワード */
	public void setPassword(String password) {
		this.password = password;
	}

	/** ユーザが持っていなければならないシステム権限のリスト */
	public List<SystemPrivilegeInfo> getRequiredSystemPrivileges() {
		if (requiredSystemPrivileges == null) return new ArrayList<>();
		return requiredSystemPrivileges;
	}

	/** ユーザが持っていなければならないシステム権限のリスト */
	public void setRequiredSystemPrivileges(List<SystemPrivilegeInfo> requiredSystemPrivileges) {
		this.requiredSystemPrivileges = requiredSystemPrivileges;
	}

	/** true: 認証結果キャッシュ無効 */
	public boolean isCacheDisabled() {
		return cacheDisabled;
	}

	/** true: 認証結果キャッシュ無効 */
	public void setCacheDisabled(boolean cacheDisabled) {
		this.cacheDisabled = cacheDisabled;
	}
}
