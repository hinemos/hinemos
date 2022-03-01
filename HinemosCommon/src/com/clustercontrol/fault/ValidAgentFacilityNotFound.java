/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * エージェントがマネージャで認識されていない場合に利用するException
 */
public class ValidAgentFacilityNotFound extends HinemosNotFound {

	private static final long serialVersionUID = -8025127362014513000L;

	/**
	 * ValidAgentFacilityNotFoundコンストラクタ
	 */
	public ValidAgentFacilityNotFound() {
		super();
	}

	/**
	 * ValidAgentFacilityNotFoundコンストラクタ
	 * @param messages
	 */
	public ValidAgentFacilityNotFound(String messages) {
		super(messages);
	}

	/**
	 * ValidAgentFacilityNotFoundコンストラクタ
	 * @param e
	 */
	public ValidAgentFacilityNotFound(Throwable e) {
		super(e);
	}

	/**
	 * ValidAgentFacilityNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public ValidAgentFacilityNotFound(String messages, Throwable e) {
		super(messages, e);
	}
}
