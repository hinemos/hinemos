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
 * The persistent class for the cc_node_disk_history_detail database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_node_disk_history_detail", schema="log")
@Cacheable(false)
public class NodeDiskHistoryDetail extends NodeDeviceHistoryDetail {
	private static final long serialVersionUID = 1L;
	private Integer deviceDiskRpm = 0;

	public NodeDiskHistoryDetail() {
		super();
	}

	public NodeDiskHistoryDetail(
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

	public NodeDiskHistoryDetail(NodeDeviceHistoryDetailPK pk) {
		super(pk);
	}

	@Column(name="device_disk_rpm")
	public Integer getDiskRpm() {
		return deviceDiskRpm;
	}
	public void setDiskRpm(Integer deviceDiskRpm) {
		this.deviceDiskRpm = deviceDiskRpm;
	}
}