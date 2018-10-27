/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib;

/**
 * MIBのパース処理(Trap関連情報取得)における値の基底クラスです。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */
public abstract class MibValue {
	
	/**
	 * 値の名称.
	 */
	private String name;

	/**
	 * コンストラクタ
	 * 
	 * @param name 値の名称
	*/
	protected MibValue(String name) {
		this.name = name;
	}

	/**
	 * 名称を返します
	 * 
	 * @return 値の名称
	*/
	public String getName() {
		return name;
	}

}
