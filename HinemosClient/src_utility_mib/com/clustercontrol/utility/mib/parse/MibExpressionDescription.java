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
 * 構文によるDESCRIPTION(TRAP-TYPEと NOTIFICATION-TYPE内)表現クラスです。
 * 
 *  構文との対応は
 *  DESCRIPTION childExpressionString1
 *  
 * @version 6.1.a
 * @since 6.1.a
 */

public class MibExpressionDescription extends MibExpressionAbstract {
	
	/**
	 * コンストラクタ
	 */
	public MibExpressionDescription ( MibToken tokenValue ) {
		super.setToken(tokenValue); 
	}
	
}