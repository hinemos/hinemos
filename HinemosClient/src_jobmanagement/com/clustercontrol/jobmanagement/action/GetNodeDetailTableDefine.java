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
 * ジョブ[ノード詳細]ビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 * 
 * マネージャにSessionBean経由でアクセスし、テーブル定義を取得する
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class GetNodeDetailTableDefine {

	/** 実行状態 */
	public static final int STATUS = 0;
	/** 戻り値 */
	public static final int RETURN_VALUE = 1;
	/** ファシリティID */
	public static final int FACILITY_ID = 2;
	/** ファシリティ名 */
	public static final int FACILITY_NAME = 3;
	/** 開始・再実行日時 */
	public static final int START_RERUN_TIME = 4;
	/** 終了・中断日時 */
	public static final int END_SUSPEND_TIME = 5;
	/** 実行時間 */
	public static final int SESSION_TIME = 6;
	/** メッセージ */
	public static final int MESSAGE = 7;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = FACILITY_ID;
	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;

	/**
	 * マネージャにSessionBean経由でアクセスし、<BR>
	 * ジョブ[ノード詳細]ビューのテーブル定義を取得する
	 * 
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(STATUS,
				new TableColumnInfo(Messages.getString("run.status", locale), TableColumnInfo.STATE, 70, SWT.LEFT));
		tableDefine.add(RETURN_VALUE,
				new TableColumnInfo(Messages.getString("return.value", locale), TableColumnInfo.NONE, 50, SWT.LEFT));
		tableDefine.add(FACILITY_ID,
				new TableColumnInfo(Messages.getString("facility.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(FACILITY_NAME,
				new TableColumnInfo(Messages.getString("facility.name", locale), TableColumnInfo.FACILITY, 150, SWT.LEFT));
		tableDefine.add(START_RERUN_TIME,
				new TableColumnInfo(Messages.getString("start.rerun.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(END_SUSPEND_TIME,
				new TableColumnInfo(Messages.getString("end.suspend.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(SESSION_TIME,
				new TableColumnInfo(Messages.getString("session.time", locale), TableColumnInfo.NONE, 70, SWT.LEFT));
		tableDefine.add(MESSAGE,
				new TableColumnInfo(Messages.getString("message", locale), TableColumnInfo.TEXT_DIALOG, 300, SWT.LEFT));

		return tableDefine;
	}
}
