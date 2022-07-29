/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference.anno;


/**
 * 比較対象の配列をプロパティとして保持するクラスに付加する。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class Root extends AnnoSubstitute {
	public Root() {
		
	}

	public Root(String funcName) {
		this.funcName = funcName;
	}
	
	/**
	 * 比較対象のタイトル。
	 */
	public String funcName;
}
