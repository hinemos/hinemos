/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.notify.model;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://notify.ws.clustercontrol.com")
@MappedSuperclass
public abstract class NotifyInfoDetail implements java.io.Serializable {
	private static final long serialVersionUID = 8895768556582532893L;

	/** 通知ID。 */
	private String notifyId;
	
	private NotifyInfo notifyInfoEntity;

	/**
	 * 通知フラグ。
	 * @see com.clustercontrol.bean.ValidConstant
	 */
	private Boolean infoValidFlg;
	private Boolean warnValidFlg;
	private Boolean criticalValidFlg;
	private Boolean unknownValidFlg;

	public NotifyInfoDetail() {
	}

	public NotifyInfoDetail(String notifyId) {
		this.notifyId = notifyId;
	}
	
	/**
	 * 通知IDを返します
	 * @return
	 */
	@XmlTransient
	@Id
	@Column(name="notify_id")
	public String getNotifyId() {
		if (notifyId == null && notifyInfoEntity != null)
			notifyId = notifyInfoEntity.getNotifyId();
		return notifyId;
	}

	/**
	 * 通知IDを設定します。
	 * @param id
	 */
	public void setNotifyId(String notifyId) {
		this.notifyId = notifyId;
	}

	@Column(name="info_valid_flg")
	public Boolean getInfoValidFlg() {
		return infoValidFlg;
	}

	public void setInfoValidFlg(Boolean infoValidFlg) {
		this.infoValidFlg = infoValidFlg;
	}

	@Column(name="warn_valid_flg")
	public Boolean getWarnValidFlg() {
		return warnValidFlg;
	}

	public void setWarnValidFlg(Boolean warnValidFlg) {
		this.warnValidFlg = warnValidFlg;
	}

	@Column(name="critical_valid_flg")
	public Boolean getCriticalValidFlg() {
		return criticalValidFlg;
	}

	public void setCriticalValidFlg(Boolean criticalValidFlg) {
		this.criticalValidFlg = criticalValidFlg;
	}

	@Column(name="unknown_valid_flg")
	public Boolean getUnknownValidFlg() {
		return unknownValidFlg;
	}

	public void setUnknownValidFlg(Boolean unknownValidFlg) {
		this.unknownValidFlg = unknownValidFlg;
	}
	
	//bi-directional one-to-one association to NotifyInfoEntity
	@XmlTransient
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="notify_id", insertable=false, updatable=false)
	public NotifyInfo getNotifyInfoEntity() {
		return this.notifyInfoEntity;
	}

	@Deprecated
	public void setNotifyInfoEntity(NotifyInfo notifyInfoEntity) {
		this.notifyInfoEntity = notifyInfoEntity;
	}
	
	protected abstract void chainNotifyInfo();
	
	protected abstract void unchainNotifyInfo();

	/**
	 * NotifyInfoEntityオブジェクト参照設定<BR>
	 * 
	 * NotifyInfoEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToNotifyInfoEntity(NotifyInfo notifyInfoEntity) {
		this.setNotifyInfoEntity(notifyInfoEntity);
		if (notifyInfoEntity != null) {
			chainNotifyInfo();
		}
	}

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
		// NotifyInfoEntity
		if (this.notifyInfoEntity != null) {
			unchainNotifyInfo();
		}
	}
}
