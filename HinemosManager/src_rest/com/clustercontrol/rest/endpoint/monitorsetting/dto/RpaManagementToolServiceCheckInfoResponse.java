/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class RpaManagementToolServiceCheckInfoResponse {
	/**
	 * コネクションタイムアウト
	 */
	private Integer connectTimeout;
	/**
	 * リクエストタイムアウト
	 */
	private Integer requestTimeout;
	
	/**
	 * コネクションタイムアウト
	 */
	public Integer getConnectTimeout() {
		return this.connectTimeout;
	}

	public void setConnectTimeout(Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	
	/**
	 * リクエストタイムアウト
	 */
	public Integer getRequestTimeout() {
		return requestTimeout;
	}

	public void setRequestTimeout(Integer requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	@Override
	public String toString() {
		return "RpaManagementToolServiceCheckInfoResponse [connectTimeout=" + connectTimeout + ", requestTimeout=" + requestTimeout + "]";
	}

}
