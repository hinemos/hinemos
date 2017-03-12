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
 * ジョブ[ファイル転送]ビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 * 
 * マネージャにSessionBean経由でアクセスし、テーブル定義を取得します。
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class GetForwardFileTableDefine {

	/** 実行状態 */
	public static final int STATUS = 0;
	/** 終了状態 */
	public static final int END_STATUS = 1;
	/** ファイル */
	public static final int FILE_NAME = 2;
	/** 転送ファシリティID */
	public static final int SRC_FACILITY_ID = 3;
	/** 転送ファシリティ名 */
	public static final int SRC_FACILITY_NAME = 4;
	/** 受信ファシリティID */
	public static final int DEST_FACILITY_ID = 5;
	/** 受信ファシリティ名 */
	public static final int DEST_FACILITY_NAME = 6;
	/** 開始・再実行日時 */
	public static final int START_RERUN_TIME = 7;
	/** 終了・中断日時 */
	public static final int END_SUSPEND_TIME = 8;
	/** 実行時間 */
	public static final int SESSION_TIME = 9;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = STATUS;
	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;


	/**
	 * マネージャにSessionBean経由でアクセスし、
	 * ジョブ[ファイル転送]ビューのテーブル定義を取得します。<BR>
	 * 
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(STATUS,
				new TableColumnInfo(Messages.getString("run.status", locale), TableColumnInfo.STATE, 60, SWT.LEFT));
		tableDefine.add(END_STATUS,
				new TableColumnInfo(Messages.getString("end.status", locale), TableColumnInfo.END_STATUS, 60, SWT.LEFT));
		tableDefine.add(FILE_NAME,
				new TableColumnInfo(Messages.getString("file", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(SRC_FACILITY_ID,
				new TableColumnInfo(Messages.getString("forward.source", locale) + Messages.getString("facility.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(SRC_FACILITY_NAME,
				new TableColumnInfo(Messages.getString("forward.source", locale) + Messages.getString("facility.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(DEST_FACILITY_ID,
				new TableColumnInfo(Messages.getString("forward.destination", locale) + Messages.getString("facility.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(DEST_FACILITY_NAME,
				new TableColumnInfo(Messages.getString("forward.destination", locale) + Messages.getString("facility.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(START_RERUN_TIME,
				new TableColumnInfo(Messages.getString("start.rerun.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(END_SUSPEND_TIME,
				new TableColumnInfo(Messages.getString("end.suspend.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(SESSION_TIME,
				new TableColumnInfo(Messages.getString("session.time", locale), TableColumnInfo.NONE, 70, SWT.LEFT));

		return tableDefine;
	}
}
