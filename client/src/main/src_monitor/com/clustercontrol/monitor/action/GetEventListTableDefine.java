/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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
 * 監視[イベント]ビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * マネージャにSessionBean経由でアクセスし、テーブル定義を取得します。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class GetEventListTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;

	/** 重要度。 */
	public static final int PRIORITY = 1;

	/** 受信日時。 */
	public static final int RECEIVE_TIME = 2;

	/** 出力日時。 */
	public static final int OUTPUT_DATE = 3;

	/** プラグインID。 */
	public static final int PLUGIN_ID = 4;

	/** 監視項目ID。 */
	public static final int MONITOR_ID = 5;

	/** 監視詳細。 */
	public static final int MONITOR_DETAIL_ID = 6;

	/** ファシリティID。 */
	public static final int FACILITY_ID = 7;

	/** スコープ。 */
	public static final int SCOPE = 8;

	/** アプリケーション。 */
	public static final int APPLICATION = 9;

	/** メッセージ。 */
	public static final int MESSAGE = 10;

	/** 確認。 */
	public static final int CONFIRMED = 11;

	/** 確認ユーザ */
	public static final int CONFIRM_USER = 12;

	/** コメント */
	public static final int COMMENT = 13;

	/** オーナーロール */
	public static final int OWNER_ROLE = 14;

	/** ダミー**/
	public static final int DUMMY=15;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX1 = RECEIVE_TIME;
	public static final int SORT_COLUMN_INDEX2 = MANAGER_NAME;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = -1;

	/**
	 * 監視[イベント]ビューのテーブル定義情報を取得します。<BR><BR>
	 * リストに、カラム毎にテーブルカラム情報をセットします。
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 *
	 * @see com.clustercontrol.bean.TableColumnInfo#TableColumnInfo(java.lang.String, int, int, int)
	 * @see com.clustercontrol.monitor.bean.EventTableDefine
	 */
	public static ArrayList<TableColumnInfo> getEventListTableDefine() {

		Locale locale = Locale.getDefault();

		/** テーブル情報定義配列 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(PRIORITY,
				new TableColumnInfo(Messages.getString("priority", locale), TableColumnInfo.PRIORITY, 55, SWT.LEFT));
		tableDefine.add(RECEIVE_TIME,
				new TableColumnInfo(Messages.getString("receive.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(OUTPUT_DATE,
				new TableColumnInfo(Messages.getString("output.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(PLUGIN_ID,
				new TableColumnInfo(Messages.getString("plugin.id", locale), TableColumnInfo.NONE, 90, SWT.LEFT));
		tableDefine.add(MONITOR_ID,
				new TableColumnInfo(Messages.getString("monitor.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(MONITOR_DETAIL_ID,
				new TableColumnInfo(Messages.getString("monitor.detail.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(FACILITY_ID,
				new TableColumnInfo(Messages.getString("facility.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(SCOPE,
				new TableColumnInfo(Messages.getString("scope", locale), TableColumnInfo.FACILITY, 150, SWT.LEFT));
		tableDefine.add(APPLICATION,
				new TableColumnInfo(Messages.getString("application", locale), TableColumnInfo.NONE, 120, SWT.LEFT));
		tableDefine.add(MESSAGE,
				new TableColumnInfo(Messages.getString("message", locale), TableColumnInfo.NONE, 200, SWT.LEFT));
		tableDefine.add(CONFIRMED,
				new TableColumnInfo(Messages.getString("confirmed", locale), TableColumnInfo.CONFIRM, 50, SWT.LEFT));
		tableDefine.add(CONFIRM_USER,
				new TableColumnInfo(Messages.getString("confirm.user", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(COMMENT,
				new TableColumnInfo(Messages.getString("comment", locale), TableColumnInfo.NONE, 200, SWT.LEFT));
		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 130, SWT.LEFT));
		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}

}
