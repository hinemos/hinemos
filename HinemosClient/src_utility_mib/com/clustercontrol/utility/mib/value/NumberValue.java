/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib.value;

import com.clustercontrol.utility.mib.MibValue;

/**
 * MIBのパース処理(Trap関連情報取得)における 値割付の数値クラスです。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */
public class NumberValue extends MibValue{
	
	/**
	 * 割付された数値
	 */
	private Number numVal;
	
	/**
	 * 値割付の数値クラスを返します
	 *
	 * @param numVal 数値
	 */
	public NumberValue(Number numVal) {
		super("number");
		this.numVal = numVal;
	}
	
	/**
	 * 文字型に変換された数値を返します
	 *
	 * @return 割付された数値の文字列
	 */
	public String toString() {
		return numVal.toString();
	}

}
