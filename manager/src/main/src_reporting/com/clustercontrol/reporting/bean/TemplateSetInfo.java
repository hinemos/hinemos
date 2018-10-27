/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

/**
 * レポーティング テンプレートセット情報のデータクラスです。
 * 
 * @version 5.0.a
 * @since 5.0.a
 *
 */
@XmlType(namespace = "http://reporting.ws.clustercontrol.com")
public class TemplateSetInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	private String templateSetId = null;
	private String templateSetName = null;
	private String ownerRoleId = null;
	private String description = null;
	private Long regDate = Long.valueOf(0);
	private Long updateDate = Long.valueOf(0);
	private String regUser = null;
	private String updateUser = null;
	private List<TemplateSetDetailInfo> templateSetDetailInfoList = 
			new ArrayList<TemplateSetDetailInfo>();

	public TemplateSetInfo() {
	}
	
	/**
	 * 
	 * @return
	 */
	public List<TemplateSetDetailInfo> getTemplateSetDetailInfoList() {
		return templateSetDetailInfoList;
	}

	/**
	 * 
	 * @param templateSetDetailInfoList
	 */
	public void setTemplateSetDetailInfoList(List<TemplateSetDetailInfo> templateSetDetailInfoList) {
		this.templateSetDetailInfoList = templateSetDetailInfoList;
	}
	
	public String getTemplateSetId() {
		return templateSetId;
	}

	public void setTemplateSetId(String templateSetId) {
		this.templateSetId = templateSetId;
	}

	public String getTemplateSetName() {
		return templateSetName;
	}

	public void setTemplateSetName(String templateSetName) {
		this.templateSetName = templateSetName;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public Long getRegDate() {
		return regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	public Long getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
}
