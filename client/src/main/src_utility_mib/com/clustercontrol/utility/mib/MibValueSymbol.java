/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib;

import com.clustercontrol.utility.mib.MibSymbol;
import com.clustercontrol.utility.mib.MibType;
import com.clustercontrol.utility.mib.MibValue;

/**
 * MIBのパース処理(Trap関連情報取得)における値割付の象徴クラスです。
 * 
 * @version6.1.a
 * @since 6.1.a
 */
public class MibValueSymbol extends MibSymbol{
	
	/**
	 * 設定値
	 */
	private MibValue presetMibValue;

	/**
	 * 値に対して定義された型
	 */
	private MibType presetMibType;

	/**
	 * 値割付の象徴インスタンスを作成
	 *
	 * @param presetMibValue 定義された値
	 * @param presetMibType	 値に対して定義された型
	 */
	public MibValueSymbol(String symbolName, MibValue presetMibValue ,MibType presetMibType) {
		super(symbolName);
		this.presetMibValue = presetMibValue;
		this.presetMibType = presetMibType;
	}
	
	/**
	 * 定義された値を返します。
	 *
	 * @return 
	 */
	public MibValue getValue() {
		return presetMibValue;
	}
	
	/**
	 * 値に対して定義された型
	 *
	 * @return 
	 */
	public MibType getType() {
		return presetMibType;
	}

}
