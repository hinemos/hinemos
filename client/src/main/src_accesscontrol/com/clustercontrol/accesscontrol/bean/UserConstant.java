/*

Copyright (C) since 2010 NTT DATA Corporation

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
