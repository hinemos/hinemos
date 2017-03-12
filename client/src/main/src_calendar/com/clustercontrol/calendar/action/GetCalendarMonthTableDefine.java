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
 * 月間カレンダテーブル定義情報を取得するクライアント側アクションクラス<BR>
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
public class GetCalendarMonthTableDefine {

	public static final int DUMMY = 0;

	/** sunday */
	public static final int SUNDAY = 1;

	/** monday */
	public static final int MONDAY = 2;

	/** tuesday */
	public static final int TUESDAY = 3;

	/** wednesday */
	public static final int WEDNESDAY = 4;

	/** thursday */
	public static final int THURSDAY = 5;

	/** friday */
	public static final int FRIDAY = 6;

	/** saturday */
	public static final int SATURDAY = 7;

	/**
	 * 月間カレンダのテーブル定義情報を取得します。
	 * 
	 * @return 月間カレンダテーブル定義情報
	 */
	public static ArrayList<TableColumnInfo> get() {
		// テーブル情報定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.NONE, 10, SWT.LEFT));
		tableDefine.add(SUNDAY,
				new TableColumnInfo(Messages.getString("week.sun", locale), TableColumnInfo.NONE, 60, SWT.LEFT));
		tableDefine.add(MONDAY,
				new TableColumnInfo(Messages.getString("week.mon", locale), TableColumnInfo.NONE, 60, SWT.LEFT));
		tableDefine.add(TUESDAY,
				new TableColumnInfo(Messages.getString("week.tue", locale), TableColumnInfo.NONE, 60, SWT.LEFT));
		tableDefine.add(WEDNESDAY,
				new TableColumnInfo(Messages.getString("week.wed", locale), TableColumnInfo.NONE, 60, SWT.LEFT));
		tableDefine.add(THURSDAY,
				new TableColumnInfo(Messages.getString("week.thu", locale), TableColumnInfo.NONE, 60, SWT.LEFT));
		tableDefine.add(FRIDAY,
				new TableColumnInfo(Messages.getString("week.fri", locale), TableColumnInfo.NONE, 60, SWT.LEFT));
		tableDefine.add(SATURDAY,
				new TableColumnInfo(Messages.getString("week.sat", locale), TableColumnInfo.NONE, 60, SWT.LEFT));

		return tableDefine;
	}
}
