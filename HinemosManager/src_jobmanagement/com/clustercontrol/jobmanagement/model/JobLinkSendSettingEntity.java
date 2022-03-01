/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.repository.session.RepositoryControllerBean;


/**
 * The persistent class for the cc_job_link_send_setting database table.
 *
 */
@Entity
@Table(name="cc_job_link_send_setting", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.JOB_LINK_SEND,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="joblink_send_setting_id", insertable=false, updatable=false))
public class JobLinkSendSettingEntity extends ObjectPrivilegeTargetInfo {
	private static final long serialVersionUID = 1L;
	private String joblinkSendSettingId;
	private String description;
	private String facilityId;
	private Integer processMode;
	private String protocol;
	private Integer port;
	private String hinemosUserId;
	private String hinemosPassword;
	private Boolean proxyFlg;
	private String proxyHost;
	private Integer proxyPort; 
	private String proxyUser;
	private String proxyPassword;
	private Long regDate;
	private String regUser;
	private Long updateDate;
	private String updateUser;

	private String scope;

	public JobLinkSendSettingEntity() {
	}

	public JobLinkSendSettingEntity(String joblinkSendSettingId) {
		this.setJoblinkSendSettingId(joblinkSendSettingId);
	}

	@Id
	@Column(name="joblink_send_setting_id")
	public String getJoblinkSendSettingId() {
		return this.joblinkSendSettingId;
	}
	public void setJoblinkSendSettingId(String joblinkSendSettingId) {
		this.joblinkSendSettingId = joblinkSendSettingId;
		setObjectId(joblinkSendSettingId);
	}

	@Column(name="description")
	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="process_mode")
	public Integer getProcessMode() {
		return this.processMode;
	}

	public void setProcessMode(Integer processMode) {
		this.processMode = processMode;
	}

	@Column(name="protocol")
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Column(name="port")
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}

	@Column(name="hinemos_user_id")
	public String getHinemosUserId() {
		return hinemosUserId;
	}

	public void setHinemosUserId(String hinemosUserId) {
		this.hinemosUserId = hinemosUserId;
	}

	@Column(name="hinemos_password")
	public String getHinemosPassword() {
		return hinemosPassword;
	}

	public void setHinemosPassword(String hinemosPassword) {
		this.hinemosPassword = hinemosPassword;
	}

	@Column(name="proxy_flg")
	public Boolean getProxyFlg() {
		return proxyFlg;
	}
	public void setProxyFlg(Boolean proxyFlg) {
		this.proxyFlg = proxyFlg;
	}

	@Column(name="proxy_host")
	public String getProxyHost() {
		return proxyHost;
	}
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	@Column(name="proxy_port")
	public Integer getProxyPort() {
		return proxyPort;
	}
	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}

	@Column(name="proxy_user")
	public String getProxyUser() {
		return proxyUser;
	}
	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	@Column(name="proxy_password")
	public String getProxyPassword() {
		return proxyPassword;
	}
	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
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

	@Transient
	public String getScope() {
		if (scope == null)
			try {
				scope = new RepositoryControllerBean().getFacilityPath(getFacilityId(), null);
			} catch (HinemosUnknown e) {
				Logger.getLogger(this.getClass()).debug(e.getMessage(), e);
			}
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
}