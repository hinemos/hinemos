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

package com.clustercontrol.accesscontrol.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * アカウント[ロール]ビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * アカウントのビュー[ロール]のカラムの情報を取得します。
 * Hinemosではロケールによって動作を変えるために定義情報も
 * マネージャから取得します。
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class GetRoleListTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;

	/** ロールID */
	public static final int ROLE_ID = 1;

	/** 名前 */
	public static final int NAME = 2;

	/** 説明 */
	public static final int DESCRIPTION = 3;

	/** 新規作成ユーザ */
	public static final int CREATOR_NAME = 4;

	/** 作成日時 */
	public static final int CREATE_TIME = 5;

	/** 最終更新ユーザ */
	public static final int MODIFIER_NAME = 6;

	/** 最終更新日時 */
	public static final int MODIFY_TIME = 7;

	/** ダミー**/
	public static final int DUMMY=8;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = ROLE_ID;

	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;

	/**
	 * マネージャにSessionBean経由でアクセスし、<BR>
	 * アクセス権限[ロール]ビューのテーブル定義を取得します。
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 *
	 * @see com.clustercontrol.accesscontrol.ejb.session.AccessController#getRoleListTableDefine(java.util.Locale)
	 */
	public static ArrayList<TableColumnInfo> get() {
		Locale locale = Locale.getDefault();
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();

		/** メイン処理 */
		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(ROLE_ID, new TableColumnInfo( Messages.getString("role.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT) );
		tableDefine.add(NAME, new TableColumnInfo( Messages.getString("role.name", locale), TableColumnInfo.NONE, 150, SWT.LEFT) );
		tableDefine.add(DESCRIPTION, new TableColumnInfo( Messages.getString("description", locale), TableColumnInfo.NONE, 200, SWT.LEFT) );
		tableDefine.add(CREATOR_NAME, new TableColumnInfo( Messages.getString("creator.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT) );
		tableDefine.add(CREATE_TIME, new TableColumnInfo( Messages.getString("create.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT) );
		tableDefine.add(MODIFIER_NAME, new TableColumnInfo( Messages.getString("modifier.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT) );
		tableDefine.add(MODIFY_TIME, new TableColumnInfo( Messages.getString("update.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT) );
		tableDefine.add(DUMMY, new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
