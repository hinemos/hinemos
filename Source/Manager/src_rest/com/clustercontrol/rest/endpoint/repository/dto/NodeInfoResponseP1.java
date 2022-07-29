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
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.SnmpAuthProtocolEnum;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.SnmpPrivProtocolEnum;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.SnmpSecurityLevelEnum;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.SnmpVersionEnum;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.WbemProtocolEnum;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.WinrmProtocolEnum;

public class NodeInfoResponseP1 {

	private Boolean autoDeviceSearch;
	private String administrator;
	private String cloudService;
	private String cloudScope;
	private String cloudResourceType;
	private String cloudResourceId;
	private String cloudResourceName;
	private String cloudLocation;
	private String contact;
	private String hardwareType;
	private String ipAddressV4;
	private String ipAddressV6;
	@RestBeanConvertEnum
	private IpaddressVersionEnum ipAddressVersion;
	private String ipmiIpAddress;
	private String ipmiLevel;
	private Integer ipmiPort;
	private String ipmiProtocol;
	private Integer ipmiRetries;
	private Integer ipmiTimeout;
	private String ipmiUser;
	private String ipmiUserPassword;
	private Integer jobPriority;
	private Integer jobMultiplicity;
	private String nodeName;
	private String platformFamily;
	private String snmpCommunity;
	private Integer snmpPort;
	private Integer snmpRetryCount;
	private Integer snmpTimeout;
	@RestBeanConvertEnum
	private SnmpVersionEnum snmpVersion;
	@RestBeanConvertEnum
	private SnmpSecurityLevelEnum snmpSecurityLevel;
	private String snmpUser;
	private String snmpAuthPassword;
	private String snmpPrivPassword;
	@RestBeanConvertEnum
	private SnmpAuthProtocolEnum snmpAuthProtocol;
	@RestBeanConvertEnum
	private SnmpPrivProtocolEnum snmpPrivProtocol;
	private String sshUser;
	private String sshUserPassword;
	private String sshPrivateKeyFilepath;
	private String sshPrivateKeyPassphrase;
	private Integer sshPort;
	private Integer sshTimeout;
	private String subPlatformFamily;
	private Integer wbemPort;
	@RestBeanConvertEnum
	private WbemProtocolEnum wbemProtocol;
	private Integer wbemRetryCount;
	private Integer wbemTimeout;
	private String wbemUser;
	private String wbemUserPassword;
	private Integer winrmPort;
	@RestBeanConvertEnum
	private WinrmProtocolEnum winrmProtocol;
	private Integer winrmRetries;
	private Integer winrmTimeout;
	private String winrmUser;
	private String winrmUserPassword;
	private String winrmVersion;
	private String rpaLogDir;
	private String rpaManagementToolType;
	private String rpaResourceId;
	private String rpaUser;
	private String rpaExecEnvId;
	private Integer agentAwakePort;

	private String ownerRoleId;

	private String facilityId;
	private String facilityName;
	private String description;
	private Integer displaySortOrder;
	private String iconImage;
	private Boolean valid;
	private String createUserId;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String createDatetime;
	private String modifyUserId;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String modifyDatetime;
	private Boolean builtInFlg;
	private Boolean notReferFlg;

	public NodeInfoResponseP1() {
	}

	public Boolean getAutoDeviceSearch() {
		return autoDeviceSearch;
	}

	public void setAutoDeviceSearch(Boolean autoDeviceSearch) {
		this.autoDeviceSearch = autoDeviceSearch;
	}

	public String getAdministrator() {
		return administrator;
	}

	public void setAdministrator(String administrator) {
		this.administrator = administrator;
	}

	public String getCloudService() {
		return cloudService;
	}

	public void setCloudService(String cloudService) {
		this.cloudService = cloudService;
	}

	public String getCloudScope() {
		return cloudScope;
	}

	public void setCloudScope(String cloudScope) {
		this.cloudScope = cloudScope;
	}

	public String getCloudResourceType() {
		return cloudResourceType;
	}

	public void setCloudResourceType(String cloudResourceType) {
		this.cloudResourceType = cloudResourceType;
	}

	public String getCloudResourceId() {
		return cloudResourceId;
	}

	public void setCloudResourceId(String cloudResourceId) {
		this.cloudResourceId = cloudResourceId;
	}

	public String getCloudResourceName() {
		return cloudResourceName;
	}

	public void setCloudResourceName(String cloudResourceName) {
		this.cloudResourceName = cloudResourceName;
	}

	public String getCloudLocation() {
		return cloudLocation;
	}

	public void setCloudLocation(String cloudLocation) {
		this.cloudLocation = cloudLocation;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getHardwareType() {
		return hardwareType;
	}

	public void setHardwareType(String hardwareType) {
		this.hardwareType = hardwareType;
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

	public IpaddressVersionEnum getIpAddressVersion() {
		return ipAddressVersion;
	}

	public void setIpAddressVersion(IpaddressVersionEnum ipAddressVersion) {
		this.ipAddressVersion = ipAddressVersion;
	}

	public String getIpmiIpAddress() {
		return ipmiIpAddress;
	}

	public void setIpmiIpAddress(String ipmiIpAddress) {
		this.ipmiIpAddress = ipmiIpAddress;
	}

	public String getIpmiLevel() {
		return ipmiLevel;
	}

	public void setIpmiLevel(String ipmiLevel) {
		this.ipmiLevel = ipmiLevel;
	}

	public Integer getIpmiPort() {
		return ipmiPort;
	}

	public void setIpmiPort(Integer ipmiPort) {
		this.ipmiPort = ipmiPort;
	}

	public String getIpmiProtocol() {
		return ipmiProtocol;
	}

	public void setIpmiProtocol(String ipmiProtocol) {
		this.ipmiProtocol = ipmiProtocol;
	}

	public Integer getIpmiRetries() {
		return ipmiRetries;
	}

	public void setIpmiRetries(Integer ipmiRetries) {
		this.ipmiRetries = ipmiRetries;
	}

	public Integer getIpmiTimeout() {
		return ipmiTimeout;
	}

	public void setIpmiTimeout(Integer ipmiTimeout) {
		this.ipmiTimeout = ipmiTimeout;
	}

	public String getIpmiUser() {
		return ipmiUser;
	}

	public void setIpmiUser(String ipmiUser) {
		this.ipmiUser = ipmiUser;
	}

	public String getIpmiUserPassword() {
		return ipmiUserPassword;
	}

	public void setIpmiUserPassword(String ipmiUserPassword) {
		this.ipmiUserPassword = ipmiUserPassword;
	}

	public Integer getJobPriority() {
		return jobPriority;
	}

	public void setJobPriority(Integer jobPriority) {
		this.jobPriority = jobPriority;
	}

	public Integer getJobMultiplicity() {
		return jobMultiplicity;
	}

	public void setJobMultiplicity(Integer jobMultiplicity) {
		this.jobMultiplicity = jobMultiplicity;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getPlatformFamily() {
		return platformFamily;
	}

	public void setPlatformFamily(String platformFamily) {
		this.platformFamily = platformFamily;
	}

	public String getSnmpCommunity() {
		return snmpCommunity;
	}

	public void setSnmpCommunity(String snmpCommunity) {
		this.snmpCommunity = snmpCommunity;
	}

	public Integer getSnmpPort() {
		return snmpPort;
	}

	public void setSnmpPort(Integer snmpPort) {
		this.snmpPort = snmpPort;
	}

	public Integer getSnmpRetryCount() {
		return snmpRetryCount;
	}

	public void setSnmpRetryCount(Integer snmpRetryCount) {
		this.snmpRetryCount = snmpRetryCount;
	}

	public Integer getSnmpTimeout() {
		return snmpTimeout;
	}

	public void setSnmpTimeout(Integer snmpTimeout) {
		this.snmpTimeout = snmpTimeout;
	}

	public SnmpVersionEnum getSnmpVersion() {
		return snmpVersion;
	}

	public void setSnmpVersion(SnmpVersionEnum snmpVersion) {
		this.snmpVersion = snmpVersion;
	}

	public SnmpSecurityLevelEnum getSnmpSecurityLevel() {
		return snmpSecurityLevel;
	}

	public void setSnmpSecurityLevel(SnmpSecurityLevelEnum snmpSecurityLevel) {
		this.snmpSecurityLevel = snmpSecurityLevel;
	}

	public String getSnmpUser() {
		return snmpUser;
	}

	public void setSnmpUser(String snmpUser) {
		this.snmpUser = snmpUser;
	}

	public String getSnmpAuthPassword() {
		return snmpAuthPassword;
	}

	public void setSnmpAuthPassword(String snmpAuthPassword) {
		this.snmpAuthPassword = snmpAuthPassword;
	}

	public String getSnmpPrivPassword() {
		return snmpPrivPassword;
	}

	public void setSnmpPrivPassword(String snmpPrivPassword) {
		this.snmpPrivPassword = snmpPrivPassword;
	}

	public SnmpAuthProtocolEnum getSnmpAuthProtocol() {
		return snmpAuthProtocol;
	}

	public void setSnmpAuthProtocol(SnmpAuthProtocolEnum snmpAuthProtocol) {
		this.snmpAuthProtocol = snmpAuthProtocol;
	}

	public SnmpPrivProtocolEnum getSnmpPrivProtocol() {
		return snmpPrivProtocol;
	}

	public void setSnmpPrivProtocol(SnmpPrivProtocolEnum snmpPrivProtocol) {
		this.snmpPrivProtocol = snmpPrivProtocol;
	}

	public String getSshUser() {
		return sshUser;
	}

	public void setSshUser(String sshUser) {
		this.sshUser = sshUser;
	}

	public String getSshUserPassword() {
		return sshUserPassword;
	}

	public void setSshUserPassword(String sshUserPassword) {
		this.sshUserPassword = sshUserPassword;
	}

	public String getSshPrivateKeyFilepath() {
		return sshPrivateKeyFilepath;
	}

	public void setSshPrivateKeyFilepath(String sshPrivateKeyFilepath) {
		this.sshPrivateKeyFilepath = sshPrivateKeyFilepath;
	}

	public String getSshPrivateKeyPassphrase() {
		return sshPrivateKeyPassphrase;
	}

	public void setSshPrivateKeyPassphrase(String sshPrivateKeyPassphrase) {
		this.sshPrivateKeyPassphrase = sshPrivateKeyPassphrase;
	}

	public Integer getSshPort() {
		return sshPort;
	}

	public void setSshPort(Integer sshPort) {
		this.sshPort = sshPort;
	}

	public Integer getSshTimeout() {
		return sshTimeout;
	}

	public void setSshTimeout(Integer sshTimeout) {
		this.sshTimeout = sshTimeout;
	}

	public String getSubPlatformFamily() {
		return subPlatformFamily;
	}

	public void setSubPlatformFamily(String subPlatformFamily) {
		this.subPlatformFamily = subPlatformFamily;
	}

	public Integer getWbemPort() {
		return wbemPort;
	}

	public void setWbemPort(Integer wbemPort) {
		this.wbemPort = wbemPort;
	}

	public WbemProtocolEnum getWbemProtocol() {
		return wbemProtocol;
	}

	public void setWbemProtocol(WbemProtocolEnum wbemProtocol) {
		this.wbemProtocol = wbemProtocol;
	}

	public Integer getWbemRetryCount() {
		return wbemRetryCount;
	}

	public void setWbemRetryCount(Integer wbemRetryCount) {
		this.wbemRetryCount = wbemRetryCount;
	}

	public Integer getWbemTimeout() {
		return wbemTimeout;
	}

	public void setWbemTimeout(Integer wbemTimeout) {
		this.wbemTimeout = wbemTimeout;
	}

	public String getWbemUser() {
		return wbemUser;
	}

	public void setWbemUser(String wbemUser) {
		this.wbemUser = wbemUser;
	}

	public String getWbemUserPassword() {
		return wbemUserPassword;
	}

	public void setWbemUserPassword(String wbemUserPassword) {
		this.wbemUserPassword = wbemUserPassword;
	}

	public Integer getWinrmPort() {
		return winrmPort;
	}

	public void setWinrmPort(Integer winrmPort) {
		this.winrmPort = winrmPort;
	}

	public WinrmProtocolEnum getWinrmProtocol() {
		return winrmProtocol;
	}

	public void setWinrmProtocol(WinrmProtocolEnum winrmProtocol) {
		this.winrmProtocol = winrmProtocol;
	}

	public Integer getWinrmRetries() {
		return winrmRetries;
	}

	public void setWinrmRetries(Integer winrmRetries) {
		this.winrmRetries = winrmRetries;
	}

	public Integer getWinrmTimeout() {
		return winrmTimeout;
	}

	public void setWinrmTimeout(Integer winrmTimeout) {
		this.winrmTimeout = winrmTimeout;
	}

	public String getWinrmUser() {
		return winrmUser;
	}

	public void setWinrmUser(String winrmUser) {
		this.winrmUser = winrmUser;
	}

	public String getWinrmUserPassword() {
		return winrmUserPassword;
	}

	public void setWinrmUserPassword(String winrmUserPassword) {
		this.winrmUserPassword = winrmUserPassword;
	}

	public String getWinrmVersion() {
		return winrmVersion;
	}

	public void setWinrmVersion(String winrmVersion) {
		this.winrmVersion = winrmVersion;
	}

	public String getRpaLogDir() {
		return rpaLogDir;
	}

	public void setRpaLogDir(String rpaLogDir) {
		this.rpaLogDir = rpaLogDir;
	}

	public String getRpaManagementToolType() {
		return rpaManagementToolType;
	}

	public void setRpaManagementToolType(String rpaManagementToolType) {
		this.rpaManagementToolType = rpaManagementToolType;
	}

	public String getRpaResourceId() {
		return rpaResourceId;
	}

	public void setRpaResourceId(String rpaResourceId) {
		this.rpaResourceId = rpaResourceId;
	}

	public String getRpaUser() {
		return rpaUser;
	}

	public void setRpaUser(String rpaUser) {
		this.rpaUser = rpaUser;
	}

	public String getRpaExecEnvId() {
		return rpaExecEnvId;
	}

	public void setRpaExecEnvId(String rpaExecEnvId) {
		this.rpaExecEnvId = rpaExecEnvId;
	}

	public Integer getAgentAwakePort() {
		return agentAwakePort;
	}

	public void setAgentAwakePort(Integer agentAwakePort) {
		this.agentAwakePort = agentAwakePort;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
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

	public Integer getDisplaySortOrder() {
		return displaySortOrder;
	}

	public void setDisplaySortOrder(Integer displaySortOrder) {
		this.displaySortOrder = displaySortOrder;
	}

	public String getIconImage() {
		return iconImage;
	}

	public void setIconImage(String iconImage) {
		this.iconImage = iconImage;
	}

	public Boolean getValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	public String getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}

	public String getCreateDatetime() {
		return createDatetime;
	}

	public void setCreateDatetime(String createDatetime) {
		this.createDatetime = createDatetime;
	}

	public String getModifyUserId() {
		return modifyUserId;
	}

	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
	}

	public String getModifyDatetime() {
		return modifyDatetime;
	}

	public void setModifyDatetime(String modifyDatetime) {
		this.modifyDatetime = modifyDatetime;
	}

	public Boolean getBuiltInFlg() {
		return builtInFlg;
	}

	public void setBuiltInFlg(Boolean builtInFlg) {
		this.builtInFlg = builtInFlg;
	}

	public Boolean getNotReferFlg() {
		return notReferFlg;
	}

	public void setNotReferFlg(Boolean notReferFlg) {
		this.notReferFlg = notReferFlg;
	}
}
