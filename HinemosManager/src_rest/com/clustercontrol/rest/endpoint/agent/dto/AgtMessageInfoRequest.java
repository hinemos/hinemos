/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.systemlog.service.MessageInfo;

@RestBeanConvertAssertion(to = MessageInfo.class)
public class AgtMessageInfoRequest extends AgentRequestDto {

	// ---- from MessageInfo
	private String hostName;
	private Long generationDate;
	private String message;

	public AgtMessageInfoRequest() {
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String m_hostName) {
		this.hostName = m_hostName;
	}

	public Long getGenerationDate() {
		return generationDate;
	}

	public void setGenerationDate(Long m_generationDate) {
		this.generationDate = m_generationDate;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String m_message) {
		this.message = m_message;
	}

}
