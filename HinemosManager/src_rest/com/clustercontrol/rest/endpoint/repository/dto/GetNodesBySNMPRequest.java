/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.SnmpVersionEnum;

public class GetNodesBySNMPRequest implements RequestDto {

	private String ipAddress;
	private Integer port;
	private String community;
	private SnmpVersionEnum version;
	private String facilityID;
	private String securityLevel;
	private String user;
	private String authPass;
	private String privPass;
	private String authProtocol;
	private String privProtocol;

	public GetNodesBySNMPRequest() {
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getCommunity() {
		return community;
	}

	public void setCommunity(String community) {
		this.community = community;
	}

	public SnmpVersionEnum getVersion() {
		return version;
	}

	public void setVersion(SnmpVersionEnum version) {
		this.version = version;
	}

	public String getFacilityID() {
		return facilityID;
	}

	public void setFacilityID(String facilityID) {
		this.facilityID = facilityID;
	}

	public String getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(String securityLevel) {
		this.securityLevel = securityLevel;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getAuthPass() {
		return authPass;
	}

	public void setAuthPass(String authPass) {
		this.authPass = authPass;
	}

	public String getPrivPass() {
		return privPass;
	}

	public void setPrivPass(String privPass) {
		this.privPass = privPass;
	}

	public String getAuthProtocol() {
		return authProtocol;
	}

	public void setAuthProtocol(String authProtocol) {
		this.authProtocol = authProtocol;
	}

	public String getPrivProtocol() {
		return privProtocol;
	}

	public void setPrivProtocol(String privProtocol) {
		this.privProtocol = privProtocol;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
