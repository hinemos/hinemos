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

package com.clustercontrol.jobmanagement.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * ジョブ[スケジュール予定]ビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * マネージャにSessionBean経由でアクセスし、テーブル定義を取得する
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class GetPlanTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;
	/** 日時*/
	public static final int DATE = 1;
	/** 実行契機ID */
	public static final int JOBKICK_ID = 2;
	/** 実行契機名 */
	public static final int JOBKICK_NAME = 3;
	/** ジョブユニットID */
	public static final int JOBUNIT_ID = 4;
	/** ジョブID */
	public static final int JOB_ID = 5;
	/** ジョブ名 */
	public static final int JOB_NAME = 6;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX1 = DATE;
	public static final int SORT_COLUMN_INDEX2 = MANAGER_NAME;
	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;

	/**
	 * マネージャにSessionBean経由でアクセスし、<BR>
	 * ジョブ[スケジュール予定]ビューのテーブル定義を取得する
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(DATE,
				new TableColumnInfo(Messages.getString("time", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(JOBKICK_ID,
				new TableColumnInfo(Messages.getString("jobkick.id", locale), TableColumnInfo.NONE, 120, SWT.LEFT));
		tableDefine.add(JOBKICK_NAME,
				new TableColumnInfo(Messages.getString("jobkick.name", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(JOBUNIT_ID,
				new TableColumnInfo(Messages.getString("jobunit.id", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(JOB_ID,
				new TableColumnInfo(Messages.getString("job.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(JOB_NAME,
				new TableColumnInfo(Messages.getString("job.name", locale), TableColumnInfo.NONE, 150, SWT.LEFT));

		return tableDefine;
	}
}
