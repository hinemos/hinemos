/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * テンプレートセット詳細一覧テーブル定義情報を取得するクライアント側アクションクラス<BR>
 * 
 * @version 5.0.a
 * @since 5.0.a
 */
public class GetTemplateSetDetailTableDefine {

	/** 順序 */
	public static final int ORDER_NO = 0;
	/** テンプレートID */
	public static final int TEMPLATE_ID = 1;
	/** 説明 */
	public static final int DESCRIPTION = 2;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = ORDER_NO;
	/** 初期表示時ソートオーダー  */
	public static final int SORT_ORDER = 1;

	/**
	 * テンプレートセット詳細一覧のテーブル定義情報を取得します。
	 * 
	 * @return テンプレートセット詳細一覧テーブル定義情報
	 */
	public static ArrayList<TableColumnInfo> get() {
		// テーブル情報定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();
		tableDefine.add(ORDER_NO,
				new TableColumnInfo(Messages.getString("order", locale), TableColumnInfo.NONE, 40, SWT.CENTER));
		tableDefine.add(TEMPLATE_ID,
				new TableColumnInfo(Messages.getString("template.id",locale), TableColumnInfo.NONE,150, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 170, SWT.LEFT));
		return tableDefine;
	}
}
