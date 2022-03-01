/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform.util.reporting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.reporting.ReportUtil;

/**
 * 
 * for RHEL
 *
 */
public class PropertyUtil {
	private static Log m_log = LogFactory.getLog(PropertyUtil.class);
	// /opt/hinemos/var/report
	public static String getLogReportPath() {
		String basePath = ReportUtil.getProperty("reporting.output.path", System.getProperty("hinemos.manager.home.dir", "/opt/hinemos") + "/var/report");

		return basePath;
	}
}
