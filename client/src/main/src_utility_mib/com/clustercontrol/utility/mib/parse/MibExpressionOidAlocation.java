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
 * 構文によるOid割付表現クラスです。
 * 
 * 適用対象となる値型とMACRO定義は以下
 *   OBJECT IDENTIFER
 *   OBJECT-TYPE
 *   OBJECT-IDENTITY
 *   MODULE-COMPLIANCE
 *   NOTIFICATION-GROUP
 *   MODULE-IDENTITY
 *   AGENT-CAPABILITIES
 * 
 * 構文との対応は
 * [シンボル名] [マクロ定義名] ～(途中は無視)～  := { [親シンボル名] [ID番号] } -- { } 内 はMibExpressionObjectIdentiferとして紐付け
 * 
 * @version 6.1.a
 * @since 6.1.a
 */

public class MibExpressionOidAlocation extends MibExpressionAbstract {
	
	/**
	 * シンボル名
	 */
	private MibExpressionString symbolName ;
	
	/**
	 * OID表現
	 */
	private MibExpressionObjectIdentifer expOid ;
	
	/**
	 * 
	 * コンストラクタ
	 * 
	 * @param tokenValue 代表語句（マクロ定義名）
	 */
	public MibExpressionOidAlocation ( MibToken tokenValue ) {
		super.setToken(tokenValue); 
	}
	
	/**
	 * 
	 * シンボル名取得
	 * 
	 * @return シンボル名
	 */
	public MibExpressionString getSymbolName() {
		return symbolName;
	}

	/**
	 * 
	 * シンボル名設定
	 * 
	 * @param symbolName シンボル名
	 */
	public void setSymbolName(MibExpressionString symbolName) {
		this.symbolName = symbolName;
	}

	/**
	 * 
	 * OID表現取得
	 * 
	 * @return  OID表現
	 */
	public MibExpressionObjectIdentifer getExpOid() {
		return this.expOid;
	}

	/**
	 * 
	 * OID表現設定
	 * 
	 * @param symbolName  OID表現
	 */
	public void setExpOid(MibExpressionObjectIdentifer expOid) {
		this.expOid = expOid;
	}

}