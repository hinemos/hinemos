/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.repository.bean.NodeRegisterFlagConstant;
import com.clustercontrol.util.HinemosTime;

/**
 * The persistent class for the cc_cfg_node_package database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name = "cc_cfg_node_custom", schema = "setting")
@Cacheable(false)
public class NodeCustomInfo implements Serializable, Cloneable {

	/** シリアルバージョンUID(カラム構成変更時にアップ). */
	private static final long serialVersionUID = 1L;

	// DBに紐づく項目.
	/** 主キー. */
	private NodeCustomInfoPK id;
	/** 収集結果の表示名. */
	private String displayName = "";
	/** 任意コマンド. */
	private String command = "";
	/** コマンド実行結果. */
	private String value = "";
	/** 登録日時. */
	private Long regDate = HinemosTime.currentTimeMillis();
	/** 登録ユーザ. */
	private String regUser = "";
	/** 更新日時. */
	private Long updateDate = HinemosTime.currentTimeMillis();
	/** 更新ユーザ. */
	private String updateUser = "";
	private Boolean searchTarget = Boolean.FALSE;

	// DBに紐づかない項目
	/** 登録フラグ(Agent収集結果). */
	private Integer registerFlag = NodeRegisterFlagConstant.GET_SUCCESS;

	// コンストラクタ
	/** コンストラクタ：空. */
	public NodeCustomInfo() {
	}

	/** コンストラクタ：主キー指定. */
	public NodeCustomInfo(String facilityId, String settingId, String settingCustomId) {
		this(new NodeCustomInfoPK(facilityId, settingId, settingCustomId));
	}

	/** コンストラクタ：キーオブジェクト指定. */
	public NodeCustomInfo(NodeCustomInfoPK id) {
		this.id = id;
	}

	// setter・getter.
	/** 主キー. */
	@XmlTransient
	@EmbeddedId
	public NodeCustomInfoPK getId() {
		if (id == null)
			id = new NodeCustomInfoPK();
		return id;
	}

	/** 主キー. */
	public void setId(NodeCustomInfoPK id) {
		this.id = id;
	}

	/** ファシリティID. */
	@XmlTransient
	@Transient
	public String getFacilityId() {
		return getId().getFacilityId();
	}

	/** ファシリティID. */
	public void setFacilityId(String facilityId) {
		getId().setFacilityId(facilityId);
	}

	/** 親の対象構成情報ID. */
	@Transient
	public String getSettingId() {
		return getId().getSettingId();
	}

	/** 親の対象構成情報ID. */
	public void setSettingId(String settingId) {
		getId().setSettingId(settingId);
	}

	/** ユーザ任意情報ID. */
	@Transient
	public String getSettingCustomId() {
		return getId().getSettingCustomId();
	}

	/** ユーザ任意情報ID. */
	public void setSettingCustomId(String settingCustomId) {
		getId().setSettingCustomId(settingCustomId);
	}

	/** 収集結果の表示名. */
	@Column(name = "display_name")
	public String getDisplayName() {
		return displayName;
	}

	/** 収集結果の表示名. */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/** 任意コマンド. */
	@Column(name = "command")
	public String getCommand() {
		return command;
	}

	/** 任意コマンド. */
	public void setCommand(String command) {
		this.command = command;
	}

	/** コマンド実行結果. */
	@Column(name = "value")
	public String getValue() {
		return value;
	}

	/** コマンド実行結果. */
	public void setValue(String value) {
		this.value = value;
	}

	/** 登録日時. */
	@Column(name = "reg_date")
	public Long getRegDate() {
		return this.regDate;
	}

	/** 登録日時. */
	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	/** 登録ユーザ. */
	@Column(name = "reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	/** 登録ユーザ. */
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	/** 更新日時. */
	@Column(name = "update_date")
	public Long getUpdateDate() {
		return this.updateDate;
	}

	/** 更新日時. */
	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	/** 更新ユーザ. */
	@Column(name = "update_user")
	public String getUpdateUser() {
		return this.updateUser;
	}

	/** 更新ユーザ. */
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	@Transient
	public Integer getRegisterFlag() {
		return registerFlag;
	}

	public void setRegisterFlag(Integer registerFlag) {
		this.registerFlag = registerFlag;
	}

	@Transient
	public Boolean getSearchTarget() {
		return this.searchTarget;
	}
	public void setSearchTarget(Boolean searchTarget) {
		this.searchTarget = searchTarget;
	}

	@Override
	public NodeCustomInfo clone() {
		try {
			NodeCustomInfo cloneInfo = (NodeCustomInfo)super.clone();
			cloneInfo.id = this.id;
			cloneInfo.displayName = this.displayName;
			cloneInfo.command = this.command;
			cloneInfo.value = this.value;
			cloneInfo.regDate = this.regDate;
			cloneInfo.regUser = this.regUser;
			cloneInfo.updateDate = this.updateDate;
			cloneInfo.updateUser = this.updateUser;
			cloneInfo.searchTarget = this.searchTarget;
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.getMessage());
		}
	}

	@Override
	public String toString() {
		return "NodeCustomInfo ["
				+ "id=" + id
				+ ", displayName=" + displayName
				+ ", command=" + command
				+ ", value=" + value
				+ ", regDate=" + regDate
				+ ", regUser=" + regUser
				+ ", updateDate=" + updateDate
				+ ", updateUser=" + updateUser
				+ ", searchTarget=" + searchTarget
				+ ", registerFlag=" + registerFlag
				+ "]";
	}
}