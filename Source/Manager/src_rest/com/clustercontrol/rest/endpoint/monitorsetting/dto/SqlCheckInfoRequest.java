/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.sql.bean.JdbcDriverInfo;
import com.clustercontrol.sql.session.MonitorSqlControllerBean;
import com.clustercontrol.util.MessageConstant;

public class SqlCheckInfoRequest implements RequestDto {
	@RestValidateString(notNull=true, minLen=1)
	@RestItemName(MessageConstant.PASSWORD)
	private String password;
	@RestValidateString(notNull=true, minLen=6)
	@RestItemName(MessageConstant.CONNECTION_URL)
	private String connectionUrl;
	@RestValidateString(notNull=true, minLen=1)
	@RestItemName(MessageConstant.USER_ID)
	private String user;
	@RestValidateString(notNull=true, minLen=6)
	@RestItemName(MessageConstant.JDBC_DRIVER)
	private String jdbcDriver;
	@RestValidateString(notNull=true, minLen=7)
	@RestItemName(MessageConstant.SQL_STRING)
	private String query;
	public SqlCheckInfoRequest() {
	}

	
	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getConnectionUrl() {
		return connectionUrl;
	}


	public void setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}


	public String getUser() {
		return user;
	}


	public void setUser(String user) {
		this.user = user;
	}


	public String getJdbcDriver() {
		return jdbcDriver;
	}


	public void setJdbcDriver(String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
	}


	public String getQuery() {
		return query;
	}


	public void setQuery(String query) {
		this.query = query;
	}


	@Override
	public String toString() {
		return "SqlCheckInfoRequest [password=" + password + ", connectionUrl=" + connectionUrl + ", user=" + user
				+ ", jdbcDriver=" + jdbcDriver + ", query=" + query + "]";
	}


	@Override
	public void correlationCheck() throws InvalidSetting {
		Log m_log = LogFactory.getLog( SqlCheckInfoRequest.class );

		if(!connectionUrl.startsWith("jdbc:")){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CONNECTION_URL_CORRECT_FORMAT.getMessage());
			m_log.info("correlationCheck() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		// query
		String work = query.substring(0, 6);
		if(!work.equalsIgnoreCase("SELECT")){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SELECT_STATEMENT_IN_SQL.getMessage());
			m_log.info("correlationCheck() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// jdbcDriver
		MonitorSqlControllerBean monitorSql = new MonitorSqlControllerBean();
		List<JdbcDriverInfo> jdbcDriverInfoList = monitorSql.getJdbcDriverList();
		List<String> jdbcDriverClassList = new ArrayList<>();
		for (JdbcDriverInfo jdbcDriverInfo : jdbcDriverInfoList) {
			jdbcDriverClassList.add(jdbcDriverInfo.getJdbcDriverClass());
		}
		if (!jdbcDriverClassList.contains(jdbcDriver)) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_CANNOT_FIND_JDBC_DRIVER.getMessage());
			m_log.info("correlationCheck() :" + e.getClass().getSimpleName() + ", " + e.getMessage()); 
			throw e;
		}

	}

}