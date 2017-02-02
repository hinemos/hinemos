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

package com.clustercontrol.snmptrap.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * SNMPTRAP監視一覧テーブル定義情報を取得するクライアント側アクションクラス<BR>
 * 
 * @version 2.1.0
 * @since 2.1.0
 */
public class GetSnmpTrapListTableDefine {


	/** 監視項目ID */
	public static final int MONITOR_ID = 0;

	/** 説明 */
	public static final int DESCRIPTION = 1;

	/** SNMPTRAP数 */
	public static final int SNMPTRAP_NUMBER = 2;

	/** スコープ */
	public static final int FACILITY_ID = 3;

	//	/** カレンダ */
	//	public static final int CALENDAR_ID = 4;

	/** 有効／無効 */
	public static final int VALID_FLG = 4;

	/** 新規作成ユーザ */
	public static final int CREATE_USER = 5;

	/** 作成日時 */
	public static final int CREATE_TIME = 6;

	/** 最終変更ユーザ */
	public static final int UPDATE_USER = 7;

	/** 最終変更日時 */
	public static final int UPDATE_TIME = 8;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = MONITOR_ID;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;

	/**
	 * SNMPTRAP監視一覧のテーブル定義情報を取得します。<BR>
	 * 
	 * @return SNMPTRAP監視一覧テーブル定義情報
	 */
	public static ArrayList<TableColumnInfo> get() {
		/** テーブル情報定義配列 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MONITOR_ID,
				new TableColumnInfo(Messages.getString("monitor.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 200, SWT.LEFT));
		tableDefine.add(SNMPTRAP_NUMBER,
				new TableColumnInfo(Messages.getString("snmptrap.records", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(FACILITY_ID,
				new TableColumnInfo(Messages.getString("scope", locale), TableColumnInfo.FACILITY, 200, SWT.LEFT));
		tableDefine.add(VALID_FLG,
				new TableColumnInfo(Messages.getString("valid", locale) + "/" + Messages.getString("invalid", locale), TableColumnInfo.VALID, 80, SWT.LEFT));
		tableDefine.add(CREATE_USER,
				new TableColumnInfo(Messages.getString("creator.name", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(CREATE_TIME,
				new TableColumnInfo(Messages.getString("create.time", locale), TableColumnInfo.NONE, 130, SWT.LEFT));
		tableDefine.add(UPDATE_USER,
				new TableColumnInfo(Messages.getString("modifier.name", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(UPDATE_TIME,
				new TableColumnInfo(Messages.getString("update.time", locale), TableColumnInfo.NONE, 130, SWT.LEFT));

		return tableDefine;
	}
}
