/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.sql.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.sql.bean.JdbcDriverInfo;

/**
 * JDBCドライバリソース取得クラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class JdbcDriverUtil {

	private static Log m_log = LogFactory.getLog( JdbcDriverUtil.class );

	private HashMap<String, JdbcDriverProperties> jdbcProperties = new HashMap<String, JdbcDriverProperties>();

	public JdbcDriverUtil() {
		m_log.debug("initializing configuration for sql monitoring...");

		//JDBCドライバ数取得
		Integer count = HinemosPropertyCommon.monitor_sql_jdbc_driver.getIntegerValue();
		m_log.debug("use " + count + " jdbc drivers for sql monitoring.");

		for(int i = 1; i <= count.intValue(); i++){
			String name = HinemosPropertyCommon.monitor_sql_jdbc_driver_name_$.getStringValue(Integer.toString(i), "");
			if ("".equals(name)) {
				continue;
			}
			String classname = HinemosPropertyCommon.monitor_sql_jdbc_driver_classname_$.getStringValue(Integer.toString(i), "");
			if ("".equals(classname)) {
				continue;
			}
			Long loginTimeout = HinemosPropertyCommon.monitor_sql_jdbc_driver_logintimeout_$.getNumericValue(Integer.toString(i), null);
			if (loginTimeout == null) {
				continue;
			}
			String properties = HinemosPropertyCommon.monitor_sql_jdbc_driver_properties_$.getStringValue(Integer.toString(i), "");
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

