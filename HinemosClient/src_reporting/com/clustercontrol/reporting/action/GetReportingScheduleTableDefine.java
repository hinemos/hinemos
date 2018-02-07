/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * レポーティングスケジュールテーブル定義情報を取得するクライアント側アクションクラス<BR>
 * 
 * @version 5.0.a
 * @since 4.1.2
 */
public class GetReportingScheduleTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;
	
	/** スケジュールID。 */
	public static final int REPORT_SCHEDULE_ID = 1;

	/** 説明。 */
	public static final int DESCRIPTION = 2;

	/** テンプレートセットID */
	public static final int TEMPLATE_SET_ID = 3;

	/** ファシリティID。 */
	public static final int FACILITY_ID = 4;

	/** スコープ。 */
	public static final int SCOPE = 5;

	/** カレンダID。 */
	public static final int CALENDAR_ID = 6;

	/**
	 * 有効／無効。
	 * 
	 * @see com.clustercontrol.bean.ValidConstant
	 */
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

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = REPORT_SCHEDULE_ID;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = 1;

	/**
	 * レポーティングスケジュールのテーブル定義情報を返します。
	 * 
	 * @return レポーティングスケジュールテーブル定義情報
	 */
	public static ArrayList<TableColumnInfo> get() {
		/** テーブル情報定義配列 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale),
						TableColumnInfo.NONE, 100, SWT.LEFT));
		
		tableDefine.add(REPORT_SCHEDULE_ID,
				new TableColumnInfo(Messages.getString("schedule.id", locale),
						TableColumnInfo.NONE, 100, SWT.LEFT));

		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale),
						TableColumnInfo.NONE, 150, SWT.LEFT));

		tableDefine.add(
				TEMPLATE_SET_ID,
				new TableColumnInfo(Messages.getString("template.set.id", locale),
						TableColumnInfo.NONE, 100, SWT.LEFT));

		tableDefine.add(FACILITY_ID,
				new TableColumnInfo(Messages.getString("facility.id", locale),
						TableColumnInfo.NONE, 100, SWT.LEFT));

		tableDefine.add(SCOPE,
				new TableColumnInfo(Messages.getString("scope", locale),
						TableColumnInfo.FACILITY, 150, SWT.LEFT));

		tableDefine.add(CALENDAR_ID,
				new TableColumnInfo(Messages.getString("calendar.id", locale),
						TableColumnInfo.NONE, 100, SWT.LEFT));

		tableDefine.add(VALID_FLG,
				new TableColumnInfo(Messages.getString("valid", locale) + "/"
						+ Messages.getString("invalid", locale),
						TableColumnInfo.VALID, 80, SWT.LEFT));

		// オーナーロール
		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(
						Messages.getString("owner.role.id", locale),
						TableColumnInfo.NONE, 130, SWT.LEFT));

		tableDefine.add(CREATE_USER,
				new TableColumnInfo(Messages.getString("creator.name", locale),
						TableColumnInfo.NONE, 80, SWT.LEFT));

		tableDefine.add(CREATE_TIME,
				new TableColumnInfo(Messages.getString("create.time", locale),
						TableColumnInfo.NONE, 140, SWT.LEFT));

		tableDefine.add(UPDATE_USER,
				new TableColumnInfo(
						Messages.getString("modifier.name", locale),
						TableColumnInfo.NONE, 80, SWT.LEFT));

		tableDefine.add(UPDATE_TIME,
				new TableColumnInfo(Messages.getString("update.time", locale),
						TableColumnInfo.NONE, 140, SWT.LEFT));

		return tableDefine;
	}
}