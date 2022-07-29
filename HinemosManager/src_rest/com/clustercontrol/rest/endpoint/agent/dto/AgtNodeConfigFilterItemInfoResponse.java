/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.repository.bean.NodeConfigFilterItemInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = NodeConfigFilterItemInfo.class)
public class AgtNodeConfigFilterItemInfoResponse {

	// --- from NodeConfigFilterItemInfo
	private String itemName;
	private String method;
	// private Object itemValue;  // Object 型をそのままでは転送できないので、とりあえず除外して、必要になったら対応方法を考える

	public AgtNodeConfigFilterItemInfoResponse() {
	}

	// ---- accessors

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

}
