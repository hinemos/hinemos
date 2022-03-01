/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.List;

public class GetCloudScopesResponse {
	
	private Integer total = 0;			//合計数（size制限が無い場合に取得できた件数）
	List<CloudScopeInfoResponse> cloudScopeInfoList;

	public GetCloudScopesResponse() {
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public List<CloudScopeInfoResponse> getCloudScopeInfoList() {
		return cloudScopeInfoList;
	}

	public void setCloudScopeInfoList(List<CloudScopeInfoResponse> cloudScopeInfoList) {
		this.cloudScopeInfoList = cloudScopeInfoList;
	}


}
