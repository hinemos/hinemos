/*

Copyright (C) 2013 NTT DATA Corporation

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
 * JobSessionIDが重複している場合に利用するException
 * @version 4.1.0
 */
public class JobSessionDuplicate extends HinemosException {

	private static final long serialVersionUID = -3292533279221432680L;
	private String m_jobSessionId = null;

	/**
	 * JobSessionDuplicateExceptionコンストラクタ
	 */
	public JobSessionDuplicate() {
		super();
	}

	/**
	 * JobSessionDuplicateExceptionコンストラクタ
	 * @param messages
	 */
	public JobSessionDuplicate(String messages) {
		super(messages);
	}

	/**
	 * JobSessionDuplicateExceptionコンストラクタ
	 * @param e
	 */
	public JobSessionDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * JobSessionDuplicateExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public JobSessionDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getJobSessionId() {
		return m_jobSessionId;
	}

	public void setJobSessionId(String jobSessionId) {
		m_jobSessionId = jobSessionId;
	}
}
