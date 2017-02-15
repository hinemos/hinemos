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

package com.clustercontrol.monitor.run.bean;

/**
 * 監視種別の定義を定数として格納するクラス<BR>
 * 
 * @version 4.0.0
 * @since 2.1.0
 */
public class MonitorTypeConstant {
	/** 真偽値（種別）。 */
	public static final int TYPE_TRUTH = 0;

	/** 数値（種別）。 */
	public static final int TYPE_NUMERIC = 1;

	/** 文字列（種別）。 */
	public static final int TYPE_STRING = 2;

	/** トラップ（種別）。 */
	public static final int TYPE_TRAP = 3;

	/** シナリオ（種別）。 */
	public static final int TYPE_SCENARIO = 4;	
}