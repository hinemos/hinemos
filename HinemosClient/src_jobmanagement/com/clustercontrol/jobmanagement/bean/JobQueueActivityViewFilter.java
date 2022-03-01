/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

/**
 * ジョブキュー(同時実行制御キュー)の活動状況ビューのフィルタです。
 * 
 * @since 6.2.0
 */
public class JobQueueActivityViewFilter extends JobQueueSettingViewFilter {

	private Integer jobCountFrom;
	private Integer jobCountTo;
	
	@Override
	public String toString() {
		return super.toString()
				+ ", jobCountFrom=" + jobCountFrom
				+ ", jobCountTo=" + jobCountTo;
	}

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
