/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import javax.xml.bind.annotation.XmlType;

/**
 * The persistent class for the cc_node_network_interface_history_detail database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_node_network_interface_history_detail", schema="log")
@Cacheable(false)
public class NodeNetworkInterfaceHistoryDetail extends NodeDeviceHistoryDetail {
	private static final long serialVersionUID = 1L;
	private String deviceNicIpAddress = "";
	private String deviceNicMacAddress = "";

	public NodeNetworkInterfaceHistoryDetail() {
		super();
	}

	public NodeNetworkInterfaceHistoryDetail(
			String facilityId,
			Integer deviceIndex,
			String deviceType,
			String deviceName,
			Long regDate) {
		super(facilityId,
				deviceIndex,
				deviceType,
				deviceName,
				regDate);
	}

	public NodeNetworkInterfaceHistoryDetail(NodeDeviceHistoryDetailPK pk) {
		super(pk);
	}

	@Column(name="device_nic_ip_address")
	public String getNicIpAddress() {
		return deviceNicIpAddress;
	}
	public void setNicIpAddress(String deviceNicIpAddress) {
		this.deviceNicIpAddress = deviceNicIpAddress;
	}

	@Column(name="device_nic_mac_address")
	public String getNicMacAddress() {
		return deviceNicMacAddress;
	}
	public void setNicMacAddress(String deviceNicMacAddress) {
		this.deviceNicMacAddress = deviceNicMacAddress;
	}
}