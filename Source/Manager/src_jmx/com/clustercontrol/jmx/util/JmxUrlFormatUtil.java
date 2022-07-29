/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jmx.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.jmx.bean.JmxUrlFormatInfo;

/**
 * JMX監視に使用するURLのフォーマットを取得するクラス
 * 
 * @version 7.0.0
 * @since 7.0.0
 */
public class JmxUrlFormatUtil {
	
	private static Log m_log = LogFactory.getLog(JmxUrlFormatUtil.class);
	
	private HashMap<String, JmxUrlFormatProperties> urlFormatProperties = new HashMap<String, JmxUrlFormatProperties>();
	
	public JmxUrlFormatUtil() {
		m_log.debug("initializing configuration for jmx monitoring...");
		
		Integer count = HinemosPropertyCommon.monitor_jmx_rmi.getIntegerValue();
		m_log.debug("use " + count + " jmx url format for jmx monitoring.");
		
		for (int i = 1; i <= count.intValue(); i++) {
			String name = HinemosPropertyCommon.monitor_jmx_rmi_name_$.getStringValue(Integer.toString(i), "");
			m_log.debug("monitor.jmx.rmi.name." + i + " = " + name);
			if ("".equals(name)) {
				continue;
			}
			String format = HinemosPropertyCommon.monitor_jmx_rmi_format_$.getStringValue(Integer.toString(i), "");
			m_log.debug("monitor.jmx.rmi.format." + i + " = " + format);
			if ("".equals(format)) {
				continue;
			}
			Integer validSecond = HinemosPropertyCommon.monitor_jmx_valid_second_$.getIntegerValue(Integer.toString(i));
			m_log.debug("monitor.jmx.valid.second." + i + " = " + validSecond);
			if (validSecond == null) {
				continue;
			}
			
			m_log.debug("setting jmx url format " + i + " : " + name + " (format = " + format + ")");
			urlFormatProperties.put(name, new JmxUrlFormatProperties(name, format, validSecond, i));
		}
		
	}
	
	public ArrayList<JmxUrlFormatInfo> getJmxFormats() {
		ArrayList<JmxUrlFormatProperties> entities = new ArrayList<>(urlFormatProperties.values());
		Collections.sort(entities, new Comparator<JmxUrlFormatProperties>() {
			@Override
			public int compare(JmxUrlFormatProperties o1, JmxUrlFormatProperties o2) {
				return o1.getPriority() - o2.getPriority();
			}
		});
		
		ArrayList<JmxUrlFormatInfo> infos = new ArrayList<>();
		for (JmxUrlFormatProperties prop : entities) {
			infos.add(new JmxUrlFormatInfo(prop.getName(), prop.getUrlFormat()));
		}
		
		return infos;
	}
	
	public JmxUrlFormatProperties getJmxUrlFormatProperties(String name) {
		return urlFormatProperties.get(name);
	}
	
}

