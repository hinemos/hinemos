/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * The persistent class for the cc_node_config_custom_setting_info database
 * table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name = "cc_node_config_custom_setting_info", schema = "setting")
@Cacheable(true)
public class NodeConfigCustomInfo implements Serializable {

	/** シリアルバージョンUID(カラム構成変更時にアップ). */
	private static final long serialVersionUID = 1L;

	// DBに紐づく項目.
	/** 主キー. */
	private NodeConfigCustomInfoPK id;
	/** 収集結果の表示名. */
	private String displayName = "";
	/** 説明. */
	private String description = "";
	/** 任意コマンド. */
	private String command = "";
	/** 実行ユーザ指定フラグ(True:指定あり、False:指定なし). */
	private Boolean specifyUser = Boolean.FALSE;
	/** 実行ユーザ. */
	private String effectiveUser = "";
	/** 設定有効フラグ(True:有効、False:無効). */
	private Boolean validFlg = Boolean.TRUE;

	// Relation管理.
	/** 主キー. */
	private NodeConfigSettingInfo nodeConfigSettingInfo;

	// コンストラクタ
	/** コンストラクタ：空. */
	public NodeConfigCustomInfo() {
	}

	/** コンストラクタ：主キー指定. */
	public NodeConfigCustomInfo(String settingId, String settingCustomId) {
		this(new NodeConfigCustomInfoPK(settingId, settingCustomId));
	}

	/** コンストラクタ：キーオブジェクト指定. */
	public NodeConfigCustomInfo(NodeConfigCustomInfoPK id) {
		this.id = id;
	}

	// setter・getter.
	/** 主キー. */
	@XmlTransient
	@EmbeddedId
	public NodeConfigCustomInfoPK getId() {
		if (id == null)
			id = new NodeConfigCustomInfoPK();
		return id;
	}

	/** 主キー. */
	public void setId(NodeConfigCustomInfoPK id) {
		this.id = id;
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

	/** 収集結果の表示名. */
	@Column(name = "display_name")
	public String getDisplayName() {
		return displayName;
	}

	/** 収集結果の表示名. */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/** 説明. */
	@Column(name = "description")
	public String getDescription() {
		return description;
	}

	/** 説明. */
	public void setDescription(String description) {
		this.description = description;
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

	/** 実行ユーザ指定フラグ(True:指定あり、False:指定なし). */
	@Column(name = "specify_user")
	public Boolean isSpecifyUser() {
		return this.specifyUser;
	}

	/** 実行ユーザ指定フラグ(True:指定あり、False:指定なし). */
	public void setSpecifyUser(Boolean specifyUser) {
		this.specifyUser = specifyUser;
	}

	/** 実行ユーザ. */
	@Column(name = "effective_user")
	public String getEffectiveUser() {
		return this.effectiveUser;
	}

	/** 実行ユーザ. */
	public void setEffectiveUser(String effectiveUser) {
		this.effectiveUser = effectiveUser;
	}

	/** 設定有効フラグ(True:有効、False:無効). */
	@Column(name = "valid_flg")
	public Boolean isValidFlg() {
		return validFlg;
	}

	/** 設定有効フラグ(True:有効、False:無効). */
	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	// 他各種メソッド.
	// bi-directional many-to-one association to NodeConfigSettingInfo
	@XmlTransient
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "setting_id", insertable = false, updatable = false)
	public NodeConfigSettingInfo getNodeConfigSettingInfo() {
		return this.nodeConfigSettingInfo;
	}

	@Deprecated
	public void setNodeConfigSettingInfo(NodeConfigSettingInfo nodeConfigSettingInfo) {
		this.nodeConfigSettingInfo = nodeConfigSettingInfo;
	}

	/**
	 * NodeConfigSettingInfoオブジェクト参照設定<BR>
	 * 
	 * NodeConfigSettingInfo設定時はSetterに代わりこちらを使用すること。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship. It is
	 * the developer’s responsibility to keep the in-memory references held on
	 * the owning side and those held on the inverse side consistent with each
	 * other when they change.
	 */
	public void relateToNodeConfigSettingInfo(NodeConfigSettingInfo nodeConfigSettingInfo) {
		this.setNodeConfigSettingInfo(nodeConfigSettingInfo);
		if (nodeConfigSettingInfo != null) {
			List<NodeConfigCustomInfo> list = nodeConfigSettingInfo.getNodeConfigCustomList();
			if (list == null) {
				list = new ArrayList<>();
			} else {
				for (NodeConfigCustomInfo entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			nodeConfigSettingInfo.setNodeConfigCustomList(list);
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
	 * based on references held by the owning side of the relationship. It is
	 * the developer’s responsibility to keep the in-memory references held on
	 * the owning side and those held on the inverse side consistent with each
	 * other when they change.
	 */
	public void unchain() {

		// NodeConfigSettingInfo
		if (this.nodeConfigSettingInfo != null) {
			List<NodeConfigCustomInfo> list = this.nodeConfigSettingInfo.getNodeConfigCustomList();
			if (list != null) {
				Iterator<NodeConfigCustomInfo> iter = list.iterator();
				while (iter.hasNext()) {
					NodeConfigCustomInfo entity = iter.next();
					if (entity.getId().equals(this.getId())) {
						iter.remove();
						break;
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		String returnString = "NodeConfigCustomInfo [" + //
				"id=" + id //
				+ ", " + "displayName=" + displayName //
				+ ", " + "description=" + description //
				+ ", " + "command=" + command //
				+ ", " + "specifyUser=" + specifyUser //
				+ ", " + "effectiveUser=" + effectiveUser //
				+ ", " + "validFlg=" + validFlg //
				+ "]";
		return returnString;
	}

}
