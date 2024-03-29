/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

 package com.clustercontrol.rest.endpoint.rpa.dto;

import org.apache.commons.lang3.StringUtils;

import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.RpaManagementRestConnectFailed;
import com.clustercontrol.fault.RpaManagementToolAccountNotFound;
import com.clustercontrol.fault.RpaManagementToolMasterNotFound;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rpa.model.RpaManagementToolAccount;
import com.clustercontrol.rpa.util.QueryUtil;
import com.clustercontrol.rpa.util.RpaUtil;
import com.clustercontrol.util.MessageConstant;

public class ModifyRpaManagementToolAccountRequest implements RequestDto {
	/** RPAスコープ名 */
	@RestItemName(MessageConstant.RPA_SCOPE_NAME)
	@RestValidateString(notNull=true, minLen=1)
	private String rpaScopeName;
	/** 説明 */
	@RestItemName(MessageConstant.DESCRIPTION)
	@RestValidateString(maxLen=256)
	private String description;
	/** 接続情報 - URL */
	@RestItemName(MessageConstant.URL)
	@RestValidateString(notNull=true, minLen=1, maxLen=256)
	private String url;
	/** 接続情報 - アカウントID */
	@RestItemName(MessageConstant.ACCOUNT_ID)
	@RestValidateString(notNull=true, minLen=1, maxLen=64)
	private String accountId;
	/** 接続情報 - 表示名 */
	@RestItemName(MessageConstant.DISPLAY_NAME)
	@RestValidateString(notNull=true, minLen=1)
	private String displayName;
	/** 接続情報 - パスワード */
	@RestItemName(MessageConstant.PASSWORD)
	@RestValidateString(notNull=true, minLen=1)
	private String password;
	/** 接続情報 - テナント名 */
	@RestItemName(MessageConstant.TENANT_NAME)
	@RestValidateString(minLen=1, maxLen=64)
	private String tenantName;
	/** プロキシ(チェックボックス) */
	@RestItemName(MessageConstant.PROXY)
	@RestValidateObject(notNull=true)
	private Boolean proxyFlg;
	/** プロキシ - パスワード */
	@RestItemName(MessageConstant.PROXYPASSWORD)
	@RestValidateString(minLen=1)
	private String proxyPassword;
	/** プロキシ - ポート */
	@RestItemName(MessageConstant.PROXYPORT)
	@RestValidateInteger(minVal=0, maxVal=65535)
	private Integer proxyPort;
	/** プロキシ - URL */
	@RestItemName(MessageConstant.PROXYURL)
	@RestValidateString(minLen=1)
	private String proxyUrl;
	/** プロキシ - ユーザ */
	@RestItemName(MessageConstant.PROXYUSER)
	@RestValidateString(minLen=1)
	private String proxyUser;

	public ModifyRpaManagementToolAccountRequest() {
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

	/** RPAスコープ名 */
	public String getRpaScopeName() {
		return this.rpaScopeName;
	}

	public void setRpaScopeName(String rpaScopeName) {
		this.rpaScopeName = rpaScopeName;
	}

	/** 接続情報 - URL */
	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public void correlationCheck(String rpaScopeId) throws InvalidSetting {
		// URLフォーマットチェック
		CommonValidator.validateUrl(MessageConstant.URL.getMessage(), url);
		
		if (proxyFlg) {
			// プロキシURLチェック
			CommonValidator.validateUrl(MessageConstant.PROXYURL.getMessage(), proxyUrl);
			
			// プロキシポートチェック
			if (proxyPort == null) {
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(MessageConstant.PROXYPORT.getMessage()));
			}
		}
		
		// URL整形
		setUrl(stripURL(getUrl()));
		setProxyUrl(stripURL(getProxyUrl()));

		// アカウントの重複チェック
		checkAccountDuplication(rpaScopeId, getAccountId(), getUrl(), getTenantName());

		// アカウント認証チェック
		try {
			checkAuthentication(rpaScopeId);
		} catch (RpaManagementToolAccountNotFound | InvalidRole | HinemosUnknown e) {
			throw new InvalidSetting(e);
		}
	}
	
	/**
	 * URLから末尾の「/」、空白を削除する。
	 * @param URL
	 * @return 整形済URL
	 */
	private String stripURL(String URL) {
		return StringUtils.strip(URL, "/ ");
	}
	
	/**
	 * RPA管理ツールアカウントの重複チェック
	 */
	private void checkAccountDuplication(String rpaScopeId, String accountId, String url, String tenantName) throws InvalidSetting {
		try {
			// 同一アカウントID、URL(、テナント名)のRPA管理ツールアカウントがある場合、バリデーションエラー
			if (tenantName == null) {
				RpaManagementToolAccount sameAccount = QueryUtil.getRpaAccountByAccountIdAndUrl_NONE(accountId, url);
				if (!sameAccount.getRpaScopeId().equals(rpaScopeId)) {
					throw new InvalidSetting(MessageConstant.MESSAGE_RPA_MANAGEMENT_ACCOUNT_DUPULICATED.getMessage(sameAccount.getRpaScopeName(), sameAccount.getRpaScopeId(), accountId, url));
				}
			} else {
				RpaManagementToolAccount sameAccount = QueryUtil.getRpaAccountByAccountIdAndUrlAndTenantName_NONE(accountId, url, tenantName);
				if (!sameAccount.getRpaScopeId().equals(rpaScopeId)) {
					throw new InvalidSetting(MessageConstant.MESSAGE_RPA_MANAGEMENT_ACCOUNT_DUPULICATED_WITH_TENANT.getMessage(sameAccount.getRpaScopeName(), sameAccount.getRpaScopeId(), accountId, url, tenantName));
				}
			}
		} catch (RpaManagementToolAccountNotFound e) {
			return;
		}
	}
	
	/**
	 * RPAアカウント認証チェック(Modify用)
	 * @param request
	 * @param rpaScopeId
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws RpaManagementToolAccountNotFound
	 * @throws InvalidRole 
	 */
	private void checkAuthentication(String rpaScopeId) throws HinemosUnknown, InvalidSetting, RpaManagementToolAccountNotFound, InvalidRole {
		try {
			RpaManagementToolAccount info = QueryUtil.getRpaAccountPK(rpaScopeId);
			
			if (getProxyFlg()) {
				RpaUtil.checkAuthentification(rpaScopeId,
						info.getRpaManagementToolId(),
						getUrl(),
						getAccountId(),
						getPassword(),
						getTenantName(),
						getProxyUrl(),
						getProxyPort(),
						getProxyUser(),
						getProxyPassword()
						);
			} else {
				RpaUtil.checkAuthentification(rpaScopeId,
						info.getRpaManagementToolId(),
						getUrl(),
						getAccountId(),
						getPassword(),
						getTenantName(),
						null,
						null,
						null,
						null
						);
			
			}
		} catch (RpaManagementToolMasterNotFound e) {
			// RPA管理ツールIDは変更不可なので入らない想定
			throw new HinemosUnknown(e);
		} catch (RpaManagementRestConnectFailed e) {
			throw new InvalidSetting(MessageConstant.MESSAGE_RPA_MANAGEMENT_ACCOUNT_AUTHENTICATION_FAILED.getMessage(getAccountId(), getUrl()), e);
		}
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
	
	@Override
	public String toString() {
		return "ModifyRpaManagementToolAccountRequest [rpaScopeName=" + rpaScopeName + ", description=" + description + ", url=" + url + ", accountId=" + accountId
				+ ", displayName=" + displayName + ", password=" + password + ", tenantName=" + tenantName + ", proxyFlg=" + proxyFlg + ", proxyPassword=" + proxyPassword
				+ ", proxyPort=" + proxyPort + ", proxyUrl=" + proxyUrl + ", proxyUser=" + proxyUser + "]";
	}
}
