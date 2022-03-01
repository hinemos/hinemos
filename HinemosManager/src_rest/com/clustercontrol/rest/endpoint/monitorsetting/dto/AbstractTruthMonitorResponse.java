/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTruthMonitorResponse extends AbstractMonitorResponse {

	public AbstractTruthMonitorResponse() {

	}

	protected List<MonitorTruthValueInfoResponse> truthValueInfo = new ArrayList<>();

	public List<MonitorTruthValueInfoResponse> getTruthValueInfo() {
		return truthValueInfo;
	}

	public void setTruthValueInfo(List<MonitorTruthValueInfoResponse> truthValueInfo) {
		this.truthValueInfo = truthValueInfo;
	}

	
}
