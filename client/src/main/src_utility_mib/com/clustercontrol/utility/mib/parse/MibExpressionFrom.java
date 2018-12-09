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
 * 構文によるFROM表現(IMPORT 内)クラスです。
 * 
 * 構文との対応は
 *    childExpressionString2 , childExpressionString3 ... FROM childExpressionString1 -- FROMの直後がMIB名、以前のMIB内の象徴名となります。
 * となっています 
 * @version 6.1.a
 * @since 6.1.a
 */

public class MibExpressionFrom extends MibExpressionAbstract {
	
	/**
	 * MIB名
	 */
	private MibExpressionString expMibName ;
	
	/**
	 * 象徴名のList
	 */
	private ArrayList<MibExpressionString> expSymbolNameList = new ArrayList<MibExpressionString>();
	
	/**
	 * コンストラクタ
	 */
	public MibExpressionFrom ( MibToken tokenValue ) {
		super.setToken(tokenValue); 
	}
	
	/**
	 * MIB名設定
	 */
	public void setMibName(MibExpressionString mibName){
		if(this.expMibName != null ){
			return;
		}
		this.expMibName = mibName;
		super.addChild(mibName);
	}

	/**
	 * 象徴名追加
	 */
	public void addSymbolName(MibExpressionString symbolName){
		super.addChild(symbolName);
		this.expSymbolNameList.add(symbolName);
	}
	
	/**
	 * MIB名取得
	 */
	public MibExpressionString getMibName(){
		return this.expMibName;
	} 

	/**
	 * 象徴名一覧取得
	 */
	public ArrayList<MibExpressionString> getSymbolNameList(){
		return this.expSymbolNameList;
	} 

	/**
	 * Importの要否判定
	 */
	public boolean isNeedImport(){
		MibExpressionFrom target = this;
		//インポート対象となる名称に値向け名称（先頭が小文字）があるかチェック
		for(MibExpressionAbstract expFromChild :  target.getSymbolNameList() ){
			if( expFromChild instanceof MibExpressionString){
				//値向け名称があればインポートする
				if(Character.isLowerCase(expFromChild.getToken().getTokenString().charAt(0))){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Importの必要な名称の取得
	 */
	public String[] getNeedImportNames(){
		MibExpressionFrom target = this;
		//インポート対象となる名称に値向け名称（先頭が小文字）があるかチェック
		ArrayList<String> list = new ArrayList<String>();
		for(MibExpressionAbstract expFromChild :  target.getSymbolNameList() ){
			if( expFromChild instanceof MibExpressionString){
				//値向け名称をリストに登録
				if(Character.isLowerCase(expFromChild.getToken().getTokenString().charAt(0))){
					list.add(expFromChild.getToken().getTokenString());
				}
			}
		}
		if( list.size() == 0 ){
			return null;
		}
		return list.toArray(new String[0]);
	}
	

}