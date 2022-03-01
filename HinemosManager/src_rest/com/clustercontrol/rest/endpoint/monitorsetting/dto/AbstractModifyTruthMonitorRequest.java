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

public abstract class AbstractModifyTruthMonitorRequest extends AbstractModifyMonitorRequest {

	public AbstractModifyTruthMonitorRequest() {

	}

	protected List<MonitorTruthValueInfoRequest> truthValueInfo = new ArrayList<>();

	public List<MonitorTruthValueInfoRequest> getTruthValueInfo() {
		return truthValueInfo;
	}

	public void setTruthValueInfo(List<MonitorTruthValueInfoRequest> truthValueInfo) {
		this.truthValueInfo = truthValueInfo;
	}

	
}
