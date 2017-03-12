/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.accesscontrol.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * アカウント[ユーザ]ビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * アカウントのビュー[ユーザ]のカラムの情報を取得します。
 * Hinemosではロケールによって動作を変えるために定義情報も
 * マネージャから取得します。
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class GetUserListTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;

	/** ユーザID */
	public static final int UID = 1;

	/** ユーザ名 */
	public static final int NAME = 2;

	/** 説明 */
	public static final int DESCRIPTION = 3;

	/** メールアドレス*/
	public static final int MAIL_ADDRESS = 4;

	/** 新規作成ユーザ */
	public static final int CREATOR_NAME = 5;

	/** 作成日時 */
	public static final int CREATE_TIME = 6;

	/** 最終更新ユーザ */
	public static final int MODIFIER_NAME = 7;

	/** 最終更新日時 */
	public static final int MODIFY_TIME = 8;

	/** ダミー**/
	public static final int DUMMY=9;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = UID;

	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;

	/**
	 * マネージャにSessionBean経由でアクセスし、<BR>
	 * アクセス[ユーザ]ビューのテーブル定義を取得します。
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 *
	 * @see com.clustercontrol.accesscontrol.ejb.session.AccessController#getUserListTableDefine(java.util.Locale)
	 */
	public static ArrayList<TableColumnInfo> get() {
		Locale locale = Locale.getDefault();
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();

		/** メイン処理 */
		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(UID, new TableColumnInfo( Messages.getString("user.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT) );
		tableDefine.add(NAME, new TableColumnInfo( Messages.getString("user.name", locale), TableColumnInfo.NONE, 150, SWT.LEFT) );
		tableDefine.add(DESCRIPTION, new TableColumnInfo( Messages.getString("description", locale), TableColumnInfo.NONE, 200, SWT.LEFT) );
		tableDefine.add(MAIL_ADDRESS, new TableColumnInfo( Messages.getString("mail.address", locale), TableColumnInfo.NONE, 150, SWT.LEFT) );
		tableDefine.add(CREATOR_NAME, new TableColumnInfo( Messages.getString("creator.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT) );
		tableDefine.add(CREATE_TIME, new TableColumnInfo( Messages.getString("create.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT) );
		tableDefine.add(MODIFIER_NAME, new TableColumnInfo( Messages.getString("modifier.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT) );
		tableDefine.add(MODIFY_TIME, new TableColumnInfo( Messages.getString("update.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT) );
		tableDefine.add(DUMMY, new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
