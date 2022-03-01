/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.nodemap.bean;

import java.io.Serializable;

import com.clustercontrol.nodemap.bean.NodeMapAttributeConstant;

/**
 * 表示用の属性値
 * 
 * @since 7.0.0
 */
public class NodeMapAttributes implements Serializable {

	private static final long serialVersionUID = -5388573697624344968L;
	private String facilityId;
	private String nodeName;
	private String description;
	private String ipProtocolNumber;
	private String ipNetworkNumber;
	private String ipNetworkNumberV6;

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIpProtocolNumber() {
		return ipProtocolNumber;
	}

	public void setIpProtocolNumber(String ipProtocolNumber) {
		this.ipProtocolNumber = ipProtocolNumber;
	}

	public String getIpNetworkNumber() {
		return ipNetworkNumber;
	}

	public void setIpNetworkNumber(String ipNetworkNumber) {
		this.ipNetworkNumber = ipNetworkNumber;
	}

	public String getIpNetworkNumberV6() {
		return ipNetworkNumberV6;
	}

	public void setIpNetworkNumberV6(String ipNetworkNumberV6) {
		this.ipNetworkNumberV6 = ipNetworkNumberV6;
	}


	public String getProperty(String key, String defaultValue) {
		switch (key) {
		case NodeMapAttributeConstant.FACILITY_ID:
			return facilityId;
		case NodeMapAttributeConstant.NODENAME:
			return nodeName;
		case NodeMapAttributeConstant.DESCRIPTION:
			return description;
		case NodeMapAttributeConstant.IPPROTOCOL_NUMBER:
			return ipProtocolNumber;
		case NodeMapAttributeConstant.IPNETWORK_NUMBER:
			return ipNetworkNumber;
		case NodeMapAttributeConstant.IPNETWORK_NUMBER_V6:
			return ipNetworkNumberV6;
		default:
			return defaultValue;
		}
	}

	public void setProperty(String key, String value) {
		switch (key) {
		case NodeMapAttributeConstant.FACILITY_ID:
			this.facilityId = value;
			break;
		case NodeMapAttributeConstant.NODENAME:
			this.nodeName = value;
			break;
		case NodeMapAttributeConstant.DESCRIPTION:
			this.description = value;
			break;
		case NodeMapAttributeConstant.IPPROTOCOL_NUMBER:
			this.ipProtocolNumber = value;
			break;
		case NodeMapAttributeConstant.IPNETWORK_NUMBER:
			this.ipNetworkNumber = value;
			break;
		case NodeMapAttributeConstant.IPNETWORK_NUMBER_V6:
			this.ipNetworkNumberV6 = value;
			break;
		}
	}
}
