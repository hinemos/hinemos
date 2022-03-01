/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

import java.util.List;

public class GetCollectDataResponse {
	private Integer total = 0;			//合計数（size制限が無い場合に取得できた件数）
	List<CollectDataResponse> collectDataList;

	public GetCollectDataResponse() {
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public List<CollectDataResponse> getCollectDataList() {
		return collectDataList;
	}

	public void setCollectDataList(List<CollectDataResponse> collectDataList) {
		this.collectDataList = collectDataList;
	}

}
