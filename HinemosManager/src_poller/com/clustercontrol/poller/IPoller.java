/*

Copyright (C) 2013 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

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
