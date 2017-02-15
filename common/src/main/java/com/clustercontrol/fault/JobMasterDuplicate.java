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
 * ジョブマスタが重複している場合に利用するException
 * 
 * @version 4.1.0
 */
public class JobMasterDuplicate extends HinemosException {

	private static final long serialVersionUID = 4861893779155148491L;
	private String m_id = null;

	/**
	 * JobMasterDuplicateExceptionコンストラクタ
	 */
	public JobMasterDuplicate() {
		super();
	}

	/**
	 * JobMasterDuplicateExceptionコンストラクタ
	 * @param messages
	 */
	public JobMasterDuplicate(String messages) {
		super(messages);
	}

	/**
	 * JobMasterDuplicateExceptionコンストラクタ
	 * @param e
	 */
	public JobMasterDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * JobMasterDuplicateExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public JobMasterDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getJobMasterId() {
		return m_id;
	}

	public void setJobMasterId(String id) {
		m_id = id;
	}
}
