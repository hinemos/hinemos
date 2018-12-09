/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib.parse;

import java.util.ArrayList;

/**
 * MIBのパース処理(Trap関連情報取得)における
 * 構文によるIMPORT表現クラスです。
 * 
 * 構文との対応は
 * IMPORT
 *   importFrom1Child1 , importFrom1Child2    FROM importFrom1 --MibExpressionFromとして紐付け
 *   importFrom2Child1 , importFrom2Child2 .. FROM importFrom2 --MibExpressionFromとして紐付け 
 *   ..
 *  ;
 * となっています。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */

public class MibExpressionImport extends MibExpressionAbstract {
	
	ArrayList<MibExpressionFrom> expFrom = new ArrayList<MibExpressionFrom>(); 

	/**
	 * コンストラクタ
	 */
	public MibExpressionImport ( MibToken tokenValue ) {
		super.setToken(tokenValue); 
	}
	
	/**
	 * 紐づく表現の追加
	 * @param childExpression 紐づける表現
	 */
	@Override
	public void addChild(MibExpressionAbstract childExpression) {
		super.addChild(childExpression);
		if(childExpression instanceof MibExpressionFrom ){
			expFrom.add((MibExpressionFrom)childExpression);
		}
	}
	
}