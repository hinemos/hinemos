/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.bean;


/**
 * オブジェクト権限、システム権限の定数を格納するクラス<BR>
 * 
 */
public class PrivilegeConstant {

	// システム権限
	public static enum SystemPrivilegeMode {ADD, READ, MODIFY, EXEC, APPROVAL}
	// オブジェクト権限（NONE=オブジェクト権限チェックしない)
	public static enum ObjectPrivilegeMode {READ, MODIFY, EXEC, NONE}
	
	// オブジェクト権限で登録されるもの
	public static final ObjectPrivilegeMode[] objectPrivilegeModes = {
		ObjectPrivilegeMode.READ,
		ObjectPrivilegeMode.MODIFY,
		ObjectPrivilegeMode.EXEC,
	};

	// システム権限設定種別
	public static final String SYSTEMPRIVILEGE_EDITTYPE_NONE = "0";
	public static final String SYSTEMPRIVILEGE_EDITTYPE_DIALOG = "1";
}