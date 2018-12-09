/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference.anno;

/**
 * オーバーライドしたい "Translate" が付加されたプロパティを含む型のプロパティに指定する。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class TranslateOverrides extends AnnoSubstitute {
	/**
	 * 上書きしたプロパティ名と上書き後に適用したい目印の "Translate" を保持する。
	 * 
	 */
	public static class Value {
		public String p = "p";
		public Translate t = new Translate();
	}

	/**
	 * 上書きしたいプロパティのリスト。
	 */
	public Value values[];
}
