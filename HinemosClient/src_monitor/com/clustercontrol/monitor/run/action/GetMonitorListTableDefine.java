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

package com.clustercontrol.monitor.run.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * 監視一覧テーブル定義情報を取得するクライアント側アクションクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class GetMonitorListTableDefine {

	/** Manager Name */
	public static final int MANAGER_NAME = 0;

	/** 監視項目ID。 */
	public static final int MONITOR_ID = 1;

	/**
	 * 監視種別ID(プラグインID)。
	 *
	 */
	public static final int MONITOR_TYPE_ID = 2;

	/**
	 * 監視種別。
	 *
	 * @see com.clustercontrol.monitor.run.bean.MonitorTypeConstant
	 */
	public static final int MONITOR_TYPE = 3;


	/** 説明。 */
	public static final int DESCRIPTION = 4;

	/** ファシリティID */
	public static final int FACILITY_ID = 5;

	/** スコープ */
	public static final int SCOPE = 6;

	/** カレンダID。 */
	public static final int CALENDAR_ID = 7;

	/**
	 * 実行間隔。
	 *
	 * @see com.clustercontrol.bean.RunIntervalConstant
	 */
	public static final int RUN_INTERVAL = 8;

	/** 監視有効／無効。 */
	public static final int MONITOR_FLG = 9;

	/** 収集有効／無効。 */
	public static final int COLLECTOR_FLG = 10;

	/** オーナーロール */
	public static final int OWNER_ROLE = 11;


	/** 新規作成ユーザ。 */
	public static final int CREATE_USER = 12;

	/** 作成日時。 */
	public static final int CREATE_TIME = 13;

	/** 最終変更ユーザ。 */
	public static final int UPDATE_USER = 14;

	/** 最終変更日時。 */
	public static final int UPDATE_TIME = 15;

	/** ダミー**/
	public static final int DUMMY = 16;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = MONITOR_ID;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = 1;

	/**
	 *監視一覧のテーブル定義情報を返します。
	 *
	 * @return 監視一覧テーブル定義情報
	 */
	public static ArrayList<TableColumnInfo> get() {
		// テーブル情報定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		// Manager Name
		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", "Manager", locale), TableColumnInfo.MANAGER_NAME, 100, SWT.LEFT));

		// 監視項目ID
		tableDefine.add(MONITOR_ID,
				new TableColumnInfo(Messages.getString("monitor.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));

		// 監視種別ID(プラグインID)
		tableDefine.add(MONITOR_TYPE_ID,
				new TableColumnInfo(Messages.getString("plugin.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));

		// 監視種別
		tableDefine.add(MONITOR_TYPE,
				new TableColumnInfo(Messages.getString("monitor.type", locale), TableColumnInfo.NONE, 80, SWT.LEFT));

		// 説明
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 200, SWT.LEFT));

		// ファシリティID
		tableDefine.add(FACILITY_ID,
				new TableColumnInfo(Messages.getString("facility.id", locale), TableColumnInfo.NONE, 110, SWT.LEFT));

		// スコープ
		tableDefine.add(SCOPE,
				new TableColumnInfo(Messages.getString("scope", locale), TableColumnInfo.FACILITY, 200, SWT.LEFT));

		// カレンダID
		tableDefine.add(CALENDAR_ID,
				new TableColumnInfo(Messages.getString("calendar", locale), TableColumnInfo.NONE, 100, SWT.LEFT));

		// 実行間隔
		tableDefine.add(RUN_INTERVAL,
				new TableColumnInfo(Messages.getString("run.interval", locale), TableColumnInfo.NONE, 40, SWT.LEFT));

		// 監視有効/無効
		tableDefine.add(MONITOR_FLG,
				new TableColumnInfo(Messages.getString("monitor.valid.name", locale), TableColumnInfo.VALID, 40, SWT.LEFT));

		// 収集有効/無効
		tableDefine.add(COLLECTOR_FLG,
				new TableColumnInfo(Messages.getString("collector.valid.name", locale), TableColumnInfo.VALID, 40, SWT.LEFT));

		// オーナーロール
		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 130, SWT.LEFT));

		// 新規作成ユーザ
		tableDefine.add(CREATE_USER,
				new TableColumnInfo(Messages.getString("creator.name", locale), TableColumnInfo.NONE, 80, SWT.LEFT));

		// 作成日時
		tableDefine.add(CREATE_TIME,
				new TableColumnInfo(Messages.getString("create.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));

		// 最終変更ユーザ
		tableDefine.add(UPDATE_USER,
				new TableColumnInfo(Messages.getString("modifier.name", locale), TableColumnInfo.NONE, 80, SWT.LEFT));

		// 最終変更日時
		tableDefine.add(UPDATE_TIME,
				new TableColumnInfo(Messages.getString("update.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));

		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
