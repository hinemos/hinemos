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
 * jobKicKIDが重複している場合に利用するException
 * @version 4.1.0
 */
public class JobKickDuplicate extends HinemosException {

	private static final long serialVersionUID = -3093747190605885555L;
	private String m_jobKickId = null;

	/**
	 * JobKickDuplicateExceptionコンストラクタ
	 */
	public JobKickDuplicate() {
		super();
	}

	/**
	 * JobKickDuplicateExceptionコンストラクタ
	 * @param messages
	 */
	public JobKickDuplicate(String messages) {
		super(messages);
	}

	/**
	 * JobKickDuplicateExceptionコンストラクタ
	 * @param e
	 */
	public JobKickDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * JobKickDuplicateExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public JobKickDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getJobKickId() {
		return m_jobKickId;
	}

	public void setJobKIckId(String jobKickId) {
		m_jobKickId = jobKickId;
	}
}
