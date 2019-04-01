/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * このクラスは、リポジトリプロパティ[デバイス]のクラスです。 NodeDataクラスのメンバ変数として利用されます。
 * 
 * @since 0.8
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@MappedSuperclass
public abstract class NodeDeviceInfo implements Serializable, Cloneable {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -8539796929718344077L;

	/** メンバ変数 */
	private NodeDeviceInfoPK id;
	private String deviceDisplayName = "";
	private Integer deviceSize = 0;
	private String deviceSizeUnit = "";
	private String deviceDescription = "";

	/**
	 * 空のコンストラクタです。setterで要素を追加して下さい。
	 */
	public NodeDeviceInfo() {
	}

	public NodeDeviceInfo(
			NodeDeviceInfoPK pk) {
		setFacilityId(pk.getFacilityId());
		setDeviceType(pk.getDeviceType());
		setDeviceIndex(pk.getDeviceIndex());
		setDeviceName(pk.getDeviceName());
	}
	
	public NodeDeviceInfo(
			String facilityId,
			Integer deviceIndex, 
			String deviceType,
			String deviceName) {
		setFacilityId(facilityId);
		setDeviceType(deviceType);
		setDeviceIndex(deviceIndex);
		setDeviceName(deviceName);
	}
	
	/**
	 * コンストラクトする際に、値をセットする事ができます。
	 * 
	 * @param deviceType
	 * @param deviceIndex
	 * @param deviceName
	 * @param deviceDisplayName
	 * @param deviceSize
	 * @param deviceSizeUnit
	 * @param deviceDescription
	 */
	public NodeDeviceInfo(String deviceType,
			Integer deviceIndex, String deviceName,
			String deviceDisplayName, Integer deviceSize,
			String deviceSizeUnit, String deviceDescription) {
		setDeviceType(deviceType);
		setDeviceIndex(deviceIndex);
		setDeviceName(deviceName);
		setDeviceDisplayName(deviceDisplayName);
		setDeviceSize(deviceSize);
		setDeviceSizeUnit(deviceSizeUnit);
		setDeviceDescription(deviceDescription);
	}

	/**
	 * NodeDeviceDataインスタンスのコピーを生成する時に利用します。
	 * 
	 * @param otherData
	 */
	public NodeDeviceInfo(NodeDeviceInfo otherData) {
		setDeviceType(otherData.getDeviceType());
		setDeviceIndex(otherData.getDeviceIndex());
		setDeviceName(otherData.getDeviceName());
		setDeviceDisplayName(otherData.getDeviceDisplayName());
		setDeviceSize(otherData.getDeviceSize());
		setDeviceSizeUnit(otherData.getDeviceSizeUnit());
		setDeviceDescription(otherData.getDeviceDescription());

	}
	
	@EmbeddedId
	@XmlTransient
	public NodeDeviceInfoPK getId() {
		if (id == null)
			id = new NodeDeviceInfoPK();
		return this.id;
	}

	public void setId(NodeDeviceInfoPK id) {
		this.id = id;
	}
	
	@XmlTransient
	@Transient
	public String getFacilityId() {
		return getId().getFacilityId();
	}

	public void setFacilityId(String facilityId) {
		getId().setFacilityId(facilityId);
	}

	@Transient
	public Integer getDeviceIndex() {
		return getId().getDeviceIndex();
	}

	public void setDeviceIndex(Integer deviceIndex) {
		getId().setDeviceIndex(deviceIndex);
	}

	@Transient
	public String getDeviceType() {
		return getId().getDeviceType();
	}

	public void setDeviceType(String deviceType) {
		getId().setDeviceType(deviceType);
	}

	@Transient
	public String getDeviceName() {
		return getId().getDeviceName();
	}

	public void setDeviceName(String deviceName) {
		getId().setDeviceName(deviceName);
	}

	/**
	 * デバイス表示名のgetterです。
	 * 
	 * @return String
	 */
	@Column(name="device_display_name")
	public String getDeviceDisplayName() {
		return this.deviceDisplayName;
	}

	/**
	 * デバイス表示名のsetterです。デバイス表示名はnot nullです。
	 * 
	 * @param deviceDisplayName
	 */
	public void setDeviceDisplayName(String deviceDisplayName) {
		this.deviceDisplayName = deviceDisplayName;
	}

	/**
	 * デバイスサイズの取得
	 * 
	 * @return Integer
	 */
	@Column(name="device_size")
	public Integer getDeviceSize() {
		return this.deviceSize;
	}

	/**
	 * デバイスサイズの設定
	 * 
	 * @param deviceSize
	 */
	public void setDeviceSize(Integer deviceSize) {
		this.deviceSize = deviceSize;
	}

	/**
	 * デバイスサイズの単位の取得
	 * 
	 * @return String
	 */
	@Column(name="device_size_unit")
	public String getDeviceSizeUnit() {
		return this.deviceSizeUnit;
	}

	/**
	 * デバイスサイズの単位の設定
	 * 
	 * @param deviceSizeUnit
	 */
	public void setDeviceSizeUnit(String deviceSizeUnit) {
		this.deviceSizeUnit = deviceSizeUnit;
	}

	/**
	 * デバイス説明のsetterです。
	 * 
	 * @return String
	 */
	@Column(name="device_description")
	public String getDeviceDescription() {
		return this.deviceDescription;
	}

	/**
	 * デバイス説明のgetterです。
	 * 
	 * @param deviceDescription
	 */
	public void setDeviceDescription(String deviceDescription) {
		this.deviceDescription = deviceDescription;
	}

	@Override
	public String toString() {
		return "NodeDeviceInfo ["
				+ "id=" + id
				+ ", deviceDisplayName=" + deviceDisplayName
				+ ", deviceSize=" + deviceSize
				+ ", deviceSizeUnit=" + deviceSizeUnit
				+ ", deviceDescription=" + deviceDescription
				+ "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((deviceDescription == null) ? 0 : deviceDescription
						.hashCode());
		result = prime
				* result
				+ ((deviceDisplayName == null) ? 0 : deviceDisplayName
						.hashCode());
		result = prime * result
				+ ((deviceSize == null) ? 0 : deviceSize.hashCode());
		result = prime * result
				+ ((deviceSizeUnit == null) ? 0 : deviceSizeUnit.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeDeviceInfo other = (NodeDeviceInfo) obj;
		if (deviceDescription == null) {
			if (other.deviceDescription != null)
				return false;
		} else if (!deviceDescription.equals(other.deviceDescription))
			return false;
		if (deviceDisplayName == null) {
			if (other.deviceDisplayName != null)
				return false;
		} else if (!deviceDisplayName.equals(other.deviceDisplayName))
			return false;
		if (deviceSize == null) {
			if (other.deviceSize != null)
				return false;
		} else if (!deviceSize.equals(other.deviceSize))
			return false;
		if (deviceSizeUnit == null) {
			if (other.deviceSizeUnit != null)
				return false;
		} else if (!deviceSizeUnit.equals(other.deviceSizeUnit))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public NodeDeviceInfo clone() {
		try {
			NodeDeviceInfo cloneInfo = (NodeDeviceInfo)super.clone();
			cloneInfo.id = this.id.clone();
			cloneInfo.deviceDisplayName = this.deviceDisplayName;
			cloneInfo.deviceSize = this.deviceSize;
			cloneInfo.deviceSizeUnit = this.deviceSizeUnit;
			cloneInfo.deviceDescription = this.deviceDescription;
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}

}