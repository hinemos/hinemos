/*

Copyright (C) 2007 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.maintenance.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * 共通設定テーブル定義情報を取得するクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class GetHinemosPropertyTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;

	/** キー。 */
	public static final int KEY = 1;

	/** 値。 */
	public static final int VALUE = 2;

	/** 値種別。 */
	public static final int VALUE_TYPE = 3;

	/** 説明。 */
	public static final int DESCRIPTION = 4;

	/** オーナーロール */
	public static final int OWNER_ROLE = 5;

	/** 新規作成ユーザ。 */
	public static final int CREATE_USER = 6;

	/** 作成日時。 */
	public static final int CREATE_TIME = 7;

	/** 最終変更ユーザ。 */
	public static final int UPDATE_USER = 8;

	/** 最終変更日時。 */
	public static final int UPDATE_TIME = 9;

	/** ダミー**/
	public static final int DUMMY=10;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX1 =  MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 =  KEY;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = 1;


	/**
	 * 共通設定テーブル定義情報を返します。
	 *
	 * @return 共通設定一覧テーブル定義情報
	 */
	public static ArrayList<TableColumnInfo> get() {
		/** テーブル情報定義配列 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));

		tableDefine.add(KEY,
				new TableColumnInfo(Messages.getString("hinemos.property.key", locale), TableColumnInfo.NONE, 300, SWT.LEFT));

		tableDefine.add(VALUE,
				new TableColumnInfo(Messages.getString("hinemos.property.value", locale), TableColumnInfo.NONE, 200, SWT.LEFT));

		tableDefine.add(VALUE_TYPE,
				new TableColumnInfo(Messages.getString("hinemos.property.value_type", locale), TableColumnInfo.NONE, 50, SWT.LEFT));

		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 450, SWT.LEFT));

		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 130, SWT.LEFT));

		tableDefine.add(CREATE_USER,
				new TableColumnInfo(Messages.getString("creator.name", locale), TableColumnInfo.NONE, 80, SWT.LEFT));

		tableDefine.add(CREATE_TIME,
				new TableColumnInfo(Messages.getString("create.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));

		tableDefine.add(UPDATE_USER,
				new TableColumnInfo(Messages.getString("modifier.name", locale), TableColumnInfo.NONE, 80, SWT.LEFT));

		tableDefine.add(UPDATE_TIME,
				new TableColumnInfo(Messages.getString("update.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));

		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
