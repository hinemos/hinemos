/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.IpaddressVersionEnum;

public class NodeInfoResponseP2 {

	private String facilityId;
	private String facilityName;
	@RestBeanConvertEnum
	private IpaddressVersionEnum ipAddressVersion;
	private String ipAddressV4;
	private String ipAddressV6;
	private String platformFamily;
	private String description;
	private String ownerRoleId;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String createDatetime;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String modifyDatetime;

	public NodeInfoResponseP2() {
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

	public IpaddressVersionEnum getIpAddressVersion() {
		return ipAddressVersion;
	}

	public void setIpAddressVersion(IpaddressVersionEnum ipAddressVersion) {
		this.ipAddressVersion = ipAddressVersion;
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

	public String getPlatformFamily() {
		return platformFamily;
	}

	public void setPlatformFamily(String platformFamily) {
		this.platformFamily = platformFamily;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public String getCreateDatetime() {
		return createDatetime;
	}

	public void setCreateDatetime(String createDatetime) {
		this.createDatetime = createDatetime;
	}

	public String getModifyDatetime() {
		return modifyDatetime;
	}

	public void setModifyDatetime(String modifyDatetime) {
		this.modifyDatetime = modifyDatetime;
	}

}
