/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * The persistent class for the cc_cal_detail_info database table.
 * 
 */
@Entity
@Table(name="cc_reporting_template_set_detail_info", schema="setting")
@Cacheable(true)
public class TemplateSetDetailInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private TemplateSetDetailInfoEntityPK id;
	private String description;
	private String templateId;
	private String titleName;
	private TemplateSetInfoEntity templateSetInfoEntity;

	@Deprecated
	public TemplateSetDetailInfoEntity() {
	}

	public TemplateSetDetailInfoEntity(TemplateSetDetailInfoEntityPK pk) {
		this.setId(pk);
	}

	public TemplateSetDetailInfoEntity(String templateSetId, Integer orderNo) {
		this(new TemplateSetDetailInfoEntityPK(templateSetId, orderNo));
	}

	@EmbeddedId
	public TemplateSetDetailInfoEntityPK getId() {
		return this.id;
	}

	public void setId(TemplateSetDetailInfoEntityPK id) {
		this.id = id;
	}

	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="template_id")
	public String getTemplateId() {
		return templateId;
	}
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
	
	@Column(name="title_name")
	public String getTitleName() {
		return titleName;
	}

	public void setTitleName(String titleName) {
		this.titleName = titleName;
	}

	//bi-directional many-to-one association to ReportingTemplateSetInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="template_set_id", insertable=false, updatable=false)
	public TemplateSetInfoEntity getTemplateSetInfoEntity() {
		return this.templateSetInfoEntity;
	}

	@Deprecated
	public void setTemplateSetInfoEntity(TemplateSetInfoEntity templateSetInfoEntity) {
		this.templateSetInfoEntity = templateSetInfoEntity;
	}

	/**
	 * TemplateSetInfoEntityオブジェクト参照設定<BR>
	 * 
	 * ReportingTemplateSetInfoEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToTemplateSetInfoEntity(TemplateSetInfoEntity templateSetInfoEntity) {
		this.setTemplateSetInfoEntity(templateSetInfoEntity);
		if (templateSetInfoEntity != null) {
			List<TemplateSetDetailInfoEntity> list = templateSetInfoEntity.getTemplateSetDetailInfoEntities();
			if (list == null) {
				list = new ArrayList<TemplateSetDetailInfoEntity>();
			} else {
				for(TemplateSetDetailInfoEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			templateSetInfoEntity.setTemplateSetDetailInfoEntities(list);
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

		// CalInfoEntity
		if (this.templateSetInfoEntity != null) {
			List<TemplateSetDetailInfoEntity> list = this.templateSetInfoEntity.getTemplateSetDetailInfoEntities();
			if (list != null) {
				Iterator<TemplateSetDetailInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					TemplateSetDetailInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
}