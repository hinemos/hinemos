/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * ジョブマスタが重複している場合に利用するException
 * 
 * @version 4.1.0
 */
public class JobMasterDuplicate extends HinemosDuplicate {

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
