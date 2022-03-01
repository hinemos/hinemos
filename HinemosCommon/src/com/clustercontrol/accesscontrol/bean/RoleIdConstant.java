/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
		
		if (ADMINISTRATORS.equals(roleId)){
			return true;
		}
		else if (HINEMOS_MODULE.equals(roleId)) {
			return true;
		}
		return false;
	}
	
}
