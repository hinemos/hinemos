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

import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.bean.JobQueueConstant;
import com.clustercontrol.util.MessageConstant;

/**
 * ジョブキュー(同時実行制御キュー)の活動状況ビューのフィルタです。
 * 
 * @since 6.2.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobQueueActivityViewFilter extends JobQueueSettingViewFilter implements Serializable {

	// 実装を変更したときのバージョン番号に合わせる。 {major(high)}_{major(low)}_{minor}_{patch}
	private static final long serialVersionUID = 6_02_00_00000000L;

	private Integer jobCountFrom;
	private Integer jobCountTo;

	/**
	 * 設定内容について書式的な観点での検証を行います。
	 * <p>
	 * Web APIではサポートされません。
	 * 
	 * @throws InvalidSetting 検証エラーがあった場合に、ユーザ向けメッセージを設定して投げます。
	 */
	public void validate() throws InvalidSetting {
		super.validate();
		CommonValidator.validateNullableInt(MessageConstant.JOBQUEUE_JOB_COUNT.getMessage(), jobCountFrom,
				0, JobQueueConstant.CONCURRENCY_MAX);
		CommonValidator.validateNullableInt(MessageConstant.JOBQUEUE_JOB_COUNT.getMessage(), jobCountTo,
				0, JobQueueConstant.CONCURRENCY_MAX);
	}
	
	@Override
	public String toString() {
		return super.toString()
				+ ", jobCountFrom=" + jobCountFrom
				+ ", jobCountTo=" + jobCountTo;
	}

	// -----------------
	// getters & setters
	// -----------------
	
	public Integer getJobCountFrom() {
		return jobCountFrom;
	}

	public void setJobCountFrom(Integer jobCountFrom) {
		this.jobCountFrom = jobCountFrom;
	}

	public Integer getJobCountTo() {
		return jobCountTo;
	}

	public void setJobCountTo(Integer jobCountTo) {
		this.jobCountTo = jobCountTo;
	}
}
