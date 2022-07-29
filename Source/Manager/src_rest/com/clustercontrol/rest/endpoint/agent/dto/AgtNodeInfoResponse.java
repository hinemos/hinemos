/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = NodeInfo.class)
public class AgtNodeInfoResponse {

	// ---- from ObjectPrivilegeTargetInfo
	// private String objectId; // XMLTransient
	private String ownerRoleId;
	private Boolean uncheckFlg;

	// ---- from FacilityInfo
	private String facilityId;
	private String facilityName;
	private Integer facilityType;
	private String description;
	private Integer displaySortOrder;
	private String iconImage;
	private Boolean valid;
	private String createUserId;
	private Long createDatetime;
	private String modifyUserId;
	private Long modifyDatetime;
	private Boolean builtInFlg;
	private Boolean notReferFlg;

	// ---- from NodeInfo
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
	private Integer ipAddressVersion;
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
	private Integer snmpVersion;
	private String snmpSecurityLevel;
	private String snmpUser;
	private String snmpAuthPassword;
	private String snmpPrivPassword;
	private String snmpAuthProtocol;
	private String snmpPrivProtocol;
	private String sshUser;
	private String sshUserPassword;
	private String sshPrivateKeyFilepath;
	private String sshPrivateKeyPassphrase;
	private Integer sshPort;
	private Integer sshTimeout;
	private String subPlatformFamily;
	private Integer wbemPort;
	private String wbemProtocol;
	private Integer wbemRetryCount;
	private Integer wbemTimeout;
	private String wbemUser;
	private String wbemUserPassword;
	private Integer winrmPort;
	private String winrmProtocol;
	private Integer winrmRetries;
	private Integer winrmTimeout;
	private String winrmUser;
	private String winrmUserPassword;
	private String winrmVersion;
	private Integer agentAwakePort;
	private AgtNodeOsInfoResponse nodeOsInfo;
	private List<AgtNodeCpuInfoResponse> nodeCpuInfo;
	private List<AgtNodeGeneralDeviceInfoResponse> nodeDeviceInfo;
	private List<AgtNodeDiskInfoResponse> nodeDiskInfo;
	private List<AgtNodeFilesystemInfoResponse> nodeFilesystemInfo;
	private List<AgtNodeHostnameInfoResponse> nodeHostnameInfo;
	private List<AgtNodeMemoryInfoResponse> nodeMemoryInfo;
	private List<AgtNodeNetworkInterfaceInfoResponse> nodeNetworkInterfaceInfo;
	private List<AgtNodeNoteInfoResponse> nodeNoteInfo;
	private List<AgtNodeVariableInfoResponse> nodeVariableInfo;
	private List<AgtNodeNetstatInfoResponse> nodeNetstatInfo;
	private List<AgtNodeProcessInfoResponse> nodeProcessInfo;
	private List<AgtNodePackageInfoResponse> nodePackageInfo;
	private List<AgtNodeProductInfoResponse> nodeProductInfo;
	private List<AgtNodeLicenseInfoResponse> nodeLicenseInfo;
	private List<AgtNodeCustomInfoResponse> nodeCustomInfo;
	private Integer nodeOsRegisterFlag;
	private Integer nodeCpuRegisterFlag;
	private Integer nodeDiskRegisterFlag;
	private Integer nodeFilesystemRegisterFlag;
	private Integer nodeHostnameRegisterFlag;
	private Integer nodeMemoryRegisterFlag;
	private Integer nodeNetworkInterfaceRegisterFlag;
	private Integer nodeNetstatRegisterFlag;
	private Integer nodeProcessRegisterFlag;
	private Integer nodePackageRegisterFlag;
	private Integer nodeProductRegisterFlag;
	private Integer nodeLicenseRegisterFlag;
	private Integer nodeVariableRegisterFlag;
	private Boolean nodeConfigAcquireOnce;
	private String nodeConfigSettingId;
	private Boolean nodeConfigFilterIsAnd;
	private List<AgtNodeConfigFilterInfoResponse> nodeConfigFilterList;
	private Long nodeConfigTargetDatetime;

	public AgtNodeInfoResponse() {
	}

	// ---- accessors

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public Boolean getUncheckFlg() {
		return uncheckFlg;
	}

	public void setUncheckFlg(Boolean uncheckFlg) {
		this.uncheckFlg = uncheckFlg;
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

	public Integer getFacilityType() {
		return facilityType;
	}

	public void setFacilityType(Integer facilityType) {
		this.facilityType = facilityType;
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

	public Long getCreateDatetime() {
		return createDatetime;
	}

	public void setCreateDatetime(Long createDatetime) {
		this.createDatetime = createDatetime;
	}

	public String getModifyUserId() {
		return modifyUserId;
	}

	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
	}

	public Long getModifyDatetime() {
		return modifyDatetime;
	}

	public void setModifyDatetime(Long modifyDatetime) {
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

	public Integer getIpAddressVersion() {
		return ipAddressVersion;
	}

	public void setIpAddressVersion(Integer ipAddressVersion) {
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

	public Integer getSnmpVersion() {
		return snmpVersion;
	}

	public void setSnmpVersion(Integer snmpVersion) {
		this.snmpVersion = snmpVersion;
	}

	public String getSnmpSecurityLevel() {
		return snmpSecurityLevel;
	}

	public void setSnmpSecurityLevel(String snmpSecurityLevel) {
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

	public String getSnmpAuthProtocol() {
		return snmpAuthProtocol;
	}

	public void setSnmpAuthProtocol(String snmpAuthProtocol) {
		this.snmpAuthProtocol = snmpAuthProtocol;
	}

	public String getSnmpPrivProtocol() {
		return snmpPrivProtocol;
	}

	public void setSnmpPrivProtocol(String snmpPrivProtocol) {
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

	public String getWbemProtocol() {
		return wbemProtocol;
	}

	public void setWbemProtocol(String wbemProtocol) {
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

	public String getWinrmProtocol() {
		return winrmProtocol;
	}

	public void setWinrmProtocol(String winrmProtocol) {
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

	public Integer getAgentAwakePort() {
		return agentAwakePort;
	}

	public void setAgentAwakePort(Integer agentAwakePort) {
		this.agentAwakePort = agentAwakePort;
	}

	public AgtNodeOsInfoResponse getNodeOsInfo() {
		return nodeOsInfo;
	}

	public void setNodeOsInfo(AgtNodeOsInfoResponse nodeOsInfo) {
		this.nodeOsInfo = nodeOsInfo;
	}

	public List<AgtNodeCpuInfoResponse> getNodeCpuInfo() {
		return nodeCpuInfo;
	}

	public void setNodeCpuInfo(List<AgtNodeCpuInfoResponse> nodeCpuInfo) {
		this.nodeCpuInfo = nodeCpuInfo;
	}

	public List<AgtNodeGeneralDeviceInfoResponse> getNodeDeviceInfo() {
		return nodeDeviceInfo;
	}

	public void setNodeDeviceInfo(List<AgtNodeGeneralDeviceInfoResponse> nodeDeviceInfo) {
		this.nodeDeviceInfo = nodeDeviceInfo;
	}

	public List<AgtNodeDiskInfoResponse> getNodeDiskInfo() {
		return nodeDiskInfo;
	}

	public void setNodeDiskInfo(List<AgtNodeDiskInfoResponse> nodeDiskInfo) {
		this.nodeDiskInfo = nodeDiskInfo;
	}

	public List<AgtNodeFilesystemInfoResponse> getNodeFilesystemInfo() {
		return nodeFilesystemInfo;
	}

	public void setNodeFilesystemInfo(List<AgtNodeFilesystemInfoResponse> nodeFilesystemInfo) {
		this.nodeFilesystemInfo = nodeFilesystemInfo;
	}

	public List<AgtNodeHostnameInfoResponse> getNodeHostnameInfo() {
		return nodeHostnameInfo;
	}

	public void setNodeHostnameInfo(List<AgtNodeHostnameInfoResponse> nodeHostnameInfo) {
		this.nodeHostnameInfo = nodeHostnameInfo;
	}

	public List<AgtNodeMemoryInfoResponse> getNodeMemoryInfo() {
		return nodeMemoryInfo;
	}

	public void setNodeMemoryInfo(List<AgtNodeMemoryInfoResponse> nodeMemoryInfo) {
		this.nodeMemoryInfo = nodeMemoryInfo;
	}

	public List<AgtNodeNetworkInterfaceInfoResponse> getNodeNetworkInterfaceInfo() {
		return nodeNetworkInterfaceInfo;
	}

	public void setNodeNetworkInterfaceInfo(List<AgtNodeNetworkInterfaceInfoResponse> nodeNetworkInterfaceInfo) {
		this.nodeNetworkInterfaceInfo = nodeNetworkInterfaceInfo;
	}

	public List<AgtNodeNoteInfoResponse> getNodeNoteInfo() {
		return nodeNoteInfo;
	}

	public void setNodeNoteInfo(List<AgtNodeNoteInfoResponse> nodeNoteInfo) {
		this.nodeNoteInfo = nodeNoteInfo;
	}

	public List<AgtNodeVariableInfoResponse> getNodeVariableInfo() {
		return nodeVariableInfo;
	}

	public void setNodeVariableInfo(List<AgtNodeVariableInfoResponse> nodeVariableInfo) {
		this.nodeVariableInfo = nodeVariableInfo;
	}

	public List<AgtNodeNetstatInfoResponse> getNodeNetstatInfo() {
		return nodeNetstatInfo;
	}

	public void setNodeNetstatInfo(List<AgtNodeNetstatInfoResponse> nodeNetstatInfo) {
		this.nodeNetstatInfo = nodeNetstatInfo;
	}

	public List<AgtNodeProcessInfoResponse> getNodeProcessInfo() {
		return nodeProcessInfo;
	}

	public void setNodeProcessInfo(List<AgtNodeProcessInfoResponse> nodeProcessInfo) {
		this.nodeProcessInfo = nodeProcessInfo;
	}

	public List<AgtNodePackageInfoResponse> getNodePackageInfo() {
		return nodePackageInfo;
	}

	public void setNodePackageInfo(List<AgtNodePackageInfoResponse> nodePackageInfo) {
		this.nodePackageInfo = nodePackageInfo;
	}

	public List<AgtNodeProductInfoResponse> getNodeProductInfo() {
		return nodeProductInfo;
	}

	public void setNodeProductInfo(List<AgtNodeProductInfoResponse> nodeProductInfo) {
		this.nodeProductInfo = nodeProductInfo;
	}

	public List<AgtNodeLicenseInfoResponse> getNodeLicenseInfo() {
		return nodeLicenseInfo;
	}

	public void setNodeLicenseInfo(List<AgtNodeLicenseInfoResponse> nodeLicenseInfo) {
		this.nodeLicenseInfo = nodeLicenseInfo;
	}

	public List<AgtNodeCustomInfoResponse> getNodeCustomInfo() {
		return nodeCustomInfo;
	}

	public void setNodeCustomInfo(List<AgtNodeCustomInfoResponse> nodeCustomInfo) {
		this.nodeCustomInfo = nodeCustomInfo;
	}

	public Integer getNodeOsRegisterFlag() {
		return nodeOsRegisterFlag;
	}

	public void setNodeOsRegisterFlag(Integer nodeOsRegisterFlag) {
		this.nodeOsRegisterFlag = nodeOsRegisterFlag;
	}

	public Integer getNodeCpuRegisterFlag() {
		return nodeCpuRegisterFlag;
	}

	public void setNodeCpuRegisterFlag(Integer nodeCpuRegisterFlag) {
		this.nodeCpuRegisterFlag = nodeCpuRegisterFlag;
	}

	public Integer getNodeDiskRegisterFlag() {
		return nodeDiskRegisterFlag;
	}

	public void setNodeDiskRegisterFlag(Integer nodeDiskRegisterFlag) {
		this.nodeDiskRegisterFlag = nodeDiskRegisterFlag;
	}

	public Integer getNodeFilesystemRegisterFlag() {
		return nodeFilesystemRegisterFlag;
	}

	public void setNodeFilesystemRegisterFlag(Integer nodeFilesystemRegisterFlag) {
		this.nodeFilesystemRegisterFlag = nodeFilesystemRegisterFlag;
	}

	public Integer getNodeHostnameRegisterFlag() {
		return nodeHostnameRegisterFlag;
	}

	public void setNodeHostnameRegisterFlag(Integer nodeHostnameRegisterFlag) {
		this.nodeHostnameRegisterFlag = nodeHostnameRegisterFlag;
	}

	public Integer getNodeMemoryRegisterFlag() {
		return nodeMemoryRegisterFlag;
	}

	public void setNodeMemoryRegisterFlag(Integer nodeMemoryRegisterFlag) {
		this.nodeMemoryRegisterFlag = nodeMemoryRegisterFlag;
	}

	public Integer getNodeNetworkInterfaceRegisterFlag() {
		return nodeNetworkInterfaceRegisterFlag;
	}

	public void setNodeNetworkInterfaceRegisterFlag(Integer nodeNetworkInterfaceRegisterFlag) {
		this.nodeNetworkInterfaceRegisterFlag = nodeNetworkInterfaceRegisterFlag;
	}

	public Integer getNodeNetstatRegisterFlag() {
		return nodeNetstatRegisterFlag;
	}

	public void setNodeNetstatRegisterFlag(Integer nodeNetstatRegisterFlag) {
		this.nodeNetstatRegisterFlag = nodeNetstatRegisterFlag;
	}

	public Integer getNodeProcessRegisterFlag() {
		return nodeProcessRegisterFlag;
	}

	public void setNodeProcessRegisterFlag(Integer nodeProcessRegisterFlag) {
		this.nodeProcessRegisterFlag = nodeProcessRegisterFlag;
	}

	public Integer getNodePackageRegisterFlag() {
		return nodePackageRegisterFlag;
	}

	public void setNodePackageRegisterFlag(Integer nodePackageRegisterFlag) {
		this.nodePackageRegisterFlag = nodePackageRegisterFlag;
	}

	public Integer getNodeProductRegisterFlag() {
		return nodeProductRegisterFlag;
	}

	public void setNodeProductRegisterFlag(Integer nodeProductRegisterFlag) {
		this.nodeProductRegisterFlag = nodeProductRegisterFlag;
	}

	public Integer getNodeLicenseRegisterFlag() {
		return nodeLicenseRegisterFlag;
	}

	public void setNodeLicenseRegisterFlag(Integer nodeLicenseRegisterFlag) {
		this.nodeLicenseRegisterFlag = nodeLicenseRegisterFlag;
	}

	public Integer getNodeVariableRegisterFlag() {
		return nodeVariableRegisterFlag;
	}

	public void setNodeVariableRegisterFlag(Integer nodeVariableRegisterFlag) {
		this.nodeVariableRegisterFlag = nodeVariableRegisterFlag;
	}

	public Boolean getNodeConfigAcquireOnce() {
		return nodeConfigAcquireOnce;
	}

	public void setNodeConfigAcquireOnce(Boolean nodeConfigAcquireOnce) {
		this.nodeConfigAcquireOnce = nodeConfigAcquireOnce;
	}

	public String getNodeConfigSettingId() {
		return nodeConfigSettingId;
	}

	public void setNodeConfigSettingId(String nodeConfigSettingId) {
		this.nodeConfigSettingId = nodeConfigSettingId;
	}

	public Boolean getNodeConfigFilterIsAnd() {
		return nodeConfigFilterIsAnd;
	}

	public void setNodeConfigFilterIsAnd(Boolean nodeConfigFilterIsAnd) {
		this.nodeConfigFilterIsAnd = nodeConfigFilterIsAnd;
	}

	public List<AgtNodeConfigFilterInfoResponse> getNodeConfigFilterList() {
		return nodeConfigFilterList;
	}

	public void setNodeConfigFilterList(List<AgtNodeConfigFilterInfoResponse> nodeConfigFilterList) {
		this.nodeConfigFilterList = nodeConfigFilterList;
	}

	public Long getNodeConfigTargetDatetime() {
		return nodeConfigTargetDatetime;
	}

	public void setNodeConfigTargetDatetime(Long nodeConfigTargetDatetime) {
		this.nodeConfigTargetDatetime = nodeConfigTargetDatetime;
	}

}
