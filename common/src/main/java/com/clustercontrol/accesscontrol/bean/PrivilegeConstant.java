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
	public enum SystemPrivilegeMode {ADD, READ, MODIFY, EXEC, APPROVAL}
	// オブジェクト権限（NONE=オブジェクト権限チェックしない)
	public enum ObjectPrivilegeMode {READ, MODIFY, EXEC, NONE}
	
	// オブジェクト権限で登録されるもの
	protected static final ObjectPrivilegeMode[] objectPrivilegeModes = {
		ObjectPrivilegeMode.READ,
		ObjectPrivilegeMode.MODIFY,
		ObjectPrivilegeMode.EXEC,
	};
	public static final ObjectPrivilegeMode[] getObjectPrivilegeMode() {
		return objectPrivilegeModes.clone();
	}

	// システム権限設定種別
	public static final String SYSTEMPRIVILEGE_EDITTYPE_NONE = "0";
	public static final String SYSTEMPRIVILEGE_EDITTYPE_DIALOG = "1";
	
	private PrivilegeConstant() {
		throw new IllegalStateException("ConstClass");
	}
}