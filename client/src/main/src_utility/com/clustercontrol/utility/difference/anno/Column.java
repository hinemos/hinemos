/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference.anno;


/**
 * 比較結果を出力するプロパティに付加する。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class Column extends AnnoSubstitute {
	
	public Column() {
		
	}
	
	public Column(String columnName) {
		this.columnName = columnName;
	}
	
	/**
	 * 比較項目名。
	 */
	public String columnName;
}
