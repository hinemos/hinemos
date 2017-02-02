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
 * ジョブパラメータの定数クラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class JobParamTypeConstant {
	/** システムパラメータ（ジョブ） */
	public static final int TYPE_SYSTEM_JOB = 0;

	/** システムパラメータ（ノード） */
	public static final int TYPE_SYSTEM_NODE = 1;

	/** ユーザパラメータ */
	public static final int TYPE_USER = 2;

	/** ランタイムパラメータ */
	public static final int TYPE_RUNTIME = 3;

}