/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.bean.SnmpProtocolConstant;
import com.clustercontrol.bean.SnmpSecurityLevelConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.commons.util.CryptUtil;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;



/**
 * The persistent class for the cc_cfg_node database table.
 *
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node", schema="setting")
@DiscriminatorValue("1")
@Cacheable(true)
public class NodeInfo extends FacilityInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	private Boolean autoDeviceSearch		= true;//
	private String administrator			= "";//
	private String characterSet				= "";//
	private String cloudService				= "";//
	private String cloudScope				= "";//
	private String cloudResourceType		= "";//
	private String cloudResourceId			= "";//
	private String cloudResourceName		= "";//
	private String cloudLocation			= "";//
	private String contact					= "";//
	private String hardwareType				= "";//
	private String ipAddressV4				= "";//
	private String ipAddressV6				= "";//
	private Integer ipAddressVersion		= -1;//
	private String ipmiIpAddress			= "";//
	private String ipmiLevel				= "";//
	private Integer ipmiPort				= 0;//
	private String ipmiProtocol				= "RMCP+";//
	private Integer ipmiRetries			= 3;//
	private Integer ipmiTimeout				= 5000;//
	private String ipmiUser					= "root";//
	private String ipmiUserPassword			= "";//
	private Integer jobPriority				= 16;//
	private Integer jobMultiplicity			= 0;//
	private String nodeName					= "";//
	private String osName					= "";//
	private String osRelease				= "";//
	private String osVersion				= "";//
	private String platformFamily			= "";//
	private String snmpCommunity			= "public";//
	private Integer snmpPort				= 161;//
	private Integer snmpRetryCount			= 3;//
	private Integer snmpTimeout				= 5000;//
	private Integer snmpVersion				= SnmpVersionConstant.TYPE_V2;//
	private String snmpSecurityLevel			= SnmpSecurityLevelConstant.NOAUTH_NOPRIV;//
	private String snmpUser					= "";//
	private String snmpAuthPassword			= "";//
	private String snmpPrivPassword			= "";//
	private String snmpAuthProtocol			= SnmpProtocolConstant.MD5;//
	private String snmpPrivProtocol			= SnmpProtocolConstant.DES;//
	private String sshUser 					= "root";//
	private String sshUserPassword				= "";//
	private String sshPrivateKeyFilepath		= "";//
	private String sshPrivateKeyPassphrase		= "";//
	private Integer sshPort					= 22;//
	private Integer sshTimeout					= 50000;//
	private String subPlatformFamily		= "";//
	private Integer wbemPort				= 5988;//
	private String wbemProtocol				= "http";//
	private Integer wbemRetryCount			= 3;//
	private Integer wbemTimeout				= 5000;//
	private String wbemUser					= "root";//
	private String wbemUserPassword			= "";//
	private Integer winrmPort				= 5985;//
	private String winrmProtocol			= "http";//
	private Integer winrmRetries			= 3;//
	private Integer winrmTimeout			= 5000;//
	private String winrmUser				= "Administrator";//
	private String winrmUserPassword		= "";//
	private String winrmVersion				= "";//
	private Integer agentAwakePort			= 24005;//
	private List<NodeCpuInfo> nodeCpuEntities = new ArrayList<>();//
	private List<NodeGeneralDeviceInfo> nodeDeviceEntities = new ArrayList<>();
	private List<NodeDiskInfo> nodeDiskEntities = new ArrayList<>();//
	private List<NodeFilesystemInfo> nodeFilesystemEntities = new ArrayList<>();//
	private List<NodeHostnameInfo> nodeHostnameEntities = new ArrayList<>();//
	private List<NodeMemoryInfo> nodeMemoryEntities = new ArrayList<>();//
	private List<NodeNetworkInterfaceInfo> nodeNetworkInterfaceEntities = new ArrayList<>();//
	private List<NodeNoteInfo> nodeNoteEntities = new ArrayList<>();//
	private List<NodeVariableInfo> nodeVariableEntities = new ArrayList<>();//

	public NodeInfo() {
		super();
	}

	public NodeInfo(String facilityId) {
		super(facilityId);
	}

	@Column(name="administrator")
	public String getAdministrator() {
		return this.administrator;
	}

	public void setAdministrator(String administrator) {
		this.administrator = administrator;
	}


	@Column(name="auto_device_search")
	public Boolean getAutoDeviceSearch() {
		return this.autoDeviceSearch;
	}

	public void setAutoDeviceSearch(Boolean autoDeviceSearch) {
		this.autoDeviceSearch = autoDeviceSearch;
	}

	@Column(name="character_set")
	public String getCharacterSet() {
		return this.characterSet;
	}

	public void setCharacterSet(String characterSet) {
		this.characterSet = characterSet;
	}


	@Column(name="cloud_service")
	public String getCloudService() {
		return this.cloudService;
	}

	public void setCloudService(String cloudService) {
		this.cloudService = cloudService;
	}


	@Column(name="cloud_scope")
	public String getCloudScope() {
		return this.cloudScope;
	}

	public void setCloudScope(String cloudScope) {
		this.cloudScope = cloudScope;
	}


	@Column(name="cloud_resource_type")
	public String getCloudResourceType() {
		return this.cloudResourceType;
	}

	public void setCloudResourceType(String cloudResourceType) {
		this.cloudResourceType = cloudResourceType;
	}


	@Column(name="cloud_resource_id")
	public String getCloudResourceId() {
		return this.cloudResourceId;
	}

	public void setCloudResourceId(String cloudResourceId) {
		this.cloudResourceId = cloudResourceId;
	}
	
	@Column(name="cloud_resource_name")
	public String getCloudResourceName() {
		return this.cloudResourceName;
	}
	
	public void setCloudResourceName(String cloudResourceName) {
		this.cloudResourceName = cloudResourceName;
	}


	@Column(name="cloud_location")
	public String getCloudLocation() {
		return this.cloudLocation;
	}

	public void setCloudLocation(String cloudLocation) {
		this.cloudLocation = cloudLocation;
	}


	@Column(name="contact")
	public String getContact() {
		return this.contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}


	@Column(name="hardware_type")
	public String getHardwareType() {
		return this.hardwareType;
	}

	public void setHardwareType(String hardwareType) {
		this.hardwareType = hardwareType;
	}

	@Column(name="ip_address_v4")
	public String getIpAddressV4() {
		return this.ipAddressV4;
	}

	public void setIpAddressV4(String ipAddressV4) {
		this.ipAddressV4 = ipAddressV4;
	}


	@Column(name="ip_address_v6")
	public String getIpAddressV6() {
		return this.ipAddressV6;
	}

	public void setIpAddressV6(String ipAddressV6) {
		this.ipAddressV6 = ipAddressV6;
	}


	@Column(name="ip_address_version")
	public Integer getIpAddressVersion() {
		return this.ipAddressVersion;
	}

	public void setIpAddressVersion(Integer ipAddressVersion) {
		this.ipAddressVersion = ipAddressVersion;
	}


	@Column(name="ipmi_ip_address")
	public String getIpmiIpAddress() {
		return this.ipmiIpAddress;
	}

	public void setIpmiIpAddress(String ipmiIpAddress) {
		this.ipmiIpAddress = ipmiIpAddress;
	}


	@Column(name="ipmi_level")
	public String getIpmiLevel() {
		return this.ipmiLevel;
	}

	public void setIpmiLevel(String ipmiLevel) {
		this.ipmiLevel = ipmiLevel;
	}


	@Column(name="ipmi_port")
	public Integer getIpmiPort() {
		return this.ipmiPort;
	}

	public void setIpmiPort(Integer ipmiPort) {
		this.ipmiPort = ipmiPort;
	}


	@Column(name="ipmi_protocol")
	public String getIpmiProtocol() {
		return this.ipmiProtocol;
	}

	public void setIpmiProtocol(String ipmiProtocol) {
		this.ipmiProtocol = ipmiProtocol;
	}


	@Column(name="ipmi_retry_count")
	public Integer getIpmiRetries() {
		return this.ipmiRetries;
	}

	public void setIpmiRetries(Integer ipmiRetryCount) {
		this.ipmiRetries = ipmiRetryCount;
	}


	@Column(name="ipmi_timeout")
	public Integer getIpmiTimeout() {
		return this.ipmiTimeout;
	}

	public void setIpmiTimeout(Integer ipmiTimeout) {
		this.ipmiTimeout = ipmiTimeout;
	}


	@Column(name="job_priority")
	public Integer getJobPriority() {
		return this.jobPriority;
	}

	public void setJobPriority(Integer jobPriority) {
		this.jobPriority = jobPriority;
	}


	@Column(name="job_multiplicity")
	public Integer getJobMultiplicity() {
		return this.jobMultiplicity;
	}

	public void setJobMultiplicity(Integer jobMultiplicity) {
		this.jobMultiplicity = jobMultiplicity;
	}


	@Column(name="ipmi_user")
	public String getIpmiUser() {
		return this.ipmiUser;
	}

	public void setIpmiUser(String ipmiUser) {
		this.ipmiUser = ipmiUser;
	}

	@Transient
	public String getIpmiUserPassword() {
		return CryptUtil.decrypt(getIpmiUserPasswordCrypt());
	}

	public void setIpmiUserPassword(String ipmiUserPassword) {
		setIpmiUserPasswordCrypt(CryptUtil.encrypt(ipmiUserPassword));
	}

	
	@Column(name="ipmi_user_password")
	public String getIpmiUserPasswordCrypt() {
		return this.ipmiUserPassword;
	}

	public void setIpmiUserPasswordCrypt(String ipmiUserPassword) {
		this.ipmiUserPassword = ipmiUserPassword;
	}


	@Column(name="node_name")
	public String getNodeName() {
		return this.nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	@Column(name="os_name")
	public String getOsName() {
		return this.osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}


	@Column(name="os_release")
	public String getOsRelease() {
		return this.osRelease;
	}

	public void setOsRelease(String osRelease) {
		this.osRelease = osRelease;
	}


	@Column(name="os_version")
	public String getOsVersion() {
		return this.osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}


	@Column(name="platform_family")
	public String getPlatformFamily() {
		return this.platformFamily;
	}

	public void setPlatformFamily(String platformFamily) {
		this.platformFamily = platformFamily;
	}


	@Column(name="snmp_community")
	public String getSnmpCommunity() {
		return this.snmpCommunity;
	}

	public void setSnmpCommunity(String snmpCommunity) {
		this.snmpCommunity = snmpCommunity;
	}


	@Column(name="snmp_port")
	public Integer getSnmpPort() {
		return this.snmpPort;
	}

	public void setSnmpPort(Integer snmpPort) {
		this.snmpPort = snmpPort;
	}


	@Column(name="snmp_retry_count")
	public Integer getSnmpRetryCount() {
		return this.snmpRetryCount;
	}

	public void setSnmpRetryCount(Integer snmpRetryCount) {
		this.snmpRetryCount = snmpRetryCount;
	}


	@Column(name="snmp_timeout")
	public Integer getSnmpTimeout() {
		return this.snmpTimeout;
	}

	public void setSnmpTimeout(Integer snmpTimeout) {
		this.snmpTimeout = snmpTimeout;
	}


	@Column(name="snmp_version")
	public Integer getSnmpVersion() {
		return this.snmpVersion;
	}

	public void setSnmpVersion(Integer snmpVersion) {
		this.snmpVersion = snmpVersion;
	}


	@Column(name="snmp_security_level")
	public String getSnmpSecurityLevel() {
		return this.snmpSecurityLevel;
	}

	public void setSnmpSecurityLevel(String snmpSecurityLevel) {
		this.snmpSecurityLevel = snmpSecurityLevel;
	}


	@Column(name="snmp_user")
	public String getSnmpUser() {
		return this.snmpUser;
	}

	public void setSnmpUser(String snmpUser) {
		this.snmpUser = snmpUser;
	}

	@Transient
	public String getSnmpAuthPassword() {
		return CryptUtil.decrypt(getSnmpAuthPasswordCrypt());
	}

	public void setSnmpAuthPassword(String snmpAuthPassword) {
		setSnmpAuthPasswordCrypt(CryptUtil.encrypt(snmpAuthPassword));
	}

	@Column(name="snmp_auth_password")
	public String getSnmpAuthPasswordCrypt() {
		return this.snmpAuthPassword;
	}

	public void setSnmpAuthPasswordCrypt(String snmpAuthPassword) {
		this.snmpAuthPassword = snmpAuthPassword;
	}


	@Transient
	public String getSnmpPrivPassword() {
		return CryptUtil.decrypt(getSnmpPrivPasswordCrypt());
	}

	public void setSnmpPrivPassword(String snmpPrivPassword) {
		setSnmpPrivPasswordCrypt(CryptUtil.encrypt(snmpPrivPassword));
	}


	@Column(name="snmp_priv_password")
	public String getSnmpPrivPasswordCrypt() {
		return this.snmpPrivPassword;
	}

	public void setSnmpPrivPasswordCrypt(String snmpPrivPassword) {
		this.snmpPrivPassword = snmpPrivPassword;
	}


	@Column(name="snmp_auth_protocol")
	public String getSnmpAuthProtocol() {
		return this.snmpAuthProtocol;
	}

	public void setSnmpAuthProtocol(String snmpAuthProtocol) {
		this.snmpAuthProtocol = snmpAuthProtocol;
	}


	@Column(name="snmp_priv_protocol")
	public String getSnmpPrivProtocol() {
		return this.snmpPrivProtocol;
	}

	public void setSnmpPrivProtocol(String snmpPrivProtocol) {
		this.snmpPrivProtocol = snmpPrivProtocol;
	}

	@Column(name="ssh_user")
	public String getSshUser() {
		return this.sshUser;
	}
	
	public void setSshUser(String sshUser) {
		this.sshUser = sshUser;
	}
	
	@Transient
	public String getSshUserPassword() {
		return CryptUtil.decrypt(getSshUserPasswordCrypt());
	}
	
	public void setSshUserPassword(String sshUserPassword) {
		setSshUserPasswordCrypt(CryptUtil.encrypt(sshUserPassword));
	}

	@Column(name="ssh_user_password")
	public String getSshUserPasswordCrypt() {
		return this.sshUserPassword;
	}
	
	public void setSshUserPasswordCrypt(String sshUserPassword) {
		this.sshUserPassword = sshUserPassword;
	}
	
	@Column(name="ssh_private_key_filepath")
	public String getSshPrivateKeyFilepath() {
		return this.sshPrivateKeyFilepath;
	}
	
	public void setSshPrivateKeyFilepath(String sshPrivateKeyFilepath) {
		this.sshPrivateKeyFilepath = sshPrivateKeyFilepath;
	}

	@Transient
	public String getSshPrivateKeyPassphrase() {
		return CryptUtil.decrypt(getSshPrivateKeyPassphraseCrypt());
	}
	
	public void setSshPrivateKeyPassphrase(String sshPrivateKeyPassphrase) {
		setSshPrivateKeyPassphraseCrypt(CryptUtil.encrypt(sshPrivateKeyPassphrase));
	}
	
	@Column(name="ssh_private_key_passphrase")
	public String getSshPrivateKeyPassphraseCrypt() {
		return this.sshPrivateKeyPassphrase;
	}
	
	public void setSshPrivateKeyPassphraseCrypt(String sshPrivateKeyPassphrase) {
		this.sshPrivateKeyPassphrase = sshPrivateKeyPassphrase;
	}
	
	@Column(name="ssh_port")
	public Integer getSshPort() {
		return this.sshPort;
	}
	
	public void setSshPort(Integer sshPort) {
		this.sshPort = sshPort;
	}
	
	@Column(name="ssh_timeout")
	public Integer getSshTimeout() {
		return this.sshTimeout;
	}
	
	public void setSshTimeout(Integer sshTimeout) {
		this.sshTimeout = sshTimeout;
	}

	@Column(name="sub_platform_family")
	public String getSubPlatformFamily() {
		return this.subPlatformFamily;
	}

	public void setSubPlatformFamily(String subPlatformFamily) {
		this.subPlatformFamily = subPlatformFamily;
	}

	@Column(name="wbem_port")
	public Integer getWbemPort() {
		return this.wbemPort;
	}

	public void setWbemPort(Integer wbemPort) {
		this.wbemPort = wbemPort;
	}


	@Column(name="wbem_protocol")
	public String getWbemProtocol() {
		return this.wbemProtocol;
	}

	public void setWbemProtocol(String wbemProtocol) {
		this.wbemProtocol = wbemProtocol;
	}


	@Column(name="wbem_retry_count")
	public Integer getWbemRetryCount() {
		return this.wbemRetryCount;
	}

	public void setWbemRetryCount(Integer wbemRetryCount) {
		this.wbemRetryCount = wbemRetryCount;
	}


	@Column(name="wbem_timeout")
	public Integer getWbemTimeout() {
		return this.wbemTimeout;
	}

	public void setWbemTimeout(Integer wbemTimeout) {
		this.wbemTimeout = wbemTimeout;
	}


	@Column(name="wbem_user")
	public String getWbemUser() {
		return this.wbemUser;
	}

	public void setWbemUser(String wbemUser) {
		this.wbemUser = wbemUser;
	}

	@Transient
	public String getWbemUserPassword() {
		return CryptUtil.decrypt(getWbemUserPasswordCrypt());
	}

	public void setWbemUserPassword(String wbemUserPassword) {
		setWbemUserPasswordCrypt(CryptUtil.encrypt(wbemUserPassword));
	}

	@Column(name="wbem_user_password")
	public String getWbemUserPasswordCrypt() {
		return this.wbemUserPassword;
	}

	public void setWbemUserPasswordCrypt(String wbemUserPassword) {
		this.wbemUserPassword = wbemUserPassword;
	}


	@Column(name="winrm_port")
	public Integer getWinrmPort() {
		return this.winrmPort;
	}

	public void setWinrmPort(Integer winrmPort) {
		this.winrmPort = winrmPort;
	}


	@Column(name="winrm_protocol")
	public String getWinrmProtocol() {
		return this.winrmProtocol;
	}

	public void setWinrmProtocol(String winrmProtocol) {
		this.winrmProtocol = winrmProtocol;
	}

	/**
	 * WinRM接続リトライ回数のgetter
	 * @return winrmRetries
	 */
	@Column(name="winrm_retry_count")
	public Integer getWinrmRetries() {
		return this.winrmRetries;
	}

	/**
	 * WinRM接続リトライ回数のsetter
	 * @param winrmRetries
	 */
	public void setWinrmRetries(Integer winrmRetryCount) {
		this.winrmRetries = winrmRetryCount;
	}

	@Column(name="winrm_timeout")
	public Integer getWinrmTimeout() {
		return this.winrmTimeout;
	}

	public void setWinrmTimeout(Integer winrmTimeout) {
		this.winrmTimeout = winrmTimeout;
	}


	@Column(name="winrm_user")
	public String getWinrmUser() {
		return this.winrmUser;
	}

	public void setWinrmUser(String winrmUser) {
		this.winrmUser = winrmUser;
	}

	@Transient
	public String getWinrmUserPassword() {
		return CryptUtil.decrypt(getWinrmUserPasswordCrypt());
	}

	public void setWinrmUserPassword(String winrmUserPassword) {
		setWinrmUserPasswordCrypt(CryptUtil.encrypt(winrmUserPassword));
	}

	@Column(name="winrm_user_password")
	public String getWinrmUserPasswordCrypt() {
		return this.winrmUserPassword;
	}

	public void setWinrmUserPasswordCrypt(String winrmUserPassword) {
		this.winrmUserPassword = winrmUserPassword;
	}


	@Column(name="winrm_version")
	public String getWinrmVersion() {
		return this.winrmVersion;
	}

	public void setWinrmVersion(String winrmVersion) {
		this.winrmVersion = winrmVersion;
	}

	@Column(name="agent_awake_port")
	public Integer getAgentAwakePort() {
		return this.agentAwakePort;
	}

	public void setAgentAwakePort(Integer agentAwakePort) {
		this.agentAwakePort = agentAwakePort;
	}

	//bi-directional many-to-one association to NodeCpuEntity
	@OneToMany(mappedBy="nodeEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeCpuInfo> getNodeCpuInfo() {
		return this.nodeCpuEntities;
	}

	public void setNodeCpuInfo(List<NodeCpuInfo> nodeCpuEntities) {
		this.nodeCpuEntities = nodeCpuEntities;
	}


	//bi-directional many-to-one association to NodeDeviceEntity
	@OneToMany(mappedBy="nodeEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeGeneralDeviceInfo> getNodeDeviceInfo() {
		return this.nodeDeviceEntities;
	}

	public void setNodeDeviceInfo(List<NodeGeneralDeviceInfo> nodeDeviceEntities) {
		this.nodeDeviceEntities = nodeDeviceEntities;
	}


	//bi-directional many-to-one association to NodeDiskEntity
	@OneToMany(mappedBy="nodeEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeDiskInfo> getNodeDiskInfo() {
		return this.nodeDiskEntities;
	}

	public void setNodeDiskInfo(List<NodeDiskInfo> nodeDiskEntities) {
		this.nodeDiskEntities = nodeDiskEntities;
	}

	//bi-directional many-to-one association to NodeFilesystemEntity
	@OneToMany(mappedBy="nodeEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeFilesystemInfo> getNodeFilesystemInfo() {
		return this.nodeFilesystemEntities;
	}

	public void setNodeFilesystemInfo(List<NodeFilesystemInfo> nodeFilesystemEntities) {
		this.nodeFilesystemEntities = nodeFilesystemEntities;
	}


	//bi-directional many-to-one association to NodeHostnameEntity
	@OneToMany(mappedBy="nodeEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeHostnameInfo> getNodeHostnameInfo() {
		return this.nodeHostnameEntities;
	}

	public void setNodeHostnameInfo(List<NodeHostnameInfo> nodeHostnameEntities) {
		this.nodeHostnameEntities = nodeHostnameEntities;
	}


	//bi-directional many-to-one association to NodeMemoryEntity
	@OneToMany(mappedBy="nodeEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeMemoryInfo> getNodeMemoryInfo() {
		return this.nodeMemoryEntities;
	}

	public void setNodeMemoryInfo(List<NodeMemoryInfo> nodeMemoryEntities) {
		this.nodeMemoryEntities = nodeMemoryEntities;
	}


	//bi-directional many-to-one association to NodeNetworkInterfaceEntity
	@OneToMany(mappedBy="nodeEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeNetworkInterfaceInfo> getNodeNetworkInterfaceInfo() {
		return this.nodeNetworkInterfaceEntities;
	}

	public void setNodeNetworkInterfaceInfo(List<NodeNetworkInterfaceInfo> nodeNetworkInterfaceEntities) {
		this.nodeNetworkInterfaceEntities = nodeNetworkInterfaceEntities;
	}


	//bi-directional many-to-one association to NodeNoteEntity
	@OneToMany(mappedBy="nodeEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeNoteInfo> getNodeNoteInfo() {
		return this.nodeNoteEntities;
	}

	public void setNodeNoteInfo(List<NodeNoteInfo> nodeNoteEntities) {
		this.nodeNoteEntities = nodeNoteEntities;
	}


	//bi-directional many-to-one association to NodeVariableEntity
	@OneToMany(mappedBy="nodeEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeVariableInfo> getNodeVariableInfo() {
		return this.nodeVariableEntities;
	}

	public void setNodeVariableInfo(List<NodeVariableInfo> nodeVariableEntities) {
		this.nodeVariableEntities = nodeVariableEntities;
	}

	/**
	 * NodeCpuEntity削除<BR>
	 *
	 * 指定されたPK以外の子Entityを削除する。
	 *
	 */
	public void deleteNodeCpuEntities(List<NodeDeviceInfoPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeCpuInfo> list = this.getNodeCpuInfo();
			Iterator<NodeCpuInfo> iter = list.iterator();
			while(iter.hasNext()) {
				NodeCpuInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}

	/**
	 * NodeDeviceEntity削除<BR>
	 *
	 * 指定されたPK以外の子Entityを削除する。
	 *
	 */
	public void deleteNodeDeviceEntities(List<NodeDeviceInfoPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeGeneralDeviceInfo> list = this.getNodeDeviceInfo();
			Iterator<NodeGeneralDeviceInfo> iter = list.iterator();
			while(iter.hasNext()) {
				NodeGeneralDeviceInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}

	/**
	 * NodeDiskEntity削除<BR>
	 *
	 * 指定されたPK以外の子Entityを削除する。
	 *
	 */
	public void deleteNodeDiskEntities(List<NodeDeviceInfoPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeDiskInfo> list = this.getNodeDiskInfo();
			Iterator<NodeDiskInfo> iter = list.iterator();
			while(iter.hasNext()) {
				NodeDiskInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}

	/**
	 * NodeFilesystemEntity削除<BR>
	 *
	 * 指定されたPK以外の子Entityを削除する。
	 *
	 */
	public void deleteNodeFilesystemEntities(List<NodeDeviceInfoPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeFilesystemInfo> list = this.getNodeFilesystemInfo();
			Iterator<NodeFilesystemInfo> iter = list.iterator();
			while(iter.hasNext()) {
				NodeFilesystemInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}

	/**
	 * NodeHostnameEntity削除<BR>
	 *
	 * 指定されたPK以外の子Entityを削除する。
	 *
	 */
	public void deleteNodeHostnameEntities(List<NodeHostnameInfoPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeHostnameInfo> list = this.getNodeHostnameInfo();
			Iterator<NodeHostnameInfo> iter = list.iterator();
			while(iter.hasNext()) {
				NodeHostnameInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}

	/**
	 * NodeMemoryEntity削除<BR>
	 *
	 * 指定されたPK以外の子Entityを削除する。
	 *
	 */
	public void deleteNodeMemoryEntities(List<NodeDeviceInfoPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeMemoryInfo> list = this.getNodeMemoryInfo();
			Iterator<NodeMemoryInfo> iter = list.iterator();
			while(iter.hasNext()) {
				NodeMemoryInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}

	/**
	 * NodeNetworkInterfaceEntity削除<BR>
	 *
	 * 指定されたPK以外の子Entityを削除する。
	 *
	 */
	public void deleteNodeNetworkInterfaceEntities(List<NodeDeviceInfoPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeNetworkInterfaceInfo> list = this.getNodeNetworkInterfaceInfo();
			Iterator<NodeNetworkInterfaceInfo> iter = list.iterator();
			while(iter.hasNext()) {
				NodeNetworkInterfaceInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}

	/**
	 * NodeNoteEntity削除<BR>
	 *
	 * 指定されたPK以外の子Entityを削除する。
	 *
	 */
	public void deleteNodeNoteEntities(List<NodeNoteInfoPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeNoteInfo> list = this.getNodeNoteInfo();
			Iterator<NodeNoteInfo> iter = list.iterator();
			while(iter.hasNext()) {
				NodeNoteInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}

	/**
	 * NodeVariableEntity削除<BR>
	 *
	 * 指定されたPK以外の子Entityを削除する。
	 *
	 */
	public void deleteNodeVariableEntities(List<NodeVariableInfoPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeVariableInfo> list = this.getNodeVariableInfo();
			Iterator<NodeVariableInfo> iter = list.iterator();
			while(iter.hasNext()) {
				NodeVariableInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}

	@Override
	public NodeInfo clone() {
		NodeInfo cloneInfo = (NodeInfo) super.clone();

		cloneInfo.autoDeviceSearch = this.autoDeviceSearch;
		// HW
		cloneInfo.platformFamily = this.platformFamily;
		cloneInfo.subPlatformFamily = this.subPlatformFamily;
		cloneInfo.hardwareType = this.hardwareType;

		// IPアドレス
		cloneInfo.ipAddressVersion = this.ipAddressVersion;
		cloneInfo.ipAddressV4 = this.ipAddressV4;
		cloneInfo.ipAddressV6 = this.ipAddressV6;

		//参照型は一旦別のオブジェクトへコピー（親クラスをcloneした時点で子クラスの
		//参照型メンバ変数への参照がセットされてしまうため）
		List<NodeHostnameInfo> tmpHostnameList = new ArrayList<NodeHostnameInfo>();
		for (NodeHostnameInfo thisInfo : getNodeHostnameInfo()) {
			tmpHostnameList.add(thisInfo.clone());
		}
		cloneInfo.setNodeHostnameInfo(tmpHostnameList);

		// OS
		cloneInfo.nodeName = this.nodeName;
		cloneInfo.osName = this.osName;
		cloneInfo.osRelease = this.osRelease;
		cloneInfo.osVersion = this.osVersion;
		cloneInfo.characterSet = this.characterSet;

		// Hinemosエージェント
		cloneInfo.agentAwakePort = this.agentAwakePort;

		// JOB
		cloneInfo.jobPriority = this.jobPriority;
		cloneInfo.jobMultiplicity = this.jobMultiplicity;

		// SNMP
		cloneInfo.snmpUser = this.snmpUser;
		cloneInfo.snmpAuthPassword = this.snmpAuthPassword;
		cloneInfo.snmpPrivPassword = this.snmpPrivPassword;
		cloneInfo.snmpPort = this.snmpPort;
		cloneInfo.snmpCommunity = this.snmpCommunity;
		cloneInfo.snmpVersion = this.snmpVersion;
		cloneInfo.snmpSecurityLevel = this.snmpSecurityLevel;
		cloneInfo.snmpAuthProtocol = this.snmpAuthProtocol;
		cloneInfo.snmpPrivProtocol = this.snmpPrivProtocol;
		cloneInfo.snmpTimeout = this.snmpTimeout;
		cloneInfo.snmpRetryCount = this.snmpRetryCount;

		// WBEM
		cloneInfo.wbemUser = this.wbemUser;
		cloneInfo.wbemUserPassword = this.wbemUserPassword;
		cloneInfo.wbemPort = this.wbemPort;
		cloneInfo.wbemProtocol = this.wbemProtocol;
		cloneInfo.wbemTimeout = this.wbemTimeout;
		cloneInfo.wbemRetryCount = this.wbemRetryCount;

		// IPMI
		cloneInfo.ipmiIpAddress = this.ipmiIpAddress;
		cloneInfo.ipmiPort = this.ipmiPort;
		cloneInfo.ipmiUser = this.ipmiUser;
		cloneInfo.ipmiUserPassword = this.ipmiUserPassword;
		cloneInfo.ipmiTimeout = this.ipmiTimeout;
		cloneInfo.ipmiRetries = this.ipmiRetries;
		cloneInfo.ipmiProtocol = this.ipmiProtocol;
		cloneInfo.ipmiLevel = this.ipmiLevel;

		// WinRM
		cloneInfo.winrmUser = this.winrmUser;
		cloneInfo.winrmUserPassword = this.winrmUserPassword;
		cloneInfo.winrmVersion = this.winrmVersion;
		cloneInfo.winrmPort = this.winrmPort;
		cloneInfo.winrmProtocol = this.winrmProtocol;
		cloneInfo.winrmTimeout = this.winrmTimeout;
		cloneInfo.winrmRetries = this.winrmRetries;

		// デバイス
		//参照型は一旦別のオブジェクトへコピー
		List<NodeGeneralDeviceInfo> tmpDeviceList = new ArrayList<NodeGeneralDeviceInfo>();
		for (NodeGeneralDeviceInfo thisInfo : getNodeDeviceInfo()) {
			tmpDeviceList.add(thisInfo.clone());
		}
		cloneInfo.setNodeDeviceInfo(tmpDeviceList);

		List<NodeCpuInfo> tmpCpuList = new ArrayList<NodeCpuInfo>();
		for (NodeCpuInfo thisInfo : this.getNodeCpuInfo()) {
			tmpCpuList.add(thisInfo.clone());
		}
		cloneInfo.setNodeCpuInfo(tmpCpuList);

		List<NodeMemoryInfo> tmpMemList = new ArrayList<NodeMemoryInfo>();
		for (NodeMemoryInfo thisInfo : this.getNodeMemoryInfo()) {
			tmpMemList.add(thisInfo.clone());
		}
		cloneInfo.setNodeMemoryInfo(tmpMemList);

		List<NodeDiskInfo> tmpDiskList = new ArrayList<NodeDiskInfo>();
		for (NodeDiskInfo thisInfo : this.getNodeDiskInfo()) {
			tmpDiskList.add(thisInfo.clone());
		}
		cloneInfo.setNodeDiskInfo(tmpDiskList);

		List<NodeNetworkInterfaceInfo> tmpNwIfList = new ArrayList<NodeNetworkInterfaceInfo>();
		for (NodeNetworkInterfaceInfo thisInfo : this.getNodeNetworkInterfaceInfo()) {
			tmpNwIfList.add(thisInfo.clone());
		}
		cloneInfo.setNodeNetworkInterfaceInfo(tmpNwIfList);

		List<NodeFilesystemInfo> tmpFSList = new ArrayList<NodeFilesystemInfo>();
		for (NodeFilesystemInfo thisInfo : this.getNodeFilesystemInfo()) {
			tmpFSList.add(thisInfo.clone());
		}
		cloneInfo.setNodeFilesystemInfo(tmpFSList);

		// クラウド・仮想化
		cloneInfo.cloudService = this.cloudService;
		cloneInfo.cloudScope = this.cloudScope;
		cloneInfo.cloudResourceType = this.cloudResourceType;
		cloneInfo.cloudResourceName = this.cloudResourceName;
		cloneInfo.cloudResourceId = this.cloudResourceId;
		cloneInfo.cloudLocation = this.cloudLocation;

		// ノード変数
		ArrayList<NodeVariableInfo> tmpVlList = new ArrayList<NodeVariableInfo>();
		for (NodeVariableInfo thisInfo : this.getNodeVariableInfo()) {
			tmpVlList.add(thisInfo.clone());
		}
		cloneInfo.setNodeVariableInfo(tmpVlList);

		// 保守
		cloneInfo.administrator = this.administrator;
		cloneInfo.contact = this.contact;

		// 備考
		ArrayList<NodeNoteInfo> tmpNoteList = new ArrayList<NodeNoteInfo>();
		for (NodeNoteInfo thisInfo : this.getNodeNoteInfo()) {
			tmpNoteList.add(thisInfo.clone());
		}
		cloneInfo.setNodeNoteInfo(tmpNoteList);

		return cloneInfo;
	}
	
	public void setDefaultInfo() {
		// HW
		if (platformFamily == null) platformFamily = "";
		if (subPlatformFamily == null) subPlatformFamily = "";
		if (hardwareType == null) hardwareType = "";

		// IPアドレス
		if (ipAddressVersion == null) ipAddressVersion = null;
		if (ipAddressV4 == null) ipAddressV4 = "";
		if (ipAddressV6 == null) ipAddressV6 = "";

		// OS
		if (nodeName == null) nodeName = "";
		if (osName == null) osName = "";
		if (osRelease == null) osRelease = "";
		if (osVersion == null) osVersion = "";
		if (characterSet == null) characterSet = "";

		// Hinemosエージェント
		if (agentAwakePort == null) agentAwakePort = 24005;

		// JOB
		if (jobPriority == null) jobPriority = 16;
		if (jobMultiplicity == null) jobMultiplicity = 0;

		// SNMP
		if (snmpUser == null) snmpUser = "";
		if (snmpAuthPassword == null) snmpAuthPassword = "";
		if (snmpPrivPassword == null) snmpPrivPassword = "";
		if (snmpPort == null) snmpPort = 161;
		if (snmpCommunity == null) snmpCommunity = "";
		if (snmpVersion == null) snmpVersion = 0;
		if (snmpSecurityLevel == null) snmpSecurityLevel = "";
		if (snmpAuthProtocol == null) snmpAuthProtocol = "";
		if (snmpPrivProtocol == null) snmpPrivProtocol = "";
		if (snmpTimeout == null) snmpTimeout = 5000;
		if (snmpRetryCount == null) snmpRetryCount = 3;

		// WBEM
		if (wbemUser == null) wbemUser = "";
		if (wbemUserPassword == null) wbemUserPassword = "";
		if (wbemPort == null) wbemPort = 5988;
		if (wbemProtocol == null) wbemProtocol = "";
		if (wbemTimeout == null) wbemTimeout = 5000;
		if (wbemRetryCount == null) wbemRetryCount = 3;

		// IPMI
		if (ipmiIpAddress == null) ipmiIpAddress = "";
		if (ipmiPort == null) ipmiPort = 0;
		if (ipmiUser == null) ipmiUser = "";
		if (ipmiUserPassword == null) ipmiUserPassword = "";
		if (ipmiTimeout == null) ipmiTimeout = 5000;
		if (ipmiRetries == null) ipmiRetries = 3;
		if (ipmiProtocol == null) ipmiProtocol = "";
		if (ipmiLevel == null) ipmiLevel = "";

		// WinRM
		if (winrmUser == null) winrmUser = "";
		if (winrmUserPassword == null) winrmUserPassword = "";
		if (winrmVersion == null) winrmVersion = "";
		if (winrmPort == null) winrmPort = 5985;
		if (winrmProtocol == null) winrmProtocol = "";
		if (winrmTimeout == null) winrmTimeout = 5000;
		if (winrmRetries == null) winrmRetries = 3;

		if (sshUser == null) sshUser = "root";
		if (sshUserPassword == null) sshUserPassword = "";
		if (sshPrivateKeyFilepath == null) sshPrivateKeyFilepath = "";
		if (sshPrivateKeyPassphrase == null) sshPrivateKeyPassphrase = "";
		if (sshPort == null) sshPort = 22;
		if (sshTimeout == null) sshTimeout = 50000;

		// デバイス

		// クラウド・サーバ仮想化
		if (cloudService == null) cloudService = "";
		if (cloudScope == null) cloudScope = "";
		if (cloudResourceType == null) cloudResourceType = "";
		if (cloudResourceName == null) cloudResourceName = "";
		if (cloudResourceId == null) cloudResourceId = "";
		if (cloudLocation == null) cloudLocation = "";

		// ノード変数

		// 保守
		if (administrator == null) administrator = "";
		if (contact == null) contact = "";
	}
	
	/**
	 * 利用可能なIPアドレスを返します。
	 *
	 * @return 利用可能なIPアドレス
	 * @throws UnknownHostException
	 */
	public String getAvailableIpAddress() {
		// 「IPアドレスのバージョン」により指定されたIPアドレスを設定する。
		Integer ipVersion = getIpAddressVersion();
		String ipAddress = null;
		if(ipVersion != null && ipVersion.intValue() == 6){
			ipAddress = getIpAddressV6();
		} else {
			ipAddress = getIpAddressV4();
		}
		return ipAddress;
	}
	
	/**
	 * WBEMで値取得時に必要となるNameSpaceです。
	 * 現在は固定値を返します。
	 * @return 固定値（root/cimv2）を返す。
	 */
	public String getWbemNameSpace(){
		return "root/cimv2";
	}
	
	@Override
	protected void preparePersisting() {
		for (NodeHostnameInfo host : getNodeHostnameInfo()) {
			host.setFacilityId(getFacilityId());
		}

		for (NodeDeviceInfo device : getNodeNetworkInterfaceInfo()) {
			device.setFacilityId(getFacilityId());
		}

		for (NodeDeviceInfo device : getNodeMemoryInfo()) {
			device.setFacilityId(getFacilityId());
		}

		for (NodeDeviceInfo device : getNodeDiskInfo()) {
			device.setFacilityId(getFacilityId());
		}

		for (NodeDeviceInfo device : getNodeDeviceInfo()) {
			device.setFacilityId(getFacilityId());
		}

		for (NodeDeviceInfo device : getNodeCpuInfo()) {
			device.setFacilityId(getFacilityId());
		}

		for (NodeDeviceInfo device : getNodeFilesystemInfo()) {
			device.setFacilityId(getFacilityId());
		}

		for (NodeVariableInfo variable : getNodeVariableInfo()) {
			variable.setFacilityId(getFacilityId());
		}
		
		for (NodeNoteInfo note: getNodeNoteInfo()) {
			note.setFacilityId(getFacilityId());
		}
	}
	
	/**
	 * ノード単位で実行する監視（プロセス監視やリソース監視）の、ノード固有の監視ディレイ値を返します。
	 * 
	 * 例えばあるノードでこの値が500の場合、
	 * 1分間隔の監視 ： 毎分20秒に起動（500 % 60 = 20s）
	 * 5分間隔の監視 ： 毎5分200秒（つまり03:20、08:20、13:20、・・・）に起動。（500 % 300 = 200s）
	 * 1時間間隔の監視 : 毎時500秒（つまり00:08:20、01:08:20、02:08:20・・・）に起動。（500 % 3600 = 500s）
	 * 
	 * @return ノード固有のディレイ時間 （0～3599[s]）
	 */
	@Transient
	public int getNodeMonitorDelaySec() {
		// Hinemosプロパティ[monitor.node.delay.fix]がtrueの場合は0固定とする
		if (HinemosPropertyCommon.monitor_node_delay_fix.getBooleanValue()) {
			return 0;
		}
		
		// ノードのFacilityIdのHashCodeを元にディレイ値を生成
		int delaySec = getFacilityId().hashCode() % 3600;
		if (delaySec < 0) {
			delaySec += 3600;
		}
		return delaySec;
	}
}