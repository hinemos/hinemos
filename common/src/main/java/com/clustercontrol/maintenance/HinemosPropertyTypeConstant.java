/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

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
}