/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.hub.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;

/**
 * The persistent class for the cc_log_Format database table.
 * 
 */
@Entity
@Table(name="cc_hub_log_format", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.HUB_LOGFORMAT,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="log_format_id", insertable=false, updatable=false))
public class LogFormat extends ObjectPrivilegeTargetInfo {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String logFormatId;
	private String description;
	private String timestampRegex;
	private String timestampFormat;
	private Long regDate;
	private Long updateDate;
	private String regUser;  
	private String updateUser;
	
	private List<LogFormatKey> keys = new ArrayList<>();

	@Deprecated
	public LogFormat(){
	}
	
	public LogFormat(String formatId) {
		this.setLogFormatId(formatId);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.setObjectId(this.getLogFormatId());
	}
	
	@Id
	@Column(name="log_format_id")
	public String getLogFormatId() {
		return logFormatId;
	}
	public void setLogFormatId(String logFormatId) {
		this.logFormatId = logFormatId;
		this.setObjectId(logFormatId);
	}
	
	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Column(name="timestamp_regex")
	public String getTimestampRegex() {
		return timestampRegex;
	}
	public void setTimestampRegex(String timestampRegex) {
		this.timestampRegex = timestampRegex;
	}

	@Column(name="timestamp_format")
	public String getTimestampFormat() {
		return timestampFormat;
	}
	public void setTimestampFormat(String timestampFormat) {
		this.timestampFormat = timestampFormat;
	}
	
	@Column(name="reg_date")
	public Long getRegDate() {
		return regDate;
	}
	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}
	
	@Column(name="update_date")
	public Long getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}
	
	@Column(name="reg_user")
	public String getRegUser() {
		return regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
	
	@Column(name="update_user")
	public String getUpdateUser() {
		return updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
	
	@ElementCollection
	@CollectionTable(
		name="cc_hub_log_format_key", schema="setting",
		joinColumns={@JoinColumn(name="log_format_id", referencedColumnName="log_format_id")}
	)
	public List<LogFormatKey> getKeyPatternList() {
		return keys;
	}
	public void setKeyPatternList(List<LogFormatKey> keys) {
		this.keys = keys;
	}
}