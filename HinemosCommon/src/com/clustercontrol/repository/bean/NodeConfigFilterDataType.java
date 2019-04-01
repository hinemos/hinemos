/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

/**
 * 構成情報検索処理で使用する比較方法を定数として格納するクラス<BR>
 * 
 * @version 6.1.0
 */
public enum NodeConfigFilterDataType {
	
	STRING_ONLYEQUAL(String.class, true),
	STRING(String.class, false),
	STRING_VERSION(String.class, false),
	INTEGER_ONLYEQUAL(Integer.class, true),
	INTEGER(Integer.class, false),
	DATETIME(Long.class, false);

	private final Class<?> clazz;			// データ型
	private final Boolean onlyEqual;	// true:等価比較、不等価比較のみ行う

	private NodeConfigFilterDataType(Class<?> clazz, Boolean onlyEqual) {
		this.clazz = clazz;
		this.onlyEqual = onlyEqual;
	}

	/**
	 * データ型を返す
	 * 
	 * @return データ型
	 */
	public Class<?> clazz() {
		return clazz;
	}

	/**
	 * 等価比較、不等価比較のみ行うか
	 * 
	 * @return true:等価比較、不等価比較のみ行う
	 */
	public Boolean isOnlyEqual() {
		return onlyEqual;
	}
}