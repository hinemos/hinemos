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
 * カレンダ詳細一覧テーブル定義情報を取得するクライアント側アクションクラス<BR>
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
public class GetCalendarDetailTableDefine {

	/** 順序。 */
	public static final int ORDER_NO = 0;
	/** 規則 日程（月グループ + 曜日グループ） */
	public static final int RULE = 1;
	/** 規則 時間 （終日 または、開始時間と終了時間） */
	public static final int TIME = 2;
	/** 稼動・非稼動フラグ */
	public static final int OPERATE_FLG = 3;
	/** 説明。 */
	public static final int DESCRIPTION = 4;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX = ORDER_NO;
	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = 1;

	/**
	 * カレンダ詳細一覧のテーブル定義情報を取得します。
	 * 
	 * @return カレンダ詳細一覧テーブル定義情報
	 */
	public static ArrayList<TableColumnInfo> get() {
		// テーブル情報定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();
		tableDefine.add(ORDER_NO,
				new TableColumnInfo(Messages.getString("order", locale), TableColumnInfo.NONE, 40, SWT.CENTER));
		tableDefine.add(RULE,
				new TableColumnInfo(Messages.getString("calendar.detail.rule.date",locale), TableColumnInfo.NONE,180, SWT.CENTER));
		tableDefine.add(TIME,
				new TableColumnInfo(Messages.getString("calendar.detail.rule.time",locale), TableColumnInfo.NONE,135, SWT.CENTER));
		tableDefine.add(OPERATE_FLG,
				new TableColumnInfo(Messages.getString("calendar.detail.operation.3", locale), TableColumnInfo.NONE, 80, SWT.CENTER));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 110, SWT.LEFT));
		return tableDefine;
	}
}
