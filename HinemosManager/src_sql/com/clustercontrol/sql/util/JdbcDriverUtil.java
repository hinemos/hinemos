/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.sql.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.sql.bean.JdbcDriverInfo;

/**
 * JDBCドライバリソース取得クラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class JdbcDriverUtil {

	private static Log m_log = LogFactory.getLog( JdbcDriverUtil.class );

	private static final String JDBC_DRIVER = "monitor.sql.jdbc.driver";
	private static final String JDBC_DRIVER_NAME = "monitor.sql.jdbc.driver.name.";
	private static final String JDBC_DRIVER_CLASSNAME = "monitor.sql.jdbc.driver.classname.";
	private static final String JDBC_DRIVER_LOGINTIMEOUT = "monitor.sql.jdbc.driver.logintimeout.";
	private static final String JDBC_DRIVER_PROPERTIES = "monitor.sql.jdbc.driver.properties.";


	private HashMap<String, JdbcDriverProperties> jdbcProperties = new HashMap<String, JdbcDriverProperties>();

	public JdbcDriverUtil() {
		m_log.debug("initializing configuration for sql monitoring...");

		//JDBCドライバ数取得
		Integer count = HinemosPropertyUtil.getHinemosPropertyNum(JDBC_DRIVER, Long.valueOf(3)).intValue();
		m_log.debug("use " + count + " jdbc drivers for sql monitoring.");

		for(int i = 1; i <= count.intValue(); i++){
			String name = HinemosPropertyUtil.getHinemosPropertyStr(JDBC_DRIVER_NAME + i, "");
			if ("".equals(name)) {
				continue;
			}
			String classname = HinemosPropertyUtil.getHinemosPropertyStr(JDBC_DRIVER_CLASSNAME + i, "");
			if ("".equals(classname)) {
				continue;
			}
			Long loginTimeout = HinemosPropertyUtil.getHinemosPropertyNum(JDBC_DRIVER_LOGINTIMEOUT + i, null);
			if (loginTimeout == null) {
				continue;
			}
			String properties = HinemosPropertyUtil.getHinemosPropertyStr(JDBC_DRIVER_PROPERTIES + i, "");
			// JDBC_DRIVER_PROPERTIESは空欄でもよい。
			
			m_log.debug("setting jdbc driver " + i + " : " + name + "(classname = " + classname + ", login_timeout = " + loginTimeout + ")");
			jdbcProperties.put(classname, new JdbcDriverProperties(classname, name, loginTimeout.intValue(), properties, i));
		}
	}

	/**
	 * JDBCドライバ名、クラス名を取得
	 *
	 * @return
	 */
	public ArrayList<JdbcDriverInfo> getJdbcDriver() {

		ArrayList<Map.Entry<String, JdbcDriverProperties>> entities = new ArrayList<Map.Entry<String, JdbcDriverProperties>>(jdbcProperties.entrySet());
		ArrayList<JdbcDriverInfo> list = new ArrayList<JdbcDriverInfo>();

		Collections.sort(entities, new Comparator<Map.Entry<String, JdbcDriverProperties>>() {
			@Override
			public int compare(Map.Entry<String, JdbcDriverProperties> o1, Map.Entry<String, JdbcDriverProperties> o2) {
				return o1.getValue().getPriority() - o2.getValue().getPriority();
			}
		});

		for (Map.Entry<String, JdbcDriverProperties> entry : entities) {
			JdbcDriverInfo driver = new JdbcDriverInfo();
			driver.setJdbcDriverName(entry.getValue().getName());
			driver.setJdbcDriverClass(entry.getValue().getClassname());
			list.add(driver);
		}

		return list;
	}

	protected JdbcDriverProperties getJdbcDriverProperty(String classname) {
		return jdbcProperties.get(classname);
	}

}

