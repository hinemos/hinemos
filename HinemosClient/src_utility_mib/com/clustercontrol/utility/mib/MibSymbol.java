/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib;

/**
 * MIBのパース処理(Trap関連情報取得)における各種象徴の基底クラスです。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */
public abstract class MibSymbol {
	
	/**
	 * 象徴の名称.
	 */
	private String symbolName;

	/**
	 * コンストラクタ
	 * 
	 * @param name 象徴の名称
	*/
	protected MibSymbol(String symbolName) {
		this.symbolName = symbolName;
	}

	/**
	 * 名称を返します
	 * 
	 * @return 象徴の名称
	*/
	public String getName() {
		return symbolName;
	}

}
