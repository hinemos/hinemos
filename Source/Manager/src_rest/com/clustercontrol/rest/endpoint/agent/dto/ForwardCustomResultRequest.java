/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

public class ForwardCustomResultRequest extends AgentRequestDto {

	private List<AgtCustomResultDTORequest> resultList;

	public ForwardCustomResultRequest() {
	}

	public List<AgtCustomResultDTORequest> getResultList() {
		return resultList;
	}

	public void setResultList(List<AgtCustomResultDTORequest> list) {
		this.resultList = list;
	}

}
