/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.notify.mail.model.MailTemplateInfo;



/**
 * The persistent class for the cc_notify_mail_info database table.
 * 
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
@Entity
@Table(name="cc_notify_mail_info", schema="setting")
@Cacheable(true)
public class NotifyMailInfo extends NotifyInfoDetail implements Serializable {
	private static final long serialVersionUID = 1L;

	private String infoMailAddress;
	private String warnMailAddress;
	private String criticalMailAddress;
	private String unknownMailAddress;

	private MailTemplateInfo mailTemplateInfoEntity;

	private String mailTemplateId;
	
	public NotifyMailInfo() {
	}

	public NotifyMailInfo(String notifyId) {
		super(notifyId);
	}
	
	@Column(name="info_mail_address")
	public String getInfoMailAddress() {
		return this.infoMailAddress;
	}

	public void setInfoMailAddress(String infoMailAddress) {
		this.infoMailAddress = infoMailAddress;
	}


	@Column(name="warn_mail_address")
	public String getWarnMailAddress() {
		return this.warnMailAddress;
	}

	public void setWarnMailAddress(String warnMailAddress) {
		this.warnMailAddress = warnMailAddress;
	}


	@Column(name="critical_mail_address")
	public String getCriticalMailAddress() {
		return this.criticalMailAddress;
	}

	public void setCriticalMailAddress(String criticalMailAddress) {
		this.criticalMailAddress = criticalMailAddress;
	}


	@Column(name="unknown_mail_address")
	public String getUnknownMailAddress() {
		return this.unknownMailAddress;
	}

	public void setUnknownMailAddress(String unknownMailAddress) {
		this.unknownMailAddress = unknownMailAddress;
	}

	
	//bi-directional many-to-one association to MailTemplateInfoEntity
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="mail_template_id")
	public MailTemplateInfo getMailTemplateInfoEntity() {
		return this.mailTemplateInfoEntity;
	}

	@Deprecated
	public void setMailTemplateInfoEntity(MailTemplateInfo mailTemplateInfoEntity) {
		this.mailTemplateInfoEntity = mailTemplateInfoEntity;
	}

	/**
	 * MailTemplateInfoEntityオブジェクト参照設定<BR>
	 * 
	 * MailTemplateInfoEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToMailTemplateInfoEntity(MailTemplateInfo mailTemplateInfoEntity) {
		this.setMailTemplateInfoEntity(mailTemplateInfoEntity);
		if (mailTemplateInfoEntity != null) {
			List<NotifyMailInfo> list = mailTemplateInfoEntity.getNotifyMailInfoEntities();
			if (list == null) {
				list = new ArrayList<NotifyMailInfo>();
			} else {
				for(NotifyMailInfo entity : list){
					if (entity.getNotifyId().equals(this.getNotifyId())) {
						return;
					}
				}
			}
			list.add(this);
			mailTemplateInfoEntity.setNotifyMailInfoEntities(list);
		}
	}
	
	@Transient
	public String getMailTemplateId() {
		if (mailTemplateId == null && mailTemplateInfoEntity != null)
			mailTemplateId = mailTemplateInfoEntity.getMailTemplateId();
		return mailTemplateId;
	}

	public void setMailTemplateId(String mailTemplateId) {
		this.mailTemplateId = mailTemplateId;
	}
	

	@Override
	protected void chainNotifyInfo() {
		getNotifyInfoEntity().setNotifyMailInfo(this);
	}

	@Override
	protected void unchainNotifyInfo() {
		getNotifyInfoEntity().setNotifyMailInfo(null);
	}
}