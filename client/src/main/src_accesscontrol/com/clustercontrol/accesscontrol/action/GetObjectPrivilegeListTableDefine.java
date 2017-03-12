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
 * オブジェクト権限一覧ダイアログのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class GetObjectPrivilegeListTableDefine {


	/** ロール名 */
	public static final int ROLE_NAME = 0;

	/** 参照チェックボックス */
	public static final int READ_CHECKBOX = 1;

	/** 更新チェックボックス */
	public static final int WRITE_CHECKBOX = 2;

	/** 実行チェックボックス */
	public static final int EXEC_CHECKBOX = 3;

	/** ダミー**/
	public static final int DUMMY=4;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = ROLE_NAME;

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
		tableDefine.add(ROLE_NAME, new TableColumnInfo( Messages.getString("role.name", locale), TableColumnInfo.NONE, 150, SWT.LEFT) );
		tableDefine.add(READ_CHECKBOX, new TableColumnInfo( Messages.getString("refer", locale), TableColumnInfo.CHECKBOX, 50, SWT.LEFT) );
		tableDefine.add(WRITE_CHECKBOX, new TableColumnInfo( Messages.getString("modify", locale), TableColumnInfo.CHECKBOX, 50, SWT.LEFT) );
		tableDefine.add(EXEC_CHECKBOX, new TableColumnInfo( Messages.getString("run", locale), TableColumnInfo.CHECKBOX, 50, SWT.LEFT) );
		tableDefine.add(DUMMY, new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
