/*

Copyright (C) since 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

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
