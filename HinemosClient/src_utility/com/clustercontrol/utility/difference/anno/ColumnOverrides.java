/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference.anno;



/**
 * オーバーライドしたい "Column" が付加されたプロパティを含む型のプロパティに指定する。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class ColumnOverrides extends AnnoSubstitute {
	/**
	 * 上書きしたプロパティ名と上書き後に適用したい目印の "Column" を保持する。
	 * 
	 * 
	 *
	 */
	public static class Value {
		public String p = "p";
		public Column c = new Column();
	}

	/**
	 * 上書きしたいプロパティのリスト。
	 */
	public Value values[];
}
