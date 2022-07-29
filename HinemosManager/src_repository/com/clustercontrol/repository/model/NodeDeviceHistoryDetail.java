/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.repository.bean.NodeConfigSettingConstant;

/**
 * このクラスは、リポジトリプロパティ[デバイス]の履歴詳細クラスです。
 * 
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@MappedSuperclass
public abstract class NodeDeviceHistoryDetail implements Serializable, NodeHistoryDetail {
	private static final long serialVersionUID = 1L;

	/** メンバ変数 */
	private NodeDeviceHistoryDetailPK id;
	private Long regDateTo = NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE;
	private String deviceDisplayName = "";
	private Long deviceSize = 0L;
	private String deviceSizeUnit = "";
	private String deviceDescription = "";
	private String regUser = "";

	/**
	 * 空のコンストラクタです。setterで要素を追加して下さい。
	 */
	public NodeDeviceHistoryDetail() {
	}

	public NodeDeviceHistoryDetail(
			NodeDeviceHistoryDetailPK pk) {
		setFacilityId(pk.getFacilityId());
		setDeviceType(pk.getDeviceType());
		setDeviceIndex(pk.getDeviceIndex());
		setDeviceName(pk.getDeviceName());
		setRegDate(pk.getRegDate());
	}
	
	public NodeDeviceHistoryDetail(
			String facilityId,
			Integer deviceIndex, 
			String deviceType,
			String deviceName,
			Long regDate) {
		setFacilityId(facilityId);
		setDeviceType(deviceType);
		setDeviceIndex(deviceIndex);
		setDeviceName(deviceName);
		setRegDate(regDate);
	}
	
	/**
	 * コンストラクトする際に、値をセットする事ができます。
	 * 
	 * @param deviceType
	 * @param deviceIndex
	 * @param deviceName
	 * @param regDate
	 * @param deviceDisplayName
	 * @param deviceSize
	 * @param deviceSizeUnit
	 * @param deviceDescription
	 * @param regUser
	 */
	public NodeDeviceHistoryDetail(String deviceType,
			Integer deviceIndex, String deviceName,
			Long regDate,
			String deviceDisplayName, Long deviceSize,
			String deviceSizeUnit, String deviceDescription,
			String regUser) {
		setDeviceType(deviceType);
		setDeviceIndex(deviceIndex);
		setDeviceName(deviceName);
		setRegDate(regDate);
		setDeviceDisplayName(deviceDisplayName);
		setDeviceSize(deviceSize);
		setDeviceSizeUnit(deviceSizeUnit);
		setDeviceDescription(deviceDescription);
		setRegUser(regUser);
	}
	
	@EmbeddedId
	@XmlTransient
	public NodeDeviceHistoryDetailPK getId() {
		if (id == null) {
			id = new NodeDeviceHistoryDetailPK();
		}
		return this.id;
	}

	public void setId(NodeDeviceHistoryDetailPK id) {
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

	@Transient
	public Long getRegDate() {
		return getId().getRegDate();
	}

	public void setRegDate(Long regDate) {
		getId().setRegDate(regDate);
	}

	@Column(name = "reg_date_to")
	public Long getRegDateTo() {
		return regDateTo;
	}

	public void setRegDateTo(Long regDateTo) {
		this.regDateTo = regDateTo;
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
	public Long getDeviceSize() {
		return this.deviceSize;
	}

	/**
	 * デバイスサイズの設定
	 * 
	 * @param deviceSize
	 */
	public void setDeviceSize(Long deviceSize) {
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

	/**
	 * 登録ユーザIDのsetterです。
	 * 
	 * @return String
	 */
	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	/**
	 * 登録ユーザIDのgetterです。
	 * 
	 * @param regUser
	 */
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
}