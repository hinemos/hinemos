/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * ジョブ[ジョブ詳細]ビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 * 
 * マネージャにSessionBean経由でアクセスし、テーブル定義を取得する
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class GetJobDetailTableDefine {

	/** ツリー */
	public static final int TREE = 0;
	/** 実行状態 */
	public static final int STATUS = 1;
	/** スキップ */
	public static final int SKIP = 2;
	/** 終了状態 */
	public static final int END_STATUS = 3;
	/** 終了値 */
	public static final int END_VALUE = 4;
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
	/** 時刻 */
	public static final int WAIT_RULE_TIME = 11;
	/** 開始・再実行日時 */
	public static final int START_RERUN_TIME = 12;
	/** 終了・中断日時 */
	public static final int END_SUSPEND_TIME = 13;
	/** 実行時間 */
	public static final int SESSION_TIME = 14;
	/** 実行回数 */
	public static final int RUN_COUNT = 15;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = JOB_ID;
	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;

	/**
	 * マネージャにSessionBean経由でアクセスし、<BR>
	 * ジョブ[ジョブ詳細]ビューのテーブル定義を取得する
	 * 
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(TREE,
				new TableColumnInfo("", TableColumnInfo.NONE, 30, SWT.LEFT));
		tableDefine.add(STATUS,
				new TableColumnInfo(Messages.getString("run.status", locale), TableColumnInfo.STATE, 100, SWT.LEFT));
		tableDefine.add(SKIP,
				new TableColumnInfo(Messages.getString("skip", locale), TableColumnInfo.CHECKBOX, 50, SWT.LEFT));
		tableDefine.add(END_STATUS,
				new TableColumnInfo(Messages.getString("end.status", locale), TableColumnInfo.END_STATUS, 65, SWT.LEFT));
		tableDefine.add(END_VALUE,
				new TableColumnInfo(Messages.getString("end.value", locale), TableColumnInfo.NONE, 50, SWT.LEFT));
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
		tableDefine.add(WAIT_RULE_TIME,
				new TableColumnInfo(Messages.getString("wait.rule.time", locale), TableColumnInfo.WAIT_RULE_VALUE, 80, SWT.LEFT));
		tableDefine.add(START_RERUN_TIME,
				new TableColumnInfo(Messages.getString("start.rerun.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(END_SUSPEND_TIME,
				new TableColumnInfo(Messages.getString("end.suspend.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(SESSION_TIME,
				new TableColumnInfo(Messages.getString("session.time", locale), TableColumnInfo.NONE, 70, SWT.LEFT));
		tableDefine.add(RUN_COUNT,
				new TableColumnInfo(Messages.getString("job.run.count", locale), TableColumnInfo.NONE, 70, SWT.LEFT));

		return tableDefine;
	}
}
