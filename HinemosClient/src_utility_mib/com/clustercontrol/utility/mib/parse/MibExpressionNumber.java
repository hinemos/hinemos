/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib.parse;

/**
 * MIBのパース処理(Trap関連情報取得)における
 * 構文による数値表現クラスです。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */

public class MibExpressionNumber extends MibExpressionAbstract {
	
	/*
	 * コンストラクタ
	 */
	public MibExpressionNumber ( MibToken tokenValue ) {
		super.setToken(tokenValue); 
	}
}