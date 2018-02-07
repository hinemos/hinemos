/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.dialog;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * 監視一覧テーブル定義情報を取得するクライアント側アクションクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class GetNodeSearchResultTableDefine {

	/** ファシリティID */
	public static final int FACILITY_ID = 0;

	/** ip */
	public static final int IP_ADDRESS = 1;

	/** message */
	public static final int MESSAGE = 2;

	/** dummy */
	public static final int DUMMY = 3;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX1 = IP_ADDRESS;
	public static final int SORT_COLUMN_INDEX2 = FACILITY_ID;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = 1;

	/**
	 *監視一覧のテーブル定義情報を返します。
	 *
	 * @return 監視一覧テーブル定義情報
	 */
	public static ArrayList<TableColumnInfo> get() {
		// テーブル情報定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		// Manager Name
		// ファシリティID
		tableDefine.add(FACILITY_ID,
				new TableColumnInfo(Messages.getString("facility.id", locale), TableColumnInfo.FACILITY_ID, 110, SWT.LEFT));

		// ip
		tableDefine.add(IP_ADDRESS,
				new TableColumnInfo(Messages.getString("ip.address", locale), TableColumnInfo.NONE, 110, SWT.LEFT));

		// message
		tableDefine.add(MESSAGE,
				new TableColumnInfo(Messages.getString("message", locale), TableColumnInfo.NONE, 300, SWT.LEFT));

		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
