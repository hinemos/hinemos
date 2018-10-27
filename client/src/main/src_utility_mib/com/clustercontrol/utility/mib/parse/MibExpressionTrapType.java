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
 * 構文によるTRAP-TYPE表現クラスです。
 * 
* 構文との対応は
 * [シンボル名] TRAP-TYPE --（本クラスの中核字句） 
 * ENTERPRISE   enterpriseExpress                             -- ENTERPRISEは MibExpressionEnterprise として紐付け
 * VARIABLES {  objectsChildExpress1 , objectsChildExpress2 } -- VARIABLESは  MibExpressionVariables として紐付け
 * DESCRIPTION   descriptionExpress                           -- DESCRIPTIONは MibExpressionDescription として紐付け
 * (他の構文は無視)
 * := { objectIdentiferName number }                          -- OID割付は ExpressionobjectIdentiferとして紐付け
  * @version 6.1.a
 * @since 6.1.a
 */

public class MibExpressionTrapType extends MibExpressionAbstract {

	/**
	 * シンボル名
	 */
	private MibExpressionString symbolName ;

	/**
	 * VARIABLES
	 */
	private MibExpressionVariables expVariables = null;

	/**
	 * DESCRIPCTION
	 */
	private MibExpressionDescription expDescription = null;
	
	/**
	 * ENTERPRISE
	 */
	private MibExpressionEnterprise expEnter = null;

	/**
	 * ID番号
	 */
	private MibExpressionNumber expIdNumber = null;

	/**
	 * コンストラクタ
	 */
	public MibExpressionTrapType ( MibToken tokenValue ) {
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
	 * VARIABLES 取得
	 */
	public MibExpressionVariables getVariables(){
		return this.expVariables ;
		
	}

	/**
	 * DESCRIPCTION 取得
	 */
	public MibExpressionDescription getDescription(){
		return this.expDescription;
		
	}

	/**
	 * ENTERPRISE 取得
	 */
	public MibExpressionEnterprise getEnterprise(){
		return this.expEnter;
		
	}

	/**
	 *  OID割付 取得
	 */
	public MibExpressionNumber getIdNumber(){
		return this.expIdNumber;
		
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
		super.getChildren().add(symbolName);
	}


	/**
	 * VARIABLES 設定
	 */
	public void setVariables(MibExpressionVariables variables){
		//設定済みなら無視
		if( this.expVariables != null ){
			return;
		}
		this.expVariables = variables;
		super.getChildren().add(variables);
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
		super.getChildren().add(description);
	}

	/**
	 * ENTERPRISE 設定
	 */
	public void setEnterprise(MibExpressionEnterprise enterprise){
		//設定済みなら無視
		if( this.expEnter != null ){
			return;
		}
		this.expEnter = enterprise;
		super.getChildren().add(enterprise);
	}

	/**
	 * ID番号 設定
	 */
	public void setIdNumber(MibExpressionNumber idNumber){
		//設定済みなら無視
		if( this.expIdNumber != null ){
			return;
		}
		this.expIdNumber = idNumber;
		super.getChildren().add(idNumber);
	}
}