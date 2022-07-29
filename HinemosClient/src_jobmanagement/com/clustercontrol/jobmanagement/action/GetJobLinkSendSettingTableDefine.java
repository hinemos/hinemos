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
 * ジョブ設定[ジョブ連携送信設定]ビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * マネージャにSessionBean経由でアクセスし、テーブル定義を取得する
 *
 * 
 */
public class GetJobLinkSendSettingTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;
	/** ジョブ連携送信設定ID */
	public static final int JOBLINK_SEND_SETTING_ID = 1;
	/** 説明 */
	public static final int DESCRIPTION = 2;
	/** 送信先ファシリティID */
	public static final int DESTINATION_FACILITY_ID = 3;
	/** 送信先スコープ */
	public static final int DESTINATION_SCOPE = 4;
	/** 送信先プロトコル */
	public static final int DESTINATION_PROTOCOL = 5;
	/** 送信先ポート */
	public static final int DESTINATION_PORT = 6;
	/** オーナーロール */
	public static final int OWNER_ROLE = 7;
	/** 新規作成ユーザ */
	public static final int CREATE_USER = 8;
	/** 作成日時 */
	public static final int CREATE_TIME = 9;
	/** 最終更新ユーザ */
	public static final int UPDATE_USER = 10;
	/** 最終更新日時 */
	public static final int UPDATE_TIME = 11;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = JOBLINK_SEND_SETTING_ID;
	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;

	/**
	 * テーブル定義を取得する
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(JOBLINK_SEND_SETTING_ID,
				new TableColumnInfo(Messages.getString("joblink.send.setting.id", locale), TableColumnInfo.NONE, 130, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(DESTINATION_FACILITY_ID,
				new TableColumnInfo(Messages.getString("destination.facility.id", locale), TableColumnInfo.NONE, 130, SWT.LEFT));
		tableDefine.add(DESTINATION_SCOPE,
				new TableColumnInfo(Messages.getString("destination.scope", locale), TableColumnInfo.FACILITY, 150, SWT.LEFT));
		tableDefine.add(DESTINATION_PROTOCOL,
				new TableColumnInfo(Messages.getString("destination.protocol", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(DESTINATION_PORT,
				new TableColumnInfo(Messages.getString("destination.port", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 130, SWT.LEFT));
		tableDefine.add(CREATE_USER,
				new TableColumnInfo(Messages.getString("creator.name", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(CREATE_TIME,
				new TableColumnInfo(Messages.getString("create.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(UPDATE_USER,
				new TableColumnInfo(Messages.getString("modifier.name", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(UPDATE_TIME,
				new TableColumnInfo(Messages.getString("update.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));

		return tableDefine;

	}
}
