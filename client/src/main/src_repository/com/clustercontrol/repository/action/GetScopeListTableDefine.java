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

package com.clustercontrol.repository.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 *登録スコープ一覧のテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class GetScopeListTableDefine {

	/** Manager Name */
	public static final int MANAGER_NAME = 0;

	/** ファシリティID */
	public static final int FACILITY_ID = 1;

	/** ファシリティ名 */
	public static final int FACILITY_NAME = 2;

	/** 注釈 */
	public static final int DESCRIPTION = 3;

	/** オーナーロール */
	public static final int OWNER_ROLE = 4;

	/** ダミー**/
	public static final int DUMMY = 5;

	/** ソート用データ**/
	public static final int SORT_VALUE=6;

	/** 初期表示時ソートカラム */
	//public static final int SORT_COLUMN_INDEX = FACILITY_ID;

	public static final int SORT_COLUMN_INDEX1 =MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 =SORT_VALUE;
	public static final int SORT_COLUMN_INDEX3= FACILITY_ID;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;

	//ソートの順番の項目を追加したのでその値でソートする必要あり。
	//public static final int SORT_ORDER = 4;


	// ----- instance メソッド ----- //

	/**
	 *スコープ一覧テーブル定義を取得します。<BR>
	 *
	 * @return ノード一覧
	 */
	public static ArrayList<TableColumnInfo> get() {
		/** 出力用変数 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		/** メイン処理 */
		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", "Manager", locale), TableColumnInfo.MANAGER_NAME, 100, SWT.LEFT));
		tableDefine.add(FACILITY_ID,
				new TableColumnInfo(Messages.getString("facility.id", locale), TableColumnInfo.FACILITY_ID, 100, SWT.LEFT));
		tableDefine.add(FACILITY_NAME,
				new TableColumnInfo(Messages.getString("facility.name", locale), TableColumnInfo.FACILITY_NAME, 150, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 200, SWT.LEFT));
		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 130, SWT.LEFT));
		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;

	}
}
