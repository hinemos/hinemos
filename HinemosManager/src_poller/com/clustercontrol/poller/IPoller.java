/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.poller;

import java.util.Set;

import com.clustercontrol.poller.util.DataTable;
import com.clustercontrol.repository.model.NodeInfo;

/**
 * Poller Common Interface
 * 
 *
 */
public interface IPoller {

	/**
	 * @param nodeInfo ノード情報(ノード変数にパラメタ含む)
	 * @param target ポーリングターゲットのリスト
	 * @param option オプション用データ
	 */
	public DataTable polling(NodeInfo nodeInfo, Set<String> targets, Object option);
}
