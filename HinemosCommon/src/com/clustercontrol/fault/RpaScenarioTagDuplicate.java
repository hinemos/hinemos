/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * RpaScenarioTagが重複している場合に利用するException
 * @version 7.0.0
 */
public class RpaScenarioTagDuplicate extends HinemosDuplicate {

	private static final long serialVersionUID = 9063996099263346415L;
	
	private String m_tagId = null;

	/**
	 * RpaScenarioTagDuplicateコンストラクタ
	 */
	public RpaScenarioTagDuplicate() {
		super();
	}

	/**
	 * RpaScenarioTagDuplicateコンストラクタ
	 * @param messages
	 */
	public RpaScenarioTagDuplicate(String messages) {
		super(messages);
	}

	/**
	 * RpaScenarioTagDuplicateコンストラクタ
	 * @param e
	 */
	public RpaScenarioTagDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * RpaScenarioTagDuplicateコンストラクタ
	 * @param messages
	 * @param e
	 */
	public RpaScenarioTagDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getTagId() {
		return m_tagId;
	}

	public void setTagId(String tagId) {
		m_tagId = tagId;
	}
}
