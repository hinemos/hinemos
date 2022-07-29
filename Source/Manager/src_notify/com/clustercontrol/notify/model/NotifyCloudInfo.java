/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.xcloud.common.CloudCryptKey;
import com.clustercontrol.xcloud.util.EncryptionUtil;


/**
 * The persistent class for the cc_notify_cloud_info database table.
 * 
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
@Entity
@Table(name="cc_notify_cloud_info", schema="setting")
@Cacheable(true)
public class NotifyCloudInfo extends NotifyInfoDetail implements Serializable {
	private static final long serialVersionUID = 1L;

	private String facilityId;
	// 0-AWS, 1-Azure
	private Integer platformType;
	
	/**転送先スコープテキスト*/
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
	private String infoJsonData;
	
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
	private String warnJsonData;
	
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
	private String critJsonData;
	
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
	private String unkJsonData;

	private String infoAccessKeyCrypt;

	private String warnAccessKeyCrypt;

	private String critAccessKeyCrypt;

	private String unkAccessKeyCrypt;

	
	public void setFacilityId(String facilityId){
		this.facilityId=facilityId;
	}
	@Column(name="facility_id")
	public String getFacilityId() {
		return facilityId;
	}
	
	
	public void setTextScope(String textScope) {
		this.textScope = textScope;
	}
	@Column(name="text_scope")
	public String getTextScope() {
		return textScope;
	}


	
	public void setPlatformType(Integer platformType) {
		this.platformType = platformType;
	}
	@Column(name="platform_type")
	public Integer getPlatformType() {
		return platformType;
	}

	@Column(name="info_event_bus")
	public String getInfoEventBus() {
		return infoEventBus;
	}
	
	public void setInfoEventBus(String infoEventBus) {
		this.infoEventBus = infoEventBus;
	}
	@Column(name="info_detail_type")
	public String getInfoDetailType() {
		return infoDetailType;
	}
	
	
	public void setInfoDetailType(String infoDetailType) {
		this.infoDetailType = infoDetailType;
	}
	@Column(name="info_source")
	public String getInfoSource() {
		return infoSource;
	}
	
	public void setInfoSource(String infoSource) {
		this.infoSource = infoSource;
	}
	@Column(name="info_data_version")
	public String getInfoDataVersion() {
		return infoDataVersion;
	}
	
	public void setInfoDataVersion(String infoDataVersion) {
		this.infoDataVersion = infoDataVersion;
	}
	@Column(name="info_access_key")
	public String getInfoAccessKeyCrypt() {
		return this.infoAccessKeyCrypt;
	}
	
	public void setInfoAccessKeyCrypt(String infoAccessKey) {
		this.infoAccessKey = null;
		this.infoAccessKeyCrypt=infoAccessKey;
	}
	
	@Column(name="info_json_data")
	public String getInfoJsonData() {
		return infoJsonData;
	}
	
	public void setInfoJsonData(String infoJsonData) {
		this.infoJsonData = infoJsonData;
	}


	/**
	 * 警告
	 */
	@Column(name="warn_event_bus")
	public String getWarnEventBus() {
		return warnEventBus;
	}
	
	public void setWarnEventBus(String warnEventBus) {
		this.warnEventBus = warnEventBus;
	}
	@Column(name="warn_detail_type")
	public String getWarnDetailType() {
		return warnDetailType;
	}
	
	public void setWarnDetailType(String warnDetailType) {
		this.warnDetailType = warnDetailType;
	}
	@Column(name="warn_source")
	public String getWarnSource() {
		return warnSource;
	}
	
	public void setWarnSource(String warnSource) {
		this.warnSource = warnSource;
	}
	@Column(name="warn_data_version")
	public String getWarnDataVersion() {
		return warnDataVersion;
	}
	
	public void setWarnDataVersion(String warnDataVersion) {
		this.warnDataVersion = warnDataVersion;
	}
	@Column(name="warn_access_key")
	public String getWarnAccessKeyCrypt() {
		return this.warnAccessKeyCrypt;
	}
	
	public void setWarnAccessKeyCrypt(String warnAccessKey) {
		this.warnAccessKey = null;
		this.warnAccessKeyCrypt=warnAccessKey;
	}
	@Column(name="warn_json_data")
	public String getWarnJsonData() {
		return warnJsonData;
	}
	public void setWarnJsonData(String warnJsonData) {
		this.warnJsonData = warnJsonData;
	}

	/**
	 * 危険
	 */
	@Column(name="critical_event_bus")
	public String getCritEventBus() {
		return critEventBus;
	}
	
	public void setCritEventBus(String critEventBus) {
		this.critEventBus = critEventBus;
	}
	@Column(name="critical_detail_type")
	public String getCritDetailType() {
		return critDetailType;
	}
	
	public void setCritDetailType(String critDetailType) {
		this.critDetailType = critDetailType;
	}
	@Column(name="critical_source")
	public String getCritSource() {
		return critSource;
	}
	
	public void setCritSource(String critSource) {
		this.critSource = critSource;
	}
	@Column(name="critical_data_version")
	public String getCritDataVersion() {
		return critDataVersion;
	}
	
	public void setCritDataVersion(String critDataVersion) {
		this.critDataVersion = critDataVersion;
	}
	@Column(name="critical_access_key")
	public String getCritAccessKeyCrypt() {
		return this.critAccessKeyCrypt;
	}
	
	public void setCritAccessKeyCrypt(String critAccessKey) {
		this.critAccessKey = null;
		this.critAccessKeyCrypt=critAccessKey;
	}
	@Column(name="critical_json_data")
	public String getCritJsonData() {
		return critJsonData;
	}
	
	public void setCritJsonData(String critJsonData) {
		this.critJsonData = critJsonData;
	}

	/**
	 * 不明
	 */
	@Column(name="unknown_event_bus")
	public String getUnkEventBus() {
		return unkEventBus;
	}
	
	public void setUnkEventBus(String unkEventBus) {
		this.unkEventBus = unkEventBus;
	}
	@Column(name="unknown_detail_type")
	public String getUnkDetailType() {
		return unkDetailType;
	}

	public void setUnkDetailType(String unkDetailType) {
		this.unkDetailType = unkDetailType;
	}
	@Column(name="unknown_source")
	public String getUnkSource() {
		return unkSource;
	}

	public void setUnkSource(String unkSource) {
		this.unkSource = unkSource;
	}
	
	@Column(name="unknown_data_version")
	public String getUnkDataVersion() {
		return unkDataVersion;
	}
	public void setUnkDataVersion(String unkDataVersion) {
		this.unkDataVersion = unkDataVersion;
	}
	@Column(name="unknown_access_key")
	public String getUnkAccessKeyCrypt() {
		return this.unkAccessKeyCrypt;
	}
	
	public void setUnkAccessKeyCrypt(String unkAccessKey) {
		this.unkAccessKey = null;
		this.unkAccessKeyCrypt=unkAccessKey;
	}
	@Column(name="unknown_json_data")
	public String getUnkJsonData() {
		return unkJsonData;
	}

	public void setUnkJsonData(String unkJsonData) {
		this.unkJsonData = unkJsonData;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}



	public NotifyCloudInfo() {
	}

	public NotifyCloudInfo(String notifyId) {
		super(notifyId);
	}

	@Transient
	public String getScopeText() {
		if (textScope == null)
			try {
				textScope = new RepositoryControllerBean().getFacilityPath(facilityId, null);
			} catch (HinemosUnknown e) {
				Logger.getLogger(this.getClass()).debug(e.getMessage(), e);
			}
		return textScope;
	}
	
	@Transient
	public String getInfoAccessKey() {
		if (this.infoAccessKey == null) {
			try {
				if (this.infoAccessKeyCrypt != null) {
					this.infoAccessKey = EncryptionUtil.decrypt(this.infoAccessKeyCrypt, CloudCryptKey.cryptKey);
				} else {
					this.infoAccessKey = null;
				}
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e.getMessage(), e);
			}
		}
		return this.infoAccessKey;
	}
	
	public void setInfoAccessKey(String infoAccessKey) {
		try {
			String cryptKey = null;
			if (infoAccessKey != null) {
				cryptKey = EncryptionUtil.crypt(infoAccessKey, CloudCryptKey.cryptKey);
			}
			setInfoAccessKeyCrypt(cryptKey);
			this.infoAccessKey = infoAccessKey;
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e.getMessage(), e);
		}
	}

	@Transient
	public String getWarnAccessKey()
	{
		if (this.warnAccessKey == null) {
			try {
				this.warnAccessKey = this.warnAccessKeyCrypt != null ?
						EncryptionUtil.decrypt(this.warnAccessKeyCrypt, CloudCryptKey.cryptKey): null;
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e.getMessage(), e);
			}
		}
		return this.warnAccessKey;
	}
	public void setWarnAccessKey( String warnAccessKey )
	{
		try {
			setWarnAccessKeyCrypt(warnAccessKey != null ? EncryptionUtil.crypt(warnAccessKey, CloudCryptKey.cryptKey): null);
			this.warnAccessKey = warnAccessKey;
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e.getMessage(), e);
		}
	}
	
	@Transient
	public String getCritAccessKey()
	{
		if (this.critAccessKey == null) {
			try {
				this.critAccessKey = this.critAccessKeyCrypt != null ?
						EncryptionUtil.decrypt(this.critAccessKeyCrypt, CloudCryptKey.cryptKey): null;
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e.getMessage(), e);
			}
		}
		return this.critAccessKey;
	}
	public void setCritAccessKey( String critAccessKey )
	{
		try {
			setCritAccessKeyCrypt(critAccessKey != null ? EncryptionUtil.crypt(critAccessKey, CloudCryptKey.cryptKey): null);
			this.critAccessKey = critAccessKey;
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e.getMessage(), e);
		}
	}
	
	
	@Transient
	public String getUnkAccessKey()
	{
		if (this.unkAccessKey == null) {
			try {
				this.unkAccessKey = this.unkAccessKeyCrypt != null ?
						EncryptionUtil.decrypt(this.unkAccessKeyCrypt, CloudCryptKey.cryptKey): null;
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e.getMessage(), e);
			}
		}
		return this.unkAccessKey;
	}
	
	public void setUnkAccessKey( String unkAccessKey )
	{
		try {
			setUnkAccessKeyCrypt(unkAccessKey != null ? EncryptionUtil.crypt(unkAccessKey, CloudCryptKey.cryptKey): null);
			this.unkAccessKey = unkAccessKey;
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e.getMessage(), e);
		}
	}
	
	
	
	
	
	@Override
	protected void chainNotifyInfo() {
		getNotifyInfoEntity().setNotifyCloudInfo(this);
	}

	@Override
	protected void unchainNotifyInfo() {
		getNotifyInfoEntity().setNotifyCloudInfo(null);
	}
}