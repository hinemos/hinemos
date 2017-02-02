/*

Copyright (C) since 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.repository.bean;

/**
 * スコープビューでの表示ソート順位を定義したクラス<BR>
 */
public class FacilitySortOrderConstant {

	// 表示ソート順位
	/** ノードのデフォルトの表示ソート順位 */
	public static int DEFAULT_SORT_ORDER_NODE = 100;

	/** スコープのデフォルトの表示ソート順位 */
	public static int DEFAULT_SORT_ORDER_SCOPE = 200;

	/** ロールスコープのデフォルトの表示ソート順位 */
	public static int DEFAULT_SORT_ORDER_ROLE_SCOPE = 300;

	/** 組み込みロールスコープのデフォルトの表示ソート順位 */
	public static int DEFAULT_SORT_ORDER_BUILDIN_ROLE_SCOPE = 400;

}
