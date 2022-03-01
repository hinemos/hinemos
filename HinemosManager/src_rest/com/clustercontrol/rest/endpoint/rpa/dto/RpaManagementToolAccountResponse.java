/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

 package com.clustercontrol.rest.endpoint.rpa.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;

public class RpaManagementToolAccountResponse {
	/** RPAスコープID */
	private String rpaScopeId;
	/** RPAスコープ名 */
	private String rpaScopeName;
	/** オーナーロールID */
	private String ownerRoleId;
	/** 説明 */
	private String description;
	/** 接続情報 - URL */
	private String url;
	/** 接続情報 - アカウントID */
	private String accountId;
	/** 接続情報 - 表示名 */
	private String displayName;
	/** 接続情報 - パスワード */
	private String password;
	/** 接続情報 - テナント名 */
	private String tenantName;
	/** プロキシ(チェックボックス) */
	private Boolean proxyFlg;
	/** プロキシ - パスワード */
	private String proxyPassword;
	/** プロキシ - ポート */
	private Integer proxyPort;
	/** プロキシ - URL */
	private String proxyUrl;
	/** プロキシ - ユーザ */
	private String proxyUser;
	/** 作成日時 */
	@RestBeanConvertDatetime
	private String regDate;
	/** 作成ユーザ */
	private String regUser;
	/** RPA管理ツール種別(ID) */
	private String rpaManagementToolId;
	/** 更新日時 */
	@RestBeanConvertDatetime
	private String updateDate;
	/** 更新ユーザ */
	private String updateUser;

	public RpaManagementToolAccountResponse() {
	}


	/** RPAスコープID */
	public String getRpaScopeId() {
		return this.rpaScopeId;
	}

	public void setRpaScopeId(String rpaScopeId) {
		this.rpaScopeId = rpaScopeId;
	}


	/** 接続情報 - アカウントID */
	public String getAccountId() {
		return this.accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	/** 説明 */
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	/** 接続情報 - 表示名 */
	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}


	/** オーナーロールID */
	public String getOwnerRoleId() {
		return this.ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}


	/** パスワード */
	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	/** プロキシ(チェックボックス) */
	public Boolean getProxyFlg() {
		return this.proxyFlg;
	}

	public void setProxyFlg(Boolean proxyFlg) {
		this.proxyFlg = proxyFlg;
	}

	/** プロキシ - パスワード */
	public String getProxyPassword() {
		return this.proxyPassword;
	}

	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}


	/** プロキシ - ポート */
	public Integer getProxyPort() {
		return this.proxyPort;
	}

	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}


	/** プロキシ - URL */
	public String getProxyUrl() {
		return this.proxyUrl;
	}

	public void setProxyUrl(String proxyUrl) {
		this.proxyUrl = proxyUrl;
	}


	/** プロキシ - ユーザ */
	public String getProxyUser() {
		return this.proxyUser;
	}

	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}


	/** 作成日時 */
	public String getRegDate() {
		return this.regDate;
	}

	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}


	/** 作成ユーザ */
	public String getRegUser() {
		return this.regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}


	/** RPA管理ツール種別(ID) */
	public String getRpaManagementToolId() {
		return rpaManagementToolId;
	}


	public void setRpaManagementToolId(String rpaManagementToolId) {
		this.rpaManagementToolId = rpaManagementToolId;
	}


	/** RPAスコープ名 */
	public String getRpaScopeName() {
		return this.rpaScopeName;
	}

	public void setRpaScopeName(String rpaScopeName) {
		this.rpaScopeName = rpaScopeName;
	}


	/** 更新日時 */
	public String getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}


	/** 更新ユーザ */
	public String getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}


	/** 接続情報 - URL */
	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}


	/**
	 * 接続情報 - テナント名
	 */
	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}
}
