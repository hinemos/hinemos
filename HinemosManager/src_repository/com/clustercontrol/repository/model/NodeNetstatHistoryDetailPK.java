/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.clustercontrol.util.HinemosTime;

/**
 * The primary key class for the cc_node_netstat_history_detail database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@Embeddable
public class NodeNetstatHistoryDetailPK implements Serializable {

	// default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String facilityId = "";
	private String protocol = "";
	private String localIpAddress = "";
	private String localPort = "";
	private String foreignIpAddress = "";
	private String foreignPort = "";
	private String processName = "";
	private Integer pid = -1;
	private Long regDate = HinemosTime.currentTimeMillis();

	/**
	 * デフォルト値生成コンストラクタ.
	 */
	public NodeNetstatHistoryDetailPK() {
	}

	/**
	 * キー指定コンストラクタ.
	 */
	public NodeNetstatHistoryDetailPK(
			String facilityId, 
			String protocol, 
			String localIpAddress, 
			String localPort, 
			String foreignIpAddress, 
			String foreignPort,
			String processName,
			Integer pid,
			Long regDate) {
		this.setFacilityId(facilityId);
		this.setProtocol(protocol);
		this.setLocalIpAddress(localIpAddress);
		this.setLocalPort(localPort);
		this.setForeignIpAddress(foreignIpAddress);
		this.setForeignPort(foreignPort);
		this.setProcessName(processName);
		this.setPid(pid);
		this.setRegDate(regDate);
	}

	@Column(name = "facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name = "protocol")
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Column(name = "local_ip_address")
	public String getLocalIpAddress() {
		return localIpAddress;
	}
	public void setLocalIpAddress(String localIpAddress) {
		this.localIpAddress = localIpAddress;
	}

	@Column(name = "local_port")
	public String getLocalPort() {
		return localPort;
	}
	public void setLocalPort(String localPort) {
		this.localPort = localPort;
	}

	@Column(name = "foreign_ip_address")
	public String getForeignIpAddress() {
		return foreignIpAddress;
	}
	public void setForeignIpAddress(String foreignIpAddress) {
		this.foreignIpAddress = foreignIpAddress;
	}

	@Column(name = "foreign_port")
	public String getForeignPort() {
		return foreignPort;
	}
	public void setForeignPort(String foreignPort) {
		this.foreignPort = foreignPort;
	}

	@Column(name = "process_name")
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}

	@Column(name = "pid")
	public Integer getPid() {
		return pid;
	}
	public void setPid(Integer pid) {
		this.pid = pid;
	}

	@Column(name = "reg_date")
	public Long getRegDate() {
		return this.regDate;
	}
	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}
}
