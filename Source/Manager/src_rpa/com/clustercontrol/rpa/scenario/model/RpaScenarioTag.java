/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.scenario.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;

/**
 * RPAシナリオタグを格納するEntity定義
 * 
 */
@Entity
@Table(name="cc_rpa_scenario_tag", schema="setting")
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.RPA_SCENARIO_TAG,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="tag_id", insertable=false, updatable=false))
public class RpaScenarioTag extends ObjectPrivilegeTargetInfo {
	private static final long serialVersionUID = 1L;
	/** タグID */
	private String tagId;
	/** タグ名 */
	private String tagName;
	/** 説明 */
	private String description;
	/** タグ階層パス */
	private String tagPath;
	/** 作成日時 */
	private Long regDate;
	/** 新規作成ユーザ */
	private String regUser;
	/** 最終変更日時 */
	private Long updateDate;
	/** 最終変更ユーザ */
	private String updateUser;

	/** タグID */
	@Id
	@Column(name = "tag_id")
	public String getTagId() {
		return tagId;
	}
	public void setTagId(String tagId) {
		this.tagId = tagId;
	}

	/** タグ名  */
	@Column(name = "tag_name")
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	/** 説明 */
	@Column(name = "description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	/** タグ階層パス */
	@Column(name = "tag_path")
	public String getTagPath() {
		return tagPath;
	}

	public void setTagPath(String tagPath) {
		this.tagPath = tagPath;
	}

	/** 作成日時 */
	@Column(name="reg_date")
	public Long getRegDate() {
		return this.regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}


	/** 新規作成ユーザ */
	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	/** 最終変更日時 */
	@Column(name="update_date")
	public Long getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}


	/** 最終変更ユーザ */
	@Column(name="update_user")
	public String getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

}
