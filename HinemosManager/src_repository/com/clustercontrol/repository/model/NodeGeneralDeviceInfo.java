/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node_device", schema="setting")
@Cacheable(true)
public class NodeGeneralDeviceInfo extends NodeDeviceInfo {
	private static final long serialVersionUID = 1L;

	public NodeGeneralDeviceInfo() {
		super();
	}

	public NodeGeneralDeviceInfo(
			String facilityId,
			Integer deviceIndex,
			String deviceType,
			String deviceName) {
		super(facilityId,
				deviceIndex,
				deviceType,
				deviceName);
	}
	
	public NodeGeneralDeviceInfo(NodeDeviceInfoPK pk) {
		super(pk);
	}
	
	@Override
	public NodeGeneralDeviceInfo clone() {
		NodeGeneralDeviceInfo cloneInfo = (NodeGeneralDeviceInfo)super.clone();
		return cloneInfo;
	}
}