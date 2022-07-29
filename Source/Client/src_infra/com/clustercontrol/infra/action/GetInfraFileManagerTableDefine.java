/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * 環境構築[ファイルマネージャ]ビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class GetInfraFileManagerTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;
	/** 配布ファイルID */
	public static final int FILE_ID = 1;
	/** 配布ファイル名 */
	public static final int FILE_NAME = 2;
	/** オーナーロールID */
	public static final int OWNER_ROLE = 3;
	/** 作成ロードユーザ */
	public static final int CREATE_USER = 4;
	/** 作成ロード日時 */
	public static final int CREATE_DATETIME = 5;
	/** 変更ロードユーザ */
	public static final int MODIFY_USER = 6;
	/** 変更ロード日時 */
	public static final int MODIFY_DATETIME = 7;
	/** ダミー**/
	public static final int DUMMY=8;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = FILE_ID;
	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;

	/**
	 * 環境構築[モジュール]ビューのテーブル定義を取得する
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(FILE_ID,
				new TableColumnInfo(Messages.getString("infra.filemanager.file.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(FILE_NAME,
				new TableColumnInfo(Messages.getString("infra.filemanager.file.name", locale), TableColumnInfo.NONE, 250, SWT.LEFT));
		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(CREATE_USER,
				new TableColumnInfo(Messages.getString("creator.name", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(CREATE_DATETIME,
				new TableColumnInfo(Messages.getString("create.time", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(MODIFY_USER,
				new TableColumnInfo(Messages.getString("modifier.name", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(MODIFY_DATETIME,
				new TableColumnInfo(Messages.getString("update.time", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}