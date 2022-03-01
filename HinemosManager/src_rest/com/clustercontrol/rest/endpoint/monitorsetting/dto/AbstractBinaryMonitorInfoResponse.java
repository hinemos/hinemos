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

public abstract class AbstractBinaryMonitorInfoResponse extends AbstractMonitorResponse {

	protected Boolean collectorFlg;
	protected List<BinaryPatternInfoResponse> binaryPatternInfo = new ArrayList<>();

	public AbstractBinaryMonitorInfoResponse() {
	}

	public Boolean getCollectorFlg() {
		return collectorFlg;
	}

	public List<BinaryPatternInfoResponse> getBinaryPatternInfo() {
		return binaryPatternInfo;
	}

	public void setCollectorFlg(Boolean collectorFlg) {
		this.collectorFlg = collectorFlg;
	}

	public void setBinaryPatternInfo(List<BinaryPatternInfoResponse> binaryPatternInfo) {
		this.binaryPatternInfo = binaryPatternInfo;
	}
}
