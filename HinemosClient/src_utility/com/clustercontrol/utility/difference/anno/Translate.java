/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference.anno;


/**
 * プロパティ値を文字列に対応させる目印。
 * プロパティに付加できる。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class Translate extends AnnoSubstitute {
	/**
	 * 変換するプロパティの値と文字列を保持する。
	 * 
	 *
	 */
	public static class Value {
		public String value = "value";
		public String name = "name";
	}
	
	/**
	 * 変換のためのプロパティ値と文字列の対応リスト。
	 */
	public Value[] values;
}
