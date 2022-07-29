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

public class GetFilterNodeListRequest implements RequestDto {

	private String administrator;
	private String contact;
	private String ipAddressV4;
	private String ipAddressV6;
	private NodeOsInfoRequest nodeOsInfo;
	private String facilityId;
	private String facilityName;
	private String description;

	public GetFilterNodeListRequest() {
	}

	public String getAdministrator() {
		return administrator;
	}

	public void setAdministrator(String administrator) {
		this.administrator = administrator;
	}


	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getIpAddressV4() {
		return ipAddressV4;
	}

	public void setIpAddressV4(String ipAddressV4) {
		this.ipAddressV4 = ipAddressV4;
	}

	public String getIpAddressV6() {
		return ipAddressV6;
	}

	public void setIpAddressV6(String ipAddressV6) {
		this.ipAddressV6 = ipAddressV6;
	}


	public NodeOsInfoRequest getNodeOsInfo() {
		return nodeOsInfo;
	}

	public void setNodeOsInfo(NodeOsInfoRequest nodeOsInfo) {
		this.nodeOsInfo = nodeOsInfo;
	}


	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getFacilityName() {
		return facilityName;
	}

	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
