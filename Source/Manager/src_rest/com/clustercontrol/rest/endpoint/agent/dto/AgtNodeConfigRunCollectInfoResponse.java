/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

import com.clustercontrol.repository.bean.NodeConfigRunCollectInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = NodeConfigRunCollectInfo.class, exclude = { "instructedInfoMapKeys", "instructedInfoMapValues" })
public class AgtNodeConfigRunCollectInfoResponse {

	// ---- from NodeConfigRunCollectInfo

	private Long loadDistributionTime;

	// private HashMap<NodeConfigSetting, Long> instructedInfoMap; // key がオブジェクトなのでそのままはNG
	private List<AgtNodeConfigSettingResponse> instructedInfoMapKeys;
	private List<Long> instructedInfoMapValues;

	public AgtNodeConfigRunCollectInfoResponse() {
	}

	// ---- accessors

	public Long getLoadDistributionTime() {
		return loadDistributionTime;
	}

	public void setLoadDistributionTime(Long loadDistributionTime) {
		this.loadDistributionTime = loadDistributionTime;
	}

	public List<AgtNodeConfigSettingResponse> getInstructedInfoMapKeys() {
		return instructedInfoMapKeys;
	}

	public void setInstructedInfoMapKeys(List<AgtNodeConfigSettingResponse> instructedInfoMapKeys) {
		this.instructedInfoMapKeys = instructedInfoMapKeys;
	}

	public List<Long> getInstructedInfoMapValues() {
		return instructedInfoMapValues;
	}

	public void setInstructedInfoMapValues(List<Long> instructedInfoMapValues) {
		this.instructedInfoMapValues = instructedInfoMapValues;
	}

}
