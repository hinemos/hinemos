/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyCloudPlatformTypeEnum;

public class CloudNotifyDetailInfoResponse {

	private Boolean infoValidFlg;

	private Boolean warnValidFlg;

	private Boolean criticalValidFlg;

	private Boolean unknownValidFlg;

	private String facilityId;
	// 0-AWS, 1-Azure
	@RestBeanConvertEnum
	private NotifyCloudPlatformTypeEnum platformType;
	
	/**転送先スコープテキスト*/
	@RestPartiallyTransrateTarget
	private String	textScope;	
	
	
	// Azureの場合エンドポイント
	private String infoEventBus;
	// Azureの場合サブジェクト
	private String infoDetailType;
	// Azureの場合イベントタイプ
	private String infoSource;
	//以下Azureのみ
	private String infoDataVersion;
	private String infoAccessKey;
	// jsonデータでディテールおよびデータを保存
	@RestPartiallyTransrateTarget
	private List<CloudNotifyLinkInfoKeyValueObjectResponse> infoKeyValueDataList;
	
	// Azureの場合エンドポイント
	private String warnEventBus;
	// Azureの場合サブジェクト
	private String warnDetailType;
	// Azureの場合イベントタイプ
	private String warnSource;
	//以下Azureのみ
	private String warnDataVersion;
	private String warnAccessKey;
	// jsonデータでディテールおよびデータを保存
	@RestPartiallyTransrateTarget
	private List<CloudNotifyLinkInfoKeyValueObjectResponse> warnKeyValueDataList;
	
	// Azureの場合エンドポイント
	private String critEventBus;
	// Azureの場合サブジェクト
	private String critDetailType;
	// Azureの場合イベントタイプ
	private String critSource;
	//以下Azureのみ
	private String critDataVersion;
	private String critAccessKey;
	// jsonデータでディテールおよびデータを保存
	@RestPartiallyTransrateTarget
	private List<CloudNotifyLinkInfoKeyValueObjectResponse> critKeyValueDataList;
	
	// Azureの場合エンドポイント
	private String unkEventBus;
	// Azureの場合サブジェクト
	private String unkDetailType;
	// Azureの場合イベントタイプ
	private String unkSource;
	//以下Azureのみ
	private String unkDataVersion;
	private String unkAccessKey;
	// jsonデータでディテールおよびデータを保存
	@RestPartiallyTransrateTarget
	private List<CloudNotifyLinkInfoKeyValueObjectResponse> unkKeyValueDataList;
	
	
	public Boolean getInfoValidFlg() {
		return infoValidFlg;
	}
	public void setInfoValidFlg(Boolean infoValidFlg) {
		this.infoValidFlg = infoValidFlg;
	}
	public Boolean getWarnValidFlg() {
		return warnValidFlg;
	}
	public void setWarnValidFlg(Boolean warnValidFlg) {
		this.warnValidFlg = warnValidFlg;
	}
	public Boolean getCriticalValidFlg() {
		return criticalValidFlg;
	}
	public void setCriticalValidFlg(Boolean criticalValidFlg) {
		this.criticalValidFlg = criticalValidFlg;
	}
	public Boolean getUnknownValidFlg() {
		return unknownValidFlg;
	}
	public void setUnknownValidFlg(Boolean unknownValidFlg) {
		this.unknownValidFlg = unknownValidFlg;
	}
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	public NotifyCloudPlatformTypeEnum getPlatformType() {
		return platformType;
	}
	public void setPlatformType(NotifyCloudPlatformTypeEnum platformType) {
		this.platformType = platformType;
	}
	public String getTextScope() {
		return textScope;
	}
	public void setTextScope(String textScope) {
		this.textScope = textScope;
	}
	public String getInfoEventBus() {
		return infoEventBus;
	}
	public void setInfoEventBus(String infoEventBus) {
		this.infoEventBus = infoEventBus;
	}
	public String getInfoDetailType() {
		return infoDetailType;
	}
	public void setInfoDetailType(String infoDetailType) {
		this.infoDetailType = infoDetailType;
	}
	public String getInfoSource() {
		return infoSource;
	}
	public void setInfoSource(String infoSource) {
		this.infoSource = infoSource;
	}
	public String getInfoDataVersion() {
		return infoDataVersion;
	}
	public void setInfoDataVersion(String infoDataVersion) {
		this.infoDataVersion = infoDataVersion;
	}
	public String getInfoAccessKey() {
		return infoAccessKey;
	}
	public void setInfoAccessKey(String infoAccessKey) {
		this.infoAccessKey = infoAccessKey;
	}
	public List<CloudNotifyLinkInfoKeyValueObjectResponse> getInfoKeyValueDataList() {
		return infoKeyValueDataList;
	}
	public void setInfoKeyValueDataList(List<CloudNotifyLinkInfoKeyValueObjectResponse> infoKeyValueDataList) {
		this.infoKeyValueDataList = infoKeyValueDataList;
	}
	public String getWarnEventBus() {
		return warnEventBus;
	}
	public void setWarnEventBus(String warnEventBus) {
		this.warnEventBus = warnEventBus;
	}
	public String getWarnDetailType() {
		return warnDetailType;
	}
	public void setWarnDetailType(String warnDetailType) {
		this.warnDetailType = warnDetailType;
	}
	public String getWarnSource() {
		return warnSource;
	}
	public void setWarnSource(String warnSource) {
		this.warnSource = warnSource;
	}
	public String getWarnDataVersion() {
		return warnDataVersion;
	}
	public void setWarnDataVersion(String warnDataVersion) {
		this.warnDataVersion = warnDataVersion;
	}
	public String getWarnAccessKey() {
		return warnAccessKey;
	}
	public void setWarnAccessKey(String warnAccessKey) {
		this.warnAccessKey = warnAccessKey;
	}
	public List<CloudNotifyLinkInfoKeyValueObjectResponse> getWarnKeyValueDataList() {
		return warnKeyValueDataList;
	}
	public void setWarnKeyValueDataList(List<CloudNotifyLinkInfoKeyValueObjectResponse> warnKeyValueDataList) {
		this.warnKeyValueDataList = warnKeyValueDataList;
	}
	public String getCritEventBus() {
		return critEventBus;
	}
	public void setCritEventBus(String critEventBus) {
		this.critEventBus = critEventBus;
	}
	public String getCritDetailType() {
		return critDetailType;
	}
	public void setCritDetailType(String critDetailType) {
		this.critDetailType = critDetailType;
	}
	public String getCritSource() {
		return critSource;
	}
	public void setCritSource(String critSource) {
		this.critSource = critSource;
	}
	public String getCritDataVersion() {
		return critDataVersion;
	}
	public void setCritDataVersion(String critDataVersion) {
		this.critDataVersion = critDataVersion;
	}
	public String getCritAccessKey() {
		return critAccessKey;
	}
	public void setCritAccessKey(String critAccessKey) {
		this.critAccessKey = critAccessKey;
	}
	public List<CloudNotifyLinkInfoKeyValueObjectResponse> getCritKeyValueDataList() {
		return critKeyValueDataList;
	}
	public void setCritKeyValueDataList(List<CloudNotifyLinkInfoKeyValueObjectResponse> critKeyValueDataList) {
		this.critKeyValueDataList = critKeyValueDataList;
	}
	public String getUnkEventBus() {
		return unkEventBus;
	}
	public void setUnkEventBus(String unkEventBus) {
		this.unkEventBus = unkEventBus;
	}
	public String getUnkDetailType() {
		return unkDetailType;
	}
	public void setUnkDetailType(String unkDetailType) {
		this.unkDetailType = unkDetailType;
	}
	public String getUnkSource() {
		return unkSource;
	}
	public void setUnkSource(String unkSource) {
		this.unkSource = unkSource;
	}
	public String getUnkDataVersion() {
		return unkDataVersion;
	}
	public void setUnkDataVersion(String unkDataVersion) {
		this.unkDataVersion = unkDataVersion;
	}
	public String getUnkAccessKey() {
		return unkAccessKey;
	}
	public void setUnkAccessKey(String unkAccessKey) {
		this.unkAccessKey = unkAccessKey;
	}
	public List<CloudNotifyLinkInfoKeyValueObjectResponse> getUnkKeyValueDataList() {
		return unkKeyValueDataList;
	}
	public void setUnkKeyValueDataList(List<CloudNotifyLinkInfoKeyValueObjectResponse> unkKeyValueDataList) {
		this.unkKeyValueDataList = unkKeyValueDataList;
	}

	
}
