/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.repository.model.NodeVariableInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.dto.RequestDto;

@RestBeanConvertIdClassSet(infoClass = NodeVariableInfo.class, idName = "id")
public class NodeVariableInfoRequest implements RequestDto {

	private String nodeVariableName;
	private String nodeVariableValue;

	public NodeVariableInfoRequest() {
	}

	public String getNodeVariableName() {
		return nodeVariableName;
	}

	public void setNodeVariableName(String nodeVariableName) {
		this.nodeVariableName = nodeVariableName;
	}

	public String getNodeVariableValue() {
		return nodeVariableValue;
	}

	public void setNodeVariableValue(String nodeVariableValue) {
		this.nodeVariableValue = nodeVariableValue;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
