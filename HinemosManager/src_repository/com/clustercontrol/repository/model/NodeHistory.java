/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * The persistent class for the cc_node_XXX_history database table.
 *
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name = "cc_node_history", schema = "log")
@Cacheable(false)
public class NodeHistory implements Serializable, Cloneable {

	// default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private NodeHistoryPK id;
	private Boolean osFlag = false;
	private Boolean cpuFlag = false;
	private Boolean memoryFlag = false;
	private Boolean networkInterfaceFlag = false;
	private Boolean diskFlag = false;
	private Boolean filesystemFlag = false;
	private Boolean nodeVariableFlag = false;
	private Boolean hostnameFlag = false;
	private Boolean netstatFlag = false;
	private Boolean packageFlag = false;
	private Boolean productFlag = false;
	private Boolean licenseFlag = false;
	private Boolean customFlag = false;
	private String regUser = "";

	/**
	 * デフォルト値生成コンストラクタ.
	 */
	public NodeHistory() {
	}

	/**
	 * キー値指定コンストラクタ.
	 */
	public NodeHistory(String facilityId, Long regDate) {
		this(new NodeHistoryPK(facilityId, regDate));
	}

	/**
	 * キーオブジェクト指定コンストラクタ.
	 */
	public NodeHistory(NodeHistoryPK id) {
		this.id = id;
	}

	@XmlTransient
	@EmbeddedId
	public NodeHistoryPK getId() {
		if (id == null)
			id = new NodeHistoryPK();
		return id;
	}
	public void setId(NodeHistoryPK id) {
		this.id = id;
	}

	@Column(name = "os_flag")
	public Boolean getOsFlag() {
		return osFlag;
	}
	public void setOsFlag(Boolean osFlag) {
		this.osFlag = osFlag;
	}

	@Column(name = "cpu_flag")
	public Boolean getCpuFlag() {
		return cpuFlag;
	}
	public void setCpuFlag(Boolean cpuFlag) {
		this.cpuFlag = cpuFlag;
	}

	@Column(name = "memory_flag")
	public Boolean getMemoryFlag() {
		return memoryFlag;
	}
	public void setMemoryFlag(Boolean memoryFlag) {
		this.memoryFlag = memoryFlag;
	}

	@Column(name = "network_interface_flag")
	public Boolean getNetworkInterfaceFlag() {
		return networkInterfaceFlag;
	}
	public void setNetworkInterfaceFlag(Boolean networkInterfaceFlag) {
		this.networkInterfaceFlag = networkInterfaceFlag;
	}

	@Column(name = "disk_flag")
	public Boolean getDiskFlag() {
		return diskFlag;
	}
	public void setDiskFlag(Boolean diskFlag) {
		this.diskFlag = diskFlag;
	}

	@Column(name = "filesystem_flag")
	public Boolean getFilesystemFlag() {
		return filesystemFlag;
	}
	public void setFilesystemFlag(Boolean filesystemFlag) {
		this.filesystemFlag = filesystemFlag;
	}

	@Column(name = "node_variable_flag")
	public Boolean getNodeVariableFlag() {
		return nodeVariableFlag;
	}
	public void setNodeVariableFlag(Boolean nodeVariableFlag) {
		this.nodeVariableFlag = nodeVariableFlag;
	}

	@Column(name = "hostname_flag")
	public Boolean getHostnameFlag() {
		return hostnameFlag;
	}
	public void setHostnameFlag(Boolean hostnameFlag) {
		this.hostnameFlag = hostnameFlag;
	}

	@Column(name = "netstat_flag")
	public Boolean getNetstatFlag() {
		return netstatFlag;
	}
	public void setNetstatFlag(Boolean netstatFlag) {
		this.netstatFlag = netstatFlag;
	}

	@Column(name = "package_flag")
	public Boolean getPackageFlag() {
		return packageFlag;
	}
	public void setPackageFlag(Boolean packageFlag) {
		this.packageFlag = packageFlag;
	}

	@Column(name = "product_flag")
	public Boolean getProductFlag() {
		return productFlag;
	}
	public void setProductFlag(Boolean productFlag) {
		this.productFlag = productFlag;
	}

	@Column(name = "license_flag")
	public Boolean getLicenseFlag() {
		return licenseFlag;
	}
	public void setLicenseFlag(Boolean licenseFlag) {
		this.licenseFlag = licenseFlag;
	}

	@Column(name = "custom_flag")
	public Boolean getCustomFlag() {
		return customFlag;
	}
	public void setCustomFlag(Boolean customFlag) {
		this.customFlag = customFlag;
	}
	
	@Column(name = "reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((osFlag == null) ? 0 : osFlag.hashCode());
		result = prime * result + ((memoryFlag == null) ? 0 : memoryFlag.hashCode());
		result = prime * result + ((networkInterfaceFlag == null) ? 0 : networkInterfaceFlag.hashCode());
		result = prime * result + ((diskFlag == null) ? 0 : diskFlag.hashCode());
		result = prime * result + ((filesystemFlag == null) ? 0 : filesystemFlag.hashCode());
		result = prime * result + ((nodeVariableFlag == null) ? 0 : nodeVariableFlag.hashCode());
		result = prime * result + ((hostnameFlag == null) ? 0 : hostnameFlag.hashCode());
		result = prime * result + ((netstatFlag == null) ? 0 : netstatFlag.hashCode());
		result = prime * result + ((packageFlag == null) ? 0 : packageFlag.hashCode());
		result = prime * result + ((productFlag == null) ? 0 : productFlag.hashCode());
		result = prime * result + ((customFlag == null) ? 0 : customFlag.hashCode());
		result = prime * result + ((regUser == null) ? 0 : regUser.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof NodeHistory)) {
			return false;
		}

		NodeHistory other = (NodeHistory) obj;
		if ((id == null && other.id != null) 
				|| (id != null && !id.equals(other.id))) {
			return false;
		}
		if ((osFlag == null && other.osFlag != null) 
				|| (osFlag != null && !osFlag.equals(other.osFlag))) {
			return false;
		}
		if ((cpuFlag == null && other.cpuFlag != null) 
				|| (cpuFlag != null && !cpuFlag.equals(other.cpuFlag))) {
			return false;
		}
		if ((memoryFlag == null && other.memoryFlag != null) 
				|| (memoryFlag != null && !memoryFlag.equals(other.memoryFlag))) {
			return false;
		}
		if ((networkInterfaceFlag == null && other.networkInterfaceFlag != null) 
				|| (networkInterfaceFlag != null && !networkInterfaceFlag.equals(other.networkInterfaceFlag))) {
			return false;
		}
		if ((diskFlag == null && other.diskFlag != null) 
				|| (diskFlag != null && !diskFlag.equals(other.diskFlag))) {
			return false;
		}
		if ((filesystemFlag == null && other.filesystemFlag != null) 
				|| (filesystemFlag != null && !filesystemFlag.equals(other.filesystemFlag))) {
			return false;
		}
		if ((nodeVariableFlag == null && other.nodeVariableFlag != null) 
				|| (nodeVariableFlag != null && !nodeVariableFlag.equals(other.nodeVariableFlag))) {
			return false;
		}
		if ((hostnameFlag == null && other.hostnameFlag != null) 
				|| (hostnameFlag != null && !hostnameFlag.equals(other.hostnameFlag))) {
			return false;
		}
		if ((netstatFlag == null && other.netstatFlag != null) 
				|| (netstatFlag != null && !netstatFlag.equals(other.netstatFlag))) {
			return false;
		}
		if ((packageFlag == null && other.packageFlag != null) 
				|| (packageFlag != null && !packageFlag.equals(other.packageFlag))) {
			return false;
		}
		if ((productFlag == null && other.productFlag != null) 
				|| (productFlag != null && !productFlag.equals(other.productFlag))) {
			return false;
		}
		if ((licenseFlag == null && other.licenseFlag != null) 
				|| (licenseFlag != null && !licenseFlag.equals(other.licenseFlag))) {
			return false;
		}
		if ((customFlag == null && other.customFlag != null) 
				|| (customFlag != null && !customFlag.equals(other.customFlag))) {
			return false;
		}

		if ((regUser == null && other.regUser != null) 
				|| (regUser != null && !regUser.equals(other.regUser))) {
			return false;
		}
		return true;
	}

	@Override
	public NodeHistory clone() {
		try {
			NodeHistory cloneInfo = (NodeHistory) super.clone();
			cloneInfo.id = this.id;
			cloneInfo.osFlag = this.osFlag;
			cloneInfo.cpuFlag = this.cpuFlag;
			cloneInfo.memoryFlag = this.memoryFlag;
			cloneInfo.networkInterfaceFlag = this.networkInterfaceFlag;
			cloneInfo.diskFlag = this.diskFlag;
			cloneInfo.filesystemFlag = this.filesystemFlag;
			cloneInfo.nodeVariableFlag = this.nodeVariableFlag;
			cloneInfo.hostnameFlag = this.hostnameFlag;
			cloneInfo.netstatFlag = this.netstatFlag;
			cloneInfo.packageFlag = this.packageFlag;
			cloneInfo.productFlag = this.productFlag;
			cloneInfo.licenseFlag = this.licenseFlag;
			cloneInfo.customFlag = this.customFlag;
			cloneInfo.regUser = this.regUser;
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.getMessage());
		}
	}
}
