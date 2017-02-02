/*

Copyright (C) 2013 NTT DATA Corporation

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
 * ジョブ[実行契機]の種別定数クラス<BR>
 * 
 * @version 4.1.0
 * @since 4.1.0
 */
public class JobKickConstant {
	/** スケジュールの場合 */
	public static final int TYPE_SCHEDULE= 0;

	/** ファイルチェックの場合 */
	public static final int TYPE_FILECHECK = 1;

	/** マニュアル実行の場合 */
	public static final int TYPE_MANUAL = 2;
}