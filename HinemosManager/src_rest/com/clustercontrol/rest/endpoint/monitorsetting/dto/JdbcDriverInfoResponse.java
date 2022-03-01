/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class JdbcDriverInfoResponse {

	private String jdbcDriverName = "";
	private String jdbcDriverClass = "";

	public JdbcDriverInfoResponse() {
	}

	public String getJdbcDriverName() {
		return jdbcDriverName;
	}
	public void setJdbcDriverName(String jdbcDriverName) {
		this.jdbcDriverName = jdbcDriverName;
	}
	public String getJdbcDriverClass() {
		return jdbcDriverClass;
	}
	public void setJdbcDriverClass(String jdbcDriverClass) {
		this.jdbcDriverClass = jdbcDriverClass;
	}
	@Override
	public String toString() {
		return "JdbcDriverInfoListResponse [jdbcDriverName=" + jdbcDriverName + ", jdbcDriverClass=" + jdbcDriverClass
				+ "]";
	}

}
