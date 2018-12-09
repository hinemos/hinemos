/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * DbmsSchedulerが存在しない場合に利用するException
 * @version 5.1.0
 */
public class DbmsSchedulerNotFound extends HinemosException {

	private static final long serialVersionUID = -1L;

	private String m_jobId = null;
	private String m_jobGroup = null;

	/**
	 * DbmsSchedulerNotFoundコンストラクタ
	 */
	public DbmsSchedulerNotFound() {
		super();
	}

	/**
	 * DbmsSchedulerNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public DbmsSchedulerNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * DbmsSchedulerNotFoundコンストラクタ
	 * @param messages
	 */
	public DbmsSchedulerNotFound(String messages) {
		super(messages);
	}

	/**
	 * DbmsSchedulerNotFoundコンストラクタ
	 * @param e
	 */
	public DbmsSchedulerNotFound(Throwable e) {
		super(e);
	}

	/**
	 * ジョブIDを返します。
	 * @return ジョブID
	 */
	public String getJobId() {
		return m_jobId;
	}

	/**
	 * ジョブIDを設定します。
	 * @param jobId ジョブID
	 */
	public void setJobId(String jobId) {
		m_jobId = jobId;
	}

	/**
	 * ジョブを返します。
	 * @return ジョブグループ
	 */
	public String getJobGroup() {
		return m_jobGroup;
	}
	/**
	 * ジョブグループを設定します。
	 * @param jobGroup ジョブグループ
	 */
	public void setJobGroup(String jobGroup) {
		m_jobGroup = jobGroup;
	}

}
