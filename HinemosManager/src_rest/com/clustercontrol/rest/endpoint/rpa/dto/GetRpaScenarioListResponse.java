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

public class GetRpaScenarioListResponse {
	
	public GetRpaScenarioListResponse() {
	}

	/** RPAツール名 */
	private String rpaToolName;
	/** シナリオID */
	private String scenarioId;
	/** シナリオ名 */
	private String scenarioName;
	/** シナリオ識別文字列 */
	private String scenarioIdentifyString;
	/** 説明 */
	private String description;
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

	/** RPAツール名 */
	public String getRpaToolName() {
		return this.rpaToolName;
	}
	public void setRpaToolName(String rpaToolName) {
		this.rpaToolName = rpaToolName;
	}

	/** シナリオID */
	public String getScenarioId() {
		return this.scenarioId;
	}
	public void setScenarioId(String scenarioId) {
		this.scenarioId = scenarioId;
	}

	/** シナリオ名 */
	public String getScenarioName() {
		return this.scenarioName;
	}
	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}

	/** シナリオ識別文字列 */
	public String getScenarioIdentifyString() {
		return this.scenarioIdentifyString;
	}
	public void setScenarioIdentifyString(String scenarioIdentifyString) {
		this.scenarioIdentifyString = scenarioIdentifyString;
	}

	/** 説明 */
	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
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
		return "RpaScenarioResponse [rpaToolName= " + rpaToolName + "scenarioId=" + scenarioId + ", scenarioName=" + scenarioName 
				+ ", scenarioIdentifyString=" + scenarioIdentifyString + ", description=" + description + ", ownerRoleId=" + ownerRoleId
				+ ", regDate = " + regDate + ", updateDate=" + updateDate 
				+ ", regUser=" + regUser + ", updateUser=" + updateUser
				+ "]";
	}

}
