/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jmx.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JMX監視のURLフォーマットの定義情報を格納するクラス
 */
public class JmxUrlFormatProperties {
	
	private static Log log = LogFactory.getLog(JmxUrlFormatProperties.class);
	
	private String name = "";
	private String urlFormat = "";
	private Integer validSecond = null;
	private int priority = 0;
	
	protected JmxUrlFormatProperties(String name, String urlFormat, Integer validSecond, int priority) {
		this.name = name;
		this.urlFormat = urlFormat;
		this.validSecond = validSecond;
		this.priority = priority;
	}
	
	public String getName() {
		return name;
	}
	
	public String getUrlFormat() {
		return urlFormat;
	}
	
	public Integer getValidSecond() {
		return validSecond;
	}
	
	public int getPriority() {
		return priority;
	}
	
}

