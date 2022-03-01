/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

public class RpaScenarioTagResponse {
	
	public RpaScenarioTagResponse() {
	}

	/** タグID */
	private String tagId;
	/** タグ名 */
	@RestPartiallyTransrateTarget
	private String tagName;
	/** 説明 */
	@RestPartiallyTransrateTarget
	private String description;
	/** タグ階層パス */
	@RestPartiallyTransrateTarget
	private String tagPath;
	/** オーナーロールID */
	private String ownerRoleId;
	/** 作成日時 */
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String regDate;
	/** 新規作成ユーザ */
	private String regUser;
	/** 最終変更日時 */
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String updateDate;
	/** 最終変更ユーザ */
	private String updateUser;

	/** タグID */
	public String getTagId() {
		return tagId;
	}
	public void setTagId(String tagId) {
		this.tagId = tagId;
	}

	/** タグ名  */
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	/** 説明 */
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	/** タグ階層パス */
	public String getTagPath() {
		return tagPath;
	}
	public void setTagPath(String tagPath) {
		this.tagPath = tagPath;
	}

	/** オーナーロールID */
	public String getOwnerRoleId() {
		return this.ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	/** 作成日時 */
	public String getRegDate() {
		return this.regDate;
	}
	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}


	/** 新規作成ユーザ */
	public String getRegUser() {
		return this.regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	/** 最終変更日時 */
	public String getUpdateDate() {
		return this.updateDate;
	}
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}


	/** 最終変更ユーザ */
	public String getUpdateUser() {
		return this.updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	@Override
	public String toString() {
		return "RpaScenarioResponse [tagId=" + tagId + ", tagName=" + tagName 
				+ ", description=" + description + ", tagPath=" + tagPath + ", ownerRoleId=" + ownerRoleId
				+ ", regDate = " + regDate + ", updateDate=" + updateDate 
				+ ", regUser=" + regUser + ", updateUser=" + updateUser
				+ "]";
	}

}
