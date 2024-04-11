/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import java.util.ArrayList;
import java.util.List;

public class GetStatusListResponse {

	private Integer total = 0;			//合計数（size制限がある場合に取得できた件数）
	private List<StatusInfoResponse> statusList  = new ArrayList<>();//ステータス一覧
	private Integer countAll = 0;		//全レコード数

	public GetStatusListResponse() {
	}

	public Integer getTotal() {
		return total;
	}
	public void setTotal(Integer total) {
		this.total = total;
	}

	public List<StatusInfoResponse> getStatusList() {
		return statusList;
	}
	public void setStatusList(List<StatusInfoResponse> statusList) {
		this.statusList = statusList;
	}
	
	public Integer getCountAll() {
		return countAll;
	}

	public void setCountAll(Integer countAll) {
		this.countAll = countAll;
	}
}
