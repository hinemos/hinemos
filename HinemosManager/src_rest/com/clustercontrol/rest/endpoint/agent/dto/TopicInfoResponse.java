/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.hinemosagent.bean.TopicInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = TopicInfo.class)
public class TopicInfoResponse {

	// ---- from TopicInfo
	private AgtRunInstructionInfoResponse runInstructionInfo;
	private Long flag;
	private Integer agentCommand;
	private Long generateDate;

	public TopicInfoResponse() {
	}

	// ---- accessors

	public AgtRunInstructionInfoResponse getRunInstructionInfo() {
		return runInstructionInfo;
	}

	public void setRunInstructionInfo(AgtRunInstructionInfoResponse runInstructionInfo) {
		this.runInstructionInfo = runInstructionInfo;
	}

	public Long getFlag() {
		return flag;
	}

	public void setFlag(Long flag) {
		this.flag = flag;
	}

	public Integer getAgentCommand() {
		return agentCommand;
	}

	public void setAgentCommand(Integer agentCommand) {
		this.agentCommand = agentCommand;
	}

	public Long getGenerateDate() {
		return generateDate;
	}

	public void setGenerateDate(Long generateDate) {
		this.generateDate = generateDate;
	}

}
