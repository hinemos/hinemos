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

package com.clustercontrol.snmptrap.action;

import java.util.ArrayList;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * OIDテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class GetTrapDefineTableDefine {

	/** 監視項目ID */
	public static final int MONITOR_ID = 0;

	/** MIB */
	public static final int MIB = 1;

	/** トラップ名 */
	public static final int TRAP_NAME = 2;

	/** バージョン */
	public static final int VERSION = 3;

	/** OID */
	public static final int TRAP_OID = 4;

	/** generic_id */
	public static final int GENERIC_ID = 5;

	/** specific_id */
	public static final int SPECIFIC_ID = 6;

	/** 有効／無効 */
	public static final int VALID_FLG = 7;

//	/** 重要度 */
//	public static final int PRIORITY = 8;
	/** 変数で判定する */
	public static final int VARIABLE = 8;

	/** メッセージ */
	public static final int MESSAGE = 9;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = TRAP_NAME;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;


	/**
	 * OIDテーブル定義を取得します。<BR>
	 * 
	 * @return OIDテーブル定義
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();

		tableDefine.add(MONITOR_ID,
				new TableColumnInfo(Messages.getString("monitor.id"), TableColumnInfo.NONE, 0, SWT.LEFT));
		tableDefine.add(MIB,
				new TableColumnInfo(Messages.getString("mib"), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(TRAP_NAME,
				new TableColumnInfo(Messages.getString("trap.name"), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(VERSION,
				new TableColumnInfo(Messages.getString("monitor.snmptrap.version"), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(TRAP_OID,
				new TableColumnInfo(Messages.getString("oid"), TableColumnInfo.NONE, 120, SWT.LEFT));
		tableDefine.add(GENERIC_ID,
				new TableColumnInfo(Messages.getString("generic.id"), TableColumnInfo.NONE, 75, SWT.LEFT));
		tableDefine.add(SPECIFIC_ID,
				new TableColumnInfo(Messages.getString("specific.id"), TableColumnInfo.NONE, 75, SWT.LEFT));
		tableDefine.add(VALID_FLG,
				new TableColumnInfo(Messages.getString("valid") + "/" + Messages.getString("invalid"), TableColumnInfo.VALID, 80, SWT.LEFT));
//		tableDefine.add(PRIORITY,
//				new TableColumnInfo(Messages.getString("priority"), TableColumnInfo.PRIORITY, 70, SWT.LEFT));
		tableDefine.add(VARIABLE,
				new TableColumnInfo(Messages.getString("monitor.snmptrap.determine.by.specified.variable"), TableColumnInfo.PRIORITY, 100, SWT.LEFT));
		tableDefine.add(MESSAGE,
				new TableColumnInfo(Messages.getString("message"), TableColumnInfo.NONE, 150, SWT.LEFT));

		return tableDefine;
	}
}
