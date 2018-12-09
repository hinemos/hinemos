/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * The persistent class for the cc_cfg_node_network_interface database table.
 * 
 */
@Entity
@Table(name="cc_cfg_node_network_interface", schema="setting")
@Cacheable(true)
public class NodeNetworkInterfaceInfo extends NodeDeviceInfo {
	private static final long serialVersionUID = 1L;
	private String deviceNicIpAddress			= "";
	private String deviceNicMacAddress			= "";

	public NodeNetworkInterfaceInfo() {
	}

	public NodeNetworkInterfaceInfo(
			String facilityId,
			Integer deviceIndex,
			String deviceType,
			String deviceName) {
		super(facilityId,
				deviceIndex,
				deviceType,
				deviceName);
	}

	public NodeNetworkInterfaceInfo(NodeDeviceInfoPK pk) {
		super(pk);
	}
	
	@Column(name="device_nic_ip_address")
	public String getNicIpAddress() {
		return this.deviceNicIpAddress;
	}

	public void setNicIpAddress(String deviceNicIpAddress) {
		this.deviceNicIpAddress = deviceNicIpAddress;
	}


	@Column(name="device_nic_mac_address")
	public String getNicMacAddress() {
		return this.deviceNicMacAddress;
	}

	public void setNicMacAddress(String deviceNicMacAddress) {
		this.deviceNicMacAddress = deviceNicMacAddress;
	}
	
	@Override
	public void relateToNodeEntity(NodeInfo nodeEntity) {
		this.setNodeEntity(nodeEntity);
		if (nodeEntity != null) {
			List<NodeNetworkInterfaceInfo> list = nodeEntity.getNodeNetworkInterfaceInfo();
			if (list == null) {
				list = new ArrayList<NodeNetworkInterfaceInfo>();
			} else {
				for(NodeNetworkInterfaceInfo entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			nodeEntity.setNodeNetworkInterfaceInfo(list);
		}
	}
	
	@Override
	public NodeNetworkInterfaceInfo clone() {
		NodeNetworkInterfaceInfo cloneInfo = (NodeNetworkInterfaceInfo)super.clone();
		return cloneInfo;
	}
}