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
 * 構文によるOBJECT IDENTIFER表現クラスです。
 * 
 * 構文との対応は
 *  := { [親シンボル名] [ID番号] } --
 *  もしくは
 *  := { [親シンボル名] [連結ID定義](1回以上繰り返し)  [ID番号] } --
 *  もしくは
 *  := { [親シンボル名] [連結ID定義](1回以上繰り返し) } --
 *  もしくは
 *  := { [連結ID定義](1回以上繰り返し)  [ID番号] } --
 *  もしくは
 *  := { [連結ID定義](1回以上繰り返し) } --
 * @version 6.1.a
 * @since 6.1.a
 */

public class MibExpressionObjectIdentifer extends MibExpressionAbstract {
	
	
	/**
	 * 親シンボル名
	 */
	private MibExpressionString parentName ;
	
	/**
	 * 連結ID一覧
	 */
	private ArrayList<MibExpressionChainId> chainIdList ;

	/**
	 * ID番号
	 */
	private MibExpressionNumber idNumber ;
	
	/**
	 * コンストラクタ
	 */
	public MibExpressionObjectIdentifer ( MibToken tokenValue ) {
		super.setToken(tokenValue); 
	}

	/**
	 * 
	 * 親シンボル名取得
	 * 
	 * @return the parentName
	 */
	public MibExpressionString getParentName() {
		return parentName;
	}

	/**
	 * 
	 * 親シンボル名設定
	 * 
	 * @param parentName 親シンボル名
	 */
	public void setParentName(MibExpressionString parentName) {
		super.addChild(parentName);
		this.parentName = parentName;
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
		super.addChild(idNumber);
		this.idNumber = idNumber;
	}
	
	/**
	 * 
	 * 連結ID一覧取得
	 * 
	 * @return 連結ID一覧
	 */
	public ArrayList<MibExpressionChainId> getChainIdList() {
		return chainIdList;
	}
	
	/**
	 * 連結ID追加
	 * 
	 * @param idNumber 連結ID
	 */
	public void addChainId(MibExpressionChainId chainId) {
		if(chainIdList==null){
			chainIdList = new ArrayList<MibExpressionChainId>(); 
		}
		super.addChild(chainId);
		chainIdList.add(chainId);
	}


		
	
}