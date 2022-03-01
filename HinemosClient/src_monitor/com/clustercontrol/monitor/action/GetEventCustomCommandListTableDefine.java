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
 * 監視履歴[イベント・カスタムコマンドの実行]ダイアログのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 */
public class GetEventCustomCommandListTableDefine {

	
	/** コマンドNO */
	public static final int COMMAND_NO = 0;
	
	/**
	/** 選択。 */
	public static final int SELECT = 1;
	
	/** カスタムコマンド名。 */
	public static final int COMMAND_NAME = 2;

	/** 説明。 */
	public static final int DESCRIPTION = 3;
	
	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX1 = COMMAND_NO;
	public static final int SORT_COLUMN_INDEX2 = -1;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = 1;
	
	/**
	 * 監視[イベント・カスタムコマンド実行]ダイアログのテーブル定義情報を取得します。<BR><BR>
	 * リストに、カラム毎にテーブルカラム情報をセットします。
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 *
	 * @see com.clustercontrol.bean.TableColumnInfo#TableColumnInfo(java.lang.String, int, int, int)
	 */
	public static ArrayList<TableColumnInfo> getEventCustomCommandListTableDefine() {

		Locale locale = Locale.getDefault();

		/** テーブル情報定義配列 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();

		tableDefine.add(COMMAND_NO,
				new TableColumnInfo(Messages.getString("", locale), TableColumnInfo.NONE, 0, SWT.LEFT));
		tableDefine.add(SELECT,
				new TableColumnInfo(Messages.getString("", locale), TableColumnInfo.RADIO_BUTTON, 20, SWT.LEFT));
		tableDefine.add(COMMAND_NAME,
				new TableColumnInfo(Messages.getString("event.customcommand.displayname", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("event.customcommand.description", locale), TableColumnInfo.TEXT_DIALOG, 550, SWT.LEFT));
		
		return tableDefine;
	}

}
