/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.fault;

/**
 * restAccessIDが重複している場合に利用するException
 */
public class RestAccessDuplicate extends HinemosDuplicate {

	private static final long serialVersionUID = -2319313621650787627L;

	/**
	 * RestAccessDuplicateコンストラクタ
	 */
	public RestAccessDuplicate() {
		super();
	}

	/**
	 * RestAccessDuplicateコンストラクタ
	 * @param messages
	 */
	public RestAccessDuplicate(String messages) {
		super(messages);
	}

	/**
	 * RestAccessDuplicateコンストラクタ
	 * @param e
	 */
	public RestAccessDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * RestAccessDuplicateコンストラクタ
	 * @param messages
	 * @param e
	 */
	public RestAccessDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

}
