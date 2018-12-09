/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.bean;

/**
 * 
 * UserPropertyの定数部分を切り出した物。
 *
 */
public class UserConstant {
	/** ユーザID */
	public static final String UID = "uid";
	/** 名前 */
	public static final String NAME = "namae";
	/** 説明 */
	public static final String DESCRIPTION = "description";
	/** 作成日時 */
	public static final String CREATE_TIME = "createTimestamp";
	/** 新規作成ユーザ */
	public static final String CREATOR_NAME = "creatorName";
	/** 最終更新ユーザ */
	public static final String MODIFIER_NAME = "ModifierName";
	/** 最終更新日時 */
	public static final String MODIFY_TIME = "ModifyTime";

	/** アクセス権限 */
	public static final String ACCESS = "access";
}
