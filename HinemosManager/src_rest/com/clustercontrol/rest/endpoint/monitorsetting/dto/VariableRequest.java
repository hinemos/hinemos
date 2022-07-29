/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.http.model.Variable;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertIdClassSet(infoClass = Variable.class, idName = "id")
public class VariableRequest {
	private String name;
	private String value;
	private Boolean matchingWithResponseFlg;

	public VariableRequest() {
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Boolean getMatchingWithResponseFlg() {
		return matchingWithResponseFlg;
	}
	public void setMatchingWithResponseFlg(Boolean matchingWithResponseFlg) {
		this.matchingWithResponseFlg = matchingWithResponseFlg;
	}
	@Override
	public String toString() {
		return "Variable [name=" + name + ", value="
				+ value + ", matchingWithResponseFlg=" + matchingWithResponseFlg + "]";
	}

}
