package com.clustercontrol.notify.mail.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.notify.model.NotifyMailInfo;


/**
 * The persistent class for the cc_mail_template_info database table.
 * 
 */
@Entity
@Table(name="cc_mail_template_info", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="mail_template_id", insertable=false, updatable=false))
public class MailTemplateInfo extends ObjectPrivilegeTargetInfo {
	private static final long serialVersionUID = 1L;
	private String mailTemplateId;
	private String body;
	private String description;
	private Long regDate;
	private String regUser;
	private String subject;
	private Long updateDate;
	private String updateUser;
	private List<NotifyMailInfo> notifyMailInfoEntities = new ArrayList<>();

	@Deprecated
	public MailTemplateInfo() {
	}

	public MailTemplateInfo(String mailTemplateId) {
		this.setMailTemplateId(mailTemplateId);
	}

	@Id
	@Column(name="mail_template_id")
	public String getMailTemplateId() {
		return this.mailTemplateId;
	}

	public void setMailTemplateId(String mailTemplateId) {
		this.mailTemplateId = mailTemplateId;
		setObjectId(mailTemplateId);
	}


	@Column(name="body")
	public String getBody() {
		return this.body;
	}

	public void setBody(String body) {
		this.body = body;
	}


	@Column(name="description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
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


	@Column(name="subject")
	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
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


	//bi-directional many-to-one association to NotifyMailInfoEntity
	@XmlTransient
	@OneToMany(mappedBy="mailTemplateInfoEntity", fetch=FetchType.LAZY)
	public List<NotifyMailInfo> getNotifyMailInfoEntities() {
		return this.notifyMailInfoEntities;
	}

	public void setNotifyMailInfoEntities(List<NotifyMailInfo> notifyMailInfoEntities) {
		this.notifyMailInfoEntities = notifyMailInfoEntities;
	}

}