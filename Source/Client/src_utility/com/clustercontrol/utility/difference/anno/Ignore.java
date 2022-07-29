/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference.anno;

/**
 * この目印は、プロパティに指定する。
 * プロパティ値の型が、Column を指定されたプロパティを保持しており、かつ、比較処理でそのプロパティの処理をキャンセルしたい場合に利用する。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class Ignore extends AnnoSubstitute {
	
	/**
	 * 比較処理をキャンセルしたプロパティのリスト。
	 */
	public String[] propNames;
}
