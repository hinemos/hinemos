/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.util.HinemosTime;

/**
 * The persistent class for the cc_collect_data_binary database table.
 * 
 * @version 6.1.0
 * @since 6.1.0
 * @see com.clustercontrol.hub.model.CollectStringData
 */
@XmlType(namespace = "http://cmdb.ws.clustercontrol.com")
@Entity
@Table(name = "cc_cfg_auto_register_node", schema = "setting")
public class AutoRegisterNodeInfo implements Serializable {
	/** シリアルバージョンUID(カラム構成変更時にアップ). */
	private static final long serialVersionUID = 1L;

	// ------DBと紐づく項目
	// 主キー.
	/** 優先順位. */
	private Integer orderNo = null;

	// 以下通常カラム.
	/** 接続元ネットワーク(IPv4/CIDR or IPv6/プリフィックス). */
	private String sourceNetwork = null;

	/** 説明. */
	private String description = null;

	/** オーナーロールID. */
	private String ownerRoleId = null;

	/** プリフィックス(FacilityID先頭文字列). */
	private String prefix = null;

	/** 設定範囲の自動登録有効(true:有効,false:無効). */
	private Boolean valid = null;

	/** 最終登録済連番. */
	private Long lastSerialNumber = null;

	/** 新規作成ユーザ. */
	private String regUser = null;

	/** 作成日時. */
	private Long regDate = null;

	/** 最終変更ユーザ. */
	private String updateUser = null;

	/** 最終更新日時. */
	private Long updateDate = null;

	/**
	 * 空コンストラクタ(デフォルト値を設定).
	 **/
	public AutoRegisterNodeInfo() {
		this.setDefaultValue();
	}

	/**
	 * 主キー指定コンストラクタ
	 **/
	public AutoRegisterNodeInfo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	/** 優先順位. */
	@Id
	@Column(name = "order_no")
	public Integer getOrderNo() {
		return orderNo;
	}

	/** 優先順位. */
	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	/** 接続元ネットワーク(IPv4/CIDR or IPv6/プリフィックス). */
	@Column(name = "source_network")
	public String getSourceNetwork() {
		return this.sourceNetwork;
	}

	/** 接続元ネットワーク(IPv4/CIDR or IPv6/プリフィックス). */
	public void setSourceNetwork(String sourceNetwork) {
		this.sourceNetwork = sourceNetwork;
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

	/** オーナーロールID. */
	@Column(name = "owner_role_id")
	public String getOwnerRoleId() {
		return this.ownerRoleId;
	}

	/** オーナーロールID. */
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	/** プリフィックス(FacilityID先頭文字列). */
	@Column(name = "prefix")
	public String getPrefix() {
		return this.prefix;
	}

	/** プリフィックス(FacilityID先頭文字列). */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/** 設定範囲の自動登録有効(true:有効,false:無効). */
	@Column(name = "valid")
	public Boolean getValid() {
		return this.valid;
	}

	/** 設定範囲の自動登録有効(true:有効,false:無効). */
	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	/** 最終登録済連番. */
	@Column(name = "last_serial_number")
	public Long getLastSerialNumber() {
		return this.lastSerialNumber;
	}

	/** 最終登録済連番. */
	public void setLastSerialNumber(Long lastSerialNumber) {
		this.lastSerialNumber = lastSerialNumber;
	}

	/** 新規作成ユーザ. */
	@Column(name = "reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	/** 新規作成ユーザ. */
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	/** 作成日時. */
	@Column(name = "reg_date")
	public Long getRegDate() {
		return this.regDate;
	}

	/** 作成日時. */
	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	/** 最終変更ユーザ. */
	@Column(name = "update_user")
	public String getUpdateUser() {
		return this.updateUser;
	}

	/** 最終変更ユーザ. */
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	/** 最終更新日時. */
	@Column(name = "update_date")
	public Long getUpdateDate() {
		return this.updateDate;
	}

	/** 最終更新日時. */
	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	/**
	 * null値が設定されている項目にデフォルト値を設定する.
	 * 
	 */
	public void setDefaultValue() {
		if (this.orderNo == null) {
			this.orderNo = Integer.valueOf(1);
		}
		if (this.sourceNetwork == null) {
			this.sourceNetwork = "0.0.0.0/0";
		}
		if (this.description == null) {
			this.description = "all nodes";
		}
		if (this.ownerRoleId == null) {
			this.ownerRoleId = "ALL_USERS";
		}
		if (this.prefix == null) {
			this.prefix = "NODE";
		}
		if (this.valid == null) {
			this.valid = Boolean.TRUE;
		}
		if (this.lastSerialNumber == null) {
			this.lastSerialNumber = Long.valueOf(0L);
		}
		if (this.regUser == null) {
			this.regUser = "system";
		}
		if (this.regDate == null) {
			this.regDate = HinemosTime.currentTimeMillis();
		}
		if (this.updateUser == null) {
			this.updateUser = "system";
		}
		if (this.updateDate == null) {
			this.updateDate = HinemosTime.currentTimeMillis();
		}
	}
}
