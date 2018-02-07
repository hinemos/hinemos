/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;


/**
 * ジョブマップ用アイコン情報を保持するクラス<BR>
 * @version 5.1.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobmapIconImage implements Serializable {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = 1L;

	/**
	 * アイコンID
	 */
	private String m_iconId;

	/** ファイルデータ */
	private byte[] m_filedata;

	/** 説明 */
	private String m_description;

	/** 新規作成ユーザ */
	private String m_createUser;

	/** 作成日時 */
	private Long m_createTime;

	/** 最新更新ユーザ */
	private String m_updateUser;

	/** 最新更新日時 */
	private Long m_updateTime;

	/** オーナーロールID */
	private String m_ownerRoleId;

	/**
	 * アイコンIDを返す<BR>
	 */
	public String getIconId() {
		return m_iconId;
	}
	/**
	 * アイコンIDを設定する<BR>
	 * @param iconId
	 */
	public void setIconId(String iconId) {
		this.m_iconId = iconId;
	}

	/**
	 * ファイルデータを返す<BR>
	 * @return ファイルデータ
	 */
	public byte[] getFiledata() {
		return m_filedata;
	}
	/**
	 * ファイルデータを設定する<BR>
	 * @param filedata ファイルデータ
	 */
	public void setFiledata(byte[] filedata) {
		this.m_filedata = filedata;
	}

	/**
	 * 説明を返す<BR>
	 * @return 説明
	 */
	public String getDescription() {
		return m_description;
	}
	/**
	 * 説明を設定する<BR>
	 * @param descriptor 説明
	 */
	public void setDescription(String description) {
		this.m_description = description;
	}

	/**
	 * 作成日時を返す<BR>
	 * @return 作成日時
	 */
	public Long getCreateTime() {
		return m_createTime;
	}
	/**
	 * 作成日時を設定する<BR>
	 * @param createTime 作成日時
	 */
	public void setCreateTime(Long createTime) {
		this.m_createTime = createTime;
	}

	/**
	 * 最新更新日時を返す<BR>
	 * @return 最新更新日時
	 */
	public Long getUpdateTime() {
		return m_updateTime;
	}
	/**
	 * 最新更新日時を設定する<BR>
	 * @param updateTime 最新更新日時
	 */
	public void setUpdateTime(Long updateTime) {
		this.m_updateTime = updateTime;
	}

	/**
	 * 新規作成ユーザを返す<BR>
	 * @return 新規作成ユーザ
	 */
	public String getCreateUser() {
		return m_createUser;
	}
	/**
	 * 新規作成ユーザを設定する<BR>
	 * @param createUser 新規作成ユーザ
	 */
	public void setCreateUser(String createUser) {
		this.m_createUser = createUser;
	}

	/**
	 * 最新更新ユーザを返す<BR>
	 * @return 最新更新ユーザ
	 */
	public String getUpdateUser() {
		return m_updateUser;
	}
	/**
	 * 最新更新ユーザを設定する<BR>
	 * @param updateUser 最新更新ユーザ
	 */
	public void setUpdateUser(String updateUser) {
		this.m_updateUser = updateUser;
	}

	/**
	 * オーナーロールIDを返す<BR>
	 * @return オーナーロールID
	 */
	public String getOwnerRoleId() {
		return m_ownerRoleId;
	}
	/**
	 * オーナーロールIDを設定する<BR>
	 * @param ownerRoleId オーナーロールID
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		this.m_ownerRoleId = ownerRoleId;
	}
}