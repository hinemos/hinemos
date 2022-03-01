/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.util;

/**
 * RPA管理ツール(setting.cc_rpa_management_tool_mst)毎のダイアログ表示方法を管理する列挙値
 * 
 */
public enum RpaManagementToolEnum {
	WIN_DIRECTOR_2_2("ACCOUNT_ID","PASSWORD", false),
	WIN_DIRECTOR_2_3("ACCOUNT_ID","PASSWORD", false),
	WIN_DIRECTOR_2_4("ACCOUNT_ID","PASSWORD", false),
	WINACTOR_MANAGER_ON_CLOUD_3_0("ACCOUNT_ID","PASSWORD", false),
	UIPATH_ORCHESTRATOR_ONPREMISE("ACCOUNT_ID","PASSWORD", true),
	UIPATH_ORCHESTRATOR_AUTOMATION_CLOUD("rpa.uipath.client.id","rpa.uipath.user.key", true)
	;
	
	/**
	 * 「アカウントID」項目のラベルに表示するプロパティ
	 */
	private String accountIdProperty;
	/**
	 * 「パスワード」項目のラベルに表示するプロパティ
	 */
	private String passwordProperty;
	/**
	 * 「テナント」項目を表示するフラグ
	 */
	private boolean tenantValid;
	
	RpaManagementToolEnum(String accountIdMessage, String passwordMessage, boolean tenantValid) {
		this.accountIdProperty = accountIdMessage;
		this.passwordProperty = passwordMessage;
		this.tenantValid = tenantValid;
	}

	/**
	 * 「アカウントID」項目のラベルに表示するプロパティ
	 */
	public String getAccountIdProperty() {
		return accountIdProperty;
	}

	/**
	 * 「パスワード」項目のラベルに表示するプロパティ
	 */
	public String getPasswordProperty() {
		return passwordProperty;
	}

	/**
	 * 「テナント」項目を表示するフラグ
	 */
	public boolean isTenantValid() {
		return tenantValid;
	}
}
