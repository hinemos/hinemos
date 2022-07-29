/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import java.util.List;

public class GetMonitorBeanListResponse {
	
	private Integer total = 0;			//合計数（size制限が無い場合に取得できた件数）

	List<MonitorInfoBeanResponse> MonitorInfoList ; //監視設定情報一覧

	public GetMonitorBeanListResponse() {
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public List<MonitorInfoBeanResponse> getMonitorInfoList() {
		return MonitorInfoList;
	}

	public void setMonitorInfoList(List<MonitorInfoBeanResponse> monitorInfoList) {
		MonitorInfoList = monitorInfoList;
	}
	

}
