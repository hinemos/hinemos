/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * 転送先設定テーブルの定義を取得するクライアント側アクションクラス<BR>
 */
public class GetTransferInfoDestPropDefine {

	/** 設定 */
	public static final int SETTINGS = 0;

	/** 値 */
	public static final int VALUE = 1;

	/** 説明 */
	public static final int DESCRIPTION = 2;

	/** ダミー**/
	public static final int DUMMY = 3;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = SETTINGS;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;

	/**
	 * 収集蓄積[転送]テーブル定義を取得します。<BR>
	 *
	 * @return
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(SETTINGS,
				new TableColumnInfo(Messages.getString("dialog.hub.log.transfer.setting.key", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(VALUE,
				new TableColumnInfo(Messages.getString("value", locale), TableColumnInfo.NONE, 250, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 100, SWT.LEFT));

		return tableDefine;
	}
}
