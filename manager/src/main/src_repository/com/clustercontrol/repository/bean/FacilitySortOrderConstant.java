/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
