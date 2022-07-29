/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.repository.dto;

import java.util.List;

public class GetNodeListResponse {
	
	private Integer total = 0;			//合計数（size制限が無い場合に取得できた件数）
	List<NodeInfoResponseP2> nodeInfoList ;
	
	public GetNodeListResponse() {
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public List<NodeInfoResponseP2> getNodeInfoList() {
		return nodeInfoList;
	}

	public void setNodeInfoList(List<NodeInfoResponseP2> nodeInfoList) {
		this.nodeInfoList = nodeInfoList;
	}

}
