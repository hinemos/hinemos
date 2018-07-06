/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform.util.reporting;

import java.io.File;

import com.clustercontrol.reporting.ReportUtil;

/**
 * 
 * for WIN
 *
 */
public class PropertyUtil {

	public static String getLogReportPath() {
		String reportPath = ReportUtil.getProperty("reporting.output.path", 
				System.getProperty("hinemos.manager.data.dir") + File.separator + "report");

		return reportPath;
	}
}
