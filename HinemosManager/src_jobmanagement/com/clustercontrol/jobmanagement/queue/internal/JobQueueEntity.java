/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.queue.internal;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;

/**
 * The persistent class for the setting.cc_job_queue database table.
 * 
 * @since 6.2.0
 */
@Entity
@Table(name = "cc_job_queue", schema = "setting")
@Cacheable(true)
@HinemosObjectPrivilege(objectType = HinemosModuleConstant.JOB_QUEUE, isModifyCheck = true)
@AttributeOverride(name = "objectId", column = @Column(name = "queue_id", insertable = false, updatable = false))
public class JobQueueEntity extends ObjectPrivilegeTargetInfo {
	// 実装を変更したときのバージョン番号に合わせる。
	// {major(high)}_{major(low)}_{minor}_{patch}
	private static final long serialVersionUID = 6_02_00_00000000L;

	private String queueId;
	private String name;
	private Integer concurrency;
	private Long regDate;
	private String regUser;
	private Long updateDate;
	private String updateUser;

	@Deprecated
	public JobQueueEntity() {
	}
	
	public JobQueueEntity(String queueId) {
		setQueueId(queueId);
	}
	
	/**
	 * 短い文字列表現を返します。
	 */
	public String toShortString() {
		return "[" + queueId + "," + name + "," + concurrency + "]";
	}

	/*-----------------
	 * getter/setter
	 ----------------*/
	@Id
	@Column(name="queue_id")
	public String getQueueId() {
		return queueId;
	}
	public void setQueueId(String queueId) {
		this.queueId = queueId;
		setObjectId(queueId);
	}

	@Column(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Column(name="concurrency")
	public Integer getConcurrency() {
		return concurrency;
	}
	public void setConcurrency(Integer concurrency) {
		this.concurrency = concurrency;
	}

	@Column(name="reg_date")
	public Long getRegDate() {
		return regDate;
	}
	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	@Column(name="reg_user")
	public String getRegUser() {
		return regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	@Column(name="update_date")
	public Long getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	@Column(name="update_user")
	public String getUpdateUser() {
		return updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}	
}