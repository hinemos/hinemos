/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.notify.mail.model.MailTemplateInfo;


/**
 * The persistent class for the cc_notify_info database table.
 *
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
@Entity
@Table(name="cc_notify_info", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.PLATFORM_NOTIFY,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="notify_id", insertable=false, updatable=false))
public class NotifyInfo extends ObjectPrivilegeTargetInfo {
	private static final long serialVersionUID = 1L;
	private String notifyId;
	private String description;
	private Integer initialCount;
	private Boolean notFirstNotify;
	private Integer notifyType;
	private Long regDate;
	private String regUser;
	private Integer renotifyPeriod;
	private Integer renotifyType;
	private Long updateDate;
	private String updateUser;
	private Boolean validFlg;
	private String calendarId;
	private NotifyCommandInfo notifyCommandInfo;
	private NotifyEventInfo notifyEventInfo;
	private NotifyJobInfo notifyJobInfo;
	private NotifyLogEscalateInfo notifyLogEscalateInfo;
	private NotifyMailInfo notifyMailInfo;
	private NotifyStatusInfo notifyStatusInfo;
	private NotifyInfraInfo notifyInfraInfo;
	private NotifyRestInfo notifyRestInfo;
	private NotifyCloudInfo notifyCloudInfo;
	private NotifyMessageInfo notifyMessageInfo;


	@Deprecated
	public NotifyInfo() {
	}

	public NotifyInfo(String notifyId) {
		this.setNotifyId(notifyId);
	}


	@Id
	@Column(name="notify_id")
	public String getNotifyId() {
		return this.notifyId;
	}

	public void setNotifyId(String notifyId) {
		this.notifyId = notifyId;
		setObjectId(notifyId);
	}


	@Column(name="description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Column(name="initial_count")
	public Integer getInitialCount() {
		return this.initialCount;
	}

	public void setInitialCount(Integer initialCount) {
		this.initialCount = initialCount;
	}


	@Column(name="not_first_notify")
	public Boolean getNotFirstNotify() {
		return this.notFirstNotify;
	}

	public void setNotFirstNotify(Boolean notFirstNotify) {
		this.notFirstNotify = notFirstNotify;
	}


	@Column(name="notify_type")
	public Integer getNotifyType() {
		return this.notifyType;
	}

	public void setNotifyType(Integer notifyType) {
		this.notifyType = notifyType;
	}


	@Column(name="reg_date")
	public Long getRegDate() {
		return this.regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}


	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}


	@Column(name="renotify_period")
	public Integer getRenotifyPeriod() {
		return this.renotifyPeriod;
	}

	public void setRenotifyPeriod(Integer renotifyPeriod) {
		this.renotifyPeriod = renotifyPeriod;
	}


	@Column(name="renotify_type")
	public Integer getRenotifyType() {
		return this.renotifyType;
	}

	public void setRenotifyType(Integer renotifyType) {
		this.renotifyType = renotifyType;
	}


	@Column(name="update_date")
	public Long getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}


	@Column(name="update_user")
	public String getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}


	@Column(name="valid_flg")
	public Boolean getValidFlg() {
		return this.validFlg;
	}

	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}


	@Column(name="calendar_id")
	public String getCalendarId() {
		return this.calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}


	//bi-directional one-to-one association to NotifyCommandInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyCommandInfo getNotifyCommandInfo() {
		return this.notifyCommandInfo;
	}

	public void setNotifyCommandInfo(NotifyCommandInfo notifyCommandInfo) {
		this.notifyCommandInfo = notifyCommandInfo;
	}


	//bi-directional one-to-one association to NotifyEventInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyEventInfo getNotifyEventInfo() {
		return this.notifyEventInfo;
	}

	public void setNotifyEventInfo(NotifyEventInfo notifyEventInfo) {
		this.notifyEventInfo = notifyEventInfo;
	}


	//bi-directional one-to-one association to NotifyJobInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyJobInfo getNotifyJobInfo() {
		return this.notifyJobInfo;
	}

	public void setNotifyJobInfo(NotifyJobInfo notifyJobInfo) {
		this.notifyJobInfo = notifyJobInfo;
	}

	//bi-directional one-to-one association to NotifyJobInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyInfraInfo getNotifyInfraInfo() {
		return this.notifyInfraInfo;
	}

	public void setNotifyInfraInfo(NotifyInfraInfo notifyInfraInfo) {
		this.notifyInfraInfo = notifyInfraInfo;
	}

	//bi-directional one-to-one association to NotifyLogEscalateInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyLogEscalateInfo getNotifyLogEscalateInfo() {
		return this.notifyLogEscalateInfo;
	}

	public void setNotifyLogEscalateInfo(NotifyLogEscalateInfo notifyLogEscalateInfo) {
		this.notifyLogEscalateInfo = notifyLogEscalateInfo;
	}

	//bi-directional one-to-one association to NotifyMailInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyMailInfo getNotifyMailInfo() {
		return this.notifyMailInfo;
	}

	public void setNotifyMailInfo(NotifyMailInfo notifyMailInfo) {
		this.notifyMailInfo = notifyMailInfo;
	}


	//bi-directional one-to-one association to NotifyStatusInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyStatusInfo getNotifyStatusInfo() {
		return this.notifyStatusInfo;
	}

	public void setNotifyStatusInfo(NotifyStatusInfo notifyStatusInfo) {
		this.notifyStatusInfo = notifyStatusInfo;
	}

	//bi-directional one-to-one association to NotifyRestInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyRestInfo getNotifyRestInfo() {
		return this.notifyRestInfo;
	}

	public void setNotifyRestInfo(NotifyRestInfo notifyRestInfo) {
		this.notifyRestInfo = notifyRestInfo;
	}
	
	//bi-directional one-to-one association to NotifyStatusInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyCloudInfo getNotifyCloudInfo() {
		return this.notifyCloudInfo;
	}
	
	

	public void setNotifyCloudInfo(NotifyCloudInfo notifyCloudInfo) {
		this.notifyCloudInfo = notifyCloudInfo;
	}

	//bi-directional one-to-one association to NotifyMessageInfo
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyMessageInfo getNotifyMessageInfo() {
		return notifyMessageInfo;
	}

	public void setNotifyMessageInfo(NotifyMessageInfo notifyMessageInfo) {
		this.notifyMessageInfo = notifyMessageInfo;
	}

	public NotifyInfoDetail getNotifyInfoDetail() {
		switch (getNotifyType()) {
		case NotifyTypeConstant.TYPE_EVENT:
			return getNotifyEventInfo();
		case NotifyTypeConstant.TYPE_STATUS:
			return getNotifyStatusInfo();
		case NotifyTypeConstant.TYPE_MAIL:
			return getNotifyMailInfo();
		case NotifyTypeConstant.TYPE_JOB:
			return getNotifyJobInfo();
		case NotifyTypeConstant.TYPE_LOG_ESCALATE:
			return getNotifyLogEscalateInfo();
		case NotifyTypeConstant.TYPE_COMMAND:
			return getNotifyCommandInfo();
		case NotifyTypeConstant.TYPE_INFRA:
			return getNotifyInfraInfo();
		case NotifyTypeConstant.TYPE_REST:
			return getNotifyRestInfo();
		case NotifyTypeConstant.TYPE_CLOUD:
			return getNotifyCloudInfo();
		case NotifyTypeConstant.TYPE_MESSAGE:
			return getNotifyMessageInfo();
		default:
			String m = "setValidFlgtoDetail() notify type mismatch. " + getNotifyId() + "  type = " + getNotifyType();
			Logger.getLogger(this.getClass()).info(m);
			throw new InternalError(m);
		}
	}
	
	public void persistSelf() {
		if (notifyCommandInfo != null) {
			notifyCommandInfo.setNotifyId(getNotifyId());
			notifyCommandInfo.relateToNotifyInfoEntity(this);
		}
		if (notifyEventInfo != null) {
			notifyEventInfo.setNotifyId(getNotifyId());
			notifyEventInfo.relateToNotifyInfoEntity(this);
		}
		if (notifyJobInfo != null) {
			notifyJobInfo.setNotifyId(getNotifyId());
			notifyJobInfo.relateToNotifyInfoEntity(this);
		}
		if (notifyLogEscalateInfo != null) {
			notifyLogEscalateInfo.setNotifyId(getNotifyId());
			notifyLogEscalateInfo.relateToNotifyInfoEntity(this);
		}
		if (notifyMailInfo != null) {
			notifyMailInfo.setNotifyId(getNotifyId());
			notifyMailInfo.relateToNotifyInfoEntity(this);
			if (notifyMailInfo.getMailTemplateId() != null && !notifyMailInfo.getMailTemplateId().isEmpty()) {
				try {
					MailTemplateInfo mailTemplateInfoEntity
						= com.clustercontrol.notify.mail.util.QueryUtil.getMailTemplateInfoPK(notifyMailInfo.getMailTemplateId());
					notifyMailInfo.relateToMailTemplateInfoEntity(mailTemplateInfoEntity);
				} catch (MailTemplateNotFound e) {
					Logger.getLogger(this.getClass()).debug(e.getMessage(), e);
				} catch (InvalidRole e) {
					Logger.getLogger(this.getClass()).debug(e.getMessage(), e);
				}
			}
		}
		if (notifyStatusInfo != null) {
			notifyStatusInfo.setNotifyId(getNotifyId());
			notifyStatusInfo.relateToNotifyInfoEntity(this);
		}
		if (notifyInfraInfo != null) {
			notifyInfraInfo.setNotifyId(getNotifyId());
			notifyInfraInfo.relateToNotifyInfoEntity(this);
		}
		if (notifyRestInfo != null) {
			notifyRestInfo.setNotifyId(getNotifyId());
			notifyRestInfo.relateToNotifyInfoEntity(this);
		}
		if (notifyCloudInfo != null) {
			notifyCloudInfo.setNotifyId(getNotifyId());
			notifyCloudInfo.relateToNotifyInfoEntity(this);
		}
		if (notifyMessageInfo != null) {
			notifyMessageInfo.setNotifyId(getNotifyId());
			notifyMessageInfo.relateToNotifyInfoEntity(this);
		}
	}
}