/*

Copyright (C) 2007 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.maintenance.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * メンテナンス一覧テーブル定義情報を取得するクライアント側アクションクラス<BR>
 *
 * @version 4.0.0
 * @since 2.2.0
 */
public class GetMaintenanceListTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;

	/** メンテナンスID。 */
	public static final int MAINTENANCE_ID = 1;

	/** 説明。 */
	public static final int DESCRIPTION = 2;

	/**
	 * 種別。
	 *
	 * @see com.clustercontrol.monitor.run.bean.MonitorTypeConstant
	 */
	public static final int TYPE_ID = 3;

	/** 保存期間(日)。 */
	public static final int RETENTION_PERIOD = 4;

	/** カレンダID。 */
	public static final int CALENDAR_ID = 5;

	/** スケジュール。*/
	public static final int SCHEDULE = 6;

	/** 有効／無効。 */
	public static final int VALID_FLG = 7;

	/** オーナーロール */
	public static final int OWNER_ROLE = 8;

	/** 新規作成ユーザ。 */
	public static final int CREATE_USER = 9;

	/** 作成日時。 */
	public static final int CREATE_TIME = 10;

	/** 最終変更ユーザ。 */
	public static final int UPDATE_USER = 11;

	/** 最終変更日時。 */
	public static final int UPDATE_TIME = 12;

	/** ダミー**/
	public static final int DUMMY=13;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = MAINTENANCE_ID;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = 1;


	/**
	 * メンテナンス一覧のテーブル定義情報を返します。
	 *
	 * @return メンテナンス一覧テーブル定義情報
	 */
	public static ArrayList<TableColumnInfo> get() {
		/** テーブル情報定義配列 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));

		tableDefine.add(MAINTENANCE_ID,
				new TableColumnInfo(Messages.getString("maintenance.id", locale), TableColumnInfo.NONE, 120, SWT.LEFT));

		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 150, SWT.LEFT));

		tableDefine.add(TYPE_ID,
				new TableColumnInfo(Messages.getString("maintenance.type", locale), TableColumnInfo.NONE, 170, SWT.LEFT));

		tableDefine.add(RETENTION_PERIOD,
				new TableColumnInfo(Messages.getString("maintenance.retention.period", locale), TableColumnInfo.NONE, 100, SWT.LEFT));

		tableDefine.add(CALENDAR_ID,
				new TableColumnInfo(Messages.getString("calendar.id", locale), TableColumnInfo.NONE, 80, SWT.LEFT));

		tableDefine.add(SCHEDULE,
				new TableColumnInfo(Messages.getString("schedule", locale), TableColumnInfo.SCHEDULE, 100, SWT.LEFT));

		tableDefine.add(VALID_FLG,
				new TableColumnInfo(Messages.getString("valid", locale) + "/" + Messages.getString("invalid", locale), TableColumnInfo.VALID, 80, SWT.LEFT));

		// オーナーロール
		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 130, SWT.LEFT));

		tableDefine.add(CREATE_USER,
				new TableColumnInfo(Messages.getString("creator.name", locale), TableColumnInfo.NONE, 80, SWT.LEFT));

		tableDefine.add(CREATE_TIME,
				new TableColumnInfo(Messages.getString("create.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));

		tableDefine.add(UPDATE_USER,
				new TableColumnInfo(Messages.getString("modifier.name", locale), TableColumnInfo.NONE, 80, SWT.LEFT));

		tableDefine.add(UPDATE_TIME,
				new TableColumnInfo(Messages.getString("update.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));

		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
