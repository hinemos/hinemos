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

import com.clustercontrol.repository.bean.NodeConfigSettingConstant;

/**
 * The persistent class for the cc_cfg_node_package database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name = "cc_node_custom_history_detail", schema = "log")
@Cacheable(false)
public class NodeCustomHistoryDetail implements Serializable, NodeHistoryDetail {

	/** シリアルバージョンUID(カラム構成変更時にアップ). */
	private static final long serialVersionUID = 1L;

	// DBに紐づく項目.
	/** 主キー. */
	private NodeCustomHistoryDetailPK id;
	/** 収集日時To(レコードの適用期間算出). */
	private Long regDateTo = NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE;
	/** 収集結果の表示名. */
	private String displayName = "";
	/** 任意コマンド. */
	private String command = "";
	/** コマンド実行結果. */
	private String value = "";
	/** 登録ユーザ. */
	private String regUser = "";

	// コンストラクタ
	/**
	 * デフォルト値生成コンストラクタ.
	 */
	public NodeCustomHistoryDetail() {
	}

	/**
	 * キー値指定コンストラクタ.
	 */
	public NodeCustomHistoryDetail(String facilityId, Long regDate, String settingId, String settingCustomId) {
		this(new NodeCustomHistoryDetailPK(facilityId, regDate, settingId, settingCustomId));
	}

	/**
	 * キーオブジェクト指定コンストラクタ.
	 */
	public NodeCustomHistoryDetail(NodeCustomHistoryDetailPK id) {
		this.id = id;
	}

	// setter・getter.
	/** 主キー. */
	@XmlTransient
	@EmbeddedId
	public NodeCustomHistoryDetailPK getId() {
		if (id == null)
			id = new NodeCustomHistoryDetailPK();
		return id;
	}

	/** 主キー. */
	public void setId(NodeCustomHistoryDetailPK id) {
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

	/** 収集日時. */
	@XmlTransient
	@Transient
	public Long getRegDate() {
		return getId().getRegDate();
	}

	/** 収集日時. */
	public void setRegDate(Long regDate) {
		getId().setRegDate(regDate);
	}

	/** 親の対象構成情報ID. */
	@XmlTransient
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

	@Column(name = "reg_date_to")
	public Long getRegDateTo() {
		return regDateTo;
	}

	public void setRegDateTo(Long regDateTo) {
		this.regDateTo = regDateTo;
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

	/** 登録ユーザ. */
	@Column(name = "reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	/** 登録ユーザ. */
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
}