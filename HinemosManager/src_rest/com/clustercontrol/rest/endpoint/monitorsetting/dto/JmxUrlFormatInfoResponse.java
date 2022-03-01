/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class JmxUrlFormatInfoResponse {
	
	private String jmxUrlFormatName = "";
	private String jmxUrlFormat = "";
	
	public JmxUrlFormatInfoResponse() {
	}
	
	public String getJmxUrlFormatName() {
		return jmxUrlFormatName;
	}
	
	public String getJmxUrlFormat() {
		return jmxUrlFormat;
	}
	
	@Override
	public String toString() {
		return "JmxFormatResponse [jmxUrlFormatName=" + jmxUrlFormatName + ", jmxUrlFormat=" + jmxUrlFormat + "]";
	}
}
