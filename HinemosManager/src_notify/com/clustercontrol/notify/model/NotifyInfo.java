package com.clustercontrol.notify.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
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
	private NotifyCommandInfo notifyCommandInfoEntity;
	private NotifyEventInfo notifyEventInfoEntity;
	private NotifyJobInfo notifyJobInfoEntity;
	private NotifyLogEscalateInfo notifyLogEscalateInfoEntity;
	private NotifyMailInfo notifyMailInfoEntity;
	private NotifyStatusInfo notifyStatusInfoEntity;
	private NotifyInfraInfo notifyInfraInfoEntity;

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
		return this.notifyCommandInfoEntity;
	}

	public void setNotifyCommandInfo(NotifyCommandInfo notifyCommandInfoEntity) {
		this.notifyCommandInfoEntity = notifyCommandInfoEntity;
	}


	//bi-directional one-to-one association to NotifyEventInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyEventInfo getNotifyEventInfo() {
		return this.notifyEventInfoEntity;
	}

	public void setNotifyEventInfo(NotifyEventInfo notifyEventInfoEntity) {
		this.notifyEventInfoEntity = notifyEventInfoEntity;
	}


	//bi-directional one-to-one association to NotifyJobInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyJobInfo getNotifyJobInfo() {
		return this.notifyJobInfoEntity;
	}

	public void setNotifyJobInfo(NotifyJobInfo notifyJobInfoEntity) {
		this.notifyJobInfoEntity = notifyJobInfoEntity;
	}

	//bi-directional one-to-one association to NotifyJobInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyInfraInfo getNotifyInfraInfo() {
		return this.notifyInfraInfoEntity;
	}

	public void setNotifyInfraInfo(NotifyInfraInfo notifyInfraInfoEntity) {
		this.notifyInfraInfoEntity = notifyInfraInfoEntity;
	}

	//bi-directional one-to-one association to NotifyLogEscalateInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyLogEscalateInfo getNotifyLogEscalateInfo() {
		return this.notifyLogEscalateInfoEntity;
	}

	public void setNotifyLogEscalateInfo(NotifyLogEscalateInfo notifyLogEscalateInfoEntity) {
		this.notifyLogEscalateInfoEntity = notifyLogEscalateInfoEntity;
	}

	//bi-directional one-to-one association to NotifyMailInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyMailInfo getNotifyMailInfo() {
		return this.notifyMailInfoEntity;
	}

	public void setNotifyMailInfo(NotifyMailInfo notifyMailInfoEntity) {
		this.notifyMailInfoEntity = notifyMailInfoEntity;
	}


	//bi-directional one-to-one association to NotifyStatusInfoEntity
	@OneToOne(mappedBy="notifyInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public NotifyStatusInfo getNotifyStatusInfo() {
		return this.notifyStatusInfoEntity;
	}

	public void setNotifyStatusInfo(NotifyStatusInfo notifyStatusInfoEntity) {
		this.notifyStatusInfoEntity = notifyStatusInfoEntity;
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
		default:
			String m = "setValidFlgtoDetail() notify type mismatch. " + getNotifyId() + "  type = " + getNotifyType();
			Logger.getLogger(this.getClass()).info(m);
			throw new InternalError(m);
		}
	}
	
	public void persistSelf(EntityManager em) {
		if (notifyCommandInfoEntity != null) {
			notifyCommandInfoEntity.setNotifyId(getNotifyId());
			notifyCommandInfoEntity.relateToNotifyInfoEntity(this);
		}
		if (notifyEventInfoEntity != null) {
			notifyEventInfoEntity.setNotifyId(getNotifyId());
			notifyEventInfoEntity.relateToNotifyInfoEntity(this);
		}
		if (notifyJobInfoEntity != null) {
			notifyJobInfoEntity.setNotifyId(getNotifyId());
			notifyJobInfoEntity.relateToNotifyInfoEntity(this);
		}
		if (notifyLogEscalateInfoEntity != null) {
			notifyLogEscalateInfoEntity.setNotifyId(getNotifyId());
			notifyLogEscalateInfoEntity.relateToNotifyInfoEntity(this);
		}
		if (notifyMailInfoEntity != null) {
			notifyMailInfoEntity.setNotifyId(getNotifyId());
			notifyMailInfoEntity.relateToNotifyInfoEntity(this);
			if (notifyMailInfoEntity.getMailTemplateId() != null && !notifyMailInfoEntity.getMailTemplateId().isEmpty()) {
				try {
					MailTemplateInfo mailTemplateInfoEntity
						= com.clustercontrol.notify.mail.util.QueryUtil.getMailTemplateInfoPK(notifyMailInfoEntity.getMailTemplateId());
					notifyMailInfoEntity.relateToMailTemplateInfoEntity(mailTemplateInfoEntity);
				} catch (MailTemplateNotFound e) {
					Logger.getLogger(this.getClass()).debug(e.getMessage(), e);
				} catch (InvalidRole e) {
					Logger.getLogger(this.getClass()).debug(e.getMessage(), e);
				}
			}
		}
		if (notifyStatusInfoEntity != null) {
			notifyStatusInfoEntity.setNotifyId(getNotifyId());
			notifyStatusInfoEntity.relateToNotifyInfoEntity(this);
		}
		if (notifyInfraInfoEntity != null) {
			notifyInfraInfoEntity.setNotifyId(getNotifyId());
			notifyInfraInfoEntity.relateToNotifyInfoEntity(this);
		}
		em.persist(this);
	}
}