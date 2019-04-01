/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.queue.bean;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.bean.JobQueueConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * ジョブキュー(同時実行制御キュー)の設定ビューのフィルタです。
 * 
 * @since 6.2.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobQueueSettingViewFilter implements Serializable {

	// 実装を変更したときのバージョン番号に合わせる。 {major(high)}_{major(low)}_{minor}_{patch}
	private static final long serialVersionUID = 6_02_00_00000000L;

	private String queueId;
	private String queueName;
	private Integer concurrencyFrom;
	private Integer concurrencyTo;
	private String ownerRoleId;
	private String regUser;
	private Long regDateFrom;
	private Long regDateTo;
	private String updateUser;
	private Long updateDateFrom;
	private Long updateDateTo;

	/**
	 * 設定内容について書式的な観点での検証を行います。
	 * <p>
	 * Web APIではサポートされません。
	 * 
	 * @throws InvalidSetting 検証エラーがあった場合に、ユーザ向けメッセージを設定して投げます。
	 */
	public void validate() throws InvalidSetting {
		// LIKE検索項目の入力値は、"NOT:%...%"のように指定された場合、最大でデータ長 + 6文字となる可能性があるが、
		// イベント履歴フィルタダイアログを見た限りでは、それも含めてデータ長で入力制限を行っているようなので、それに倣う。
		CommonValidator.validateString(MessageConstant.JOBQUEUE_ID.getMessage(), queueId, false, 0,
				JobQueueConstant.ID_MAXLEN);
		CommonValidator.validateString(MessageConstant.JOBQUEUE_NAME.getMessage(), queueName, false, 0,
				JobQueueConstant.NAME_MAXLEN);
		CommonValidator.validateNullableInt(MessageConstant.JOBQUEUE_CONCURRENCY.getMessage(), concurrencyFrom,
				JobQueueConstant.CONCURRENCY_MIN, JobQueueConstant.CONCURRENCY_MAX);
		CommonValidator.validateNullableInt(MessageConstant.JOBQUEUE_CONCURRENCY.getMessage(), concurrencyTo,
				JobQueueConstant.CONCURRENCY_MIN, JobQueueConstant.CONCURRENCY_MAX);
		CommonValidator.validateString(MessageConstant.OWNER_ROLE_ID.getMessage(), ownerRoleId, false, 0,
				DataRangeConstant.OWNER_ROLE_ID_MAXLEN);
		CommonValidator.validateString(MessageConstant.REG_USER_ID.getMessage(), regUser, false, 0,
				DataRangeConstant.USER_ID_MAXLEN);
		CommonValidator.validateString(MessageConstant.UPDATE_USER_ID.getMessage(), updateUser, false, 0,
				DataRangeConstant.USER_ID_MAXLEN);
	}

	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		sdf.setTimeZone(HinemosTime.getTimeZone());
		return "queueId=" + queueId
				+ ", queueName=" + queueName
				+ ", concurrencyFrom=" + concurrencyFrom
				+ ", concurrencyTo=" + concurrencyTo
				+ ", ownerRoleId=" + ownerRoleId
				+ ", regUser=" + regUser
				+ ", regDateFrom=" + dateStr(sdf, regDateFrom)
				+ ", regDateTo=" + dateStr(sdf, regDateTo)
				+ ", updateUser=" + updateUser
				+ ", updateDateFrom=" + dateStr(sdf, updateDateFrom)
				+ ", updateDateTo=" + dateStr(sdf, updateDateTo);
	}
	
	private String dateStr(DateFormat df, Long date) {
		if (date == null) return "null";
		return df.format(new Date(date));
	}

	// -----------------
	// getters & setters
	// -----------------
	
	public String getQueueId() {
		return queueId;
	}

	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	
	public Integer getConcurrencyFrom() {
		return concurrencyFrom;
	}

	public void setConcurrencyFrom(Integer concurrencyFrom) {
		this.concurrencyFrom = concurrencyFrom;
	}

	public Integer getConcurrencyTo() {
		return concurrencyTo;
	}

	public void setConcurrencyTo(Integer concurrencyTo) {
		this.concurrencyTo = concurrencyTo;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public Long getRegDateFrom() {
		return regDateFrom;
	}

	public void setRegDateFrom(Long regDateFrom) {
		this.regDateFrom = regDateFrom;
	}

	public Long getRegDateTo() {
		return regDateTo;
	}

	public void setRegDateTo(Long regDateTo) {
		this.regDateTo = regDateTo;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Long getUpdateDateFrom() {
		return updateDateFrom;
	}

	public void setUpdateDateFrom(Long updateDateFrom) {
		this.updateDateFrom = updateDateFrom;
	}

	public Long getUpdateDateTo() {
		return updateDateTo;
	}

	public void setUpdateDateTo(Long updateDateTo) {
		this.updateDateTo = updateDateTo;
	}
}
