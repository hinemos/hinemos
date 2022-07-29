/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.rest.endpoint.repository.dto.enumtype.AssociationEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;

public class MapAssociationInfoResponse {

	private String source;
	private String target;
	@RestBeanConvertEnum
	private AssociationEnum type;

	public MapAssociationInfoResponse() {
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public AssociationEnum getType() {
		return type;
	}

	public void setType(AssociationEnum type) {
		this.type = type;
	}
}
