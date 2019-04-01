/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.update;

import com.clustercontrol.fault.HinemosException;

/**
 * ファイル展開の失敗を表します。
 * 
 * @since 6.2.0
 */
public class ExtractFailureException extends HinemosException {

	private static final long serialVersionUID = 1L;

	public ExtractFailureException() {
		super();
	}

	public ExtractFailureException(String messages, Throwable e) {
		super(messages, e);
	}

	public ExtractFailureException(String messages) {
		super(messages);
	}

	public ExtractFailureException(Throwable e) {
		super(e);
	}
}
