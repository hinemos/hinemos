/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * ダイアログ用タグ情報一覧のテーブル定義情報を取得するクライアント側アクションクラス<BR>
 */
public class GetRpaScenarioOperationResultDetailTableDefine {

	/** 時刻  */
	public static final int LOG_TIME = 0;

	/** 重要度 */
	public static final int PRIORITY = 1;

	/** メッセージ */
	public static final int MESSAGE = 2;

	/** 実行時間 */
	public static final int RUNTIME = 3;
	
	/** 手動操作コスト */
	public static final int COEFFICIENT_COST = 4;

	/** ダミー**/
	public static final int DUMMY = 5;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX = LOG_TIME;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = 1;

	/**
	 * シナリオ実績詳細一覧のテーブル定義情報を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 */
	public static ArrayList<TableColumnInfo> get() {
		/** テーブル情報定義配列 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(LOG_TIME,
				new TableColumnInfo(Messages.getString("view.rpa.scenario.operation.result.search.column.date", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(PRIORITY,
				new TableColumnInfo(Messages.getString("view.rpa.scenario.operation.result.search.column.priority", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(MESSAGE,
				new TableColumnInfo(Messages.getString("view.rpa.scenario.operation.result.search.column.log", locale), TableColumnInfo.NONE, 450, SWT.LEFT));
		tableDefine.add(RUNTIME,
				new TableColumnInfo(Messages.getString("view.rpa.scenario.operation.result.search.column.runtime", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(COEFFICIENT_COST,
				new TableColumnInfo(Messages.getString("view.rpa.scenario.operation.result.search.column.coefficient.cost", locale), TableColumnInfo.NONE, 150, SWT.LEFT));

		return tableDefine;
	}
}
