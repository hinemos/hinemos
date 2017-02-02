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
 * エージェント一覧テーブルの定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class GetAgentListTableDefine {

	/** Manager Name */
	public static final int MANAGER_NAME = 0;

	/** ファシリティID */
	public static final int FACILITY_ID = 1;

	/** ファシリティ名 */
	public static final int FACILITY_NAME = 2;

	/** エージェント起動時刻 */
	public static final int STARTUP_TIME = 3;

	/** エージェント最終接続時刻 */
	public static final int LAST_LOGIN = 4;

	/** 多重度 */
	public static final int MULTIPLICITY = 5;

	/** new/old */
	public static final int NEW_OLD = 6;

	/** ダミー**/
	public static final int DUMMY = 7;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = FACILITY_ID;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;


	// ----- instance メソッド ----- //

	/**
	 * 全てのノード一覧を取得します。
	 *
	 * @return ノード一覧
	 */
	public static ArrayList<TableColumnInfo> get() {
		/** 出力用変数 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		/** メイン処理 */
		// Manager Name
		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", "Manager", locale), TableColumnInfo.MANAGER_NAME, 100, SWT.LEFT));
		tableDefine.add(FACILITY_ID,
				new TableColumnInfo(Messages.getString("facility.id", locale), TableColumnInfo.FACILITY_ID, 100, SWT.LEFT));
		tableDefine.add(FACILITY_NAME,
				new TableColumnInfo(Messages.getString("facility.name", locale), TableColumnInfo.FACILITY_NAME, 140, SWT.LEFT));
		tableDefine.add(STARTUP_TIME,
				new TableColumnInfo(Messages.getString("startup.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(LAST_LOGIN,
				new TableColumnInfo(Messages.getString("last.login.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(MULTIPLICITY,
				new TableColumnInfo(Messages.getString("job.multiplicity", locale), TableColumnInfo.NONE, 110, SWT.LEFT));
		tableDefine.add(NEW_OLD,
				new TableColumnInfo(Messages.getString("update.e", locale), TableColumnInfo.NONE, 90, SWT.LEFT));
		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
