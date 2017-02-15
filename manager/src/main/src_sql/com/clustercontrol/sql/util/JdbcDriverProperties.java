/*

Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.sql.util;

import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JDBCの定義情報(jdbc.properties)を格納するクラス
 */
public class JdbcDriverProperties {

	private static Log log = LogFactory.getLog(JdbcDriverProperties.class);

	private String classname = "";

	private String name = "";
	private Integer loginTimeout = null;
	private Properties props = new Properties();

	private int priority = 0;

	private static final String _propertiesSepareteToken = "&";
	private static final String _propertiesDefineToken = "=";

	protected JdbcDriverProperties(String classname, String name, Integer loginTimeout, String props, int priority) {
		this.classname = classname;
		this.name = name;
		this.loginTimeout = loginTimeout;
		this.props = parseProperties(props);
		this.priority = priority;
	}

	/**
	 * properties(param1=xxx&param2=yyy&param3=zzz...)を解析して、Propertiesインスタンスを生成する。
	 * @param props propertiesに定義された文字列
	 * @return
	 */
	private Properties parseProperties(String props) {
		Properties ret = new Properties();

		StringTokenizer separeteToken = new StringTokenizer(props, _propertiesSepareteToken);
		StringTokenizer defineToken = null;

		while (separeteToken.hasMoreTokens()) {
			String property = separeteToken.nextToken();
			log.debug(this.classname + " : parsing property. (" + property + ")");
			defineToken = new StringTokenizer(property, _propertiesDefineToken);
			if (defineToken.countTokens() == 2) {
				String key = defineToken.nextToken();
				String value = defineToken.nextToken();
				ret.setProperty(key, value);
				log.debug(this.classname + " : setting property. (key = " + key + ", value = " + value + ")");
			} else {
				log.info(this.classname + " : skipped, because of invalid jdbc parameter. (" + property + ")");
			}
		}

		return ret;
	}

	protected String getClassname() {
		return classname;
	}

	protected String getName() {
		return name;
	}

	protected boolean isLoginTimeoutEnable() {
		return loginTimeout == null ? false : true;
	}

	protected int getLoginTimeout() {
		return loginTimeout.intValue();
	}

	protected int getPriority() {
		return priority;
	}

	/**
	 * 定義されたProperties情報のコピーを返す
	 * @return Propertiesインスタンス
	 */
	protected Properties getProperties() {
		Properties ret = new Properties();

		for (Object key : props.keySet()) {
			ret.setProperty((String)key, props.getProperty((String)key));
		}

		return ret;
	}

}
