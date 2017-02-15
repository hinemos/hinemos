/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.plugin.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


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
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
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


	public byte[] getParam() {
		return this.param;
	}

	public void setParam(byte[] param) {
		this.param = param;
	}

}