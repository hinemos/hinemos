/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.sql.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class JdbcDriverInfo implements Serializable {

	private static final long serialVersionUID = 2802260307520621507L;
	private String jdbcDriverName = "";
	private String jdbcDriverClass = "";

	public JdbcDriverInfo() {}

	public JdbcDriverInfo(String jdbcDriverName, String jdbcDriverClass) {
		super();
		this.jdbcDriverName = jdbcDriverName;
		this.jdbcDriverClass = jdbcDriverClass;
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

}
