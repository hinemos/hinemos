/*

Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.fault;

/**
 * ジョブが存在しない場合に利用するException
 * @version 3.2.0
 */
public class JobInfoNotFound extends HinemosException {

	private static final long serialVersionUID = -8922714276557399736L;

	private String m_sessionId = null;
	private String m_jobunitId = null;
	private String m_jobId = null;

	private String m_parentJobunitId = null;
	private String m_parentJobId = null;

	private String m_targetJobunitId = null;
	private String m_targetJobId = null;

	private String m_facilityId = null;

	private int m_status = -1;

	private int m_endStatus = -1;

	private int m_noticeType = -1;

	private String m_paramId = null;

	/**
	 * JobNotFoundExceptionコンストラクタ
	 */
	public JobInfoNotFound() {
		super();
	}

	/**
	 * JobNotFoundExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public JobInfoNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * JobNotFoundExceptionコンストラクタ
	 * @param messages
	 */
	public JobInfoNotFound(String messages) {
		super(messages);
	}

	/**
	 * JobNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public JobInfoNotFound(Throwable e) {
		super(e);
	}

	public String getSessionId() {
		return m_sessionId;
	}

	public void setSessionId(String sessionId) {
		m_sessionId = sessionId;
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
	 * 親となるジョブが所属するジョブユニットのジョブIDを返します。
	 * @return 親となるジョブが所属するジョブユニットのジョブID
	 */
	public String getParentJobunitId() {
		return m_parentJobunitId;
	}

	/**
	 * 親となるジョブが所属するジョブユニットのジョブIDを設定します。
	 * @param parentJobunitId 親となるジョブが所属するジョブユニットのジョブID
	 */
	public void setParentJobunitId(String parentJobunitId) {
		m_parentJobunitId = parentJobunitId;
	}

	/**
	 * 親となるジョブのジョブIDを返します。
	 * @return 親となるジョブのジョブID
	 */
	public String getParentJobId() {
		return m_parentJobId;
	}

	/**
	 * 親となるジョブのジョブIDを設定します。
	 * @param parentJobId 親となるジョブのジョブID
	 */
	public void setParentJobId(String parentJobId) {
		m_parentJobId = parentJobId;
	}

	public String getTargetJobunitId() {
		return m_targetJobunitId;
	}

	public void setTargetJobunitId(String targetJobunitId) {
		m_targetJobunitId = targetJobunitId;
	}

	public String getTargetJobId() {
		return m_targetJobId;
	}

	public void setTargetJobId(String targetJobId) {
		m_targetJobId = targetJobId;
	}

	/**
	 * ジョブの実行状態を取得します。
	 * @return ジョブの実行状態
	 */
	public int getStatus() {
		return m_status;
	}

	/**
	 * ジョブの実行状態を設定します。
	 * @param status ジョブの実行状態
	 */
	public void setStatus(int status) {
		this.m_status = status;
	}

	/**
	 * ジョブの終了状態を返します。
	 * @return ジョブの終了状態
	 */
	public int getEndStatus() {
		return m_endStatus;
	}

	/**
	 * ジョブの終了状態を設定します。
	 * @param status ジョブの終了状態
	 */
	public void setEndStatus(int status) {
		m_endStatus = status;
	}

	/**
	 * ファシリティIDを返します。
	 * @return ファシリティID
	 */
	public String getFacilityId() {
		return m_facilityId;
	}

	/**
	 * ファシリティIDを設定します。
	 * @param facilityId ファシリティIDを返します。
	 */
	public void setFacilityId(String facilityId) {
		m_facilityId = facilityId;
	}

	/**
	 * 終了状態を取得します。
	 * @return 終了状態
	 */
	public int getNoticeType() {
		return m_noticeType;
	}

	/**
	 * 終了状態を設定します。
	 * @param noticeType 終了状態
	 */
	public void setNoticeType(int noticeType) {
		m_noticeType = noticeType;
	}

	/**
	 * パラメータIDを返します。
	 * @return パラメータID
	 */
	public String getParamId() {
		return m_paramId;
	}

	/**
	 * パラメータIDを設定します。
	 * @param paramId パラメータID
	 */
	public void setParamId(String paramId) {
		m_paramId = paramId;
	}




}
