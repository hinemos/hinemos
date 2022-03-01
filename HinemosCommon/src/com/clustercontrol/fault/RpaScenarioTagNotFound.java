/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * RPAシナリオタグが存在しない場合に利用するException
 * @version 7.0.0
 */
public class RpaScenarioTagNotFound extends HinemosNotFound {

	private static final long serialVersionUID = -650461760049895470L;

	private String m_tagId = null;

	/**
	 * RpaScenarioNotFoundExceptionコンストラクタ
	 */
	public RpaScenarioTagNotFound() {
		super();
	}

	/**
	 * RpaScenarioTagNotFoundExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public RpaScenarioTagNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * RpaScenarioTagNotFoundExceptionコンストラクタ
	 * @param messages
	 */
	public RpaScenarioTagNotFound(String messages) {
		super(messages);
	}

	/**
	 * RpaScenarioTagNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public RpaScenarioTagNotFound(Throwable e) {
		super(e);
	}

	/**
	 * RPAシナリオタグIDを返します。
	 * @return RPAシナリオタグID
	 */
	public String getTagId() {
		return m_tagId;
	}

	/**
	 * RPAシナリオIDを設定します。
	 * @param tagId タグID
	 */
	public void setTagId(String tagId) {
		m_tagId = tagId;
	}

}
