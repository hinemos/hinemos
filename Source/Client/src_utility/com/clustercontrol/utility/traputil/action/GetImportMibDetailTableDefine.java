/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.traputil.action;

import java.util.ArrayList;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.traputil.bean.ImportMibDetailTableDefine;


/**
 * マスタテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 6.1.0
 * @since 2.4.0
 */
public class GetImportMibDetailTableDefine {

	/**
	 * マスタテーブル定義を取得します。<BR>
	 * 
	 * @return マスタテーブル定義
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<>();

		tableDefine.add(ImportMibDetailTableDefine.RUN_STATUS, 
				new TableColumnInfo(Messages.getString("traputil.status"), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(ImportMibDetailTableDefine.MIB, 
				new TableColumnInfo(Messages.getString("mib"), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(ImportMibDetailTableDefine.TRAP_NAME, 
				new TableColumnInfo(Messages.getString("trap.name"), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(ImportMibDetailTableDefine.TRAP_OID, 
				new TableColumnInfo(Messages.getString("oid"), TableColumnInfo.NONE, 120, SWT.LEFT));
		tableDefine.add(ImportMibDetailTableDefine.GENERIC_ID, 
				new TableColumnInfo(Messages.getString("generic.id"), TableColumnInfo.NONE, 75, SWT.LEFT));
		tableDefine.add(ImportMibDetailTableDefine.SPECIFIC_ID, 
				new TableColumnInfo(Messages.getString("specific.id"), TableColumnInfo.NONE, 75, SWT.LEFT));
		
		return tableDefine;
	}
}
