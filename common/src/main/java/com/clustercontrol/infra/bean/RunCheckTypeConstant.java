/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.bean;

/**
 * 環境構築機能のチェックと実行のタイプを定数として格納するクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class RunCheckTypeConstant {
	/** 実行 */
	public static final int TYPE_RUN = 1;
	/** チェック */
	public static final int TYPE_CHECK = 2;
	/** プレチェック */
	public static final int TYPE_PRECHECK = 3;
}
