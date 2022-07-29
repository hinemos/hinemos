/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

 package com.clustercontrol.rpa.model;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.CryptUtil;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;


/**
 * RPA管理ツールアカウント設定を格納するEntity定義
 *
 */
@Entity
@Table(name="cc_cfg_rpa_management_tool_account", schema="setting")
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.RPA_ACCOUNT,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="rpa_scope_id", insertable=false, updatable=false))
public class RpaManagementToolAccount extends ObjectPrivilegeTargetInfo {
	private static final long serialVersionUID = 1L;
	
	/** RPAスコープID */
	private String rpaScopeId;
	/** 接続情報 - アカウントID */
	private String accountId;
	/** 説明 */
	private String description;
	/** 接続情報 - 表示名 */
	private String displayName;
	/** パスワード */
	private String password;
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
	private Long regDate;
	/** 作成ユーザ */
	private String regUser;
	/** RPA管理ツール種別(ID) */
	private String rpaManagementToolId;
	/** RPAスコープ名 */
	private String rpaScopeName;
	/** 更新日時 */
	private Long updateDate;
	/** 更新ユーザ */
	private String updateUser;
	/** 接続情報 - URL */
	private String url;
	/** 接続情報 - テナント名 */
	private String tenantName;

	public RpaManagementToolAccount() {
	}


	/** RPAスコープID */
	@Id
	@Column(name="rpa_scope_id")
	public String getRpaScopeId() {
		return this.rpaScopeId;
	}

	public void setRpaScopeId(String rpaScopeId) {
		this.rpaScopeId = rpaScopeId;
	}


	/** 接続情報 - アカウントID */
	@Column(name="account_id")
	public String getAccountId() {
		return this.accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	/** 説明 */
	@Column(name="description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	/** 接続情報 - 表示名 */
	@Column(name="display_name")
	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/** パスワード */
	@Transient
	public String getPassword() {
		return CryptUtil.decrypt(getPasswordCrypt());
	}

	public void setPassword(String password) {
		setPasswordCrypt(CryptUtil.encrypt(password));
	}

	
	@Column(name="password")
	public String getPasswordCrypt() {
		return this.password;
	}

	public void setPasswordCrypt(String password) {
		this.password = password;
	}


	/** プロキシ(チェックボックス) */
	@Column(name="proxy_flg")
	public Boolean getProxyFlg() {
		return this.proxyFlg;
	}

	public void setProxyFlg(Boolean proxyFlg) {
		this.proxyFlg = proxyFlg;
	}

	/** プロキシ - パスワード */
	@Transient
	public String getProxyPassword() {
		return CryptUtil.decrypt(getProxyPasswordCrypt());
	}

	public void setProxyPassword(String proxyPassword) {
		setProxyPasswordCrypt(CryptUtil.encrypt(proxyPassword));
	}

	@Column(name="proxy_password")
	public String getProxyPasswordCrypt() {
		return this.proxyPassword;
	}

	public void setProxyPasswordCrypt(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}


	/** プロキシ - ポート */
	@Column(name="proxy_port")
	public Integer getProxyPort() {
		return this.proxyPort;
	}

	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}


	/** プロキシ - URL */
	@Column(name="proxy_url")
	public String getProxyUrl() {
		return this.proxyUrl;
	}

	public void setProxyUrl(String proxyUrl) {
		this.proxyUrl = proxyUrl;
	}


	/** プロキシ - ユーザ */
	@Column(name="proxy_user")
	public String getProxyUser() {
		return this.proxyUser;
	}

	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}


	/** 作成日時 */
	@Column(name="reg_date")
	public Long getRegDate() {
		return this.regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}


	/** 作成ユーザ */
	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}


	/** RPA管理ツール種別(ID) */
	@Column(name="rpa_management_tool_id")
	public String getRpaManagementToolId() {
		return rpaManagementToolId;
	}


	public void setRpaManagementToolId(String rpaManagementToolId) {
		this.rpaManagementToolId = rpaManagementToolId;
	}


	/** RPAスコープ名 */
	@Column(name="rpa_scope_name")
	public String getRpaScopeName() {
		return this.rpaScopeName;
	}

	public void setRpaScopeName(String rpaScopeName) {
		this.rpaScopeName = rpaScopeName;
	}


	/** 更新日時 */
	@Column(name="update_date")
	public Long getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}


	/** 更新ユーザ */
	@Column(name="update_user")
	public String getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}


	/** 接続情報 - URL */
	@Column(name="url")
	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}


	/** 接続情報 - テナント名 */
	@Column(name="tenant_name")
	public String getTenantName() {
		return tenantName;
	}


	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}
}