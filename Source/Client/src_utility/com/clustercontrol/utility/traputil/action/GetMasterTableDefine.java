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
import com.clustercontrol.utility.traputil.bean.MasterTableDefine;


/**
 * マスタテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 6.1.0
 * @since 2.1.0
 */
public class GetMasterTableDefine {

	/**
	 * マスタテーブル定義を取得します。<BR>
	 * 
	 * @return マスタテーブル定義
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<>();

		tableDefine.add(MasterTableDefine.MIB, 
				new TableColumnInfo(Messages.getString("mib"), TableColumnInfo.NONE, 0, SWT.LEFT));
		tableDefine.add(MasterTableDefine.TRAP_NAME, 
				new TableColumnInfo(Messages.getString("trap.name"), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(MasterTableDefine.TRAP_OID, 
				new TableColumnInfo(Messages.getString("oid"), TableColumnInfo.NONE, 120, SWT.LEFT));
		tableDefine.add(MasterTableDefine.GENERIC_ID, 
				new TableColumnInfo(Messages.getString("generic.id"), TableColumnInfo.NONE, 75, SWT.LEFT));
		tableDefine.add(MasterTableDefine.SPECIFIC_ID, 
				new TableColumnInfo(Messages.getString("specific.id"), TableColumnInfo.NONE, 75, SWT.LEFT));
		tableDefine.add(MasterTableDefine.PRIORITY, 
				new TableColumnInfo(Messages.getString("priority"), TableColumnInfo.PRIORITY, 70, SWT.LEFT));
		tableDefine.add(MasterTableDefine.MESSAGE, 
				new TableColumnInfo(Messages.getString("message"), TableColumnInfo.NONE, 150, SWT.LEFT));
		
		return tableDefine;
	}
}
