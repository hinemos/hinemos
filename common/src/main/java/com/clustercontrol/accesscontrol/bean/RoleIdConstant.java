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
		
		if (roleId.equals(ADMINISTRATORS)){
			return true;
		}
		else if (roleId.equals(HINEMOS_MODULE)) {
			return true;
		}
		return false;
	}
	
	private RoleIdConstant() {
		throw new IllegalStateException("ConstClass");
	}
}
