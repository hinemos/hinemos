/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlType;

/**
 * The persistent class for the cc_node_filesystem_history_detail database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_node_filesystem_history_detail", schema="log")
@Cacheable(false)
public class NodeFilesystemHistoryDetail extends NodeDeviceHistoryDetail {
	private static final long serialVersionUID = 1L;
	private String deviceFilesystemType = "";

	public NodeFilesystemHistoryDetail() {
		super();
	}

	public NodeFilesystemHistoryDetail(
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

	public NodeFilesystemHistoryDetail(NodeDeviceHistoryDetailPK pk) {
		super(pk);
	}

	@Column(name="device_filesystem_type")
	public String getFilesystemType() {
		return deviceFilesystemType;
	}
	public void setFilesystemType(String deviceFilesystemType) {
		this.deviceFilesystemType = deviceFilesystemType;
	}
}