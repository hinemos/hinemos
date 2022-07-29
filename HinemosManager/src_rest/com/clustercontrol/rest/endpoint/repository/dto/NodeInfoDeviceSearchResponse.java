/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import java.util.ArrayList;

import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

public class NodeInfoDeviceSearchResponse {

	private NodeInfoResponse nodeInfo;
	private NodeInfoResponse newNodeInfo;
	private ArrayList<DeviceSearchMessageInfoResponse> deviceSearchMessageInfo = new ArrayList<>();
	@RestPartiallyTransrateTarget
	private String errorMessage;

	public NodeInfoDeviceSearchResponse() {
	}

	public NodeInfoResponse getNodeInfo() {
		return nodeInfo;
	}

	public void setNodeInfo(NodeInfoResponse nodeInfo) {
		this.nodeInfo = nodeInfo;
	}

	public NodeInfoResponse getNewNodeInfo() {
		return newNodeInfo;
	}

	public void setNewNodeInfo(NodeInfoResponse newNodeInfo) {
		this.newNodeInfo = newNodeInfo;
	}

	public ArrayList<DeviceSearchMessageInfoResponse> getDeviceSearchMessageInfo() {
		return deviceSearchMessageInfo;
	}

	public void setDeviceSearchMessageInfo(ArrayList<DeviceSearchMessageInfoResponse> deviceSearchMessageInfo) {
		this.deviceSearchMessageInfo = deviceSearchMessageInfo;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
