/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * ジョブマスタが存在しない場合に利用するException
 * @version 3.2.0
 */
public class JobMasterNotFound extends HinemosNotFound {

	private static final long serialVersionUID = -8922714276557399736L;

	// ジョブマスタ共通
	private String m_jobunitId = null;
	private String m_jobId = null;

	// cc_job_relation_mst用
	private String m_parentJobunitId = null;
	private String m_parentJobId = null;

	// cc_start_job_mst用
	private String m_targetJobunitId = null;
	private String m_targetJobId = null;

	private int m_jobType = -100;



	/**
	 * JobNotFoundExceptionコンストラクタ
	 */
	public JobMasterNotFound() {
		super();
	}

	/**
	 * JobNotFoundExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public JobMasterNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * JobNotFoundExceptionコンストラクタ
	 * @param messages
	 */
	public JobMasterNotFound(String messages) {
		super(messages);
	}

	/**
	 * JobNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public JobMasterNotFound(Throwable e) {
		super(e);
	}

	/**
	 * 所属ジョブユニットのジョブIDを返します。
	 * @return 所属ジョブユニットのジョブID
	 */
	public String getJobunitId() {
		return m_jobunitId;
	}

	/**
	 * 所属ジョブユニットのジョブIDを設定します。
	 * @param jobunitId 所属ジョブユニットのジョブID
	 */
	public void setJobunitId(String jobunitId) {
		m_jobunitId = jobunitId;
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
	 * 親ジョブが所属するジョブユニットのジョブIDを返します。
	 * @return 親ジョブが所属するジョブユニットのジョブID
	 */
	public String getParentJobunitId() {
		return m_parentJobunitId;
	}

	/**
	 * 親ジョブが所属するジョブユニットのジョブIDを設定します。
	 * @param parentJobunitId 親ジョブが所属するジョブユニットのジョブID
	 */
	public void setParentJobunitId(String parentJobunitId) {
		m_parentJobunitId = parentJobunitId;
	}

	/**
	 * 親ジョブのジョブIDを返します。
	 * @return 親ジョブのジョブID
	 */
	public String getParentJobId() {
		return m_parentJobId;
	}

	/**
	 * 親ジョブのジョブIDを設定します。
	 * @param parentJobId 親ジョブのジョブID
	 */
	public void setParentJobId(String parentJobId) {
		m_parentJobId = parentJobId;
	}

	public String getTargetJobunitId() {
		return m_targetJobunitId;
	}

	public void setTargetJobunitId(String jobunitId) {
		m_targetJobunitId = jobunitId;
	}

	public String getTargetJobId() {
		return m_targetJobId;
	}

	public void setTargetJobId(String targetJobId) {
		m_targetJobId = targetJobId;
	}

	public int getJobType() {
		return m_jobType;
	}

	public void setJobType(int jobType) {
		m_jobType = jobType;
	}



}
