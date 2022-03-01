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
 * The persistent class for the cc_node_cpu_history_detail database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_node_cpu_history_detail", schema="log")
@Cacheable(false)
public class NodeCpuHistoryDetail extends NodeDeviceHistoryDetail {
	private static final long serialVersionUID = 1L;
	private Integer coreCount = 0;
	private Integer threadCount = 0;
	private Integer clockCount = 0;

	public NodeCpuHistoryDetail() {
		super();
	}

	public NodeCpuHistoryDetail(
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
	
	public NodeCpuHistoryDetail(NodeDeviceHistoryDetailPK pk) {
		super(pk);
	}

	@Column(name="core_count")
	public Integer getCoreCount() {
		return coreCount;
	}

	public void setCoreCount(Integer coreCount) {
		this.coreCount = coreCount;
	}

	@Column(name="thread_count")
	public Integer getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(Integer threadCount) {
		this.threadCount = threadCount;
	}

	@Column(name="clock_count")
	public Integer getClockCount() {
		return clockCount;
	}

	public void setClockCount(Integer clockCount) {
		this.clockCount = clockCount;
	}
}