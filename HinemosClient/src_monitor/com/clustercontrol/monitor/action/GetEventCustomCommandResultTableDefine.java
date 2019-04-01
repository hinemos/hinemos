/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * 監視履歴[イベント・カスタムコマンドの実行結果]ダイアログのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 */
public class GetEventCustomCommandResultTableDefine {

	
	/** 処理順 */
	public static final int ORDER = 0;
	
	/** イベント情報 */
	public static final int EVENTINFO = 1;
	
	/** 結果 */
	public static final int RESULT = 2;

	/** リターンコード */
	public static final int RETURN_CODE = 3;

	/** 詳細メッセージ */
	public static final int MESSAGE = 4;
	
	/** ダミー */
	public static final int DUMMY = 5;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX1 = ORDER;
	public static final int SORT_COLUMN_INDEX2 = -1;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = 1;

	/**
	 * イベントカスタムコマンド実行結果一覧のテーブル定義情報を返します。
	 *
	 * @return イベントカスタムコマンド実行結果一覧テーブル定義情報
	 */
	public static ArrayList<TableColumnInfo> getCustomCommandResultTableDefine() {
		// テーブル情報定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		
		// 処理順
		tableDefine.add(ORDER,
				new TableColumnInfo(Messages.getString("", locale), TableColumnInfo.NONE, 0, SWT.LEFT));
		
		// イベント情報
		tableDefine.add(EVENTINFO,
				new TableColumnInfo(Messages.getString("event.customcommand.result.processing.event.info", locale), TableColumnInfo.TEXT_DIALOG, 80, SWT.LEFT));
		
		// 結果
		tableDefine.add(RESULT,
				new TableColumnInfo(Messages.getString("event.customcommand.result.processing.result", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		
		// リターンコード
		tableDefine.add(RETURN_CODE,
				new TableColumnInfo(Messages.getString("event.customcommand.result.processing.returncode", locale), TableColumnInfo.NONE, 30, SWT.LEFT));
		
		// 詳細メッセージ
		tableDefine.add(MESSAGE,
				new TableColumnInfo(Messages.getString("event.customcommand.result.processing.message", locale), TableColumnInfo.TEXT_DIALOG, 350, SWT.LEFT));

		//ダミー
		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 10, SWT.LEFT));

		return tableDefine;
	}
}
