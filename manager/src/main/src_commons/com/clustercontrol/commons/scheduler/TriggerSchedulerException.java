/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.scheduler;

import com.clustercontrol.fault.HinemosException;

/**
 * スケジュールの登録に失敗した場合に利用するException
 */
public class TriggerSchedulerException extends HinemosException {

	private static final long serialVersionUID = -1156559497977250520L;

	private String _jobName = "not initialized";
	private String _jobGroupName = "not initialized";

	/**
	 * TriggerSchedulerExceptionコンストラクタ
	 */
	public TriggerSchedulerException() {
		super();
	}

	/**
	 * TriggerSchedulerExceptionコンストラクタ
	 * @param messages
	 */
	public TriggerSchedulerException(String messages) {
		super(messages);
	}

	/**
	 * TriggerSchedulerExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public TriggerSchedulerException(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * TriggerSchedulerExceptionコンストラクタ
	 * @param messages
	 * @param e
	 * @param jobName ジョブ名
	 * @param jobGroupName ジョブグループ名
	 */
	public TriggerSchedulerException(String messages, Throwable e, String jobName, String jobGroupName) {
		super(messages, e);
		_jobName = jobName;
		_jobGroupName = jobGroupName;
	}

	/**
	 * ジョブ名を返します。
	 * @return ジョブ名
	 */
	public String getJobName() {
		return _jobName;
	}

	/**
	 * ジョブグループ名を返します。
	 * @return ジョブグループ名
	 */
	public String getJobGroupName() {
		return _jobGroupName;
	}
}
