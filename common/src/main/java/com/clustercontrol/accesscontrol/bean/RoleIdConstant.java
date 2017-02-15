/*

Copyright (C) since 2006 NTT DATA Corporation

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
 * 既存のロールIDの格納文字列を定義するクラス<BR>
 */
public class RoleIdConstant {

	/** ALL_USERS */
	public static final String ALL_USERS = "ALL_USERS";

	/** INTERNAL */
	public static final String INTERNAL = "INTERNAL";

	/** ADMINISTRATORS */
	public static final String ADMINISTRATORS = "ADMINISTRATORS";

	/** HINEMOS_MODULE */
	public static final String HINEMOS_MODULE = "HINEMOS_MODULE";

	/** 特権ロールか否かを返す */
	public static final boolean isAdministratorRole(String roleId) {
		
		if (roleId.equals(ADMINISTRATORS)){
			return true;
		}
		else if (roleId.equals(HINEMOS_MODULE)) {
			return true;
		}
		return false;
	}
	
}
