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
 * ジョブ実行処理方法の定数クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class ProcessingMethodConstant {
	/** 全てのノードで実行 */
	public static final int TYPE_ALL_NODE = 0;

	/** 正常終了するまでノードを順次リトライ */
	public static final int TYPE_RETRY = 1;
}