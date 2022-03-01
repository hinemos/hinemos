/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jmx.session;

import java.util.ArrayList;

import com.clustercontrol.jmx.bean.JmxUrlFormatInfo;
import com.clustercontrol.jmx.util.JmxUrlFormatUtil;

/**
 * Jmx監視を制御するSession Bean
*/
public class MonitorJmxControllerBean {
	public ArrayList<JmxUrlFormatInfo> getJmxFormatList() {
		return new JmxUrlFormatUtil().getJmxFormats();
	}
}
