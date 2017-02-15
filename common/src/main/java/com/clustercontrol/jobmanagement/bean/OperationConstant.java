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

package com.clustercontrol.jobmanagement.bean;



/**
 * ジョブの操作種別を定数として定義するクラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class OperationConstant {
	/** 開始[即時] */
	public static final int TYPE_START_AT_ONCE = 1;
	/** 開始[中断解除] */
	public static final int TYPE_START_SUSPEND = 3;
	/** 開始[スキップ解除] */
	public static final int TYPE_START_SKIP = 5;
	/** 開始[保留解除] */
	public static final int TYPE_START_WAIT = 7;

	/** 停止[コマンド] */
	public static final int TYPE_STOP_AT_ONCE = 0;
	/** 停止[中断] */
	public static final int TYPE_STOP_SUSPEND = 2;
	/** 停止[スキップ] */
	public static final int TYPE_STOP_SKIP = 4;
	/** 停止[保留] */
	public static final int TYPE_STOP_WAIT = 6;
	/** 停止[状態変更] */
	public static final int TYPE_STOP_MAINTENANCE = 8;
	/** 停止[状態指定] */
	public static final int TYPE_STOP_SET_END_VALUE = 10;
	/** 停止[強制] */
	public static final int TYPE_STOP_FORCE = 11;
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToMessageCode(int type) {
		if (type == TYPE_START_AT_ONCE) {
			return "JOB_START_AT_ONCE";
		} else if (type == TYPE_START_SUSPEND) {
			return "JOB_START_RELEASE_SUSPEND";
		} else if (type == TYPE_START_SKIP) {
			return "JOB_START_RELEASE_SKIP";
		} else if (type == TYPE_START_WAIT) {
			return "JOB_START_RELEASE_RESERVE";
		} else if (type == TYPE_STOP_AT_ONCE) {
			return "JOB_STOP_AT_ONCE";
		} else if (type == TYPE_STOP_SUSPEND) {
			return "JOB_STOP_SUSPEND";
		} else if (type == TYPE_STOP_SKIP) {
			return "JOB_STOP_SKIP";
		} else if (type == TYPE_STOP_WAIT) {
			return "JOB_STOP_RESERVE";
		} else if (type == TYPE_STOP_MAINTENANCE) {
			return "JOB_STOP_MAINTENANCE";
		} else if (type == TYPE_STOP_SET_END_VALUE) {
			return "JOB_STOP_SET_END_VALUE";
		} else if (type == TYPE_STOP_FORCE) {
			return "JOB_STOP_FORCE";
		}
		return "";
	}
}
