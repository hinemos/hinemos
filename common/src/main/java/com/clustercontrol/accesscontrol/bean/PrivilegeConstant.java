/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
	public static ObjectPrivilegeMode[] objectPrivilegeModes = {
		ObjectPrivilegeMode.READ,
		ObjectPrivilegeMode.MODIFY,
		ObjectPrivilegeMode.EXEC,
	};

	// システム権限設定種別
	public static final String SYSTEMPRIVILEGE_EDITTYPE_NONE = "0";
	public static final String SYSTEMPRIVILEGE_EDITTYPE_DIALOG = "1";
}