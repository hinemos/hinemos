/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * The persistent class for the cc_log_async_task database table.
 * 
 */
@Entity
@Table(name="cc_log_async_task", schema="setting")
@Cacheable(false)
public class AsyncTaskEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private AsyncTaskEntityPK id;
	private Long createDatetime;
	private byte[] param;

	@Deprecated
	public AsyncTaskEntity() {
	}

	public AsyncTaskEntity(AsyncTaskEntityPK pk) {
		this.setId(pk);
	}

	public AsyncTaskEntity(String worker, Long taskId) {
		this(new AsyncTaskEntityPK(worker, taskId));
	}


	@EmbeddedId
	public AsyncTaskEntityPK getId() {
		return this.id;
	}

	public void setId(AsyncTaskEntityPK id) {
		this.id = id;
	}


	@Column(name="create_datetime")
	public Long getCreateDatetime() {
		return this.createDatetime;
	}

	public void setCreateDatetime(Long createDatetime) {
		this.createDatetime = createDatetime;
	}


	@Column(name="param")
	public byte[] getParam() {
		return this.param;
	}

	public void setParam(byte[] param) {
		this.param = param;
	}

}