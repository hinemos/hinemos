/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance;


/**
 * Hinemosプロパティ種別の定義を定数として格納するクラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class HinemosPropertyTypeConstant {
	/** 文字列（種別）。 */
	public static final int TYPE_STRING = 1;
	
	/** 数値（種別）。 */
	public static final int TYPE_NUMERIC = 2;
	
	/** 真偽値（種別）。 */
	public static final int TYPE_TRUTH = 3;

	/** 真偽値:true */
	public static final String BOOL_TRUE = "true";
	
	/** 真偽値:false */
	public static final String BOOL_FALSE = "false";

	private HinemosPropertyTypeConstant() {
		throw new IllegalStateException("ConstClass");
	}
}