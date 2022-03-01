/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.v1;

import java.util.List;

import com.clustercontrol.hub.bean.StringSampleTag;
import com.clustercontrol.sdml.factory.ISdmlOption;
import com.clustercontrol.sdml.v1.constant.SdmlCollectStringTag;
import com.clustercontrol.sdml.v1.util.MonitoringLogUtil;

public class SdmlV1Option implements ISdmlOption {
	// バージョン
	public static final String VERSION = "1.0";

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public List<StringSampleTag> extractTagsFromMonitoringLog(String message) {
		return MonitoringLogUtil.parse(message);
	}

	@Override
	public List<String> getSampleTagList(String sdmlMonitorTypeId) {
		return SdmlCollectStringTag.getSampleTagList(sdmlMonitorTypeId);
	}
}
