/*

Copyright (C) 2013 NTT DATA Corporation

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
 * カレンダ[カレンダパターン]テーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class GetCalendarPatternTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;

	/** ID */
	public static final int CAL_PATTERN_ID = 1;

	/** 名前 */
	public static final int CAL_PATTERN_NAME = 2;

	/** 日数 */
	public static final int NUMBER_OF_DAYS = 3;

	/** 登録日 */
	public static final int RECORD_DATE = 4;

	/** オーナーロール */
	public static final int OWNER_ROLE = 5;

	/** 作成者 */
	public static final int CREATOR_NAME = 6;

	/** 作成日時 */
	public static final int CREATE_TIME = 7;

	/** 更新者 */
	public static final int MODIFIER_NAME = 8;

	/** 更新日時 */
	public static final int MODIFY_TIME = 9;

	/** ダミー**/
	public static final int DUMMY=10;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = CAL_PATTERN_ID;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;

	/**
	 * カレンダ[カレンダパターン]一覧テーブル定義を取得します。<BR>
	 *
	 * @return
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(CAL_PATTERN_ID,
				new TableColumnInfo(Messages.getString("calendar.pattern.id", locale), TableColumnInfo.NONE, 130, SWT.LEFT));
		tableDefine.add(CAL_PATTERN_NAME,
				new TableColumnInfo(Messages.getString("calendar.pattern.name", locale), TableColumnInfo.NONE, 130, SWT.LEFT));
		tableDefine.add(NUMBER_OF_DAYS,
				new TableColumnInfo(Messages.getString("calendar.pattern.days", locale), TableColumnInfo.NONE, 70, SWT.LEFT));
		tableDefine.add(RECORD_DATE,
				new TableColumnInfo(Messages.getString("calendar.pattern.record.date", locale), TableColumnInfo.NONE, 250, SWT.LEFT));
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
