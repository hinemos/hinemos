/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform.util.reporting;

import com.clustercontrol.reporting.ReportUtil;

/**
 * 
 * for RHEL
 *
 */
public class PropertyUtil {

	// /opt/hinemos/var/report
	public static String getLogReportPath() {
		String basePath = ReportUtil.getProperty("reporting.output.path", System.getProperty("hinemos.manager.home.dir", "/opt/hinemos") + "/var/report");

		return basePath;
	}
}
