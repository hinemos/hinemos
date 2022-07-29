/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto;

import java.util.Set;

public class ManagerInfoResponse {

	private int timeZoneOffset;
	private long timeOffsetMillis;
	private Set<String> options;

	public ManagerInfoResponse() {
	}
	
	public ManagerInfoResponse(int timeZoneOffset, long timeOffsetMillis, Set<String> options){
		this.timeZoneOffset = timeZoneOffset;
		this.timeOffsetMillis = timeOffsetMillis;
		this.options = options;
	}

	public int getTimeZoneOffset() {
		return timeZoneOffset;
	}

	public void setTimeZoneOffset(int timeZoneOffset) {
		this.timeZoneOffset = timeZoneOffset;
	}

	public long getTimeOffsetMillis() {
		return timeOffsetMillis;
	}

	public void setTimeOffsetMillis(long timeOffsetMillis) {
		this.timeOffsetMillis = timeOffsetMillis;
	}

	public Set<String> getOptions() {
		return options;
	}

	public void setOptions(Set<String> options) {
		this.options = options;
	}

}
