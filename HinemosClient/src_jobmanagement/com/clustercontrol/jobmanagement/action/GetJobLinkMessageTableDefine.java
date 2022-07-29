/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
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
 * ジョブ履歴[受信ジョブ連携メッセージ一覧]ビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * マネージャにアクセスし、テーブル定義を取得する
 *
 */
public class GetJobLinkMessageTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;
	/** ジョブ連携メッセージID */
	public static final int JOBLINK_MESSAGE_ID = 1;
	/** 送信元ファシリティID */
	public static final int FACILITY_ID = 2;
	/** 送信元スコープ */
	public static final int SCOPE = 3;
	/** 監視詳細 */
	public static final int MONITOR_DETAIL_ID = 4;
	/** アプリケーション */
	public static final int APPLICATION = 5;
	/** 重要度 */
	public static final int PRIORITY = 6;
	/** メッセージ */
	public static final int MESSAGE = 7;
	/** オリジナルメッセージ */
	public static final int MESSAGE_ORG = 8;
	/** 送信日時 */
	public static final int SEND_DATE = 9;
	/** 受信日時 */
	public static final int ACCEPT_DATE = 10;
	/** ダミー **/
	public static final int DUMMY = 11;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX1 = SEND_DATE;
	public static final int SORT_COLUMN_INDEX2 = MANAGER_NAME;
	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = -1;

	/**
	 * マネージャにアクセスし、<BR>
	 * ジョブ履歴[受信ジョブ連携メッセージ一覧]ビューのテーブル定義を取得する
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 */
	public static ArrayList<TableColumnInfo> get() {
		// テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME, new TableColumnInfo(Messages.getString("facility.manager", locale),
				TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(JOBLINK_MESSAGE_ID, new TableColumnInfo(Messages.getString("joblink.message.id", locale),
				TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(FACILITY_ID, new TableColumnInfo(Messages.getString("source.facility.id", locale),
				TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(SCOPE, new TableColumnInfo(Messages.getString("source.scope", locale), TableColumnInfo.FACILITY,
				100, SWT.LEFT));
		tableDefine.add(MONITOR_DETAIL_ID, new TableColumnInfo(Messages.getString("monitor.detail.id", locale),
				TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(APPLICATION,
				new TableColumnInfo(Messages.getString("application", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(PRIORITY,
				new TableColumnInfo(Messages.getString("priority", locale), TableColumnInfo.PRIORITY, 60, SWT.LEFT));
		tableDefine.add(MESSAGE,
				new TableColumnInfo(Messages.getString("message", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(MESSAGE_ORG,
				new TableColumnInfo(Messages.getString("message.org", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(SEND_DATE,
				new TableColumnInfo(Messages.getString("send.time", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(ACCEPT_DATE,
				new TableColumnInfo(Messages.getString("receive.time", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(DUMMY, new TableColumnInfo("", TableColumnInfo.DUMMY, 100, SWT.LEFT));

		return tableDefine;
	}
}
