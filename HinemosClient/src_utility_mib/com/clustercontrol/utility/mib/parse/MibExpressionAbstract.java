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
 * 構文による各種表現(構文木)の基底クラスです。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */

public abstract class MibExpressionAbstract {
	/**
	 * 表現を代表する字句 
	 */
	private MibToken tokenValue;

	/**
	 * この表現に紐付く他の表現 
	 */
	private ArrayList<MibExpressionAbstract> expressionChildren ;
	
	/**
	 * 代表となる字句の設定
	 * @param mibToken 字句インスタンス
	 */
	public void setToken(MibToken mibToken) {
		this.tokenValue = mibToken; 
	}

	/**
	 * 代表となる字句の取得
	 * @return 字句インスタンス
	 */
	public MibToken getToken() {
		return tokenValue; 
	}

	/**
	 * 紐づく表現の一覧取得
	 * @return 紐づく一覧
	 */
	public ArrayList<MibExpressionAbstract> getChildren() {
		if( expressionChildren == null ){
			expressionChildren = new ArrayList<MibExpressionAbstract>();
		}
		return expressionChildren;
	}

	/**
	 * 紐づく表現の追加
	 * @param childExpression 紐づける表現
	 */
	public void addChild(MibExpressionAbstract childExpression) {
		if(expressionChildren==null){
			expressionChildren = new ArrayList<MibExpressionAbstract>();
		}
		expressionChildren.add(childExpression);
	}
	
}