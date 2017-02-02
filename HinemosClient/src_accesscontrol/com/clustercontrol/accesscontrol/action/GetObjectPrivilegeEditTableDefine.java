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
 * オブジェクト権限編集ダイアログのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class GetObjectPrivilegeEditTableDefine {


	/** オブジェクト権限 */
	public static final int OBJECT_PRIVILEGE_NAME = 0;

	/** 許可チェックボックス */
	public static final int ALLOW_CHECKBOX = 1;

	/** カラム種別選定用 */
	public static final int PRIVILEGE = 2;

	/** ソート用データ */
	public static final int SORT_VALUE = 3;

	/** ダミー**/
	public static final int DUMMY=2;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = SORT_VALUE;

	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;
	/**
	 * マネージャにSessionBean経由でアクセスし、<BR>
	 * オブジェクト権限定義を取得します。
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 *
	 * @see com.clustercontrol.accesscontrol.ejb.session.AccessController#getUserListTableDefine(java.util.Locale)
	 */
	public static ArrayList<TableColumnInfo> get() {
		Locale locale = Locale.getDefault();
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();

		/** メイン処理 */
		tableDefine.add(OBJECT_PRIVILEGE_NAME, new TableColumnInfo( Messages.getString("privilege", locale), TableColumnInfo.NONE, 150, SWT.LEFT) );
		tableDefine.add(ALLOW_CHECKBOX, new TableColumnInfo( Messages.getString("allow", locale), TableColumnInfo.CHECKBOX, 50, SWT.LEFT) );
		tableDefine.add(DUMMY, new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
