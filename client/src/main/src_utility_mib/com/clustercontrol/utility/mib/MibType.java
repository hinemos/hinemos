/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.mib;

/**
 * MIBのパース処理(Trap関連情報取得)における型の基底クラスです。
 * 
 * @version 6.1.a
 * @since 6.1.a
 */
public abstract class MibType {
	
	/**
	 * 型の名称.
	 */
	private String typeName;

	/**
	 * コンストラクタ
	 * 
	 * @param name 型の名称
	*/
	protected MibType(String typeName) {
		this.typeName = typeName;
	}

	/**
	 * 名称を返します
	 * 
	 * @return 型の名称
	*/
	public String getName() {
		return typeName;
	}

}
