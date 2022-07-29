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
 * 環境構築[構築・チェック]ビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * マネージャにSessionBean経由でアクセスし、テーブル定義を取得する
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class GetInfraManagementTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;
	/** 構築ID */
	public static final int MANAGEMENT_ID = 1;
	/** 構築名 */
	public static final int MANAGEMENT_NAME = 2;
	/** 説明 */
	public static final int DESCRIPTION = 3;
	/** 有効・無効 */
	public static final int VALID = 4;
	/** ファシリティID */
	public static final int FACILITY_ID = 5;
	/** スコープ */
	public static final int SCOPE = 6;
	/** オーナーロール */
	public static final int OWNER_ROLE = 7;
	/** 登録ユーザ */
	public static final int CREATE_USER = 8;
	/** 登録日 */
	public static final int CREATE_TIME = 9;
	/** 更新ユーザ */
	public static final int UPDATE_USER = 10;
	/** 更新日 */
	public static final int UPDATE_TIME = 11;
	/** ダミー**/
	public static final int DUMMY = 12;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = MANAGEMENT_ID;
	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;

	/**
	 * マネージャにSessionBean経由でアクセスし、<BR>
	 * 環境構築[構築・チェック]ビューのテーブル定義を取得する
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(MANAGEMENT_ID,
				new TableColumnInfo(Messages.getString("infra.management.id", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(MANAGEMENT_NAME,
				new TableColumnInfo(Messages.getString("infra.management.name", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(VALID,
				new TableColumnInfo(Messages.getString("valid", locale) + "/" + Messages.getString("invalid", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(FACILITY_ID,
				new TableColumnInfo(Messages.getString("facility.id", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(SCOPE,
				new TableColumnInfo(Messages.getString("scope", locale), TableColumnInfo.FACILITY, 150, SWT.LEFT));
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
		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));


		return tableDefine;
	}
}
