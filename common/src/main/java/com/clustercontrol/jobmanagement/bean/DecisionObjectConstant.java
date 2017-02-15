/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.bean;

/**
 * 判定条件の定数クラス<BR>
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public class DecisionObjectConstant {
	/** =(数値) */
	public static final int EQUAL_NUMERIC = 0;

	/** !=(数値) */
	public static final int NOT_EQUAL_NUMERIC = 1;

	/** >(数値) */
	public static final int GREATER_THAN = 2;

	/** >=(数値) */
	public static final int GREATER_THAN_OR_EQUAL_TO = 3;

	/** <(数値) */
	public static final int LESS_THAN = 4;

	/** <=(数値) */
	public static final int LESS_THAN_OR_EQUAL_TO = 5;

	/** =(文字列) */
	public static final int EQUAL_STRING = 6;

	/** !=(文字列) */
	public static final int NOT_EQUAL_STRING = 7;
}