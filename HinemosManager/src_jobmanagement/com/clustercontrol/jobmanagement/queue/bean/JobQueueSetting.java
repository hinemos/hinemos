/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.queue.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.bean.JobQueueConstant;
import com.clustercontrol.jobmanagement.queue.internal.JobQueueEntity;
import com.clustercontrol.util.MessageConstant;

/**
 * ジョブキュー(同時実行制御キュー)の設定内容をやりとりするためのクラスです。
 * 
 * @since 6.2.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobQueueSetting implements Serializable {

	// 実装を変更したときのバージョン番号に合わせる。 {major(high)}_{major(low)}_{minor}_{patch}
	private static final long serialVersionUID = 6_02_00_00000000L;

	private String queueId;
	private String name;
	private Integer concurrency;
	private String ownerRoleId;

	public JobQueueSetting() {
	}

	/**
	 * エンティティを元にインスタンスを生成します。
	 * <p>
	 * Web APIではサポートされません。
	 */
	public JobQueueSetting(JobQueueEntity entity) {
		if (entity == null) return;
		queueId = entity.getQueueId();
		name = entity.getName();
		concurrency = entity.getConcurrency();
		ownerRoleId = entity.getOwnerRoleId();
	}
	
	/**
	 * 設定内容について書式的な観点での検証を行います。
	 * リレーションなどの複雑な観点での検証は行いません。
	 * <p>
	 * Web APIではサポートされません。
	 * 
	 * @throws InvalidSetting 検証エラーがあった場合に、ユーザ向けメッセージを設定して投げます。
	 */
	public void validate() throws InvalidSetting {
		CommonValidator.validateId(MessageConstant.JOBQUEUE_ID.getMessage(), queueId, JobQueueConstant.ID_MAXLEN);
		CommonValidator.validateString(MessageConstant.JOBQUEUE_NAME.getMessage(), name, true, 1,
				JobQueueConstant.NAME_MAXLEN);
		CommonValidator.validateInt(MessageConstant.JOBQUEUE_CONCURRENCY.getMessage(), concurrency,
				JobQueueConstant.CONCURRENCY_MIN, JobQueueConstant.CONCURRENCY_MAX);
		CommonValidator.validateId(MessageConstant.OWNER_ROLE_ID.getMessage(), ownerRoleId,
				DataRangeConstant.OWNER_ROLE_ID_MAXLEN);
	}
	
	// ---------------
	// getters/setters
	// ---------------
	
	public String getQueueId() {
		return queueId;
	}

	/**
	 * キューIDを設定します。
	 */
	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

	public String getName() {
		return name;
	}

	/**
	 * キュー名を設定します。
	 */
	public void setName(String name) {
		this.name = name;
	}

	public Integer getConcurrency() {
		return concurrency;
	}

	/**
	 * 同時実行可能数を設定します。
	 */
	public void setConcurrency(Integer concurrency) {
		this.concurrency = concurrency;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	/**
	 * オーナーロールを設定します。
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

}