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
 * アカウント[システム権限]ビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * アカウントのビュー[システム権限]のカラムの情報を取得します。
 * Hinemosではロケールによって動作を変えるために定義情報も
 * マネージャから取得します。
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class GetSystemPrivilegeListTableDefine {

	/** システム権限 */
	public static final int SYSTEM_PRIVILEGE = 0;

	/** ダミー**/
	public static final int DUMMY=1;

	//	/** 新規作成ユーザ */
	//	public static final int CREATOR_NAME = 1;
	//
	//	/** 作成日時 */
	//	public static final int CREATE_TIME = 2;
	//
	//	/** 最終更新ユーザ */
	//	public static final int MODIFIER_NAME = 3;
	//
	//	/** 最終更新日時 */
	//	public static final int MODIFY_TIME = 4;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = SYSTEM_PRIVILEGE;

	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;

	/**
	 * マネージャにSessionBean経由でアクセスし、<BR>
	 * アクセス[ユーザ]ビューのテーブル定義を取得します。
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 *
	 * @see com.clustercontrol.accesscontrol.ejb.session.AccessController#getUserListTableDefine(java.util.Locale)
	 */
	public static ArrayList<TableColumnInfo> get() {
		Locale locale = Locale.getDefault();
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();

		/** メイン処理 */
		tableDefine.add(SYSTEM_PRIVILEGE, new TableColumnInfo( Messages.getString("system_privilege", locale), TableColumnInfo.NONE, 300, SWT.LEFT) );
		tableDefine.add(DUMMY, new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));
		//		tableDefine.add(CREATOR_NAME, new TableColumnInfo( Messages.getString("creator.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT) );
		//		tableDefine.add(CREATE_TIME, new TableColumnInfo( Messages.getString("create.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT) );
		//		tableDefine.add(MODIFIER_NAME, new TableColumnInfo( Messages.getString("modifier.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT) );
		//		tableDefine.add(MODIFY_TIME, new TableColumnInfo( Messages.getString("update.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT) );

		return tableDefine;
	}
}
