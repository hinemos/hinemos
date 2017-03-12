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

package com.clustercontrol.jobmanagement.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * ジョブ[実行契機]ビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * マネージャにSessionBean経由でアクセスし、テーブル定義を取得する
 *
 * @version 4.1.0
 * @since 1.0.0
 */
public class GetJobKickTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;
	/** 実行契機種別 */
	public static final int TYPE = 1;
	/** 実行契機ID */
	public static final int JOBKICK_ID = 2;
	/** 実行契機名 */
	public static final int JOBKICK_NAME = 3;
	/** ジョブID */
	public static final int JOB_ID = 4;
	/** ジョブ名 */
	public static final int JOB_NAME = 5;
	/** ジョブユニットID */
	public static final int JOBUNIT_ID = 6;
	/** 概要 */
	public static final int DETAIL = 7;
	/** カレンダID */
	public static final int CALENDAR_ID = 8;
	/** 有効/無効 */
	public static final int VALID = 9;
	/** オーナーロール */
	public static final int OWNER_ROLE = 10;
	/** 新規作成ユーザ */
	public static final int CREATE_USER = 11;
	/** 作成日時 */
	public static final int CREATE_TIME = 12;
	/** 最終更新ユーザ */
	public static final int UPDATE_USER = 13;
	/** 最終更新日時 */
	public static final int UPDATE_TIME = 14;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = JOBKICK_ID;
	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;

	/**
	 * マネージャにSessionBean経由でアクセスし、<BR>
	 * ジョブ[実行契機]ビューのテーブル定義を取得する
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(TYPE,
				new TableColumnInfo(Messages.getString("jobkick.type", locale), TableColumnInfo.NONE, 110, SWT.LEFT));
		tableDefine.add(JOBKICK_ID,
				new TableColumnInfo(Messages.getString("jobkick.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(JOBKICK_NAME,
				new TableColumnInfo(Messages.getString("jobkick.name", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(JOB_ID,
				new TableColumnInfo(Messages.getString("job.id", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(JOB_NAME,
				new TableColumnInfo(Messages.getString("job.name", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(JOBUNIT_ID,
				new TableColumnInfo(Messages.getString("jobunit.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(DETAIL,
				new TableColumnInfo(Messages.getString("schedule.setting", locale) + "/" + 
									Messages.getString("file.check.setting", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(CALENDAR_ID,
				new TableColumnInfo(Messages.getString("calendar.id", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(VALID,
				new TableColumnInfo(Messages.getString("valid", locale) + "/" + Messages.getString("invalid", locale), TableColumnInfo.VALID, 70, SWT.LEFT));
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

		return tableDefine;

	}
}
