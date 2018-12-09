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
 * 構文による連結ID定義表現クラスです。
 * 
 * 構文との対応は
 *  := { [親シンボル名] [連結ID定義](1回以上繰り返し)  [ID番号] } --
 *  における [連結ID定義] となる 名称（ID番号) 表現です。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */

public class MibExpressionChainId extends MibExpressionAbstract {
	
	/**
	 * ID番号
	 */
	private MibExpressionNumber idNumber ;
	
	/**
	 * コンストラクタ
	 */
	public MibExpressionChainId ( MibToken tokenValue ) {
		super.setToken(tokenValue); 
	}

	/**
	 * 
	 * ID番号取得
	 * 
	 * @return ID番号
	 */
	public MibExpressionNumber getIdNumber() {
		return idNumber;
	}

	/**
	 * ID番号設定
	 * 
	 * @param idNumber ID番号
	 */
	public void setIdNumber(MibExpressionNumber idNumber) {
		this.idNumber = idNumber;
	}

}