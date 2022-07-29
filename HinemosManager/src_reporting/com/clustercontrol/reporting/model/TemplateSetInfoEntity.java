/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.model;

import java.util.Iterator;
import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_reporting_template_mst database table.
 * 
 */
@Entity
@Table(name="cc_reporting_template_set_info", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType="REPORTING",
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="template_set_id", insertable=false, updatable=false))
public class TemplateSetInfoEntity extends ObjectPrivilegeTargetInfo {
	private static final long serialVersionUID = 1L;
	private String templateSetId;
	private String templateSetName;
	private String description;
	private Long regDate;
	private Long updateDate;
	private String regUser;
	private String updateUser;
	private List<TemplateSetDetailInfoEntity> templateSetDetailInfoEntities;

	@Deprecated
	public TemplateSetInfoEntity() {
	}

	public TemplateSetInfoEntity(String templateSetId) {
		this.setTemplateSetId(templateSetId);
	}

	@Id
	@Column(name="template_set_id")
	public String getTemplateSetId() {
		return this.templateSetId;
	}

	public void setTemplateSetId(String templateSetId) {
		this.templateSetId = templateSetId;
	}


	@Column(name="template_set_name")
	public String getTemplateSetName() {
		return this.templateSetName;
	}

	public void setTemplateSetName(String templateSetName) {
		this.templateSetName = templateSetName;
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

	@Column(name="update_date")
	public Long getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	@Column(name="update_user")
	public String getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
	
	
	//bi-directional many-to-one association to CalPatternDetailInfoEntity
	@OneToMany(mappedBy="templateSetInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<TemplateSetDetailInfoEntity> getTemplateSetDetailInfoEntities() {
		return this.templateSetDetailInfoEntities;
	}

	public void setTemplateSetDetailInfoEntities(List<TemplateSetDetailInfoEntity> templateSetDetailInfoEntities) {
		this.templateSetDetailInfoEntities = templateSetDetailInfoEntities;
	}


	/**
	 * TemplateSetDetailInfoEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteTemplateSetDetailInfoEntities(List<TemplateSetDetailInfoEntityPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<TemplateSetDetailInfoEntity> list = this.getTemplateSetDetailInfoEntities();
			Iterator<TemplateSetDetailInfoEntity> iter = list.iterator();
			while(iter.hasNext()) {
				TemplateSetDetailInfoEntity entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}
}