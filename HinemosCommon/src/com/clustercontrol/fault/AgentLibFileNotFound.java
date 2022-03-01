/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * エージェントのライブラリファイルが存在しなかったことを表します。
 *
 */
public class AgentLibFileNotFound extends HinemosNotFound {

    private static final long serialVersionUID = -4633992198669624652L;

	public AgentLibFileNotFound() {
		super();
	}

	public AgentLibFileNotFound(String message) {
		super(message);
	}

	public AgentLibFileNotFound(Throwable cause) {
		super(cause);
	}

	public AgentLibFileNotFound(String message, Throwable cause) {
		super(message, cause);
	}
}
