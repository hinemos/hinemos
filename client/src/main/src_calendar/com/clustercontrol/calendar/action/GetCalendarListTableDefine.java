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

package com.clustercontrol.calendar.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * カレンダ一覧テーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class GetCalendarListTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;

	/** UID */
	public static final int CALENDAR_ID = 1;

	/** 名前 */
	public static final int CALENDAR_NAME = 2;

	/** 有効期間(FROM) */
	public static final int VALID_TIME_FROM = 3;

	/** 有効期間(TO) */
	public static final int VALID_TIME_TO = 4;

	/** 説明 */
	public static final int DESCRIPTION = 5;

	/** オーナーロール */
	public static final int OWNER_ROLE = 6;

	/** 作成者 */
	public static final int CREATOR_NAME = 7;

	/** 作成日時 */
	public static final int CREATE_TIME = 8;

	/** 更新者 */
	public static final int MODIFIER_NAME = 9;

	/** 更新日時 */
	public static final int MODIFY_TIME = 10;

	/** ダミー**/
	public static final int DUMMY=11;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = CALENDAR_ID;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;

	// ----- instance メソッド ----- //

	/**
	 * カレンダ一覧テーブル定義を取得します。<BR>
	 *
	 * @return
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(CALENDAR_ID,
				new TableColumnInfo(Messages.getString("calendar.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(CALENDAR_NAME,
				new TableColumnInfo(Messages.getString("calendar.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(VALID_TIME_FROM,
				new TableColumnInfo(Messages.getString("valid.time", locale) + "(" + Messages.getString("start", locale) + ")",
						TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(VALID_TIME_TO,
				new TableColumnInfo(Messages.getString("valid.time", locale) + "(" + Messages.getString("end", locale) + ")",
						TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 130, SWT.LEFT));
		tableDefine.add(CREATOR_NAME,
				new TableColumnInfo(Messages.getString("creator.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(CREATE_TIME,
				new TableColumnInfo(Messages.getString("create.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(MODIFIER_NAME,
				new TableColumnInfo(Messages.getString("modifier.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(MODIFY_TIME,
				new TableColumnInfo(Messages.getString("update.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(DUMMY, new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
