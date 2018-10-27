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
 * 構文によるNOTIFICATION-TYPE表現クラスです。
 * 
 * 構文との対応は
 * childExpress1 NOTIFICATION-TYPE --（本クラスの中核字句） 
 * OBJECTS { objectsChildExpress1 , objectsChildExpress2 } -- OBJECTSは ExpressionObjects として紐付け
 * DESCRIPTION   descriptionChildExpress2                  -- DESCRIPTIONは ExpressionDescription として紐付け
 * (他の構文は無視)
 * := { objectIdentiferName number }                       -- OID割付は ExpressionobjectIdentiferとして紐付け
 * 
 * @version 6.1.a
 * @since 6.1.a
 */

public class MibExpressionNotificationType extends MibExpressionAbstract {
	
	/**
	 * シンボル名
	 */
	private MibExpressionString symbolName ;

	/**
	 * OBJECTS
	 */
	private MibExpressionObjects expObjects = null;

	/**
	 * DESCRIPCTION
	 */
	private MibExpressionDescription expDescription = null;
	
	/**
	 * OID割付
	 */
	private MibExpressionObjectIdentifer expOid = null;
	
	/**
	 * コンストラクタ
	 */
	public MibExpressionNotificationType ( MibToken tokenValue ) {
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
	 * OBJECTS 取得
	 */
	public MibExpressionObjects getObjects(){
		return this.expObjects ;
		
	}

	/**
	 * DESCRIPCTION 取得
	 */
	public MibExpressionDescription getDescription(){
		return this.expDescription;
		
	}

	/**
	 *  OID割付 取得
	 */
	public MibExpressionObjectIdentifer getExpOid(){
		return this.expOid;
		
	}

	/**
	 * 
	 * シンボル名設定
	 * 
	 * @param symbolName シンボル名
	 */
	public void setSymbolName(MibExpressionString symbolName) {
		//設定済みなら無視
		if( this.symbolName != null ){
			return;
		}
		this.symbolName = symbolName;
		super.addChild(symbolName);
	}


	/**
	 * OBJECTS 設定
	 */
	public void setObjects(MibExpressionObjects objects){
		//設定済みなら無視
		if( this.expObjects != null ){
			return;
		}
		this.expObjects = objects;
		super.addChild(objects);
	}

	/**
	 * DESCRIPCTION 設定
	 */
	public void setDescription(MibExpressionDescription description){
		//設定済みなら無視
		if( this.expDescription != null ){
			return;
		}
		this.expDescription = description;
		super.addChild(description);
	}

	/**
	 * OID割付 設定
	 */
	public void setExpOid(MibExpressionObjectIdentifer oid){
		//設定済みなら無視
		if( this.expOid != null ){
			return;
		}
		this.expOid = oid;
		super.getChildren().add(oid);
	}
	
	
}