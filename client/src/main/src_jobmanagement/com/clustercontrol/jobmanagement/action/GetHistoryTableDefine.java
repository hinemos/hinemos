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
 * ジョブ[履歴]ビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * マネージャにSessionBean経由でアクセスし、テーブル定義を取得する
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class GetHistoryTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;
	/** 実行状態 */
	public static final int STATUS = 1;
	/** 終了状態 */
	public static final int END_STATUS = 2;
	/** 終了値 */
	public static final int END_VALUE = 3;
	/** セッションID */
	public static final int SESSION_ID = 4;
	/** ジョブID */
	public static final int JOB_ID = 5;
	/** ジョブ名 */
	public static final int JOB_NAME = 6;
	/** 所属ジョブユニットのジョブID */
	public static final int JOBUNIT_ID = 7;
	/** 種別 */
	public static final int JOB_TYPE = 8;
	/** ファシリティID */
	public static final int FACILITY_ID = 9;
	/** スコープ */
	public static final int SCOPE = 10;
	/** オーナーロール */
	public static final int OWNER_ROLE = 11;
	/** 開始予定日時 */
	public static final int SCHEDULED_START_TIME = 12;
	/** 開始・再実行日時 */
	public static final int START_RERUN_TIME = 13;
	/** 終了・中断日時 */
	public static final int END_SUSPEND_TIME = 14;
	/** 実行時間 */
	public static final int SESSION_TIME = 15;
	/** 実行契機種別 */
	public static final int TRIGGER_TYPE = 16;
	/** 実行契機情報 */
	public static final int TRIGGER_INFO = 17;
	/** ダミー**/
	public static final int DUMMY=18;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX1 = SESSION_ID;
	public static final int SORT_COLUMN_INDEX2 = MANAGER_NAME;
	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = -1;

	/**
	 * マネージャにSessionBean経由でアクセスし、<BR>
	 * ジョブ[履歴]ビューのテーブル定義を取得する
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(STATUS,
				new TableColumnInfo(Messages.getString("run.status", locale), TableColumnInfo.STATE, 70, SWT.LEFT));
		tableDefine.add(END_STATUS,
				new TableColumnInfo(Messages.getString("end.status", locale), TableColumnInfo.END_STATUS, 60, SWT.LEFT));
		tableDefine.add(END_VALUE,
				new TableColumnInfo(Messages.getString("end.value", locale), TableColumnInfo.NONE, 50, SWT.LEFT));
		tableDefine.add(SESSION_ID,
				new TableColumnInfo(Messages.getString("session.id", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(JOB_ID,
				new TableColumnInfo(Messages.getString("job.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(JOB_NAME,
				new TableColumnInfo(Messages.getString("job.name", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(JOBUNIT_ID,
				new TableColumnInfo(Messages.getString("jobunit.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(JOB_TYPE,
				new TableColumnInfo(Messages.getString("type", locale), TableColumnInfo.JOB, 110, SWT.LEFT));
		tableDefine.add(FACILITY_ID,
				new TableColumnInfo(Messages.getString("facility.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(SCOPE,
				new TableColumnInfo(Messages.getString("scope", locale), TableColumnInfo.FACILITY, 150, SWT.LEFT));
		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 130, SWT.LEFT));
		tableDefine.add(SCHEDULED_START_TIME,
				new TableColumnInfo(Messages.getString("scheduled.start.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(START_RERUN_TIME,
				new TableColumnInfo(Messages.getString("start.rerun.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(END_SUSPEND_TIME,
				new TableColumnInfo(Messages.getString("end.suspend.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(SESSION_TIME,
				new TableColumnInfo(Messages.getString("session.time", locale), TableColumnInfo.NONE, 70, SWT.LEFT));
		tableDefine.add(TRIGGER_TYPE,
				new TableColumnInfo(Messages.getString("trigger.type", locale), TableColumnInfo.NONE, 110, SWT.LEFT));
		tableDefine.add(TRIGGER_INFO,
				new TableColumnInfo(Messages.getString("trigger.info", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(DUMMY, new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
