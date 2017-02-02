/*

Copyright (C) since 2010 NTT DATA Corporation

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
 * 
 * JobOperationPropertyの定数部分を切り出したもの。
 *
 */
public class JobOperationConstant {
	/** セッションID */
	public static final String SESSION = "session";
	/** 所属するジョブユニットのジョブID */
	public static final String JOB_UNIT = "jobUnit";
	/** ジョブID */
	public static final String JOB = "job";
	/** ファシリティID */
	public static final String FACILITY = "facility";
	/** 制御 */
	public static final String CONTROL = "control";
	/** 終了状態 */
	public static final String END_STATUS = "endStatus";
	/** 終了値 */
	public static final String END_VALUE = "endValue";

}
