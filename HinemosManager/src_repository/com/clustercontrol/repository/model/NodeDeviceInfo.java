/*

Copyright (C) since 2009 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.repository.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
	
	private NodeInfo nodeEntity;

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
		setFacilityId(deviceType);
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
		StringBuffer str = new StringBuffer("{");

		str.append("deviceType=" + getDeviceType() + " " + "deviceIndex="
				+ getDeviceIndex() + " " + "deviceName=" + getDeviceName()
				+ " " + "deviceDisplayName=" + getDeviceDisplayName() + " "
				+ "seviceSize=" + getDeviceSize() + " " + "seviceSizeUnit="
				+ getDeviceSizeUnit() + " " + "deviceDescription="
				+ getDeviceDescription());
		str.append('}');

		return (str.toString());
	}

	
	//bi-directional many-to-one association to nodeEntity
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="facility_id", insertable=false, updatable=false)
	public NodeInfo getNodeEntity() {
		return this.nodeEntity;
	}

	public void setNodeEntity(NodeInfo nodeEntity) {
		this.nodeEntity = nodeEntity;
	}

	/**
	 * nodeEntityオブジェクト参照設定<BR>
	 * 
	 * nodeEntity設定時はSetterに代わりこちらを使用すること。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public abstract void relateToNodeEntity(NodeInfo nodeEntity);

	/**
	 * 削除前処理<BR>
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void unchain() {
		// NodeEntity
		if (this.nodeEntity != null) {
			List<NodeFilesystemInfo> list = this.nodeEntity.getNodeFilesystemInfo();
			if (list != null) {
				Iterator<NodeFilesystemInfo> iter = list.iterator();
				while(iter.hasNext()) {
					NodeFilesystemInfo entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
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
		result = prime * result
				+ ((nodeEntity == null) ? 0 : nodeEntity.hashCode());
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
		if (nodeEntity == null) {
			if (other.nodeEntity != null)
				return false;
		} else if (!nodeEntity.equals(other.nodeEntity))
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