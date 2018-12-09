/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.etc.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 *登録スコープ一覧のテーブル定義を取得するクライアント側アクションクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class NodeMapListTableDefine {
	/** 種別 */
	public static final int FACILITY_TYPE = 0;

	/** ファシリティID */
	public static final int FACILITY_ID = 1;

	/** ファシリティ名 */
	public static final int FACILITY_NAME = 2;

	/** IPv4 */
	public static final int IPADDRESS_V4 = 3;

	/** IPv6 */
	public static final int IPADDRESS_V6 = 4;

	/** ソート用データ**/
	public static final int SORT_VALUE=0;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX =SORT_VALUE ;

	public static final int SORT_COLUMN_INDEX_SECOND= FACILITY_ID;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;

	// ----- instance メソッド ----- //

	/**
	 *スコープ一覧テーブル定義を取得します。<BR>
	 * 
	 * @return ノード一覧
	 */
	public static ArrayList<TableColumnInfo> get() {
		/** 出力用変数 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		/** メイン処理 */
		tableDefine.add(FACILITY_TYPE,
				new TableColumnInfo(Messages.getString("type", locale), TableColumnInfo.FACILITY_ID, 80, SWT.LEFT));
		tableDefine.add(FACILITY_ID,
				new TableColumnInfo(Messages.getString("facility.id", locale), TableColumnInfo.FACILITY_ID, 100, SWT.LEFT));
		tableDefine.add(FACILITY_NAME,
				new TableColumnInfo(Messages.getString("facility.name", locale), TableColumnInfo.FACILITY_NAME, 150, SWT.LEFT));
		tableDefine.add(IPADDRESS_V4,
				new TableColumnInfo(Messages.getString("ip.address.v4", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(IPADDRESS_V6,
				new TableColumnInfo(Messages.getString("ip.address.v6", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		return tableDefine;
	}
}