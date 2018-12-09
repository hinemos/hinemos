/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference.anno;

/**
 * 配列を比較するための情報を保持する。
 * 配列のプロパティに設定する。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class ArrayId extends AnnoSubstitute {
	/**
	 * 配列の各要素の一意性を担保するプロパティの指定方法の種別。
	 * 
	 * 
	 *
	 */
	public enum IdType {
		/**
		 * 目印の "PrimaryKey" が指定されたプロパティ
		 */
		pk,
		/**
		 * 単一のプロパティ
		 */
		prop,
		/**
		 * 複数のプロパティ
		 */
		props
	}
	
	public ArrayId() {
		idType = IdType.pk;
		terminate = false;
	}
	
	public ArrayId(IdType idType, String propName, boolean terminate) {
		this.idType = idType;
		this.propName = propName;
		this.terminate = terminate;
	}

	/**
	 * 配列の各要素の識別方法。
	 */
	public IdType idType;
	
	/**
	 * "idType" に "prop" が指定されている場合に有効。
	 */
	public String propName;
	
	/**
	 * "idType" に "props" が指定されている場合に有効。
	 */
	public String[] props;

	/**
	 * 配列内の各要素の比較をしない場合に true
	 */
	public boolean terminate;
}