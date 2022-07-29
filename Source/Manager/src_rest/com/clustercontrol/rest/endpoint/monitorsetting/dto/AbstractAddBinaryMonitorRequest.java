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

public abstract class AbstractAddBinaryMonitorRequest extends AbstractAddMonitorRequest {

	public AbstractAddBinaryMonitorRequest() {

	}

	protected Boolean collectorFlg;
	protected List<BinaryPatternInfoRequest> binaryPatternInfo = new ArrayList<>();

	public Boolean getCollectorFlg() {
		return collectorFlg;
	}

	public void setCollectorFlg(Boolean collectorFlg) {
		this.collectorFlg = collectorFlg;
	}

	public List<BinaryPatternInfoRequest> getBinaryPatternInfo() {
		return binaryPatternInfo;
	}

	public void setBinaryPatternInfo(List<BinaryPatternInfoRequest> binaryPatternInfo) {
		this.binaryPatternInfo = binaryPatternInfo;
	}

}
