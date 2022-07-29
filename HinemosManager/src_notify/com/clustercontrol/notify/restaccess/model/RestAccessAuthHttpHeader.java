/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.model;
import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The persistent class for the cc_rest_access_auth_http_header database table.
 * 
 */

@Entity
@Table(name="cc_rest_access_auth_http_header", schema="setting")
@Cacheable(true)
public class RestAccessAuthHttpHeader implements Serializable {

	private static final long serialVersionUID = 1L;
	private RestAccessAuthHttpHeaderPK id ;
	private String key;
	private String value;

	private RestAccessInfo restAccessInfoEntity;

	public RestAccessAuthHttpHeader() {
		super();
	}

	@EmbeddedId
	public RestAccessAuthHttpHeaderPK getId() {
		return id;
	}

	public void setId(RestAccessAuthHttpHeaderPK id) {
		this.id = id;
	}

	@Column(name="key")
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Column(name="value")
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	//bi-directional many-to-one association to RestAccessInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="rest_access_id", insertable=false, updatable=false)
	public RestAccessInfo getRestAccessInfoEntity() {
		return restAccessInfoEntity;
	}

	public void setRestAccessInfoEntity(RestAccessInfo restAccessInfoEntity) {
		this.restAccessInfoEntity = restAccessInfoEntity;
	}

}
