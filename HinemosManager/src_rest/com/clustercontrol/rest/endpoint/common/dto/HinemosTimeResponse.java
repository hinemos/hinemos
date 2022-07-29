/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.common.dto;

public class HinemosTimeResponse {

	public HinemosTimeResponse() {
	}

	private Long currentTimeMillis;

	public Long getCurrentTimeMillis() {
		return this.currentTimeMillis;
	}

	public void setCurrentTimeMillis(Long currentTimeMillis) {
		this.currentTimeMillis = currentTimeMillis;
	}

	@Override
	public String toString() {
		return "HinemosTimeResponse [currentTimeMillis=" + currentTimeMillis + "]";
	}
}
