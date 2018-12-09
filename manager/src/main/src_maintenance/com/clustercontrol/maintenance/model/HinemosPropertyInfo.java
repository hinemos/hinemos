/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlType;


/**
 * The persistent class for the cc_hinemos_property database table.
 * 
 */
@XmlType(namespace = "http://maintenance.ws.clustercontrol.com")
@Entity
@Table(name="cc_hinemos_property", schema="setting")
@Cacheable(true)

public class HinemosPropertyInfo {

	private String key;
	private Long createDatetime;
	private String createUserId;
	private String description;
	private Long modifyDatetime;
	private String modifyUserId;
	private String ownerRoleId;
	private Boolean valueBoolean;
	private Long valueNumeric;
	private String valueString;
	private Integer valueType;

	@Deprecated
	public HinemosPropertyInfo() {
	}

	@Id
	@Column(name="property_key")
	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Column(name="create_datetime")
	public Long getCreateDatetime() {
		return this.createDatetime;
	}

	public void setCreateDatetime(Long createDatetime) {
		this.createDatetime = createDatetime;
	}

	@Column(name="create_user_id")
	public String getCreateUserId() {
		return this.createUserId;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}

	@Column(name="description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="modify_datetime")
	public Long getModifyDatetime() {
		return this.modifyDatetime;
	}

	public void setModifyDatetime(Long modifyDatetime) {
		this.modifyDatetime = modifyDatetime;
	}

	@Column(name="modify_user_id")
	public String getModifyUserId() {
		return this.modifyUserId;
	}

	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
	}

	@Column(name="owner_role_id")
	public String getOwnerRoleId() {
		return this.ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	@Column(name="value_boolean")
	public Boolean getValueBoolean() {
		return this.valueBoolean;
	}

	public void setValueBoolean(Boolean valueBoolean) {
		this.valueBoolean = valueBoolean;
	}

	@Column(name="value_numeric")
	public Long getValueNumeric() {
		return this.valueNumeric;
	}

	public void setValueNumeric(Long valueNumeric) {
		this.valueNumeric = valueNumeric;
	}

	@Column(name="value_string")
	public String getValueString() {
		return this.valueString;
	}

	public void setValueString(String valueString) {
		this.valueString = valueString;
	}

	@Column(name="value_type")
	public Integer getValueType() {
		return this.valueType;
	}

	public void setValueType(Integer valueType) {
		this.valueType = valueType;
	}

}