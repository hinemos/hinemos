/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.model;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.rest.dto.EnumDto;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * シナリオ情報を格納するEntity定義
 * 
 */
@Entity
@Table(name="cc_rpa_scenario", schema="log")
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.RPA_SCENARIO,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="scenario_id", insertable=false, updatable=false))
public class RpaScenario extends ObjectPrivilegeTargetInfo {
	private static final long serialVersionUID = 1L;
	/** シナリオID */
	private String scenarioId;
	/** シナリオ実績作成設定ID */
	private String scenarioOperationResultCreateSettingId;
	/** 説明 */
	private String description;
	/** RPAツールID */
	private String rpaToolId;
	/** 手動操作時間 */
	private Long manualTime;
	/** 複数ノード共通のシナリオ(チェックボックス) */
	private Boolean commonNodeScenario;
	/** 手動操作時間算出方式 */
	private CulcType manualTimeCulcType;

	public static enum CulcType implements EnumDto<String> {
		/** 自動算出する */
		AUTO,
		/** 時間を指定する */
		FIX_TIME;

		@Override
		public String getCode() {
			return this.name();
		}
	}
	
	/** 運用開始日時 */
	private Long opeStartDate;
	/** シナリオ識別文字列 */
	private String scenarioIdentifyString;
	/** シナリオ名 */
	private String scenarioName;
	/** オーナーロールID */
	private String ownerRoleId;
	/** 実行ノード */
	private List<RpaScenarioExecNode> execNodes = new ArrayList<>();
	/** シナリオタグ紐付け情報 */
	private List<String> tagRelationList = new ArrayList<>();

	/** 作成日時 */
	private Long regDate;
	/** 新規作成ユーザ */
	private String regUser;
	/** 最終変更日時 */
	private Long updateDate;
	/** 最終変更ユーザ */
	private String updateUser;

	public RpaScenario() {
	}

	/** シナリオID */
	@Id
	@Column(name="scenario_id")
	public String getScenarioId() {
		return this.scenarioId;
	}
	public void setScenarioId(String scenarioId) {
		this.scenarioId = scenarioId;
		for (RpaScenarioExecNode node : this.execNodes) {
			node.setScenarioId(scenarioId);
		}
	}
	
	/** シナリオ実績作成設定ID */
	@Column(name="scenario_operation_result_create_setting_id")
	public String getScenarioOperationResultCreateSettingId() {
		return scenarioOperationResultCreateSettingId;
	}

	public void setScenarioOperationResultCreateSettingId(String scenarioOperationResultCreateSettingId) {
		this.scenarioOperationResultCreateSettingId = scenarioOperationResultCreateSettingId;
	}

	/** 説明 */
	@Column(name="description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	/** RPAツールID */
	@Column(name="rpa_tool_id")
	public String getRpaToolId() {
		return rpaToolId;
	}

	public void setRpaToolId(String rpaToolId) {
		this.rpaToolId = rpaToolId;
	}

	/** 手動操作時間 */
	@Column(name="manual_time")
	public Long getManualTime() {
		return this.manualTime;
	}

	public void setManualTime(Long manualTime) {
		this.manualTime = manualTime;
	}


	/** 複数ノード共通のシナリオ(チェックボックス) */
	@Column(name="common_node_scenario")
	public Boolean getCommonNodeScenario() {
		return commonNodeScenario;
	}

	public void setCommonNodeScenario(Boolean commonNodeScenario) {
		this.commonNodeScenario = commonNodeScenario;
	}

	/** 手動操作時間算出方式 */
	@Enumerated(EnumType.STRING)
	@Column(name="manual_time_culc_type")
	public CulcType getManualTimeCulcType() {
		return this.manualTimeCulcType;
	}

	public void setManualTimeCulcType(CulcType manualTimeCulcType) {
		this.manualTimeCulcType = manualTimeCulcType;
	}


	/** 運用開始日時 */
	@Column(name="ope_start_date")
	public Long getOpeStartDate() {
		return this.opeStartDate;
	}

	public void setOpeStartDate(Long opeStartDate) {
		this.opeStartDate = opeStartDate;
	}


	/** シナリオ識別文字列 */
	@Column(name="scenario_identify_string")
	public String getScenarioIdentifyString() {
		return this.scenarioIdentifyString;
	}

	public void setScenarioIdentifyString(String scenarioIdentifyString) {
		this.scenarioIdentifyString = scenarioIdentifyString;
		for (RpaScenarioExecNode node : this.execNodes) {
			node.setScenarioIdentifyString(scenarioIdentifyString);
		}
	}

	/** シナリオ名 */
	@Column(name="scenario_name")
	public String getScenarioName() {
		return this.scenarioName;
	}

	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}
	
	/** オーナーロールID */
	@Column(name="owner_role_id")
	public String getOwnerRoleId() {
		return this.ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	/** 実行ノード */
	@OneToMany(mappedBy="rpaScenario", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
	public List<RpaScenarioExecNode> getExecNodes() {
		return execNodes;
	}

	public void setExecNodes(List<RpaScenarioExecNode> execNodes) {
		this.execNodes = execNodes;
	}

	/**
	 * 実行ノードを登録する。
	 * @param facilityId
	 */
	public void addExecNode(String facilityId) {
		execNodes.add(new RpaScenarioExecNode(this.scenarioId, facilityId, this.scenarioIdentifyString));
	}
	
	/**
	 * 実行ノードを削除する。
	 * @param facilityId
	 */
	public void removeExecNode(String facilityId) {
		for (int i=0;i<execNodes.size();i++) {
			if (execNodes.get(i).getId().getFacilityId().equals(facilityId)) {
				execNodes.remove(i);
				break;
			}
		}
	}

	/**
	 * 実行ノードが登録済か判定する。
	 * @param facilityId
	 * @return
	 */
	public boolean containsExecNode(String facilityId) {
		for (RpaScenarioExecNode execNode : execNodes) {
			if (execNode.getId().getFacilityId().equals(facilityId)) {
				return true;
			}
		}
		return false;
	}

	/** シナリオタグ紐付け情報 */
	@ElementCollection(fetch=FetchType.LAZY)
	@CollectionTable(
			name="cc_rpa_scenario_tag_relation", schema="log",
	        joinColumns=@JoinColumn(name="scenario_id"))
	@Column(name="tag_id")
	public List<String> getTagRelationList() {
		return tagRelationList;
	}

	public void setTagRelationList(List<String> tagRelationList) {
		this.tagRelationList = tagRelationList;
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

	@Override
	public String toString() {
		return "RpaScenario [scenarioId=" + scenarioId + ", scenarioOperationResultCreateSettingId=" + scenarioOperationResultCreateSettingId + ", description="
				+ description + ", rpaToolId=" + rpaToolId + ", manualTime=" + manualTime + ", commonNodeScenario=" + commonNodeScenario + ", manualTimeCulcType="
				+ manualTimeCulcType + ", opeStartDate=" + opeStartDate + ", scenarioIdentifyString=" + scenarioIdentifyString + ", scenarioName=" + scenarioName
				+ ", ownerRoleId=" + ownerRoleId + ", execNodes=" + execNodes + ", tagRelationList=" + tagRelationList + ", regDate=" + regDate + ", regUser=" + regUser
				+ ", updateDate=" + updateDate + ", updateUser=" + updateUser + "]";
	}
}
