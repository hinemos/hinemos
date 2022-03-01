/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.repository;

import org.openapitools.client.model.AgtNodeInfoRequest;

public class NodeConfigResult {

	/** 取得日時 */
	private long aquireDate = 0;

	/** 構成情報 */
	private AgtNodeInfoRequest nodeInfo = null;

	public NodeConfigResult(long aquireDate, AgtNodeInfoRequest nodeInfo) {
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
	public AgtNodeInfoRequest getNodeInfo() {
		return nodeInfo;
	}

	/** 構成情報 */
	public void setNodeInfo(AgtNodeInfoRequest nodeInfo) {
		this.nodeInfo = nodeInfo;
	}

}
