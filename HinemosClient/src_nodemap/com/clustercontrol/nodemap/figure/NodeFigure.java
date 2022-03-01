/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.figure;

import org.openapitools.client.model.FacilityElementResponse;


/**
 * アイコン(ノード)画像のクラス
 * @since 1.0.0
 */
public class NodeFigure extends FacilityFigure {
	public NodeFigure(String managerName, FacilityElementResponse element){
		super(managerName, element);
	}

	@Override
	public void draw(String filename) throws Exception {
		super.draw(filename);
		// 属性値をツールチップに設定
		addTooltip(com.clustercontrol.nodemap.messages.Messages.getString("tooltip.facilityid") +
				" : " + getFacilityElementProperty("FacilityId"));
		addTooltip(com.clustercontrol.nodemap.messages.Messages.getString("tooltip.description") +
				" : " + getFacilityElementProperty("Description"));
		addTooltip(com.clustercontrol.nodemap.messages.Messages.getString("tooltip.ipv4") +
				" : " + getFacilityElementProperty("IpNetworkNumber"));
		addTooltip(com.clustercontrol.nodemap.messages.Messages.getString("tooltip.ipv6") +
				" : " + getFacilityElementProperty("IpNetworkNumberV6"));
		addTooltip(com.clustercontrol.nodemap.messages.Messages.getString("tooltip.nodename") +
				" : " + getFacilityElementProperty("NodeName"));
	}
}
