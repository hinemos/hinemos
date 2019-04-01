/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.repository;

import com.clustercontrol.ws.repository.NodeInfo;

public class NodeConfigResult {

	/** 取得日時 */
	private long aquireDate = 0;

	/** 構成情報 */
	private NodeInfo nodeInfo = null;

	public NodeConfigResult(long aquireDate, NodeInfo nodeInfo) {
		this.aquireDate = aquireDate;
		this.nodeInfo = nodeInfo;
	}

	/** 取得日時 */
	public long getAquireDate() {
		return aquireDate;
	}

	/** 取得日時 */
	public void setAquireDate(long aquireDate) {
		this.aquireDate = aquireDate;
	}

	/** 構成情報 */
	public NodeInfo getNodeInfo() {
		return nodeInfo;
	}

	/** 構成情報 */
	public void setNodeInfo(NodeInfo nodeInfo) {
		this.nodeInfo = nodeInfo;
	}

}
