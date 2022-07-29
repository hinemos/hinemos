/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;
import java.util.Arrays;

import jakarta.persistence.*;

/**
 * The primary key class for the cc_cfg_node_netstat database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@Embeddable
public class NodeNetstatInfoPK implements Serializable, Cloneable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String facilityId = "";
	private String protocol = "";
	private String localIpAddress = "";
	private String localPort = "";
	private String foreignIpAddress = "";
	private String foreignPort = "";
	private String processName = "";
	private Integer pid = -1;


	public NodeNetstatInfoPK() {
	}

	public NodeNetstatInfoPK(
			String facilityId, 
			String protocol, 
			String localIpAddress, 
			String localPort,
			String foreignIpAddress,
			String foreignPort,
			String processName,
			Integer pid) {
		this.setFacilityId(facilityId);
		this.setProtocol(protocol);
		this.setLocalIpAddress(localIpAddress);
		this.setLocalPort(localPort);
		this.setForeignIpAddress(foreignIpAddress);
		this.setForeignPort(foreignPort);
		this.setProcessName(processName);
		this.setPid(pid);
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="protocol")
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Column(name="local_ip_address")
	public String getLocalIpAddress() {
		return localIpAddress;
	}
	public void setLocalIpAddress(String localIpAddress) {
		this.localIpAddress = localIpAddress;
	}

	@Column(name="local_port")
	public String getLocalPort() {
		return localPort;
	}
	public void setLocalPort(String localPort) {
		this.localPort = localPort;
	}

	@Column(name="foreign_ip_address")
	public String getForeignIpAddress() {
		return foreignIpAddress;
	}
	public void setForeignIpAddress(String foreignIpAddress) {
		this.foreignIpAddress = foreignIpAddress;
	}

	@Column(name="foreign_port")
	public String getForeignPort() {
		return foreignPort;
	}
	public void setForeignPort(String foreignPort) {
		this.foreignPort = foreignPort;
	}

	@Column(name="process_name")
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}

	@Column(name="pid")
	public Integer getPid() {
		return pid;
	}
	public void setPid(Integer pid) {
		this.pid = pid;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof NodeNetstatInfoPK)) {
			return false;
		}
		NodeNetstatInfoPK castOther = (NodeNetstatInfoPK)other;
		return
				this.facilityId.equals(castOther.facilityId)
				&& this.protocol.equals(castOther.protocol)
				&& this.localIpAddress.equals(castOther.localIpAddress)
				&& this.localPort.equals(castOther.localPort)
				&& this.foreignIpAddress.equals(castOther.foreignIpAddress)
				&& this.foreignPort.equals(castOther.foreignPort)
				&& this.processName.equals(castOther.processName)
				&& this.pid.equals(castOther.pid)	;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.facilityId.hashCode();
		hash = hash * prime + this.protocol.hashCode();
		hash = hash * prime + this.localIpAddress.hashCode();
		hash = hash * prime + this.localPort.hashCode();
		hash = hash * prime + this.foreignIpAddress.hashCode();
		hash = hash * prime + this.foreignPort.hashCode();
		hash = hash * prime + this.processName.hashCode();
		hash = hash * prime + this.pid.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"facilityId",
				"protocol",
				"localIpAddress",
				"localPort",
				"foreignIpAddress",
				"foreignPort",
				"processName",
				"pid"
		};
		String[] values = {
				this.facilityId,
				this.protocol,
				this.localIpAddress,
				this.localPort,
				this.foreignIpAddress,
				this.foreignPort,
				this.processName,
				this.pid.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
	
	@Override
	public NodeNetstatInfoPK clone() {
		try {
			NodeNetstatInfoPK cloneInfo = (NodeNetstatInfoPK)super.clone();
			cloneInfo.facilityId = this.facilityId;
			cloneInfo.protocol = this.protocol;
			cloneInfo.localIpAddress = this.localIpAddress;
			cloneInfo.localPort = this.localPort;
			cloneInfo.foreignIpAddress = this.foreignIpAddress;
			cloneInfo.foreignPort = this.foreignPort;
			cloneInfo.processName = this.processName;
			cloneInfo.pid = this.pid;
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
}